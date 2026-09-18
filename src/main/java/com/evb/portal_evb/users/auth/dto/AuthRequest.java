package com.evb.portal_evb.users.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthRequest(
        @NotBlank(message = "Preencha o nome de usuário") String username,
        @NotBlank(message = "Preencha a senha") String password
) {}
