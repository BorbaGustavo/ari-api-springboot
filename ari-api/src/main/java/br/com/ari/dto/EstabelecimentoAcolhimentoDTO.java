/**
 * DTO de apoio ao Front-end para habilitar ou desabilitar
 * o recebimento de solicitações de acolhimento.
 */

package br.com.ari.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record EstabelecimentoAcolhimentoDTO(

        @NotBlank(message = "Informe se o acolhimento está ativo.")
        @Pattern(
                regexp = "S|N",
                message = "O valor deve ser S ou N."
        )
        String flAcolhimentoAtivo

) {}
