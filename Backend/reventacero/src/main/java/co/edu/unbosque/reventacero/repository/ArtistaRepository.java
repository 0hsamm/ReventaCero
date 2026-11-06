package co.edu.unbosque.reventacero.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.LongFunction;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.reventacero.entity.Artista;
import co.edu.unbosque.reventacero.entity.Genero;

/**
 * Acceso JDBC de solo lectura para Artista. RF-04, RF-24; Fase 2, semana 11.
 * Las relaciones contienen sus claves; no se cargan recursivamente.
 */
@Repository
public class ArtistaRepository {

    private static final String SELECT_ARTISTA = 
            "SELECT a.ID_ARTISTA, a.NOMBRE_ARTISTICO, a.PAIS, a.ID_GENERO"
             + " FROM ARTISTA a ";

    private final JdbcClient jdbcClient;

    public ArtistaRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<Artista> findAll() {
        return jdbcClient.sql(SELECT_ARTISTA + "ORDER BY a.ID_ARTISTA")
                .query(this::mapRow)
                .list().stream().map(this::cargarRelaciones).toList();
    }

    public Optional<Artista> findById(long id) {
        return jdbcClient.sql(SELECT_ARTISTA + "WHERE a.ID_ARTISTA = :id")
                .param("id", id)
                .query(this::mapRow)
                .optional().map(this::cargarRelaciones);
    }

    public boolean existsById(long id) {
        return jdbcClient.sql("SELECT COUNT(*) FROM ARTISTA a WHERE a.ID_ARTISTA = :id")
                .param("id", id)
                .query(Integer.class).single() > 0;
    }

    public long count() {
        return jdbcClient.sql("SELECT COUNT(*) FROM ARTISTA a").query(Long.class).single();
    }

    public List<Artista> findByGeneroId(long idGenero) {
        return jdbcClient.sql(SELECT_ARTISTA + "WHERE a.ID_GENERO = :idGenero" + " ORDER BY a.ID_ARTISTA")
                .param("idGenero", idGenero)
                .query(this::mapRow)
                .list().stream().map(this::cargarRelaciones).toList();
    }

    public List<Artista> findByNombreArtistico(String nombreArtistico) {
        return jdbcClient.sql(SELECT_ARTISTA + "WHERE a.NOMBRE_ARTISTICO = :nombreArtistico" + " ORDER BY a.ID_ARTISTA")
                .param("nombreArtistico", nombreArtistico)
                .query(this::mapRow)
                .list().stream().map(this::cargarRelaciones).toList();
    }

    private Artista mapRow(ResultSet rs, int fila) throws SQLException {
        var genero = referencia(rs, "ID_GENERO",
                id -> { var g = new Genero(); g.setIdGenero(id); return g; });
        return new Artista(rs.getLong("ID_ARTISTA"), rs.getString("NOMBRE_ARTISTICO"),
                rs.getString("PAIS"), genero, new ArrayList<>(), new ArrayList<>());
    }

    private Artista cargarRelaciones(Artista entidad) {
        entidad.setRedesSociales(new ArrayList<>(jdbcClient.sql(
                "SELECT RED_SOCIAL FROM ARTISTA_RED_SOCIAL WHERE ID_ARTISTA = :idArtista ORDER BY RED_SOCIAL")
                .param("idArtista", entidad.getIdArtista()).query(String.class).list()));
        entidad.setArtistaInvitados(new ArrayList<>(jdbcClient.sql(
                "SELECT a.ID_ARTISTA, a.NOMBRE_ARTISTICO, a.PAIS, a.ID_GENERO "
                + "FROM COLABORACION c JOIN ARTISTA a ON a.ID_ARTISTA = c.ARTISTA_INVITADO "
                + "WHERE c.ARTISTA_PRINCIPAL = :idArtista ORDER BY a.ID_ARTISTA")
                .param("idArtista", entidad.getIdArtista()).query(this::mapRow).list()));
        return entidad;
    }

    private static <T> T referencia(ResultSet rs, String columna, LongFunction<T> factory) throws SQLException {
        long id = rs.getLong(columna);
        return rs.wasNull() ? null : factory.apply(id);
    }
    
    // ---------- Escritura ----------

    public long nextId() {
        return jdbcClient.sql("SELECT SEQ_ARTISTA.NEXTVAL FROM DUAL").query(Long.class).single();
    }

    public Artista save(Artista artista) {
        artista.setIdArtista(nextId());
        jdbcClient.sql("INSERT INTO ARTISTA (ID_ARTISTA, NOMBRE_ARTISTICO, PAIS, ID_GENERO) "
                + "VALUES (:id, :nombre, :pais, :idGenero)")
                .param("id", artista.getIdArtista())
                .param("nombre", artista.getNombreArtistico())
                .param("pais", artista.getPais(), java.sql.Types.VARCHAR)
                .param("idGenero", artista.getGenero().getIdGenero())
                .update();
        return artista;
    }

    public void update(Artista artista) {
        jdbcClient.sql("UPDATE ARTISTA SET NOMBRE_ARTISTICO = :nombre, PAIS = :pais, ID_GENERO = :idGenero "
                + "WHERE ID_ARTISTA = :id")
                .param("id", artista.getIdArtista())
                .param("nombre", artista.getNombreArtistico())
                .param("pais", artista.getPais(), java.sql.Types.VARCHAR)
                .param("idGenero", artista.getGenero().getIdGenero())
                .update();
    }

    public void deleteById(long id) {
        jdbcClient.sql("DELETE FROM ARTISTA WHERE ID_ARTISTA = :id").param("id", id).update();
    }

    public void guardarRedSocial(long idArtista, String redSocial) {
        jdbcClient.sql("INSERT INTO ARTISTA_RED_SOCIAL (ID_ARTISTA, RED_SOCIAL) VALUES (:idArtista, :redSocial)")
                .param("idArtista", idArtista)
                .param("redSocial", redSocial)
                .update();
    }

    public void eliminarRedesSociales(long idArtista) {
        jdbcClient.sql("DELETE FROM ARTISTA_RED_SOCIAL WHERE ID_ARTISTA = :idArtista")
                .param("idArtista", idArtista)
                .update();
    }

    public void guardarColaboracion(long idPrincipal, long idInvitado) {
        jdbcClient.sql("INSERT INTO COLABORACION (ARTISTA_PRINCIPAL, ARTISTA_INVITADO) VALUES (:principal, :invitado)")
                .param("principal", idPrincipal)
                .param("invitado", idInvitado)
                .update();
    }

    public void eliminarInvitados(long idPrincipal) {
        jdbcClient.sql("DELETE FROM COLABORACION WHERE ARTISTA_PRINCIPAL = :id")
                .param("id", idPrincipal)
                .update();
    }

    public void eliminarColaboracionesDe(long idArtista) {
        jdbcClient.sql("DELETE FROM COLABORACION WHERE ARTISTA_PRINCIPAL = :id OR ARTISTA_INVITADO = :id")
                .param("id", idArtista)
                .update();
    }
}
