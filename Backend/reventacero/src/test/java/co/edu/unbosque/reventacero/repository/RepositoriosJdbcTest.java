package co.edu.unbosque.reventacero.repository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.simple.JdbcClient;

import co.edu.unbosque.reventacero.enums.TipoTransaccion;
import co.edu.unbosque.reventacero.enums.TipoUsuario;

/** Usa JdbcClient real y dobles JDBC: comprueba enlazado y mapeo sin una instancia Oracle. */
class RepositoriosJdbcTest {

    private final DataSource dataSource = mock(DataSource.class);
    private final Connection connection = mock(Connection.class);
    private final Deque<ResultSet> resultados = new ArrayDeque<>();
    private final List<String> consultas = new ArrayList<>();
    private final List<PreparedStatement> statements = new ArrayList<>();
    private final JdbcClient jdbc = JdbcClient.create(dataSource);

    @BeforeEach
    void prepararJdbc() throws Exception {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenAnswer(invocation -> {
            consultas.add(invocation.getArgument(0));
            var statement = mock(PreparedStatement.class);
            when(statement.executeQuery()).thenReturn(resultados.removeFirst());
            statements.add(statement);
            return statement;
        });
    }

    @Test
    void escenarioEnlazaAmbasPartesDeLaClaveSinInterpolarElNombre() throws Exception {
        String nombre = "Tarima ' OR '1'='1";
        resultados.add(filas(List.of(Map.of("ID_FESTIVAL", 55L, "NOMBRE_ESCENARIO", nombre, "CAPACIDAD", 450))));
        var escenario = new EscenarioRepository(jdbc).findById(55, nombre).orElseThrow();
        assertEquals(55, escenario.getFestival().getIdFestival());
        assertEquals(nombre, escenario.getNombreEscenario());
        assertEquals(450, escenario.getCapacidad());
        assertTrue(consultas.getFirst().contains("e.ID_FESTIVAL = ? AND e.NOMBRE_ESCENARIO = ?"));
        assertFalse(consultas.getFirst().contains(nombre));
        verify(statements.getFirst()).setString(2, nombre);
    }

    @Test
    void inscripcionConservaClaveCompuestaYFechaDeSolicitud() throws Exception {
        var fecha = LocalDate.of(2026, 11, 1);
        resultados.add(filas(List.of(Map.of("DOCUMENTO", 1001L, "ID_LISTA", 7L,
                "FECHA_SOLICITUD", java.sql.Date.valueOf(fecha), "ESTADO", "PENDIENTE"))));
        var inscripcion = new InscripcionRepository(jdbc).findById(1001, 7).orElseThrow();
        assertEquals(1001, inscripcion.getUsuario().getDocumento());
        assertEquals(7, inscripcion.getListaEspera().getIdLista());
        assertEquals(fecha, inscripcion.getFechaSolicitud());
        assertTrue(consultas.getFirst().contains("i.DOCUMENTO = ? AND i.ID_LISTA = ?"));
    }

    @Test
    void presentacionConservaHorasYLaClaveCompletaDelEscenario() throws Exception {
        var inicio = LocalDateTime.of(2026, 11, 1, 23, 30);
        var fin = inicio.plusHours(2);
        resultados.add(filas(List.of(Map.of("ID_PRESENTACION", 1L,
                "FECHA", java.sql.Date.valueOf(inicio.toLocalDate()),
                "HORA_INICIO", Timestamp.valueOf(inicio), "HORA_FIN", Timestamp.valueOf(fin),
                "ESCENARIO_FESTIVAL_ID", 55L, "ESCENARIO_NOMBRE", "Principal", "ID_ARTISTA", 9L))));
        var presentacion = new PresentacionRepository(jdbc).findById(1).orElseThrow();
        assertEquals(inicio, presentacion.getHoraInicio());
        assertEquals(fin, presentacion.getHoraFin());
        assertEquals(55, presentacion.getEscenario().getFestival().getIdFestival());
        assertEquals("Principal", presentacion.getEscenario().getNombreEscenario());
        assertEquals(9, presentacion.getArtista().getIdArtista());
    }

