package br.com.ari.dto;

import java.time.LocalDateTime;

public record UsuarioResponseDTO(
        Long cdUsuario,
        String nmUsuario,
        String nrTelefone,
        String dsEmail,
        String stUsuario,
        LocalDateTime dtCadastro
) {}