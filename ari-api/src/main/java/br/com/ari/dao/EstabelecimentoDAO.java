
package br.com.ari.dao;

import br.com.ari.model.Estabelecimento;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class EstabelecimentoDAO implements CrudDAO<Estabelecimento, Long> {

    private final JdbcTemplate jdbcTemplate;

    public EstabelecimentoDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // MAPEAMENTO DO RESULTADO DO BANCO PARA MODEL
    private final RowMapper<Estabelecimento> mapper =
            (rs, rowNum) -> Estabelecimento.builder()

                    .cdEstabelecimento(
                            rs.getLong("CD_ESTABELECIMENTO")
                    )

                    .nrCnpj(rs.getString("NR_CNPJ"))
                    .nmRazaoSocial(rs.getString("NM_RAZAO_SOCIAL"))
                    .nmFantasia(rs.getString("NM_FANTASIA"))

                    .dsEmail(rs.getString("DS_EMAIL"))
                    .nrTelefone(rs.getString("NR_TELEFONE"))
                    .dsTipoEstabelecimento(
                            rs.getString("DS_TIPO_ESTABELECIMENTO")
                    )

                    .nrCep(rs.getString("NR_CEP"))
                    .dsLogradouro(rs.getString("DS_LOGRADOURO"))
                    .nrEndereco(rs.getString("NR_ENDERECO"))
                    .dsComplemento(rs.getString("DS_COMPLEMENTO"))
                    .nmBairro(rs.getString("NM_BAIRRO"))
                    .nmCidade(rs.getString("NM_CIDADE"))
                    .sgUf(rs.getString("SG_UF"))

                    .nrLatitude(rs.getBigDecimal("NR_LATITUDE"))
                    .nrLongitude(rs.getBigDecimal("NR_LONGITUDE"))

                    .flAcolhimentoAtivo(
                            rs.getString("FL_ACOLHIMENTO_ATIVO")
                    )

                    .dtSuspensaoAte(
                            timestampParaLocalDateTime(
                                    rs.getTimestamp("DT_SUSPENSAO_ATE")
                            )
                    )

                    .stEstabelecimento(
                            rs.getString("ST_ESTABELECIMENTO")
                    )

                    .dtCadastro(
                            timestampParaLocalDateTime(
                                    rs.getTimestamp("DT_CADASTRO")
                            )
                    )

                    .build();

    // CONVERSÃO DE TIMESTAMP PARA LOCALDATETIME
    private static LocalDateTime timestampParaLocalDateTime(
            Timestamp timestamp) {

        return timestamp != null
                ? timestamp.toLocalDateTime()
                : null;
    }

    // CONVERSÃO DE LOCALDATETIME PARA TIMESTAMP
    private static Timestamp localDateTimeParaTimestamp(
            LocalDateTime data) {

        return data != null
                ? Timestamp.valueOf(data)
                : null;
    }

    // COLUNAS UTILIZADAS NAS CONSULTAS
    private static final String COLUNAS = """
            CD_ESTABELECIMENTO,
            NR_CNPJ,
            NM_RAZAO_SOCIAL,
            NM_FANTASIA,
            DS_EMAIL,
            NR_TELEFONE,
            DS_TIPO_ESTABELECIMENTO,
            NR_CEP,
            DS_LOGRADOURO,
            NR_ENDERECO,
            DS_COMPLEMENTO,
            NM_BAIRRO,
            NM_CIDADE,
            SG_UF,
            NR_LATITUDE,
            NR_LONGITUDE,
            FL_ACOLHIMENTO_ATIVO,
            DT_SUSPENSAO_ATE,
            ST_ESTABELECIMENTO,
            DT_CADASTRO
            """;

    // CADASTRAR ESTABELECIMENTO
    @Override
    public int criar(Estabelecimento estabelecimento) {

        String sql = """
                INSERT INTO T_ARI_ESTABELECIMENTO (
                    NR_CNPJ,
                    NM_RAZAO_SOCIAL,
                    NM_FANTASIA,
                    DS_EMAIL,
                    NR_TELEFONE,
                    DS_TIPO_ESTABELECIMENTO,
                    NR_CEP,
                    DS_LOGRADOURO,
                    NR_ENDERECO,
                    DS_COMPLEMENTO,
                    NM_BAIRRO,
                    NM_CIDADE,
                    SG_UF,
                    NR_LATITUDE,
                    NR_LONGITUDE
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        return jdbcTemplate.update(
                sql,
                estabelecimento.getNrCnpj(),
                estabelecimento.getNmRazaoSocial(),
                estabelecimento.getNmFantasia(),
                estabelecimento.getDsEmail(),
                estabelecimento.getNrTelefone(),
                estabelecimento.getDsTipoEstabelecimento(),
                estabelecimento.getNrCep(),
                estabelecimento.getDsLogradouro(),
                estabelecimento.getNrEndereco(),
                estabelecimento.getDsComplemento(),
                estabelecimento.getNmBairro(),
                estabelecimento.getNmCidade(),
                estabelecimento.getSgUf(),
                estabelecimento.getNrLatitude(),
                estabelecimento.getNrLongitude()
        );
    }

    // BUSCAR ESTABELECIMENTO POR ID
    @Override
    public Optional<Estabelecimento> buscarPorId(Long id) {

        String sql = """
                SELECT %s
                FROM T_ARI_ESTABELECIMENTO
                WHERE CD_ESTABELECIMENTO = ?
                """.formatted(COLUNAS);

        return jdbcTemplate.query(sql, mapper, id)
                .stream()
                .findFirst();
    }

    // LISTAR TODOS OS ESTABELECIMENTOS
    public List<Estabelecimento> buscarTodos() {

        String sql = """
                SELECT %s
                FROM T_ARI_ESTABELECIMENTO
                ORDER BY CD_ESTABELECIMENTO
                """.formatted(COLUNAS);

        return jdbcTemplate.query(sql, mapper);
    }

    // BUSCAR ESTABELECIMENTO POR CNPJ
    public Optional<Estabelecimento> buscarPorCnpj(String cnpj) {

        String sql = """
                SELECT %s
                FROM T_ARI_ESTABELECIMENTO
                WHERE NR_CNPJ = ?
                """.formatted(COLUNAS);

        return jdbcTemplate.query(sql, mapper, cnpj)
                .stream()
                .findFirst();
    }

    // ATUALIZAR ESTABELECIMENTO
    @Override
    public int atualizar(Estabelecimento estabelecimento) {

        String sql = """
                UPDATE T_ARI_ESTABELECIMENTO
                SET
                    NM_RAZAO_SOCIAL = ?,
                    NM_FANTASIA = ?,
                    DS_EMAIL = ?,
                    NR_TELEFONE = ?,
                    DS_TIPO_ESTABELECIMENTO = ?,
                    NR_CEP = ?,
                    DS_LOGRADOURO = ?,
                    NR_ENDERECO = ?,
                    DS_COMPLEMENTO = ?,
                    NM_BAIRRO = ?,
                    NM_CIDADE = ?,
                    SG_UF = ?,
                    NR_LATITUDE = ?,
                    NR_LONGITUDE = ?,
                    FL_ACOLHIMENTO_ATIVO = ?,
                    DT_SUSPENSAO_ATE = ?,
                    ST_ESTABELECIMENTO = ?
                WHERE CD_ESTABELECIMENTO = ?
                """;

        return jdbcTemplate.update(
                sql,
                estabelecimento.getNmRazaoSocial(),
                estabelecimento.getNmFantasia(),
                estabelecimento.getDsEmail(),
                estabelecimento.getNrTelefone(),
                estabelecimento.getDsTipoEstabelecimento(),
                estabelecimento.getNrCep(),
                estabelecimento.getDsLogradouro(),
                estabelecimento.getNrEndereco(),
                estabelecimento.getDsComplemento(),
                estabelecimento.getNmBairro(),
                estabelecimento.getNmCidade(),
                estabelecimento.getSgUf(),
                estabelecimento.getNrLatitude(),
                estabelecimento.getNrLongitude(),
                estabelecimento.getFlAcolhimentoAtivo(),
                localDateTimeParaTimestamp(
                        estabelecimento.getDtSuspensaoAte()
                ),
                estabelecimento.getStEstabelecimento(),
                estabelecimento.getCdEstabelecimento()
        );
    }

    // DESATIVAR RECEBIMENTO DE ACOLHIMENTOS
    // Preserva o cadastro e o histórico do estabelecimento.
    @Override
    public int excluir(Long id) {

        String sql = """
                UPDATE T_ARI_ESTABELECIMENTO
                SET FL_ACOLHIMENTO_ATIVO = 'N'
                WHERE CD_ESTABELECIMENTO = ?
                """;

        return jdbcTemplate.update(sql, id);
    }

    public int excluirFisico(Long id) {
        return jdbcTemplate.update(
                "DELETE FROM T_ARI_ESTABELECIMENTO WHERE CD_ESTABELECIMENTO = ?",
                id
        );
    }

    // BUSCAR ESTABELECIMENTOS POR CEP
    public List<Estabelecimento> buscarPorCep(String cep) {

        String sql = """
                SELECT %s
                FROM T_ARI_ESTABELECIMENTO
                WHERE NR_CEP = ?
                  AND ST_ESTABELECIMENTO = 'ATIVO'
                  AND FL_ACOLHIMENTO_ATIVO = 'S'
                  AND (
                      DT_SUSPENSAO_ATE IS NULL
                      OR DT_SUSPENSAO_ATE <= SYSTIMESTAMP
                  )
                ORDER BY NM_FANTASIA
                """.formatted(COLUNAS);

        return jdbcTemplate.query(sql, mapper, cep);
    }

    // BUSCAR ESTABELECIMENTOS PRÓXIMOS
    // Ordenação aproximada baseada nas coordenadas geográficas.
    public List<Estabelecimento> buscarProximos(
            Double latitude,
            Double longitude) {

        String sql = """
                SELECT %s
                FROM T_ARI_ESTABELECIMENTO
                WHERE ST_ESTABELECIMENTO = 'ATIVO'
                  AND FL_ACOLHIMENTO_ATIVO = 'S'
                  AND (
                      DT_SUSPENSAO_ATE IS NULL
                      OR DT_SUSPENSAO_ATE <= SYSTIMESTAMP
                  )
                  AND NR_LATITUDE IS NOT NULL
                  AND NR_LONGITUDE IS NOT NULL
                ORDER BY
                    POWER(NR_LATITUDE - ?, 2) +
                    POWER(NR_LONGITUDE - ?, 2)
                """.formatted(COLUNAS);

        return jdbcTemplate.query(
                sql,
                mapper,
                latitude,
                longitude
        );
    }

}
