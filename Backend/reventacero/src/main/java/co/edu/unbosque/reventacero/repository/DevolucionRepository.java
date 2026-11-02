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
import co.edu.unbosque.reventacero.entity.Devolucion;
import co.edu.unbosque.reventacero.entity.Dispositivo;
import co.edu.unbosque.reventacero.entity.Transaccion;
import co.edu.unbosque.reventacero.enums.TipoTransaccion;

/**
 * Acceso JDBC de solo lectura para Devolucion. RF-14, RF-20; Fase 2, semana 11.
 * Las relaciones contienen sus claves; no se cargan recursivamente.
 */
@Repository
public class DevolucionRepository {

    private static final String SELECT_DEVOLUCION = 
            "SELECT t.ID_TRANSACCION, t.FECHA_HORA, t.PRECIO, t.COMISION_ORGANIZADOR, t.METODO_PAGO, "
             + "t.CODIGO_BOLETA, t.ID_DISPOSITIVO, t.TIPO_TRANSACCION, d.MOTIVO, d.PORCENTAJE_REEMBOLSO"
             + " FROM DEVOLUCION d JOIN TRANSACCION t ON t.ID_TRANSACCION = d.ID_TRANSACCION ";

    private final JdbcClient jdbcClient;

    public DevolucionRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<Devolucion> findAll() {
        return jdbcClient.sql(SELECT_DEVOLUCION + "ORDER BY t.FECHA_HORA, t.ID_TRANSACCION")
                .query(this::mapRow)
                .list();
    }

    public Optional<Devolucion> findById(long id) {
        return jdbcClient.sql(SELECT_DEVOLUCION + "WHERE t.ID_TRANSACCION = :id")
                .param("id", id)
                .query(this::mapRow)
                .optional();
    }

    public boolean existsById(long id) {
        return jdbcClient.sql("SELECT COUNT(*) FROM DEVOLUCION d JOIN TRANSACCION t ON t.ID_TRANSACCION = d.ID_TRANSACCION WHERE t.ID_TRANSACCION = :id")
                .param("id", id)
                .query(Integer.class).single() > 0;
    }

    public long count() {
        return jdbcClient.sql("SELECT COUNT(*) FROM DEVOLUCION d JOIN TRANSACCION t ON t.ID_TRANSACCION = d.ID_TRANSACCION").query(Long.class).single();
    }

    public List<Devolucion> findByBoletaCodigoBoleta(long codigoBoleta) {
        return jdbcClient.sql(SELECT_DEVOLUCION + "WHERE t.CODIGO_BOLETA = :codigoBoleta" + " ORDER BY t.FECHA_HORA, t.ID_TRANSACCION")
                .param("codigoBoleta", codigoBoleta)
                .query(this::mapRow)
                .list();
    }

    private Devolucion mapRow(ResultSet rs, int fila) throws SQLException {
        var e = datosTransaccion(rs, new Devolucion());
        e.setMotivo(rs.getString("MOTIVO"));
        e.setPorcentajeReembolso(rs.getBigDecimal("PORCENTAJE_REEMBOLSO"));
        return e;
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
    
    public void save(Devolucion devolucion) {
        jdbcClient.sql("INSERT INTO DEVOLUCION (ID_TRANSACCION, MOTIVO, PORCENTAJE_REEMBOLSO) "
                + "VALUES (:id, :motivo, :porcentaje)")
                .param("id", devolucion.getIdTransaccion())
                .param("motivo", devolucion.getMotivo(), java.sql.Types.VARCHAR)
                .param("porcentaje", devolucion.getPorcentajeReembolso(), java.sql.Types.NUMERIC)
                .update();
    }
}
