package com.evb.protal_evb.users.role.dto;

public record RoleResponse(
        Integer id,
        String name,
        String role,
        Integer userId,
        String userName,
        boolean isActive
) {}
