
package br.com.ari.config;

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

    private final FuncionarioSessaoService funcionarioSessaoService;

    public FuncionarioSessaoFilter(
            FuncionarioSessaoService funcionarioSessaoService) {
        this.funcionarioSessaoService = funcionarioSessaoService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String uuid = request.getHeader("X-Funcionario-Session-UUID");

        if (uuid != null && !uuid.isBlank()) {

            Sessao sessao = funcionarioSessaoService
                    .buscarAtiva(uuid)
                    .orElse(null);

            if (sessao != null) {

                var autenticacao =
                        new UsernamePasswordAuthenticationToken(
                                sessao.email(),
                                null,
                                List.of(
                                        new SimpleGrantedAuthority(
                                                "ROLE_" + sessao.perfil()
                                        )
                                )
                        );

                SecurityContextHolder.getContext()
                        .setAuthentication(autenticacao);
            }
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
