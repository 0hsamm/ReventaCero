package co.edu.unbosque.reventacero.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.reventacero.dto.SolicitudReventaDTO;

/** Acceso JDBC a las solicitudes de reventa, con los datos de la boleta que muestra el panel. */
@Repository
public class SolicitudReventaRepository {

    private static final String PENDIENTE = "PENDIENTE";

    private static final String SELECT_SOLICITUD =
            "SELECT s.ID_SOLICITUD, s.FECHA_HORA, s.PRECIO, s.METODO_PAGO, s.ESTADO, s.FECHA_RESPUESTA, "
            + "s.CODIGO_BOLETA, s.VENDEDOR_DOCUMENTO, s.COMPRADOR_DOCUMENTO, "
            + "u.NOMBRES || ' ' || u.APELLIDOS AS NOMBRE_VENDEDOR, f.NOMBRE AS NOMBRE_FESTIVAL, "
            + "te.CATEGORIA AS CATEGORIA_ENTRADA, z.NOMBRE AS NOMBRE_ZONA"
            + " FROM SOLICITUD_REVENTA s"
            + " JOIN USUARIO u ON u.DOCUMENTO = s.VENDEDOR_DOCUMENTO"
            + " JOIN BOLETA b ON b.CODIGO_BOLETA = s.CODIGO_BOLETA"
            + " JOIN TIPO_ENTRADA te ON te.ID_TIPO_ENTRADA = b.ID_TIPO_ENTRADA"
            + " JOIN ZONA z ON z.ID_ZONA = te.ID_ZONA"
            + " JOIN FESTIVAL f ON f.ID_FESTIVAL = z.ID_FESTIVAL ";

    private final JdbcClient jdbcClient;

    public SolicitudReventaRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public Optional<SolicitudReventaDTO> findById(long id) {
        return jdbcClient.sql(SELECT_SOLICITUD + "WHERE s.ID_SOLICITUD = :id")
                .param("id", id)
                .query(this::mapRow)
                .optional();
    }

    public List<SolicitudReventaDTO> findByComprador(long documento) {
        return jdbcClient.sql(SELECT_SOLICITUD + "WHERE s.COMPRADOR_DOCUMENTO = :documento ORDER BY s.FECHA_HORA DESC")
                .param("documento", documento)
                .query(this::mapRow)
                .list();
    }

    public List<SolicitudReventaDTO> findByVendedor(long documento) {
        return jdbcClient.sql(SELECT_SOLICITUD + "WHERE s.VENDEDOR_DOCUMENTO = :documento ORDER BY s.FECHA_HORA DESC")
                .param("documento", documento)
                .query(this::mapRow)
                .list();
    }

    public boolean existePendiente(long codigoBoleta) {
        return jdbcClient.sql("SELECT COUNT(*) FROM SOLICITUD_REVENTA WHERE CODIGO_BOLETA = :codigo AND ESTADO = :estado")
                .param("codigo", codigoBoleta)
                .param("estado", PENDIENTE)
                .query(Integer.class).single() > 0;
    }

    public long save(SolicitudReventaDTO solicitud) {
        long id = jdbcClient.sql("SELECT SEQ_SOLICITUD_REVENTA.NEXTVAL FROM DUAL").query(Long.class).single();
        jdbcClient.sql("INSERT INTO SOLICITUD_REVENTA (ID_SOLICITUD, FECHA_HORA, PRECIO, METODO_PAGO, ESTADO, "
                + "CODIGO_BOLETA, VENDEDOR_DOCUMENTO, COMPRADOR_DOCUMENTO) "
                + "VALUES (:id, :fechaHora, :precio, :metodoPago, :estado, :codigo, :vendedor, :comprador)")
                .param("id", id)
                .param("fechaHora", Timestamp.valueOf(solicitud.getFechaHora()))
                .param("precio", solicitud.getPrecio())
                .param("metodoPago", solicitud.getMetodoPago())
                .param("estado", PENDIENTE)
                .param("codigo", solicitud.getCodigoBoleta())
                .param("vendedor", solicitud.getVendedorDocumento())
                .param("comprador", solicitud.getCompradorDocumento())
                .update();
        return id;
    }

    /** Cierra la solicitud solo si sigue pendiente: devuelve 0 si otra operación ya la resolvió. */
    public int resolverSiPendiente(long id, String estado, LocalDateTime fecha) {
        return jdbcClient.sql("UPDATE SOLICITUD_REVENTA SET ESTADO = :estado, FECHA_RESPUESTA = :fecha "
                + "WHERE ID_SOLICITUD = :id AND ESTADO = :pendiente")
                .param("estado", estado)
                .param("fecha", Timestamp.valueOf(fecha))
                .param("id", id)
                .param("pendiente", PENDIENTE)
                .update();
    }

    public void cancelarPendientesDeBoleta(long codigoBoleta, String estado, LocalDateTime fecha) {
        jdbcClient.sql("UPDATE SOLICITUD_REVENTA SET ESTADO = :estado, FECHA_RESPUESTA = :fecha "
                + "WHERE CODIGO_BOLETA = :codigo AND ESTADO = :pendiente")
                .param("estado", estado)
                .param("fecha", Timestamp.valueOf(fecha))
                .param("codigo", codigoBoleta)
                .param("pendiente", PENDIENTE)
                .update();
    }

    private SolicitudReventaDTO mapRow(ResultSet rs, int fila) throws SQLException {
        var dto = new SolicitudReventaDTO();
        dto.setIdSolicitud(rs.getLong("ID_SOLICITUD"));
        dto.setFechaHora(fechaHora(rs, "FECHA_HORA"));
        dto.setPrecio(rs.getBigDecimal("PRECIO"));
        dto.setMetodoPago(rs.getString("METODO_PAGO"));
        dto.setEstado(rs.getString("ESTADO"));
        dto.setFechaRespuesta(fechaHora(rs, "FECHA_RESPUESTA"));
        dto.setCodigoBoleta(rs.getLong("CODIGO_BOLETA"));
        dto.setVendedorDocumento(rs.getLong("VENDEDOR_DOCUMENTO"));
        dto.setCompradorDocumento(rs.getLong("COMPRADOR_DOCUMENTO"));
        dto.setNombreVendedor(rs.getString("NOMBRE_VENDEDOR"));
        dto.setNombreFestival(rs.getString("NOMBRE_FESTIVAL"));
        dto.setCategoriaEntrada(rs.getString("CATEGORIA_ENTRADA"));
        dto.setNombreZona(rs.getString("NOMBRE_ZONA"));
        return dto;
    }

    private static LocalDateTime fechaHora(ResultSet rs, String columna) throws SQLException {
        var valor = rs.getTimestamp(columna);
        return valor == null ? null : valor.toLocalDateTime();
    }
}
