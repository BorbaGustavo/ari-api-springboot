
package br.com.ari.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados para cadastrar um estabelecimento.")
public record EstabelecimentoRequestDTO(

        @NotBlank(message = "CNPJ é obrigatório.")
        @Pattern(
                regexp = "\\d{14}",
                message = "CNPJ deve conter exatamente 14 números, sem pontuação."
        )
        @Schema(description = "CNPJ com 14 dígitos, sem pontuação.", example = "12345678000195", requiredMode = Schema.RequiredMode.REQUIRED)
        String nrCnpj,

        @NotBlank(message = "Razão social é obrigatória.")
        @Size(max = 150, message = "Razão social deve ter no máximo 150 caracteres.")
        @Schema(description = "Razão social.", example = "Instituição ARI", requiredMode = Schema.RequiredMode.REQUIRED)
        String nmRazaoSocial,

        @Size(max = 120, message = "Nome fantasia deve ter no máximo 120 caracteres.")
        @Schema(description = "Nome fantasia.", example = "ARI")
        String nmFantasia,

        @NotBlank(message = "E-mail é obrigatório.")
        @Email(message = "E-mail inválido.")
        @Size(max = 150, message = "E-mail deve ter no máximo 150 caracteres.")
        @Schema(description = "E-mail de contato.", example = "contato@ari.example", requiredMode = Schema.RequiredMode.REQUIRED)
        String dsEmail,

        @Size(max = 20, message = "Telefone deve ter no máximo 20 caracteres.")
        @Schema(description = "Telefone.", example = "1133334444")
        String nrTelefone,

        @NotBlank(message = "Tipo de estabelecimento é obrigatório.")
        @Size(max = 60, message = "Tipo deve ter no máximo 60 caracteres.")
        @Schema(description = "Tipo do estabelecimento.", example = "Centro de acolhimento", requiredMode = Schema.RequiredMode.REQUIRED)
        String dsTipoEstabelecimento,

        @Pattern(
                regexp = "\\d{8}",
                message = "CEP deve conter 8 números, sem pontuação."
        )
        @Schema(description = "CEP com 8 dígitos, sem pontuação.", example = "01310100")
        String nrCep,

        @Size(max = 150, message = "Logradouro deve ter no máximo 150 caracteres.")
        @Schema(description = "Nome da rua ou avenida.", example = "Avenida Paulista")
        String dsLogradouro,

        @Size(max = 10, message = "Número deve ter no máximo 10 caracteres.")
        @Schema(description = "Número do endereço.", example = "1000")
        String nrEndereco,

        @Size(max = 100, message = "Complemento deve ter no máximo 100 caracteres.")
        String dsComplemento,

        @Size(max = 80, message = "Bairro deve ter no máximo 80 caracteres.")
        @Schema(description = "Bairro.", example = "Bela Vista")
        String nmBairro,

        @Size(max = 80, message = "Cidade deve ter no máximo 80 caracteres.")
        @Schema(description = "Cidade.", example = "São Paulo")
        String nmCidade,

        @Pattern(
                regexp = "[A-Z]{2}",
                message = "UF deve conter duas letras maiúsculas."
        )
        @Schema(description = "UF com duas letras maiúsculas.", example = "SP")
        String sgUf,

        @Digits(integer = 3, fraction = 7, message = "Latitude inválida.")
        @DecimalMin(value = "-90.0", message = "Latitude mínima é -90.")
        @DecimalMax(value = "90.0", message = "Latitude máxima é 90.")
        @Schema(description = "Latitude entre -90 e 90.", example = "-23.561684")
        BigDecimal nrLatitude,

        @Digits(integer = 3, fraction = 7, message = "Longitude inválida.")
        @DecimalMin(value = "-180.0", message = "Longitude mínima é -180.")
        @DecimalMax(value = "180.0", message = "Longitude máxima é 180.")
        @Schema(description = "Longitude entre -180 e 180.", example = "-46.655981")
        BigDecimal nrLongitude,

        @NotNull(message = "Responsável pelo estabelecimento é obrigatório.")
        @Valid
        @Schema(description = "Dados do primeiro administrador do estabelecimento.", requiredMode = Schema.RequiredMode.REQUIRED)
        ResponsavelEstabelecimentoRequestDTO responsavel

) {}
