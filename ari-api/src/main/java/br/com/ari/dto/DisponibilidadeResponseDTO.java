
package br.com.ari.dto;

import java.time.LocalDateTime;

public record DisponibilidadeResponseDTO(

        Long cdDisponibilidade,
        Long cdEstabelecimento,
        Integer nrDiaSemana,
        Integer nrPeriodo,
        String hrInicio,
        String hrFim,
        String flAtivo,
        LocalDateTime dtCadastro

) {}
