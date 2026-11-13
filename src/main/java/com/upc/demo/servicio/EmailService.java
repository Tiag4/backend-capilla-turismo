package com.upc.demo.servicio;

import com.upc.demo.entidad.Booking;
import com.upc.demo.entidad.enums.BookingStatus;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Locale ARGENTINE_LOCALE = new Locale("es", "AR");

    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${app.mail.from:no-reply@capilladelmonte.gov.ar}")
    private String fromAddress;

    @Value("${app.mail.from-name:Cámara de Turismo de Capilla del Monte}")
    private String fromName;

    @Value("${app.portal.base-url:http://localhost:5173}")
    private String portalBaseUrl;

    public boolean isEnabled() {
        return mailEnabled && mailSenderProvider.getIfAvailable() != null;
    }

    public boolean sendVoucherEmails(Booking booking) {
        boolean guestSent = sendVoucherToGuest(booking);
        boolean hostSent = sendVoucherToHost(booking);
        return guestSent || hostSent;
    }

    public boolean sendVoucherToGuest(Booking booking) {
        return sendVoucherEmail(booking, booking.getGuestEmail(), false);
    }

    public boolean sendVoucherToHost(Booking booking) {
        if (booking.getAccommodation() == null || booking.getAccommodation().getHost() == null) {
            return false;
        }
        return sendVoucherEmail(booking, booking.getAccommodation().getHost().getEmail(), true);
    }

    private boolean sendVoucherEmail(Booking booking, String recipient, boolean forHost) {
        if (recipient == null || recipient.isBlank()) {
            log.warn("No se envio el voucher {}: el destinatario no tiene email configurado", booking.getBookingCode());
            return false;
        }

        if (!isEnabled()) {
            log.info("Correo deshabilitado. Voucher {} preparado para {} ({})",
                    booking.getBookingCode(), recipient, forHost ? "anfitrión" : "turista");
            return false;
        }

        try {
            JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setFrom(new InternetAddress(fromAddress, fromName, StandardCharsets.UTF_8.name()));
            helper.setTo(recipient);
            helper.setSubject(buildSubject(booking, forHost));
            helper.setText(buildVoucherHtml(booking, forHost), true);
            mailSender.send(message);
            log.info("Voucher {} enviado a {}", booking.getBookingCode(), recipient);
            return true;
        } catch (MailException | jakarta.mail.MessagingException | java.io.UnsupportedEncodingException exception) {
            log.error("No se pudo enviar el voucher {} a {}: {}",
                    booking.getBookingCode(), recipient, exception.getMessage());
            return false;
        }
    }

    private String buildSubject(Booking booking, boolean forHost) {
        if (forHost) {
            return "Nueva reserva " + booking.getBookingCode() + " - " + booking.getAccommodation().getName();
        }
        return "Tu reserva " + booking.getBookingCode() + " - Cámara de Turismo de Capilla del Monte";
    }

    private String buildVoucherHtml(Booking booking, boolean forHost) {
        String accommodationName = escapeHtml(booking.getAccommodation().getName());
        String guestName = escapeHtml(booking.getGuestName());
        String guestEmail = escapeHtml(booking.getGuestEmail());
        String guestPhone = escapeHtml(booking.getGuestPhone());
        String address = escapeHtml(booking.getAccommodation().getAddress());
        String locality = escapeHtml(booking.getAccommodation().getLocality());
        String status = statusLabel(booking.getStatus());
        String voucherUrl = buildVoucherUrl(booking);
        String coordinates = buildCoordinates(booking);

        String intro = forHost
                ? "Recibiste una nueva solicitud de reserva en <strong>" + accommodationName + "</strong>."
                : "Tu reserva en <strong>" + accommodationName + "</strong> fue registrada correctamente.";

        String guestBlock = forHost
                ? "<tr><td style=\"padding:8px 0;color:#6b665c;\">Huésped</td><td style=\"padding:8px 0;text-align:right;font-weight:700;color:#22201e;\">"
                    + guestName + "<br><span style=\"font-weight:400;color:#6b665c;\">" + guestEmail + " · " + guestPhone + "</span></td></tr>"
                : "";

        String actionLabel = forHost ? "Ver reserva en el panel" : "Consultar mi voucher";
        String actionUrl = forHost ? portalBaseUrl.replaceAll("/+$", "") + "/reservas/consulta?code="
                + encode(booking.getBookingCode()) + "&email=" + encode(booking.getGuestEmail()) : voucherUrl;

        return "<!doctype html>"
                + "<html lang=\"es\"><head><meta charset=\"utf-8\">"
                + "<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">"
                + "<title>Voucher de reserva</title></head>"
                + "<body style=\"margin:0;padding:0;background:#f4f0e8;font-family:Arial,Helvetica,sans-serif;color:#22201e;\">"
                + "<table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" style=\"background:#f4f0e8;padding:24px 12px;\">"
                + "<tr><td align=\"center\">"
                + "<table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" style=\"max-width:620px;background:#ffffff;border:1px solid #ded8cb;border-radius:18px;overflow:hidden;\">"
                + "<tr><td style=\"background:#e06d39;padding:22px 28px;\">"
                + "<span style=\"display:block;color:#ffffff;font-size:11px;letter-spacing:2px;text-transform:uppercase;font-weight:700;\">Cámara de Turismo de Capilla del Monte</span>"
                + "<span style=\"display:block;color:#ffffff;font-size:22px;font-weight:800;margin-top:6px;\">Voucher Oficial de Reserva</span>"
                + "</td></tr>"
                + "<tr><td style=\"padding:26px 28px;\">"
                + "<p style=\"margin:0 0 14px;font-size:15px;line-height:1.6;\">" + intro + "</p>"
                + "<div style=\"display:inline-block;background:#faf8f5;border:1px dashed #bcbaa9;border-radius:12px;padding:12px 18px;margin-bottom:20px;\">"
                + "<span style=\"display:block;font-size:10px;letter-spacing:1.5px;text-transform:uppercase;color:#8c877a;font-weight:700;\">Código de reserva</span>"
                + "<span style=\"display:block;font-size:22px;font-weight:800;color:#c95627;letter-spacing:1px;\">" + escapeHtml(booking.getBookingCode()) + "</span>"
                + "<span style=\"display:block;font-size:11px;color:#6b665c;\">Estado: " + status + "</span>"
                + "</div>"
                + "<table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" style=\"font-size:14px;border-top:1px solid #ece8df;border-bottom:1px solid #ece8df;margin-bottom:18px;\">"
                + row("Ingreso", booking.getCheckIn().format(DATE_FORMATTER))
                + row("Egreso", booking.getCheckOut().format(DATE_FORMATTER))
                + row("Noches", String.valueOf(booking.getTotalNights()))
                + row("Huéspedes", String.valueOf(booking.getGuestCount()))
                + guestBlock
                + row("Alojamiento", accommodationName + "<br><span style=\"font-weight:400;color:#6b665c;\">" + address + " · " + locality + "</span>")
                + (coordinates.isEmpty() ? "" : row("Ubicación", coordinates))
                + "</table>"
                + "<table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" style=\"background:#faf8f5;border:1px solid #ece8df;border-radius:12px;padding:14px 18px;margin-bottom:20px;font-size:14px;\">"
                + row("Total de la estadía", formatMoney(booking.getTotalAmount()))
                + row("Seña requerida (" + booking.getDownPaymentPercentage() + "%)", formatMoney(booking.getDownPaymentAmount()))
                + row("Saldo restante", formatMoney(booking.getRemainingBalance()))
                + "</table>"
                + "<p style=\"margin:0 0 18px;font-size:13px;color:#6b665c;line-height:1.6;\">"
                + (forHost
                    ? "Comunicate con el huésped para coordinar el ingreso y la acreditación de la seña."
                    : "Presenta este voucher al ingresar. El saldo restante se abona directamente en el establecimiento.")
                + "</p>"
                + "<a href=\"" + actionUrl + "\" style=\"display:inline-block;background:#22201e;color:#ffffff;text-decoration:none;font-weight:700;font-size:14px;padding:12px 22px;border-radius:10px;\">"
                + actionLabel + "</a>"
                + "</td></tr>"
                + "<tr><td style=\"background:#0d2f22;padding:16px 28px;color:#d6e7c0;font-size:11px;line-height:1.5;\">"
                + "Cámara de Turismo de Capilla del Monte · Córdoba, Argentina<br>"
                + "Este es un mensaje automático, no respondas a esta dirección."
                + "</td></tr>"
                + "</table></td></tr></table></body></html>";
    }

    private String row(String label, String value) {
        return "<tr><td style=\"padding:8px 0;color:#6b665c;\">" + label + "</td>"
                + "<td style=\"padding:8px 0;text-align:right;font-weight:700;color:#22201e;\">" + value + "</td></tr>";
    }

    public boolean sendPasswordResetOtp(String recipient, String userName, String code) {
        if (recipient == null || recipient.isBlank()) {
            return false;
        }

        if (!isEnabled()) {
            log.info("Correo deshabilitado. Código de recuperación OTP para {} ({}): {}", recipient, userName, code);
            return false;
        }

        try {
            JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setFrom(new InternetAddress(fromAddress, fromName, StandardCharsets.UTF_8.name()));
            helper.setTo(recipient);
            helper.setSubject("Código de recuperación de contraseña: " + code);
            helper.setText(buildResetPasswordHtml(userName, code), true);
            mailSender.send(message);
            log.info("Código OTP de recuperación enviado a {}", recipient);
            return true;
        } catch (MailException | jakarta.mail.MessagingException | java.io.UnsupportedEncodingException exception) {
            log.error("No se pudo enviar el código OTP a {}: {}", recipient, exception.getMessage());
            return false;
        }
    }

    private String buildResetPasswordHtml(String userName, String code) {
        String safeName = escapeHtml(userName != null && !userName.isBlank() ? userName : "Usuario");
        String safeCode = escapeHtml(code);

        return "<!doctype html>"
                + "<html lang=\"es\"><head><meta charset=\"utf-8\">"
                + "<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">"
                + "<title>Recuperación de contraseña</title></head>"
                + "<body style=\"margin:0;padding:0;background:#f4f0e8;font-family:Arial,Helvetica,sans-serif;color:#22201e;\">"
                + "<table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" style=\"background:#f4f0e8;padding:24px 12px;\">"
                + "<tr><td align=\"center\">"
                + "<table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" style=\"max-width:540px;background:#ffffff;border:1px solid #ded8cb;border-radius:18px;overflow:hidden;\">"
                + "<tr><td style=\"background:#e06d39;padding:22px 28px;\">"
                + "<span style=\"display:block;color:#ffffff;font-size:11px;letter-spacing:2px;text-transform:uppercase;font-weight:700;\">Turismo Capilla del Monte</span>"
                + "<span style=\"display:block;color:#ffffff;font-size:22px;font-weight:800;margin-top:6px;\">Recuperación de Contraseña</span>"
                + "</td></tr>"
                + "<tr><td style=\"padding:28px;\">"
                + "<p style=\"margin:0 0 16px;font-size:15px;line-height:1.5;\">Hola <strong>" + safeName + "</strong>,</p>"
                + "<p style=\"margin:0 0 20px;font-size:14px;color:#555;line-height:1.5;\">Recibimos una solicitud para restablecer la contraseña de tu cuenta. Ingresá el siguiente código de 6 dígitos en la pantalla de recuperación:</p>"
                + "<div style=\"background:#faf8f5;border:2px dashed #e06d39;border-radius:12px;padding:16px;text-align:center;margin:24px 0;\">"
                + "<span style=\"font-size:32px;font-weight:800;letter-spacing:8px;color:#e06d39;font-family:monospace;\">" + safeCode + "</span>"
                + "</div>"
                + "<p style=\"margin:0 0 8px;font-size:13px;color:#777;\">⏰ Este código tiene una validez de <strong>15 minutos</strong> y vencerá automáticamente tras 5 intentos fallidos.</p>"
                + "<p style=\"margin:0;font-size:13px;color:#999;\">Si no solicitaste este cambio, podés ignorar este correo; tu cuenta permanece segura.</p>"
                + "</td></tr>"
                + "<tr><td style=\"background:#faf8f5;padding:16px 28px;text-align:center;border-top:1px solid #eee;font-size:12px;color:#888;\">"
                + "Secretaría y Comisión de Turismo de Capilla del Monte, Córdoba"
                + "</td></tr>"
                + "</table></td></tr></table></body></html>";
    }

    private String buildVoucherUrl(Booking booking) {
        String baseUrl = portalBaseUrl.replaceAll("/+$", "");
        return baseUrl + "/reservas/consulta?code=" + encode(booking.getBookingCode())
                + "&email=" + encode(booking.getGuestEmail());
    }

    private String buildCoordinates(Booking booking) {
        if (booking.getAccommodation().getLatitude() == null || booking.getAccommodation().getLongitude() == null) {
            return "";
        }
        return booking.getAccommodation().getLatitude() + ", " + booking.getAccommodation().getLongitude();
    }

    private String statusLabel(BookingStatus status) {
        return switch (status) {
            case CONFIRMED -> "Reserva confirmada";
            case CANCELLED -> "Reserva cancelada";
            case COMPLETED -> "Estadía completada";
            default -> "Pendiente de confirmacion";
        };
    }

    private String formatMoney(BigDecimal amount) {
        if (amount == null) {
            return "$ 0";
        }
        NumberFormat formatter = NumberFormat.getCurrencyInstance(ARGENTINE_LOCALE);
        formatter.setMaximumFractionDigits(0);
        return formatter.format(amount);
    }

    private String encode(String value) {
        return URLEncoder.encode(value != null ? value : "", StandardCharsets.UTF_8);
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
