package co.edu.unbosque.reventacero.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.LongFunction;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.reventacero.entity.Inscripcion;
import co.edu.unbosque.reventacero.entity.ListaEspera;
import co.edu.unbosque.reventacero.entity.TipoEntrada;
import co.edu.unbosque.reventacero.entity.Usuario;

/**
 * Acceso JDBC de solo lectura para Inscripcion. RF-16; Fase 2, semana 11.
 * Las relaciones contienen sus claves; no se cargan recursivamente.
 */
@Repository
public class InscripcionRepository {

    private static final String SELECT_INSCRIPCION = 
            "SELECT i.DOCUMENTO, i.ID_LISTA, i.FECHA_SOLICITUD, i.ESTADO"
             + " FROM INSCRIPCION i ";

    private final JdbcClient jdbcClient;

    public InscripcionRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<Inscripcion> findAll() {
        return jdbcClient.sql(SELECT_INSCRIPCION + "ORDER BY i.FECHA_SOLICITUD, i.DOCUMENTO, i.ID_LISTA")
                .query(this::mapRow)
                .list();
    }

    public Optional<Inscripcion> findById(long documento, long idLista) {
        return jdbcClient.sql(SELECT_INSCRIPCION + "WHERE i.DOCUMENTO = :documento AND i.ID_LISTA = :idLista")
                .param("documento", documento)
                .param("idLista", idLista)
                .query(this::mapRow)
                .optional();
    }

    public boolean existsById(long documento, long idLista) {
        return jdbcClient.sql("SELECT COUNT(*) FROM INSCRIPCION i WHERE i.DOCUMENTO = :documento AND i.ID_LISTA = :idLista")
                .param("documento", documento)
                .param("idLista", idLista)
                .query(Integer.class).single() > 0;
    }

    public long count() {
        return jdbcClient.sql("SELECT COUNT(*) FROM INSCRIPCION i").query(Long.class).single();
    }

    public List<Inscripcion> findByUsuarioDocumento(long documento) {
        return jdbcClient.sql(SELECT_INSCRIPCION + "WHERE i.DOCUMENTO = :documento" + " ORDER BY i.FECHA_SOLICITUD, i.DOCUMENTO, i.ID_LISTA")
                .param("documento", documento)
                .query(this::mapRow)
                .list();
    }

    public List<Inscripcion> findByListaEsperaId(long idLista) {
        return jdbcClient.sql(SELECT_INSCRIPCION + "WHERE i.ID_LISTA = :idLista" + " ORDER BY i.FECHA_SOLICITUD, i.DOCUMENTO, i.ID_LISTA")
                .param("idLista", idLista)
                .query(this::mapRow)
                .list();
    }

    /** Inscripciones pendientes de los tipos de entrada de una zona, de la más antigua a la más reciente. */
    public List<Inscripcion> findPendientesPorZona(long idZona) {
        return jdbcClient.sql("SELECT i.DOCUMENTO, i.ID_LISTA, i.FECHA_SOLICITUD, i.ESTADO, l.ID_TIPO_ENTRADA"
                + " FROM INSCRIPCION i"
                + " JOIN LISTA_ESPERA l ON l.ID_LISTA = i.ID_LISTA"
                + " JOIN TIPO_ENTRADA te ON te.ID_TIPO_ENTRADA = l.ID_TIPO_ENTRADA"
                + " WHERE te.ID_ZONA = :idZona AND i.ESTADO = :estado"
                + " ORDER BY i.FECHA_SOLICITUD, i.DOCUMENTO, i.ID_LISTA")
                .param("idZona", idZona)
                .param("estado", "PENDIENTE")
                .query((rs, fila) -> {
                    var inscripcion = mapRow(rs, fila);
                    var tipoEntrada = new TipoEntrada();
                    tipoEntrada.setIdTipoEntrada(rs.getLong("ID_TIPO_ENTRADA"));
                    inscripcion.getListaEspera().setTipoEntrada(tipoEntrada);
                    return inscripcion;
                })
                .list();
    }

    private Inscripcion mapRow(ResultSet rs, int fila) throws SQLException {
        var lista = referencia(rs, "ID_LISTA",
                id -> { var l = new ListaEspera(); l.setIdLista(id); return l; });
        return new Inscripcion(usuarioRef(rs, "DOCUMENTO"), lista, fecha(rs, "FECHA_SOLICITUD"),
                rs.getString("ESTADO"));
    }

    private static LocalDate fecha(ResultSet rs, String columna) throws SQLException {
        var valor = rs.getDate(columna);
        return valor == null ? null : valor.toLocalDate();
    }

    private static <T> T referencia(ResultSet rs, String columna, LongFunction<T> factory) throws SQLException {
        long id = rs.getLong(columna);
        return rs.wasNull() ? null : factory.apply(id);
    }

    private static Usuario usuarioRef(ResultSet rs, String columna) throws SQLException {
        return referencia(rs, columna, id -> { var e = new Usuario(); e.setDocumento(id); return e; });
    }
    
    public void save(Inscripcion inscripcion) {
        jdbcClient.sql("INSERT INTO INSCRIPCION (DOCUMENTO, ID_LISTA, FECHA_SOLICITUD, ESTADO) "
                + "VALUES (:documento, :idLista, SYSDATE, :estado)")
                .param("documento", inscripcion.getUsuario().getDocumento())
                .param("idLista", inscripcion.getListaEspera().getIdLista())
                .param("estado", inscripcion.getEstado())
                .update();
    }

    public void reactivar(long documento, long idLista, String estado) {
        jdbcClient.sql("UPDATE INSCRIPCION SET ESTADO = :estado, FECHA_SOLICITUD = SYSDATE "
                + "WHERE DOCUMENTO = :documento AND ID_LISTA = :idLista")
                .param("documento", documento)
                .param("idLista", idLista)
                .param("estado", estado)
                .update();
    }

    public void actualizarEstado(long documento, long idLista, String estado) {
        jdbcClient.sql("UPDATE INSCRIPCION SET ESTADO = :estado WHERE DOCUMENTO = :documento AND ID_LISTA = :idLista")
                .param("documento", documento)
                .param("idLista", idLista)
                .param("estado", estado)
                .update();
    }
}
