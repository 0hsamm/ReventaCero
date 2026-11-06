package co.edu.unbosque.reventacero.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.LongFunction;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.reventacero.dto.DatosQrBoletaDTO;
import co.edu.unbosque.reventacero.entity.Boleta;
import co.edu.unbosque.reventacero.entity.TipoEntrada;
import co.edu.unbosque.reventacero.entity.Usuario;

/**
 * Acceso JDBC de solo lectura para Boleta. RF-12, RF-20; Fase 2, semana 11.
 * Las relaciones contienen sus claves; no se cargan recursivamente.
 */
@Repository
public class BoletaRepository {

    private static final String SELECT_BOLETA = 
            "SELECT b.CODIGO_BOLETA, b.FECHA_EMISION, b.ID_TIPO_ENTRADA, b.COMPRADOR_DOCUMENTO"
             + " FROM BOLETA b ";

    private final JdbcClient jdbcClient;

    public BoletaRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<Boleta> findAll() {
        return jdbcClient.sql(SELECT_BOLETA + "ORDER BY b.CODIGO_BOLETA")
                .query(this::mapRow)
                .list();
    }

    public Optional<Boleta> findById(long id) {
        return jdbcClient.sql(SELECT_BOLETA + "WHERE b.CODIGO_BOLETA = :id")
                .param("id", id)
                .query(this::mapRow)
                .optional();
    }

    public boolean existsById(long id) {
        return jdbcClient.sql("SELECT COUNT(*) FROM BOLETA b WHERE b.CODIGO_BOLETA = :id")
                .param("id", id)
                .query(Integer.class).single() > 0;
    }

    public long count() {
        return jdbcClient.sql("SELECT COUNT(*) FROM BOLETA b").query(Long.class).single();
    }

    public List<Boleta> findByCompradorDocumento(long documento) {
        return jdbcClient.sql(SELECT_BOLETA + "WHERE b.COMPRADOR_DOCUMENTO = :documento" + " ORDER BY b.CODIGO_BOLETA")
                .param("documento", documento)
                .query(this::mapRow)
                .list();
    }

    public List<Boleta> findByTipoEntradaId(long idTipoEntrada) {
        return jdbcClient.sql(SELECT_BOLETA + "WHERE b.ID_TIPO_ENTRADA = :idTipoEntrada" + " ORDER BY b.CODIGO_BOLETA")
                .param("idTipoEntrada", idTipoEntrada)
                .query(this::mapRow)
                .list();
    }

    private Boleta mapRow(ResultSet rs, int fila) throws SQLException {
        return new Boleta(rs.getLong("CODIGO_BOLETA"), fecha(rs, "FECHA_EMISION"),
                tipoEntradaRef(rs, "ID_TIPO_ENTRADA"), usuarioRef(rs, "COMPRADOR_DOCUMENTO"));
    }

    private static LocalDate fecha(ResultSet rs, String columna) throws SQLException {
        var valor = rs.getDate(columna);
        return valor == null ? null : valor.toLocalDate();
    }

    private static Usuario usuarioRef(ResultSet rs, String columna) throws SQLException {
        return referencia(rs, columna, id -> { var e = new Usuario(); e.setDocumento(id); return e; });
    }

    private static TipoEntrada tipoEntradaRef(ResultSet rs, String columna) throws SQLException {
        return referencia(rs, columna, id -> { var e = new TipoEntrada(); e.setIdTipoEntrada(id); return e; });
    }

    private static <T> T referencia(ResultSet rs, String columna, LongFunction<T> factory) throws SQLException {
        long id = rs.getLong(columna);
        return rs.wasNull() ? null : factory.apply(id);
    }

    public long nextId() {
        return jdbcClient.sql("SELECT SEQ_BOLETA.NEXTVAL FROM DUAL").query(Long.class).single();
    }

    public Boleta save(Boleta boleta) {
        boleta.setCodigoBoleta(nextId());
        jdbcClient.sql("INSERT INTO BOLETA (CODIGO_BOLETA, FECHA_EMISION, ID_TIPO_ENTRADA, COMPRADOR_DOCUMENTO) "
                + "VALUES (:codigo, :fechaEmision, :idTipoEntrada, :comprador)")
                .param("codigo", boleta.getCodigoBoleta())
                .param("fechaEmision",
                        boleta.getFechaEmision() != null ? java.sql.Date.valueOf(boleta.getFechaEmision()) : null,
                        java.sql.Types.DATE)
                .param("idTipoEntrada", boleta.getTipoEntrada().getIdTipoEntrada())
                .param("comprador", boleta.getComprador().getDocumento())
                .update();
        return boleta;
    }

    public void actualizarComprador(long codigoBoleta, long documentoComprador) {
        jdbcClient.sql("UPDATE BOLETA SET COMPRADOR_DOCUMENTO = :comprador WHERE CODIGO_BOLETA = :codigo")
                .param("codigo", codigoBoleta)
                .param("comprador", documentoComprador)
                .update();
    }

