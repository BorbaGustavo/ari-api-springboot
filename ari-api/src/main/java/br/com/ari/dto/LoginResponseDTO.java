
package br.com.ari.dto;

import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Sessão criada para a usuária.")
public record LoginResponseDTO(

        @Schema(description = "UUID para enviar no header X-Session-UUID.", example = "550e8400-e29b-41d4-a716-446655440000")
        String dsUuid,
        @Schema(description = "Data e hora de expiração da sessão.", example = "2026-11-10T18:00:00")
        LocalDateTime dtExpiracao

) {}
