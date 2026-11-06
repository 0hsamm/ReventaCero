package co.edu.unbosque.reventacero.service;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import co.edu.unbosque.reventacero.dto.DatosQrBoletaDTO;
import co.edu.unbosque.reventacero.exception.RecursoNoEncontradoException;
import co.edu.unbosque.reventacero.repository.BoletaRepository;

@Service
public class BoletaQrService {

	private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.of("es", "CO"));

	private static final int MARGEN_MODULOS = 2;

	private static final String SIN_DATO = "-";

	private final BoletaRepository boletaRepo;

	private final byte[] secreto;

	public BoletaQrService(BoletaRepository boletaRepo,
			@Value("${app.qr.secret:${jwt.secret}}") String secreto) {
		this.boletaRepo = boletaRepo;
		this.secreto = secreto.getBytes(StandardCharsets.UTF_8);
	}

	public String contenido(long codigoBoleta) {
		return contenido(datos(codigoBoleta));
	}

	public String svg(long codigoBoleta) {
		return svgDeTexto(contenido(codigoBoleta));
	}

	public DatosQrBoletaDTO datos(long codigoBoleta) {
		return boletaRepo.findDatosQr(codigoBoleta)
				.orElseThrow(() -> new RecursoNoEncontradoException("La boleta " + codigoBoleta + " no existe"));
	}

	String contenido(DatosQrBoletaDTO d) {
		StringBuilder texto = new StringBuilder();
		texto.append("REVENTACERO - BOLETA OFICIAL\n");
		texto.append("----------------------------\n");
		linea(texto, "Comprador", d.comprador());
		if (d.revendedor() != null && !d.revendedor().isBlank()) {
			linea(texto, "Revendedor", d.revendedor().trim());
		}
		linea(texto, "Festival", d.festival() + " (Edición " + d.edicion() + ")");
		linea(texto, "Tipo de boleta", d.categoria());
		linea(texto, "Número de boleta", String.valueOf(d.codigoBoleta()));
		texto.append("----------------------------\n");
		linea(texto, "Zona", d.zona());
		linea(texto, "Fechas", fecha(d.fechaInicio()) + " al " + fecha(d.fechaFin()));
		linea(texto, "Ciudad", d.ciudad());
		linea(texto, "Documento comprador", enmascarar(d.documentoComprador()));
		linea(texto, "Emitida", fecha(d.fechaEmision()));
		linea(texto, "Transacción", d.idTransaccion() != null ? String.valueOf(d.idTransaccion()) : SIN_DATO);
		linea(texto, "Estado", d.estado());
		linea(texto, "Verificación", codigoVerificacion(d));
		texto.append("Intransferible fuera de ReventaCero. Preséntala con tu documento.");
		return texto.toString();
	}

	String codigoVerificacion(DatosQrBoletaDTO d) {
		String firmado = String.join("|",
				"RC1",
				String.valueOf(d.codigoBoleta()),
				String.valueOf(d.documentoComprador()),
				String.valueOf(d.idTipoEntrada()),
				String.valueOf(d.idFestival()),
				String.valueOf(d.fechaEmision()),
				String.valueOf(d.idTransaccion()));
		try {
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(new SecretKeySpec(secreto, "HmacSHA256"));
			String hex = HexFormat.of().withUpperCase()
					.formatHex(mac.doFinal(firmado.getBytes(StandardCharsets.UTF_8)))
					.substring(0, 16);
			return hex.substring(0, 4) + "-" + hex.substring(4, 8) + "-" + hex.substring(8, 12) + "-"
					+ hex.substring(12, 16);
		} catch (NoSuchAlgorithmException | InvalidKeyException e) {
			throw new IllegalStateException("No se pudo firmar el QR de la boleta", e);
		}
	}

	static String svgDeTexto(String texto) {
		Map<EncodeHintType, Object> opciones = new EnumMap<>(EncodeHintType.class);
		opciones.put(EncodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name());
		opciones.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
		opciones.put(EncodeHintType.MARGIN, MARGEN_MODULOS);

		BitMatrix matriz;
		try {
			matriz = new QRCodeWriter().encode(texto, BarcodeFormat.QR_CODE, 0, 0, opciones);
		} catch (WriterException e) {
			throw new IllegalStateException("No se pudo generar el QR de la boleta", e);
		}

		int ancho = matriz.getWidth();
		int alto = matriz.getHeight();
		StringBuilder trazo = new StringBuilder();
		for (int y = 0; y < alto; y++) {
			int x = 0;
			while (x < ancho) {
				if (!matriz.get(x, y)) {
					x++;
					continue;
				}
				int inicio = x;
				while (x < ancho && matriz.get(x, y)) {
					x++;
				}
				trazo.append('M').append(inicio).append(' ').append(y)
						.append('h').append(x - inicio).append("v1h-").append(x - inicio).append('z');
			}
		}

		return "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 " + ancho + " " + alto
				+ "\" shape-rendering=\"crispEdges\" role=\"img\" aria-label=\"Código QR de la boleta\">"
				+ "<rect width=\"100%\" height=\"100%\" fill=\"#ffffff\"/>"
				+ "<path fill=\"#000000\" d=\"" + trazo + "\"/></svg>";
	}

	private static void linea(StringBuilder texto, String etiqueta, String valor) {
		texto.append(etiqueta).append(": ").append(valor == null || valor.isBlank() ? SIN_DATO : valor).append('\n');
	}

	private static String fecha(LocalDate fecha) {
		return fecha == null ? SIN_DATO : FECHA.format(fecha);
	}

	static String enmascarar(long documento) {
		String valor = String.valueOf(documento);
		if (valor.length() <= 4) {
			return valor;
		}
		return "*".repeat(valor.length() - 4) + valor.substring(valor.length() - 4);
	}
}
