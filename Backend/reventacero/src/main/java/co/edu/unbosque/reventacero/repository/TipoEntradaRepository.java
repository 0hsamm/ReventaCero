package co.edu.unbosque.reventacero.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.function.LongFunction;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.reventacero.entity.TipoEntrada;
import co.edu.unbosque.reventacero.entity.Zona;

/**
 * Acceso JDBC de solo lectura para TipoEntrada. RF-09; Fase 2, semana 11.
 * Las relaciones contienen sus claves; no se cargan recursivamente.
 */
@Repository
public class TipoEntradaRepository {

    private static final String SELECT_TIPO_ENTRADA = 
            "SELECT te.ID_TIPO_ENTRADA, te.CATEGORIA, te.PRECIO_BASE, te.PRECIO_TOPE_REVENTA, te.ID_ZONA"
             + " FROM TIPO_ENTRADA te ";

    private final JdbcClient jdbcClient;

    public TipoEntradaRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<TipoEntrada> findAll() {
        return jdbcClient.sql(SELECT_TIPO_ENTRADA + "ORDER BY te.ID_TIPO_ENTRADA")
                .query(this::mapRow)
                .list();
    }

    public Optional<TipoEntrada> findById(long id) {
        return jdbcClient.sql(SELECT_TIPO_ENTRADA + "WHERE te.ID_TIPO_ENTRADA = :id")
                .param("id", id)
                .query(this::mapRow)
                .optional();
    }

    public boolean existsById(long id) {
        return jdbcClient.sql("SELECT COUNT(*) FROM TIPO_ENTRADA te WHERE te.ID_TIPO_ENTRADA = :id")
                .param("id", id)
                .query(Integer.class).single() > 0;
    }

    public long count() {
        return jdbcClient.sql("SELECT COUNT(*) FROM TIPO_ENTRADA te").query(Long.class).single();
    }

    public List<TipoEntrada> findByZonaId(long idZona) {
        return jdbcClient.sql(SELECT_TIPO_ENTRADA + "WHERE te.ID_ZONA = :idZona" + " ORDER BY te.ID_TIPO_ENTRADA")
                .param("idZona", idZona)
                .query(this::mapRow)
                .list();
    }

    private TipoEntrada mapRow(ResultSet rs, int fila) throws SQLException {
        var zona = referencia(rs, "ID_ZONA", id -> { var z = new Zona(); z.setIdZona(id); return z; });
        return new TipoEntrada(rs.getLong("ID_TIPO_ENTRADA"), rs.getString("CATEGORIA"),
                rs.getBigDecimal("PRECIO_BASE"), rs.getBigDecimal("PRECIO_TOPE_REVENTA"), zona);
    }

    private static <T> T referencia(ResultSet rs, String columna, LongFunction<T> factory) throws SQLException {
        long id = rs.getLong(columna);
        return rs.wasNull() ? null : factory.apply(id);
    }

    public long nextId() {
        return jdbcClient.sql("SELECT SEQ_TIPO_ENTRADA.NEXTVAL FROM DUAL").query(Long.class).single();
    }

    public TipoEntrada save(TipoEntrada tipoEntrada) {
        tipoEntrada.setIdTipoEntrada(nextId());
        jdbcClient.sql("INSERT INTO TIPO_ENTRADA (ID_TIPO_ENTRADA, CATEGORIA, PRECIO_BASE, PRECIO_TOPE_REVENTA, ID_ZONA) "
                + "VALUES (:id, :categoria, :precioBase, :precioTope, :idZona)")
                .param("id", tipoEntrada.getIdTipoEntrada())
                .param("categoria", tipoEntrada.getCategoria())
                .param("precioBase", tipoEntrada.getPrecioBase())
                .param("precioTope", tipoEntrada.getPrecioTopeReventa())
                .param("idZona", tipoEntrada.getZona().getIdZona())
                .update();
        return tipoEntrada;
    }

    public void update(TipoEntrada tipoEntrada) {
        jdbcClient.sql("UPDATE TIPO_ENTRADA SET CATEGORIA = :categoria, PRECIO_BASE = :precioBase, "
                + "PRECIO_TOPE_REVENTA = :precioTope, ID_ZONA = :idZona WHERE ID_TIPO_ENTRADA = :id")
                .param("id", tipoEntrada.getIdTipoEntrada())
                .param("categoria", tipoEntrada.getCategoria())
                .param("precioBase", tipoEntrada.getPrecioBase())
                .param("precioTope", tipoEntrada.getPrecioTopeReventa())
                .param("idZona", tipoEntrada.getZona().getIdZona())
                .update();
    }

    public void deleteById(long id) {
        jdbcClient.sql("DELETE FROM TIPO_ENTRADA WHERE ID_TIPO_ENTRADA = :id").param("id", id).update();
    }
}
