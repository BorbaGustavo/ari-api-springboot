package br.com.ari.model;


import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Estabelecimento {
    private Long cdEstabelecimento;
    private Long cdTipoEstabelecimento;

    private String nrCnpj;
    private String nmRazaoSocial;
    private String nmFantasia;

    private String nrCep;
    private String nmLogradouro;
    private String nrEndereco;
    private String nmBairro;
    private String nmCidade;
    private String sgUf;

    private BigDecimal nrLatitude;
    private BigDecimal nrLongitude;

    private String stEstabelecimento;
    private LocalDateTime dtCadastro;
}
