package com.sahab.demo.modules.hr.controller;

import com.sahab.demo.modules.hr.dto.LeaveBalanceDTO;
import com.sahab.demo.modules.hr.dto.LeaveSummaryDTO;
import com.sahab.demo.modules.hr.service.LeaveBalanceService;
import com.sahab.demo.modules.hr.service.LeaveService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/leave")
public class LeaveController {

    private final LeaveService leaveService;
    private final LeaveBalanceService leaveBalanceService;

    public LeaveController(
            LeaveService leaveService,
            LeaveBalanceService leaveBalanceService
    ) {
        this.leaveService = leaveService;
        this.leaveBalanceService = leaveBalanceService;
    }

    /**
     * Returns the current user's leave summary.
     *
     * GET /leave/summary
     */
    @GetMapping("/summary")
    public LeaveSummaryDTO getLeaveSummary() {

        return leaveService.getCurrentUserLeaveSummary();
    }

    /**
     * Returns the current user's annual leave
     * and remote work balance.
     *
     * GET /leave/balance
     */
    @GetMapping("/balance")
    public LeaveBalanceDTO getLeaveBalance() {

        return leaveBalanceService.getCurrentUserBalance();
    }
}