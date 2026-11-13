package br.com.ari.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados de retorno de uma solicitação de acolhimento.")
public record AvisoAcolhimentoResponseDTO(

        Long cdAviso,

        Long cdUsuario,

        Long cdSessaoWeb,

        Long cdEstabelecimento,

        Long cdFuncionario,

        LocalDateTime dtAviso,

        String stAviso,

        Integer nrTempoEstimado,

        LocalDateTime dtAcolhimento

) {}