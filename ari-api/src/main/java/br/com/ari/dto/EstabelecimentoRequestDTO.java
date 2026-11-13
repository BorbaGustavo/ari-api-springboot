
package br.com.ari.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados para cadastrar um estabelecimento.")
public record EstabelecimentoRequestDTO(

        @NotBlank(message = "CNPJ é obrigatório.")
        @Pattern(
                regexp = "\\d{14}",
                message = "CNPJ deve conter 14 números, sem pontuação."
        )
        String nrCnpj,

        @NotBlank(message = "Nome da empresa é obrigatório.")
        @Size(max = 120)
        String nmEmpresa,

        @NotBlank(message = "E-mail é obrigatório.")
        @Email(message = "E-mail inválido.")
        @Size(max = 150)
        String dsEmail,

        @Size(max = 20)
        String nrTelefone,

        @NotBlank(message = "Tipo de estabelecimento é obrigatório.")
        @Size(max = 60)
        String dsTipoEstabelecimento,

        @NotNull(message = "Latitude é obrigatória.")
        @Digits(integer = 3, fraction = 7)
        @DecimalMin("-90.0")
        @DecimalMax("90.0")
        BigDecimal nrLatitude,

        @NotNull(message = "Longitude é obrigatória.")
        @Digits(integer = 3, fraction = 7)
        @DecimalMin("-180.0")
        @DecimalMax("180.0")
        BigDecimal nrLongitude,

        @NotNull(message = "Responsável pelo estabelecimento é obrigatório.")
        @Valid
        ResponsavelEstabelecimentoRequestDTO responsavel

) {}
