package com.evb.portal_evb.users.user.dto;

public record UserResponse(
        Integer id,
        String name,
        boolean isActive
) {}

