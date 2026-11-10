
package br.com.ari.service;

import br.com.ari.dao.UsuarioDAO;
import br.com.ari.dto.LoginRequestDTO;
import br.com.ari.dto.LoginResponseDTO;
import br.com.ari.dto.UsuarioRequestDTO;
import br.com.ari.dto.UsuarioUpdateDTO;
import br.com.ari.dto.UsuarioSenhaUpdateDTO;

import br.com.ari.model.Usuario;
import br.com.ari.model.SessaoWeb;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioDAO usuarioDAO;
    private final PasswordEncoder passwordEncoder;
    private final SessaoWebService sessaoWebService;

    // CONSTRUTOR
    public UsuarioService(
            UsuarioDAO usuarioDAO,
            PasswordEncoder passwordEncoder,
            SessaoWebService sessaoWebService) {

        this.usuarioDAO = usuarioDAO;
        this.passwordEncoder = passwordEncoder;
        this.sessaoWebService = sessaoWebService;
    }

    // CADASTRAR USUÁRIO
    public int criar(UsuarioRequestDTO dto) {

        if (dto == null) {
            throw new IllegalArgumentException(
                    "Dados do usuário não informados."
            );
        }

        validarDadosCadastro(dto);

        String email = dto.dsEmail().trim().toLowerCase();

        // VERIFICAR SE O E-MAIL JÁ ESTÁ CADASTRADO
        if (usuarioDAO.buscarPorEmail(email).isPresent()) {
            throw new IllegalArgumentException(
                    "Este e-mail já está cadastrado."
            );
        }

        // GERAR HASH BCrypt
        String senhaHash = passwordEncoder.encode(dto.dsSenha());

        // CONVERTER DTO PARA MODEL
        Usuario usuario = Usuario.builder()
                .nmUsuario(dto.nmUsuario().trim())
                .nrTelefone(dto.nrTelefone())
                .dsEmail(email)
                .dsSenhaHash(senhaHash)
                .stUsuario("ATIVO")
                .build();

        return usuarioDAO.criar(usuario);
    }

    // BUSCAR USUÁRIO POR ID
    public Optional<Usuario> buscarPorId(Long id) {

        validarId(id);

        return usuarioDAO.buscarPorId(id);
    }

    // BUSCAR TODOS OS USUÁRIOS
    // Operação destinada a consultas administrativas.
    public List<Usuario> buscarTodos() {

        return usuarioDAO.buscarTodos();
    }

    // ATUALIZAR DADOS CADASTRAIS
    public int atualizar(Long id, UsuarioUpdateDTO dto) {

        validarId(id);

        if (dto == null) {
            throw new IllegalArgumentException(
                    "Dados do usuário não informados."
            );
        }

        validarDadosAtualizacao(dto);

        Usuario existente = usuarioDAO.buscarPorId(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Usuário não encontrado."
                        )
                );

        validarUsuarioAtivo(existente);

        String email = dto.dsEmail().trim().toLowerCase();

        // VERIFICAR SE O E-MAIL JÁ PERTENCE A OUTRA USUÁRIA
        Optional<Usuario> usuarioComEmail =
                usuarioDAO.buscarPorEmail(email);

        if (usuarioComEmail.isPresent()
                && !usuarioComEmail.get().getCdUsuario().equals(id)) {

            throw new IllegalArgumentException(
                    "Este e-mail já está sendo utilizado."
            );
        }

        // CONVERTER DTO PARA MODEL
        Usuario usuario = Usuario.builder()
                .cdUsuario(id)
                .nmUsuario(dto.nmUsuario().trim())
                .nrTelefone(dto.nrTelefone())
                .dsEmail(email)
                .build();

        return usuarioDAO.atualizar(usuario);
    }

    // ATUALIZAR SENHA
    public int atualizarSenha(
            Long id,
            UsuarioSenhaUpdateDTO dto) {

        validarId(id);

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

        Usuario existente = usuarioDAO.buscarPorId(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Usuário não encontrado."
                        )
                );

        validarUsuarioAtivo(existente);

        // GERAR NOVO HASH BCrypt
        String senhaHash = passwordEncoder.encode(
                dto.dsNovaSenha()
        );

        return usuarioDAO.atualizarSenha(id, senhaHash);
    }

    // BLOQUEAR USUÁRIO
    // Preserva o cadastro e o histórico de avisos.
    public int excluir(Long id) {

        validarId(id);

        Usuario existente = usuarioDAO.buscarPorId(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Usuário não encontrado."
                        )
                );

        if ("BLOQUEADO".equals(existente.getStUsuario())) {
            throw new IllegalStateException(
                    "Esta conta já está bloqueada."
            );
        }

        return usuarioDAO.excluir(id);
    }

    // AUTENTICAR USUÁRIO
    public LoginResponseDTO autenticar(LoginRequestDTO dto) {

        if (dto == null
                || dto.dsEmail() == null
                || dto.dsEmail().isBlank()
                || dto.dsSenha() == null
                || dto.dsSenha().isBlank()) {

            throw new IllegalArgumentException(
                    "E-mail e senha são obrigatórios."
            );
        }

        String email = dto.dsEmail().trim().toLowerCase();

        // BUSCAR USUÁRIO PELO E-MAIL
        Usuario usuario = usuarioDAO.buscarPorEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "E-mail ou senha inválidos."
                        )
                );

        // VERIFICAR SITUAÇÃO DA CONTA
        if (!"ATIVO".equals(usuario.getStUsuario())) {
            throw new IllegalArgumentException(
                    "E-mail ou senha inválidos."
            );
        }

        // COMPARAR SENHA COM HASH BCrypt
        boolean senhaValida = passwordEncoder.matches(
                dto.dsSenha(),
                usuario.getDsSenhaHash()
        );

        if (!senhaValida) {
            throw new IllegalArgumentException(
                    "E-mail ou senha inválidos."
            );
        }

        // CRIAR SESSÃO COM VALIDADE DE DUAS HORAS
        SessaoWeb sessao = sessaoWebService.criar(
                usuario.getCdUsuario()
        );

        // RETORNAR TOKEN E DATA DE EXPIRAÇÃO
        return new LoginResponseDTO(
                sessao.getDsUuid(),
                sessao.getDtExpiracao()
        );
    }

    // VALIDAR DADOS DO CADASTRO
    private void validarDadosCadastro(UsuarioRequestDTO dto) {

        if (dto.nmUsuario() == null || dto.nmUsuario().isBlank()) {
            throw new IllegalArgumentException(
                    "Nome é obrigatório."
            );
        }

        if (dto.dsEmail() == null || dto.dsEmail().isBlank()) {
            throw new IllegalArgumentException(
                    "E-mail é obrigatório."
            );
        }

        if (dto.dsSenha() == null || dto.dsSenha().isBlank()) {
            throw new IllegalArgumentException(
                    "Senha é obrigatória."
            );
        }

        if (dto.dsSenha().length() < 8) {
            throw new IllegalArgumentException(
                    "A senha deve ter pelo menos 8 caracteres."
            );
        }
    }

    // VALIDAR DADOS DA ATUALIZAÇÃO
    private void validarDadosAtualizacao(UsuarioUpdateDTO dto) {

        if (dto.nmUsuario() == null || dto.nmUsuario().isBlank()) {
            throw new IllegalArgumentException(
                    "Nome é obrigatório."
            );
        }

        if (dto.dsEmail() == null || dto.dsEmail().isBlank()) {
            throw new IllegalArgumentException(
                    "E-mail é obrigatório."
            );
        }
    }

    // VALIDAR SE A CONTA ESTÁ ATIVA
    private void validarUsuarioAtivo(Usuario usuario) {

        if (!"ATIVO".equals(usuario.getStUsuario())) {
            throw new IllegalStateException(
                    "A conta está bloqueada."
            );
        }
    }

    // VALIDAR ID
    private void validarId(Long id) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    "ID de usuário inválido."
            );
        }
    }

}
