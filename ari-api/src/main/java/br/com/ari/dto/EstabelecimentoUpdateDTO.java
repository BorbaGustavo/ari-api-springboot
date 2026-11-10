
package br.com.ari.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record EstabelecimentoUpdateDTO(

        @NotBlank(message = "Razão social é obrigatória.")
        @Size(max = 150, message = "Razão social deve ter no máximo 150 caracteres.")
        String nmRazaoSocial,

        @Size(max = 120, message = "Nome fantasia deve ter no máximo 120 caracteres.")
        String nmFantasia,

        @NotBlank(message = "E-mail é obrigatório.")
        @Email(message = "E-mail inválido.")
        @Size(max = 150, message = "E-mail deve ter no máximo 150 caracteres.")
        String dsEmail,

        @Size(max = 20, message = "Telefone deve ter no máximo 20 caracteres.")
        String nrTelefone,

        @NotBlank(message = "Tipo de estabelecimento é obrigatório.")
        @Size(max = 60, message = "Tipo deve ter no máximo 60 caracteres.")
        String dsTipoEstabelecimento,

        @Pattern(
                regexp = "\\d{8}",
                message = "CEP deve conter 8 números, sem pontuação."
        )
        String nrCep,

        @Size(max = 150, message = "Logradouro deve ter no máximo 150 caracteres.")
        String dsLogradouro,

        @Size(max = 10, message = "Número deve ter no máximo 10 caracteres.")
        String nrEndereco,

        @Size(max = 100, message = "Complemento deve ter no máximo 100 caracteres.")
        String dsComplemento,

        @Size(max = 80, message = "Bairro deve ter no máximo 80 caracteres.")
        String nmBairro,

        @Size(max = 80, message = "Cidade deve ter no máximo 80 caracteres.")
        String nmCidade,

        @Pattern(
                regexp = "[A-Z]{2}",
                message = "UF deve conter duas letras maiúsculas."
        )
        String sgUf,

        @Digits(integer = 3, fraction = 7, message = "Latitude inválida.")
        @DecimalMin(value = "-90.0", message = "Latitude mínima é -90.")
        @DecimalMax(value = "90.0", message = "Latitude máxima é 90.")
        BigDecimal nrLatitude,

        @Digits(integer = 3, fraction = 7, message = "Longitude inválida.")
        @DecimalMin(value = "-180.0", message = "Longitude mínima é -180.")
        @DecimalMax(value = "180.0", message = "Longitude máxima é 180.")
        BigDecimal nrLongitude

) {}
