package com.sahab.demo.modules.departments.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentDetailsDTO {

    // =========================================================
    // DEPARTMENT
    // =========================================================

    private Long id;

    private String name;


    // =========================================================
    // PEOPLE
    // =========================================================

    private List<ManagerDTO> managers;

    private List<EmployeeDTO> employees;


    // =========================================================
    // COUNTS
    // =========================================================

    private long employeeCount;

    private long requestCount;


    // =========================================================
    // SLA
    // =========================================================

    private SlaDTO sla;


    // =========================================================
    // ANALYTICS
    // =========================================================

    private AnalyticsDTO analytics;


    // =========================================================
    // MANAGER DTO
    // =========================================================

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ManagerDTO {

        private Long id;

        private String name;

        private String email;
    }


    // =========================================================
    // EMPLOYEE DTO
    // =========================================================

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmployeeDTO {

        private Long id;

        private String name;

        private String email;

        private String role;
    }


    // =========================================================
    // SLA DTO
    // =========================================================

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SlaDTO {

        private Integer durationHours;

        private Integer warningHours;

        private Boolean active;
    }


    // =========================================================
    // ANALYTICS DTO
    // =========================================================

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnalyticsDTO {

        private long pending;

        private long approved;

        private long rejected;

        private long slaWarning;

        private long slaBreached;

        private double slaCompliancePercentage;
    }
}