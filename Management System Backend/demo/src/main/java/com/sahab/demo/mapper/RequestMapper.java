package com.sahab.demo.mapper;

import com.sahab.demo.dto.RequestDTO;
import com.sahab.demo.dto.RequestResponse;
import com.sahab.demo.entity.Request;
import com.sahab.demo.entity.User;
import com.sahab.demo.enums.RequestStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class RequestMapper {

    public Request toEntity(RequestDTO dto, User user){
        Request request =new Request();

        request.setTitle(dto.getTitle());
        request.setDescription(dto.getDescription());

        request.setStatus(RequestStatus.PENDING);

        request.setUser(user);
        request.setCreatedAt(LocalDateTime.now());
        return request;

    }

    public RequestResponse toResponse(Request request){

        RequestResponse response = new RequestResponse();

        response.setId(request.getId());
        response.setTitle(request.getTitle());
        response.setDescription(request.getDescription());
        response.setStatus(request.getStatus().name());
        response.setUserName(request.getUser().getName());
        response.setCreatedAt(request.getCreatedAt());
        response.setApprovedBy(request.getApprovedBy());
        response.setApprovedAt(request.getApprovedAt());

        return response;
    }
}
