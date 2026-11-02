package co.edu.unbosque.reventacero.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.reventacero.entity.Patrocinador;

/**
 * Acceso JDBC de solo lectura para Patrocinador. RF-08; Fase 2, semana 11.
 * Las relaciones contienen sus claves; no se cargan recursivamente.
 */
@Repository
public class PatrocinadorRepository {

    private static final String SELECT_PATROCINADOR = 
            "SELECT p.ID_PATROCINADOR, p.NOMBRE, p.TIPO, p.CONTACTO"
             + " FROM PATROCINADOR p ";

    private final JdbcClient jdbcClient;

    public PatrocinadorRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<Patrocinador> findAll() {
        return jdbcClient.sql(SELECT_PATROCINADOR + "ORDER BY p.ID_PATROCINADOR")
                .query(this::mapRow)
                .list();
    }

    public Optional<Patrocinador> findById(long id) {
        return jdbcClient.sql(SELECT_PATROCINADOR + "WHERE p.ID_PATROCINADOR = :id")
                .param("id", id)
                .query(this::mapRow)
                .optional();
    }

    public boolean existsById(long id) {
        return jdbcClient.sql("SELECT COUNT(*) FROM PATROCINADOR p WHERE p.ID_PATROCINADOR = :id")
                .param("id", id)
                .query(Integer.class).single() > 0;
    }

    public long count() {
        return jdbcClient.sql("SELECT COUNT(*) FROM PATROCINADOR p").query(Long.class).single();
    }

    public List<Patrocinador> findByNombre(String nombre) {
        return jdbcClient.sql(SELECT_PATROCINADOR + "WHERE p.NOMBRE = :nombre" + " ORDER BY p.ID_PATROCINADOR")
                .param("nombre", nombre)
                .query(this::mapRow)
                .list();
    }

    private Patrocinador mapRow(ResultSet rs, int fila) throws SQLException {
        return new Patrocinador(rs.getLong("ID_PATROCINADOR"), rs.getString("NOMBRE"),
                rs.getString("TIPO"), rs.getString("CONTACTO"));
    }

    public long nextId() {
        return jdbcClient.sql("SELECT SEQ_PATROCINADOR.NEXTVAL FROM DUAL").query(Long.class).single();
    }

    public Patrocinador save(Patrocinador patrocinador) {
        patrocinador.setIdPatrocinador(nextId());
        jdbcClient.sql("INSERT INTO PATROCINADOR (ID_PATROCINADOR, NOMBRE, TIPO, CONTACTO) "
                + "VALUES (:id, :nombre, :tipo, :contacto)")
                .param("id", patrocinador.getIdPatrocinador())
                .param("nombre", patrocinador.getNombre())
                .param("tipo", patrocinador.getTipo(), java.sql.Types.VARCHAR)
                .param("contacto", patrocinador.getContacto(), java.sql.Types.VARCHAR)
                .update();
        return patrocinador;
    }

    public void update(Patrocinador patrocinador) {
        jdbcClient.sql("UPDATE PATROCINADOR SET NOMBRE = :nombre, TIPO = :tipo, CONTACTO = :contacto "
                + "WHERE ID_PATROCINADOR = :id")
                .param("id", patrocinador.getIdPatrocinador())
                .param("nombre", patrocinador.getNombre())
                .param("tipo", patrocinador.getTipo(), java.sql.Types.VARCHAR)
                .param("contacto", patrocinador.getContacto(), java.sql.Types.VARCHAR)
                .update();
    }

    public void deleteById(long id) {
        jdbcClient.sql("DELETE FROM PATROCINADOR WHERE ID_PATROCINADOR = :id").param("id", id).update();
    }
}
