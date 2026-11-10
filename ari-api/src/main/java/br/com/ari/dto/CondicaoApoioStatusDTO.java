
package br.com.ari.dto;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;

/**
 * DTO de apoio ao Front-end para ativar ou desativar
 * uma condição de apoio do estabelecimento.
 */
@Schema(description = "Novo status da condição de apoio.")
public record CondicaoApoioStatusDTO(

        @NotBlank(message = "Status é obrigatório.")
        @Pattern(
                regexp = "S|N",
                message = "Status deve ser S ou N."
        )
        @Schema(description = "S ativa a condição; N desativa.", example = "S", allowableValues = {"S", "N"}, requiredMode = Schema.RequiredMode.REQUIRED)
        String flAtivo

) {}
