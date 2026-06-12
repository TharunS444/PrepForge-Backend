package com.platform.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String name;
    private String email;
    private String password;
    private String role; // "ROLE_STUDENT", "ROLE_ADMIN"
}
