package co.edu.unbosque.reventacero.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.function.LongFunction;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.reventacero.entity.Genero;

/**
 * Acceso JDBC de solo lectura para Genero. RF-04; Fase 2, semana 11.
 * Las relaciones contienen sus claves; no se cargan recursivamente.
 */
@Repository
public class GeneroRepository {

    private static final String SELECT_GENERO = 
            "SELECT g.ID_GENERO, g.NOMBRE, g.GENERO_PRINCIPAL_ID"
             + " FROM GENERO g ";

    private final JdbcClient jdbcClient;

    public GeneroRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<Genero> findAll() {
        return jdbcClient.sql(SELECT_GENERO + "ORDER BY g.ID_GENERO")
                .query(this::mapRow)
                .list();
    }

    public Optional<Genero> findById(long id) {
        return jdbcClient.sql(SELECT_GENERO + "WHERE g.ID_GENERO = :id")
                .param("id", id)
                .query(this::mapRow)
                .optional();
    }

    public boolean existsById(long id) {
        return jdbcClient.sql("SELECT COUNT(*) FROM GENERO g WHERE g.ID_GENERO = :id")
                .param("id", id)
                .query(Integer.class).single() > 0;
    }

    public long count() {
        return jdbcClient.sql("SELECT COUNT(*) FROM GENERO g").query(Long.class).single();
    }

    public List<Genero> findByNombre(String nombre) {
        return jdbcClient.sql(SELECT_GENERO + "WHERE g.NOMBRE = :nombre" + " ORDER BY g.ID_GENERO")
                .param("nombre", nombre)
                .query(this::mapRow)
                .list();
    }

    public List<Genero> findByGeneroPrincipalId(long idGenero) {
        return jdbcClient.sql(SELECT_GENERO + "WHERE g.GENERO_PRINCIPAL_ID = :idGenero" + " ORDER BY g.ID_GENERO")
                .param("idGenero", idGenero)
                .query(this::mapRow)
                .list();
    }

    private Genero mapRow(ResultSet rs, int fila) throws SQLException {
        var principal = referencia(rs, "GENERO_PRINCIPAL_ID",
                id -> { var g = new Genero(); g.setIdGenero(id); return g; });
        return new Genero(rs.getLong("ID_GENERO"), rs.getString("NOMBRE"), principal);
    }

    private static <T> T referencia(ResultSet rs, String columna, LongFunction<T> factory) throws SQLException {
        long id = rs.getLong(columna);
        return rs.wasNull() ? null : factory.apply(id);
    }
    
    public long nextId() {
        return jdbcClient.sql("SELECT SEQ_GENERO.NEXTVAL FROM DUAL").query(Long.class).single();
    }

    public Genero save(Genero genero) {
        genero.setIdGenero(nextId());
        jdbcClient.sql("INSERT INTO GENERO (ID_GENERO, NOMBRE, GENERO_PRINCIPAL_ID) "
                + "VALUES (:id, :nombre, :principal)")
                .param("id", genero.getIdGenero())
                .param("nombre", genero.getNombre())
                .param("principal", genero.getGeneroPrincipal() != null ? genero.getGeneroPrincipal().getIdGenero() : null,
                        java.sql.Types.NUMERIC)
                .update();
        return genero;
    }

    public void update(Genero genero) {
        jdbcClient.sql("UPDATE GENERO SET NOMBRE = :nombre, GENERO_PRINCIPAL_ID = :principal WHERE ID_GENERO = :id")
                .param("id", genero.getIdGenero())
                .param("nombre", genero.getNombre())
                .param("principal", genero.getGeneroPrincipal() != null ? genero.getGeneroPrincipal().getIdGenero() : null,
                        java.sql.Types.NUMERIC)
                .update();
    }

    public void deleteById(long id) {
        jdbcClient.sql("DELETE FROM GENERO WHERE ID_GENERO = :id").param("id", id).update();
    }
}
