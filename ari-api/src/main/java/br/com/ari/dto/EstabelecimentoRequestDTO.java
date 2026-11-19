
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
        @Schema(description = "CNPJ do estabelecimento, somente numeros.", example = "00000000000001", requiredMode = Schema.RequiredMode.REQUIRED)
        String nrCnpj,

        @NotBlank(message = "Nome da empresa é obrigatório.")
        @Size(max = 120)
        @Schema(description = "Nome publico do estabelecimento no ARI.", example = "ARI Unidade Paulista", requiredMode = Schema.RequiredMode.REQUIRED)
        String nmEmpresa,

        @NotBlank(message = "E-mail é obrigatório.")
        @Email(message = "E-mail inválido.")
        @Size(max = 150)
        @Schema(description = "E-mail de contato.", example = "contato@ari.example", requiredMode = Schema.RequiredMode.REQUIRED)
        String dsEmail,

        @Size(max = 20)
        @Schema(description = "Telefone de contato.", example = "11999999999")
        String nrTelefone,

        @NotBlank(message = "Tipo de estabelecimento é obrigatório.")
        @Size(max = 60)
        @Schema(description = "Ramo ou tipo de estabelecimento.", example = "Centro de acolhimento", requiredMode = Schema.RequiredMode.REQUIRED)
        String dsTipoEstabelecimento,

        @NotNull(message = "Latitude é obrigatória.")
        @Digits(integer = 3, fraction = 7)
        @DecimalMin("-90.0")
        @DecimalMax("90.0")
        @Schema(description = "Latitude geografica entre -90 e 90.", example = "-23.561684", requiredMode = Schema.RequiredMode.REQUIRED)
        BigDecimal nrLatitude,

        @NotNull(message = "Longitude é obrigatória.")
        @Digits(integer = 3, fraction = 7)
        @DecimalMin("-180.0")
        @DecimalMax("180.0")
        @Schema(description = "Longitude geografica entre -180 e 180.", example = "-46.655981", requiredMode = Schema.RequiredMode.REQUIRED)
        BigDecimal nrLongitude,

        @NotNull(message = "Responsável pelo estabelecimento é obrigatório.")
        @Valid
        @Schema(description = "Dados do primeiro administrador do estabelecimento.", requiredMode = Schema.RequiredMode.REQUIRED)
        ResponsavelEstabelecimentoRequestDTO responsavel

) {}
