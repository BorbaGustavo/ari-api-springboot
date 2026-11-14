
package br.com.ari.config;

import br.com.ari.service.FuncionarioService;
import br.com.ari.service.FuncionarioSessaoService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

@Configuration
public class SecurityConfig {

    // =====================================================
    // CONFIGURAÇÃO DE SEGURANÇA
    // =====================================================

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            FuncionarioSessaoService funcionarioSessaoService,
            FuncionarioService funcionarioService
    ) throws Exception {

        http

                // API não utiliza autenticação por formulário.
                .formLogin(form -> form.disable())

                // API não utiliza autenticação HTTP Basic.
                .httpBasic(basic -> basic.disable())

                // API utiliza autenticação própria por UUID.
                .csrf(csrf -> csrf.disable())

                // Permite configuração de CORS.
                .cors(cors -> {})

                // Não cria sessão HTTP do Spring.
                .sessionManagement(session -> session
                        .sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                // Não armazena requisições para redirecionamento.
                .requestCache(cache -> cache.disable())

                // =====================================================
                // AUTORIZAÇÃO DAS ROTAS
                // =====================================================

                .authorizeHttpRequests(auth -> auth

                        // Preflight do navegador.
                        .requestMatchers(
                                HttpMethod.OPTIONS,
                                "/**"
                        ).permitAll()

                        // Login público dos funcionários.
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/funcionarios/login"
                        ).permitAll()

                        // Demais rotas de funcionários exigem autenticação.
                        .requestMatchers(
                                "/api/funcionarios",
                                "/api/funcionarios/**"
                        ).authenticated()

                        // Demais rotas permanecem públicas neste momento.
                        .anyRequest().permitAll()
                )

                // =====================================================
                // TRATAMENTO DE ACESSO
                // =====================================================

                .exceptionHandling(exception -> exception

                        // Usuário sem autenticação.
                        .authenticationEntryPoint(
                                new HttpStatusEntryPoint(
                                        HttpStatus.UNAUTHORIZED
                                )
                        )
                );

        // =====================================================
        // FILTRO DE SESSÃO DO FUNCIONÁRIO
        // =====================================================

        http.addFilterBefore(
                new FuncionarioSessaoFilter(
                        funcionarioSessaoService,
                        funcionarioService
                ),
                AnonymousAuthenticationFilter.class
        );

        return http.build();
    }

    // =====================================================
    // CRIPTOGRAFIA DE SENHAS
    // =====================================================

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }
}
