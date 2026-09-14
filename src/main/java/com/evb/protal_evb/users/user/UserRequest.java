package com.evb.protal_evb.users.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRequest (
    @NotBlank(message = "Preencha o nome do usuário")
    @Size(max = 50, message = "O nome deve ter no máximo 50 caracteres")
    String name,

    @NotBlank(message = "Preencha a senha")
    String password
) {}

