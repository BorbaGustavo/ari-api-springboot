
package br.com.ari.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EstabelecimentoResponseDTO(

        Long cdEstabelecimento,

        String nrCnpj,
        String nmRazaoSocial,
        String nmFantasia,
        String nmEmpresa,

        String dsEmail,
        String nrTelefone,
        String dsTipoEstabelecimento,

        String nrCep,
        String dsLogradouro,
        String nrEndereco,
        String dsComplemento,
        String nmBairro,
        String nmCidade,
        String sgUf,

        BigDecimal nrLatitude,
        BigDecimal nrLongitude,

        String flAcolhimentoAtivo,
        LocalDateTime dtSuspensaoAte,

        String stEstabelecimento,
        LocalDateTime dtCadastro

) {}
