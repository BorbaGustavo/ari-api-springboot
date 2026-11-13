
package br.com.ari.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados públicos do perfil da usuária.")
public record UsuarioResponseDTO(

        @Schema(description = "Identificador da usuária.", example = "1")
        Long cdUsuario,

        @Schema(description = "Nome completo.", example = "Ana Souza")
        String nmUsuario,

        @Schema(description = "Telefone para contato.", example = "11999999999")
        String nrTelefone,

        @Schema(description = "E-mail cadastrado.", example = "ana@example.com")
        String dsEmail,

        @Schema(description = "Situação da conta.", example = "ATIVO")
        String stUsuario,

        @Schema(description = "Data e hora do cadastro.", example = "2026-11-10T14:30:00")
        LocalDateTime dtCadastro

) {}
