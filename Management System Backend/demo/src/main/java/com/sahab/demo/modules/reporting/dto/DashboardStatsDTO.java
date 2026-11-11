package com.sahab.demo.modules.reporting.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class DashboardStatsDTO {

    private long totalRequests;
    private long pendingRequests;
    private long approvedRequests;
    private long rejectedRequests;

    private long itRequests;
    private long fleetRequests;
    private long leaveRequests;
    private long hrRequests;
    private long generalRequests;
    private long remoteWorkRequests;
}