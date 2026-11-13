package br.com.ari.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Max;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados para solicitar acolhimento.")
public record AvisoAcolhimentoRequestDTO(

        @NotNull(message = "Estabelecimento é obrigatório.")
        @Positive(message = "Identificador do estabelecimento inválido.")
        @Schema(
                description = "Identificador do estabelecimento.",
                example = "1",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        Long cdEstabelecimento,

        @Positive(message = "Tempo estimado deve ser maior que zero.")
        @Max(value = 99999, message = "Tempo estimado excede o limite permitido.")
        @Schema(
                description = "Tempo estimado de espera em minutos. Caso não informado, será utilizado o padrão de 60 minutos.",
                example = "30"
        )
        Integer nrTempoEstimado

) {}