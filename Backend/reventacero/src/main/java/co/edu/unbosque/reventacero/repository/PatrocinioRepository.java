package co.edu.unbosque.reventacero.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.function.LongFunction;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.reventacero.entity.Festival;
import co.edu.unbosque.reventacero.entity.Patrocinador;
import co.edu.unbosque.reventacero.entity.Patrocinio;

/**
 * Acceso JDBC de solo lectura para Patrocinio. RF-08, RF-24; Fase 2, semana 11.
 * Las relaciones contienen sus claves; no se cargan recursivamente.
 */
@Repository
public class PatrocinioRepository {

    private static final String SELECT_PATROCINIO = 
            "SELECT p.ID_PATROCINIO, p.MONTO, p.TIPO_APORTE, p.ID_PATROCINADOR, p.ID_FESTIVAL"
             + " FROM PATROCINIO p ";

    private final JdbcClient jdbcClient;

    public PatrocinioRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<Patrocinio> findAll() {
        return jdbcClient.sql(SELECT_PATROCINIO + "ORDER BY p.ID_PATROCINIO")
                .query(this::mapRow)
                .list();
    }

    public Optional<Patrocinio> findById(long id) {
        return jdbcClient.sql(SELECT_PATROCINIO + "WHERE p.ID_PATROCINIO = :id")
                .param("id", id)
                .query(this::mapRow)
                .optional();
    }

    public boolean existsById(long id) {
        return jdbcClient.sql("SELECT COUNT(*) FROM PATROCINIO p WHERE p.ID_PATROCINIO = :id")
                .param("id", id)
                .query(Integer.class).single() > 0;
    }

    public long count() {
        return jdbcClient.sql("SELECT COUNT(*) FROM PATROCINIO p").query(Long.class).single();
    }

    public List<Patrocinio> findByFestivalId(long idFestival) {
        return jdbcClient.sql(SELECT_PATROCINIO + "WHERE p.ID_FESTIVAL = :idFestival" + " ORDER BY p.ID_PATROCINIO")
                .param("idFestival", idFestival)
                .query(this::mapRow)
                .list();
    }

    public List<Patrocinio> findByPatrocinadorId(long idPatrocinador) {
        return jdbcClient.sql(SELECT_PATROCINIO + "WHERE p.ID_PATROCINADOR = :idPatrocinador" + " ORDER BY p.ID_PATROCINIO")
                .param("idPatrocinador", idPatrocinador)
                .query(this::mapRow)
                .list();
    }

    private Patrocinio mapRow(ResultSet rs, int fila) throws SQLException {
        var patrocinador = referencia(rs, "ID_PATROCINADOR",
                id -> { var p = new Patrocinador(); p.setIdPatrocinador(id); return p; });
        return new Patrocinio(rs.getLong("ID_PATROCINIO"), rs.getBigDecimal("MONTO"),
                rs.getString("TIPO_APORTE"), patrocinador, festivalRef(rs, "ID_FESTIVAL"));
    }

    private static <T> T referencia(ResultSet rs, String columna, LongFunction<T> factory) throws SQLException {
        long id = rs.getLong(columna);
        return rs.wasNull() ? null : factory.apply(id);
    }

    private static Festival festivalRef(ResultSet rs, String columna) throws SQLException {
        return referencia(rs, columna, id -> { var e = new Festival(); e.setIdFestival(id); return e; });
    }
}
