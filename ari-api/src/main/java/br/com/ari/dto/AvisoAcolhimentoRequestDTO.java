
package br.com.ari.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Max;

public record AvisoAcolhimentoRequestDTO(

        @NotNull(message = "Estabelecimento é obrigatório.")
        @Positive(message = "Identificador do estabelecimento inválido.")
        Long cdEstabelecimento,

        @Positive(message = "Tempo estimado deve ser maior que zero.")
        @Max(value = 99999, message = "Tempo estimado excede o limite permitido.")
        Integer nrTempoEstimado

) {}
