package br.com.ari.service;

import br.com.ari.exception.RecursoNaoEncontradoException;
import br.com.ari.dao.DisponibilidadeDAO;
import br.com.ari.dao.FuncionarioDAO;
import br.com.ari.dto.DisponibilidadeRequestDTO;
import br.com.ari.dto.DisponibilidadeUpdateDTO;
import br.com.ari.dto.DisponibilidadeResponseDTO;
import br.com.ari.model.Disponibilidade;
import br.com.ari.model.Funcionario;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;

@Service
public class DisponibilidadeService {

    private final DisponibilidadeDAO disponibilidadeDAO;
    private final FuncionarioDAO funcionarioDAO;

    public DisponibilidadeService(
            DisponibilidadeDAO disponibilidadeDAO,
            FuncionarioDAO funcionarioDAO) {
        this.disponibilidadeDAO = disponibilidadeDAO;
        this.funcionarioDAO = funcionarioDAO;
    }

    // CADASTRAR UM NOVO PERÍODO NA GRADE SEMANAL
    @Transactional
    public DisponibilidadeResponseDTO criar(
            Long cdFuncionarioAdmin,
            DisponibilidadeRequestDTO dto) {

        Funcionario admin = buscarAdmin(cdFuncionarioAdmin);
        validarDadosCadastro(dto);

        Long cdEstabelecimento = admin.getCdEstabelecimento();

        // A chave única do banco considera também os períodos inativos.
        validarPeriodoDuplicado(
                cdEstabelecimento,
                dto.nrDiaSemana(),
                dto.nrPeriodo(),
                null
        );

        // Novos períodos começam ativos; portanto, não podem sobrepor
        // outro período ativo do mesmo dia.
        validarSobreposicao(
                cdEstabelecimento,
                dto.nrDiaSemana(),
                dto.hrInicio(),
                dto.hrFim(),
                null
        );

        Disponibilidade disponibilidade = Disponibilidade.builder()
                .cdEstabelecimento(cdEstabelecimento)
                .nrDiaSemana(dto.nrDiaSemana())
                .nrPeriodo(dto.nrPeriodo())
                .hrInicio(dto.hrInicio())
                .hrFim(dto.hrFim())
                .flAtivo("S")
                .build();

        try {
            int linhasAfetadas = disponibilidadeDAO.criar(disponibilidade);

            if (linhasAfetadas == 0 || disponibilidade.getCdDisponibilidade() == null) {
                throw new IllegalStateException(
                        "Não foi possível cadastrar a disponibilidade."
                );
            }
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException(
                    "Não foi possível cadastrar. Verifique se o dia e o número do período já existem."
            );
        }

        Disponibilidade cadastrada = disponibilidadeDAO
                .buscarPorId(disponibilidade.getCdDisponibilidade())
                .orElseThrow(() -> new IllegalStateException(
                        "Erro ao recuperar a disponibilidade cadastrada."
                ));

        return converterParaResponse(cadastrada);
    }

    // BUSCAR UM PERÍODO, GARANTINDO QUE PERTENCE AO ESTABELECIMENTO DO ADMIN
    public DisponibilidadeResponseDTO buscarPorId(
            Long cdFuncionarioAdmin,
            Long cdDisponibilidade) {

        Funcionario admin = buscarAdmin(cdFuncionarioAdmin);
        Disponibilidade disponibilidade = buscarDisponibilidadeDaEmpresa(
                cdDisponibilidade,
                admin.getCdEstabelecimento()
        );

        return converterParaResponse(disponibilidade);
    }

    // LISTAR TODOS OS PERÍODOS, INCLUINDO OS INATIVOS
    public List<DisponibilidadeResponseDTO> buscarPorEstabelecimento(
            Long cdFuncionarioAdmin) {

        Funcionario admin = buscarAdmin(cdFuncionarioAdmin);

        return disponibilidadeDAO.buscarPorEstabelecimento(
                        admin.getCdEstabelecimento()
                ).stream()
                .map(this::converterParaResponse)
                .toList();
    }

