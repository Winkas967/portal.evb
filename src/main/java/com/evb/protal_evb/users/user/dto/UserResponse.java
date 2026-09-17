package com.evb.protal_evb.users.user.dto;

public record UserResponse(
        Integer id,
        String name,
        boolean isActive
) {}