    public Optional<DatosQrBoletaDTO> findDatosQr(long codigoBoleta) {
        return jdbcClient.sql(SELECT_DATOS_QR)
                .param("codigo", codigoBoleta)
                .query(this::mapDatosQr)
                .optional();
    }

    private static final String SELECT_DATOS_QR =
            "SELECT b.CODIGO_BOLETA, b.FECHA_EMISION, te.ID_TIPO_ENTRADA, te.CATEGORIA, z.NOMBRE AS ZONA_NOMBRE, "
            + "f.ID_FESTIVAL, f.NOMBRE AS FESTIVAL_NOMBRE, f.EDICION, f.FECHA_INICIO, f.FECHA_FIN, f.UBICACION_CIUDAD, "
            + "u.DOCUMENTO, u.NOMBRES, u.APELLIDOS, "
            + "(SELECT MAX(v.NOMBRES || ' ' || v.APELLIDOS) "
            + "        KEEP (DENSE_RANK LAST ORDER BY t.FECHA_HORA, t.ID_TRANSACCION) "
            + "   FROM REVENTA_AUTORIZADA r "
            + "   JOIN TRANSACCION t ON t.ID_TRANSACCION = r.ID_TRANSACCION "
            + "   JOIN USUARIO v ON v.DOCUMENTO = r.VENDEDOR_DOCUMENTO "
            + "  WHERE t.CODIGO_BOLETA = b.CODIGO_BOLETA "
            + "    AND r.COMPRADOR_DOCUMENTO = b.COMPRADOR_DOCUMENTO) AS REVENDEDOR, "
            + "(SELECT MAX(t2.ID_TRANSACCION) KEEP (DENSE_RANK LAST ORDER BY t2.FECHA_HORA, t2.ID_TRANSACCION) "
            + "   FROM TRANSACCION t2 "
            + "  WHERE t2.CODIGO_BOLETA = b.CODIGO_BOLETA "
            + "    AND t2.TIPO_TRANSACCION IN ('VENTA_PRIMARIA', 'REVENTA_AUTORIZADA')) AS ID_TRANSACCION, "
            + "CASE "
            + "  WHEN EXISTS (SELECT 1 FROM DEVOLUCION d JOIN TRANSACCION t3 ON t3.ID_TRANSACCION = d.ID_TRANSACCION "
            + "               WHERE t3.CODIGO_BOLETA = b.CODIGO_BOLETA) THEN 'DEVUELTA' "
            + "  WHEN EXISTS (SELECT 1 FROM CONTROL_ACCESO c WHERE c.CODIGO_BOLETA = b.CODIGO_BOLETA "
            + "               AND c.RESULTADO = 'PERMITIDO') THEN 'UTILIZADA' "
            + "  ELSE 'VIGENTE' END AS ESTADO "
            + "FROM BOLETA b "
            + "JOIN TIPO_ENTRADA te ON te.ID_TIPO_ENTRADA = b.ID_TIPO_ENTRADA "
            + "JOIN ZONA z ON z.ID_ZONA = te.ID_ZONA "
            + "JOIN FESTIVAL f ON f.ID_FESTIVAL = z.ID_FESTIVAL "
            + "JOIN USUARIO u ON u.DOCUMENTO = b.COMPRADOR_DOCUMENTO "
            + "WHERE b.CODIGO_BOLETA = :codigo";

    private DatosQrBoletaDTO mapDatosQr(ResultSet rs, int fila) throws SQLException {
        long idTransaccion = rs.getLong("ID_TRANSACCION");
        Long transaccion = rs.wasNull() ? null : idTransaccion;
        return new DatosQrBoletaDTO(
                rs.getLong("CODIGO_BOLETA"),
                fecha(rs, "FECHA_EMISION"),
                rs.getLong("ID_TIPO_ENTRADA"),
                rs.getString("CATEGORIA"),
                rs.getString("ZONA_NOMBRE"),
                rs.getLong("ID_FESTIVAL"),
                rs.getString("FESTIVAL_NOMBRE"),
                rs.getInt("EDICION"),
                fecha(rs, "FECHA_INICIO"),
                fecha(rs, "FECHA_FIN"),
                rs.getString("UBICACION_CIUDAD"),
                rs.getLong("DOCUMENTO"),
                nombreCompleto(rs.getString("NOMBRES"), rs.getString("APELLIDOS")),
                rs.getString("REVENDEDOR"),
                transaccion,
                rs.getString("ESTADO"));
    }

    private static String nombreCompleto(String nombres, String apellidos) {
        return ((nombres == null ? "" : nombres) + " " + (apellidos == null ? "" : apellidos)).trim();
    }

    public void bloquearPorCodigo(long codigoBoleta) {
        jdbcClient.sql("SELECT CODIGO_BOLETA FROM BOLETA WHERE CODIGO_BOLETA = :codigo FOR UPDATE")
                .param("codigo", codigoBoleta)
                .query(Long.class)
                .optional();
    }
}
