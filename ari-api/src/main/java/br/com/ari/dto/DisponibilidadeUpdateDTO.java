
package br.com.ari.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados editáveis de uma disponibilidade.", example = "{\"nrDiaSemana\":1,\"nrPeriodo\":1,\"hrInicio\":\"08:00\",\"hrFim\":\"17:00\"}")
public record DisponibilidadeUpdateDTO(

        @NotNull(message = "Dia da semana é obrigatório.")
        @Min(value = 1, message = "O dia mínimo é 1 (segunda-feira).")
        @Max(value = 7, message = "O dia máximo é 7 (domingo).")
        Integer nrDiaSemana,

        @NotNull(message = "Número do período é obrigatório.")
        @Min(value = 1, message = "O período deve ser no mínimo 1.")
        @Max(value = 99, message = "O período deve ser no máximo 99.")
        Integer nrPeriodo,

        @NotBlank(message = "Horário de início é obrigatório.")
        @Pattern(
                regexp = "^(?:[01]\\d|2[0-3]):[0-5]\\d$",
                message = "Horário de início deve seguir o formato HH:mm."
        )
        String hrInicio,

        @NotBlank(message = "Horário de término é obrigatório.")
        @Pattern(
                regexp = "^(?:[01]\\d|2[0-3]):[0-5]\\d$",
                message = "Horário de término deve seguir o formato HH:mm."
        )
        String hrFim

) {}