    @Test
    void reventaCargaDatosHeredadosYLosDosUsuariosSinRedondearImportes() throws Exception {
        var precio = new BigDecimal("123456789.99");
        var comision = new BigDecimal("1234.56");
        resultados.add(filas(List.of(Map.of("ID_TRANSACCION", 8L, "TIPO_TRANSACCION", "REVENTA_AUTORIZADA",
                "PRECIO", precio, "COMISION_ORGANIZADOR", comision, "CODIGO_BOLETA", 17L,
                "ID_DISPOSITIVO", 4L, "VENDEDOR_DOCUMENTO", 1001L, "COMPRADOR_DOCUMENTO", 1002L))));
        var reventa = new ReventaAutorizadaRepository(jdbc).findById(8).orElseThrow();
        assertEquals(TipoTransaccion.REVENTA_AUTORIZADA, reventa.getTipoTransaccion());
        assertEquals(precio, reventa.getPrecio());
        assertEquals(comision, reventa.getComisionOrganizador());
        assertEquals(1001, reventa.getVendedor().getDocumento());
        assertEquals(1002, reventa.getComprador().getDocumento());
        assertEquals(4, reventa.getDispositivo().getIdDispositivo());
        assertEquals(17, reventa.getBoleta().getCodigoBoleta());
        assertTrue(consultas.getFirst().contains("JOIN TRANSACCION"));
    }

    @Test
    void solicitudDeReventaTraeLosDatosQueVeElReceptor() throws Exception {
        var precio = new BigDecimal("25000.50");
        resultados.add(filas(List.of(Map.of("ID_SOLICITUD", 3L, "PRECIO", precio, "ESTADO", "PENDIENTE",
                "CODIGO_BOLETA", 17L, "VENDEDOR_DOCUMENTO", 1001L, "COMPRADOR_DOCUMENTO", 1002L,
                "NOMBRE_VENDEDOR", "Ana Prueba", "NOMBRE_FESTIVAL", "Fiesta",
                "CATEGORIA_ENTRADA", "VIP", "NOMBRE_ZONA", "Sofa"))));
        var solicitud = new SolicitudReventaRepository(jdbc).findById(3).orElseThrow();
        assertEquals(precio, solicitud.getPrecio());
        assertEquals("PENDIENTE", solicitud.getEstado());
        assertEquals(1002, solicitud.getCompradorDocumento());
        assertEquals("Ana Prueba", solicitud.getNombreVendedor());
        assertEquals("Fiesta", solicitud.getNombreFestival());
        assertEquals("VIP", solicitud.getCategoriaEntrada());
        assertEquals("Sofa", solicitud.getNombreZona());
    }

    @Test
    void administradorCargaUsuarioTelefonosYRelacionOpcionalNula() throws Exception {
        resultados.add(filas(List.of(Map.of("DOCUMENTO", 1001L, "NOMBRE_USUARIO", "admin-ficticio",
                "NOMBRES", "Ana", "APELLIDOS", "Prueba", "CORREO", "admin@example.test",
                "TIPO_USUARIO", "ADMINISTRADOR", "NIVEL_ACCESO", "ACADEMICO"))));
        resultados.add(filas(List.of(Map.of("TELEFONO", "3000000001"), Map.of("TELEFONO", "3000000002"))));
        var administrador = new AdministradorRepository(jdbc).findById(1001).orElseThrow();
        assertEquals(TipoUsuario.ADMINISTRADOR, administrador.getTipoUsuario());
        assertEquals("Ana", administrador.getNombreCompleto().getNombre());
        assertEquals("admin-ficticio", administrador.getNombreUsuario());
        assertEquals("ACADEMICO", administrador.getNivelAcceso());
        assertEquals(List.of("3000000001", "3000000002"), administrador.getTelefono());
        assertNull(administrador.getAutorizadoPor());
        assertNull(administrador.getFechaNacimiento());
    }

    @Test
    void artistaCargaMultivaluadosYColaboracionesSinRecursion() throws Exception {
        resultados.add(filas(List.of(Map.of("ID_ARTISTA", 1L, "NOMBRE_ARTISTICO", "Principal", "ID_GENERO", 3L))));
        resultados.add(filas(List.of(Map.of("RED_SOCIAL", "https://example.test/artista"))));
        resultados.add(filas(List.of(Map.of("ID_ARTISTA", 2L, "NOMBRE_ARTISTICO", "Invitado", "ID_GENERO", 3L))));
        var artista = new ArtistaRepository(jdbc).findById(1).orElseThrow();
        assertEquals(List.of("https://example.test/artista"), artista.getRedesSociales());
        assertEquals(2, artista.getArtistaInvitados().getFirst().getIdArtista());
        assertEquals("Invitado", artista.getArtistaInvitados().getFirst().getNombreArtistico());
        assertEquals(3, consultas.size());
    }

