package com.evb.protal_evb.users.user;

public record UserResponse(
        Integer id,
        String name,
        boolean isActive
) {}

