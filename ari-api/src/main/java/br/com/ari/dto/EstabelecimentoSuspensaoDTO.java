
package br.com.ari.dto;

import jakarta.validation.constraints.Future;

import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO de apoio ao Front-end para configurar ou remover
 * a suspensão temporária de acolhimentos.
 */
@Schema(description = "Configuração de suspensão temporária do acolhimento.")
public record EstabelecimentoSuspensaoDTO(

        @Future(message = "A data de suspensão deve estar no futuro.")
        @Schema(description = "Data e hora futuras até quando o acolhimento ficará suspenso. Use null para remover.", example = "2026-12-31T18:00:00", nullable = true)
        LocalDateTime dtSuspensaoAte

) {}
