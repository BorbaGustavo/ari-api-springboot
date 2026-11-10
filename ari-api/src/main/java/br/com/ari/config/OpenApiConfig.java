package br.com.ari.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "ARI API",
                version = "1.0.0",
                description = """
                        API REST do projeto ARI para gerenciamento de
                        estabelecimentos, funcionários, condições de apoio,
                        disponibilidades e solicitações de acolhimento.

                        Consulte cada operação para ver exemplos de JSON,
                        parâmetros, validações e códigos de resposta.
                        """,
                contact = @Contact(name = "Equipe ARI")
        )
)
@SecurityScheme(
        name = "sessionUuid",
        type = SecuritySchemeType.APIKEY,
        in = io.swagger.v3.oas.annotations.enums.SecuritySchemeIn.HEADER,
        paramName = "X-Session-UUID",
        description = "UUID retornado no login da usuária. Use somente nas operações que exigem sessão."
)
public class OpenApiConfig {
}
