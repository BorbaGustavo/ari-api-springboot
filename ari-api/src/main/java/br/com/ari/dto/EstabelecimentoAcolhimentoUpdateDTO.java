/**
 * DTO de apoio ao Front-end para habilitar ou desabilitar
 * o recebimento de solicitações de acolhimento.
 */

package br.com.ari.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Configuração de recebimento de solicitações de acolhimento.")
public record EstabelecimentoAcolhimentoUpdateDTO(

        @NotBlank(message = "Informe se o acolhimento está ativo.")
        @Pattern(
                regexp = "S|N",
                message = "O valor deve ser S ou N."
        )
        @Schema(description = "S habilita o acolhimento; N desabilita.", example = "S", allowableValues = {"S", "N"}, requiredMode = Schema.RequiredMode.REQUIRED)
        String flAcolhimentoAtivo

) {}
