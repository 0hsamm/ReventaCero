package co.edu.unbosque.reventacero.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import co.edu.unbosque.reventacero.dto.DatosQrBoletaDTO;
import co.edu.unbosque.reventacero.exception.RecursoNoEncontradoException;
import co.edu.unbosque.reventacero.repository.BoletaRepository;

class BoletaQrServiceTest {

	private BoletaRepository boletaRepo;

	private BoletaQrService service;

	@BeforeEach
	void setUp() {
		boletaRepo = mock(BoletaRepository.class);
		service = new BoletaQrService(boletaRepo, "secreto-de-prueba-reventacero-con-mas-de-32-caracteres");
	}

	private static DatosQrBoletaDTO datos(long comprador, String revendedor, Long transaccion) {
		return new DatosQrBoletaDTO(4521L, LocalDate.of(2026, 10, 1), 7L, "VIP", "Platea", 3L,
				"Estéreo Picnic", 2026, LocalDate.of(2026, 12, 4), LocalDate.of(2026, 12, 6), "Bogotá",
				comprador, "Ana Pérez", revendedor, transaccion, "VIGENTE");
	}

	@Test
	void elTextoTieneLosDatosPedidosYNingunaUrl() {
		String texto = service.contenido(datos(1012345678L, null, 88L));

		assertTrue(texto.contains("Comprador: Ana Pérez"));
		assertTrue(texto.contains("Festival: Estéreo Picnic (Edición 2026)"));
		assertTrue(texto.contains("Tipo de boleta: VIP"));
		assertTrue(texto.contains("Número de boleta: 4521"));
		assertTrue(texto.contains("Documento comprador: ******5678"));
		assertTrue(texto.contains("Estado: VIGENTE"));
		assertTrue(Pattern.compile("Verificación: [0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{4}").matcher(texto).find());
		assertFalse(texto.contains("Revendedor"));
		assertFalse(texto.matches("(?is).*(https?://|www\\.|\\.com|\\.co\\b).*"));
	}

	@Test
	void elRevendedorApareceSoloSiLaBoletaVieneDeUnaReventa() {
		String texto = service.contenido(datos(1012345678L, "Juan Gómez", 90L));

		assertTrue(texto.contains("Revendedor: Juan Gómez"));
	}

	@Test
	void siCambiaElDuenoCambiaElCodigoDeVerificacion() {
		String original = service.codigoVerificacion(datos(1012345678L, null, 88L));

		assertEquals(original, service.codigoVerificacion(datos(1012345678L, null, 88L)));
		assertNotEquals(original, service.codigoVerificacion(datos(1099999999L, "Ana Pérez", 95L)));
	}

	@Test
	void elSvgSeLeeComoElMismoTextoAlEscanearlo() throws Exception {
		when(boletaRepo.findDatosQr(4521L)).thenReturn(Optional.of(datos(1012345678L, "Juan Gómez", 90L)));

		String svg = service.svg(4521L);

		assertTrue(svg.startsWith("<svg"));
		assertEquals(service.contenido(4521L), escanear(svg));
	}

	@Test
	void boletaInexistenteEs404() {
		when(boletaRepo.findDatosQr(1L)).thenReturn(Optional.empty());

		assertThrows(RecursoNoEncontradoException.class, () -> service.svg(1L));
	}

	private static String escanear(String svg) throws Exception {
		Matcher caja = Pattern.compile("viewBox=\"0 0 (\\d+) (\\d+)\"").matcher(svg);
		assertTrue(caja.find());
		int ancho = Integer.parseInt(caja.group(1));
		int alto = Integer.parseInt(caja.group(2));
		int escala = 4;

		int[] pixeles = new int[ancho * escala * alto * escala];
		java.util.Arrays.fill(pixeles, 0xFFFFFFFF);

		Matcher tramo = Pattern.compile("M(\\d+) (\\d+)h(\\d+)").matcher(svg);
		while (tramo.find()) {
			int x0 = Integer.parseInt(tramo.group(1));
			int y = Integer.parseInt(tramo.group(2));
			int largo = Integer.parseInt(tramo.group(3));
			for (int x = x0; x < x0 + largo; x++) {
				for (int dy = 0; dy < escala; dy++) {
					for (int dx = 0; dx < escala; dx++) {
						pixeles[(y * escala + dy) * ancho * escala + x * escala + dx] = 0xFF000000;
					}
				}
			}
		}

		RGBLuminanceSource fuente = new RGBLuminanceSource(ancho * escala, alto * escala, pixeles);
		Map<DecodeHintType, Object> opciones = new EnumMap<>(DecodeHintType.class);
		opciones.put(DecodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name());
		opciones.put(DecodeHintType.PURE_BARCODE, Boolean.TRUE);
		return new QRCodeReader().decode(new BinaryBitmap(new HybridBinarizer(fuente)), opciones).getText();
	}
}
