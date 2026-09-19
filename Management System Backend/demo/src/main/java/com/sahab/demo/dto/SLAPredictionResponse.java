package com.sahab.demo.dto;

import com.sahab.demo.enums.SLAStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SLAPredictionResponse {

    private Long requestId;

    private String title;

    private String category;

    private String priority;

    private String status;

    private double slaHours;

    private double elapsedHours;

    private long remainingMinutes;

    private double breachProbability;

    private SLAStatus riskLevel;

    private List<String> reasons;
}