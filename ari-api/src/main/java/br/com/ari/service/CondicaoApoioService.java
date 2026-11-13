
package br.com.ari.service;

import br.com.ari.dao.CondicaoApoioDAO;
import br.com.ari.dao.FuncionarioDAO;

import br.com.ari.dto.CondicaoApoioRequestDTO;
import br.com.ari.dto.CondicaoApoioUpdateDTO;
import br.com.ari.dto.CondicaoApoioResponseDTO;

import br.com.ari.model.CondicaoApoio;
import br.com.ari.model.Funcionario;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class CondicaoApoioService {

    private final CondicaoApoioDAO condicaoApoioDAO;
    private final FuncionarioDAO funcionarioDAO;

    // CONSTRUTOR
    public CondicaoApoioService(
            CondicaoApoioDAO condicaoApoioDAO,
            FuncionarioDAO funcionarioDAO) {

        this.condicaoApoioDAO = condicaoApoioDAO;
        this.funcionarioDAO = funcionarioDAO;
    }

    // CADASTRAR CONDIÇÃO DE APOIO
    // Somente ADMIN pode cadastrar condições da própria empresa.
    @Transactional
    public CondicaoApoioResponseDTO criar(
            Long cdFuncionarioAdmin,
            CondicaoApoioRequestDTO dto) {

        Funcionario admin = buscarAdmin(cdFuncionarioAdmin);

        validarDadosCadastro(dto);

        String nome = dto.nmCondicao().trim();

        // VERIFICAR SE A CONDIÇÃO JÁ EXISTE NA EMPRESA
        validarNomeDuplicado(
                admin.getCdEstabelecimento(),
                nome,
                null
        );

        // CONVERTER DTO PARA MODEL
        CondicaoApoio condicao = CondicaoApoio.builder()
                .cdEstabelecimento(admin.getCdEstabelecimento())
                .tpCategoria(dto.tpCategoria())
                .nmCondicao(nome)
                .dsCondicao(normalizarDescricao(dto.dsCondicao()))
                .flAtivo("S")
                .build();

        try {

            int linhasAfetadas = condicaoApoioDAO.criar(condicao);

            if (linhasAfetadas == 0
                    || condicao.getCdCondicaoApoio() == null) {

                throw new IllegalStateException(
                        "Não foi possível cadastrar a condição de apoio."
                );
            }

        } catch (DuplicateKeyException e) {

            throw new IllegalArgumentException(
                    "Esta condição já está cadastrada na empresa."
            );
        }

        // RECUPERAR CONDIÇÃO CADASTRADA
        CondicaoApoio cadastrada = condicaoApoioDAO.buscarPorId(
                condicao.getCdCondicaoApoio()
        ).orElseThrow(() ->
                new IllegalStateException(
                        "Erro ao recuperar condição cadastrada."
                )
        );

        return converterParaResponse(cadastrada);
    }

    // BUSCAR CONDIÇÃO POR ID
    // ADMIN só pode consultar condições da própria empresa.
    public CondicaoApoioResponseDTO buscarPorId(
            Long cdFuncionarioAdmin,
            Long cdCondicaoApoio) {

        Funcionario admin = buscarAdmin(cdFuncionarioAdmin);

        CondicaoApoio condicao = buscarCondicaoDaEmpresa(
                cdCondicaoApoio,
                admin.getCdEstabelecimento()
        );

        return converterParaResponse(condicao);
    }

    // LISTAR TODAS AS CONDIÇÕES DA EMPRESA
    // Inclui condições ativas e inativas.
    public List<CondicaoApoioResponseDTO> buscarPorEstabelecimento(
            Long cdFuncionarioAdmin) {

        Funcionario admin = buscarAdmin(cdFuncionarioAdmin);

        return condicaoApoioDAO.buscarPorEstabelecimento(
                        admin.getCdEstabelecimento()
                ).stream()
                .map(this::converterParaResponse)
                .toList();
    }

    // LISTAR CONDIÇÕES ATIVAS DA EMPRESA
    public List<CondicaoApoioResponseDTO> buscarAtivas(
            Long cdFuncionarioAdmin) {

        Funcionario admin = buscarAdmin(cdFuncionarioAdmin);

        return condicaoApoioDAO.buscarAtivasPorEstabelecimento(
                        admin.getCdEstabelecimento()
                ).stream()
                .map(this::converterParaResponse)
                .toList();
    }

    // CONSULTA PÚBLICA DAS CONDIÇÕES DE APOIO
    // Utilizada pelo Front-end para apresentar os apoios oferecidos.
    public List<CondicaoApoioResponseDTO> buscarAtivasPublicas(
            Long cdEstabelecimento) {

        validarId(cdEstabelecimento);

        return condicaoApoioDAO.buscarAtivasPorEstabelecimento(
                        cdEstabelecimento
                ).stream()
                .map(this::converterParaResponse)
                .toList();
    }

    // ATUALIZAR CONDIÇÃO DE APOIO
    @Transactional
    public CondicaoApoioResponseDTO atualizar(
            Long cdFuncionarioAdmin,
            Long cdCondicaoApoio,
            CondicaoApoioUpdateDTO dto) {

        Funcionario admin = buscarAdmin(cdFuncionarioAdmin);

        validarDadosAtualizacao(dto);

        // VERIFICAR SE A CONDIÇÃO PERTENCE À EMPRESA
        CondicaoApoio existente = buscarCondicaoDaEmpresa(
                cdCondicaoApoio,
                admin.getCdEstabelecimento()
        );

        validarCondicaoAtiva(existente);

        String nome = dto.nmCondicao().trim();

        // VERIFICAR DUPLICIDADE DO NOME
        validarNomeDuplicado(
                admin.getCdEstabelecimento(),
                nome,
                cdCondicaoApoio
        );

        // CONVERTER DTO PARA MODEL
        CondicaoApoio condicao = CondicaoApoio.builder()
                .cdCondicaoApoio(cdCondicaoApoio)
                .cdEstabelecimento(admin.getCdEstabelecimento())
                .tpCategoria(dto.tpCategoria())
                .nmCondicao(nome)
                .dsCondicao(normalizarDescricao(dto.dsCondicao()))
                .flAtivo(existente.getFlAtivo())
                .dtCadastro(existente.getDtCadastro())
                .build();

        try {

            int linhasAfetadas = condicaoApoioDAO.atualizar(condicao);

            if (linhasAfetadas == 0) {
                throw new IllegalStateException(
                        "Não foi possível atualizar a condição de apoio."
                );
            }

        } catch (DuplicateKeyException e) {

            throw new IllegalArgumentException(
                    "Já existe uma condição com esse nome na empresa."
            );
        }

        // RECUPERAR DADOS ATUALIZADOS
        CondicaoApoio atualizada = buscarCondicaoDaEmpresa(
                cdCondicaoApoio,
                admin.getCdEstabelecimento()
        );

        return converterParaResponse(atualizada);
    }

    // DESATIVAR CONDIÇÃO DE APOIO
    // Preserva o registro e altera FL_ATIVO para N.
    @Transactional
    public int desativar(
            Long cdFuncionarioAdmin,
            Long cdCondicaoApoio) {

        Funcionario admin = buscarAdmin(cdFuncionarioAdmin);

        CondicaoApoio condicao = buscarCondicaoDaEmpresa(
                cdCondicaoApoio,
                admin.getCdEstabelecimento()
        );

        if ("N".equals(condicao.getFlAtivo())) {
            throw new IllegalStateException(
                    "Esta condição de apoio já está desativada."
            );
        }

        int linhasAfetadas = condicaoApoioDAO.atualizarStatus(
                cdCondicaoApoio,
                admin.getCdEstabelecimento(),
                "N"
        );

        if (linhasAfetadas == 0) {
            throw new IllegalStateException(
                    "Não foi possível desativar a condição de apoio."
            );
        }

        return linhasAfetadas;
    }

    // REATIVAR CONDIÇÃO DE APOIO
    @Transactional
    public int ativar(
            Long cdFuncionarioAdmin,
            Long cdCondicaoApoio) {

        Funcionario admin = buscarAdmin(cdFuncionarioAdmin);

        CondicaoApoio condicao = buscarCondicaoDaEmpresa(
                cdCondicaoApoio,
                admin.getCdEstabelecimento()
        );

        if ("S".equals(condicao.getFlAtivo())) {
            throw new IllegalStateException(
                    "Esta condição de apoio já está ativa."
            );
        }

        // VERIFICAR SE OUTRA CONDIÇÃO POSSUI O MESMO NOME
        validarNomeDuplicado(
                admin.getCdEstabelecimento(),
                condicao.getNmCondicao(),
                cdCondicaoApoio
        );

        int linhasAfetadas = condicaoApoioDAO.atualizarStatus(
                cdCondicaoApoio,
                admin.getCdEstabelecimento(),
                "S"
        );

        if (linhasAfetadas == 0) {
            throw new IllegalStateException(
                    "Não foi possível reativar a condição de apoio."
            );
        }

        return linhasAfetadas;
    }

    // ATUALIZAR SELEÇÕES DOS CHECKBOXES
    // IDs selecionados recebem S; os demais recebem N.
    @Transactional
    public List<CondicaoApoioResponseDTO> atualizarSelecoes(
            Long cdFuncionarioAdmin,
            List<Long> idsSelecionados) {

        Funcionario admin = buscarAdmin(cdFuncionarioAdmin);

        if (idsSelecionados == null) {
            throw new IllegalArgumentException(
                    "A lista de condições selecionadas não foi informada."
            );
        }

        // VALIDAR IDs E IMPEDIR REPETIÇÕES
        Set<Long> idsUnicos = new HashSet<>();

        for (Long id : idsSelecionados) {

            validarId(id);

            if (!idsUnicos.add(id)) {
                throw new IllegalArgumentException(
                        "A lista contém condições repetidas."
                );
            }
        }

        // BUSCAR TODAS AS CONDIÇÕES DA EMPRESA
        List<CondicaoApoio> condicoes =
                condicaoApoioDAO.buscarPorEstabelecimento(
                        admin.getCdEstabelecimento()
                );

        // IDENTIFICAR OS IDs PERTENCENTES AO ESTABELECIMENTO
        Set<Long> idsExistentes = new HashSet<>();

        for (CondicaoApoio condicao : condicoes) {
            idsExistentes.add(condicao.getCdCondicaoApoio());
        }

        // IMPEDIR SELEÇÃO DE CONDIÇÕES DE OUTRA EMPRESA
        if (!idsExistentes.containsAll(idsUnicos)) {
            throw new IllegalArgumentException(
                    "A seleção contém condições inexistentes "
                            + "ou pertencentes a outro estabelecimento."
            );
        }

        // ATUALIZAR TODAS AS SELEÇÕES NO BANCO
        int linhasAfetadas = condicaoApoioDAO.atualizarSelecoes(
                admin.getCdEstabelecimento(),
                idsSelecionados
        );

        // VERIFICAR SE A ATUALIZAÇÃO FOI EXECUTADA
        if (!idsSelecionados.isEmpty() && linhasAfetadas == 0) {
            throw new IllegalStateException(
                    "Não foi possível atualizar as condições de apoio."
            );
        }

        // RETORNAR TODAS AS CONDIÇÕES COM OS STATUS ATUALIZADOS
        return condicaoApoioDAO.buscarPorEstabelecimento(
                        admin.getCdEstabelecimento()
                ).stream()
                .map(this::converterParaResponse)
                .toList();
    }

    // BUSCAR ADMIN AUTORIZADO
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

        // VALIDAR ESTABELECIMENTO VINCULADO AO ADMIN
        validarId(admin.getCdEstabelecimento());

        return admin;
    }

    // BUSCAR CONDIÇÃO PERTENCENTE AO ESTABELECIMENTO
    private CondicaoApoio buscarCondicaoDaEmpresa(
            Long cdCondicaoApoio,
            Long cdEstabelecimento) {

        validarId(cdCondicaoApoio);

        CondicaoApoio condicao =
                condicaoApoioDAO.buscarPorId(cdCondicaoApoio)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Condição de apoio não encontrada."
                                )
                        );

        if (!cdEstabelecimento.equals(
                condicao.getCdEstabelecimento())) {

            throw new IllegalArgumentException(
                    "Esta condição não pertence ao seu estabelecimento."
            );
        }

        return condicao;
    }

    // VALIDAR SE A CONDIÇÃO ESTÁ ATIVA
    private void validarCondicaoAtiva(CondicaoApoio condicao) {

        if (!"S".equals(condicao.getFlAtivo())) {
            throw new IllegalStateException(
                    "Não é possível atualizar uma condição desativada."
            );
        }
    }

    // VERIFICAR SE O NOME JÁ EXISTE NA EMPRESA
    // A comparação ignora diferenças entre letras maiúsculas e minúsculas.
    // Considera também registros inativos.
    private void validarNomeDuplicado(
            Long cdEstabelecimento,
            String nome,
            Long cdCondicaoAtual) {

        boolean duplicado = condicaoApoioDAO
                .buscarPorEstabelecimento(cdEstabelecimento)
                .stream()
                .anyMatch(condicao ->

                        condicao.getNmCondicao()
                                .equalsIgnoreCase(nome)

                                && (cdCondicaoAtual == null
                                || !condicao.getCdCondicaoApoio()
                                .equals(cdCondicaoAtual))
                );

        if (duplicado) {
            throw new IllegalArgumentException(
                    "Esta condição já está cadastrada na empresa."
            );
        }
    }

    // VALIDAR DADOS DO CADASTRO
    private void validarDadosCadastro(
            CondicaoApoioRequestDTO dto) {

        if (dto == null) {
            throw new IllegalArgumentException(
                    "Dados da condição de apoio não informados."
            );
        }

        if (dto.nmCondicao() == null
                || dto.nmCondicao().isBlank()) {

            throw new IllegalArgumentException(
                    "Nome da condição é obrigatório."
            );
        }

        validarCategoria(dto.tpCategoria());

        if (dto.nmCondicao().trim().length() > 100) {
            throw new IllegalArgumentException(
                    "Nome da condição deve ter no máximo 100 caracteres."
            );
        }

        validarDescricao(normalizarDescricao(dto.dsCondicao()));
    }

    // VALIDAR DADOS DA ATUALIZAÇÃO
    private void validarDadosAtualizacao(
            CondicaoApoioUpdateDTO dto) {

        if (dto == null) {
            throw new IllegalArgumentException(
                    "Dados da condição de apoio não informados."
            );
        }

        if (dto.nmCondicao() == null
                || dto.nmCondicao().isBlank()) {

            throw new IllegalArgumentException(
                    "Nome da condição é obrigatório."
            );
        }

        validarCategoria(dto.tpCategoria());

        if (dto.nmCondicao().trim().length() > 100) {
            throw new IllegalArgumentException(
                    "Nome da condição deve ter no máximo 100 caracteres."
            );
        }

        validarDescricao(normalizarDescricao(dto.dsCondicao()));
    }

    // VALIDAR DESCRIÇÃO
    private void validarDescricao(String descricao) {

        if (descricao != null && descricao.length() > 255) {
            throw new IllegalArgumentException(
                    "Descrição deve ter no máximo 255 caracteres."
            );
        }
    }

    // NORMALIZAR DESCRIÇÃO OPCIONAL
    private String normalizarDescricao(String descricao) {

        if (descricao == null || descricao.isBlank()) {
            return null;
        }

        return descricao.trim();
    }

    // VALIDAR CATEGORIA
    private void validarCategoria(String categoria) {

        if (!"CONDICAO".equals(categoria)
                && !"APOIO_ADICIONAL".equals(categoria)) {

            throw new IllegalArgumentException(
                    "Categoria deve ser CONDICAO ou APOIO_ADICIONAL."
            );
        }
    }

    // CONVERTER MODEL PARA RESPONSE DTO
    private CondicaoApoioResponseDTO converterParaResponse(
            CondicaoApoio condicao) {

        return new CondicaoApoioResponseDTO(
                condicao.getCdCondicaoApoio(),
                condicao.getCdEstabelecimento(),
                condicao.getTpCategoria(),
                condicao.getNmCondicao(),
                condicao.getDsCondicao(),
                condicao.getFlAtivo(),
                condicao.getDtCadastro()
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
