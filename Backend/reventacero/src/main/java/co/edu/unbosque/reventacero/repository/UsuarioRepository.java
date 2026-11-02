package co.edu.unbosque.reventacero.repository;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.reventacero.entity.Administrador;
import co.edu.unbosque.reventacero.entity.NombreCompleto;
import co.edu.unbosque.reventacero.entity.Usuario;
import co.edu.unbosque.reventacero.enums.TipoUsuario;

/**
 * Acceso a la tabla USUARIO mediante JDBC.
 * Tablas USUARIO y USUARIO_TELEFONO (creadas en SQL Developer).
 */
@Repository
public class UsuarioRepository {

	private static final String SELECT_USUARIO =
			"SELECT DOCUMENTO, NOMBRE_USUARIO, CONTRASENIA, FECHA_REGISTRO, ESTADO_CUENTA, "
			+ "NOMBRES, APELLIDOS, CORREO, FECHA_NACIMIENTO, ADMINISTRADOR_DOCUMENTO, FECHA_AUTORIZACION, TIPO_USUARIO "
			+ "FROM USUARIO ";

	private final JdbcClient jdbcClient;

	public UsuarioRepository(JdbcClient jdbcClient) {
		this.jdbcClient = jdbcClient;
	}

	public Optional<Usuario> findByCorreo(String correo) {
		return jdbcClient.sql(SELECT_USUARIO + "WHERE CORREO = :correo")
				.param("correo", correo)
				.query(this::mapRow)
				.optional()
				.map(this::cargarTelefonos);
	}

	public Optional<Usuario> findByNombreUsuario(String nombreUsuario) {
		return jdbcClient.sql(SELECT_USUARIO + "WHERE NOMBRE_USUARIO = :nombreUsuario")
				.param("nombreUsuario", nombreUsuario)
				.query(this::mapRow)
				.optional()
				.map(this::cargarTelefonos);
	}

	public boolean existsByDocumento(long documento) {
		return jdbcClient.sql("SELECT COUNT(*) FROM USUARIO WHERE DOCUMENTO = :documento")
				.param("documento", documento)
				.query(Integer.class)
				.single() > 0;
	}

	@Transactional
	public void save(Usuario usuario) {
		jdbcClient.sql("INSERT INTO USUARIO (DOCUMENTO, NOMBRE_USUARIO, CONTRASENIA, FECHA_REGISTRO, "
				+ "ESTADO_CUENTA, NOMBRES, APELLIDOS, CORREO, FECHA_NACIMIENTO, ADMINISTRADOR_DOCUMENTO, "
				+ "FECHA_AUTORIZACION, TIPO_USUARIO) VALUES (:documento, :nombreUsuario, :contrasenia, "
				+ ":fechaRegistro, :estadoCuenta, :nombre, :apellido, :correo, :fechaNacimiento, "
				+ ":autorizadoPor, :fechaAutorizacion, :tipoUsuario)")
				.param("documento", usuario.getDocumento())
				.param("nombreUsuario", usuario.getNombreUsuario())
				.param("contrasenia", usuario.getContrasenia())
				.param("fechaRegistro", toSqlDate(usuario.getFechaRegistro()))
				.param("estadoCuenta", usuario.getEstadoCuenta())
				.param("nombre", usuario.getNombreCompleto() != null ? usuario.getNombreCompleto().getNombre() : null)
				.param("apellido", usuario.getNombreCompleto() != null ? usuario.getNombreCompleto().getApellido() : null)
				.param("correo", usuario.getCorreo())
				.param("fechaNacimiento", toSqlDate(usuario.getFechaNacimiento()))
				.param("autorizadoPor", usuario.getAutorizadoPor() != null ? usuario.getAutorizadoPor().getDocumento() : null)
				.param("fechaAutorizacion", toSqlDate(usuario.getFechaAutorizacion()))
				.param("tipoUsuario", usuario.getTipoUsuario() != null ? usuario.getTipoUsuario().name() : null)
				.update();

		if (usuario.getTelefono() != null) {
			for (String telefono : usuario.getTelefono()) {
				jdbcClient.sql("INSERT INTO USUARIO_TELEFONO (DOCUMENTO, TELEFONO) VALUES (:documento, :telefono)")
						.param("documento", usuario.getDocumento())
						.param("telefono", telefono)
						.update();
			}
		}
	}

	private Usuario cargarTelefonos(Usuario usuario) {
		List<String> telefonos = jdbcClient
				.sql("SELECT TELEFONO FROM USUARIO_TELEFONO WHERE DOCUMENTO = :documento")
				.param("documento", usuario.getDocumento())
				.query(String.class)
				.list();
		usuario.setTelefono(new ArrayList<>(telefonos));
		return usuario;
	}

