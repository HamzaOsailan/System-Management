package com.sahab.demo.modules.hr.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LeaveBalanceDTO {

    private int currentYear;

    // ==============================
    // Annual Leave
    // ==============================

    private int annualLeaveLimit;

    private int annualLeaveUsed;

    private int annualLeaveRemaining;

    private double annualLeavePercentage;

    // ==============================
    // Remote Work
    // ==============================

    private int remoteWorkMonthlyLimit;

    private int remoteWorkUsed;

    private int remoteWorkRemaining;

    private double remoteWorkPercentage;

    // ==============================
    // Current Month
    // ==============================

    private int currentMonth;
}