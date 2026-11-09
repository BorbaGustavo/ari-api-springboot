package br.com.ari.dao;


import br.com.ari.model.Usuario;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public class UsuarioDAO  implements CrudDAO<Usuario, Long>  {

    private final JdbcTemplate jdbcTemplate;
    public UsuarioDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    public void testarUsuarios() {

        Integer total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM T_ARI_USUARIO",
                Integer.class
        );

        Integer usuarioId1 = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM T_ARI_USUARIO WHERE CD_USUARIO = ?",
                Integer.class,
                1L
        );

        System.out.println("Total de usuários no Java: " + total);
        System.out.println("Usuário com ID 1: " + usuarioId1);
    }

    @Override
    public int criar(Usuario usuario) {
        String sql= """
                INSERT INTO T_ARI_USUARIO(
                NM_USUARIO,
                NR_TELEFONE,
                DS_EMAIL,
                DS_SENHA_HASH,
                ST_USUARIO
                )
                VALUES(?, ?, ?, ?, ?)
                """;

        return jdbcTemplate.update(sql,
                usuario.getNmUsuario(),
                usuario.getNrTelefone(),
                usuario.getDsEmail(),
                usuario.getDsSenhaHash(),
                usuario.getStUsuario()
        );
    }

    @Override
    public Optional<Usuario> buscarPorId(Long cdUsuario) {

        String sql = """
        SELECT
            CD_USUARIO,
            NM_USUARIO,
            NR_TELEFONE,
            DS_EMAIL,
            DS_SENHA_HASH,
            ST_USUARIO,
            DT_CADASTRO
        FROM T_ARI_USUARIO
        WHERE CD_USUARIO = ?
        """;

        RowMapper<Usuario> mapper = (rs, rowNum) -> Usuario.builder()
                .cdUsuario(rs.getLong("CD_USUARIO"))
                .nmUsuario(rs.getString("NM_USUARIO"))
                .nrTelefone(rs.getString("NR_TELEFONE"))
                .dsEmail(rs.getString("DS_EMAIL"))
                .dsSenhaHash(rs.getString("DS_SENHA_HASH"))
                .stUsuario(rs.getString("ST_USUARIO"))
                .dtCadastro(rs.getTimestamp("DT_CADASTRO").toLocalDateTime())
                .build();

        return jdbcTemplate.query(sql, mapper, cdUsuario)
                .stream()
                .findFirst();
    }

    public List<Usuario> buscarTodos() {

        String sql = """
        SELECT
            CD_USUARIO,
            NM_USUARIO,
            NR_TELEFONE,
            DS_EMAIL,
            ST_USUARIO,
            DT_CADASTRO
        FROM T_ARI_USUARIO
        ORDER BY CD_USUARIO
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) ->
                Usuario.builder()
                        .cdUsuario(rs.getLong("CD_USUARIO"))
                        .nmUsuario(rs.getString("NM_USUARIO"))
                        .nrTelefone(rs.getString("NR_TELEFONE"))
                        .dsEmail(rs.getString("DS_EMAIL"))
                        .stUsuario(rs.getString("ST_USUARIO"))
                        .dtCadastro(rs.getTimestamp("DT_CADASTRO").toLocalDateTime())
                        .build()
        );
    }

    @Override
    public int atualizar(Usuario usuario) {

        String sql = """
        UPDATE T_ARI_USUARIO
        SET
            NM_USUARIO = ?,
            NR_TELEFONE = ?,
            DS_EMAIL = ?,
            ST_USUARIO = ?
        WHERE CD_USUARIO = ?
        """;

        return jdbcTemplate.update(
                sql,
                usuario.getNmUsuario(),
                usuario.getNrTelefone(),
                usuario.getDsEmail(),
                usuario.getStUsuario(),
                usuario.getCdUsuario()
        );
    }

    @Override
    public int excluir(Long id) {

        String sql = """
        DELETE FROM T_ARI_USUARIO
        WHERE CD_USUARIO = ?
        """;

        return jdbcTemplate.update(sql, id);
    }


}
