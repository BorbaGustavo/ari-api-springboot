
package br.com.ari.controller;

import br.com.ari.dto.UsuarioRequestDTO;
import br.com.ari.dto.UsuarioResponseDTO;
import br.com.ari.dto.UsuarioSenhaUpdateDTO;
import br.com.ari.dto.UsuarioUpdateDTO;

import br.com.ari.model.SessaoWeb;
import br.com.ari.model.Usuario;

import br.com.ari.service.UsuarioService;
import br.com.ari.service.SessaoWebService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuárias", description = "Cadastro, manutenção e encerramento da sessão de usuárias.")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final SessaoWebService sessaoWebService;

    // CONSTRUTOR
    public UsuarioController(
            UsuarioService usuarioService,
            SessaoWebService sessaoWebService) {

        this.usuarioService = usuarioService;
        this.sessaoWebService = sessaoWebService;
    }

    // =====================================================
    // CADASTRAR USUÁRIO
    // =====================================================

    @PostMapping
    @Operation(summary = "Cadastrar usuária", description = "Cria uma nova conta de usuária.")
    public ResponseEntity<Void> criar(
            @Valid @RequestBody UsuarioRequestDTO dto) {

        usuarioService.criar(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar usuária", description = "Consulta os dados públicos de uma usuária pelo identificador.")
    public ResponseEntity<UsuarioResponseDTO> buscarPorId(
            @PathVariable Long id) {

        return usuarioService.buscarPorId(id)
                .map(this::converterParaResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar usuária", description = "Substitui os dados editáveis da usuária.")
    public ResponseEntity<Void> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioUpdateDTO dto) {

        usuarioService.atualizar(id, dto);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/senha")
    @Operation(summary = "Alterar senha da usuária", description = "Atualiza a senha da usuária. A nova senha deve ter pelo menos 8 caracteres.")
    public ResponseEntity<Void> atualizarSenha(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioSenhaUpdateDTO dto) {

        usuarioService.atualizarSenha(id, dto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir usuária", description = "Exclui a usuária identificada.")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        usuarioService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    // =====================================================
    // LOGOUT
    // =====================================================

    @PostMapping("/logout")
    @Operation(
            summary = "Encerrar sessão",
            description = "Revoga a sessão ativa identificada pelo header X-Session-UUID."
    )
    public ResponseEntity<Void> sair(
            @Parameter(description = "UUID retornado no login.", example = "550e8400-e29b-41d4-a716-446655440000", in = ParameterIn.HEADER)
            @RequestHeader("X-Session-UUID") String uuid) {

        SessaoWeb sessao = obterSessaoValida(uuid);

        sessaoWebService.revogar(
                sessao.getCdSessaoWeb()
        );

        return ResponseEntity.noContent().build();
    }

    // =====================================================
    // VALIDAR SESSÃO
    // =====================================================

    private SessaoWeb obterSessaoValida(String uuid) {

        if (uuid == null || uuid.isBlank()) {
            throw new IllegalArgumentException(
                    "Token de sessão não informado."
            );
        }

        return sessaoWebService.buscarSessaoAtiva(uuid)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Sessão inválida ou expirada. "
                                        + "Faça login novamente."
                        )
                );
    }

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
