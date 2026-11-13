
package br.com.ari.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EstabelecimentoResponseDTO(

        @Schema(description = "Código único do estabelecimento", example = "1")
        Long cdEstabelecimento,

        @Schema(description = "CNPJ da empresa (somente números)", example = "00000000000001")
        String nrCnpj,

        @Schema(description = "Razão social", example = "Livraria da Praça LTDA")
        String nmRazaoSocial,

        @Schema(description = "Nome fantasia", example = "Praca Livros")
        String nmFantasia,

        @Schema(description = "Nome público no ARI", example = "Livraria da Praça")
        String nmEmpresa,

        @Schema(description = "E-mail de contato", example = "contato@livraria.com")
        String dsEmail,

        @Schema(description = "Telefone de contato", example = "11988887777")
        String nrTelefone,

        @Schema(description = "Ramo de atuação", example = "Comércio")
        String dsTipoEstabelecimento,

        @Schema(description = "CEP do endereço", example = "01001000")
        String nrCep,

        @Schema(description = "Logradouro", example = "Praça da Sé")
        String dsLogradouro,

        @Schema(description = "Número", example = "100")
        String nrEndereco,

        @Schema(description = "Complemento", example = "Loja 1")
        String dsComplemento,

        @Schema(description = "Bairro", example = "Sé")
        String nmBairro,

        @Schema(description = "Cidade", example = "São Paulo")
        String nmCidade,

        @Schema(description = "Sigla do Estado", example = "SP")
        String sgUf,

        @Schema(description = "Latitude geográfica", example = "-23.5505200")
        BigDecimal nrLatitude,

        @Schema(description = "Longitude geográfica", example = "-46.6333080")
        BigDecimal nrLongitude,

        @Schema(description = "Indica se o acolhimento está ativo (S/N)", example = "S")
        String flAcolhimentoAtivo,

        @Schema(description = "Data limite de suspensão temporária", example = "2026-12-31T23:59:59")
        LocalDateTime dtSuspensaoAte,

        @Schema(description = "Status do estabelecimento", example = "ATIVO")
        String stEstabelecimento,

        @Schema(description = "Data de cadastro no sistema", example = "2026-03-01T10:00:00")
        LocalDateTime dtCadastro
) {}