    // LISTAR SOMENTE OS PERÍODOS ATIVOS
    public List<DisponibilidadeResponseDTO> buscarAtivas(
            Long cdFuncionarioAdmin) {

        Funcionario admin = buscarAdmin(cdFuncionarioAdmin);

        return disponibilidadeDAO.buscarAtivasPorEstabelecimento(
                        admin.getCdEstabelecimento()
                ).stream()
                .map(this::converterParaResponse)
                .toList();
    }

    // LISTAR OS PERÍODOS ATIVOS DE UM DIA ESPECÍFICO
    public List<DisponibilidadeResponseDTO> buscarPorDiaSemana(
            Long cdFuncionarioAdmin,
            Integer nrDiaSemana) {

        Funcionario admin = buscarAdmin(cdFuncionarioAdmin);
        validarDiaSemana(nrDiaSemana);

        return disponibilidadeDAO.buscarPorDiaSemana(
                        admin.getCdEstabelecimento(),
                        nrDiaSemana
                ).stream()
                .map(this::converterParaResponse)
                .toList();
    }

    // ATUALIZAR DIA, NÚMERO DO PERÍODO OU HORÁRIOS
    @Transactional
    public DisponibilidadeResponseDTO atualizar(
            Long cdFuncionarioAdmin,
            Long cdDisponibilidade,
            DisponibilidadeUpdateDTO dto) {

        Funcionario admin = buscarAdmin(cdFuncionarioAdmin);
        validarDadosAtualizacao(dto);

        Long cdEstabelecimento = admin.getCdEstabelecimento();
        Disponibilidade existente = buscarDisponibilidadeDaEmpresa(
                cdDisponibilidade,
                cdEstabelecimento
        );

        validarPeriodoDuplicado(
                cdEstabelecimento,
                dto.nrDiaSemana(),
                dto.nrPeriodo(),
                cdDisponibilidade
        );

        // Só precisa validar sobreposição se o registro continuar ativo.
        if ("S".equals(existente.getFlAtivo())) {
            validarSobreposicao(
                    cdEstabelecimento,
                    dto.nrDiaSemana(),
                    dto.hrInicio(),
                    dto.hrFim(),
                    cdDisponibilidade
            );
        }

        Disponibilidade disponibilidade = Disponibilidade.builder()
                .cdDisponibilidade(cdDisponibilidade)
                .cdEstabelecimento(cdEstabelecimento)
                .nrDiaSemana(dto.nrDiaSemana())
                .nrPeriodo(dto.nrPeriodo())
                .hrInicio(dto.hrInicio())
                .hrFim(dto.hrFim())
                .flAtivo(existente.getFlAtivo())
                .dtCadastro(existente.getDtCadastro())
                .build();

        try {
            int linhasAfetadas = disponibilidadeDAO.atualizar(disponibilidade);

            if (linhasAfetadas == 0) {
                throw new IllegalStateException(
                        "Não foi possível atualizar a disponibilidade."
                );
            }
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException(
                    "Não foi possível atualizar. Verifique se o dia e o número do período já existem."
            );
        }

        return converterParaResponse(disponibilidade);
    }

    // DESATIVAR UM PERÍODO SEM EXCLUÍ-LO DO BANCO
    @Transactional
    public int desativar(
            Long cdFuncionarioAdmin,
            Long cdDisponibilidade) {

        Funcionario admin = buscarAdmin(cdFuncionarioAdmin);
        Disponibilidade disponibilidade = buscarDisponibilidadeDaEmpresa(
                cdDisponibilidade,
                admin.getCdEstabelecimento()
        );

        if ("N".equals(disponibilidade.getFlAtivo())) {
            throw new IllegalStateException(
                    "Esta disponibilidade já está desativada."
            );
        }

        disponibilidade.setFlAtivo("N");
        int linhasAfetadas = disponibilidadeDAO.atualizar(disponibilidade);

        if (linhasAfetadas == 0) {
            throw new IllegalStateException(
                    "Não foi possível desativar a disponibilidade."
            );
        }

        return linhasAfetadas;
    }

