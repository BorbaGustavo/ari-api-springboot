
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

    private String nrCnpj;
    private String nmRazaoSocial;
    private String nmFantasia;

    private String dsEmail;
    private String nrTelefone;
    private String dsTipoEstabelecimento;

    private String nrCep;
    private String dsLogradouro;
    private String nrEndereco;
    private String dsComplemento;
    private String nmBairro;
    private String nmCidade;
    private String sgUf;

    private BigDecimal nrLatitude;
    private BigDecimal nrLongitude;

    private String flAcolhimentoAtivo;
    private LocalDateTime dtSuspensaoAte;

    private String stEstabelecimento;
    private LocalDateTime dtCadastro;
}
