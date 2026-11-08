package br.com.ari.model;


import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessaoWeb {

    private Long cdSessaoWeb;
    private String dsUuid;
    private Long cdUsuario;

    private LocalDateTime dtCriacao;
    private LocalDateTime dtExpiracao;

    private String stSessao;
}
