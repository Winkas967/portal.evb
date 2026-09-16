package com.evb.protal_evb.users.role.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record RoleRequest(
        @NotBlank(message = "Preencha o nome do papel")
        @Size(max = 50, message = "O nome deve ter no máximo 50 caracteres")
        String name,

        @NotBlank(message = "Preencha o papel/permissão")
        @Size(max = 20, message = "O papel deve ter no máximo 20 caracteres")
        String role,

        @NotNull(message = "Informe o usuário")
        @Positive(message = "Usuário inválido")
        Integer userId
) {}
