
package br.com.ari.dto;

import java.util.List;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Condições de apoio selecionadas pelo estabelecimento.")
public record CondicaoApoioSelecaoDTO(

        @NotNull(message = "A lista de condições selecionadas é obrigatória.")
        @Schema(
                description = "IDs das condições que devem permanecer ativas.",
                example = "[1, 3, 5]",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        List<@NotNull @Positive Long> idsSelecionados

) {}
