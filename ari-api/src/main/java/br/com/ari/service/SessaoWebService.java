
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

    // CREATE
    public int criar(SessaoWeb sessao) {

        if (sessao == null) {
            throw new IllegalArgumentException(
                    "Sessão não informada."
            );
        }

        // Gera um UUID caso não tenha sido informado
        if (sessao.getDsUuid() == null ||
                sessao.getDsUuid().isBlank()) {

            sessao.setDsUuid(UUID.randomUUID().toString());
        }

        // Verifica a data de expiração
        if (sessao.getDtExpiracao() == null ||
                !sessao.getDtExpiracao().isAfter(LocalDateTime.now())) {

            throw new IllegalArgumentException(
                    "A data de expiração deve ser futura."
            );
        }

        // Usuário é opcional, mas se informado deve ter ID válido
        if (sessao.getCdUsuario() != null &&
                sessao.getCdUsuario() <= 0) {

            throw new IllegalArgumentException(
                    "ID do usuário inválido."
            );
        }

        // Define o status inicial
        if (sessao.getStSessao() == null ||
                sessao.getStSessao().isBlank()) {

            sessao.setStSessao("ATIVA");
        }

        if (!sessao.getStSessao().equals("ATIVA")) {
            throw new IllegalArgumentException(
                    "Uma nova sessão deve iniciar como ATIVA."
            );
        }

        return sessaoWebDAO.criar(sessao);
    }

    // READ
    public Optional<SessaoWeb> buscarPorId(Long id) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID inválido.");
        }

        return sessaoWebDAO.buscarPorId(id);
    }

    // UPDATE
    public int atualizar(SessaoWeb sessao) {

        if (sessao == null ||
                sessao.getCdSessaoWeb() == null ||
                sessao.getCdSessaoWeb() <= 0) {

            throw new IllegalArgumentException(
                    "Sessão ou ID inválido."
            );
        }

        if (sessaoWebDAO.buscarPorId(
                sessao.getCdSessaoWeb()
        ).isEmpty()) {

            throw new IllegalArgumentException(
                    "Sessão não encontrada."
            );
        }

        if (sessao.getDtExpiracao() == null ||
                !sessao.getDtExpiracao().isAfter(LocalDateTime.now())) {

            throw new IllegalArgumentException(
                    "A data de expiração deve ser futura."
            );
        }

        if (sessao.getStSessao() == null ||
                !(sessao.getStSessao().equals("ATIVA") ||
                        sessao.getStSessao().equals("EXPIRADA") ||
                        sessao.getStSessao().equals("REVOGADA"))) {

            throw new IllegalArgumentException(
                    "Status da sessão inválido."
            );
        }

        return sessaoWebDAO.atualizar(sessao);
    }

    // DELETE
    public int excluir(Long id) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID inválido.");
        }

        if (sessaoWebDAO.buscarPorId(id).isEmpty()) {
            throw new IllegalArgumentException(
                    "Sessão não encontrada."
            );
        }

        return sessaoWebDAO.excluir(id);
    }
}
