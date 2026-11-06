package co.edu.unbosque.reventacero.repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.reventacero.dto.PublicacionReventaDTO;

/** Boletas en venta: se guardan en BOLETA (EN_VENTA y PRECIO_VENTA), sin tabla propia. */
@Repository
public class PublicacionReventaRepository {

    private static final String SELECT_EN_VENTA =
            "SELECT b.CODIGO_BOLETA, b.PRECIO_VENTA, b.COMPRADOR_DOCUMENTO, "
            + "(SELECT MAX(t.ID_TRANSACCION) FROM TRANSACCION t WHERE t.CODIGO_BOLETA = b.CODIGO_BOLETA) AS REFERENCIA, "
            + "f.ID_FESTIVAL, f.NOMBRE AS NOMBRE_FESTIVAL, f.UBICACION_CIUDAD, f.FECHA_INICIO, f.FECHA_FIN, "
            + "te.CATEGORIA AS CATEGORIA_ENTRADA, z.NOMBRE AS NOMBRE_ZONA"
            + " FROM BOLETA b"
            + " JOIN TIPO_ENTRADA te ON te.ID_TIPO_ENTRADA = b.ID_TIPO_ENTRADA"
            + " JOIN ZONA z ON z.ID_ZONA = te.ID_ZONA"
            + " JOIN FESTIVAL f ON f.ID_FESTIVAL = z.ID_FESTIVAL"
            + " WHERE b.EN_VENTA = 1 ";

    private final JdbcClient jdbcClient;

    public PublicacionReventaRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    /** Lo que otra persona puede comprar: oculta lo propio, lo ya usado y los festivales terminados. */
    public List<PublicacionReventaDTO> findDisponiblesParaComprador(long documento) {
        return jdbcClient.sql(SELECT_EN_VENTA + "AND b.COMPRADOR_DOCUMENTO <> :documento"
                + " AND f.FECHA_FIN >= TRUNC(SYSDATE)"
                + " AND NOT EXISTS (SELECT 1 FROM CONTROL_ACCESO c WHERE c.CODIGO_BOLETA = b.CODIGO_BOLETA)"
                + " ORDER BY f.FECHA_INICIO, b.PRECIO_VENTA, b.CODIGO_BOLETA")
                .param("documento", documento)
                .query(this::mapRow)
                .list();
    }

    public List<PublicacionReventaDTO> findDelVendedor(long documento) {
        return jdbcClient.sql(SELECT_EN_VENTA + "AND b.COMPRADOR_DOCUMENTO = :documento ORDER BY b.CODIGO_BOLETA")
                .param("documento", documento)
                .query(this::mapRow)
                .list();
    }

    /** La boleta en venta a la que apunta una referencia, si esa referencia sigue siendo la vigente. */
    public Optional<PublicacionReventaDTO> findByReferencia(long referencia) {
        return jdbcClient.sql(SELECT_EN_VENTA
                + "AND b.CODIGO_BOLETA = (SELECT t.CODIGO_BOLETA FROM TRANSACCION t WHERE t.ID_TRANSACCION = :referencia)")
                .param("referencia", referencia)
                .query(this::mapRow)
                .optional()
                .filter(publicacion -> publicacion.getReferencia() == referencia);
    }

    /** Pone la boleta en venta solo si no lo estaba: devuelve 0 si ya estaba publicada. */
    public int publicar(long codigoBoleta, BigDecimal precio) {
        return jdbcClient.sql("UPDATE BOLETA SET EN_VENTA = 1, PRECIO_VENTA = :precio "
                + "WHERE CODIGO_BOLETA = :codigo AND EN_VENTA = 0")
                .param("precio", precio)
                .param("codigo", codigoBoleta)
                .update();
    }

    /** Quita la boleta de la venta: devuelve 0 si no estaba publicada (otra persona la compró, por ejemplo). */
    public int quitarDeVenta(long codigoBoleta) {
        return jdbcClient.sql("UPDATE BOLETA SET EN_VENTA = 0, PRECIO_VENTA = NULL "
                + "WHERE CODIGO_BOLETA = :codigo AND EN_VENTA = 1")
                .param("codigo", codigoBoleta)
                .update();
    }

    private PublicacionReventaDTO mapRow(ResultSet rs, int fila) throws SQLException {
        var dto = new PublicacionReventaDTO();
        dto.setReferencia(rs.getLong("REFERENCIA"));
        dto.setPrecio(rs.getBigDecimal("PRECIO_VENTA"));
        dto.setCodigoBoleta(rs.getLong("CODIGO_BOLETA"));
        dto.setVendedorDocumento(rs.getLong("COMPRADOR_DOCUMENTO"));
        dto.setIdFestival(rs.getLong("ID_FESTIVAL"));
        dto.setNombreFestival(rs.getString("NOMBRE_FESTIVAL"));
        dto.setCiudad(rs.getString("UBICACION_CIUDAD"));
        dto.setFechaInicio(fecha(rs, "FECHA_INICIO"));
        dto.setFechaFin(fecha(rs, "FECHA_FIN"));
        dto.setCategoriaEntrada(rs.getString("CATEGORIA_ENTRADA"));
        dto.setNombreZona(rs.getString("NOMBRE_ZONA"));
        return dto;
    }

    private static LocalDate fecha(ResultSet rs, String columna) throws SQLException {
        var valor = rs.getDate(columna);
        return valor == null ? null : valor.toLocalDate();
    }
}