    // REATIVAR UM PERÍODO JÁ CADASTRADO
    @Transactional
    public int ativar(
            Long cdFuncionarioAdmin,
            Long cdDisponibilidade) {

        Funcionario admin = buscarAdmin(cdFuncionarioAdmin);
        Disponibilidade existente = buscarDisponibilidadeDaEmpresa(
                cdDisponibilidade,
                admin.getCdEstabelecimento()
        );

        if ("S".equals(existente.getFlAtivo())) {
            throw new IllegalStateException(
                    "Esta disponibilidade já está ativa."
            );
        }

        // Ao reativar, validar para não criar conflito com outro horário ativo.
        validarSobreposicao(
                admin.getCdEstabelecimento(),
                existente.getNrDiaSemana(),
                existente.getHrInicio(),
                existente.getHrFim(),
                cdDisponibilidade
        );

        existente.setFlAtivo("S");

        try {
            int linhasAfetadas = disponibilidadeDAO.atualizar(existente);

            if (linhasAfetadas == 0) {
                throw new IllegalStateException(
                        "Não foi possível reativar a disponibilidade."
                );
            }

            return linhasAfetadas;
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException(
                    "Não foi possível reativar. Verifique se existe conflito na grade semanal."
            );
        }
    }

    // LOCALIZAR E VALIDAR O ADMINISTRADOR
    private Funcionario buscarAdmin(Long id) {
        validarId(id);

        Funcionario admin = funcionarioDAO.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Funcionário administrador não encontrado."
                ));

        if (!"ATIVO".equals(admin.getStFuncionario())) {
            throw new IllegalStateException(
                    "Funcionário inativo não pode realizar esta operação."
            );
        }

        if (!"ADMIN".equals(admin.getTpPerfil())) {
            throw new IllegalArgumentException(
                    "Acesso permitido somente para ADMIN."
            );
        }

        if (admin.getCdEstabelecimento() == null) {
            throw new IllegalStateException(
                    "O administrador não está vinculado a um estabelecimento."
            );
        }

        return admin;
    }

    // BUSCAR UM PERÍODO E GARANTIR QUE PERTENCE À EMPRESA DO ADMIN
    private Disponibilidade buscarDisponibilidadeDaEmpresa(
            Long cdDisponibilidade,
            Long cdEstabelecimento) {

        validarId(cdDisponibilidade);

        Disponibilidade disponibilidade = disponibilidadeDAO
                .buscarPorId(cdDisponibilidade)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Disponibilidade não encontrada."
                ));

        if (!Objects.equals(
                disponibilidade.getCdEstabelecimento(),
                cdEstabelecimento)) {
            throw new IllegalArgumentException(
                    "Esta disponibilidade não pertence ao seu estabelecimento."
            );
        }

        return disponibilidade;
    }

    // A CHAVE ÚNICA DO SQL CONSIDERA ESTABELECIMENTO + DIA + PERÍODO,
    // INCLUSIVE QUANDO O REGISTRO ESTÁ INATIVO.
    private void validarPeriodoDuplicado(
            Long cdEstabelecimento,
            Integer nrDiaSemana,
            Integer nrPeriodo,
            Long cdDisponibilidadeAtual) {

        boolean duplicado = disponibilidadeDAO
                .buscarPorEstabelecimento(cdEstabelecimento)
                .stream()
                .anyMatch(disponibilidade ->
                        Objects.equals(disponibilidade.getNrDiaSemana(), nrDiaSemana)
                                && Objects.equals(disponibilidade.getNrPeriodo(), nrPeriodo)
                                && !Objects.equals(
                                disponibilidade.getCdDisponibilidade(),
                                cdDisponibilidadeAtual
                        )
                );

        if (duplicado) {
            throw new IllegalArgumentException(
                    "Já existe um registro para esse número de período nesse dia. "
                            + "Edite ou reative o registro existente."
            );
        }
    }

    // IMPEDIR QUE DOIS PERÍODOS ATIVOS DO MESMO DIA SE SOBREPONHAM.
    // Horários encostados (ex.: 08:00–12:00 e 12:00–18:00) são permitidos.
    private void validarSobreposicao(
            Long cdEstabelecimento,
            Integer nrDiaSemana,
            String hrInicio,
            String hrFim,
            Long cdDisponibilidadeAtual) {

        LocalTime novoInicio = LocalTime.parse(hrInicio);
        LocalTime novoFim = LocalTime.parse(hrFim);

        boolean sobreposto = disponibilidadeDAO
                .buscarPorEstabelecimento(cdEstabelecimento)
                .stream()
                .filter(disponibilidade -> "S".equals(disponibilidade.getFlAtivo()))
                .filter(disponibilidade -> Objects.equals(
                        disponibilidade.getNrDiaSemana(), nrDiaSemana))
                .filter(disponibilidade -> !Objects.equals(
                        disponibilidade.getCdDisponibilidade(), cdDisponibilidadeAtual))
                .anyMatch(disponibilidade -> {
                    LocalTime inicioExistente = LocalTime.parse(disponibilidade.getHrInicio());
                    LocalTime fimExistente = LocalTime.parse(disponibilidade.getHrFim());

                    return novoInicio.isBefore(fimExistente)
                            && novoFim.isAfter(inicioExistente);
                });

        if (sobreposto) {
            throw new IllegalArgumentException(
                    "Este horário se sobrepõe a outro período ativo do mesmo dia."
            );
        }
    }

    private void validarDadosCadastro(DisponibilidadeRequestDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException(
                    "Dados da disponibilidade não informados."
            );
        }

        validarDiaSemana(dto.nrDiaSemana());
        validarNumeroPeriodo(dto.nrPeriodo());
        validarHorarios(dto.hrInicio(), dto.hrFim());
    }

    private void validarDadosAtualizacao(DisponibilidadeUpdateDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException(
                    "Dados da disponibilidade não informados."
            );
        }

        validarDiaSemana(dto.nrDiaSemana());
        validarNumeroPeriodo(dto.nrPeriodo());
        validarHorarios(dto.hrInicio(), dto.hrFim());
    }

    private void validarDiaSemana(Integer dia) {
        if (dia == null || dia < 1 || dia > 7) {
            throw new IllegalArgumentException(
                    "Dia da semana deve estar entre 1 e 7."
            );
        }
    }

    private void validarNumeroPeriodo(Integer periodo) {
        if (periodo == null || periodo < 1 || periodo > 99) {
            throw new IllegalArgumentException(
                    "Número do período deve estar entre 1 e 99."
            );
        }
    }

    private void validarHorarios(String hrInicio, String hrFim) {
        if (hrInicio == null || hrFim == null
                || hrInicio.isBlank() || hrFim.isBlank()) {
            throw new IllegalArgumentException(
                    "Horários de início e término são obrigatórios."
            );
        }

        try {
            if (!hrInicio.matches("^([01][0-9]|2[0-3]):[0-5][0-9]$")
                    || !hrFim.matches("^([01][0-9]|2[0-3]):[0-5][0-9]$")) {
                throw new IllegalArgumentException(
                        "Horários devem seguir o formato HH:mm."
                );
            }

            LocalTime inicio = LocalTime.parse(hrInicio);
            LocalTime fim = LocalTime.parse(hrFim);

            if (!fim.isAfter(inicio)) {
                throw new IllegalArgumentException(
                        "O horário de término deve ser posterior ao início."
                );
            }
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Horários devem seguir o formato HH:mm."
            );
        }
    }

    private DisponibilidadeResponseDTO converterParaResponse(
            Disponibilidade disponibilidade) {
        return new DisponibilidadeResponseDTO(
                disponibilidade.getCdDisponibilidade(),
                disponibilidade.getCdEstabelecimento(),
                disponibilidade.getNrDiaSemana(),
                disponibilidade.getNrPeriodo(),
                disponibilidade.getHrInicio(),
                disponibilidade.getHrFim(),
                disponibilidade.getFlAtivo(),
                disponibilidade.getDtCadastro()
        );
    }

    private void validarId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    "Identificador inválido."
            );
        }
    }
}
