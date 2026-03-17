package com.sahab.demo.controller;

import com.sahab.demo.dto.RequestDTO;
import com.sahab.demo.dto.RequestResponse;
import com.sahab.demo.entity.Request;
import com.sahab.demo.mapper.RequestMapper; // 🟢 تعديل: إضافة المابر
import com.sahab.demo.response.ApiResponse;
import com.sahab.demo.service.RequestService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/requests")
public class RequestController {

    private final RequestService requestService;
    private final RequestMapper requestMapper; // 🟢 تعديل: إضافة mapper


    public RequestController(RequestService requestService,
                             RequestMapper requestMapper){
        this.requestService = requestService;
        this.requestMapper = requestMapper;
    }


    @Operation(summary = "Create new request")
    @PostMapping
    public Request createRequest(@Valid @RequestBody RequestDTO dto){
        return requestService.createRequest(dto);
    }

    // =====================================================
    // APPROVE REQUEST
    // =====================================================

    //@PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/approve") // 🟢 تعديل: كان ناقص
    public RequestResponse approveRequest(@PathVariable Long id){

        Request request = requestService.approveRequest(id);

        return requestMapper.toResponse(request);
    }


    //@PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/reject")
    public Request reject(@PathVariable Long id){
        return requestService.rejectRequest(id);
    }


    @Operation(summary = "Get all requests with pagination")
    @GetMapping
    public ApiResponse<Request> getAllRequests(Pageable pageable) {

        Page<Request> page = requestService.getAllRequests(pageable);

        return new ApiResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements()
        );
    }



    @GetMapping("/my")
    public List<Request> getMyRequests(){
        return requestService.getMyRequests();
    }


    @GetMapping("/status/{status}")
    public List<Request> getRequestsByStatus(@PathVariable String status){
        return requestService.getRequestsByStatus(status);
    }
}