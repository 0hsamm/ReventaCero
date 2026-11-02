package co.edu.unbosque.reventacero.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.LongFunction;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.reventacero.entity.Administrador;
import co.edu.unbosque.reventacero.entity.NombreCompleto;
import co.edu.unbosque.reventacero.entity.Usuario;
import co.edu.unbosque.reventacero.enums.TipoUsuario;

/**
 * Acceso JDBC de solo lectura para Administrador. RF-10; Fase 2, semana 11.
 * Las relaciones contienen sus claves; no se cargan recursivamente.
 */
@Repository
public class AdministradorRepository {

    private static final String SELECT_ADMINISTRADOR = 
            "SELECT u.DOCUMENTO, u.NOMBRE_USUARIO, u.CONTRASENIA, u.FECHA_REGISTRO, u.ESTADO_CUENTA, "
             + "u.NOMBRES, u.APELLIDOS, u.CORREO, u.FECHA_NACIMIENTO, u.ADMINISTRADOR_DOCUMENTO, "
             + "u.FECHA_AUTORIZACION, u.TIPO_USUARIO, a.NIVEL_ACCESO, a.FECHA_DESIGNACION"
             + " FROM ADMINISTRADOR a JOIN USUARIO u ON u.DOCUMENTO = a.DOCUMENTO ";

    private final JdbcClient jdbcClient;

    public AdministradorRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<Administrador> findAll() {
        return jdbcClient.sql(SELECT_ADMINISTRADOR + "ORDER BY a.DOCUMENTO")
                .query(this::mapRow)
                .list().stream().map(this::cargarTelefonos).toList();
    }

    public Optional<Administrador> findById(long id) {
        return jdbcClient.sql(SELECT_ADMINISTRADOR + "WHERE a.DOCUMENTO = :id")
                .param("id", id)
                .query(this::mapRow)
                .optional().map(this::cargarTelefonos);
    }

    public boolean existsById(long id) {
        return jdbcClient.sql("SELECT COUNT(*) FROM ADMINISTRADOR a JOIN USUARIO u ON u.DOCUMENTO = a.DOCUMENTO WHERE a.DOCUMENTO = :id")
                .param("id", id)
                .query(Integer.class).single() > 0;
    }

    public long count() {
        return jdbcClient.sql("SELECT COUNT(*) FROM ADMINISTRADOR a JOIN USUARIO u ON u.DOCUMENTO = a.DOCUMENTO").query(Long.class).single();
    }

    private Administrador mapRow(ResultSet rs, int fila) throws SQLException {
        var e = datosUsuario(rs, new Administrador());
        e.setNivelAcceso(rs.getString("NIVEL_ACCESO"));
        e.setFechaDesignacion(fecha(rs, "FECHA_DESIGNACION"));
        return e;
    }

    private Administrador cargarTelefonos(Administrador entidad) {
        List<String> telefonos = jdbcClient.sql(
                "SELECT TELEFONO FROM USUARIO_TELEFONO WHERE DOCUMENTO = :documento ORDER BY TELEFONO")
                .param("documento", entidad.getDocumento()).query(String.class).list();
        entidad.setTelefono(new ArrayList<>(telefonos));
        return entidad;
    }

    private static LocalDate fecha(ResultSet rs, String columna) throws SQLException {
        var valor = rs.getDate(columna);
        return valor == null ? null : valor.toLocalDate();
    }

    private static <T extends Usuario> T datosUsuario(ResultSet rs, T e) throws SQLException {
        e.setDocumento(rs.getLong("DOCUMENTO"));
        e.setNombreUsuario(rs.getString("NOMBRE_USUARIO"));
        e.setContrasenia(rs.getString("CONTRASENIA"));
        e.setFechaRegistro(fecha(rs, "FECHA_REGISTRO"));
        e.setEstadoCuenta(rs.getString("ESTADO_CUENTA"));
        e.setNombreCompleto(new NombreCompleto(rs.getString("NOMBRES"), rs.getString("APELLIDOS")));
        e.setCorreo(rs.getString("CORREO"));
        e.setFechaNacimiento(fecha(rs, "FECHA_NACIMIENTO"));
        e.setFechaAutorizacion(fecha(rs, "FECHA_AUTORIZACION"));
        e.setAutorizadoPor(referencia(rs, "ADMINISTRADOR_DOCUMENTO",
                id -> { var a = new Administrador(); a.setDocumento(id); return a; }));
        String tipo = rs.getString("TIPO_USUARIO");
        e.setTipoUsuario(tipo == null ? null : TipoUsuario.valueOf(tipo));
        return e;
    }

    private static <T> T referencia(ResultSet rs, String columna, LongFunction<T> factory) throws SQLException {
        long id = rs.getLong(columna);
        return rs.wasNull() ? null : factory.apply(id);
    }

    public void save(Administrador administrador) {
        jdbcClient.sql("INSERT INTO ADMINISTRADOR (DOCUMENTO, NIVEL_ACCESO, FECHA_DESIGNACION) "
                + "VALUES (:documento, :nivelAcceso, :fechaDesignacion)")
                .param("documento", administrador.getDocumento())
                .param("nivelAcceso", administrador.getNivelAcceso(), java.sql.Types.VARCHAR)
                .param("fechaDesignacion",
                        administrador.getFechaDesignacion() != null
                                ? java.sql.Date.valueOf(administrador.getFechaDesignacion()) : null,
                        java.sql.Types.DATE)
                .update();
    }

    public void update(Administrador administrador) {
        jdbcClient.sql("UPDATE ADMINISTRADOR SET NIVEL_ACCESO = :nivelAcceso WHERE DOCUMENTO = :documento")
                .param("documento", administrador.getDocumento())
                .param("nivelAcceso", administrador.getNivelAcceso(), java.sql.Types.VARCHAR)
                .update();
    }

    public void deleteById(long documento) {
        jdbcClient.sql("DELETE FROM ADMINISTRADOR WHERE DOCUMENTO = :documento")
                .param("documento", documento)
                .update();
    }
}
