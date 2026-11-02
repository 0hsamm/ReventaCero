package co.edu.unbosque.reventacero.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.function.LongFunction;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.reventacero.entity.Festival;
import co.edu.unbosque.reventacero.entity.Zona;

/**
 * Acceso JDBC de solo lectura para Zona. RF-02, RF-19; Fase 2, semana 11.
 * Las relaciones contienen sus claves; no se cargan recursivamente.
 */
@Repository
public class ZonaRepository {

    private static final String SELECT_ZONA = 
            "SELECT z.ID_ZONA, z.NOMBRE, z.TIPO_ZONA, z.AFORO_MAXIMO, z.ID_FESTIVAL"
             + " FROM ZONA z ";

    private final JdbcClient jdbcClient;

    public ZonaRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<Zona> findAll() {
        return jdbcClient.sql(SELECT_ZONA + "ORDER BY z.ID_ZONA")
                .query(this::mapRow)
                .list();
    }

    public Optional<Zona> findById(long id) {
        return jdbcClient.sql(SELECT_ZONA + "WHERE z.ID_ZONA = :id")
                .param("id", id)
                .query(this::mapRow)
                .optional();
    }

    public boolean existsById(long id) {
        return jdbcClient.sql("SELECT COUNT(*) FROM ZONA z WHERE z.ID_ZONA = :id")
                .param("id", id)
                .query(Integer.class).single() > 0;
    }

    public long count() {
        return jdbcClient.sql("SELECT COUNT(*) FROM ZONA z").query(Long.class).single();
    }

    public List<Zona> findByFestivalId(long idFestival) {
        return jdbcClient.sql(SELECT_ZONA + "WHERE z.ID_FESTIVAL = :idFestival" + " ORDER BY z.ID_ZONA")
                .param("idFestival", idFestival)
                .query(this::mapRow)
                .list();
    }

    private Zona mapRow(ResultSet rs, int fila) throws SQLException {
        return new Zona(rs.getLong("ID_ZONA"), rs.getString("NOMBRE"), rs.getString("TIPO_ZONA"),
                rs.getInt("AFORO_MAXIMO"), festivalRef(rs, "ID_FESTIVAL"));
    }

    private static Festival festivalRef(ResultSet rs, String columna) throws SQLException {
        return referencia(rs, columna, id -> { var e = new Festival(); e.setIdFestival(id); return e; });
    }

    private static <T> T referencia(ResultSet rs, String columna, LongFunction<T> factory) throws SQLException {
        long id = rs.getLong(columna);
        return rs.wasNull() ? null : factory.apply(id);
    }
    
    public long nextId() {
        return jdbcClient.sql("SELECT SEQ_ZONA.NEXTVAL FROM DUAL").query(Long.class).single();
    }

    public Zona save(Zona zona) {
        zona.setIdZona(nextId());
        jdbcClient.sql("INSERT INTO ZONA (ID_ZONA, NOMBRE, TIPO_ZONA, AFORO_MAXIMO, ID_FESTIVAL) "
                + "VALUES (:id, :nombre, :tipoZona, :aforo, :idFestival)")
                .param("id", zona.getIdZona())
                .param("nombre", zona.getNombre())
                .param("tipoZona", zona.getTipoZona(), java.sql.Types.VARCHAR)
                .param("aforo", zona.getAforoMaximo())
                .param("idFestival", zona.getFestival().getIdFestival())
                .update();
        return zona;
    }

    public void update(Zona zona) {
        jdbcClient.sql("UPDATE ZONA SET NOMBRE = :nombre, TIPO_ZONA = :tipoZona, AFORO_MAXIMO = :aforo, "
                + "ID_FESTIVAL = :idFestival WHERE ID_ZONA = :id")
                .param("id", zona.getIdZona())
                .param("nombre", zona.getNombre())
                .param("tipoZona", zona.getTipoZona(), java.sql.Types.VARCHAR)
                .param("aforo", zona.getAforoMaximo())
                .param("idFestival", zona.getFestival().getIdFestival())
                .update();
    }

    public void deleteById(long id) {
        jdbcClient.sql("DELETE FROM ZONA WHERE ID_ZONA = :id").param("id", id).update();
    }

    public int countBoletasEmitidas(long idZona) {
        return jdbcClient.sql("SELECT COUNT(*) FROM BOLETA b JOIN TIPO_ENTRADA te "
                + "ON te.ID_TIPO_ENTRADA = b.ID_TIPO_ENTRADA WHERE te.ID_ZONA = :idZona "
                + "AND NOT EXISTS (SELECT 1 FROM TRANSACCION t WHERE t.CODIGO_BOLETA = b.CODIGO_BOLETA "
                + "AND t.TIPO_TRANSACCION = 'DEVOLUCION')")
                .param("idZona", idZona)
                .query(Integer.class).single();
    }

    public void bloquearPorId(long idZona) {
        jdbcClient.sql("SELECT ID_ZONA FROM ZONA WHERE ID_ZONA = :idZona FOR UPDATE")
                .param("idZona", idZona)
                .query(Long.class)
                .optional();
    }
}
