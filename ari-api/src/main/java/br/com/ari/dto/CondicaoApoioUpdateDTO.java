
package br.com.ari.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        description = "Dados editáveis de uma condição de apoio.",
        example = "{\"tpCategoria\":\"CONDICAO\",\"nmCondicao\":\"Atendimento em Libras\",\"dsCondicao\":\"Atendimento com intérprete de Libras.\"}"
)
public record CondicaoApoioUpdateDTO(

        @NotBlank(message = "Categoria da condição é obrigatória.")
        @Pattern(
                regexp = "CONDICAO|APOIO_ADICIONAL",
                message = "Categoria deve ser CONDICAO ou APOIO_ADICIONAL."
        )
        @Schema(
                description = "Categoria da condição.",
                example = "CONDICAO",
                allowableValues = {"CONDICAO", "APOIO_ADICIONAL"},
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String tpCategoria,

        @NotBlank(message = "Nome da condição é obrigatório.")
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres.")
        @Schema(
                description = "Nome da condição.",
                example = "Atendimento em Libras",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String nmCondicao,

        @Size(max = 255, message = "A descrição deve ter no máximo 255 caracteres.")
        @Schema(
                description = "Descrição detalhada da condição.",
                example = "Atendimento com intérprete de Libras."
        )
        String dsCondicao

) {}
