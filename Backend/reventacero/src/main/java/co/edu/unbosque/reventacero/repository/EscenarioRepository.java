package co.edu.unbosque.reventacero.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.function.LongFunction;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.reventacero.entity.Escenario;
import co.edu.unbosque.reventacero.entity.Festival;

/**
 * Acceso JDBC de solo lectura para Escenario. RF-03, RF-18; Fase 2, semana 11.
 * Las relaciones contienen sus claves; no se cargan recursivamente.
 */
@Repository
public class EscenarioRepository {

    private static final String SELECT_ESCENARIO = 
            "SELECT e.ID_FESTIVAL, e.NOMBRE_ESCENARIO, e.CAPACIDAD"
             + " FROM ESCENARIO e ";

    private final JdbcClient jdbcClient;

    public EscenarioRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<Escenario> findAll() {
        return jdbcClient.sql(SELECT_ESCENARIO + "ORDER BY e.ID_FESTIVAL, e.NOMBRE_ESCENARIO")
                .query(this::mapRow)
                .list();
    }

    public Optional<Escenario> findById(long idFestival, String nombreEscenario) {
        return jdbcClient.sql(SELECT_ESCENARIO + "WHERE e.ID_FESTIVAL = :idFestival AND e.NOMBRE_ESCENARIO = :nombreEscenario")
                .param("idFestival", idFestival)
                .param("nombreEscenario", nombreEscenario)
                .query(this::mapRow)
                .optional();
    }

    public boolean existsById(long idFestival, String nombreEscenario) {
        return jdbcClient.sql("SELECT COUNT(*) FROM ESCENARIO e WHERE e.ID_FESTIVAL = :idFestival AND e.NOMBRE_ESCENARIO = :nombreEscenario")
                .param("idFestival", idFestival)
                .param("nombreEscenario", nombreEscenario)
                .query(Integer.class).single() > 0;
    }

    public long count() {
        return jdbcClient.sql("SELECT COUNT(*) FROM ESCENARIO e").query(Long.class).single();
    }

    public List<Escenario> findByFestivalId(long idFestival) {
        return jdbcClient.sql(SELECT_ESCENARIO + "WHERE e.ID_FESTIVAL = :idFestival" + " ORDER BY e.ID_FESTIVAL, e.NOMBRE_ESCENARIO")
                .param("idFestival", idFestival)
                .query(this::mapRow)
                .list();
    }

    private Escenario mapRow(ResultSet rs, int fila) throws SQLException {
        return new Escenario(festivalRef(rs, "ID_FESTIVAL"), rs.getString("NOMBRE_ESCENARIO"),
                rs.getInt("CAPACIDAD"));
    }

    private static Festival festivalRef(ResultSet rs, String columna) throws SQLException {
        return referencia(rs, columna, id -> { var e = new Festival(); e.setIdFestival(id); return e; });
    }

    private static <T> T referencia(ResultSet rs, String columna, LongFunction<T> factory) throws SQLException {
        long id = rs.getLong(columna);
        return rs.wasNull() ? null : factory.apply(id);
    }
}
