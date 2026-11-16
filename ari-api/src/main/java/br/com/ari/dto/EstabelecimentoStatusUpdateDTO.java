
package br.com.ari.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Atualização do status do estabelecimento.")
public record EstabelecimentoStatusUpdateDTO(

        @NotBlank(message = "Informe o status do estabelecimento.")
        @Pattern(
                regexp = "PENDENTE|ATIVO|SUSPENSO|REPROVADO",
                message = "Status inválido. Selecione um dos status permitidos: PENDENTE, ATIVO, SUSPENSO ou REPROVADO."
        )
        @Schema(
                description = "Informe somente um dos status permitidos para o estabelecimento.",
                example = "ATIVO",
                allowableValues = {
                        "PENDENTE",
                        "ATIVO",
                        "SUSPENSO",
                        "REPROVADO"
                }
        )
        String stEstabelecimento

) {}
