
package br.com.ari.controller;

import br.com.ari.dto.FuncionarioLoginDTO;
import br.com.ari.dto.FuncionarioRequestDTO;
import br.com.ari.dto.FuncionarioResponseDTO;
import br.com.ari.dto.FuncionarioSenhaUpdateDTO;
import br.com.ari.dto.FuncionarioUpdateDTO;
import br.com.ari.model.Funcionario;

import br.com.ari.service.FuncionarioService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/funcionarios")
public class FuncionarioController {

    private final FuncionarioService funcionarioService;

    // CONSTRUTOR
    public FuncionarioController(
            FuncionarioService funcionarioService) {

        this.funcionarioService = funcionarioService;
    }

    @PostMapping
    public ResponseEntity<Void> criar(
            Principal principal,
            @Valid @RequestBody FuncionarioRequestDTO dto) {

        funcionarioService.criar(obterAdminId(principal), dto);
        return ResponseEntity.status(201).build();
    }

    @GetMapping
    public ResponseEntity<List<FuncionarioResponseDTO>> buscarPorEstabelecimento(
            Principal principal) {

        return ResponseEntity.ok(funcionarioService
                .buscarPorEstabelecimento(obterAdminId(principal))
                .stream()
                .map(this::converterParaResponse)
                .toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FuncionarioResponseDTO> buscarPorId(
            Principal principal,
            @PathVariable Long id) {

        return ResponseEntity.ok(converterParaResponse(
                funcionarioService.buscarPorId(obterAdminId(principal), id)
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> atualizar(
            Principal principal,
            @PathVariable Long id,
            @Valid @RequestBody FuncionarioUpdateDTO dto) {

        funcionarioService.atualizar(obterAdminId(principal), id, dto);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/senha")
    public ResponseEntity<Void> atualizarSenha(
            Principal principal,
            @PathVariable Long id,
            @Valid @RequestBody FuncionarioSenhaUpdateDTO dto) {

        funcionarioService.atualizarSenha(obterAdminId(principal), id, dto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(
            Principal principal,
            @PathVariable Long id) {

        funcionarioService.excluir(obterAdminId(principal), id);
        return ResponseEntity.noContent().build();
    }

    private Long obterAdminId(Principal principal) {
        if (principal == null || principal.getName() == null) {
            throw new IllegalStateException(
                    "Funcionário administrador não autenticado."
            );
        }

        return funcionarioService.buscarIdAdminAutenticado(
                principal.getName()
        );
    }

    private FuncionarioResponseDTO converterParaResponse(
            Funcionario funcionario) {
        return new FuncionarioResponseDTO(
                funcionario.getCdFuncionario(),
                funcionario.getCdEstabelecimento(),
                funcionario.getNmFuncionario(),
                funcionario.getNrTelefone(),
                funcionario.getDsEmailLogin(),
                funcionario.getTpPerfil(),
                funcionario.getFlTreinado(),
                funcionario.getStFuncionario(),
                funcionario.getDtCadastro()
        );
    }

    // =====================================================
    // LOGIN DO FUNCIONÁRIO
    // =====================================================

    @PostMapping("/login")
    public ResponseEntity<FuncionarioResponseDTO> autenticar(
            @Valid @RequestBody FuncionarioLoginDTO dto) {

        FuncionarioResponseDTO resposta =
                funcionarioService.autenticar(dto);

        return ResponseEntity.ok(resposta);
    }

}
