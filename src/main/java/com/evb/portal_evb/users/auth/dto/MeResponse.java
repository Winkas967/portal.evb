package com.evb.portal_evb.users.auth.dto;

import java.util.List;

public record MeResponse (
        String username,
        List<String> roles,
        String displayRole
){}