    @Test
    void generoSinPadreNoSeConvierteEnUnaReferenciaConIdCero() throws Exception {
        resultados.add(filas(List.of(Map.of("ID_GENERO", 1L, "NOMBRE", "Rock"))));
        assertNull(new GeneroRepository(jdbc).findById(1).orElseThrow().getGeneroPrincipal());
    }

    @Test
    void devolucionYVentaConservanAtributosDelSubtipo() throws Exception {
        resultados.add(filas(List.of(Map.of("ID_TRANSACCION", 1L, "TIPO_TRANSACCION", "DEVOLUCION",
                "MOTIVO", "Prueba", "PORCENTAJE_REEMBOLSO", new BigDecimal("95.50")))));
        var devolucion = new DevolucionRepository(jdbc).findById(1).orElseThrow();
        assertEquals("Prueba", devolucion.getMotivo());
        assertEquals(new BigDecimal("95.50"), devolucion.getPorcentajeReembolso());
        resultados.add(filas(List.of(Map.of("ID_TRANSACCION", 2L, "TIPO_TRANSACCION", "VENTA_PRIMARIA", "CANAL_VENTA", "TAQUILLA"))));
        assertEquals("TAQUILLA", new VentaPrimariaRepository(jdbc).findById(2).orElseThrow().getCanalVena());
    }

    @Test
    void notificacionConservaLasDosClavesDeLaInscripcion() throws Exception {
        resultados.add(filas(List.of(Map.of("ID_NOTIFICACION", 1L, "USUARIO_DOCUMENTO", 1001L, "ID_LISTA", 5L))));
        var notificacion = new NotificacionRepository(jdbc).findById(1).orElseThrow();
        assertEquals(1001, notificacion.getInscripcion().getUsuario().getDocumento());
        assertEquals(5, notificacion.getInscripcion().getListaEspera().getIdLista());
    }

    @Test
    void consultaIndividualDistingueAusenciaYResultadosDuplicados() throws Exception {
        var repository = new EscenarioRepository(jdbc);
        resultados.add(filas(List.of()));
        assertTrue(repository.findById(1, "Principal").isEmpty());
        var row = Map.<String, Object>of("ID_FESTIVAL", 1L, "NOMBRE_ESCENARIO", "Principal");
        resultados.add(filas(List.of(row, row)));
        assertThrows(IncorrectResultSizeDataAccessException.class, () -> repository.findById(1, "Principal"));
    }

    @Test
    void falloJdbcNoSeDisfrazaDeUnaListaVacia() throws Exception {
        doThrow(new SQLException("Fallo JDBC de prueba")).when(connection).prepareStatement(anyString());
        assertThrows(DataAccessException.class, () -> new FestivalRepository(jdbc).findAll());
    }

    private ResultSet filas(List<Map<String, Object>> rows) throws Exception {
        var rs = mock(ResultSet.class);
        var indice = new AtomicInteger(-1);
        var nulo = new AtomicBoolean();
        when(rs.next()).thenAnswer(invocation -> indice.incrementAndGet() < rows.size());
        java.util.function.Function<String, Object> leer = columna -> {
            Object valor = rows.get(indice.get()).get(columna);
            nulo.set(valor == null);
            return valor;
        };
        when(rs.wasNull()).thenAnswer(invocation -> nulo.get());
        when(rs.getLong(anyString())).thenAnswer(invocation -> {
            var valor = (Number) leer.apply(invocation.getArgument(0));
            return valor == null ? 0L : valor.longValue();
        });
        when(rs.getInt(anyString())).thenAnswer(invocation -> {
            var valor = (Number) leer.apply(invocation.getArgument(0));
            return valor == null ? 0 : valor.intValue();
        });
        when(rs.getString(anyString())).thenAnswer(invocation -> (String) leer.apply(invocation.getArgument(0)));
        when(rs.getDate(anyString())).thenAnswer(invocation -> (java.sql.Date) leer.apply(invocation.getArgument(0)));
        when(rs.getTimestamp(anyString())).thenAnswer(invocation -> (Timestamp) leer.apply(invocation.getArgument(0)));
        when(rs.getBigDecimal(anyString())).thenAnswer(invocation -> (BigDecimal) leer.apply(invocation.getArgument(0)));
        var metadata = mock(ResultSetMetaData.class);
        when(metadata.getColumnCount()).thenReturn(1);
        when(metadata.getColumnType(1)).thenReturn(Types.VARCHAR);
        when(rs.getMetaData()).thenReturn(metadata);
        when(rs.getString(1)).thenAnswer(invocation -> (String) rows.get(indice.get()).values().iterator().next());
        return rs;
    }
}
