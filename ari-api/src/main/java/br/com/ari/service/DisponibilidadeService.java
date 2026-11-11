
package br.com.ari.service;

import br.com.ari.dao.DisponibilidadeDAO;
import br.com.ari.dao.FuncionarioDAO;

import br.com.ari.dto.DisponibilidadeRequestDTO;
import br.com.ari.dto.DisponibilidadeUpdateDTO;
import br.com.ari.dto.DisponibilidadeResponseDTO;

import br.com.ari.model.Disponibilidade;
import br.com.ari.model.Funcionario;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
public class DisponibilidadeService {

    private final DisponibilidadeDAO disponibilidadeDAO;
    private final FuncionarioDAO funcionarioDAO;

    // CONSTRUTOR
    public DisponibilidadeService(
            DisponibilidadeDAO disponibilidadeDAO,
            FuncionarioDAO funcionarioDAO) {

        this.disponibilidadeDAO = disponibilidadeDAO;
        this.funcionarioDAO = funcionarioDAO;
    }

    // CADASTRAR DISPONIBILIDADE
    // Somente ADMIN pode cadastrar períodos da própria empresa.
    public DisponibilidadeResponseDTO criar(
            Long cdFuncionarioAdmin,
            DisponibilidadeRequestDTO dto) {

        Funcionario admin = buscarAdmin(cdFuncionarioAdmin);

        validarDadosCadastro(dto);

        // VERIFICAR SE O PERÍODO JÁ EXISTE
        validarPeriodoDuplicado(
                admin.getCdEstabelecimento(),
                dto.nrDiaSemana(),
                dto.nrPeriodo(),
                null
        );

        // CONVERTER DTO PARA MODEL
        Disponibilidade disponibilidade = Disponibilidade.builder()
                .cdEstabelecimento(admin.getCdEstabelecimento())
                .nrDiaSemana(dto.nrDiaSemana())
                .nrPeriodo(dto.nrPeriodo())
                .hrInicio(dto.hrInicio())
                .hrFim(dto.hrFim())
                .flAtivo("S")
                .build();

        try {

            int linhasAfetadas = disponibilidadeDAO.criar(
                    disponibilidade
            );

            if (linhasAfetadas == 0
                    || disponibilidade.getCdDisponibilidade() == null) {

                throw new IllegalStateException(
                        "Não foi possível cadastrar a disponibilidade."
                );
            }

        } catch (DataIntegrityViolationException e) {

            throw new IllegalArgumentException(
                    "Não foi possível cadastrar. "
                            + "Verifique se o período já existe nesse dia."
            );
        }

        // RECUPERAR DISPONIBILIDADE CADASTRADA
        Disponibilidade cadastrada = disponibilidadeDAO.buscarPorId(
                disponibilidade.getCdDisponibilidade()
        ).orElseThrow(() ->
                new IllegalStateException(
                        "Erro ao recuperar disponibilidade cadastrada."
                )
        );

        return converterParaResponse(cadastrada);
    }

    // BUSCAR DISPONIBILIDADE POR ID
    // ADMIN só consulta períodos da própria empresa.
    public DisponibilidadeResponseDTO buscarPorId(
            Long cdFuncionarioAdmin,
            Long cdDisponibilidade) {

        Funcionario admin = buscarAdmin(cdFuncionarioAdmin);

        Disponibilidade disponibilidade =
                buscarDisponibilidadeDaEmpresa(
                        cdDisponibilidade,
                        admin.getCdEstabelecimento()
                );

        return converterParaResponse(disponibilidade);
    }

    // LISTAR TODAS AS DISPONIBILIDADES DO ESTABELECIMENTO
    // Inclui períodos ativos e inativos.
    public List<DisponibilidadeResponseDTO> buscarPorEstabelecimento(
            Long cdFuncionarioAdmin) {

        Funcionario admin = buscarAdmin(cdFuncionarioAdmin);

        return disponibilidadeDAO.buscarPorEstabelecimento(
                        admin.getCdEstabelecimento()
                ).stream()
                .map(this::converterParaResponse)
                .toList();
    }

    // LISTAR SOMENTE DISPONIBILIDADES ATIVAS
    public List<DisponibilidadeResponseDTO> buscarAtivas(
            Long cdFuncionarioAdmin) {

        Funcionario admin = buscarAdmin(cdFuncionarioAdmin);

        return disponibilidadeDAO.buscarAtivasPorEstabelecimento(
                        admin.getCdEstabelecimento()
                ).stream()
                .map(this::converterParaResponse)
                .toList();
    }

    // BUSCAR DISPONIBILIDADES POR DIA DA SEMANA
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

