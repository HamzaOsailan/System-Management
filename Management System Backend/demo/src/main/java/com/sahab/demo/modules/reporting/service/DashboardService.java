package com.sahab.demo.modules.reporting.service;

import com.sahab.demo.modules.reporting.dto.DashboardStatsDTO;
import com.sahab.demo.enums.RequestCategory;
import com.sahab.demo.enums.RequestStatus;
import com.sahab.demo.repository.RequestRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

    private final RequestRepository requestRepository;

    public DashboardService(
            RequestRepository requestRepository
    ) {
        this.requestRepository = requestRepository;
    }

    @Cacheable(
            value = "dashboardStats",
            key = "'all'"
    )
    public DashboardStatsDTO getStats() {

        long total =
                requestRepository.count();

        long pending =
                requestRepository.countByStatus(
                        RequestStatus.PENDING
                );

        long approved =
                requestRepository.countByStatus(
                        RequestStatus.APPROVED
                );

        long rejected =
                requestRepository.countByStatus(
                        RequestStatus.REJECTED
                );

        long it =
                requestRepository.countByCategory(
                        RequestCategory.IT
                );

        long fleet =
                requestRepository.countByCategory(
                        RequestCategory.FLEET
                );

        long leave =
                requestRepository.countByCategory(
                        RequestCategory.LEAVE
                );

        long hr =
                requestRepository.countByCategory(
                        RequestCategory.HR
                );

        long general =
                requestRepository.countByCategory(
                        RequestCategory.GENERAL
                );

        long remoteWork =
                requestRepository.countByCategory(
                        RequestCategory.REMOTE_WORK
                );

        return new DashboardStatsDTO(
                total,
                pending,
                approved,
                rejected,
                it,
                fleet,
                leave,
                hr,
                general,
                remoteWork
        );
    }
}

