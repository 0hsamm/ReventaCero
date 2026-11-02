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
import co.edu.unbosque.reventacero.entity.Organizador;
import co.edu.unbosque.reventacero.entity.Usuario;
import co.edu.unbosque.reventacero.enums.TipoUsuario;

/**
 * Acceso JDBC de solo lectura para Organizador. RF-01, RF-10; Fase 2, semana 11.
 * Las relaciones contienen sus claves; no se cargan recursivamente.
 */
@Repository
public class OrganizadorRepository {

    private static final String SELECT_ORGANIZADOR = 
            "SELECT u.DOCUMENTO, u.NOMBRE_USUARIO, u.CONTRASENIA, u.FECHA_REGISTRO, u.ESTADO_CUENTA, "
             + "u.NOMBRES, u.APELLIDOS, u.CORREO, u.FECHA_NACIMIENTO, u.ADMINISTRADOR_DOCUMENTO, "
             + "u.FECHA_AUTORIZACION, u.TIPO_USUARIO, o.CARGO, o.SUPERVISOR_DOCUMENTO"
             + " FROM ORGANIZADOR o JOIN USUARIO u ON u.DOCUMENTO = o.DOCUMENTO ";

    private final JdbcClient jdbcClient;

    public OrganizadorRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<Organizador> findAll() {
        return jdbcClient.sql(SELECT_ORGANIZADOR + "ORDER BY o.DOCUMENTO")
                .query(this::mapRow)
                .list().stream().map(this::cargarTelefonos).toList();
    }

    public Optional<Organizador> findById(long id) {
        return jdbcClient.sql(SELECT_ORGANIZADOR + "WHERE o.DOCUMENTO = :id")
                .param("id", id)
                .query(this::mapRow)
                .optional().map(this::cargarTelefonos);
    }

    public boolean existsById(long id) {
        return jdbcClient.sql("SELECT COUNT(*) FROM ORGANIZADOR o JOIN USUARIO u ON u.DOCUMENTO = o.DOCUMENTO WHERE o.DOCUMENTO = :id")
                .param("id", id)
                .query(Integer.class).single() > 0;
    }

    public long count() {
        return jdbcClient.sql("SELECT COUNT(*) FROM ORGANIZADOR o JOIN USUARIO u ON u.DOCUMENTO = o.DOCUMENTO").query(Long.class).single();
    }

    public List<Organizador> findBySupervisorDocumento(long documento) {
        return jdbcClient.sql(SELECT_ORGANIZADOR + "WHERE o.SUPERVISOR_DOCUMENTO = :documento" + " ORDER BY o.DOCUMENTO")
                .param("documento", documento)
                .query(this::mapRow)
                .list().stream().map(this::cargarTelefonos).toList();
    }

    private Organizador mapRow(ResultSet rs, int fila) throws SQLException {
        var e = datosUsuario(rs, new Organizador());
        e.setCargo(rs.getString("CARGO"));
        e.setSupervisor(referencia(rs, "SUPERVISOR_DOCUMENTO",
                id -> { var o = new Organizador(); o.setDocumento(id); return o; }));
        return e;
    }

    private Organizador cargarTelefonos(Organizador entidad) {
        List<String> telefonos = jdbcClient.sql(
                "SELECT TELEFONO FROM USUARIO_TELEFONO WHERE DOCUMENTO = :documento ORDER BY TELEFONO")
                .param("documento", entidad.getDocumento()).query(String.class).list();
        entidad.setTelefono(new ArrayList<>(telefonos));
        return entidad;
    }

    private static <T> T referencia(ResultSet rs, String columna, LongFunction<T> factory) throws SQLException {
        long id = rs.getLong(columna);
        return rs.wasNull() ? null : factory.apply(id);
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

    private static LocalDate fecha(ResultSet rs, String columna) throws SQLException {
        var valor = rs.getDate(columna);
        return valor == null ? null : valor.toLocalDate();
    }
    

    public void save(Organizador organizador) {
        jdbcClient.sql("INSERT INTO ORGANIZADOR (DOCUMENTO, CARGO, SUPERVISOR_DOCUMENTO) "
                + "VALUES (:documento, :cargo, :supervisor)")
                .param("documento", organizador.getDocumento())
                .param("cargo", organizador.getCargo(), java.sql.Types.VARCHAR)
                .param("supervisor", organizador.getSupervisor() != null ? organizador.getSupervisor().getDocumento() : null,
                        java.sql.Types.NUMERIC)
                .update();
    }

    public void update(Organizador organizador) {
        jdbcClient.sql("UPDATE ORGANIZADOR SET CARGO = :cargo, SUPERVISOR_DOCUMENTO = :supervisor "
                + "WHERE DOCUMENTO = :documento")
                .param("documento", organizador.getDocumento())
                .param("cargo", organizador.getCargo(), java.sql.Types.VARCHAR)
                .param("supervisor", organizador.getSupervisor() != null ? organizador.getSupervisor().getDocumento() : null,
                        java.sql.Types.NUMERIC)
                .update();
    }

    public void deleteById(long documento) {
        jdbcClient.sql("DELETE FROM ORGANIZADOR WHERE DOCUMENTO = :documento")
                .param("documento", documento)
                .update();
    }
}
