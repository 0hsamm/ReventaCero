package co.edu.unbosque.reventacero.service;

import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;
import org.springframework.web.util.UriComponentsBuilder;

import co.edu.unbosque.reventacero.entity.Usuario;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class CorreoService {

	private static final Logger LOG = LoggerFactory.getLogger(CorreoService.class);

	private final JavaMailSender mailSender;

	private final String remitente;

	private final String urlFrontend;

	public CorreoService(JavaMailSender mailSender,
			@Value("${app.mail.from}") String remitente,
			@Value("${app.frontend.url}") String urlFrontend) {
		this.mailSender = mailSender;
		this.remitente = remitente;
		this.urlFrontend = urlFrontend.endsWith("/") ? urlFrontend.substring(0, urlFrontend.length() - 1)
				: urlFrontend;
	}

	public boolean enviarVerificacion(Usuario usuario, String token, long horasValidez) {

		String enlace = UriComponentsBuilder.fromUriString(urlFrontend)
				.path("/verificar-correo")
				.queryParam("token", token)
				.build()
				.toUriString();

		String nombre = usuario.getNombreCompleto() != null && usuario.getNombreCompleto().getNombre() != null
				? usuario.getNombreCompleto().getNombre()
				: usuario.getNombreUsuario();

		try {
			MimeMessage mensaje = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, StandardCharsets.UTF_8.name());
			helper.setFrom(remitente, "ReventaCero");
			helper.setTo(usuario.getCorreo());
			helper.setSubject("Verifica tu cuenta de ReventaCero");
			helper.setText(textoPlano(nombre, enlace, horasValidez), html(nombre, enlace, horasValidez));
			mailSender.send(mensaje);
			return true;
		} catch (MailException | MessagingException | java.io.UnsupportedEncodingException e) {
			LOG.error("No se pudo enviar el correo de verificación a {}: {}", usuario.getCorreo(), e.getMessage());
			return false;
		}
	}

	private static String textoPlano(String nombre, String enlace, long horas) {
		return "Hola " + nombre + ",\n\n"
				+ "Gracias por registrarte en ReventaCero. Para activar tu cuenta abre este enlace:\n\n"
				+ enlace + "\n\n"
				+ "El enlace vence en " + horas + " horas. Si no creaste esta cuenta, ignora este correo.\n\n"
				+ "— Equipo ReventaCero";
	}

	private static String html(String nombre, String enlace, long horas) {
		String n = HtmlUtils.htmlEscape(nombre);
		String e = HtmlUtils.htmlEscape(enlace);
		return """
				<!doctype html>
				<html lang="es"><body style="margin:0;padding:0;background:#050505;">
				<table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="background:#050505;padding:32px 0;">
				<tr><td align="center">
				<table role="presentation" width="560" cellpadding="0" cellspacing="0"
				 style="max-width:560px;width:100%%;background:#121212;border:2px solid #f4f4f5;font-family:Arial,Helvetica,sans-serif;color:#f4f4f5;">
				<tr><td style="padding:20px 28px;border-bottom:2px dashed #2e2e2e;">
				<span style="display:inline-block;width:28px;height:28px;line-height:28px;text-align:center;background:#ccff00;color:#050505;font-weight:bold;">R</span>
				<span style="font-weight:bold;font-size:16px;vertical-align:middle;margin-left:8px;">ReventaCero</span>
				</td></tr>
				<tr><td style="padding:32px 28px 8px;">
				<h1 style="margin:0 0 16px;font-family:Impact,'Arial Narrow',sans-serif;font-weight:400;font-size:40px;line-height:1;text-transform:uppercase;">
				Verifica tu <span style="background:#ccff00;color:#050505;padding:0 6px;">correo</span></h1>
				<p style="margin:0 0 12px;font-size:16px;line-height:1.5;color:#c4c4c9;">Hola %s, gracias por registrarte.
				Confirma que este correo es tuyo para activar tu cuenta y empezar a comprar boletas.</p>
				</td></tr>
				<tr><td style="padding:16px 28px 8px;">
				<a href="%s" style="display:inline-block;padding:14px 24px;background:#ccff00;color:#050505;border:2px solid #050505;font-weight:bold;font-size:16px;text-decoration:none;text-transform:uppercase;">Verificar mi cuenta</a>
				</td></tr>
				<tr><td style="padding:16px 28px 28px;font-size:13px;line-height:1.5;color:#a1a1aa;">
				El enlace vence en %d horas. Si el botón no funciona, copia esta dirección en tu navegador:<br>
				<span style="color:#f4f4f5;word-break:break-all;">%s</span><br><br>
				Si no creaste esta cuenta, ignora este correo.
				</td></tr>
				</table>
				</td></tr></table>
				</body></html>
				""".formatted(n, e, horas, e);
	}
}
