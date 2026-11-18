
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

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
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
import java.util.Map;

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
// ATUALIZAR DADOS DO FUNCIONÁRIO
// ADMIN E ATENDENTE
// =====================================================

    @PutMapping("/{id}")
    @Operation(
            summary = "Atualizar dados do funcionário",
            description = "ADMIN pode atualizar seus próprios dados ou os de funcionários do estabelecimento. ATENDENTE pode atualizar somente os próprios dados."
    )
    @Parameter(
            name = HEADER_SESSAO,
            in = ParameterIn.HEADER,
            required = true,
            description = "UUID da sessão autenticada."
    )
    public ResponseEntity<Void> atualizar(
            Principal principal,
            @PathVariable Long id,
            @RequestHeader(HEADER_SESSAO) String uuid,
            @Valid @RequestBody FuncionarioUpdateDTO dto) {

        Long cdFuncionarioAutenticado = obterFuncionarioId(principal);

        // Atualiza os dados conforme as permissões do funcionário.
        funcionarioService.atualizar(
                cdFuncionarioAutenticado,
                id,
                dto
        );

        // Sincroniza a sessão somente quando o funcionário
        // estiver atualizando os próprios dados.
        if (id.equals(cdFuncionarioAutenticado)) {

            FuncionarioResponseDTO funcionarioAtualizado =
                    funcionarioService.buscarFuncionarioAutenticado(
                            cdFuncionarioAutenticado
                    );

            funcionarioSessaoService.atualizarDados(
                    uuid.trim(),
                    funcionarioAtualizado
            );
        }

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

    //Precisei fazer essa gambiarra pq já fiz a doc não queria criar outro DTO
    //SIM eu sei que ta errado mas, quebra meu galho ai.
    @PatchMapping("/{id}/status")
    @Operation(
            summary = "Ativar ou inativar funcionário",
            description = "Permite ao ADMIN ativar ou inativar funcionários do próprio estabelecimento."
    )
    @Parameter(
            name = HEADER_SESSAO,
            in = ParameterIn.HEADER,
            required = true,
            description = "UUID retornado no login do administrador."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Informe o status desejado do funcionário.",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(
                            example = "{\"stFuncionario\":\"INATIVO\"}"
                    ),
                    examples = {
                            @ExampleObject(
                                    name = "Inativar funcionário",
                                    value = "{\"stFuncionario\":\"INATIVO\"}"
                            ),
                            @ExampleObject(
                                    name = "Ativar funcionário",
                                    value = "{\"stFuncionario\":\"ATIVO\"}"
                            )
                    }
            )
    )
    public ResponseEntity<Void> atualizarStatus(
            Principal principal,
            @PathVariable Long id,
            @RequestHeader(HEADER_SESSAO) String uuid,
            @RequestBody Map<String, String> dados) {

        if (dados == null || dados.get("stFuncionario") == null) {
            throw new IllegalArgumentException(
                    "O status do funcionário é obrigatório."
            );
        }

        funcionarioService.atualizarStatus(
                obterAdminId(principal),
                id,
                dados.get("stFuncionario")
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
