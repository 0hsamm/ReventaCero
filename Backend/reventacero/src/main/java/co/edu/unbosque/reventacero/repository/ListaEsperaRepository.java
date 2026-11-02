package co.edu.unbosque.reventacero.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.function.LongFunction;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.reventacero.entity.ListaEspera;
import co.edu.unbosque.reventacero.entity.TipoEntrada;

/**
 * Acceso JDBC de solo lectura para ListaEspera. RF-16; Fase 2, semana 11.
 * Las relaciones contienen sus claves; no se cargan recursivamente.
 */
@Repository
public class ListaEsperaRepository {

    private static final String SELECT_LISTA_ESPERA = 
            "SELECT l.ID_LISTA, l.ID_TIPO_ENTRADA"
             + " FROM LISTA_ESPERA l ";

    private final JdbcClient jdbcClient;

    public ListaEsperaRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<ListaEspera> findAll() {
        return jdbcClient.sql(SELECT_LISTA_ESPERA + "ORDER BY l.ID_LISTA")
                .query(this::mapRow)
                .list();
    }

    public Optional<ListaEspera> findById(long id) {
        return jdbcClient.sql(SELECT_LISTA_ESPERA + "WHERE l.ID_LISTA = :id")
                .param("id", id)
                .query(this::mapRow)
                .optional();
    }

    public boolean existsById(long id) {
        return jdbcClient.sql("SELECT COUNT(*) FROM LISTA_ESPERA l WHERE l.ID_LISTA = :id")
                .param("id", id)
                .query(Integer.class).single() > 0;
    }

    public long count() {
        return jdbcClient.sql("SELECT COUNT(*) FROM LISTA_ESPERA l").query(Long.class).single();
    }

    public Optional<ListaEspera> findByTipoEntradaId(long idTipoEntrada) {
        return jdbcClient.sql(SELECT_LISTA_ESPERA + "WHERE l.ID_TIPO_ENTRADA = :idTipoEntrada")
                .param("idTipoEntrada", idTipoEntrada)
                .query(this::mapRow)
                .optional();
    }

    private ListaEspera mapRow(ResultSet rs, int fila) throws SQLException {
        return new ListaEspera(rs.getLong("ID_LISTA"), tipoEntradaRef(rs, "ID_TIPO_ENTRADA"));
    }

    private static TipoEntrada tipoEntradaRef(ResultSet rs, String columna) throws SQLException {
        return referencia(rs, columna, id -> { var e = new TipoEntrada(); e.setIdTipoEntrada(id); return e; });
    }

    private static <T> T referencia(ResultSet rs, String columna, LongFunction<T> factory) throws SQLException {
        long id = rs.getLong(columna);
        return rs.wasNull() ? null : factory.apply(id);
    }
    
    public long nextId() {
        return jdbcClient.sql("SELECT SEQ_LISTA_ESPERA.NEXTVAL FROM DUAL").query(Long.class).single();
    }

    public ListaEspera save(ListaEspera lista) {
        lista.setIdLista(nextId());
        jdbcClient.sql("INSERT INTO LISTA_ESPERA (ID_LISTA, ID_TIPO_ENTRADA) VALUES (:id, :idTipoEntrada)")
                .param("id", lista.getIdLista())
                .param("idTipoEntrada", lista.getTipoEntrada().getIdTipoEntrada())
                .update();
        return lista;
    }

    public void deleteById(long id) {
        jdbcClient.sql("DELETE FROM LISTA_ESPERA WHERE ID_LISTA = :id").param("id", id).update();
    }
}
