package com.sahab.demo.repository;

import com.sahab.demo.entity.Request;
import com.sahab.demo.entity.User;
import com.sahab.demo.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {

        List<Request> findByStatus(RequestStatus status);

        List<Request> findByUser(User user);
}