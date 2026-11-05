package co.edu.unbosque.reventacero.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.reventacero.dto.PublicacionReventaDTO;

/** Acceso JDBC a las boletas publicadas, con los datos del festival que muestra la página. */
@Repository
public class PublicacionReventaRepository {

    private static final String DISPONIBLE = "DISPONIBLE";

    private static final String SELECT_PUBLICACION =
            "SELECT p.ID_PUBLICACION, p.FECHA_PUBLICACION, p.PRECIO, p.ESTADO, p.FECHA_CIERRE, p.CODIGO_BOLETA, "
            + "p.VENDEDOR_DOCUMENTO, p.COMPRADOR_DOCUMENTO, f.ID_FESTIVAL, f.NOMBRE AS NOMBRE_FESTIVAL, "
            + "f.UBICACION_CIUDAD, f.FECHA_INICIO, f.FECHA_FIN, te.CATEGORIA AS CATEGORIA_ENTRADA, "
            + "z.NOMBRE AS NOMBRE_ZONA"
            + " FROM PUBLICACION_REVENTA p"
            + " JOIN BOLETA b ON b.CODIGO_BOLETA = p.CODIGO_BOLETA"
            + " JOIN TIPO_ENTRADA te ON te.ID_TIPO_ENTRADA = b.ID_TIPO_ENTRADA"
            + " JOIN ZONA z ON z.ID_ZONA = te.ID_ZONA"
            + " JOIN FESTIVAL f ON f.ID_FESTIVAL = z.ID_FESTIVAL ";

    private final JdbcClient jdbcClient;

    public PublicacionReventaRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public Optional<PublicacionReventaDTO> findById(long id) {
        return jdbcClient.sql(SELECT_PUBLICACION + "WHERE p.ID_PUBLICACION = :id")
                .param("id", id)
                .query(this::mapRow)
                .optional();
    }

    /** Lo que otra persona puede comprar: oculta lo propio, lo ya usado y los festivales terminados. */
    public List<PublicacionReventaDTO> findDisponiblesParaComprador(long documento) {
        return jdbcClient.sql(SELECT_PUBLICACION + "WHERE p.ESTADO = :estado AND p.VENDEDOR_DOCUMENTO <> :documento"
                + " AND f.FECHA_FIN >= TRUNC(SYSDATE)"
                + " AND NOT EXISTS (SELECT 1 FROM CONTROL_ACCESO c WHERE c.CODIGO_BOLETA = p.CODIGO_BOLETA)"
                + " ORDER BY f.FECHA_INICIO, p.PRECIO, p.ID_PUBLICACION")
                .param("estado", DISPONIBLE)
                .param("documento", documento)
                .query(this::mapRow)
                .list();
    }

    public List<PublicacionReventaDTO> findByVendedor(long documento) {
        return jdbcClient.sql(SELECT_PUBLICACION + "WHERE p.VENDEDOR_DOCUMENTO = :documento ORDER BY p.FECHA_PUBLICACION DESC")
                .param("documento", documento)
                .query(this::mapRow)
                .list();
    }

    public boolean existeDisponible(long codigoBoleta) {
        return jdbcClient.sql("SELECT COUNT(*) FROM PUBLICACION_REVENTA WHERE CODIGO_BOLETA = :codigo AND ESTADO = :estado")
                .param("codigo", codigoBoleta)
                .param("estado", DISPONIBLE)
                .query(Integer.class).single() > 0;
    }

    public long save(PublicacionReventaDTO publicacion) {
        long id = jdbcClient.sql("SELECT SEQ_PUBLICACION_REVENTA.NEXTVAL FROM DUAL").query(Long.class).single();
        jdbcClient.sql("INSERT INTO PUBLICACION_REVENTA (ID_PUBLICACION, FECHA_PUBLICACION, PRECIO, ESTADO, "
                + "CODIGO_BOLETA, VENDEDOR_DOCUMENTO) VALUES (:id, :fecha, :precio, :estado, :codigo, :vendedor)")
                .param("id", id)
                .param("fecha", Timestamp.valueOf(publicacion.getFechaPublicacion()))
                .param("precio", publicacion.getPrecio())
                .param("estado", DISPONIBLE)
                .param("codigo", publicacion.getCodigoBoleta())
                .param("vendedor", publicacion.getVendedorDocumento())
                .update();
        return id;
    }

