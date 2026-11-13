
package br.com.ari.dao;

import br.com.ari.model.AvisoAcolhimento;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;
import java.util.Optional;

@Repository
public class AvisoAcolhimentoDAO
        implements CrudDAO<AvisoAcolhimento, Long> {

    private final JdbcTemplate jdbcTemplate;

    public AvisoAcolhimentoDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // =====================================================
    // MAPEAMENTO DO RESULTADO DO BANCO PARA MODEL
    // =====================================================

    private final RowMapper<AvisoAcolhimento> mapper =
            (rs, rowNum) -> mapearAviso(rs);

    private static AvisoAcolhimento mapearAviso(ResultSet rs)
            throws SQLException {

        Timestamp dtAviso = rs.getTimestamp("DT_AVISO");
        Timestamp dtAcolhimento = rs.getTimestamp("DT_ACOLHIMENTO");

        Object funcionario = rs.getObject("CD_FUNCIONARIO");

        int tempoEstimado = rs.getInt("NR_TEMPO_ESTIMADO");

        Integer nrTempoEstimado = rs.wasNull()
                ? null
                : tempoEstimado;

        return AvisoAcolhimento.builder()
                .cdAviso(rs.getLong("CD_AVISO"))
                .cdUsuario(rs.getLong("CD_USUARIO"))
                .cdSessaoWeb(rs.getLong("CD_SESSAO_WEB"))
                .cdEstabelecimento(rs.getLong("CD_ESTABELECIMENTO"))
                .cdFuncionario(
                        funcionario != null
                                ? ((Number) funcionario).longValue()
                                : null
                )
                .dtAviso(
                        dtAviso != null
                                ? dtAviso.toLocalDateTime()
                                : null
                )
                .stAviso(rs.getString("ST_AVISO"))
                .nrTempoEstimado(nrTempoEstimado)
                .dtAcolhimento(
                        dtAcolhimento != null
                                ? dtAcolhimento.toLocalDateTime()
                                : null
                )
                .build();
    }

    // =====================================================
    // COLUNAS UTILIZADAS NAS CONSULTAS
    // =====================================================

    private static final String COLUNAS = """
            CD_AVISO,
            CD_USUARIO,
            CD_SESSAO_WEB,
            CD_ESTABELECIMENTO,
            CD_FUNCIONARIO,
            DT_AVISO,
            ST_AVISO,
            NR_TEMPO_ESTIMADO,
            DT_ACOLHIMENTO
            """;

    // =====================================================
    // CADASTRAR AVISO DE ACOLHIMENTO
    // =====================================================

    @Override
    public int criar(AvisoAcolhimento aviso) {

        String sql = """
                INSERT INTO T_ARI_AVISO_ACOLHIMENTO (
                    CD_USUARIO,
                    CD_SESSAO_WEB,
                    CD_ESTABELECIMENTO,
                    NR_TEMPO_ESTIMADO
                ) VALUES (?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        int linhasAfetadas = jdbcTemplate.update(connection -> {

            PreparedStatement ps = connection.prepareStatement(
                    sql,
                    new String[]{"CD_AVISO"}
            );

            ps.setLong(1, aviso.getCdUsuario());
            ps.setLong(2, aviso.getCdSessaoWeb());
            ps.setLong(3, aviso.getCdEstabelecimento());

            if (aviso.getNrTempoEstimado() != null) {
                ps.setInt(4, aviso.getNrTempoEstimado());
            } else {
                ps.setNull(4, Types.NUMERIC);
            }

            return ps;

        }, keyHolder);

        // RECUPERAR ID GERADO PELO ORACLE
        Number chaveGerada = keyHolder.getKey();

        if (chaveGerada != null) {
            aviso.setCdAviso(chaveGerada.longValue());
        }

        return linhasAfetadas;
    }

    // =====================================================
    // BUSCAR AVISO POR ID
    // =====================================================

    @Override
    public Optional<AvisoAcolhimento> buscarPorId(Long id) {

        String sql = """
                SELECT %s
                FROM T_ARI_AVISO_ACOLHIMENTO
                WHERE CD_AVISO = ?
                """.formatted(COLUNAS);

        return jdbcTemplate.query(sql, mapper, id)
                .stream()
                .findFirst();
    }

    // =====================================================
    // LISTAR TODOS OS AVISOS
    // =====================================================

    public List<AvisoAcolhimento> buscarTodos() {

        String sql = """
                SELECT %s
                FROM T_ARI_AVISO_ACOLHIMENTO
                ORDER BY DT_AVISO DESC
                """.formatted(COLUNAS);

        return jdbcTemplate.query(sql, mapper);
    }

    // =====================================================
    // BUSCAR AVISOS DE UMA USUÁRIA
    // Mantém o histórico de solicitações.
    // =====================================================

    public List<AvisoAcolhimento> buscarPorUsuario(Long cdUsuario) {

        String sql = """
                SELECT %s
                FROM T_ARI_AVISO_ACOLHIMENTO
                WHERE CD_USUARIO = ?
                ORDER BY DT_AVISO DESC
                """.formatted(COLUNAS);

        return jdbcTemplate.query(sql, mapper, cdUsuario);
    }

    // =====================================================
    // BUSCAR AVISOS DE UM ESTABELECIMENTO
    // =====================================================

    public List<AvisoAcolhimento> buscarPorEstabelecimento(
            Long cdEstabelecimento) {

        String sql = """
                SELECT %s
                FROM T_ARI_AVISO_ACOLHIMENTO
                WHERE CD_ESTABELECIMENTO = ?
                ORDER BY DT_AVISO DESC
                """.formatted(COLUNAS);

        return jdbcTemplate.query(
                sql,
                mapper,
                cdEstabelecimento
        );
    }

    // =====================================================
    // BUSCAR AVISOS PENDENTES DE UM ESTABELECIMENTO
    // Retorna somente avisos A_CAMINHO.
    // =====================================================

    public List<AvisoAcolhimento> buscarPendentesPorEstabelecimento(
            Long cdEstabelecimento) {

        String sql = """
                SELECT %s
                FROM T_ARI_AVISO_ACOLHIMENTO
                WHERE CD_ESTABELECIMENTO = ?
                  AND ST_AVISO = 'A_CAMINHO'
                ORDER BY DT_AVISO ASC
                """.formatted(COLUNAS);

        return jdbcTemplate.query(
                sql,
                mapper,
                cdEstabelecimento
        );
    }

    // =====================================================
    // BUSCAR AVISO ATIVO DE UMA USUÁRIA
    // Uma usuária pode ter somente um aviso A_CAMINHO.
    // =====================================================

    public Optional<AvisoAcolhimento> buscarAvisoAtivoPorUsuario(
            Long cdUsuario) {

        String sql = """
                SELECT %s
                FROM T_ARI_AVISO_ACOLHIMENTO
                WHERE CD_USUARIO = ?
                  AND ST_AVISO = 'A_CAMINHO'
                """.formatted(COLUNAS);

        return jdbcTemplate.query(sql, mapper, cdUsuario)
                .stream()
                .findFirst();
    }

    // =====================================================
    // ATUALIZAR AVISO
    // Altera status e dados da confirmação.
    // A regra de transição é validada no Service.
    // =====================================================

    @Override
    public int atualizar(AvisoAcolhimento aviso) {

        String sql = """
                UPDATE T_ARI_AVISO_ACOLHIMENTO
                SET
                    ST_AVISO = ?,
                    CD_FUNCIONARIO = ?,
                    DT_ACOLHIMENTO = ?
                WHERE CD_AVISO = ?
                  AND ST_AVISO = 'A_CAMINHO'
                """;

        Timestamp dtAcolhimento = aviso.getDtAcolhimento() != null
                ? Timestamp.valueOf(aviso.getDtAcolhimento())
                : null;

        return jdbcTemplate.update(
                sql,
                aviso.getStAviso(),
                aviso.getCdFuncionario(),
                dtAcolhimento,
                aviso.getCdAviso()
        );
    }

    // =====================================================
    // BUSCAR TODOS OS AVISOS EM ANDAMENTO
    // Utilizado pelo Service para verificar expiração.
    // =====================================================

    public List<AvisoAcolhimento> buscarAvisosEmAndamento() {

        String sql = """
                SELECT %s
                FROM T_ARI_AVISO_ACOLHIMENTO
                WHERE ST_AVISO = 'A_CAMINHO'
                ORDER BY DT_AVISO ASC
                """.formatted(COLUNAS);

        return jdbcTemplate.query(sql, mapper);
    }

    // =====================================================
    // EXPIRAR AVISO
    // Atualiza somente avisos que ainda estão A_CAMINHO.
    // O Service verifica se o prazo realmente venceu.
    // =====================================================

    public int expirar(Long cdAviso) {

        String sql = """
                UPDATE T_ARI_AVISO_ACOLHIMENTO
                SET ST_AVISO = 'EXPIRADO'
                WHERE CD_AVISO = ?
                  AND ST_AVISO = 'A_CAMINHO'
                """;

        return jdbcTemplate.update(sql, cdAviso);
    }

    // =====================================================
    // CONFIRMAR ACOLHIMENTO
    // Registra funcionário e data da confirmação.
    // =====================================================

    public int confirmar(Long cdAviso, Long cdFuncionario) {

        String sql = """
                UPDATE T_ARI_AVISO_ACOLHIMENTO
                SET
                    ST_AVISO = 'ACOLHIDA',
                    CD_FUNCIONARIO = ?,
                    DT_ACOLHIMENTO = SYSTIMESTAMP
                WHERE CD_AVISO = ?
                  AND ST_AVISO = 'A_CAMINHO'
                """;

        return jdbcTemplate.update(
                sql,
                cdFuncionario,
                cdAviso
        );
    }

    // =====================================================
    // CANCELAR AVISO
    // A usuária só pode cancelar o próprio aviso.
    // =====================================================

    public int cancelar(Long cdAviso, Long cdUsuario) {

        String sql = """
                UPDATE T_ARI_AVISO_ACOLHIMENTO
                SET
                    ST_AVISO = 'CANCELADO',
                    CD_FUNCIONARIO = NULL,
                    DT_ACOLHIMENTO = NULL
                WHERE CD_AVISO = ?
                  AND CD_USUARIO = ?
                  AND ST_AVISO = 'A_CAMINHO'
                """;

        return jdbcTemplate.update(
                sql,
                cdAviso,
                cdUsuario
        );
    }

    // =====================================================
    // CANCELAMENTO ADMINISTRATIVO
    // Preserva o histórico do aviso no banco.
    // A autorização deve ser validada pelo Service.
    // =====================================================

    @Override
    public int excluir(Long id) {

        String sql = """
                UPDATE T_ARI_AVISO_ACOLHIMENTO
                SET
                    ST_AVISO = 'CANCELADO',
                    CD_FUNCIONARIO = NULL,
                    DT_ACOLHIMENTO = NULL
                WHERE CD_AVISO = ?
                  AND ST_AVISO = 'A_CAMINHO'
                """;

        return jdbcTemplate.update(sql, id);
    }
}
