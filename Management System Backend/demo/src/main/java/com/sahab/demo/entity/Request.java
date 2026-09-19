package com.sahab.demo.entity;

import com.sahab.demo.enums.RequestCategory;
import com.sahab.demo.enums.RequestPriority;
import com.sahab.demo.enums.RequestStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
public class Request {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String description;

    @Enumerated(EnumType.STRING)
    private RequestStatus status;

    @Enumerated(EnumType.STRING)
    private RequestCategory category;


    @Enumerated(EnumType.STRING)
    private RequestPriority priority = RequestPriority.MEDIUM;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    //AUDIT FIELDS
    private LocalDateTime createdAt;
    private Long approvedBy;
    private LocalDateTime approvedAt;



}


