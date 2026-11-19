package br.com.ari.exception;

import java.time.LocalDateTime;
import java.util.Map;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Formato padrao de erro retornado pela API.")
public record ApiErrorResponse(
        @Schema(description = "Data e hora em que o erro ocorreu.", example = "2026-11-19T14:30:00")
        LocalDateTime timestamp,
        @Schema(description = "Codigo HTTP do erro.", example = "400")
        int status,
        @Schema(description = "Descricao HTTP do status.", example = "Bad Request")
        String error,
        @Schema(description = "Mensagem explicando o erro.", example = "Dados invalidos.")
        String message,
        @Schema(description = "Caminho da requisicao que gerou o erro.", example = "/api/usuarios")
        String path,
        @Schema(description = "Erros associados a campos da requisicao.", nullable = true)
        Map<String, String> fields
) {}
