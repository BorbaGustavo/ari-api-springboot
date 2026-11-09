
package br.com.ari.dto;

import java.time.LocalDateTime;

public record FuncionarioResponseDTO(
        Long cdFuncionario,
        Long cdEstabelecimento,
        String nrTelefone,
        String nmFuncionario,
        String dsEmailLogin,
        String tpPerfil,
        String flTreinado,
        String stFuncionario,
        LocalDateTime dtCadastro
) {}
