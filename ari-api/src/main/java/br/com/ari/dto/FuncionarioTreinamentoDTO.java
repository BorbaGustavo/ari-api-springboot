package br.com.ari.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Situação de treinamento do funcionário.")
public record FuncionarioTreinamentoDTO(

        @NotBlank(message = "Informe se o funcionário está treinado.")
        @Pattern(regexp = "[SN]", message = "O valor deve ser S ou N.")
        @Schema(description = "S indica treinado; N indica não treinado.", example = "S", allowableValues = {"S", "N"}, requiredMode = Schema.RequiredMode.REQUIRED)
        String flTreinado

) {}
