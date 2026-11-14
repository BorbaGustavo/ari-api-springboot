package br.com.ari.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Dados necessários para manter uma sessão web ativa.")
public record SessaoWebResponseDTO(

        @Schema(description = "Identificador da sessão para enviar no header X-Session-UUID.")
        String dsUuid,

        @Schema(description = "Data e hora de expiração da sessão.")
        LocalDateTime dtExpiracao

) {}
