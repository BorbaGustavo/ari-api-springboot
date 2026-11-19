package br.com.ari.dto;

import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados retornados no login do funcionario.")
public record FuncionarioLoginResponseDTO(
        @Schema(description = "Dados do funcionario autenticado.")
        FuncionarioResponseDTO funcionario,
        @Schema(description = "UUID da sessao para enviar no header X-Session-UUID.", example = "550e8400-e29b-41d4-a716-446655440000")
        String dsUuid,
        @Schema(description = "Data e hora de expiracao da sessao.", example = "2026-11-18T18:00:00")
        LocalDateTime dtExpiracao
) {}
