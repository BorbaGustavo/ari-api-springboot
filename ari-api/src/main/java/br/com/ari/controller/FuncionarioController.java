
package br.com.ari.controller;

import br.com.ari.dto.FuncionarioLoginDTO;
import br.com.ari.dto.FuncionarioLoginResponseDTO;
import br.com.ari.dto.FuncionarioRequestDTO;
import br.com.ari.dto.FuncionarioResponseDTO;
import br.com.ari.dto.FuncionarioSenhaUpdateDTO;
import br.com.ari.dto.FuncionarioUpdateDTO;
import br.com.ari.dto.FuncionarioTreinamentoDTO;
import br.com.ari.exception.AcessoNegadoException;
import br.com.ari.exception.ConflitoException;
import br.com.ari.exception.NaoAutenticadoException;

import br.com.ari.service.FuncionarioService;
import br.com.ari.service.FuncionarioSessaoService;
import br.com.ari.service.FuncionarioSessaoService.Sessao;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/funcionarios")
@Tag(
        name = "Funcionários",
        description = "Autenticação, gerenciamento e atualização dos dados dos funcionários."
)
public class FuncionarioController {

    private static final String HEADER_SESSAO =
            "X-Funcionario-Session-UUID";

    private final FuncionarioService funcionarioService;
    private final FuncionarioSessaoService funcionarioSessaoService;

    public FuncionarioController(
            FuncionarioService funcionarioService,
            FuncionarioSessaoService funcionarioSessaoService) {

        this.funcionarioService = funcionarioService;
        this.funcionarioSessaoService = funcionarioSessaoService;
    }

    // =====================================================
    // CADASTRAR FUNCIONÁRIO
    // ADMIN
    // =====================================================

    @PostMapping
    @Operation(
            summary = "Cadastrar funcionário",
            description = "Cadastra um novo ATENDENTE no estabelecimento do administrador autenticado."
    )
    @Parameter(
            name = HEADER_SESSAO,
            in = ParameterIn.HEADER,
            required = true,
            description = "UUID retornado no login do administrador."
    )
    public ResponseEntity<Void> criar(
            Principal principal,
            @Valid @RequestBody FuncionarioRequestDTO dto) {

        funcionarioService.criar(
                obterAdminId(principal),
                dto
        );

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // =====================================================
    // LISTAR FUNCIONÁRIOS DO ESTABELECIMENTO
    // ADMIN
    // =====================================================

    @GetMapping
    @Operation(
            summary = "Listar funcionários",
            description = "Lista os funcionários do estabelecimento do administrador autenticado."
    )
    public ResponseEntity<List<FuncionarioResponseDTO>> buscarPorEstabelecimento(
            Principal principal) {

        List<FuncionarioResponseDTO> funcionarios =
                funcionarioService.buscarPorEstabelecimento(
                        obterAdminId(principal)
                );

        return ResponseEntity.ok(funcionarios);
    }

    // =====================================================
    // BUSCAR FUNCIONÁRIO POR ID
    // ADMIN
    // =====================================================

    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar funcionário por ID",
            description = "Consulta um funcionário pertencente ao estabelecimento do administrador."
    )
    public ResponseEntity<FuncionarioResponseDTO> buscarPorId(
            Principal principal,
            @PathVariable Long id) {

        FuncionarioResponseDTO funcionario =
                funcionarioService.buscarPorId(
                        obterAdminId(principal),
                        id
                );

        return ResponseEntity.ok(funcionario);
    }

    // =====================================================
    // BUSCAR PRÓPRIO PERFIL
    // ADMIN E ATENDENTE
    // =====================================================

    @GetMapping("/me")
    @Operation(
            summary = "Consultar próprio perfil",
            description = "Retorna os dados do funcionário autenticado."
    )
    public ResponseEntity<FuncionarioResponseDTO> buscarMeuPerfil(
            Principal principal) {

        FuncionarioResponseDTO funcionario =
                funcionarioService.buscarFuncionarioAutenticado(
                        obterFuncionarioId(principal)
                );

        return ResponseEntity.ok(funcionario);
    }

    // =====================================================
    // ATUALIZAR PRÓPRIO PERFIL
    // ADMIN E ATENDENTE
    // =====================================================

    @PutMapping("/me")
    @Operation(
            summary = "Atualizar próprio perfil",
            description = "Permite atualizar nome, telefone e e-mail do próprio funcionário."
    )
    @Parameter(
            name = HEADER_SESSAO,
            in = ParameterIn.HEADER,
            required = true,
            description = "UUID da sessão autenticada."
    )
    public ResponseEntity<Void> atualizarMeuPerfil(
            Principal principal,
            @RequestHeader(HEADER_SESSAO) String uuid,
            @Valid @RequestBody FuncionarioUpdateDTO dto) {

        Long cdFuncionario = obterFuncionarioId(principal);

        // Atualiza somente os dados pessoais.
        funcionarioService.atualizar(
                cdFuncionario,
                dto
        );

        // Recupera os dados atualizados no banco.
        FuncionarioResponseDTO funcionarioAtualizado =
                funcionarioService.buscarFuncionarioAutenticado(
                        cdFuncionario
                );

        // Sincroniza os dados da sessão sem renovar sua expiração.
        funcionarioSessaoService.atualizarDados(
                uuid.trim(),
                funcionarioAtualizado
        );

        return ResponseEntity.noContent().build();
    }

