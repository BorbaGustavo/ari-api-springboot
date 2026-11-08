package br.com.ari.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {
    private Long cdUsuario;
    private String nmUsuario;
    private String nrTelefone;
    private String dsEmail;
    private String dsSenhaHash;
    private String stUsuario;
    private LocalDateTime dtCadastro;
}
