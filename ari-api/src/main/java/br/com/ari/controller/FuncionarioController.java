
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/funcionarios")
@Tag(name = "Funcionários", description = "Autenticação e administração dos funcionários de um estabelecimento.")
public class FuncionarioController {

    private final FuncionarioService funcionarioService;

    // CONSTRUTOR
    public FuncionarioController(
            FuncionarioService funcionarioService) {

        this.funcionarioService = funcionarioService;
    }

    @PostMapping
    @Operation(summary = "Cadastrar funcionário", description = "Cria um funcionário no estabelecimento do administrador autenticado.")
    public ResponseEntity<Void> criar(
            Principal principal,
            @Valid @RequestBody FuncionarioRequestDTO dto) {

        funcionarioService.criar(obterAdminId(principal), dto);
        return ResponseEntity.status(201).build();
    }

    @GetMapping
    @Operation(summary = "Listar funcionários", description = "Lista os funcionários do estabelecimento do administrador autenticado.")
    public ResponseEntity<List<FuncionarioResponseDTO>> buscarPorEstabelecimento(
            Principal principal) {

        return ResponseEntity.ok(funcionarioService
                .buscarPorEstabelecimento(obterAdminId(principal))
                .stream()
                .map(this::converterParaResponse)
                .toList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar funcionário", description = "Consulta um funcionário do estabelecimento do administrador autenticado.")
    public ResponseEntity<FuncionarioResponseDTO> buscarPorId(
            Principal principal,
            @PathVariable Long id) {

        return ResponseEntity.ok(converterParaResponse(
                funcionarioService.buscarPorId(obterAdminId(principal), id)
        ));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar funcionário", description = "Atualiza os dados editáveis de um funcionário.")
    public ResponseEntity<Void> atualizar(
            Principal principal,
            @PathVariable Long id,
            @Valid @RequestBody FuncionarioUpdateDTO dto) {

        funcionarioService.atualizar(obterAdminId(principal), id, dto);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/senha")
    @Operation(summary = "Alterar senha do funcionário", description = "Altera a senha de um funcionário.")
    public ResponseEntity<Void> atualizarSenha(
            Principal principal,
            @PathVariable Long id,
            @Valid @RequestBody FuncionarioSenhaUpdateDTO dto) {

        funcionarioService.atualizarSenha(obterAdminId(principal), id, dto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir funcionário", description = "Exclui um funcionário do estabelecimento.")
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
    @Operation(summary = "Autenticar funcionário", description = "Valida as credenciais de login do funcionário.")
    public ResponseEntity<FuncionarioResponseDTO> autenticar(
            @Valid @RequestBody FuncionarioLoginDTO dto) {

        FuncionarioResponseDTO resposta =
                funcionarioService.autenticar(dto);

        return ResponseEntity.ok(resposta);
    }

}
