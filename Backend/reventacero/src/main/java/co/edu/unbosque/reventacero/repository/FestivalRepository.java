package co.edu.unbosque.reventacero.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.LongFunction;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.reventacero.entity.Festival;
import co.edu.unbosque.reventacero.entity.Organizador;
import co.edu.unbosque.reventacero.entity.Ubicacion;

/**
 * Acceso JDBC de solo lectura para Festival. RF-01; Fase 2, semana 11.
 * Las relaciones contienen sus claves; no se cargan recursivamente.
 */
@Repository
public class FestivalRepository {

    private static final String SELECT_FESTIVAL = 
            "SELECT f.ID_FESTIVAL, f.NOMBRE, f.EDICION, f.FECHA_INICIO, f.FECHA_FIN, f.UBICACION_PAIS, "
             + "f.UBICACION_CIUDAD, f.UBICACION_DIRECCION, f.ORGANIZADOR_DOCUMENTO"
             + " FROM FESTIVAL f ";

    private final JdbcClient jdbcClient;

    public FestivalRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<Festival> findAll() {
        return jdbcClient.sql(SELECT_FESTIVAL + "ORDER BY f.ID_FESTIVAL")
                .query(this::mapRow)
                .list();
    }

    public Optional<Festival> findById(long id) {
        return jdbcClient.sql(SELECT_FESTIVAL + "WHERE f.ID_FESTIVAL = :id")
                .param("id", id)
                .query(this::mapRow)
                .optional();
    }

    public boolean existsById(long id) {
        return jdbcClient.sql("SELECT COUNT(*) FROM FESTIVAL f WHERE f.ID_FESTIVAL = :id")
                .param("id", id)
                .query(Integer.class).single() > 0;
    }

    public long count() {
        return jdbcClient.sql("SELECT COUNT(*) FROM FESTIVAL f").query(Long.class).single();
    }

    public List<Festival> findByOrganizadorDocumento(long documento) {
        return jdbcClient.sql(SELECT_FESTIVAL + "WHERE f.ORGANIZADOR_DOCUMENTO = :documento" + " ORDER BY f.ID_FESTIVAL")
                .param("documento", documento)
                .query(this::mapRow)
                .list();
    }

    private Festival mapRow(ResultSet rs, int fila) throws SQLException {
        var organizador = referencia(rs, "ORGANIZADOR_DOCUMENTO",
                id -> { var o = new Organizador(); o.setDocumento(id); return o; });
        return new Festival(rs.getLong("ID_FESTIVAL"), rs.getString("NOMBRE"), rs.getInt("EDICION"),
                fecha(rs, "FECHA_INICIO"), fecha(rs, "FECHA_FIN"),
                new Ubicacion(rs.getString("UBICACION_PAIS"), rs.getString("UBICACION_CIUDAD"),
                        rs.getString("UBICACION_DIRECCION")), organizador);
    }

    private static LocalDate fecha(ResultSet rs, String columna) throws SQLException {
        var valor = rs.getDate(columna);
        return valor == null ? null : valor.toLocalDate();
    }

    private static <T> T referencia(ResultSet rs, String columna, LongFunction<T> factory) throws SQLException {
        long id = rs.getLong(columna);
        return rs.wasNull() ? null : factory.apply(id);
    }
    
    public long nextId() {
        return jdbcClient.sql("SELECT SEQ_FESTIVAL.NEXTVAL FROM DUAL").query(Long.class).single();
    }

    public Festival save(Festival festival) {
        festival.setIdFestival(nextId());
        jdbcClient.sql("INSERT INTO FESTIVAL (ID_FESTIVAL, NOMBRE, EDICION, FECHA_INICIO, FECHA_FIN, UBICACION_PAIS, "
                + "UBICACION_CIUDAD, UBICACION_DIRECCION, ORGANIZADOR_DOCUMENTO) "
                + "VALUES (:id, :nombre, :edicion, :fechaInicio, :fechaFin, :pais, :ciudad, :direccion, :organizador)")
                .param("id", festival.getIdFestival())
                .param("nombre", festival.getNombre())
                .param("edicion", festival.getEdicion())
                .param("fechaInicio", java.sql.Date.valueOf(festival.getFechaInicio()))
                .param("fechaFin", java.sql.Date.valueOf(festival.getFechaFin()))
                .param("pais", festival.getUbicacion().getPais(), java.sql.Types.VARCHAR)
                .param("ciudad", festival.getUbicacion().getCiudad(), java.sql.Types.VARCHAR)
                .param("direccion", festival.getUbicacion().getDireccion(), java.sql.Types.VARCHAR)
                .param("organizador", festival.getOrganizador().getDocumento())
                .update();
        return festival;
    }

    public void update(Festival festival) {
        jdbcClient.sql("UPDATE FESTIVAL SET NOMBRE = :nombre, EDICION = :edicion, FECHA_INICIO = :fechaInicio, "
                + "FECHA_FIN = :fechaFin, UBICACION_PAIS = :pais, UBICACION_CIUDAD = :ciudad, "
                + "UBICACION_DIRECCION = :direccion, ORGANIZADOR_DOCUMENTO = :organizador "
                + "WHERE ID_FESTIVAL = :id")
                .param("id", festival.getIdFestival())
                .param("nombre", festival.getNombre())
                .param("edicion", festival.getEdicion())
                .param("fechaInicio", java.sql.Date.valueOf(festival.getFechaInicio()))
                .param("fechaFin", java.sql.Date.valueOf(festival.getFechaFin()))
                .param("pais", festival.getUbicacion().getPais(), java.sql.Types.VARCHAR)
                .param("ciudad", festival.getUbicacion().getCiudad(), java.sql.Types.VARCHAR)
                .param("direccion", festival.getUbicacion().getDireccion(), java.sql.Types.VARCHAR)
                .param("organizador", festival.getOrganizador().getDocumento())
                .update();
    }

    public void deleteById(long id) {
        jdbcClient.sql("DELETE FROM FESTIVAL WHERE ID_FESTIVAL = :id").param("id", id).update();
    }

    public List<Festival> findByNombreAndEdicion(String nombre, int edicion) {
        return jdbcClient.sql(SELECT_FESTIVAL + "WHERE f.NOMBRE = :nombre AND f.EDICION = :edicion ORDER BY f.ID_FESTIVAL")
                .param("nombre", nombre)
                .param("edicion", edicion)
                .query(this::mapRow)
                .list();
    }
}
