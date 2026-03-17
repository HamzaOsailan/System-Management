package com.sahab.demo.service;

import com.sahab.demo.dto.RequestDTO;
import com.sahab.demo.entity.Request;
import com.sahab.demo.entity.User;
import com.sahab.demo.enums.RequestStatus;
import com.sahab.demo.enums.Role;
import com.sahab.demo.exception.ResourceNotFoundException;
import com.sahab.demo.mapper.RequestMapper;
import com.sahab.demo.repository.RequestRepository;
import com.sahab.demo.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class RequestService {

    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final RequestMapper requestMapper;
    private final NotificationService notificationService;
    private static final Logger logger=LoggerFactory.getLogger(RequestService.class);
    public RequestService(RequestRepository requestRepository,
                          UserRepository userRepository,
                          ModelMapper modelMapper, RequestMapper requestMapper, NotificationService notificationService) {

        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
        this.requestMapper = requestMapper;
        this.notificationService = notificationService;
    }

    public Request createRequest(RequestDTO dto){
        logger.info("Creating new request from userId: {}",dto.getUserId());
        User user=userRepository
                .findById(dto.getUserId())
                .orElseThrow(()->{
                    logger.error("User not found with id: {}",dto.getUserId());
                    return new ResourceNotFoundException("User not found");
                });

        Request request=requestMapper.toEntity(dto,user);

        Request saved=requestRepository.save(request);
        logger.info("Request created successfully with id: {}",saved.getId());

        return saved;
    }

    public Page<Request> getAllRequests(Pageable pageable){
        logger.info("Fetching all requests with  pagination");
        return requestRepository.findAll(pageable);
    }
    public List<Request> getMyRequests(){
        Authentication auth = SecurityContextHolder
                .getContext()
                .getAuthentication();

        String email = auth.getName();

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return requestRepository.findByUser(user);
    }




    public Request approveRequest(Long id){

        Request request = requestRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found"));

        // ⭐ إيجاد admin
        User admin = userRepository
                .findById(1L)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        request.setStatus(RequestStatus.APPROVED);

        request.setApprovedAt(LocalDateTime.now());

        request.setApprovedBy(admin.getId());

        return requestRepository.save(request);
    }

    public Request rejectRequest(Long id){
        logger.info("Attempting to reject request id: {}",id);

        Request request = requestRepository
                .findById(id)
                .orElseThrow(() ->{
                    logger.info("Request not found with id: {}",id);
                    return new ResourceNotFoundException("Request not found");
                });


        request.setStatus(RequestStatus.REJECTED);

        Request saved=requestRepository.save(request);
        logger.info("Request rejected successfully id: {}",id);
        return saved;
    }


    public List<Request> getRequestsByStatus(String status){
        logger.info("Fetching requests with status: {}",status);
        RequestStatus requestStatus = RequestStatus.valueOf(status.toUpperCase());

        return requestRepository.findByStatus(requestStatus);
    }

}