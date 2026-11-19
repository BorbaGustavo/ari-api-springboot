
package br.com.ari.dto;

import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados de retorno de um periodo de atendimento.")
public record DisponibilidadeResponseDTO(

        @Schema(description = "Identificador da disponibilidade.", example = "1")
        Long cdDisponibilidade,
        @Schema(description = "Identificador do estabelecimento.", example = "1")
        Long cdEstabelecimento,
        @Schema(description = "Dia da semana: 1=segunda-feira ate 7=domingo.", example = "1", minimum = "1", maximum = "7")
        Integer nrDiaSemana,
        @Schema(description = "Numero sequencial do periodo no dia.", example = "1")
        Integer nrPeriodo,
        @Schema(description = "Horario inicial no formato HH:mm.", example = "08:00")
        String hrInicio,
        @Schema(description = "Horario final no formato HH:mm.", example = "17:00")
        String hrFim,
        @Schema(description = "Indica se o periodo esta ativo.", example = "S", allowableValues = {"S", "N"})
        String flAtivo,
        @Schema(description = "Data e hora do cadastro.", example = "2026-11-18T10:00:00")
        LocalDateTime dtCadastro

) {}
