package com.evb.protal_evb.users.user.dto;

import jakarta.validation.constraints.Size;

public record UserUpdateRequest (
    @Size(max = 50, message = "O nome deve ter no máximo 50 caracteres")
    String name,

    @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres")
    String password
    ) {}
