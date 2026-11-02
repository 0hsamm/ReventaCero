package co.edu.unbosque.reventacero.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.LongFunction;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.reventacero.entity.Boleta;
import co.edu.unbosque.reventacero.entity.ControlAcceso;

/**
 * Acceso JDBC de solo lectura para ControlAcceso. RF-17, RF-20; Fase 2, semana 11.
 * Las relaciones contienen sus claves; no se cargan recursivamente.
 */
@Repository
public class ControlAccesoRepository {

    private static final String SELECT_CONTROL_ACCESO = 
            "SELECT c.ID_ACCESO, c.FECHA_HORA_INGRESO, c.RESULTADO, c.CODIGO_BOLETA"
             + " FROM CONTROL_ACCESO c ";

    private final JdbcClient jdbcClient;

    public ControlAccesoRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<ControlAcceso> findAll() {
        return jdbcClient.sql(SELECT_CONTROL_ACCESO + "ORDER BY c.ID_ACCESO")
                .query(this::mapRow)
                .list();
    }

    public Optional<ControlAcceso> findById(long id) {
        return jdbcClient.sql(SELECT_CONTROL_ACCESO + "WHERE c.ID_ACCESO = :id")
                .param("id", id)
                .query(this::mapRow)
                .optional();
    }

    public boolean existsById(long id) {
        return jdbcClient.sql("SELECT COUNT(*) FROM CONTROL_ACCESO c WHERE c.ID_ACCESO = :id")
                .param("id", id)
                .query(Integer.class).single() > 0;
    }

    public long count() {
        return jdbcClient.sql("SELECT COUNT(*) FROM CONTROL_ACCESO c").query(Long.class).single();
    }

    public Optional<ControlAcceso> findByBoletaCodigoBoleta(long codigoBoleta) {
        return jdbcClient.sql(SELECT_CONTROL_ACCESO + "WHERE c.CODIGO_BOLETA = :codigoBoleta")
                .param("codigoBoleta", codigoBoleta)
                .query(this::mapRow)
                .optional();
    }

    private ControlAcceso mapRow(ResultSet rs, int fila) throws SQLException {
        return new ControlAcceso(rs.getLong("ID_ACCESO"), fechaHora(rs, "FECHA_HORA_INGRESO"),
                rs.getString("RESULTADO"), boletaRef(rs, "CODIGO_BOLETA"));
    }

    private static LocalDateTime fechaHora(ResultSet rs, String columna) throws SQLException {
        var valor = rs.getTimestamp(columna);
        return valor == null ? null : valor.toLocalDateTime();
    }

    private static Boleta boletaRef(ResultSet rs, String columna) throws SQLException {
        return referencia(rs, columna, id -> { var e = new Boleta(); e.setCodigoBoleta(id); return e; });
    }

    private static <T> T referencia(ResultSet rs, String columna, LongFunction<T> factory) throws SQLException {
        long id = rs.getLong(columna);
        return rs.wasNull() ? null : factory.apply(id);
    }
}