    /** Vende solo si sigue disponible: devuelve 0 si otra persona la compró primero. */
    public int venderSiDisponible(long id, long comprador, LocalDateTime fecha) {
        return jdbcClient.sql("UPDATE PUBLICACION_REVENTA SET ESTADO = 'VENDIDA', COMPRADOR_DOCUMENTO = :comprador, "
                + "FECHA_CIERRE = :fecha WHERE ID_PUBLICACION = :id AND ESTADO = :estado")
                .param("comprador", comprador)
                .param("fecha", Timestamp.valueOf(fecha))
                .param("id", id)
                .param("estado", DISPONIBLE)
                .update();
    }

    public int retirarSiDisponible(long id, LocalDateTime fecha) {
        return jdbcClient.sql("UPDATE PUBLICACION_REVENTA SET ESTADO = 'RETIRADA', FECHA_CIERRE = :fecha "
                + "WHERE ID_PUBLICACION = :id AND ESTADO = :estado")
                .param("fecha", Timestamp.valueOf(fecha))
                .param("id", id)
                .param("estado", DISPONIBLE)
                .update();
    }

    public void retirarDisponiblesDeBoleta(long codigoBoleta, LocalDateTime fecha) {
        jdbcClient.sql("UPDATE PUBLICACION_REVENTA SET ESTADO = 'RETIRADA', FECHA_CIERRE = :fecha "
                + "WHERE CODIGO_BOLETA = :codigo AND ESTADO = :estado")
                .param("fecha", Timestamp.valueOf(fecha))
                .param("codigo", codigoBoleta)
                .param("estado", DISPONIBLE)
                .update();
    }

    private PublicacionReventaDTO mapRow(ResultSet rs, int fila) throws SQLException {
        var dto = new PublicacionReventaDTO();
        dto.setIdPublicacion(rs.getLong("ID_PUBLICACION"));
        dto.setFechaPublicacion(fechaHora(rs, "FECHA_PUBLICACION"));
        dto.setPrecio(rs.getBigDecimal("PRECIO"));
        dto.setEstado(rs.getString("ESTADO"));
        dto.setFechaCierre(fechaHora(rs, "FECHA_CIERRE"));
        dto.setCodigoBoleta(rs.getLong("CODIGO_BOLETA"));
        dto.setVendedorDocumento(rs.getLong("VENDEDOR_DOCUMENTO"));
        dto.setCompradorDocumento(rs.getLong("COMPRADOR_DOCUMENTO"));
        dto.setIdFestival(rs.getLong("ID_FESTIVAL"));
        dto.setNombreFestival(rs.getString("NOMBRE_FESTIVAL"));
        dto.setCiudad(rs.getString("UBICACION_CIUDAD"));
        dto.setFechaInicio(fecha(rs, "FECHA_INICIO"));
        dto.setFechaFin(fecha(rs, "FECHA_FIN"));
        dto.setCategoriaEntrada(rs.getString("CATEGORIA_ENTRADA"));
        dto.setNombreZona(rs.getString("NOMBRE_ZONA"));
        return dto;
    }

    private static LocalDateTime fechaHora(ResultSet rs, String columna) throws SQLException {
        var valor = rs.getTimestamp(columna);
        return valor == null ? null : valor.toLocalDateTime();
    }

    private static LocalDate fecha(ResultSet rs, String columna) throws SQLException {
        var valor = rs.getDate(columna);
        return valor == null ? null : valor.toLocalDate();
    }
}
