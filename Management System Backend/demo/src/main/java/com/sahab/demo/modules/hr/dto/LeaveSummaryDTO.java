package com.sahab.demo.modules.hr.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LeaveSummaryDTO {

    private int annualLeaveAllowance;
    private int annualLeaveUsed;
    private int annualLeaveRemaining;
    private double annualLeavePercentage;

    private int remoteWorkMonthlyAllowance;
    private int remoteWorkUsed;
    private int remoteWorkRemaining;
    private double remoteWorkPercentage;

    private int currentMonth;
    private int currentYear;
}