package com.sahab.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SLADashboardResponse {

    private long withinSLA;

    private long atRisk;

    private long breached;

    private long totalRequests;

    private List<SLAPredictionResponse> criticalRequests;
}