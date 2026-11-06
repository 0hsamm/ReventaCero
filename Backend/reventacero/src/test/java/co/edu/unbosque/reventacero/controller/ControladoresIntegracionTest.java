package co.edu.unbosque.reventacero.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import co.edu.unbosque.reventacero.dto.*;
import co.edu.unbosque.reventacero.entity.Usuario;
import co.edu.unbosque.reventacero.enums.TipoUsuario;
import co.edu.unbosque.reventacero.exception.RecursoNoEncontradoException;
import co.edu.unbosque.reventacero.exception.ReglaNegocioException;
import co.edu.unbosque.reventacero.security.JwtAuthenticationFilter;
import co.edu.unbosque.reventacero.security.JwtUtil;
import co.edu.unbosque.reventacero.service.BoletaQrService;
import co.edu.unbosque.reventacero.security.SecurityConfig;
import co.edu.unbosque.reventacero.service.*;

@WebMvcTest(controllers = {
        GeneroController.class,
        ArtistaController.class,
        PatrocinadorController.class,
        AdministradorController.class,
        OrganizadorController.class,
        FestivalController.class,
        ZonaController.class,
        PatrocinioController.class,
        TipoEntradaController.class,
        EscenarioController.class,
        PresentacionController.class,
        UsuarioController.class,
        DispositivoController.class,
        BoletaController.class,
        TransaccionController.class,
        VentaPrimariaController.class,
        DevolucionController.class,
        ReventaAutorizadaController.class,
        PublicacionReventaController.class,
        ListaEsperaController.class,
        InscripcionController.class,
        NotificacionController.class,
        ControlAccesoController.class
})
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtUtil.class, ManejadorExcepcionesController.class})
@TestPropertySource(properties = {
        "jwt.secret=secreto-de-pruebas-de-controladores-con-mas-de-32-caracteres",
        "app.frontend.allowed-origins=http://localhost:4200"
})
class ControladoresIntegracionTest {

    @Autowired private MockMvc mvc;
    @MockitoBean private UserDetailsService userDetailsService;
    @MockitoBean private GeneroService generoService;
    @MockitoBean private ArtistaService artistaService;
    @MockitoBean private PatrocinadorService patrocinadorService;
    @MockitoBean private AdministradorService administradorService;
    @MockitoBean private OrganizadorService organizadorService;
    @MockitoBean private FestivalService festivalService;
    @MockitoBean private ZonaService zonaService;
    @MockitoBean private PatrocinioService patrocinioService;
    @MockitoBean private TipoEntradaService tipoEntradaService;
    @MockitoBean private EscenarioService escenarioService;
    @MockitoBean private PresentacionService presentacionService;
    @MockitoBean private UsuarioService usuarioService;
    @MockitoBean private DispositivoService dispositivoService;
    @MockitoBean private BoletaService boletaService;
    @MockitoBean private BoletaQrService boletaQrService;
    @MockitoBean private TransaccionService transaccionService;
    @MockitoBean private VentaPrimariaService ventaPrimariaService;
    @MockitoBean private DevolucionService devolucionService;
    @MockitoBean private ReventaAutorizadaService reventaAutorizadaService;
    @MockitoBean private PublicacionReventaService publicacionReventaService;
    @MockitoBean private ListaEsperaService listaEsperaService;
    @MockitoBean private InscripcionService inscripcionService;
    @MockitoBean private NotificacionService notificacionService;
    @MockitoBean private ControlAccesoService controlAccesoService;

    private Usuario administrador;
    private Usuario organizador;
    private Usuario asistente;

