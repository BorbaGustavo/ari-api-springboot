package br.com.ari.model;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Funcionario {
    private Long cdFuncionario;
    private Long cdEstabelecimento;

    private String nrTelefone;
    private String nmFuncionario;
    private String dsEmailLogin;
    private String dsSenhaHash;

    private String tpPerfil;
    private String flTreinado;
    private String stFuncionario;

    private LocalDateTime dtCadastro;
}
