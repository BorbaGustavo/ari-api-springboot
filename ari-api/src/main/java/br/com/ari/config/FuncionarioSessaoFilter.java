
package br.com.ari.config;

import br.com.ari.dto.FuncionarioResponseDTO;
import br.com.ari.service.FuncionarioService;
import br.com.ari.service.FuncionarioSessaoService;
import br.com.ari.service.FuncionarioSessaoService.Sessao;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class FuncionarioSessaoFilter extends OncePerRequestFilter {

    private static final String HEADER_SESSAO =
            "X-Funcionario-Session-UUID";

    private static final String PERFIL_ADMIN = "ADMIN";
    private static final String PERFIL_ATENDENTE = "ATENDENTE";

    private final FuncionarioSessaoService funcionarioSessaoService;
    private final FuncionarioService funcionarioService;

    public FuncionarioSessaoFilter(
            FuncionarioSessaoService funcionarioSessaoService,
            FuncionarioService funcionarioService) {

        this.funcionarioSessaoService = funcionarioSessaoService;
        this.funcionarioService = funcionarioService;
    }

    // =====================================================
    // VALIDAR SESSÃO DO FUNCIONÁRIO
    //
    // 1. Recupera o UUID enviado no cabeçalho.
    // 2. Localiza a sessão ativa.
    // 3. Consulta o funcionário atualizado no banco.
    // 4. Atualiza os dados da sessão.
    // 5. Cria a autenticação com o ID do funcionário.
    // =====================================================

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // Evita reaproveitar autenticação anterior.
        SecurityContextHolder.clearContext();

        String uuid = request.getHeader(HEADER_SESSAO);

        try {

            if (uuid != null && !uuid.isBlank()) {

                autenticarFuncionario(uuid.trim());
            }

            filterChain.doFilter(request, response);

        } finally {

            // Limpa a autenticação ao finalizar a requisição.
            SecurityContextHolder.clearContext();
        }
    }

    // =====================================================
    // AUTENTICAR FUNCIONÁRIO PELA SESSÃO
    // =====================================================

    private void autenticarFuncionario(String uuid) {

        Sessao sessao = funcionarioSessaoService
                .buscarAtiva(uuid)
                .orElse(null);

        if (sessao == null) {
            return;
        }

        try {

            // Busca os dados atuais no banco.
            // Funcionário inexistente ou inativo não será autenticado.
            FuncionarioResponseDTO funcionario =
                    funcionarioService.buscarFuncionarioAutenticado(
                            sessao.cdFuncionario()
                    );

            // Confere se o perfil atual é permitido.
            validarPerfil(funcionario.tpPerfil());

            // Atualiza os dados armazenados na sessão.
            // Não renova o prazo de expiração.
            funcionarioSessaoService.atualizarDados(
                    uuid,
                    funcionario
            );

            // O Principal contém o ID numérico do funcionário.
            UsernamePasswordAuthenticationToken autenticacao =
                    new UsernamePasswordAuthenticationToken(
                            funcionario.cdFuncionario().toString(),
                            null,
                            List.of(
                                    new SimpleGrantedAuthority(
                                            "ROLE_" + funcionario.tpPerfil()
                                    )
                            )
                    );

            SecurityContextHolder.getContext()
                    .setAuthentication(autenticacao);

        } catch (IllegalArgumentException | IllegalStateException e) {

            // Sessão inválida, funcionário inexistente,
            // inativo ou com perfil não permitido.
            funcionarioSessaoService.encerrar(uuid);

            SecurityContextHolder.clearContext();
        }
    }

    // =====================================================
    // VALIDAR PERFIL
    // =====================================================

    private void validarPerfil(String perfil) {

        if (!PERFIL_ADMIN.equals(perfil)
                && !PERFIL_ATENDENTE.equals(perfil)) {

            throw new IllegalArgumentException(
                    "Perfil de funcionário inválido."
            );
        }
    }
}
