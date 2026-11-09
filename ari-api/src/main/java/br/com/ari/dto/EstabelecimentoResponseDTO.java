
package br.com.ari.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EstabelecimentoResponseDTO(
        Long cdEstabelecimento,
        Long cdTipoEstabelecimento,
        String nrCnpj,
        String nmRazaoSocial,
        String nmFantasia,
        String nrCep,
        String nmLogradouro,
        String nrEndereco,
        String nmBairro,
        String nmCidade,
        String sgUf,
        BigDecimal nrLatitude,
        BigDecimal nrLongitude,
        String stEstabelecimento,
        LocalDateTime dtCadastro
) {}
