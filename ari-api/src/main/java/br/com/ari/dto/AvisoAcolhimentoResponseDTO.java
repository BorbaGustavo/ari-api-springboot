
package br.com.ari.dto;

import java.time.LocalDateTime;

public record AvisoAcolhimentoResponseDTO(

        Long cdAviso,
        Long cdUsuario,
        Long cdEstabelecimento,
        Long cdFuncionario,

        LocalDateTime dtAviso,
        String stAviso,
        Integer nrTempoEstimado,
        LocalDateTime dtAcolhimento

) {}
