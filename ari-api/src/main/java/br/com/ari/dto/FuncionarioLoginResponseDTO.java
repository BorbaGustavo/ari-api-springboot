package br.com.ari.dto;

import java.time.LocalDateTime;

public record FuncionarioLoginResponseDTO(
        FuncionarioResponseDTO funcionario,
        String dsUuid,
        LocalDateTime dtExpiracao
) {}