	private Usuario mapRow(ResultSet rs, int rowNum) throws SQLException {
		Usuario usuario = new Usuario();
		usuario.setDocumento(rs.getLong("DOCUMENTO"));
		usuario.setNombreUsuario(rs.getString("NOMBRE_USUARIO"));
		usuario.setContrasenia(rs.getString("CONTRASENIA"));
		usuario.setFechaRegistro(toLocalDate(rs.getDate("FECHA_REGISTRO")));
		usuario.setEstadoCuenta(rs.getString("ESTADO_CUENTA"));
		usuario.setNombreCompleto(new NombreCompleto(rs.getString("NOMBRES"), rs.getString("APELLIDOS")));
		usuario.setCorreo(rs.getString("CORREO"));
		usuario.setFechaNacimiento(toLocalDate(rs.getDate("FECHA_NACIMIENTO")));
		usuario.setFechaAutorizacion(toLocalDate(rs.getDate("FECHA_AUTORIZACION")));

		long administradorDocumento = rs.getLong("ADMINISTRADOR_DOCUMENTO");
		if (!rs.wasNull()) {
			Administrador autorizadoPor = new Administrador();
			autorizadoPor.setDocumento(administradorDocumento);
			usuario.setAutorizadoPor(autorizadoPor);
		}

		String tipo = rs.getString("TIPO_USUARIO");
		if (tipo != null) {
			usuario.setTipoUsuario(TipoUsuario.valueOf(tipo));
		}
		return usuario;
	}

	private static Date toSqlDate(LocalDate fecha) {
		return fecha != null ? Date.valueOf(fecha) : null;
	}

	private static LocalDate toLocalDate(Date fecha) {
		return fecha != null ? fecha.toLocalDate() : null;
	}
	

	public Optional<Usuario> findByDocumento(long documento) {
		return jdbcClient.sql(SELECT_USUARIO + "WHERE DOCUMENTO = :documento")
				.param("documento", documento)
				.query(this::mapRow)
				.optional()
				.map(this::cargarTelefonos);
	}

	public List<Usuario> findAll() {
		return jdbcClient.sql(SELECT_USUARIO + "ORDER BY DOCUMENTO")
				.query(this::mapRow)
				.list().stream().map(this::cargarTelefonos).toList();
	}

	public long countByAutorizadoPor(long documentoAdministrador) {
		return jdbcClient.sql("SELECT COUNT(*) FROM USUARIO WHERE ADMINISTRADOR_DOCUMENTO = :documento")
				.param("documento", documentoAdministrador)
				.query(Long.class).single();
	}

	@Transactional
	public void update(Usuario usuario) {
		jdbcClient.sql("UPDATE USUARIO SET NOMBRE_USUARIO = :nombreUsuario, NOMBRES = :nombre, "
				+ "APELLIDOS = :apellido, CORREO = :correo, FECHA_NACIMIENTO = :fechaNacimiento "
				+ "WHERE DOCUMENTO = :documento")
				.param("documento", usuario.getDocumento())
				.param("nombreUsuario", usuario.getNombreUsuario())
				.param("nombre", usuario.getNombreCompleto().getNombre())
				.param("apellido", usuario.getNombreCompleto().getApellido())
				.param("correo", usuario.getCorreo())
				.param("fechaNacimiento", toSqlDate(usuario.getFechaNacimiento()), java.sql.Types.DATE)
				.update();

		jdbcClient.sql("DELETE FROM USUARIO_TELEFONO WHERE DOCUMENTO = :documento")
				.param("documento", usuario.getDocumento())
				.update();

		if (usuario.getTelefono() != null) {
			for (String telefono : usuario.getTelefono()) {
				jdbcClient.sql("INSERT INTO USUARIO_TELEFONO (DOCUMENTO, TELEFONO) VALUES (:documento, :telefono)")
						.param("documento", usuario.getDocumento())
						.param("telefono", telefono)
						.update();
			}
		}
	}

	public void actualizarContrasenia(long documento, String contraseniaCodificada) {
		jdbcClient.sql("UPDATE USUARIO SET CONTRASENIA = :contrasenia WHERE DOCUMENTO = :documento")
				.param("documento", documento)
				.param("contrasenia", contraseniaCodificada)
				.update();
	}

	public void actualizarEstado(long documento, String estado) {
		jdbcClient.sql("UPDATE USUARIO SET ESTADO_CUENTA = :estado WHERE DOCUMENTO = :documento")
				.param("documento", documento)
				.param("estado", estado)
				.update();
	}

	@Transactional
	public void deleteById(long documento) {
		jdbcClient.sql("DELETE FROM USUARIO_TELEFONO WHERE DOCUMENTO = :documento")
				.param("documento", documento)
				.update();
		jdbcClient.sql("DELETE FROM USUARIO WHERE DOCUMENTO = :documento")
				.param("documento", documento)
				.update();
	}
}
