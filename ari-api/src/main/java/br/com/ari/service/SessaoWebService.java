
package br.com.ari.service;

import br.com.ari.dao.SessaoWebDAO;
import br.com.ari.model.SessaoWeb;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

@Service
public class SessaoWebService {

    private static final String STATUS_ATIVA = "ATIVA";
    private static final String STATUS_EXPIRADA = "EXPIRADA";
    private static final String STATUS_REVOGADA = "REVOGADA";
    private static final ZoneId FUSO_HORARIO = ZoneId.of("America/Sao_Paulo");

    // PRAZOS DIFERENTES CONFORME O TIPO DE SESSÃO
    private static final long DURACAO_SESSAO_ANONIMA_HORAS = 2;
    private static final long DURACAO_SESSAO_AUTENTICADA_HORAS = 8;

    private final SessaoWebDAO sessaoWebDAO;

    // CONSTRUTOR
    public SessaoWebService(SessaoWebDAO sessaoWebDAO) {
        this.sessaoWebDAO = sessaoWebDAO;
    }

    // ==================================================
    // CRIAR SESSÃO
    // ANÔNIMA OU AUTENTICADA
    // ==================================================

    public SessaoWeb criar(Long cdUsuario) {

        // VALIDAR ID SOMENTE QUANDO INFORMADO
        if (cdUsuario != null) {
            validarUsuarioId(cdUsuario);
        }

        LocalDateTime dtCriacao = LocalDateTime.now(FUSO_HORARIO);

        // DEFINIR DURAÇÃO CONFORME O TIPO DE SESSÃO
        long duracaoHoras = cdUsuario == null
                ? DURACAO_SESSAO_ANONIMA_HORAS
                : DURACAO_SESSAO_AUTENTICADA_HORAS;

        // MONTAR SESSÃO
        SessaoWeb sessao = SessaoWeb.builder()
                .cdUsuario(cdUsuario)
                .dsUuid(UUID.randomUUID().toString())
                .dtCriacao(dtCriacao)
                .dtExpiracao(dtCriacao.plusHours(duracaoHoras))
                .stSessao(STATUS_ATIVA)
                .build();

        // SALVAR NO BANCO
        int linhasAfetadas = sessaoWebDAO.criar(sessao);

        if (linhasAfetadas == 0
                || sessao.getCdSessaoWeb() == null) {

            throw new IllegalStateException(
                    "Não foi possível criar a sessão."
            );
        }

        // RECUPERAR SESSÃO CADASTRADA
        return sessaoWebDAO.buscarPorId(
                sessao.getCdSessaoWeb()
        ).orElseThrow(() ->
                new IllegalStateException(
                        "Erro ao recuperar sessão criada."
                )
        );
    }

    // Cria uma sessão sem vínculo com usuária para acesso ao mapa público.
    public SessaoWeb criarAnonima() {
        return criar(null);
    }

    // ==================================================
    // BUSCAR SESSÃO POR UUID
    // ==================================================

    public Optional<SessaoWeb> buscarPorUuid(String uuid) {

        validarUuid(uuid);

        return sessaoWebDAO.buscarPorUuid(uuid.trim());
    }

    // ==================================================
    // BUSCAR SESSÃO ATIVA E DENTRO DA VALIDADE
    // ==================================================

    public Optional<SessaoWeb> buscarSessaoAtiva(String uuid) {

        Optional<SessaoWeb> resultado = buscarPorUuid(uuid);

        if (resultado.isEmpty()) {
            return Optional.empty();
        }

        SessaoWeb sessao = resultado.get();

        // VERIFICAR STATUS
        if (!STATUS_ATIVA.equals(sessao.getStSessao())) {
            return Optional.empty();
        }

        // VERIFICAR DATA DE EXPIRAÇÃO
        LocalDateTime agora = LocalDateTime.now(FUSO_HORARIO);

        if (sessao.getDtExpiracao() == null
                || !sessao.getDtExpiracao().isAfter(agora)) {

            sessaoWebDAO.atualizarStatus(
                    sessao.getCdSessaoWeb(),
                    STATUS_EXPIRADA
            );

            return Optional.empty();
        }

        return Optional.of(sessao);
    }

    // ==================================================
    // REVOGAR UMA SESSÃO
    // ==================================================

    public void revogar(Long id) {

        validarId(id);

        SessaoWeb sessao = sessaoWebDAO.buscarPorId(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Sessão não encontrada."
                        )
                );

        if (!STATUS_ATIVA.equals(sessao.getStSessao())) {
            throw new IllegalStateException(
                    "A sessão não está ativa para revogação."
            );
        }

        int linhasAfetadas = sessaoWebDAO.atualizarStatus(
                id,
                STATUS_REVOGADA
        );

        if (linhasAfetadas == 0) {
            throw new IllegalStateException(
                    "Não foi possível revogar a sessão."
            );
        }
    }

    // ==================================================
    // REVOGAR TODAS AS SESSÕES ATIVAS DE UM USUÁRIO
    // ==================================================

    public int revogarSessoesAtivasUsuario(Long cdUsuario) {

        validarUsuarioId(cdUsuario);

        return sessaoWebDAO.revogarSessoesAtivasUsuario(
                cdUsuario
        );
    }

    // ==================================================
    // VALIDAÇÕES
    // ==================================================

    private void validarUuid(String uuid) {

        if (uuid == null || uuid.isBlank()) {
            throw new IllegalArgumentException(
                    "UUID não informado."
            );
        }

        String identificador = uuid.trim();

        // DS_UUID é VARCHAR2(36). O identificador é opaco:
        // aceita UUID padrão e identificadores de teste como "uuid-001".
        if (identificador.length() > 36) {
            throw new IllegalArgumentException(
                    "Identificador da sessão deve ter no máximo 36 caracteres."
            );
        }

        if (identificador.chars().anyMatch(Character::isWhitespace)) {
            throw new IllegalArgumentException(
                    "Identificador da sessão não pode conter espaços."
            );
        }
    }

    private void validarUsuarioId(Long cdUsuario) {

        if (cdUsuario == null || cdUsuario <= 0) {
            throw new IllegalArgumentException(
                    "Usuário inválido."
            );
        }
    }

    private void validarId(Long id) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    "ID da sessão inválido."
            );
        }
    }
}
