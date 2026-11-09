package co.edu.unbosque.reventacero.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.LongFunction;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.reventacero.entity.Artista;
import co.edu.unbosque.reventacero.entity.Escenario;
import co.edu.unbosque.reventacero.entity.Festival;
import co.edu.unbosque.reventacero.entity.Presentacion;

/**
 * Acceso JDBC de solo lectura para Presentacion. RF-05, RF-18, RF-24; Fase 2, semana 11.
 * Las relaciones contienen sus claves; no se cargan recursivamente.
 */
@Repository
public class PresentacionRepository {

    private static final String SELECT_PRESENTACION = 
            "SELECT p.ID_PRESENTACION, p.FECHA, p.HORA_INICIO, p.HORA_FIN, p.ESCENARIO_FESTIVAL_ID, "
             + "p.ESCENARIO_NOMBRE, p.ID_ARTISTA"
             + " FROM PRESENTACION p ";

    private final JdbcClient jdbcClient;

    public PresentacionRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<Presentacion> findAll() {
        return jdbcClient.sql(SELECT_PRESENTACION + "ORDER BY p.FECHA, p.HORA_INICIO, p.ID_PRESENTACION")
                .query(this::mapRow)
                .list();
    }

    public Optional<Presentacion> findById(long id) {
        return jdbcClient.sql(SELECT_PRESENTACION + "WHERE p.ID_PRESENTACION = :id")
                .param("id", id)
                .query(this::mapRow)
                .optional();
    }

    public boolean existsById(long id) {
        return jdbcClient.sql("SELECT COUNT(*) FROM PRESENTACION p WHERE p.ID_PRESENTACION = :id")
                .param("id", id)
                .query(Integer.class).single() > 0;
    }

    public long count() {
        return jdbcClient.sql("SELECT COUNT(*) FROM PRESENTACION p").query(Long.class).single();
    }

    public List<Presentacion> findByArtistaId(long idArtista) {
        return jdbcClient.sql(SELECT_PRESENTACION + "WHERE p.ID_ARTISTA = :idArtista" + " ORDER BY p.FECHA, p.HORA_INICIO, p.ID_PRESENTACION")
                .param("idArtista", idArtista)
                .query(this::mapRow)
                .list();
    }

    public List<Presentacion> findByEscenario(long idFestival, String nombreEscenario) {
        return jdbcClient.sql(SELECT_PRESENTACION + "WHERE p.ESCENARIO_FESTIVAL_ID = :idFestival AND p.ESCENARIO_NOMBRE = :nombreEscenario" + " ORDER BY p.FECHA, p.HORA_INICIO, p.ID_PRESENTACION")
                .param("idFestival", idFestival)
                .param("nombreEscenario", nombreEscenario)
                .query(this::mapRow)
                .list();
    }

    public List<Presentacion> findByFecha(java.time.LocalDate fecha) {
        return jdbcClient.sql(SELECT_PRESENTACION + "WHERE TRUNC(p.FECHA) = :fecha" + " ORDER BY p.FECHA, p.HORA_INICIO, p.ID_PRESENTACION")
                .param("fecha", java.sql.Date.valueOf(Objects.requireNonNull(fecha)))
                .query(this::mapRow)
                .list();
    }

    private Presentacion mapRow(ResultSet rs, int fila) throws SQLException {
        var escenario = new Escenario(festivalRef(rs, "ESCENARIO_FESTIVAL_ID"),
                rs.getString("ESCENARIO_NOMBRE"), 0);
        return new Presentacion(rs.getLong("ID_PRESENTACION"), fecha(rs, "FECHA"),
                fechaHora(rs, "HORA_INICIO"), fechaHora(rs, "HORA_FIN"), escenario,
                artistaRef(rs, "ID_ARTISTA"));
    }

    private static LocalDate fecha(ResultSet rs, String columna) throws SQLException {
        var valor = rs.getDate(columna);
        return valor == null ? null : valor.toLocalDate();
    }

    private static LocalDateTime fechaHora(ResultSet rs, String columna) throws SQLException {
        var valor = rs.getTimestamp(columna);
        return valor == null ? null : valor.toLocalDateTime();
    }

    private static Festival festivalRef(ResultSet rs, String columna) throws SQLException {
        return referencia(rs, columna, id -> { var e = new Festival(); e.setIdFestival(id); return e; });
    }

    private static Artista artistaRef(ResultSet rs, String columna) throws SQLException {
        return referencia(rs, columna, id -> { var e = new Artista(); e.setIdArtista(id); return e; });
    }

    private static <T> T referencia(ResultSet rs, String columna, LongFunction<T> factory) throws SQLException {
        long id = rs.getLong(columna);
        return rs.wasNull() ? null : factory.apply(id);
    }
    
    public long nextId() {
        return jdbcClient.sql("SELECT SEQ_PRESENTACION.NEXTVAL FROM DUAL").query(Long.class).single();
    }

    public Presentacion save(Presentacion presentacion) {
        presentacion.setIdPresentacion(nextId());
        jdbcClient.sql("INSERT INTO PRESENTACION (ID_PRESENTACION, FECHA, HORA_INICIO, HORA_FIN, "
                + "ESCENARIO_FESTIVAL_ID, ESCENARIO_NOMBRE, ID_ARTISTA) "
                + "VALUES (:id, :fecha, :horaInicio, :horaFin, :idFestival, :nombreEscenario, :idArtista)")
                .param("id", presentacion.getIdPresentacion())
                .param("fecha", java.sql.Date.valueOf(presentacion.getFecha()))
                .param("horaInicio", java.sql.Timestamp.valueOf(presentacion.getHoraInicio()))
                .param("horaFin", java.sql.Timestamp.valueOf(presentacion.getHoraFin()))
                .param("idFestival", presentacion.getEscenario().getFestival().getIdFestival())
                .param("nombreEscenario", presentacion.getEscenario().getNombreEscenario())
                .param("idArtista", presentacion.getArtista().getIdArtista())
                .update();
        return presentacion;
    }

    public void update(Presentacion presentacion) {
        jdbcClient.sql("UPDATE PRESENTACION SET FECHA = :fecha, HORA_INICIO = :horaInicio, HORA_FIN = :horaFin, "
                + "ESCENARIO_FESTIVAL_ID = :idFestival, ESCENARIO_NOMBRE = :nombreEscenario, ID_ARTISTA = :idArtista "
                + "WHERE ID_PRESENTACION = :id")
                .param("id", presentacion.getIdPresentacion())
                .param("fecha", java.sql.Date.valueOf(presentacion.getFecha()))
                .param("horaInicio", java.sql.Timestamp.valueOf(presentacion.getHoraInicio()))
                .param("horaFin", java.sql.Timestamp.valueOf(presentacion.getHoraFin()))
                .param("idFestival", presentacion.getEscenario().getFestival().getIdFestival())
                .param("nombreEscenario", presentacion.getEscenario().getNombreEscenario())
                .param("idArtista", presentacion.getArtista().getIdArtista())
                .update();
    }

    public void deleteById(long id) {
        jdbcClient.sql("DELETE FROM PRESENTACION WHERE ID_PRESENTACION = :id").param("id", id).update();
    }

    public List<Presentacion> findByFestivalId(long idFestival) {
        return jdbcClient.sql(SELECT_PRESENTACION + "WHERE p.ESCENARIO_FESTIVAL_ID = :idFestival"
                + " ORDER BY p.FECHA, p.HORA_INICIO, p.ID_PRESENTACION")
                .param("idFestival", idFestival)
                .query(this::mapRow)
                .list();
    }
}