    // ATUALIZAR DISPONIBILIDADE
    public DisponibilidadeResponseDTO atualizar(
            Long cdFuncionarioAdmin,
            Long cdDisponibilidade,
            DisponibilidadeUpdateDTO dto) {

        Funcionario admin = buscarAdmin(cdFuncionarioAdmin);

        validarDadosAtualizacao(dto);

        // VERIFICAR SE A DISPONIBILIDADE PERTENCE À EMPRESA
        Disponibilidade existente = buscarDisponibilidadeDaEmpresa(
                cdDisponibilidade,
                admin.getCdEstabelecimento()
        );

        // VERIFICAR SE O NOVO PERÍODO JÁ ESTÁ CADASTRADO
        validarPeriodoDuplicado(
                admin.getCdEstabelecimento(),
                dto.nrDiaSemana(),
                dto.nrPeriodo(),
                cdDisponibilidade
        );

        // PRESERVAR IDENTIFICADOR, EMPRESA E STATUS
        Disponibilidade disponibilidade = Disponibilidade.builder()
                .cdDisponibilidade(cdDisponibilidade)
                .cdEstabelecimento(admin.getCdEstabelecimento())
                .nrDiaSemana(dto.nrDiaSemana())
                .nrPeriodo(dto.nrPeriodo())
                .hrInicio(dto.hrInicio())
                .hrFim(dto.hrFim())
                .flAtivo(existente.getFlAtivo())
                .dtCadastro(existente.getDtCadastro())
                .build();

        try {

            int linhasAfetadas = disponibilidadeDAO.atualizar(
                    disponibilidade
            );

            if (linhasAfetadas == 0) {
                throw new IllegalStateException(
                        "Não foi possível atualizar a disponibilidade."
                );
            }

        } catch (DataIntegrityViolationException e) {

            throw new IllegalArgumentException(
                    "Não foi possível atualizar. "
                            + "Verifique se o período já existe nesse dia."
            );
        }

        return converterParaResponse(disponibilidade);
    }


    // DESATIVAR DISPONIBILIDADE
    // Preserva o registro no banco, alterando FL_ATIVO para N.
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

        return disponibilidadeDAO.atualizar(disponibilidade);
    }


    // REATIVAR DISPONIBILIDADE
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

        existente.setFlAtivo("S");

        try {

            return disponibilidadeDAO.atualizar(existente);

        } catch (DataIntegrityViolationException e) {

            throw new IllegalArgumentException(
                    "Não foi possível reativar a disponibilidade."
            );
        }
    }

    // BUSCAR ADMIN AUTENTICADO
    private Funcionario buscarAdmin(Long id) {

        validarId(id);

        Funcionario admin = funcionarioDAO.buscarPorId(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Funcionário administrador não encontrado."
                        )
                );

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

        return admin;
    }

    // BUSCAR DISPONIBILIDADE DA EMPRESA DO ADMIN
    private Disponibilidade buscarDisponibilidadeDaEmpresa(
            Long cdDisponibilidade,
            Long cdEstabelecimento) {

        validarId(cdDisponibilidade);

        Disponibilidade disponibilidade =
                disponibilidadeDAO.buscarPorId(cdDisponibilidade)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Disponibilidade não encontrada."
                                )
                        );

        if (!disponibilidade.getCdEstabelecimento()
                .equals(cdEstabelecimento)) {

            throw new IllegalArgumentException(
                    "Esta disponibilidade não pertence ao seu estabelecimento."
            );
        }

        return disponibilidade;
    }

    // VERIFICAR SE JÁ EXISTE O NÚMERO DO PERÍODO NO DIA
    // A unicidade considera também períodos inativos.
    private void validarPeriodoDuplicado(
            Long cdEstabelecimento,
            Integer nrDiaSemana,
            Integer nrPeriodo,
            Long cdDisponibilidadeAtual) {

        boolean duplicado = disponibilidadeDAO
                .buscarPorEstabelecimento(cdEstabelecimento)
                .stream()
                .anyMatch(disponibilidade ->

                        disponibilidade.getNrDiaSemana()
                                .equals(nrDiaSemana)

                                && disponibilidade.getNrPeriodo()
                                .equals(nrPeriodo)

                                && (cdDisponibilidadeAtual == null
                                || !disponibilidade.getCdDisponibilidade()
                                .equals(cdDisponibilidadeAtual))
                );

        if (duplicado) {
            throw new IllegalArgumentException(
                    "Já existe um período com esse número "
                            + "cadastrado nesse dia."
            );
        }
    }

    // VALIDAR DADOS DO CADASTRO
    private void validarDadosCadastro(
            DisponibilidadeRequestDTO dto) {

        if (dto == null) {
            throw new IllegalArgumentException(
                    "Dados da disponibilidade não informados."
            );
        }

        validarDiaSemana(dto.nrDiaSemana());
        validarNumeroPeriodo(dto.nrPeriodo());

        validarHorarios(
                dto.hrInicio(),
                dto.hrFim()
        );
    }

    // VALIDAR DADOS DA ATUALIZAÇÃO
    private void validarDadosAtualizacao(
            DisponibilidadeUpdateDTO dto) {

        if (dto == null) {
            throw new IllegalArgumentException(
                    "Dados da disponibilidade não informados."
            );
        }

        validarDiaSemana(dto.nrDiaSemana());
        validarNumeroPeriodo(dto.nrPeriodo());

        validarHorarios(
                dto.hrInicio(),
                dto.hrFim()
        );
    }

    // VALIDAR DIA DA SEMANA
    private void validarDiaSemana(Integer dia) {

        if (dia == null || dia < 1 || dia > 7) {
            throw new IllegalArgumentException(
                    "Dia da semana deve estar entre 1 e 7."
            );
        }
    }

    // VALIDAR NÚMERO DO PERÍODO
    private void validarNumeroPeriodo(Integer periodo) {

        if (periodo == null || periodo < 1 || periodo > 99) {
            throw new IllegalArgumentException(
                    "Número do período deve estar entre 1 e 99."
            );
        }
    }


    // VALIDAR HORÁRIOS
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

    // CONVERTER MODEL PARA RESPONSE DTO
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

    // VALIDAR ID
    private void validarId(Long id) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    "Identificador inválido."
            );
        }
    }

}
