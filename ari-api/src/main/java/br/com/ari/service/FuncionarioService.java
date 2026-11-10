
package br.com.ari.service;

import br.com.ari.dao.FuncionarioDAO;

import br.com.ari.dto.FuncionarioRequestDTO;
import br.com.ari.dto.FuncionarioUpdateDTO;
import br.com.ari.dto.FuncionarioSenhaUpdateDTO;
import br.com.ari.dto.FuncionarioLoginDTO;
import br.com.ari.dto.FuncionarioResponseDTO;
import br.com.ari.dto.ResponsavelEstabelecimentoRequestDTO;

import br.com.ari.model.Funcionario;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FuncionarioService {

    private final FuncionarioDAO funcionarioDAO;
    private final PasswordEncoder passwordEncoder;

    // CONSTRUTOR
    public FuncionarioService(
            FuncionarioDAO funcionarioDAO,
            PasswordEncoder passwordEncoder) {

        this.funcionarioDAO = funcionarioDAO;
        this.passwordEncoder = passwordEncoder;
    }

    // CADASTRAR FUNCIONÁRIO
    // O estabelecimento é identificado pelo ADMIN autenticado.
    public int criar(
            Long cdFuncionarioAdmin,
            FuncionarioRequestDTO dto) {

        Funcionario admin = buscarAdmin(cdFuncionarioAdmin);

        if (dto == null) {
            throw new IllegalArgumentException(
                    "Dados do funcionário não informados."
            );
        }

        validarDadosCadastro(dto);

        String email = dto.dsEmailLogin()
                .trim()
                .toLowerCase();

        // VERIFICAR E-MAIL DUPLICADO
        if (funcionarioDAO.buscarPorEmailLogin(email).isPresent()) {
            throw new IllegalArgumentException(
                    "Este e-mail já está sendo utilizado."
            );
        }

        // GERAR HASH BCrypt
        String senhaHash = passwordEncoder.encode(dto.dsSenha());

        // TREINAMENTO PADRÃO: NÃO TREINADO
        String flTreinado = dto.flTreinado() == null
                ? "N"
                : dto.flTreinado();

        validarTreinamento(flTreinado);

        // CONVERTER DTO PARA MODEL
        Funcionario funcionario = Funcionario.builder()
                .cdEstabelecimento(admin.getCdEstabelecimento())
                .nmFuncionario(dto.nmFuncionario().trim())
                .nrTelefone(dto.nrTelefone())
                .dsEmailLogin(email)
                .dsSenhaHash(senhaHash)
                .tpPerfil(dto.tpPerfil())
                .flTreinado(flTreinado)
                .stFuncionario("ATIVO")
                .build();

        return funcionarioDAO.criar(funcionario);
    }

    public int criarAdministradorInicial(
            Long cdEstabelecimento,
            ResponsavelEstabelecimentoRequestDTO dto) {

        if (cdEstabelecimento == null || cdEstabelecimento <= 0) {
            throw new IllegalArgumentException(
                    "Estabelecimento inválido."
            );
        }

        if (dto == null) {
            throw new IllegalArgumentException(
                    "Dados do responsável não informados."
            );
        }

        String email = dto.dsEmailLogin().trim().toLowerCase();

        if (funcionarioDAO.buscarPorEmailLogin(email).isPresent()) {
            throw new IllegalArgumentException(
                    "Este e-mail já está sendo utilizado."
            );
        }

        Funcionario administrador = Funcionario.builder()
                .cdEstabelecimento(cdEstabelecimento)
                .nmFuncionario(dto.nmResponsavel().trim())
                .nrTelefone(dto.nrTelefone())
                .dsEmailLogin(email)
                .dsSenhaHash(passwordEncoder.encode(dto.dsSenha()))
                .tpPerfil("ADMIN")
                .flTreinado("N")
                .stFuncionario("ATIVO")
                .build();

        int linhasAfetadas = funcionarioDAO.criar(administrador);
        if (linhasAfetadas == 0) {
            throw new IllegalStateException(
                    "Não foi possível cadastrar o administrador."
            );
        }

        return linhasAfetadas;
    }

    // BUSCAR FUNCIONÁRIO POR ID
    // Somente ADMIN pode consultar funcionários da própria empresa.
    public Funcionario buscarPorId(
            Long cdFuncionarioAdmin,
            Long cdFuncionarioAlvo) {

        Funcionario admin = buscarAdmin(cdFuncionarioAdmin);

        return buscarFuncionarioDaEmpresa(
                cdFuncionarioAlvo,
                admin.getCdEstabelecimento()
        );
    }

    // LISTAR FUNCIONÁRIOS DA EMPRESA
    public List<Funcionario> buscarPorEstabelecimento(
            Long cdFuncionarioAdmin) {

        Funcionario admin = buscarAdmin(cdFuncionarioAdmin);

        return funcionarioDAO.buscarPorEstabelecimento(
                admin.getCdEstabelecimento()
        );
    }

    // ATUALIZAR DADOS DO FUNCIONÁRIO
    public int atualizar(
            Long cdFuncionarioAdmin,
            Long cdFuncionarioAlvo,
            FuncionarioUpdateDTO dto) {

        Funcionario admin = buscarAdmin(cdFuncionarioAdmin);

        if (dto == null) {
            throw new IllegalArgumentException(
                    "Dados do funcionário não informados."
            );
        }

        validarDadosAtualizacao(dto);

        Funcionario existente = buscarFuncionarioDaEmpresa(
                cdFuncionarioAlvo,
                admin.getCdEstabelecimento()
        );

        validarFuncionarioAtivo(existente);

        String email = dto.dsEmailLogin()
                .trim()
                .toLowerCase();

        // VERIFICAR SE O E-MAIL PERTENCE A OUTRO FUNCIONÁRIO
        Optional<Funcionario> funcionarioComEmail =
                funcionarioDAO.buscarPorEmailLogin(email);

        if (funcionarioComEmail.isPresent()
                && !funcionarioComEmail.get()
                .getCdFuncionario()
                .equals(cdFuncionarioAlvo)) {

            throw new IllegalArgumentException(
                    "Este e-mail já está sendo utilizado."
            );
        }

        validarTreinamento(dto.flTreinado());

        // CONVERTER DTO PARA MODEL
        Funcionario funcionario = Funcionario.builder()
                .cdFuncionario(cdFuncionarioAlvo)
                .cdEstabelecimento(admin.getCdEstabelecimento())
                .nmFuncionario(dto.nmFuncionario().trim())
                .nrTelefone(dto.nrTelefone())
                .dsEmailLogin(email)
                .tpPerfil(dto.tpPerfil())
                .flTreinado(dto.flTreinado())
                .stFuncionario(existente.getStFuncionario())
                .build();

        return funcionarioDAO.atualizar(funcionario);
    }

    // ATUALIZAR SENHA DO FUNCIONÁRIO
    // Conforme RN17, somente ADMIN pode realizar essa operação.
    public int atualizarSenha(
            Long cdFuncionarioAdmin,
            Long cdFuncionarioAlvo,
            FuncionarioSenhaUpdateDTO dto) {

        Funcionario admin = buscarAdmin(cdFuncionarioAdmin);

        if (dto == null
                || dto.dsNovaSenha() == null
                || dto.dsNovaSenha().isBlank()) {

            throw new IllegalArgumentException(
                    "Nova senha não informada."
            );
        }

        if (dto.dsNovaSenha().length() < 8) {
            throw new IllegalArgumentException(
                    "A senha deve ter pelo menos 8 caracteres."
            );
        }

        Funcionario existente = buscarFuncionarioDaEmpresa(
                cdFuncionarioAlvo,
                admin.getCdEstabelecimento()
        );

        validarFuncionarioAtivo(existente);

        // GERAR HASH BCrypt
        String senhaHash = passwordEncoder.encode(
                dto.dsNovaSenha()
        );

        return funcionarioDAO.atualizarSenha(
                cdFuncionarioAlvo,
                senhaHash
        );
    }

    // INATIVAR FUNCIONÁRIO
    // Preserva o histórico de avisos.
    public int inativar(
            Long cdFuncionarioAdmin,
            Long cdFuncionarioAlvo) {

        Funcionario admin = buscarAdmin(cdFuncionarioAdmin);

        Funcionario funcionario = buscarFuncionarioDaEmpresa(
                cdFuncionarioAlvo,
                admin.getCdEstabelecimento()
        );

        if ("INATIVO".equals(funcionario.getStFuncionario())) {
            throw new IllegalStateException(
                    "Este funcionário já está inativo."
            );
        }

        return funcionarioDAO.inativar(
                cdFuncionarioAlvo,
                admin.getCdEstabelecimento()
        );
    }

    // EXCLUIR FUNCIONÁRIO
    // A exclusão física depende da existência de referências históricas.
    public int excluir(
            Long cdFuncionarioAdmin,
            Long cdFuncionarioAlvo) {

        Funcionario admin = buscarAdmin(cdFuncionarioAdmin);

        buscarFuncionarioDaEmpresa(
                cdFuncionarioAlvo,
                admin.getCdEstabelecimento()
        );

        try {

            return funcionarioDAO.excluir(cdFuncionarioAlvo);

        } catch (DataIntegrityViolationException e) {
            return funcionarioDAO.inativar(
                    cdFuncionarioAlvo,
                    admin.getCdEstabelecimento()
            );
        }
    }

    // AUTENTICAR FUNCIONÁRIO
    public FuncionarioResponseDTO autenticar(FuncionarioLoginDTO dto) {

        if (dto == null
                || dto.dsEmailLogin() == null
                || dto.dsEmailLogin().isBlank()
                || dto.dsSenha() == null
                || dto.dsSenha().isBlank()) {

            throw new IllegalArgumentException(
                    "E-mail e senha são obrigatórios."
            );
        }

        String email = dto.dsEmailLogin()
                .trim()
                .toLowerCase();

        // BUSCAR FUNCIONÁRIO PELO E-MAIL
        Funcionario funcionario =
                funcionarioDAO.buscarPorEmailLogin(email)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "E-mail ou senha inválidos."
                                )
                        );

        // VERIFICAR SITUAÇÃO DO FUNCIONÁRIO
        if (!"ATIVO".equals(funcionario.getStFuncionario())) {
            throw new IllegalArgumentException(
                    "E-mail ou senha inválidos."
            );
        }

        // COMPARAR SENHA COM HASH BCrypt
        boolean senhaValida = passwordEncoder.matches(
                dto.dsSenha(),
                funcionario.getDsSenhaHash()
        );

        if (!senhaValida) {
            throw new IllegalArgumentException(
                    "E-mail ou senha inválidos."
            );
        }

        return converterParaResponse(funcionario);
    }

    // CONVERTER MODEL PARA RESPONSE DTO
    private FuncionarioResponseDTO converterParaResponse(
            Funcionario funcionario) {

        return new FuncionarioResponseDTO(
                funcionario.getCdFuncionario(),
                funcionario.getCdEstabelecimento(),
                funcionario.getNmFuncionario(),
                funcionario.getNrTelefone(),
                funcionario.getDsEmailLogin(),
                funcionario.getTpPerfil(),
                funcionario.getFlTreinado(),
                funcionario.getStFuncionario(),
                funcionario.getDtCadastro()
        );
    }

    // BUSCAR E VALIDAR ADMIN
    private Funcionario buscarAdmin(Long id) {

        validarId(id);

        Funcionario admin = funcionarioDAO.buscarPorId(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Funcionário administrador não encontrado."
                        )
                );

        validarFuncionarioAtivo(admin);

        if (!"ADMIN".equals(admin.getTpPerfil())) {
            throw new IllegalArgumentException(
                    "Acesso permitido somente para ADMIN."
            );
        }

        return admin;
    }

    // BUSCAR FUNCIONÁRIO DA EMPRESA DO ADMIN
    private Funcionario buscarFuncionarioDaEmpresa(
            Long id,
            Long cdEstabelecimento) {

        validarId(id);

        Funcionario funcionario = funcionarioDAO.buscarPorId(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Funcionário não encontrado."
                        )
                );

        if (!funcionario.getCdEstabelecimento()
                .equals(cdEstabelecimento)) {

            throw new IllegalArgumentException(
                    "Funcionário não pertence ao seu estabelecimento."
            );
        }

        return funcionario;
    }

    // VALIDAR SITUAÇÃO DO FUNCIONÁRIO
    private void validarFuncionarioAtivo(Funcionario funcionario) {

        if (!"ATIVO".equals(funcionario.getStFuncionario())) {
            throw new IllegalStateException(
                    "Funcionário inativo não pode realizar esta operação."
            );
        }
    }

    // VALIDAR DADOS DO CADASTRO
    private void validarDadosCadastro(FuncionarioRequestDTO dto) {

        if (dto.nmFuncionario() == null
                || dto.nmFuncionario().isBlank()) {

            throw new IllegalArgumentException(
                    "Nome é obrigatório."
            );
        }

        if (dto.dsEmailLogin() == null
                || dto.dsEmailLogin().isBlank()) {

            throw new IllegalArgumentException(
                    "E-mail de login é obrigatório."
            );
        }

        if (dto.dsSenha() == null
                || dto.dsSenha().length() < 8) {

            throw new IllegalArgumentException(
                    "A senha deve ter pelo menos 8 caracteres."
            );
        }

        if (!"ADMIN".equals(dto.tpPerfil())
                && !"ATENDENTE".equals(dto.tpPerfil())) {

            throw new IllegalArgumentException(
                    "Perfil deve ser ADMIN ou ATENDENTE."
            );
        }
    }

    // VALIDAR DADOS DA ATUALIZAÇÃO
    private void validarDadosAtualizacao(FuncionarioUpdateDTO dto) {

        if (dto.nmFuncionario() == null
                || dto.nmFuncionario().isBlank()) {

            throw new IllegalArgumentException(
                    "Nome é obrigatório."
            );
        }

        if (dto.dsEmailLogin() == null
                || dto.dsEmailLogin().isBlank()) {

            throw new IllegalArgumentException(
                    "E-mail de login é obrigatório."
            );
        }

        if (!"ADMIN".equals(dto.tpPerfil())
                && !"ATENDENTE".equals(dto.tpPerfil())) {

            throw new IllegalArgumentException(
                    "Perfil deve ser ADMIN ou ATENDENTE."
            );
        }
    }

    // VALIDAR TREINAMENTO
    private void validarTreinamento(String flTreinado) {

        if (!"S".equals(flTreinado)
                && !"N".equals(flTreinado)) {

            throw new IllegalArgumentException(
                    "Treinamento deve ser S ou N."
            );
        }
    }

    // VALIDAR ID
    private void validarId(Long id) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    "ID inválido."
            );
        }
    }


    // IDENTIFICAR ADMIN AUTENTICADO PELO E-MAIL
    public Long buscarIdAdminAutenticado(String email) {

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "Identificação do funcionário não informada."
            );
        }

        Funcionario admin = funcionarioDAO.buscarPorEmailLogin(
                email.trim().toLowerCase()
        ).orElseThrow(() ->
                new IllegalArgumentException(
                        "Funcionário não encontrado."
                )
        );

        validarFuncionarioAtivo(admin);

        if (!"ADMIN".equals(admin.getTpPerfil())) {
            throw new IllegalArgumentException(
                    "Acesso permitido somente para ADMIN."
            );
        }

        return admin.getCdFuncionario();
    }

    // VALIDAR SE O ADMIN PERTENCE AO ESTABELECIMENTO
    public void validarAcessoAdminEstabelecimento(
            String email,
            Long cdEstabelecimento) {

        validarId(cdEstabelecimento);

        // IDENTIFICAR ADMIN PELO E-MAIL AUTENTICADO
        Long cdFuncionarioAdmin =
                buscarIdAdminAutenticado(email);

        Funcionario admin = funcionarioDAO.buscarPorId(
                cdFuncionarioAdmin
        ).orElseThrow(() ->
                new IllegalArgumentException(
                        "Funcionário administrador não encontrado."
                )
        );

        // CONFERIR VÍNCULO COM A EMPRESA
        if (!admin.getCdEstabelecimento()
                .equals(cdEstabelecimento)) {

            throw new IllegalArgumentException(
                    "Você não possui acesso a este estabelecimento."
            );
        }
    }


    // IDENTIFICAR FUNCIONÁRIO AUTENTICADO PELO E-MAIL
    // Permite ADMIN e ATENDENTE, desde que estejam ativos.
    public Long buscarIdFuncionarioAutenticado(String email) {

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "Identificação do funcionário não informada."
            );
        }

        Funcionario funcionario =
                funcionarioDAO.buscarPorEmailLogin(
                        email.trim().toLowerCase()
                ).orElseThrow(() ->
                        new IllegalArgumentException(
                                "Funcionário não encontrado."
                        )
                );

        validarFuncionarioAtivo(funcionario);

        return funcionario.getCdFuncionario();
    }




}
