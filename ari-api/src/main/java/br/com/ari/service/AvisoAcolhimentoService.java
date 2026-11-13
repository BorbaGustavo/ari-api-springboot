
package br.com.ari.service;

import br.com.ari.dao.AvisoAcolhimentoDAO;
import br.com.ari.dao.UsuarioDAO;
import br.com.ari.dao.EstabelecimentoDAO;
import br.com.ari.dao.DisponibilidadeDAO;
import br.com.ari.dao.FuncionarioDAO;

import br.com.ari.dto.AvisoAcolhimentoRequestDTO;
import br.com.ari.dto.AvisoAcolhimentoResponseDTO;

import br.com.ari.model.AvisoAcolhimento;
import br.com.ari.model.Usuario;
import br.com.ari.model.SessaoWeb;
import br.com.ari.model.Estabelecimento;
import br.com.ari.model.Disponibilidade;
import br.com.ari.model.Funcionario;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class AvisoAcolhimentoService {

    private static final ZoneId FUSO_HORARIO =
            ZoneId.of("America/Sao_Paulo");

    private final AvisoAcolhimentoDAO avisoAcolhimentoDAO;
    private final UsuarioDAO usuarioDAO;
    private final SessaoWebService sessaoWebService;
    private final EstabelecimentoDAO estabelecimentoDAO;
    private final DisponibilidadeDAO disponibilidadeDAO;
    private final FuncionarioDAO funcionarioDAO;

    // CONSTRUTOR
    public AvisoAcolhimentoService(
            AvisoAcolhimentoDAO avisoAcolhimentoDAO,
            UsuarioDAO usuarioDAO,
            SessaoWebService sessaoWebService,
            EstabelecimentoDAO estabelecimentoDAO,
            DisponibilidadeDAO disponibilidadeDAO,
            FuncionarioDAO funcionarioDAO) {

        this.avisoAcolhimentoDAO = avisoAcolhimentoDAO;
        this.usuarioDAO = usuarioDAO;
        this.sessaoWebService = sessaoWebService;
        this.estabelecimentoDAO = estabelecimentoDAO;
        this.disponibilidadeDAO = disponibilidadeDAO;
        this.funcionarioDAO = funcionarioDAO;
    }

    // =====================================================
    // CRIAR AVISO DE ACOLHIMENTO
    // =====================================================

    public AvisoAcolhimentoResponseDTO criar(
            String uuidSessao,
            AvisoAcolhimentoRequestDTO dto) {

        // VALIDAR SESSÃO E IDENTIFICAR USUÁRIA
        SessaoWeb sessao = obterSessaoValida(uuidSessao);

        if (dto == null) {
            throw new IllegalArgumentException(
                    "Dados do aviso não informados."
            );
        }

        validarId(dto.cdEstabelecimento());

        // VALIDAR TEMPO ESTIMADO INFORMADO
        if (dto.nrTempoEstimado() != null
                && (dto.nrTempoEstimado() <= 0
                || dto.nrTempoEstimado() > 99999)) {

            throw new IllegalArgumentException(
                    "Tempo estimado deve estar entre 1 e 99999 minutos."
            );
        }

        // DEFINIR TEMPO ESTIMADO
        // Caso não informado, utilizar 60 minutos.
        Integer tempoEstimado = dto.nrTempoEstimado() != null
                ? dto.nrTempoEstimado()
                : 60;

        // EXPIRAR AVISOS VENCIDOS ANTES DE VERIFICAR
        // SE A USUÁRIA POSSUI UM AVISO EM ANDAMENTO
        expirarAvisosVencidos();

        // VERIFICAR SE A USUÁRIA JÁ POSSUI AVISO ATIVO
        if (avisoAcolhimentoDAO.buscarAvisoAtivoPorUsuario(
                sessao.getCdUsuario()).isPresent()) {

            throw new IllegalStateException(
                    "Você já possui uma solicitação de acolhimento em andamento."
            );
        }

        // BUSCAR ESTABELECIMENTO
        Estabelecimento estabelecimento =
                estabelecimentoDAO.buscarPorId(
                        dto.cdEstabelecimento()
                ).orElseThrow(() ->
                        new IllegalArgumentException(
                                "Estabelecimento não encontrado."
                        )
                );

        // VALIDAR SE A EMPRESA PODE RECEBER ACOLHIMENTOS
        validarEstabelecimentoDisponivel(estabelecimento);

        // CRIAR AVISO
        // CD_USUARIO E CD_SESSAO_WEB VÊM DA SESSÃO VALIDADA.
        // DT_AVISO E ST_AVISO SÃO DEFINIDOS PELOS DEFAULTS DO BANCO.
        AvisoAcolhimento aviso = AvisoAcolhimento.builder()
                .cdUsuario(sessao.getCdUsuario())
                .cdSessaoWeb(sessao.getCdSessaoWeb())
                .cdEstabelecimento(
                        estabelecimento.getCdEstabelecimento()
                )
                .nrTempoEstimado(tempoEstimado)
                .build();

        try {

            int linhasAfetadas = avisoAcolhimentoDAO.criar(aviso);

            if (linhasAfetadas == 0
                    || aviso.getCdAviso() == null) {

                throw new IllegalStateException(
                        "Não foi possível registrar o aviso."
                );
            }

        } catch (DuplicateKeyException e) {
            throw new IllegalStateException(
                    "Você já possui uma solicitação de acolhimento em andamento.",
                    e
            );
        }

        // RECUPERAR AVISO CADASTRADO
        AvisoAcolhimento cadastrado =
                avisoAcolhimentoDAO.buscarPorId(
                        aviso.getCdAviso()
                ).orElseThrow(() ->
                        new IllegalStateException(
                                "Erro ao recuperar aviso cadastrado."
                        )
                );

        return converterParaResponse(cadastrado);
    }

    // =====================================================
    // CONSULTAR AVISOS DA USUÁRIA
    // =====================================================

    // BUSCAR HISTÓRICO DA USUÁRIA AUTENTICADA
    public List<AvisoAcolhimentoResponseDTO> buscarPorUsuario(
            String uuidSessao) {

        SessaoWeb sessao = obterSessaoValida(uuidSessao);

        expirarAvisosVencidos();

        return avisoAcolhimentoDAO.buscarPorUsuario(
                        sessao.getCdUsuario()
                ).stream()
                .map(this::converterParaResponse)
                .toList();
    }

    // BUSCAR AVISO ESPECÍFICO DA USUÁRIA
    public AvisoAcolhimentoResponseDTO buscarPorIdParaUsuario(
            String uuidSessao,
            Long cdAviso) {

        SessaoWeb sessao = obterSessaoValida(uuidSessao);

        validarId(cdAviso);

        expirarAvisosVencidos();

        AvisoAcolhimento aviso = buscarAvisoExistente(cdAviso);

        // GARANTIR QUE O AVISO PERTENCE À USUÁRIA
        if (!Objects.equals(
                aviso.getCdUsuario(),
                sessao.getCdUsuario())) {

            throw new IllegalArgumentException(
                    "Este aviso não pertence à sua conta."
            );
        }

        return converterParaResponse(aviso);
    }

    // BUSCAR AVISO ATIVO DA USUÁRIA
    public Optional<AvisoAcolhimentoResponseDTO> buscarAvisoAtivo(
            String uuidSessao) {

        SessaoWeb sessao = obterSessaoValida(uuidSessao);

        expirarAvisosVencidos();

        return avisoAcolhimentoDAO.buscarAvisoAtivoPorUsuario(
                sessao.getCdUsuario()
        ).map(this::converterParaResponse);
    }

    // =====================================================
    // CONSULTAR AVISOS DO ESTABELECIMENTO
    // =====================================================

    // LISTAR AVISOS DA EMPRESA DO FUNCIONÁRIO AUTENTICADO
    public List<AvisoAcolhimentoResponseDTO> buscarPorEstabelecimento(
            Long cdFuncionario) {

        Funcionario funcionario = buscarFuncionarioAtivo(cdFuncionario);

        expirarAvisosVencidos();

        return avisoAcolhimentoDAO.buscarPorEstabelecimento(
                        funcionario.getCdEstabelecimento()
                ).stream()
                .map(this::converterParaResponse)
                .toList();
    }

    // LISTAR SOMENTE AVISOS A_CAMINHO
    // Utilizado no painel de atendimento da empresa.
    public List<AvisoAcolhimentoResponseDTO> buscarPendentesPorEstabelecimento(
            Long cdFuncionario) {

        Funcionario funcionario = buscarFuncionarioAtivo(cdFuncionario);

        expirarAvisosVencidos();

        return avisoAcolhimentoDAO.buscarPendentesPorEstabelecimento(
                        funcionario.getCdEstabelecimento()
                ).stream()
                .map(this::converterParaResponse)
                .toList();
    }

    // =====================================================
    // CANCELAR AVISO
    // =====================================================

    // SOMENTE A USUÁRIA DONA DO AVISO PODE CANCELAR
    public AvisoAcolhimentoResponseDTO cancelar(
            String uuidSessao,
            Long cdAviso) {

        SessaoWeb sessao = obterSessaoValida(uuidSessao);

        validarId(cdAviso);

        expirarAvisosVencidos();

        AvisoAcolhimento aviso = buscarAvisoExistente(cdAviso);

        // VERIFICAR PROPRIEDADE DO AVISO
        if (!Objects.equals(
                aviso.getCdUsuario(),
                sessao.getCdUsuario())) {

            throw new IllegalArgumentException(
                    "Você não pode cancelar o aviso de outra usuária."
            );
        }

        if (!"A_CAMINHO".equals(aviso.getStAviso())) {
            throw new IllegalStateException(
                    "Somente avisos A_CAMINHO podem ser cancelados."
            );
        }

        int linhasAfetadas = avisoAcolhimentoDAO.cancelar(
                cdAviso,
                sessao.getCdUsuario()
        );

        if (linhasAfetadas == 0) {
            throw new IllegalStateException(
                    "Não foi possível cancelar o aviso."
            );
        }

        return converterParaResponse(
                buscarAvisoExistente(cdAviso)
        );
    }

    // =====================================================
    // CONFIRMAR ACOLHIMENTO
    // =====================================================

    // SOMENTE FUNCIONÁRIO ATIVO E TREINADO PODE CONFIRMAR
    public AvisoAcolhimentoResponseDTO confirmar(
            Long cdFuncionario,
            Long cdAviso) {

        Funcionario funcionario = buscarFuncionarioAtivo(cdFuncionario);

        // VALIDAR TREINAMENTO
        if (!"S".equals(funcionario.getFlTreinado())) {
            throw new IllegalStateException(
                    "Somente funcionários treinados podem confirmar acolhimentos."
            );
        }

        validarId(cdAviso);

        expirarAvisosVencidos();

        AvisoAcolhimento aviso = buscarAvisoExistente(cdAviso);

        // VERIFICAR SE O AVISO PERTENCE À EMPRESA DO FUNCIONÁRIO
        if (!Objects.equals(
                aviso.getCdEstabelecimento(),
                funcionario.getCdEstabelecimento())) {

            throw new IllegalArgumentException(
                    "Este aviso não pertence ao seu estabelecimento."
            );
        }

        if (!"A_CAMINHO".equals(aviso.getStAviso())) {
            throw new IllegalStateException(
                    "Somente avisos A_CAMINHO podem ser confirmados."
            );
        }

        int linhasAfetadas = avisoAcolhimentoDAO.confirmar(
                cdAviso,
                cdFuncionario
        );

        if (linhasAfetadas == 0) {
            throw new IllegalStateException(
                    "Não foi possível confirmar o acolhimento."
            );
        }

        return converterParaResponse(
                buscarAvisoExistente(cdAviso)
        );
    }

    // =====================================================
    // EXPIRAÇÃO DE AVISOS
    // =====================================================

    // EXPIRAR AVISOS QUE ULTRAPASSARAM O DOBRO
    // DO TEMPO ESTIMADO.
    private void expirarAvisosVencidos() {

        LocalDateTime agora = LocalDateTime.now(FUSO_HORARIO);

        List<AvisoAcolhimento> avisos =
                avisoAcolhimentoDAO.buscarAvisosEmAndamento();

        for (AvisoAcolhimento aviso : avisos) {

            if (aviso.getDtAviso() == null) {
                continue;
            }

            // PADRÃO DE 60 MINUTOS PARA REGISTROS NULOS
            Integer tempoEstimado = aviso.getNrTempoEstimado() != null
                    ? aviso.getNrTempoEstimado()
                    : 60;

            // EXPIRAR APÓS O DOBRO DO TEMPO ESTIMADO
            LocalDateTime limite = aviso.getDtAviso()
                    .plusMinutes(tempoEstimado * 2L);

            if (!limite.isAfter(agora)) {

                avisoAcolhimentoDAO.expirar(
                        aviso.getCdAviso()
                );
            }
        }
    }

    // =====================================================
    // VALIDAÇÕES DE NEGÓCIO
    // =====================================================

    // OBTER SESSÃO VÁLIDA E VERIFICAR CONTA DA USUÁRIA
    private SessaoWeb obterSessaoValida(String uuid) {

        SessaoWeb sessao = sessaoWebService.buscarSessaoAtiva(uuid)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Sessão inválida ou expirada. "
                                        + "Faça login novamente."
                        )
                );

        // IMPEDIR QUE SESSÕES ANÔNIMAS ACESSEM OPERAÇÕES DE USUÁRIA
        if (sessao.getCdUsuario() == null) {
            throw new IllegalStateException(
                    "É necessário possuir uma conta para solicitar ou consultar avisos."
            );
        }

        // VERIFICAR SE A USUÁRIA AINDA EXISTE
        Usuario usuario = usuarioDAO.buscarPorId(
                sessao.getCdUsuario()
        ).orElseThrow(() ->
                new IllegalArgumentException(
                        "Usuária não encontrada."
                )
        );

        // BLOQUEAR CONTAS INATIVAS
        if (!"ATIVO".equals(usuario.getStUsuario())) {
            throw new IllegalStateException(
                    "Sua conta está bloqueada."
            );
        }

        return sessao;
    }

    // VALIDAR SE O ESTABELECIMENTO PODE RECEBER AVISOS
    private void validarEstabelecimentoDisponivel(
            Estabelecimento estabelecimento) {

        if (!"ATIVO".equals(
                estabelecimento.getStEstabelecimento())) {

            throw new IllegalStateException(
                    "Este estabelecimento não está ativo na plataforma."
            );
        }

        // VERIFICAR ATIVAÇÃO GERAL
        if (!"S".equals(
                estabelecimento.getFlAcolhimentoAtivo())) {

            throw new IllegalStateException(
                    "Este estabelecimento não está recebendo acolhimentos."
            );
        }

        LocalDateTime agora = LocalDateTime.now(FUSO_HORARIO);

        // VERIFICAR SUSPENSÃO TEMPORÁRIA
        if (estabelecimento.getDtSuspensaoAte() != null
                && estabelecimento.getDtSuspensaoAte().isAfter(agora)) {

            throw new IllegalStateException(
                    "Este estabelecimento está temporariamente suspenso."
            );
        }

        // VERIFICAR GRADE SEMANAL
        validarDisponibilidadeSemanal(estabelecimento, agora);
    }

    // VALIDAR DIA E HORÁRIO DE ACOLHIMENTO
    private void validarDisponibilidadeSemanal(
            Estabelecimento estabelecimento,
            LocalDateTime agora) {

        int diaSemana = agora.getDayOfWeek().getValue();

        LocalTime horarioAtual = agora.toLocalTime();

        List<Disponibilidade> periodos =
                disponibilidadeDAO.buscarPorDiaSemana(
                        estabelecimento.getCdEstabelecimento(),
                        diaSemana
                );

        boolean dentroDoHorario = periodos.stream()
                .anyMatch(periodo -> {

                    try {

                        LocalTime inicio = LocalTime.parse(
                                periodo.getHrInicio()
                        );

                        LocalTime fim = LocalTime.parse(
                                periodo.getHrFim()
                        );

                        // INÍCIO INCLUSIVO E FIM EXCLUSIVO
                        return !horarioAtual.isBefore(inicio)
                                && horarioAtual.isBefore(fim);

                    } catch (DateTimeParseException e) {

                        throw new IllegalStateException(
                                "Existe um horário inválido "
                                        + "na grade de disponibilidade."
                        );
                    }
                });

        if (!dentroDoHorario) {
            throw new IllegalStateException(
                    "Este estabelecimento não possui disponibilidade "
                            + "para acolhimento neste dia e horário."
            );
        }
    }

    // BUSCAR FUNCIONÁRIO E VALIDAR SITUAÇÃO
    private Funcionario buscarFuncionarioAtivo(Long id) {

        validarId(id);

        Funcionario funcionario = funcionarioDAO.buscarPorId(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Funcionário não encontrado."
                        )
                );

        if (!"ATIVO".equals(funcionario.getStFuncionario())) {
            throw new IllegalStateException(
                    "Funcionário inativo não pode realizar esta operação."
            );
        }

        return funcionario;
    }

    // BUSCAR AVISO EXISTENTE
    private AvisoAcolhimento buscarAvisoExistente(Long id) {

        return avisoAcolhimentoDAO.buscarPorId(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Aviso de acolhimento não encontrado."
                        )
                );
    }

    // CONVERTER MODEL PARA RESPONSE DTO
    private AvisoAcolhimentoResponseDTO converterParaResponse(
            AvisoAcolhimento aviso) {

        return new AvisoAcolhimentoResponseDTO(
                aviso.getCdAviso(),
                aviso.getCdUsuario(),
                aviso.getCdSessaoWeb(),
                aviso.getCdEstabelecimento(),
                aviso.getCdFuncionario(),
                aviso.getDtAviso(),
                aviso.getStAviso(),
                aviso.getNrTempoEstimado(),
                aviso.getDtAcolhimento()
        );
    }

    // VALIDAR IDENTIFICADOR
    private void validarId(Long id) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    "Identificador inválido."
            );
        }
    }
}
