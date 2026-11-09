
package br.com.ari.service;

import br.com.ari.dao.SessaoWebDAO;
import br.com.ari.model.SessaoWeb;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class SessaoWebService {

    private final SessaoWebDAO sessaoWebDAO;

    public SessaoWebService(SessaoWebDAO sessaoWebDAO) {
        this.sessaoWebDAO = sessaoWebDAO;
    }

    // CRIAR SESSÃO
    public SessaoWeb criar(Long cdUsuario, long duracaoMinutos) {

        if (cdUsuario != null && cdUsuario <= 0) {
            throw new IllegalArgumentException("Usuário inválido.");
        }

        if (duracaoMinutos <= 0) {
            throw new IllegalArgumentException(
                    "A duração da sessão deve ser maior que zero."
            );
        }

        SessaoWeb sessao = SessaoWeb.builder()
                .dsUuid(UUID.randomUUID().toString())
                .cdUsuario(cdUsuario)
                .dtExpiracao(LocalDateTime.now().plusMinutes(duracaoMinutos))
                .stSessao("ATIVA")
                .build();

        int linhasAfetadas = sessaoWebDAO.criar(sessao);

        if (linhasAfetadas == 0 || sessao.getCdSessaoWeb() == null) {
            throw new IllegalStateException("Não foi possível criar a sessão.");
        }

        return sessaoWebDAO.buscarPorUuid(sessao.getDsUuid())
                .orElseThrow(() ->
                        new IllegalStateException("Erro ao recuperar sessão criada.")
                );
    }

    // BUSCAR SESSÃO POR UUID
    public Optional<SessaoWeb> buscarPorUuid(String uuid) {

        if (uuid == null || uuid.isBlank()) {
            throw new IllegalArgumentException("UUID não informado.");
        }

        return sessaoWebDAO.buscarPorUuid(uuid);
    }

    // VALIDAR SE A SESSÃO ESTÁ ATIVA
    public Optional<SessaoWeb> buscarSessaoAtiva(String uuid) {

        Optional<SessaoWeb> resultado = buscarPorUuid(uuid);

        if (resultado.isEmpty()) {
            return Optional.empty();
        }

        SessaoWeb sessao = resultado.get();

        if (!"ATIVA".equals(sessao.getStSessao())) {
            return Optional.empty();
        }

        if (!sessao.getDtExpiracao().isAfter(LocalDateTime.now())) {

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

        if (sessaoWebDAO.buscarPorId(id).isEmpty()) {
            throw new IllegalArgumentException("Sessão não encontrada.");
        }

        int linhasAfetadas = sessaoWebDAO.excluir(id);

        if (linhasAfetadas == 0) {
            throw new IllegalStateException(
                    "Não foi possível revogar a sessão."
            );
        }
    }

    private void validarId(Long id) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID da sessão inválido.");
        }
    }
}
