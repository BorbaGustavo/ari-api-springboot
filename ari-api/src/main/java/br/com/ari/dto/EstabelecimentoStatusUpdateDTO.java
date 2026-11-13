package br.com.ari.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Atualização do status do estabelecimento.")
public record EstabelecimentoStatusUpdateDTO(

        @NotBlank(message = "Informe o status do estabelecimento.")
        @Pattern(
                regexp = "ATIVO|INATIVO",
                message = "O status deve ser ATIVO ou INATIVO."
        )
        @Schema(
                description = "Novo status do estabelecimento.",
                example = "INATIVO",
                allowableValues = {"ATIVO", "INATIVO"}
        )
        String stEstabelecimento

) {}