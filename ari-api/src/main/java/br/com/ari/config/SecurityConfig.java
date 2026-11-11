
package br.com.ari.config;

import br.com.ari.service.FuncionarioSessaoService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            FuncionarioSessaoService funcionarioSessaoService)
            throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {})
                .authorizeHttpRequests(auth -> auth

                        // Permitir preflight do navegador
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Login público
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/funcionarios/login"
                        ).permitAll()

                        // Demais rotas de funcionários exigem sessão
                        .requestMatchers("/api/funcionarios/**").authenticated()

                        // Manter as demais rotas conforme a configuração atual
                        .anyRequest().permitAll()
                )
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable());

        http.addFilterBefore(
                new FuncionarioSessaoFilter(funcionarioSessaoService),
                AnonymousAuthenticationFilter.class
        );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
