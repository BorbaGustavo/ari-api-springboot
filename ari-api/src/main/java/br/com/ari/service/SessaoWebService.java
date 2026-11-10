
package br.com.ari.service;

import br.com.ari.dao.SessaoWebDAO;
import br.com.ari.model.SessaoWeb;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class SessaoWebService {

    // REGRA DO ARI: SESSÃO COM VALIDADE DE DUAS HORAS
    private static final long DURACAO_SESSAO_HORAS = 2;

    private final SessaoWebDAO sessaoWebDAO;

    public SessaoWebService(SessaoWebDAO sessaoWebDAO) {
        this.sessaoWebDAO = sessaoWebDAO;
    }

    // CRIAR SESSÃO
    public SessaoWeb criar(Long cdUsuario) {

        validarUsuarioId(cdUsuario);

        LocalDateTime dtCriacao = LocalDateTime.now();

        SessaoWeb sessao = SessaoWeb.builder()
                .cdUsuario(cdUsuario)
                .dsUuid(UUID.randomUUID().toString())
                .dtExpiracao(dtCriacao.plusHours(DURACAO_SESSAO_HORAS))
                .stSessao("ATIVA")
                .build();

        int linhasAfetadas = sessaoWebDAO.criar(sessao);

        if (linhasAfetadas == 0 || sessao.getCdSessaoWeb() == null) {
            throw new IllegalStateException(
                    "Não foi possível criar a sessão."
            );
        }

        // RECUPERAR SESSÃO CADASTRADA
        return sessaoWebDAO.buscarPorId(sessao.getCdSessaoWeb())
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Erro ao recuperar sessão criada."
                        )
                );
    }

    // BUSCAR SESSÃO POR UUID
    public Optional<SessaoWeb> buscarPorUuid(String uuid) {

        if (uuid == null || uuid.isBlank()) {
            throw new IllegalArgumentException(
                    "UUID não informado."
            );
        }

        return sessaoWebDAO.buscarPorUuid(uuid);
    }

    // VALIDAR SE A SESSÃO ESTÁ ATIVA E DENTRO DA VALIDADE
    public Optional<SessaoWeb> buscarSessaoAtiva(String uuid) {

        Optional<SessaoWeb> resultado = buscarPorUuid(uuid);

        if (resultado.isEmpty()) {
            return Optional.empty();
        }

        SessaoWeb sessao = resultado.get();

        // VERIFICAR STATUS
        if (!"ATIVA".equals(sessao.getStSessao())) {
            return Optional.empty();
        }

        // VERIFICAR EXPIRAÇÃO
        LocalDateTime agora = LocalDateTime.now();

        if (sessao.getDtExpiracao() == null
                || !sessao.getDtExpiracao().isAfter(agora)) {

            sessaoWebDAO.atualizarStatus(
                    sessao.getCdSessaoWeb(),
                    "EXPIRADA"
            );

            return Optional.empty();
        }

        return Optional.of(sessao);
    }

    // REVOGAR SESSÃO
    public void revogar(Long id) {

        validarId(id);

        SessaoWeb sessao = sessaoWebDAO.buscarPorId(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Sessão não encontrada."
                        )
                );

        if (!"ATIVA".equals(sessao.getStSessao())) {
            throw new IllegalStateException(
                    "A sessão não está ativa para revogação."
            );
        }

        int linhasAfetadas = sessaoWebDAO.atualizarStatus(
                id,
                "REVOGADA"
        );

        if (linhasAfetadas == 0) {
            throw new IllegalStateException(
                    "Não foi possível revogar a sessão."
            );
        }
    }

    // VALIDAR ID DA USUÁRIA
    private void validarUsuarioId(Long cdUsuario) {

        if (cdUsuario == null || cdUsuario <= 0) {
            throw new IllegalArgumentException(
                    "Usuário inválido."
            );
        }
    }

    // VALIDAR ID DA SESSÃO
    private void validarId(Long id) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    "ID da sessão inválido."
            );
        }
    }
}
