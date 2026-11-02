package co.edu.unbosque.reventacero.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.LongFunction;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.reventacero.entity.Inscripcion;
import co.edu.unbosque.reventacero.entity.ListaEspera;
import co.edu.unbosque.reventacero.entity.Notificacion;
import co.edu.unbosque.reventacero.entity.Usuario;

/**
 * Acceso JDBC de solo lectura para Notificacion. RF-16; Fase 2, semana 11.
 * Las relaciones contienen sus claves; no se cargan recursivamente.
 */
@Repository
public class NotificacionRepository {

    private static final String SELECT_NOTIFICACION = 
            "SELECT n.ID_NOTIFICACION, n.FECHA_HORA, n.CANAL, n.MENSAJE, n.USUARIO_DOCUMENTO, n.ID_LISTA"
             + " FROM NOTIFICACION n ";

    private final JdbcClient jdbcClient;

    public NotificacionRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<Notificacion> findAll() {
        return jdbcClient.sql(SELECT_NOTIFICACION + "ORDER BY n.FECHA_HORA, n.ID_NOTIFICACION")
                .query(this::mapRow)
                .list();
    }

    public Optional<Notificacion> findById(long id) {
        return jdbcClient.sql(SELECT_NOTIFICACION + "WHERE n.ID_NOTIFICACION = :id")
                .param("id", id)
                .query(this::mapRow)
                .optional();
    }

    public boolean existsById(long id) {
        return jdbcClient.sql("SELECT COUNT(*) FROM NOTIFICACION n WHERE n.ID_NOTIFICACION = :id")
                .param("id", id)
                .query(Integer.class).single() > 0;
    }

    public long count() {
        return jdbcClient.sql("SELECT COUNT(*) FROM NOTIFICACION n").query(Long.class).single();
    }

    public List<Notificacion> findByInscripcion(long documento, long idLista) {
        return jdbcClient.sql(SELECT_NOTIFICACION + "WHERE n.USUARIO_DOCUMENTO = :documento AND n.ID_LISTA = :idLista" + " ORDER BY n.FECHA_HORA, n.ID_NOTIFICACION")
                .param("documento", documento)
                .param("idLista", idLista)
                .query(this::mapRow)
                .list();
    }

    public List<Notificacion> findByUsuarioDocumento(long documento) {
        return jdbcClient.sql(SELECT_NOTIFICACION + "WHERE n.USUARIO_DOCUMENTO = :documento" + " ORDER BY n.FECHA_HORA, n.ID_NOTIFICACION")
                .param("documento", documento)
                .query(this::mapRow)
                .list();
    }

    private Notificacion mapRow(ResultSet rs, int fila) throws SQLException {
        var lista = referencia(rs, "ID_LISTA",
                id -> { var l = new ListaEspera(); l.setIdLista(id); return l; });
        var inscripcion = new Inscripcion();
        inscripcion.setUsuario(usuarioRef(rs, "USUARIO_DOCUMENTO"));
        inscripcion.setListaEspera(lista);
        return new Notificacion(rs.getLong("ID_NOTIFICACION"), fechaHora(rs, "FECHA_HORA"),
                rs.getString("CANAL"), rs.getString("MENSAJE"), inscripcion);
    }

    private static LocalDateTime fechaHora(ResultSet rs, String columna) throws SQLException {
        var valor = rs.getTimestamp(columna);
        return valor == null ? null : valor.toLocalDateTime();
    }

    private static <T> T referencia(ResultSet rs, String columna, LongFunction<T> factory) throws SQLException {
        long id = rs.getLong(columna);
        return rs.wasNull() ? null : factory.apply(id);
    }

    private static Usuario usuarioRef(ResultSet rs, String columna) throws SQLException {
        return referencia(rs, columna, id -> { var e = new Usuario(); e.setDocumento(id); return e; });
    }
}