    @BeforeEach
    void preparar() {
        administrador = usuario(1, TipoUsuario.ADMINISTRADOR);
        organizador = usuario(10, TipoUsuario.ORGANIZADOR);
        asistente = usuario(20, TipoUsuario.USUARIO);
        when(zonaService.buscarPorId(anyLong())).thenReturn(new ZonaDTO());
        when(tipoEntradaService.buscarPorId(anyLong())).thenReturn(new TipoEntradaDTO());
        when(patrocinioService.buscarPorId(anyLong())).thenReturn(new PatrocinioDTO());
        when(presentacionService.buscarPorId(anyLong())).thenReturn(new PresentacionDTO());
        when(dispositivoService.buscarPorId(anyLong())).thenReturn(new DispositivoDTO());
        when(listaEsperaService.buscarPorId(anyLong())).thenReturn(new ListaEsperaDTO());
        var acceso = new ControlAccesoDTO();
        acceso.setResultado(ControlAccesoService.PERMITIDO);
        when(controlAccesoService.registrarIngreso(anyLong())).thenReturn(acceso);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/administradores", "/organizadores", "/generos", "/artistas", "/patrocinadores", "/festivales", "/zonas", "/patrocinios", "/tipos-entrada", "/escenarios", "/presentaciones", "/usuarios", "/dispositivos", "/boletas", "/transacciones", "/ventas-primarias", "/devoluciones", "/reventas-autorizadas", "/listas-espera", "/inscripciones", "/notificaciones", "/controles-acceso"})
    void todosLosControladoresExponenSuListado(String ruta) throws Exception {
        mvc.perform(get(ruta).with(user(administrador))).andExpect(status().isOk());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/administradores", "/organizadores", "/generos", "/artistas", "/patrocinadores", "/festivales", "/zonas", "/patrocinios", "/tipos-entrada", "/escenarios", "/presentaciones", "/dispositivos", "/ventas-primarias", "/devoluciones", "/listas-espera", "/inscripciones", "/controles-acceso/boleta/1"})
    void operacionesDeCreacionResponden201(String ruta) throws Exception {
        mvc.perform(post(ruta).with(user(administrador))
                .contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isCreated());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/administradores/1", "/organizadores/1", "/generos/1", "/artistas/1", "/patrocinadores/1", "/festivales/1", "/zonas/1", "/patrocinios/1", "/tipos-entrada/1", "/escenarios/1/Principal", "/presentaciones/1", "/usuarios/1", "/dispositivos/1"})
    void operacionesDeActualizacionResponden200(String ruta) throws Exception {
        mvc.perform(put(ruta).with(user(administrador))
                .contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isOk());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/administradores/1", "/organizadores/1", "/generos/1", "/artistas/1", "/patrocinadores/1", "/festivales/1", "/zonas/1", "/patrocinios/1", "/tipos-entrada/1", "/escenarios/1/Principal", "/presentaciones/1", "/usuarios/1", "/dispositivos/1", "/listas-espera/1", "/inscripciones/1/2"})
    void operacionesDeEliminacionResponden204(String ruta) throws Exception {
        mvc.perform(delete(ruta).with(user(administrador))).andExpect(status().isNoContent());
    }

    @Test
    void crearFestivalUtilizaElOrganizadorAutenticado() throws Exception {
        mvc.perform(post("/festivales").with(user(organizador)).contentType(MediaType.APPLICATION_JSON)
                .content("{\"documentoOrganizador\":999}")).andExpect(status().isCreated());
        var captor = ArgumentCaptor.forClass(FestivalDTO.class);
        verify(festivalService).crear(captor.capture());
        assertEquals(organizador.getDocumento(), captor.getValue().getDocumentoOrganizador());
    }

    @Test
    void organizadorNoPuedeActualizarFestivalAjeno() throws Exception {
        var festival = new FestivalDTO();
        festival.setDocumentoOrganizador(99);
        when(festivalService.buscarPorId(5)).thenReturn(festival);
        mvc.perform(put("/festivales/5").with(user(organizador)).contentType(MediaType.APPLICATION_JSON)
                .content("{}")).andExpect(status().isForbidden());
        verify(festivalService, never()).actualizar(anyLong(), any());
    }

    @Test
    void organizadorNoPuedeMoverUnaZonaAHaciaUnFestivalAjeno() throws Exception {
        var zona = new ZonaDTO();
        zona.setIdFestival(5);
        when(zonaService.buscarPorId(1)).thenReturn(zona);
        var propio = new FestivalDTO();
        propio.setDocumentoOrganizador(10);
        var ajeno = new FestivalDTO();
        ajeno.setDocumentoOrganizador(99);
        when(festivalService.buscarPorId(5)).thenReturn(propio);
        when(festivalService.buscarPorId(6)).thenReturn(ajeno);
        mvc.perform(put("/zonas/1").with(user(organizador)).contentType(MediaType.APPLICATION_JSON)
                .content("{\"idFestival\":6}")).andExpect(status().isForbidden());
        verify(zonaService, never()).actualizar(anyLong(), any());
    }

    @Test
    void dispositivoNuevoSeVinculaAlUsuarioAutenticado() throws Exception {
        mvc.perform(post("/dispositivos").with(user(asistente)).contentType(MediaType.APPLICATION_JSON)
                .content("{\"documentoUsuario\":999}")).andExpect(status().isCreated());
        var captor = ArgumentCaptor.forClass(DispositivoDTO.class);
        verify(dispositivoService).crear(captor.capture());
        assertEquals(asistente.getDocumento(), captor.getValue().getDocumentoUsuario());
    }

    @Test
    void dispositivoAjenoNoPuedeEliminarse() throws Exception {
        var dispositivo = new DispositivoDTO();
        dispositivo.setDocumentoUsuario(99);
        when(dispositivoService.buscarPorId(1)).thenReturn(dispositivo);
        mvc.perform(delete("/dispositivos/1").with(user(asistente))).andExpect(status().isForbidden());
        verify(dispositivoService, never()).eliminar(anyLong());
    }

    @Test
    void noSeCompraConElDispositivoDeOtraCuenta() throws Exception {
        var dispositivo = new DispositivoDTO();
        dispositivo.setDocumentoUsuario(99);
        when(dispositivoService.buscarPorId(1)).thenReturn(dispositivo);
        mvc.perform(post("/ventas-primarias").with(user(asistente)).contentType(MediaType.APPLICATION_JSON)
                .content("{\"idDispositivo\":1}")).andExpect(status().isForbidden());
        verify(ventaPrimariaService, never()).comprar(any());
    }

    @Test
    void reventaDeBoletaAjenaEsRechazada() throws Exception {
        var boleta = new BoletaDTO();
        boleta.setDocumentoComprador(99);
        when(boletaService.buscarPorCodigo(1)).thenReturn(boleta);
        mvc.perform(post("/publicaciones-reventa").with(user(asistente)).contentType(MediaType.APPLICATION_JSON)
                .content("{\"codigoBoleta\":1,\"precio\":10}")).andExpect(status().isForbidden());
        verify(publicacionReventaService, never()).publicar(anyLong(), any());
    }

    @Test
    void elDuenoPublicaSuBoleta() throws Exception {
        var boleta = new BoletaDTO();
        boleta.setDocumentoComprador(asistente.getDocumento());
        when(boletaService.buscarPorCodigo(1)).thenReturn(boleta);
        mvc.perform(post("/publicaciones-reventa").with(user(asistente)).contentType(MediaType.APPLICATION_JSON)
                .content("{\"codigoBoleta\":1,\"precio\":10}")).andExpect(status().isCreated());
        verify(publicacionReventaService).publicar(1, new java.math.BigDecimal("10"));
    }

    @Test
    void noExisteLaReventaDirectaSinCompradorQueLaCompre() throws Exception {
        mvc.perform(post("/reventas-autorizadas").with(user(administrador)).contentType(MediaType.APPLICATION_JSON)
                .content("{\"codigoBoleta\":1}")).andExpect(status().is4xxClientError());
        verify(reventaAutorizadaService, never()).revender(any());
    }

    @Test
    void compraUnaPublicacionConLaCuentaAutenticada() throws Exception {
        mvc.perform(put("/publicaciones-reventa/40/comprar").with(user(asistente)).contentType(MediaType.APPLICATION_JSON)
                .content("{\"idDispositivo\":7,\"metodoPago\":\"PSE\"}")).andExpect(status().isNoContent());
        verify(publicacionReventaService).comprar(40, asistente.getDocumento(), 7, "PSE");
    }

    @Test
    void soloElUsuarioNormalCompraReventas() throws Exception {
        mvc.perform(put("/publicaciones-reventa/40/comprar").with(user(organizador)).contentType(MediaType.APPLICATION_JSON)
                .content("{\"idDispositivo\":7,\"metodoPago\":\"PSE\"}")).andExpect(status().isForbidden());
        verify(publicacionReventaService, never()).comprar(anyLong(), anyLong(), anyLong(), any());
    }

    @Test
    void soloElDuenoRetiraSuPublicacion() throws Exception {
        var boleta = new BoletaDTO();
        boleta.setDocumentoComprador(99);
        when(boletaService.buscarPorCodigo(1)).thenReturn(boleta);
        mvc.perform(put("/publicaciones-reventa/boleta/1/retirar").with(user(asistente))).andExpect(status().isForbidden());
        verify(publicacionReventaService, never()).retirar(anyLong());
    }

    @Test
    void inscripcionNuevaUsaLaIdentidadAutenticada() throws Exception {
        mvc.perform(post("/inscripciones").with(user(asistente)).contentType(MediaType.APPLICATION_JSON)
                .content("{\"documento\":999}")).andExpect(status().isCreated());
        var captor = ArgumentCaptor.forClass(InscripcionDTO.class);
        verify(inscripcionService).inscribir(captor.capture());
        assertEquals(asistente.getDocumento(), captor.getValue().getDocumento());
    }

    @Test
    void noSeCancelaLaInscripcionDeOtraCuenta() throws Exception {
        mvc.perform(delete("/inscripciones/99/2").with(user(asistente))).andExpect(status().isForbidden());
        verify(inscripcionService, never()).cancelar(anyLong(), anyLong());
    }

    @Test
    void notificacionesAjenasNoSeExponen() throws Exception {
        var notificacion = new NotificacionDTO();
        notificacion.setUsuarioDocumento(99);
        when(notificacionService.buscarPorId(1)).thenReturn(notificacion);
        mvc.perform(get("/notificaciones/1").with(user(asistente))).andExpect(status().isForbidden());
    }

    @Test
    void clavesCompuestasConservanLosDosParametros() throws Exception {
        mvc.perform(get("/escenarios/{festival}/{nombre}", 5, "Tarima Norte").with(user(administrador)))
                .andExpect(status().isOk());
        verify(escenarioService).buscar(5, "Tarima Norte");
        mvc.perform(get("/inscripciones/20/7").with(user(asistente))).andExpect(status().isOk());
        verify(inscripcionService).buscar(20, 7);
    }

    @Test
    void fechaDeProgramacionSeInterpretaComoFechaIso() throws Exception {
        mvc.perform(get("/presentaciones/fecha/2026-11-02").with(user(asistente))).andExpect(status().isOk());
        verify(presentacionService).listarPorFecha(java.time.LocalDate.of(2026, 11, 2));
        mvc.perform(get("/presentaciones/fecha/no-es-fecha").with(user(asistente)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void excepcionesDelServicioTienenRespuestaHttp() throws Exception {
        when(festivalService.buscarPorId(1)).thenThrow(new RecursoNoEncontradoException("Festival inexistente"));
        mvc.perform(get("/festivales/1")).andExpect(status().isNotFound());
        when(generoService.crear(any())).thenThrow(new ReglaNegocioException("Genero duplicado"));
        mvc.perform(post("/generos").with(user(administrador)).contentType(MediaType.APPLICATION_JSON)
                .content("{}")).andExpect(status().isConflict());
        doThrow(new IllegalArgumentException("Nombre requerido")).when(generoService).crear(any());
        mvc.perform(post("/generos").with(user(administrador)).contentType(MediaType.APPLICATION_JSON)
                .content("{}")).andExpect(status().isBadRequest());
    }

    @Test
    void rolesYAutenticacionProtegenLasOperaciones() throws Exception {
        mvc.perform(get("/festivales")).andExpect(status().isOk());
        mvc.perform(get("/boletas")).andExpect(status().isUnauthorized());
        mvc.perform(get("/boletas").with(user(asistente))).andExpect(status().isForbidden());
        mvc.perform(post("/generos").with(user(asistente)).contentType(MediaType.APPLICATION_JSON)
                .content("{}")).andExpect(status().isForbidden());
        mvc.perform(post("/administradores").with(user(organizador)).contentType(MediaType.APPLICATION_JSON)
                .content("{}")).andExpect(status().isForbidden());
        mvc.perform(post("/festivales").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void organizadorPuedeActualizarSuFestivalSinAsignarloAOtraCuenta() throws Exception {
        var festival = new FestivalDTO();
        festival.setDocumentoOrganizador(organizador.getDocumento());
        when(festivalService.buscarPorId(5)).thenReturn(festival);
        mvc.perform(put("/festivales/5").with(user(organizador)).contentType(MediaType.APPLICATION_JSON)
                .content("{\"documentoOrganizador\":999}")).andExpect(status().isOk());
        var captor = ArgumentCaptor.forClass(FestivalDTO.class);
        verify(festivalService).actualizar(eq(5L), captor.capture());
        assertEquals(organizador.getDocumento(), captor.getValue().getDocumentoOrganizador());
    }

    @Test
    void compraConDispositivoPropioLlegaAlServicio() throws Exception {
        var dispositivo = new DispositivoDTO();
        dispositivo.setDocumentoUsuario(asistente.getDocumento());
        when(dispositivoService.buscarPorId(1)).thenReturn(dispositivo);
        mvc.perform(post("/ventas-primarias").with(user(asistente)).contentType(MediaType.APPLICATION_JSON)
                .content("{\"idDispositivo\":1}")).andExpect(status().isCreated());
        verify(ventaPrimariaService).comprar(any(VentaPrimariaDTO.class));
    }

    @Test
    void ingresoDenegadoNoSePresentaComoIngresoAutorizado() throws Exception {
        var acceso = new ControlAccesoDTO();
        acceso.setResultado(ControlAccesoService.DENEGADO);
        when(controlAccesoService.registrarIngreso(1)).thenReturn(acceso);
        mvc.perform(post("/controles-acceso/boleta/1").with(user(administrador)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.resultado").value(ControlAccesoService.DENEGADO));
    }

    @Test
    void cambioDeEstadoExigeValorExplicito() throws Exception {
        mvc.perform(put("/dispositivos/1/estado").with(user(administrador))
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        verify(dispositivoService, never()).cambiarEstado(anyLong(), anyBoolean());
        mvc.perform(put("/dispositivos/1/estado").with(user(administrador))
                .contentType(MediaType.APPLICATION_JSON).content("{\"activo\":false}"))
                .andExpect(status().isOk());
        verify(dispositivoService).cambiarEstado(1, false);
    }

    @Test
    void administracionDeCuentaDelegadaSinDevolverContrasenias() throws Exception {
        mvc.perform(put("/usuarios/1/estado").with(user(administrador))
                .contentType(MediaType.APPLICATION_JSON).content("{\"estado\":\"ACTIVA\"}"))
                .andExpect(status().isOk());
        verify(usuarioService).cambiarEstado(1, "ACTIVA");
        mvc.perform(put("/usuarios/1/contrasenia").with(user(administrador))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"contraseniaActual\":\"anterior-ficticia\",\"contraseniaNueva\":\"nueva-ficticia\"}"))
                .andExpect(status().isNoContent()).andExpect(content().string(""));
        verify(usuarioService).cambiarContrasenia(1, "anterior-ficticia", "nueva-ficticia");
    }

    @Test
    void erroresDeIntegridadNoExponenSqlInterno() throws Exception {
        when(generoService.crear(any())).thenThrow(
                new org.springframework.dao.DataIntegrityViolationException("SQL y datos internos de prueba"));
        mvc.perform(post("/generos").with(user(administrador)).contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isConflict())
                .andExpect(content().string("La operacion entra en conflicto con los datos existentes"));
    }

    @Test
    void elDuenoObtieneElQrDeSuBoletaComoSvgYTexto() throws Exception {
        var boleta = new BoletaDTO();
        boleta.setDocumentoComprador(asistente.getDocumento());
        when(boletaService.buscarPorCodigo(7)).thenReturn(boleta);
        when(boletaQrService.svg(7)).thenReturn("<svg xmlns=\"http://www.w3.org/2000/svg\"></svg>");
        when(boletaQrService.contenido(7)).thenReturn("Número de boleta: 7");

        mvc.perform(get("/boletas/7/qr").with(user(asistente)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", org.hamcrest.Matchers.startsWith("image/svg+xml")))
                .andExpect(header().string("Cache-Control", org.hamcrest.Matchers.containsString("no-store")));
        mvc.perform(get("/boletas/7/qr/texto").with(user(asistente)))
                .andExpect(status().isOk())
                .andExpect(content().string("Número de boleta: 7"));
    }

    @Test
    void elQrDeUnaBoletaAjenaEsRechazado() throws Exception {
        var boleta = new BoletaDTO();
        boleta.setDocumentoComprador(99);
        when(boletaService.buscarPorCodigo(7)).thenReturn(boleta);

        mvc.perform(get("/boletas/7/qr").with(user(asistente))).andExpect(status().isForbidden());
        mvc.perform(get("/boletas/7/qr/texto").with(user(asistente))).andExpect(status().isForbidden());
        verify(boletaQrService, never()).svg(anyLong());
        verify(boletaQrService, never()).contenido(anyLong());
    }

    @Test
    void elAdministradorPuedeVerElQrDeCualquierBoleta() throws Exception {
        var boleta = new BoletaDTO();
        boleta.setDocumentoComprador(99);
        when(boletaService.buscarPorCodigo(7)).thenReturn(boleta);
        when(boletaQrService.svg(7)).thenReturn("<svg></svg>");

        mvc.perform(get("/boletas/7/qr").with(user(administrador))).andExpect(status().isOk());
    }

    @Test
    void elCatalogoDeBoletasEsPublicoPeroSuGestionNo() throws Exception {
        mvc.perform(get("/zonas/festival/1")).andExpect(status().isOk());
        mvc.perform(get("/zonas/1/aforo-disponible")).andExpect(status().isOk());
        mvc.perform(get("/tipos-entrada")).andExpect(status().isOk());
        mvc.perform(post("/zonas").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/tipos-entrada").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/boletas/7/qr")).andExpect(status().isUnauthorized());
    }

    @Test
    void elListadoDeReventasEsPublicoPeroComprarYVerLasPropiasNo() throws Exception {
        mvc.perform(get("/publicaciones-reventa")).andExpect(status().isOk());
        verify(publicacionReventaService).disponibles(0);
        mvc.perform(get("/publicaciones-reventa/mias")).andExpect(status().isUnauthorized());
        mvc.perform(put("/publicaciones-reventa/1/comprar").contentType(MediaType.APPLICATION_JSON)
                .content("{\"idDispositivo\":1,\"metodoPago\":\"PSE\"}")).andExpect(status().isUnauthorized());
    }

    @Test
    void conSesionElListadoDeReventasExcluyeLasDelPropioUsuario() throws Exception {
        mvc.perform(get("/publicaciones-reventa").with(user(asistente))).andExpect(status().isOk());
        verify(publicacionReventaService).disponibles(asistente.getDocumento());
    }

    private Usuario usuario(long documento, TipoUsuario rol) {
        var usuario = new Usuario();
        usuario.setDocumento(documento);
        usuario.setCorreo("ficticio-" + documento + "@example.test");
        usuario.setNombreUsuario("ficticio-" + documento);
        usuario.setContrasenia("no-se-utiliza-en-estas-pruebas");
        usuario.setTipoUsuario(rol);
        return usuario;
    }
}
