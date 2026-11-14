
package br.com.ari.service;

import br.com.ari.dto.FuncionarioResponseDTO;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class FuncionarioSessaoService {

    private static final long DURACAO_SESSAO_HORAS = 8;

    private static final String PERFIL_ADMIN = "ADMIN";
    private static final String PERFIL_ATENDENTE = "ATENDENTE";
    private static final String STATUS_ATIVO = "ATIVO";

    private final ConcurrentHashMap<String, Sessao> sessoes =
            new ConcurrentHashMap<>();

    // =====================================================
    // CRIAR SESSÃO
    // Somente funcionários ativos podem iniciar sessão.
    // A identificação é feita pelo ID do funcionário.
    // =====================================================

    public Sessao criar(FuncionarioResponseDTO funcionario) {

        validarDadosFuncionario(funcionario);

        LocalDateTime expiracao = LocalDateTime.now()
                .plusHours(DURACAO_SESSAO_HORAS);

        String uuid = UUID.randomUUID().toString();

        Sessao sessao = new Sessao(
                uuid,
                funcionario.cdFuncionario(),
                funcionario.dsEmailLogin(),
                funcionario.tpPerfil(),
                expiracao
        );

        sessoes.put(uuid, sessao);

        return sessao;
    }

    // =====================================================
    // BUSCAR SESSÃO ATIVA
    // Remove sessões expiradas automaticamente.
    // =====================================================

    public Optional<Sessao> buscarAtiva(String uuid) {

        if (uuid == null || uuid.isBlank()) {
            return Optional.empty();
        }

        Sessao sessao = sessoes.get(uuid);

        if (sessao == null) {
            return Optional.empty();
        }

        if (!sessao.expiracao().isAfter(LocalDateTime.now())) {

            sessoes.remove(uuid, sessao);

            return Optional.empty();
        }

        return Optional.of(sessao);
    }

    // =====================================================
    // ATUALIZAR DADOS DA SESSÃO
    // Atualiza e-mail e perfil sem renovar a expiração.
    // A sessão deve pertencer ao funcionário informado.
    // =====================================================

    public void atualizarDados(
            String uuid,
            FuncionarioResponseDTO funcionario) {

        validarDadosFuncionario(funcionario);

        Sessao sessao = buscarAtiva(uuid)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Sessão inválida ou expirada."
                        )
                );

        if (!sessao.cdFuncionario().equals(
                funcionario.cdFuncionario())) {

            throw new IllegalArgumentException(
                    "A sessão não pertence a este funcionário."
            );
        }

        if (!STATUS_ATIVO.equals(funcionario.stFuncionario())) {

            encerrar(uuid);

            throw new IllegalStateException(
                    "Funcionário inativo não pode manter uma sessão."
            );
        }

        Sessao sessaoAtualizada = new Sessao(
                sessao.uuid(),
                funcionario.cdFuncionario(),
                funcionario.dsEmailLogin(),
                funcionario.tpPerfil(),
                sessao.expiracao()
        );

        boolean atualizada = sessoes.replace(
                uuid,
                sessao,
                sessaoAtualizada
        );

        if (!atualizada) {
            throw new IllegalStateException(
                    "Não foi possível atualizar a sessão."
            );
        }
    }

    // =====================================================
    // ENCERRAR SESSÃO
    // Utilizado no logout.
    // =====================================================

    public void encerrar(String uuid) {

        if (uuid != null && !uuid.isBlank()) {
            sessoes.remove(uuid);
        }
    }

    // =====================================================
    // ENCERRAR TODAS AS SESSÕES DO FUNCIONÁRIO
    // Utilizado na inativação e exclusão.
    // =====================================================

    public void encerrarTodasDoFuncionario(
            Long cdFuncionario) {

        validarIdFuncionario(cdFuncionario);

        sessoes.entrySet().removeIf(
                entrada -> entrada.getValue()
                        .cdFuncionario()
                        .equals(cdFuncionario)
        );
    }

    // =====================================================
    // VALIDAR DADOS DO FUNCIONÁRIO
    // =====================================================

    private void validarDadosFuncionario(
            FuncionarioResponseDTO funcionario) {

        if (funcionario == null) {
            throw new IllegalArgumentException(
                    "Dados do funcionário não informados."
            );
        }

        validarIdFuncionario(funcionario.cdFuncionario());

        if (!STATUS_ATIVO.equals(funcionario.stFuncionario())) {
            throw new IllegalStateException(
                    "Funcionário inativo não pode iniciar ou manter uma sessão."
            );
        }

        if (funcionario.dsEmailLogin() == null
                || funcionario.dsEmailLogin().isBlank()) {

            throw new IllegalArgumentException(
                    "E-mail do funcionário não informado."
            );
        }

        if (!PERFIL_ADMIN.equals(funcionario.tpPerfil())
                && !PERFIL_ATENDENTE.equals(funcionario.tpPerfil())) {

            throw new IllegalArgumentException(
                    "Perfil de funcionário inválido."
            );
        }
    }

    // =====================================================
    // VALIDAR ID DO FUNCIONÁRIO
    // =====================================================

    private void validarIdFuncionario(Long cdFuncionario) {

        if (cdFuncionario == null || cdFuncionario <= 0) {
            throw new IllegalArgumentException(
                    "ID do funcionário inválido."
            );
        }
    }

    // =====================================================
    // DADOS DA SESSÃO
    // =====================================================

    public record Sessao(
            String uuid,
            Long cdFuncionario,
            String email,
            String perfil,
            LocalDateTime expiracao
    ) {}
}
