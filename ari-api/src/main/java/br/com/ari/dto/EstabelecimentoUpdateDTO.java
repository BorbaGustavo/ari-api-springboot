
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
        String nmEmpresa,

        @NotBlank(message = "E-mail é obrigatório.")
        @Email(message = "E-mail inválido.")
        @Size(
                max = 150,
                message = "E-mail deve ter no máximo 150 caracteres."
        )
        String dsEmail,

        @Size(
                max = 20,
                message = "Telefone deve ter no máximo 20 caracteres."
        )
        String nrTelefone,

        @NotBlank(message = "Tipo de estabelecimento é obrigatório.")
        @Size(
                max = 60,
                message = "Tipo deve ter no máximo 60 caracteres."
        )
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
        BigDecimal nrLongitude

) {}