    // =====================================================
    // ALTERAR PRÓPRIA SENHA
    // ADMIN E ATENDENTE
    // =====================================================

    @PatchMapping("/me/senha")
    @Operation(
            summary = "Alterar própria senha",
            description = "Permite que o funcionário autenticado altere somente a própria senha."
    )
    public ResponseEntity<Void> atualizarMinhaSenha(
            Principal principal,
            @Valid @RequestBody FuncionarioSenhaUpdateDTO dto) {

        funcionarioService.atualizarSenha(
                obterFuncionarioId(principal),
                dto
        );

        return ResponseEntity.noContent().build();
    }

    // =====================================================
    // ATUALIZAR TREINAMENTO DO FUNCIONÁRIO
    // ADMIN
    // =====================================================

    @PatchMapping("/{id}/treinamento")
    @Operation(
            summary = "Atualizar treinamento do funcionário",
            description = "Permite ao ADMIN marcar outro funcionário ativo do estabelecimento como treinado (S) ou não treinado (N)."
    )
    public ResponseEntity<Void> atualizarTreinamento(
            Principal principal,
            @PathVariable Long id,
            @Valid @RequestBody FuncionarioTreinamentoDTO dto) {

        funcionarioService.atualizarTreinamento(
                obterAdminId(principal),
                id,
                dto.flTreinado()
        );

        return ResponseEntity.noContent().build();
    }

    // =====================================================
    // INATIVAR FUNCIONÁRIO
    // ADMIN
    // =====================================================

    @PatchMapping("/{id}/inativar")
    @Operation(
            summary = "Inativar funcionário",
            description = "Inativa outro funcionário do estabelecimento e encerra suas sessões ativas."
    )
    public ResponseEntity<Void> inativar(
            Principal principal,
            @PathVariable Long id) {

        funcionarioService.inativar(
                obterAdminId(principal),
                id
        );

        return ResponseEntity.noContent().build();
    }

    // =====================================================
    // EXCLUIR FUNCIONÁRIO
    // ADMIN
    // =====================================================

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Excluir funcionário",
            description = "Exclui fisicamente outro funcionário do estabelecimento do administrador."
    )
    public ResponseEntity<Void> excluir(
            Principal principal,
            @PathVariable Long id) {

        funcionarioService.excluir(
                obterAdminId(principal),
                id
        );

        return ResponseEntity.noContent().build();
    }

    // =====================================================
    // LOGIN DO FUNCIONÁRIO
    // PÚBLICO
    // =====================================================

    @PostMapping("/login")
    @Operation(
            summary = "Autenticar funcionário",
            description = "Valida as credenciais e cria uma sessão para o funcionário."
    )
    public ResponseEntity<FuncionarioLoginResponseDTO> autenticar(
            @Valid @RequestBody FuncionarioLoginDTO dto) {

        FuncionarioResponseDTO funcionario =
                funcionarioService.autenticar(dto);

        Sessao sessao = funcionarioSessaoService.criar(funcionario);

        FuncionarioLoginResponseDTO resposta =
                new FuncionarioLoginResponseDTO(
                        funcionario,
                        sessao.uuid(),
                        sessao.expiracao()
                );

        return ResponseEntity.ok(resposta);
    }

    // =====================================================
    // LOGOUT DO FUNCIONÁRIO
    // ADMIN E ATENDENTE
    // =====================================================

    @PostMapping("/logout")
    @Operation(
            summary = "Encerrar sessão",
            description = "Encerra a sessão do funcionário autenticado."
    )
    @Parameter(
            name = HEADER_SESSAO,
            in = ParameterIn.HEADER,
            required = true,
            description = "UUID retornado no login."
    )
    public ResponseEntity<Void> logout(
            Principal principal,
            @RequestHeader(HEADER_SESSAO) String uuid) {

        Long cdFuncionario = obterFuncionarioId(principal);

        Sessao sessao = funcionarioSessaoService
                .buscarAtiva(uuid.trim())
                .orElse(null);

        // Logout de sessão inexistente ou expirada é idempotente.
        if (sessao == null) {
            return ResponseEntity.noContent().build();
        }

        // Impede encerrar a sessão de outro funcionário.
        if (!sessao.cdFuncionario().equals(cdFuncionario)) {
            throw new AcessoNegadoException(
                    "A sessão informada não pertence ao funcionário autenticado."
            );
        }

        funcionarioSessaoService.encerrar(uuid.trim());

        return ResponseEntity.noContent().build();
    }

    // =====================================================
    // IDENTIFICAR FUNCIONÁRIO AUTENTICADO
    // =====================================================

    private Long obterFuncionarioId(Principal principal) {

        if (principal == null || principal.getName() == null) {
            throw new NaoAutenticadoException(
                    "Funcionário não autenticado."
            );
        }

        try {

            return Long.valueOf(principal.getName());

        } catch (NumberFormatException e) {

            throw new NaoAutenticadoException(
                    "Identificação do funcionário autenticado inválida."
            );
        }
    }

    // =====================================================
    // VALIDAR ADMINISTRADOR AUTENTICADO
    // =====================================================

    private Long obterAdminId(Principal principal) {

        Long cdFuncionario = obterFuncionarioId(principal);

        return funcionarioService.buscarIdAdminAutenticado(
                cdFuncionario
        );
    }
}
