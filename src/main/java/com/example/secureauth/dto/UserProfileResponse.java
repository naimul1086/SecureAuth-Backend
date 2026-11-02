
package com.example.secureauth.dto;

public class UserProfileResponse {

    private Long id;
    private String userId;
    private String name;
    private String email;
    private String role;

    public UserProfileResponse() {
    }

    public UserProfileResponse(
            Long id,
            String userId,
            String name,
            String email,
            String role
    ) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }
}
