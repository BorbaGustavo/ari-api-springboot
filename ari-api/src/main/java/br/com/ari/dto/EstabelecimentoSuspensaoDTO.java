
package br.com.ari.dto;

import jakarta.validation.constraints.Future;

import java.time.LocalDateTime;

/**
 * DTO de apoio ao Front-end para configurar ou remover
 * a suspensão temporária de acolhimentos.
 */
public record EstabelecimentoSuspensaoDTO(

        @Future(message = "A data de suspensão deve estar no futuro.")
        LocalDateTime dtSuspensaoAte

) {}
