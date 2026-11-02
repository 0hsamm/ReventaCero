package co.edu.unbosque.reventacero.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.function.LongFunction;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.reventacero.entity.Dispositivo;
import co.edu.unbosque.reventacero.entity.Usuario;

/**
 * Acceso JDBC de solo lectura para Dispositivo. RF-11, RF-20, RF-22; Fase 2, semana 11.
 * Las relaciones contienen sus claves; no se cargan recursivamente.
 */
@Repository
public class DispositivoRepository {

    private static final String SELECT_DISPOSITIVO = 
            "SELECT d.ID_DISPOSITIVO, d.IDENTIFICADOR_UNICO, d.TIPO, d.ACTIVO, d.DOCUMENTO"
             + " FROM DISPOSITIVO d ";

    private final JdbcClient jdbcClient;

    public DispositivoRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<Dispositivo> findAll() {
        return jdbcClient.sql(SELECT_DISPOSITIVO + "ORDER BY d.ID_DISPOSITIVO")
                .query(this::mapRow)
                .list();
    }

    public Optional<Dispositivo> findById(long id) {
        return jdbcClient.sql(SELECT_DISPOSITIVO + "WHERE d.ID_DISPOSITIVO = :id")
                .param("id", id)
                .query(this::mapRow)
                .optional();
    }

    public boolean existsById(long id) {
        return jdbcClient.sql("SELECT COUNT(*) FROM DISPOSITIVO d WHERE d.ID_DISPOSITIVO = :id")
                .param("id", id)
                .query(Integer.class).single() > 0;
    }

    public long count() {
        return jdbcClient.sql("SELECT COUNT(*) FROM DISPOSITIVO d").query(Long.class).single();
    }

    public List<Dispositivo> findByUsuarioDocumento(long documento) {
        return jdbcClient.sql(SELECT_DISPOSITIVO + "WHERE d.DOCUMENTO = :documento" + " ORDER BY d.ID_DISPOSITIVO")
                .param("documento", documento)
                .query(this::mapRow)
                .list();
    }

    public List<Dispositivo> findByIdentificadorUnico(String identificadorUnico) {
        return jdbcClient.sql(SELECT_DISPOSITIVO + "WHERE d.IDENTIFICADOR_UNICO = :identificadorUnico" + " ORDER BY d.ID_DISPOSITIVO")
                .param("identificadorUnico", identificadorUnico)
                .query(this::mapRow)
                .list();
    }

    public List<Dispositivo> findByUsuarioDocumentoAndActivoTrue(long documento) {
        return jdbcClient.sql(SELECT_DISPOSITIVO + "WHERE d.DOCUMENTO = :documento AND d.ACTIVO = 1" + " ORDER BY d.ID_DISPOSITIVO")
                .param("documento", documento)
                .query(this::mapRow)
                .list();
    }

    private Dispositivo mapRow(ResultSet rs, int fila) throws SQLException {
        return new Dispositivo(rs.getLong("ID_DISPOSITIVO"), rs.getString("IDENTIFICADOR_UNICO"),
                rs.getString("TIPO"), rs.getInt("ACTIVO") == 1, usuarioRef(rs, "DOCUMENTO"));
    }

    private static Usuario usuarioRef(ResultSet rs, String columna) throws SQLException {
        return referencia(rs, columna, id -> { var e = new Usuario(); e.setDocumento(id); return e; });
    }

    private static <T> T referencia(ResultSet rs, String columna, LongFunction<T> factory) throws SQLException {
        long id = rs.getLong(columna);
        return rs.wasNull() ? null : factory.apply(id);
    }
    

    public long nextId() {
        return jdbcClient.sql("SELECT SEQ_DISPOSITIVO.NEXTVAL FROM DUAL").query(Long.class).single();
    }

    public Dispositivo save(Dispositivo dispositivo) {
        dispositivo.setIdDispositivo(nextId());
        jdbcClient.sql("INSERT INTO DISPOSITIVO (ID_DISPOSITIVO, IDENTIFICADOR_UNICO, TIPO, ACTIVO, DOCUMENTO) "
                + "VALUES (:id, :identificador, :tipo, :activo, :documento)")
                .param("id", dispositivo.getIdDispositivo())
                .param("identificador", dispositivo.getIdentificadorUnico())
                .param("tipo", dispositivo.getTipo(), java.sql.Types.VARCHAR)
                .param("activo", dispositivo.isActivo() ? 1 : 0)
                .param("documento", dispositivo.getUsuario().getDocumento())
                .update();
        return dispositivo;
    }

    public void update(Dispositivo dispositivo) {
        jdbcClient.sql("UPDATE DISPOSITIVO SET IDENTIFICADOR_UNICO = :identificador, TIPO = :tipo, ACTIVO = :activo "
                + "WHERE ID_DISPOSITIVO = :id")
                .param("id", dispositivo.getIdDispositivo())
                .param("identificador", dispositivo.getIdentificadorUnico())
                .param("tipo", dispositivo.getTipo(), java.sql.Types.VARCHAR)
                .param("activo", dispositivo.isActivo() ? 1 : 0)
                .update();
    }

    public void deleteById(long id) {
        jdbcClient.sql("DELETE FROM DISPOSITIVO WHERE ID_DISPOSITIVO = :id").param("id", id).update();
    }
}
