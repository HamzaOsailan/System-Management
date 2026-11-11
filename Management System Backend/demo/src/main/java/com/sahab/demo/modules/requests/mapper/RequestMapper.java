package com.sahab.demo.modules.requests.mapper;

import com.sahab.demo.modules.requests.dto.CreateRequestDTO;
import com.sahab.demo.modules.requests.dto.RequestResponse;
import com.sahab.demo.entity.Request;
import com.sahab.demo.entity.User;
import com.sahab.demo.enums.RequestStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class RequestMapper {

    // =========================================================
    // DTO -> ENTITY
    // =========================================================

    public Request toEntity(
            CreateRequestDTO dto,
            User user
    ) {

        Request request = new Request();

        request.setTitle(
                dto.getTitle()
        );

        request.setDescription(
                dto.getDescription()
        );

        request.setCategory(
                dto.getCategory()
        );

        request.setStatus(
                RequestStatus.PENDING
        );

        request.setUser(
                user
        );

        LocalDateTime now =
                LocalDateTime.now();

        request.setCreatedAt(
                now
        );

        request.setUpdatedAt(
                now
        );

        return request;
    }

    // =========================================================
    // ENTITY -> RESPONSE DTO
    // =========================================================

    public RequestResponse toResponse(
            Request request
    ) {

        RequestResponse response =
                new RequestResponse();

        // =====================================================
        // BASIC
        // =====================================================

        response.setId(
                request.getId()
        );

        response.setTitle(
                request.getTitle()
        );

        response.setDescription(
                request.getDescription()
        );

        // =====================================================
        // STATUS
        // =====================================================

        if (request.getStatus() != null) {

            response.setStatus(
                    request.getStatus().name()
            );
        }

        // =====================================================
        // REQUEST OWNER
        // =====================================================

        if (request.getUser() != null) {

            User user =
                    request.getUser();

            response.setUserId(
                    user.getId()
            );

            response.setUserName(
                    user.getName()
            );

            response.setUserEmail(
                    user.getEmail()
            );
        }

        // =====================================================
        // DATES
        // =====================================================

        response.setCreatedAt(
                request.getCreatedAt()
        );

        response.setUpdatedAt(
                request.getUpdatedAt()
        );

        // =====================================================
        // CATEGORY
        // =====================================================

        if (request.getCategory() != null) {

            response.setCategory(
                    request.getCategory().name()
            );
        }

        // =====================================================
        // PRIORITY
        // =====================================================

        if (request.getPriority() != null) {

            response.setPriority(
                    request.getPriority().name()
            );
        }

        // =====================================================
        // DURATION
        // =====================================================

        response.setDurationDays(
                request.getDurationDays()
        );

        // =====================================================
        // APPROVAL
        // =====================================================

        response.setApprovedBy(
                request.getApprovedBy()
        );

        response.setApprovedAt(
                request.getApprovedAt()
        );

        // =====================================================
        // SLA
        // =====================================================

        response.setSlaDeadline(
                request.getSlaDeadline()
        );

        response.setSlaWarningAt(
                request.getSlaWarningAt()
        );

        response.setSlaBreached(
                request.getSlaBreached()
        );

        response.setSlaWarningSent(
                request.getSlaWarningSent()
        );

        response.setSlaEscalated(
                request.getSlaEscalated()
        );

        return response;
    }
}