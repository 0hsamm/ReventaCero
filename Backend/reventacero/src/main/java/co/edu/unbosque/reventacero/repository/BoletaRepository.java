package co.edu.unbosque.reventacero.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.LongFunction;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

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
}
