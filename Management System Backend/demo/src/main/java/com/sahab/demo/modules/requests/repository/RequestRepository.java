package com.sahab.demo.modules.requests.repository;

import com.sahab.demo.entity.Request;
import com.sahab.demo.entity.User;
import com.sahab.demo.enums.RequestCategory;
import com.sahab.demo.enums.RequestStatus;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RequestRepository
        extends JpaRepository<Request, Long> {

        // =========================================================
        // LOCKED REQUEST
        // =========================================================

        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("""
        SELECT r
        FROM Request r
        WHERE r.id = :id
    """)
        Optional<Request> findByIdForUpdate(
                @Param("id") Long id
        );

        // =========================================================
        // BASIC REQUEST QUERIES
        // =========================================================

        List<Request> findByUser(User user);

        List<Request> findByStatus(RequestStatus status);

        long countByStatus(RequestStatus status);

        long countByCategory(RequestCategory category);

        long countByCategoryAndStatus(
                RequestCategory category,
                RequestStatus status
        );

        List<Request> findByUserAndCategoryAndStatus(
                User user,
                RequestCategory category,
                RequestStatus status
        );

        // =========================================================
        // LEAVE / DURATION CALCULATIONS
        // =========================================================

        @Query("""
        SELECT COALESCE(SUM(r.durationDays), 0)
        FROM Request r
        WHERE r.user = :user
          AND r.category = :category
          AND r.status = :status
          AND YEAR(r.approvedAt) = :year
    """)
        Integer sumApprovedDurationByUserAndCategoryAndYear(
                @Param("user") User user,
                @Param("category") RequestCategory category,
                @Param("status") RequestStatus status,
                @Param("year") int year
        );

        @Query("""
        SELECT COALESCE(SUM(r.durationDays), 0)
        FROM Request r
        WHERE r.user = :user
          AND r.category = :category
          AND r.status = :status
          AND YEAR(r.approvedAt) = :year
          AND MONTH(r.approvedAt) = :month
    """)
        Integer sumApprovedDurationByUserAndCategoryAndMonth(
                @Param("user") User user,
                @Param("category") RequestCategory category,
                @Param("status") RequestStatus status,
                @Param("year") int year,
                @Param("month") int month
        );

        // =========================================================
        // MANAGER REQUESTS
        // =========================================================

        List<Request> findByManagerId(Long managerId);

        List<Request> findByUserIdOrderByCreatedAtDesc(Long userId);

        // =========================================================
        // SLA - WARNING
        // =========================================================

        @Query("""
        SELECT r
        FROM Request r
        WHERE r.status = :status
          AND r.slaWarningAt IS NOT NULL
          AND r.slaWarningAt <= :now
          AND r.slaDeadline IS NOT NULL
          AND r.slaDeadline > :now
          AND (r.slaWarningSent = false OR r.slaWarningSent IS NULL)
    """)
        List<Request> findRequestsReachingSlaWarning(
                @Param("status") RequestStatus status,
                @Param("now") LocalDateTime now
        );

        // =========================================================
        // SLA - BREACHED
        // =========================================================

        @Query("""
        SELECT r
        FROM Request r
        WHERE r.status = :status
          AND r.slaDeadline IS NOT NULL
          AND r.slaDeadline <= :now
          AND (r.slaBreached = false OR r.slaBreached IS NULL)
    """)
        List<Request> findRequestsPastSlaDeadline(
                @Param("status") RequestStatus status,
                @Param("now") LocalDateTime now
        );

        // =========================================================
        // SLA - PENDING WITH SLA
        // =========================================================

        @Query("""
        SELECT r
        FROM Request r
        WHERE r.status = :status
          AND r.slaDeadline IS NOT NULL
    """)
        List<Request> findPendingRequestsWithSla(
                @Param("status") RequestStatus status
        );

        // =========================================================
        // DEPARTMENT ANALYTICS
        // =========================================================

        /**
         * Total requests belonging to a department.
         *
         * Department is determined through:
         *
         * Request -> User -> Department
         */
        @Query("""
        SELECT COUNT(r)
        FROM Request r
        WHERE r.user.department.id = :departmentId
    """)
        long countByDepartmentId(
                @Param("departmentId") Long departmentId
        );

        /**
         * Requests belonging to a department
         * filtered by status.
         */
        @Query("""
        SELECT COUNT(r)
        FROM Request r
        WHERE r.user.department.id = :departmentId
          AND r.status = :status
    """)
        long countByDepartmentIdAndStatus(
                @Param("departmentId") Long departmentId,
                @Param("status") RequestStatus status
        );

        // =========================================================
        // DEPARTMENT SLA - BREACHED
        // =========================================================

        /**
         * Number of currently pending requests
         * that have passed their SLA deadline.
         */
        @Query("""
        SELECT COUNT(r)
        FROM Request r
        WHERE r.user.department.id = :departmentId
          AND r.status = :status
          AND r.slaDeadline IS NOT NULL
          AND r.slaDeadline <= :now
    """)
        long countBreachedRequestsByDepartment(
                @Param("departmentId") Long departmentId,
                @Param("status") RequestStatus status,
                @Param("now") LocalDateTime now
        );

        // =========================================================
        // DEPARTMENT SLA - WARNING
        // =========================================================

        /**
         * Number of currently pending requests
         * that are inside the SLA warning window.
         */
        @Query("""
        SELECT COUNT(r)
        FROM Request r
        WHERE r.user.department.id = :departmentId
          AND r.status = :status
          AND r.slaWarningAt IS NOT NULL
          AND r.slaWarningAt <= :now
          AND r.slaDeadline IS NOT NULL
          AND r.slaDeadline > :now
    """)
        long countWarningRequestsByDepartment(
                @Param("departmentId") Long departmentId,
                @Param("status") RequestStatus status,
                @Param("now") LocalDateTime now
        );

        // =========================================================
        // DEPARTMENT SLA - COMPLETED REQUESTS
        // =========================================================

        /**
         * Number of completed requests that had an SLA deadline.
         *
         * A completed request is either:
         *
         * APPROVED
         * REJECTED
         *
         * completedAt is required because it represents
         * the actual final workflow completion time.
         */
        @Query("""
        SELECT COUNT(r)
        FROM Request r
        WHERE r.user.department.id = :departmentId
          AND r.status IN (
              com.sahab.demo.enums.RequestStatus.APPROVED,
              com.sahab.demo.enums.RequestStatus.REJECTED
          )
          AND r.slaDeadline IS NOT NULL
          AND r.completedAt IS NOT NULL
    """)
        long countCompletedRequestsWithSlaByDepartment(
                @Param("departmentId") Long departmentId
        );

        /**
         * Number of completed requests that finished
         * within their SLA deadline.
         */
        @Query("""
        SELECT COUNT(r)
        FROM Request r
        WHERE r.user.department.id = :departmentId
          AND r.status IN (
              com.sahab.demo.enums.RequestStatus.APPROVED,
              com.sahab.demo.enums.RequestStatus.REJECTED
          )
          AND r.slaDeadline IS NOT NULL
          AND r.completedAt IS NOT NULL
          AND r.completedAt <= r.slaDeadline
    """)
        long countCompletedRequestsWithinSlaByDepartment(
                @Param("departmentId") Long departmentId
        );
}