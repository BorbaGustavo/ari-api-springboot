package br.com.ari.config;

import br.com.ari.service.FuncionarioSessaoService;
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

    private static final String HEADER = "X-Funcionario-Session-UUID";

    private final FuncionarioSessaoService sessaoService;

    public FuncionarioSessaoFilter(FuncionarioSessaoService sessaoService) {
        this.sessaoService = sessaoService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String uuid = request.getHeader(HEADER);

        sessaoService.buscarAtiva(uuid).ifPresent(sessao -> {
            var autenticacao = new UsernamePasswordAuthenticationToken(
                    sessao.email(),
                    null,
                    List.of(new SimpleGrantedAuthority(
                            "ROLE_" + sessao.perfil()
                    ))
            );
            SecurityContextHolder.getContext().setAuthentication(
                    autenticacao
            );
        });

        filterChain.doFilter(request, response);
    }
}
