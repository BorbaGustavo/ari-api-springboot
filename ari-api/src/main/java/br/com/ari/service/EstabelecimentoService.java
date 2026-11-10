
package br.com.ari.service;

import br.com.ari.dao.EstabelecimentoDAO;

import br.com.ari.dto.EstabelecimentoRequestDTO;
import br.com.ari.dto.EstabelecimentoUpdateDTO;
import br.com.ari.dto.EstabelecimentoAcolhimentoUpdateDTO;
import br.com.ari.dto.EstabelecimentoSuspensaoDTO;
import br.com.ari.integration.BrasilApiCepResponse;
import br.com.ari.integration.BrasilApiCnpjResponse;
import br.com.ari.integration.BrasilApiService;
import br.com.ari.dto.ResponsavelEstabelecimentoRequestDTO;

import br.com.ari.model.Estabelecimento;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class EstabelecimentoService {

    private final EstabelecimentoDAO estabelecimentoDAO;
    private final BrasilApiService brasilApiService;
    private final FuncionarioService funcionarioService;

    // CONSTRUTOR
    public EstabelecimentoService(
            EstabelecimentoDAO estabelecimentoDAO,
            BrasilApiService brasilApiService,
            FuncionarioService funcionarioService) {

        this.estabelecimentoDAO = estabelecimentoDAO;
        this.brasilApiService = brasilApiService;
        this.funcionarioService = funcionarioService;
    }

    // CADASTRAR ESTABELECIMENTO
    @Transactional
    public Estabelecimento criar(EstabelecimentoRequestDTO dto) {

        validarDadosCadastro(dto);

        String cnpj = dto.nrCnpj().trim();
        validarDadosBrasilApi(
                cnpj,
                dto.nrCep()
        );

        // VERIFICAR SE O CNPJ JÁ ESTÁ CADASTRADO
        if (estabelecimentoDAO.buscarPorCnpj(cnpj).isPresent()) {
            throw new IllegalArgumentException(
                    "Este CNPJ já está cadastrado."
            );
        }

        // CONVERTER DTO PARA MODEL
        Estabelecimento estabelecimento = converterParaModel(dto);

        try {

            int linhasAfetadas = estabelecimentoDAO.criar(estabelecimento);

            if (linhasAfetadas == 0) {
                throw new IllegalStateException(
                        "Não foi possível cadastrar o estabelecimento."
                );
            }

        } catch (DataIntegrityViolationException e) {

            throw new IllegalArgumentException(
                    "Não foi possível cadastrar. "
                            + "Verifique se o CNPJ já está cadastrado."
            );
        }

        // RECUPERAR ESTABELECIMENTO CADASTRADO
        Estabelecimento criado = estabelecimentoDAO.buscarPorCnpj(cnpj)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Erro ao recuperar estabelecimento cadastrado."
                        )
                );

        funcionarioService.criarAdministradorInicial(
                criado.getCdEstabelecimento(),
                dto.responsavel()
        );

        return criado;
    }

    // LISTAR TODOS OS ESTABELECIMENTOS
    // A autorização dessa operação deve ser definida no Controller/Security.
    public List<Estabelecimento> buscarTodos() {

        return estabelecimentoDAO.buscarTodos();
    }

    // BUSCAR ESTABELECIMENTO POR ID
    public Optional<Estabelecimento> buscarPorId(Long id) {

        validarId(id);

        return estabelecimentoDAO.buscarPorId(id);
    }

    // ATUALIZAR DADOS CADASTRAIS
    public int atualizar(
            Long id,
            EstabelecimentoUpdateDTO dto) {

        validarId(id);
        validarDadosAtualizacao(dto);

        Estabelecimento existente = estabelecimentoDAO.buscarPorId(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Estabelecimento não encontrado."
                        )
                );

        validarDadosBrasilApi(
                existente.getNrCnpj(),
                dto.nrCep()
        );

        // PRESERVAR CNPJ, ESTADO E CONTROLES OPERACIONAIS
        // A atualização cadastral não altera esses campos.
        Estabelecimento estabelecimento = converterParaModel(dto);

        estabelecimento.setCdEstabelecimento(id);
        estabelecimento.setNrCnpj(existente.getNrCnpj());

        estabelecimento.setFlAcolhimentoAtivo(
                existente.getFlAcolhimentoAtivo()
        );

        estabelecimento.setDtSuspensaoAte(
                existente.getDtSuspensaoAte()
        );

        estabelecimento.setStEstabelecimento(
                existente.getStEstabelecimento()
        );

        estabelecimento.setDtCadastro(
                existente.getDtCadastro()
        );

        return estabelecimentoDAO.atualizar(estabelecimento);
    }

    // DESATIVAR RECEBIMENTO DE ACOLHIMENTOS
    // Preserva o cadastro e o histórico do estabelecimento.
    public int excluir(Long id) {

        validarId(id);
        buscarEstabelecimentoExistente(id);

        try {
            return estabelecimentoDAO.excluirFisico(id);
        } catch (DataIntegrityViolationException e) {
            return estabelecimentoDAO.excluir(id);
        }
    }

    // HABILITAR OU DESABILITAR ACOLHIMENTO
    public int atualizarAcolhimento(
            Long id,
            EstabelecimentoAcolhimentoUpdateDTO dto) {

        validarId(id);

        if (dto == null
                || dto.flAcolhimentoAtivo() == null
                || (!"S".equals(dto.flAcolhimentoAtivo())
                && !"N".equals(dto.flAcolhimentoAtivo()))) {

            throw new IllegalArgumentException(
                    "Informe S ou N para o acolhimento."
            );
        }

        Estabelecimento estabelecimento =
                buscarEstabelecimentoExistente(id);

        // SOMENTE ESTABELECIMENTOS ATIVOS PODEM HABILITAR ACOLHIMENTO
        if ("S".equals(dto.flAcolhimentoAtivo())
                && !"ATIVO".equals(estabelecimento.getStEstabelecimento())) {

            throw new IllegalStateException(
                    "Somente estabelecimentos ativos podem habilitar acolhimentos."
            );
        }

        estabelecimento.setFlAcolhimentoAtivo(
                dto.flAcolhimentoAtivo()
        );

        return estabelecimentoDAO.atualizar(estabelecimento);
    }

    // CONFIGURAR SUSPENSÃO TEMPORÁRIA
    public int suspenderAcolhimento(
            Long id,
            EstabelecimentoSuspensaoDTO dto) {

        validarId(id);

        if (dto == null || dto.dtSuspensaoAte() == null) {
            throw new IllegalArgumentException(
                    "Data de suspensão não informada."
            );
        }

        if (!dto.dtSuspensaoAte().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException(
                    "A data de suspensão deve estar no futuro."
            );
        }

        Estabelecimento estabelecimento =
                buscarEstabelecimentoExistente(id);

        if (!"ATIVO".equals(estabelecimento.getStEstabelecimento())) {
            throw new IllegalStateException(
                    "Somente estabelecimentos ativos podem configurar suspensão."
            );
        }

        // A GRADE SEMANAL NÃO É ALTERADA
        estabelecimento.setDtSuspensaoAte(
                dto.dtSuspensaoAte()
        );

        return estabelecimentoDAO.atualizar(estabelecimento);
    }

    // REMOVER SUSPENSÃO TEMPORÁRIA
    public int removerSuspensao(Long id) {

        validarId(id);

        Estabelecimento estabelecimento =
                buscarEstabelecimentoExistente(id);

        estabelecimento.setDtSuspensaoAte(null);

        return estabelecimentoDAO.atualizar(estabelecimento);
    }

    // BUSCAR ESTABELECIMENTOS PRÓXIMOS
    public List<Estabelecimento> buscarProximos(
            Double latitude,
            Double longitude) {

        if (latitude == null
                || longitude == null
                || latitude < -90
                || latitude > 90
                || longitude < -180
                || longitude > 180) {

            throw new IllegalArgumentException(
                    "Coordenadas inválidas."
            );
        }

        return estabelecimentoDAO.buscarProximos(
                latitude,
                longitude
        );
    }

    // BUSCAR ESTABELECIMENTOS POR CEP
    public List<Estabelecimento> buscarPorCep(String cep) {

        if (cep == null || !cep.matches("\\d{8}")) {
            throw new IllegalArgumentException(
                    "CEP inválido. Informe 8 dígitos."
            );
        }

        return estabelecimentoDAO.buscarPorCep(cep);
    }

    // BUSCAR ESTABELECIMENTO EXISTENTE
    private Estabelecimento buscarEstabelecimentoExistente(Long id) {

        return estabelecimentoDAO.buscarPorId(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Estabelecimento não encontrado."
                        )
                );
    }

    // VALIDAR DADOS DO CADASTRO
    private void validarDadosCadastro(EstabelecimentoRequestDTO dto) {

        if (dto == null) {
            throw new IllegalArgumentException(
                    "Dados do estabelecimento são obrigatórios."
            );
        }

        validarCnpj(dto.nrCnpj());

        if (dto.nmRazaoSocial() == null
                || dto.nmRazaoSocial().isBlank()) {

            throw new IllegalArgumentException(
                    "Razão social é obrigatória."
            );
        }

        if (dto.dsEmail() == null
                || dto.dsEmail().isBlank()) {

            throw new IllegalArgumentException(
                    "E-mail é obrigatório."
            );
        }

        if (dto.dsTipoEstabelecimento() == null
                || dto.dsTipoEstabelecimento().isBlank()) {

            throw new IllegalArgumentException(
                    "Tipo de estabelecimento é obrigatório."
            );
        }
    }

    private void validarDadosBrasilApi(
            String cnpj,
            String cep) {

        BrasilApiCnpjResponse dadosCnpj =
                brasilApiService.consultarCnpj(cnpj);

        if (!"ATIVA".equalsIgnoreCase(
                normalizar(dadosCnpj.descricaoSituacaoCadastral()))) {

            throw new IllegalArgumentException(
                    "O CNPJ não está ativo na Receita Federal."
            );
        }

        if (cep == null || cep.isBlank()) {
            return;
        }

        BrasilApiCepResponse dadosCep =
                brasilApiService.consultarCep(cep);
        if (dadosCep.cep() == null || dadosCep.cep().isBlank()) {
            throw new IllegalArgumentException(
                    "O CEP informado não foi encontrado."
            );
        }
    }

    private String normalizar(String valor) {
        if (valor == null) {
            return "";
        }

        return java.text.Normalizer.normalize(
                        valor,
                        java.text.Normalizer.Form.NFD
                )
                .replaceAll("\\p{M}", "")
                .replaceAll("[^A-Za-z0-9]", "")
                .toUpperCase();
    }

    // VALIDAR DADOS DA ATUALIZAÇÃO
    private void validarDadosAtualizacao(
            EstabelecimentoUpdateDTO dto) {

        if (dto == null) {
            throw new IllegalArgumentException(
                    "Dados do estabelecimento não informados."
            );
        }

        if (dto.nmRazaoSocial() == null
                || dto.nmRazaoSocial().isBlank()) {

            throw new IllegalArgumentException(
                    "Razão social é obrigatória."
            );
        }

        if (dto.dsEmail() == null
                || dto.dsEmail().isBlank()) {

            throw new IllegalArgumentException(
                    "E-mail é obrigatório."
            );
        }

        if (dto.dsTipoEstabelecimento() == null
                || dto.dsTipoEstabelecimento().isBlank()) {

            throw new IllegalArgumentException(
                    "Tipo de estabelecimento é obrigatório."
            );
        }
    }

    // VALIDAR FORMATO E DÍGITOS VERIFICADORES DO CNPJ
    private void validarCnpj(String cnpj) {

        if (cnpj == null || !cnpj.matches("\\d{14}")) {
            throw new IllegalArgumentException(
                    "CNPJ deve conter 14 dígitos."
            );
        }

        // REJEITAR CNPJ COM TODOS OS DÍGITOS IGUAIS
        if (cnpj.chars().distinct().count() == 1) {
            throw new IllegalArgumentException(
                    "CNPJ inválido."
            );
        }

        int[] pesosPrimeiroDigito = {
                5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2
        };

        int[] pesosSegundoDigito = {
                6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2
        };

        int primeiroDigito = calcularDigitoCnpj(
                cnpj.substring(0, 12),
                pesosPrimeiroDigito
        );

        if (primeiroDigito != Character.getNumericValue(
                cnpj.charAt(12))) {

            throw new IllegalArgumentException("CNPJ inválido.");
        }

        int segundoDigito = calcularDigitoCnpj(
                cnpj.substring(0, 13),
                pesosSegundoDigito
        );

        if (segundoDigito != Character.getNumericValue(
                cnpj.charAt(13))) {

            throw new IllegalArgumentException("CNPJ inválido.");
        }
    }

    // CALCULAR DÍGITO VERIFICADOR DO CNPJ
    private int calcularDigitoCnpj(
            String base,
            int[] pesos) {

        int soma = 0;

        for (int i = 0; i < pesos.length; i++) {
            soma += Character.getNumericValue(base.charAt(i))
                    * pesos[i];
        }

        int resto = soma % 11;

        return resto < 2 ? 0 : 11 - resto;
    }

    // CONVERTER REQUEST DTO PARA MODEL
    private Estabelecimento converterParaModel(
            EstabelecimentoRequestDTO dto) {

        return Estabelecimento.builder()
                .nrCnpj(dto.nrCnpj().trim())
                .nmRazaoSocial(dto.nmRazaoSocial().trim())
                .nmFantasia(dto.nmFantasia())
                .dsEmail(dto.dsEmail().trim().toLowerCase())
                .nrTelefone(dto.nrTelefone())
                .dsTipoEstabelecimento(dto.dsTipoEstabelecimento().trim())
                .nrCep(dto.nrCep())
                .dsLogradouro(dto.dsLogradouro())
                .nrEndereco(dto.nrEndereco())
                .dsComplemento(dto.dsComplemento())
                .nmBairro(dto.nmBairro())
                .nmCidade(dto.nmCidade())
                .sgUf(dto.sgUf())
                .nrLatitude(dto.nrLatitude())
                .nrLongitude(dto.nrLongitude())
                .build();
    }

    // CONVERTER UPDATE DTO PARA MODEL
    private Estabelecimento converterParaModel(
            EstabelecimentoUpdateDTO dto) {

        return Estabelecimento.builder()
                .nmRazaoSocial(dto.nmRazaoSocial().trim())
                .nmFantasia(dto.nmFantasia())
                .dsEmail(dto.dsEmail().trim().toLowerCase())
                .nrTelefone(dto.nrTelefone())
                .dsTipoEstabelecimento(dto.dsTipoEstabelecimento().trim())
                .nrCep(dto.nrCep())
                .dsLogradouro(dto.dsLogradouro())
                .nrEndereco(dto.nrEndereco())
                .dsComplemento(dto.dsComplemento())
                .nmBairro(dto.nmBairro())
                .nmCidade(dto.nmCidade())
                .sgUf(dto.sgUf())
                .nrLatitude(dto.nrLatitude())
                .nrLongitude(dto.nrLongitude())
                .build();
    }

    // VALIDAR ID
    private void validarId(Long id) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    "ID do estabelecimento inválido."
            );
        }
    }


    // CONFIGURAR OU REMOVER SUSPENSÃO TEMPORÁRIA
    public int atualizarSuspensao(
            Long id,
            EstabelecimentoSuspensaoDTO dto) {

        validarId(id);

        if (dto == null) {
            throw new IllegalArgumentException(
                    "Dados da suspensão não informados."
            );
        }

        Estabelecimento estabelecimento =
                buscarEstabelecimentoExistente(id);

        if (!"ATIVO".equals(estabelecimento.getStEstabelecimento())) {
            throw new IllegalStateException(
                    "Estabelecimento não está ativo."
            );
        }

        if (dto.dtSuspensaoAte() != null
                && !dto.dtSuspensaoAte().isAfter(LocalDateTime.now())) {

            throw new IllegalArgumentException(
                    "A data de suspensão deve estar no futuro."
            );
        }

        // DATA FUTURA = SUSPENDER
        // NULL = REMOVER SUSPENSÃO
        estabelecimento.setDtSuspensaoAte(dto.dtSuspensaoAte());

        return estabelecimentoDAO.atualizar(estabelecimento);
    }


}
