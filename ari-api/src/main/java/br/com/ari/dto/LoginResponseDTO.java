
package br.com.ari.dto;

import java.time.LocalDateTime;

public record LoginResponseDTO(

        String dsUuid,
        LocalDateTime dtExpiracao

) {}
