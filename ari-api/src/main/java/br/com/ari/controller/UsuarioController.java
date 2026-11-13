
package br.com.ari.controller;

import br.com.ari.dto.LoginRequestDTO;
import br.com.ari.dto.LoginResponseDTO;
import br.com.ari.dto.UsuarioRequestDTO;
import br.com.ari.dto.UsuarioResponseDTO;
import br.com.ari.dto.UsuarioSenhaUpdateDTO;
import br.com.ari.dto.UsuarioUpdateDTO;

import br.com.ari.model.Usuario;

import br.com.ari.service.UsuarioService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/usuarios")
@Tag(
        name = "Usuárias",
        description = "Cadastro, autenticação e gerenciamento da própria conta."
)
public class UsuarioController {

    private final UsuarioService usuarioService;

    // CONSTRUTOR
    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // ==========================================
    // CADASTRAR USUÁRIA
    // ==========================================

    @PostMapping
    @Operation(
            summary = "Cadastrar usuária",
            description = "Cria uma nova conta de usuária."
    )
    public ResponseEntity<Void> criar(
            @Valid @RequestBody UsuarioRequestDTO dto) {

        usuarioService.criar(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }

    // ==========================================
    // LOGIN
    // ==========================================

    @PostMapping("/login")
    @Operation(
            summary = "Autenticar usuária",
            description = "Valida as credenciais e cria uma sessão autenticada."
    )
    public ResponseEntity<LoginResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO dto) {

        LoginResponseDTO resposta = usuarioService.autenticar(dto);

        return ResponseEntity.ok(resposta);
    }

    // ==========================================
    // BUSCAR PRÓPRIO PERFIL
    // ==========================================


    @GetMapping("/me")
    @Operation(
            summary = "Consultar próprio perfil",
            description = "Retorna os dados da usuária identificada pela sessão."
    )
    public ResponseEntity<UsuarioResponseDTO> buscarMeuPerfil(
            @Parameter(
                    description = "UUID da sessão autenticada.",
                    example = "550e8400-e29b-41d4-a716-446655440000",
                    in = ParameterIn.HEADER
            )
            @RequestHeader("X-Session-UUID") String uuid) {

        return ResponseEntity.ok(
                usuarioService.buscarMeuPerfil(uuid)
        );
    }


    // ==========================================
    // ATUALIZAR PRÓPRIO PERFIL
    // ==========================================

    @PutMapping("/me")
    @Operation(
            summary = "Atualizar próprio perfil",
            description = "Atualiza os dados cadastrais da usuária autenticada."
    )
    public ResponseEntity<Void> atualizarMeuPerfil(
            @RequestHeader("X-Session-UUID") String uuid,
            @Valid @RequestBody UsuarioUpdateDTO dto) {

        usuarioService.atualizarMeuPerfil(uuid, dto);

        return ResponseEntity.noContent().build();
    }

    // ==========================================
    // ALTERAR PRÓPRIA SENHA
    // ==========================================

    @PatchMapping("/me/senha")
    @Operation(
            summary = "Alterar própria senha",
            description = "Atualiza a senha da usuária autenticada."
    )
    public ResponseEntity<Void> atualizarMinhaSenha(
            @RequestHeader("X-Session-UUID") String uuid,
            @Valid @RequestBody UsuarioSenhaUpdateDTO dto) {

        usuarioService.atualizarMinhaSenha(uuid, dto);

        return ResponseEntity.noContent().build();
    }

    // ==========================================
    // BLOQUEAR PRÓPRIA CONTA
    // ==========================================

    @DeleteMapping("/me")
    @Operation(
            summary = "Bloquear própria conta",
            description = "Realiza o bloqueio lógico da conta e revoga suas sessões ativas."
    )
    public ResponseEntity<Void> excluirMinhaConta(
            @RequestHeader("X-Session-UUID") String uuid) {

        usuarioService.excluirMinhaConta(uuid);

        return ResponseEntity.noContent().build();
    }

    // ==========================================
    // LOGOUT
    // ==========================================

    @PostMapping("/logout")
    @Operation(
            summary = "Encerrar sessão",
            description = "Revoga a sessão identificada pelo UUID enviado no header."
    )
    public ResponseEntity<Void> sair(
            @RequestHeader("X-Session-UUID") String uuid) {

        usuarioService.encerrarSessao(uuid);

        return ResponseEntity.noContent().build();
    }

    // ==========================================
    // CONVERTER MODEL PARA RESPONSE DTO
    // ==========================================

    private UsuarioResponseDTO converterParaResponse(Usuario usuario) {

        return new UsuarioResponseDTO(
                usuario.getCdUsuario(),
                usuario.getNmUsuario(),
                usuario.getNrTelefone(),
                usuario.getDsEmail(),
                usuario.getStUsuario(),
                usuario.getDtCadastro()
        );
    }
}
