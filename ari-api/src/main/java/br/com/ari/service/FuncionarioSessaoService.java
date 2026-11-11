package br.com.ari.service;

import br.com.ari.dto.FuncionarioResponseDTO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class FuncionarioSessaoService {

    private static final long DURACAO_SESSAO_HORAS = 2;

    private final ConcurrentHashMap<String, Sessao> sessoes =
            new ConcurrentHashMap<>();

    public Sessao criar(FuncionarioResponseDTO funcionario) {
        LocalDateTime expiracao = LocalDateTime.now()
                .plusHours(DURACAO_SESSAO_HORAS);

        Sessao sessao = new Sessao(
                UUID.randomUUID().toString(),
                funcionario.dsEmailLogin(),
                funcionario.tpPerfil(),
                expiracao
        );

        sessoes.put(sessao.uuid(), sessao);
        return sessao;
    }

    public Optional<Sessao> buscarAtiva(String uuid) {
        if (uuid == null || uuid.isBlank()) {
            return Optional.empty();
        }

        Sessao sessao = sessoes.get(uuid);
        if (sessao == null || !sessao.expiracao().isAfter(LocalDateTime.now())) {
            if (sessao != null) {
                sessoes.remove(uuid);
            }
            return Optional.empty();
        }

        return Optional.of(sessao);
    }

    public void encerrar(String uuid) {
        if (uuid != null && !uuid.isBlank()) {
            sessoes.remove(uuid);
        }
    }

    public record Sessao(
            String uuid,
            String email,
            String perfil,
            LocalDateTime expiracao
    ) {}
}
