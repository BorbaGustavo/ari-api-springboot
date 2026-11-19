package br.com.ari.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados de retorno de uma solicitação de acolhimento.")
public record AvisoAcolhimentoResponseDTO(

        @Schema(description = "Identificador do aviso de acolhimento.", example = "1")
        Long cdAviso,

        @Schema(description = "Identificador da usuaria que solicitou o acolhimento.", example = "1")
        Long cdUsuario,

        @Schema(description = "Identificador da sessao web associada.", example = "1")
        Long cdSessaoWeb,

        @Schema(description = "Identificador do estabelecimento solicitado.", example = "1")
        Long cdEstabelecimento,

        @Schema(description = "Identificador do funcionario que confirmou o acolhimento.", example = "1", nullable = true)
        Long cdFuncionario,

        @Schema(description = "Data e hora da solicitacao.", example = "2026-11-18T10:00:00")
        LocalDateTime dtAviso,

        @Schema(description = "Situacao atual do aviso.", example = "A_CAMINHO")
        String stAviso,

        @Schema(description = "Tempo estimado para chegada, em minutos.", example = "30", nullable = true)
        Integer nrTempoEstimado,

        @Schema(description = "Data e hora da confirmacao do acolhimento.", example = "2026-11-18T10:30:00", nullable = true)
        LocalDateTime dtAcolhimento

) {}
