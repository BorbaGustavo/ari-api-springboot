
package br.com.ari.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * DTO de apoio ao Front-end para ativar ou desativar
 * um período de disponibilidade.
 */
public record DisponibilidadeStatusDTO(

        @NotBlank(message = "Status é obrigatório.")
        @Pattern(
                regexp = "S|N",
                message = "Status deve ser S ou N."
        )
        String flAtivo

) {}
