
package br.com.ari.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        description = "Dados operacionais editáveis do estabelecimento.",
        example = """
                {
                  "nmEmpresa": "ARI - Unidade Paulista",
                  "dsEmail": "contato@ari.example",
                  "nrTelefone": "11999999999",
                  "dsTipoEstabelecimento": "Centro de acolhimento",
                  "nrLatitude": -23.561684,
                  "nrLongitude": -46.655981
                }
                """
)
public record EstabelecimentoUpdateDTO(

        @NotBlank(message = "Nome da empresa é obrigatório.")
        @Size(
                max = 120,
                message = "Nome da empresa deve ter no máximo 120 caracteres."
        )
        @Schema(description = "Nome publico do estabelecimento no ARI.", example = "ARI Unidade Paulista", requiredMode = Schema.RequiredMode.REQUIRED)
        String nmEmpresa,

        @NotBlank(message = "E-mail é obrigatório.")
        @Email(message = "E-mail inválido.")
        @Size(
                max = 150,
                message = "E-mail deve ter no máximo 150 caracteres."
        )
        @Schema(description = "E-mail de contato.", example = "contato@ari.example", requiredMode = Schema.RequiredMode.REQUIRED)
        String dsEmail,

        @Size(
                max = 20,
                message = "Telefone deve ter no máximo 20 caracteres."
        )
        @Schema(description = "Telefone de contato.", example = "11999999999")
        String nrTelefone,

        @NotBlank(message = "Tipo de estabelecimento é obrigatório.")
        @Size(
                max = 60,
                message = "Tipo deve ter no máximo 60 caracteres."
        )
        @Schema(description = "Ramo ou tipo de estabelecimento.", example = "Centro de acolhimento", requiredMode = Schema.RequiredMode.REQUIRED)
        String dsTipoEstabelecimento,

        @Digits(
                integer = 3,
                fraction = 7,
                message = "Latitude inválida."
        )
        @DecimalMin(
                value = "-90.0",
                message = "Latitude mínima é -90."
        )
        @DecimalMax(
                value = "90.0",
                message = "Latitude máxima é 90."
        )
        @Schema(description = "Latitude geografica entre -90 e 90.", example = "-23.561684")
        BigDecimal nrLatitude,

        @Digits(
                integer = 3,
                fraction = 7,
                message = "Longitude inválida."
        )
        @DecimalMin(
                value = "-180.0",
                message = "Longitude mínima é -180."
        )
        @DecimalMax(
                value = "180.0",
                message = "Longitude máxima é 180."
        )
        @Schema(description = "Longitude geografica entre -180 e 180.", example = "-46.655981")
        BigDecimal nrLongitude

) {}
