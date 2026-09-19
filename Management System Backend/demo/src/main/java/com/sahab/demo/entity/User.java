package com.sahab.demo.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sahab.demo.enums.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true)
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    // جديد: خاصين بميزة "نسيت الباسورد"
    // WRITE_ONLY عشان ما يطلعوا أبدًا بأي API response (زي password بالضبط)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String resetToken;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private LocalDateTime resetTokenExpiry;

}