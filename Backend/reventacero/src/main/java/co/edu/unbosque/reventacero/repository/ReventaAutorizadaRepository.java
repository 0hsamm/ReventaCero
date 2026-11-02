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
import co.edu.unbosque.reventacero.entity.ReventaAutorizada;
import co.edu.unbosque.reventacero.entity.Transaccion;
import co.edu.unbosque.reventacero.entity.Usuario;
import co.edu.unbosque.reventacero.enums.TipoTransaccion;

/**
 * Acceso JDBC de solo lectura para ReventaAutorizada. RF-14, RF-21, RF-23; Fase 2, semana 11.
 * Las relaciones contienen sus claves; no se cargan recursivamente.
 */
@Repository
public class ReventaAutorizadaRepository {

    private static final String SELECT_REVENTA_AUTORIZADA = 
            "SELECT t.ID_TRANSACCION, t.FECHA_HORA, t.PRECIO, t.COMISION_ORGANIZADOR, t.METODO_PAGO, "
             + "t.CODIGO_BOLETA, t.ID_DISPOSITIVO, t.TIPO_TRANSACCION, r.VENDEDOR_DOCUMENTO, r.COMPRADOR_DOCUMENTO"
             + " FROM REVENTA_AUTORIZADA r JOIN TRANSACCION t ON t.ID_TRANSACCION = r.ID_TRANSACCION ";

    private final JdbcClient jdbcClient;

    public ReventaAutorizadaRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<ReventaAutorizada> findAll() {
        return jdbcClient.sql(SELECT_REVENTA_AUTORIZADA + "ORDER BY t.FECHA_HORA, t.ID_TRANSACCION")
                .query(this::mapRow)
                .list();
    }

    public Optional<ReventaAutorizada> findById(long id) {
        return jdbcClient.sql(SELECT_REVENTA_AUTORIZADA + "WHERE t.ID_TRANSACCION = :id")
                .param("id", id)
                .query(this::mapRow)
                .optional();
    }

    public boolean existsById(long id) {
        return jdbcClient.sql("SELECT COUNT(*) FROM REVENTA_AUTORIZADA r JOIN TRANSACCION t ON t.ID_TRANSACCION = r.ID_TRANSACCION WHERE t.ID_TRANSACCION = :id")
                .param("id", id)
                .query(Integer.class).single() > 0;
    }

    public long count() {
        return jdbcClient.sql("SELECT COUNT(*) FROM REVENTA_AUTORIZADA r JOIN TRANSACCION t ON t.ID_TRANSACCION = r.ID_TRANSACCION").query(Long.class).single();
    }

    public List<ReventaAutorizada> findByBoletaCodigoBoleta(long codigoBoleta) {
        return jdbcClient.sql(SELECT_REVENTA_AUTORIZADA + "WHERE t.CODIGO_BOLETA = :codigoBoleta" + " ORDER BY t.FECHA_HORA, t.ID_TRANSACCION")
                .param("codigoBoleta", codigoBoleta)
                .query(this::mapRow)
                .list();
    }

    public List<ReventaAutorizada> findByVendedorDocumento(long documento) {
        return jdbcClient.sql(SELECT_REVENTA_AUTORIZADA + "WHERE r.VENDEDOR_DOCUMENTO = :documento" + " ORDER BY t.FECHA_HORA, t.ID_TRANSACCION")
                .param("documento", documento)
                .query(this::mapRow)
                .list();
    }

    public List<ReventaAutorizada> findByCompradorDocumento(long documento) {
        return jdbcClient.sql(SELECT_REVENTA_AUTORIZADA + "WHERE r.COMPRADOR_DOCUMENTO = :documento" + " ORDER BY t.FECHA_HORA, t.ID_TRANSACCION")
                .param("documento", documento)
                .query(this::mapRow)
                .list();
    }

    public List<ReventaAutorizada> findByFestivalId(long idFestival) {
        return jdbcClient.sql(SELECT_REVENTA_AUTORIZADA + "WHERE t.CODIGO_BOLETA IN (SELECT b.CODIGO_BOLETA FROM BOLETA b JOIN TIPO_ENTRADA te ON te.ID_TIPO_ENTRADA = b.ID_TIPO_ENTRADA JOIN ZONA z ON z.ID_ZONA = te.ID_ZONA WHERE z.ID_FESTIVAL = :idFestival)" + " ORDER BY t.FECHA_HORA, t.ID_TRANSACCION")
                .param("idFestival", idFestival)
                .query(this::mapRow)
                .list();
    }

    private ReventaAutorizada mapRow(ResultSet rs, int fila) throws SQLException {
        var e = datosTransaccion(rs, new ReventaAutorizada());
        e.setVendedor(usuarioRef(rs, "VENDEDOR_DOCUMENTO"));
        e.setComprador(usuarioRef(rs, "COMPRADOR_DOCUMENTO"));
        return e;
    }

    private static Usuario usuarioRef(ResultSet rs, String columna) throws SQLException {
        return referencia(rs, columna, id -> { var e = new Usuario(); e.setDocumento(id); return e; });
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

    public void save(ReventaAutorizada reventa) {
        jdbcClient.sql("INSERT INTO REVENTA_AUTORIZADA (ID_TRANSACCION, VENDEDOR_DOCUMENTO, COMPRADOR_DOCUMENTO) "
                + "VALUES (:id, :vendedor, :comprador)")
                .param("id", reventa.getIdTransaccion())
                .param("vendedor", reventa.getVendedor().getDocumento())
                .param("comprador", reventa.getComprador().getDocumento())
                .update();
    }
}
