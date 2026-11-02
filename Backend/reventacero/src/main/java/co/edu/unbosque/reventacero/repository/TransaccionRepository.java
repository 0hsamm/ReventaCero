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
import co.edu.unbosque.reventacero.entity.Dispositivo;
import co.edu.unbosque.reventacero.entity.Transaccion;
import co.edu.unbosque.reventacero.enums.TipoTransaccion;

/**
 * Acceso JDBC de solo lectura para Transaccion. RF-14, RF-20; Fase 2, semana 11.
 * Las relaciones contienen sus claves; no se cargan recursivamente.
 */
@Repository
public class TransaccionRepository {

    private static final String SELECT_TRANSACCION = 
            "SELECT t.ID_TRANSACCION, t.FECHA_HORA, t.PRECIO, t.COMISION_ORGANIZADOR, t.METODO_PAGO, "
             + "t.CODIGO_BOLETA, t.ID_DISPOSITIVO, t.TIPO_TRANSACCION"
             + " FROM TRANSACCION t ";

    private final JdbcClient jdbcClient;

    public TransaccionRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<Transaccion> findAll() {
        return jdbcClient.sql(SELECT_TRANSACCION + "ORDER BY t.FECHA_HORA, t.ID_TRANSACCION")
                .query(this::mapRow)
                .list();
    }

    public Optional<Transaccion> findById(long id) {
        return jdbcClient.sql(SELECT_TRANSACCION + "WHERE t.ID_TRANSACCION = :id")
                .param("id", id)
                .query(this::mapRow)
                .optional();
    }

    public boolean existsById(long id) {
        return jdbcClient.sql("SELECT COUNT(*) FROM TRANSACCION t WHERE t.ID_TRANSACCION = :id")
                .param("id", id)
                .query(Integer.class).single() > 0;
    }

    public long count() {
        return jdbcClient.sql("SELECT COUNT(*) FROM TRANSACCION t").query(Long.class).single();
    }

    public List<Transaccion> findByBoletaCodigoBoleta(long codigoBoleta) {
        return jdbcClient.sql(SELECT_TRANSACCION + "WHERE t.CODIGO_BOLETA = :codigoBoleta" + " ORDER BY t.FECHA_HORA, t.ID_TRANSACCION")
                .param("codigoBoleta", codigoBoleta)
                .query(this::mapRow)
                .list();
    }

    public List<Transaccion> findByDispositivoId(long idDispositivo) {
        return jdbcClient.sql(SELECT_TRANSACCION + "WHERE t.ID_DISPOSITIVO = :idDispositivo" + " ORDER BY t.FECHA_HORA, t.ID_TRANSACCION")
                .param("idDispositivo", idDispositivo)
                .query(this::mapRow)
                .list();
    }

    private Transaccion mapRow(ResultSet rs, int fila) throws SQLException {
        return datosTransaccion(rs, new Transaccion());
    }

    private static <T extends Transaccion> T datosTransaccion(ResultSet rs, T e) throws SQLException {
        e.setIdTransaccion(rs.getLong("ID_TRANSACCION"));
        e.setFechaHora(fechaHora(rs, "FECHA_HORA"));
        e.setPrecio(rs.getBigDecimal("PRECIO"));
        e.setComisionOrganizador(rs.getBigDecimal("COMISION_ORGANIZADOR"));
        e.setMetodoPago(rs.getString("METODO_PAGO"));
        e.setBoleta(boletaRef(rs, "CODIGO_BOLETA"));
        e.setDispositivo(referencia(rs, "ID_DISPOSITIVO",
                id -> { var d = new Dispositivo(); d.setIdDispositivo(id); return d; }));
        String tipo = rs.getString("TIPO_TRANSACCION");
        e.setTipoTransaccion(tipo == null ? null : TipoTransaccion.valueOf(tipo));
        return e;
    }

    private static LocalDateTime fechaHora(ResultSet rs, String columna) throws SQLException {
        var valor = rs.getTimestamp(columna);
        return valor == null ? null : valor.toLocalDateTime();
    }

    private static <T> T referencia(ResultSet rs, String columna, LongFunction<T> factory) throws SQLException {
        long id = rs.getLong(columna);
        return rs.wasNull() ? null : factory.apply(id);
    }

    private static Boleta boletaRef(ResultSet rs, String columna) throws SQLException {
        return referencia(rs, columna, id -> { var e = new Boleta(); e.setCodigoBoleta(id); return e; });
    }
}
