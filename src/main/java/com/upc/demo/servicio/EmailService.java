package com.upc.demo.servicio;

import com.upc.demo.entidad.Accommodation;
import com.upc.demo.entidad.Booking;
import com.upc.demo.entidad.User;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Locale ARGENTINE_LOCALE = new Locale("es", "AR");

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final AsyncMailDispatcher asyncMailDispatcher;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${app.mail.from:no-reply@capilladelmonte.gov.ar}")
    private String fromAddress;

    @Value("${app.mail.from-name:Cámara de Turismo de Capilla del Monte}")
    private String fromName;

    @Value("${app.portal.base-url:http://localhost:5173}")
    private String portalBaseUrl;

    @Value("${app.dashboard.base-url:http://localhost:5174}")
    private String dashboardBaseUrl;

    public boolean isEnabled() {
        return mailEnabled && mailSenderProvider.getIfAvailable() != null;
    }

    public boolean sendVoucherEmails(Booking booking) {
        boolean guestSent = dispatchBookingEmail(booking, booking.getGuestEmail(), false, true);
        boolean hostSent = false;
        if (booking.getAccommodation() != null && booking.getAccommodation().getHost() != null) {
            hostSent = dispatchBookingEmail(booking, booking.getAccommodation().getHost().getEmail(), true, true);
        }
        return guestSent || hostSent;
    }

    public boolean sendVoucherToGuest(Booking booking) {
        return dispatchBookingEmail(booking, booking.getGuestEmail(), false, false);
    }

    public boolean sendVoucherToHost(Booking booking) {
        if (booking.getAccommodation() == null || booking.getAccommodation().getHost() == null) {
            return false;
        }
        return dispatchBookingEmail(booking, booking.getAccommodation().getHost().getEmail(), true, false);
    }

    public void sendInvitationEmail(String recipientEmail, String token, LocalDateTime expiresAt) {
        if (recipientEmail == null || recipientEmail.isBlank() || token == null || token.isBlank()) {
            log.warn("No se envió la invitación por email: destinatario o token vacío");
            return;
        }

        if (!isEnabled()) {
            log.info("Correo deshabilitado. Invitación preparada para {} con token {}", recipientEmail, token);
            return;
        }

        String subject = "Invitación a Prestador — Portal Oficial Capilla del Monte";
        String html = buildInvitationHtml(recipientEmail, token, expiresAt);
        asyncMailDispatcher.sendHtmlAsync(fromAddress, fromName, recipientEmail, subject, html, "Invitación " + recipientEmail);
    }

    private boolean dispatchBookingEmail(Booking booking, String recipient, boolean forHost, boolean async) {
        if (recipient == null || recipient.isBlank()) {
            log.warn("No se envio el correo {}: el destinatario no tiene email configurado", booking.getBookingCode());
            return false;
        }

        if (!isEnabled()) {
            log.info("Correo deshabilitado. Notificación {} ({}) preparada para {} ({})",
                    booking.getBookingCode(), booking.getStatus(), recipient, forHost ? "anfitrión" : "turista");
            return false;
        }

        String subject = buildSubject(booking, forHost);
        String html = buildVoucherHtml(booking, forHost);
        String label = booking.getBookingCode() + " (" + booking.getStatus() + ")";

        if (async) {
            asyncMailDispatcher.sendHtmlAsync(fromAddress, fromName, recipient, subject, html, label);
            return true;
        }
        return asyncMailDispatcher.sendHtmlSync(fromAddress, fromName, recipient, subject, html, label);
    }

    private String buildSubject(Booking booking, boolean forHost) {
        String accommodationName = booking.getAccommodation().getName();
        BookingStatus status = booking.getStatus();

        if (forHost) {
            return switch (status) {
                case CONFIRMED -> "Reserva " + booking.getBookingCode() + " confirmada — " + accommodationName;
                case CANCELLED -> "Reserva " + booking.getBookingCode() + " cancelada — " + accommodationName;
                case COMPLETED -> "Estadía completada " + booking.getBookingCode() + " — " + accommodationName;
                default -> "Nueva solicitud de reserva recibida — " + accommodationName;
            };
        }

        return switch (status) {
            case CONFIRMED -> "¡Tu reserva en " + accommodationName + " está confirmada!";
            case CANCELLED -> "Actualización de tu reserva " + booking.getBookingCode() + " — " + accommodationName;
            case COMPLETED -> "¡Gracias por tu estadía en " + accommodationName + "! Dejá tu reseña oficial";
            default -> "Tu reserva " + booking.getBookingCode() + " - Cámara de Turismo de Capilla del Monte";
        };
    }

    private String buildVoucherHtml(Booking booking, boolean forHost) {
        String accommodationName = escapeHtml(booking.getAccommodation().getName());
        String guestName = escapeHtml(booking.getGuestName());
        String guestEmail = escapeHtml(booking.getGuestEmail());
        String guestPhone = escapeHtml(booking.getGuestPhone());
        String address = escapeHtml(booking.getAccommodation().getAddress());
        String locality = escapeHtml(booking.getAccommodation().getLocality());
        BookingStatus bookingStatus = booking.getStatus();
        String status = statusLabel(bookingStatus);
        String voucherUrl = buildVoucherUrl(booking);
        String coordinates = buildCoordinates(booking);

        User host = booking.getAccommodation().getHost();
        String hostName = host != null ? escapeHtml(((host.getName() != null ? host.getName() : "") + " " + (host.getLastName() != null ? host.getLastName() : "")).trim()) : "";
        String hostEmail = host != null && host.getEmail() != null ? escapeHtml(host.getEmail()) : "";
        String hostPhone = host != null && host.getPhone() != null ? escapeHtml(host.getPhone()) : "";

        String headerTitle = switch (bookingStatus) {
            case CONFIRMED -> forHost ? "Reserva Confirmada" : "¡Reserva Confirmada!";
            case CANCELLED -> "Reserva Cancelada";
            case COMPLETED -> "Estadía Completada";
            default -> forHost ? "Nueva Solicitud de Reserva" : "Voucher Oficial de Reserva";
        };

        String intro;
        if (forHost) {
            intro = switch (bookingStatus) {
                case CONFIRMED -> "Confirmaste la reserva <strong>" + escapeHtml(booking.getBookingCode()) + "</strong> en <strong>" + accommodationName + "</strong>. Ya enviamos el voucher actualizado al huésped.";
                case CANCELLED -> "La reserva <strong>" + escapeHtml(booking.getBookingCode()) + "</strong> en <strong>" + accommodationName + "</strong> fue <strong>cancelada</strong>. Las fechas quedaron liberadas en tu inventario.";
                case COMPLETED -> "La estadía correspondiente a la reserva <strong>" + escapeHtml(booking.getBookingCode()) + "</strong> en <strong>" + accommodationName + "</strong> fue marcada como completada.";
                default -> "Recibiste una nueva solicitud de reserva en <strong>" + accommodationName + "</strong>. Revisá los detalles del huésped y confirmala desde tu panel.";
            };
        } else {
            intro = switch (bookingStatus) {
                case CONFIRMED -> "¡Excelentes noticias! Tu reserva en <strong>" + accommodationName + "</strong> está <strong>confirmada</strong>. Te compartimos el voucher oficial y los datos de contacto del establecimiento.";
                case CANCELLED -> "Te informamos que tu reserva en <strong>" + accommodationName + "</strong> fue <strong>cancelada / declinada</strong>. Si seguís buscando estadía en Capilla del Monte, podés explorar otras opciones disponibles en el portal.";
                case COMPLETED -> "Esperamos que hayas disfrutado tu estadía en <strong>" + accommodationName + "</strong> al pie de las sierras. Tu opinión sobre la limpieza, veracidad, atención y comodidades es muy valiosa para futuros turistas de Capilla del Monte.";
                default -> "Tu solicitud de reserva en <strong>" + accommodationName + "</strong> fue registrada correctamente.";
            };
        }

        String guestBlock = forHost
                ? "<tr><td style=\"padding:8px 0;color:#6b665c;\">Huésped</td><td style=\"padding:8px 0;text-align:right;font-weight:700;color:#22201e;\">"
                    + guestName + "<br><span style=\"font-weight:400;color:#6b665c;\">" + guestEmail + " · " + guestPhone + "</span></td></tr>"
                : "";

        String hostContactBlock = (!forHost && bookingStatus == BookingStatus.CONFIRMED && (!hostName.isEmpty() || !hostEmail.isEmpty() || !hostPhone.isEmpty()))
                ? row("Contacto del prestador", (hostName.isEmpty() ? "Anfitrión" : hostName)
                    + "<br><span style=\"font-weight:400;color:#6b665c;\">"
                    + hostEmail + (hostPhone.isEmpty() ? "" : " · " + hostPhone) + "</span>")
                : "";

        String guidanceText;
        if (forHost) {
            guidanceText = switch (bookingStatus) {
                case CANCELLED -> "Podés revisar la disponibilidad actualizada en tu calendario del Dashboard.";
                case CONFIRMED -> "Recordá coordinar el horario de ingreso con el huésped.";
                default -> "Comunicate con el huésped para coordinar el ingreso y la acreditación de la seña.";
            };
        } else {
            guidanceText = switch (bookingStatus) {
                case CONFIRMED -> "Presentá este voucher al ingresar (check-in a partir de las 14:00 hs / check-out hasta las 10:00 hs). El saldo restante se abona directamente en el establecimiento.";
                case CANCELLED -> "Podés consultar otros alojamientos habilitados por la Secretaría de Turismo en nuestro portal oficial.";
                case COMPLETED -> "Dejá tu reseña oficial haciendo clic en el botón inferior para calificar los 6 aspectos clave de tu estadía.";
                default -> "Presentá este voucher al ingresar. El saldo restante se abona directamente en el establecimiento.";
            };
        }

        String cleanDashboardUrl = (dashboardBaseUrl != null ? dashboardBaseUrl : "http://localhost:5174").replaceAll("/+$", "");
        String cleanPortalUrl = (portalBaseUrl != null ? portalBaseUrl : "http://localhost:5173").replaceAll("/+$", "");
        String reviewUrl = cleanPortalUrl + "/alojamientos/"
                + (booking.getAccommodation() != null ? booking.getAccommodation().getId() : "")
                + "?reviewBookingCode=" + booking.getBookingCode() + "#opiniones-huespedes";

        String actionLabel = forHost
                ? "Gestionar en el Dashboard"
                : (bookingStatus == BookingStatus.COMPLETED
                    ? "Dejar mi reseña sobre la estadía"
                    : (bookingStatus == BookingStatus.CANCELLED ? "Explorar alojamientos" : "Consultar mi voucher"));
        String actionUrl = forHost
                ? cleanDashboardUrl
                : (bookingStatus == BookingStatus.COMPLETED
                    ? reviewUrl
                    : (bookingStatus == BookingStatus.CANCELLED ? cleanPortalUrl + "/alojamientos" : voucherUrl));

        String statusBadgeBg = switch (bookingStatus) {
            case CONFIRMED -> "#1c8b68";
            case COMPLETED -> "#0d2f22";
            case CANCELLED -> "#b91c1c";
            default -> "#c95627";
        };

        Accommodation acc = booking.getAccommodation();
        String bankHolder = acc != null && acc.getBankAccountHolder() != null ? escapeHtml(acc.getBankAccountHolder()) : "";
        String bankCbu = acc != null && acc.getBankCbu() != null ? escapeHtml(acc.getBankCbu()) : "";
        String bankAlias = acc != null && acc.getBankAlias() != null ? escapeHtml(acc.getBankAlias()) : "";
        String bankName = acc != null && acc.getBankName() != null ? escapeHtml(acc.getBankName()) : "";

        String bankBlock = (!forHost && (!bankCbu.isEmpty() || !bankAlias.isEmpty()))
                ? "<table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" style=\"background:#ffffff;border:1px solid #e6e2d8;border-radius:12px;margin-bottom:20px;overflow:hidden;\">"
                    + "<tr><td style=\"background:#faf8f5;padding:12px 18px;border-bottom:1px solid #e6e2d8;\">"
                    + "<span style=\"color:#6b665c;font-size:11px;font-weight:700;letter-spacing:1px;text-transform:uppercase;\">Datos Bancarios para Transferencia de la Seña</span>"
                    + "</td></tr>"
                    + "<tr><td style=\"padding:14px 18px;font-size:13px;line-height:1.6;\">"
                    + (bankHolder.isEmpty() ? "" : "<div><span style=\"color:#8c877a;\">Titular:</span> <strong>" + bankHolder + "</strong></div>")
                    + (bankName.isEmpty() ? "" : "<div><span style=\"color:#8c877a;\">Entidad:</span> <strong>" + bankName + "</strong></div>")
                    + (bankCbu.isEmpty() ? "" : "<div style=\"margin-top:4px;\"><span style=\"color:#8c877a;\">CBU/CVU:</span> <code style=\"font-family:Courier,monospace;background:#faf8f5;padding:2px 6px;border-radius:4px;border:1px solid #e6e2d8;font-size:12px;color:#1a1a1a;font-weight:700;\">" + bankCbu + "</code></div>")
                    + (bankAlias.isEmpty() ? "" : "<div style=\"margin-top:4px;\"><span style=\"color:#8c877a;\">Alias:</span> <code style=\"font-family:Courier,monospace;background:#faf8f5;padding:2px 6px;border-radius:4px;border:1px solid #e6e2d8;font-size:12px;color:#c95627;font-weight:700;\">" + bankAlias + "</code></div>")
                    + "<p style=\"margin:8px 0 0;font-size:11px;color:#8c877a;font-style:italic;\">Una vez realizada la transferencia, enviá el comprobante al anfitrión por WhatsApp para registrar el pago.</p>"
                    + "</td></tr></table>"
                : "";

        return "<!doctype html>"
                + "<html lang=\"es\"><head><meta charset=\"utf-8\">"
                + "<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">"
                + "<title>" + escapeHtml(headerTitle) + "</title></head>"
                + "<body style=\"margin:0;padding:0;background:#f4f0e8;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif;color:#22201e;\">"
                + "<table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" style=\"background:#f4f0e8;padding:24px 12px;\">"
                + "<tr><td align=\"center\">"
                + "<table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" style=\"max-width:600px;background:#ffffff;border:1px solid #ded8cb;border-radius:18px;overflow:hidden;box-shadow:0 4px 16px rgba(0,0,0,0.04);\">"
                + "<tr><td style=\"background:#0d2f22;padding:22px 28px;border-bottom:3px solid #e06d39;\">"
                + "<span style=\"display:block;color:#749b3f;font-size:11px;letter-spacing:1.5px;text-transform:uppercase;font-weight:700;margin-bottom:4px;\">Cámara de Turismo de Capilla del Monte</span>"
                + "<span style=\"display:block;color:#ffffff;font-size:20px;font-weight:800;line-height:1.2;\">" + escapeHtml(headerTitle) + "</span>"
                + "<span style=\"display:block;color:#d6e7c0;font-size:12px;margin-top:4px;\">Portal Oficial de Turismo · Capilla del Monte, Córdoba</span>"
                + "</td></tr>"
                + "<tr><td style=\"padding:28px 28px 24px 28px;\">"
                + "<table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" style=\"background:#faf8f5;border:1px solid #e6e2d8;border-radius:14px;margin-bottom:20px;\">"
                + "<tr><td style=\"padding:16px 20px;\">"
                + "<table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\"><tr>"
                + "<td valign=\"middle\">"
                + "<span style=\"display:block;font-size:10px;letter-spacing:1.5px;text-transform:uppercase;color:#6b665c;font-weight:700;\">Localizador Oficial</span>"
                + "<span style=\"display:block;font-size:24px;font-weight:800;color:#c95627;letter-spacing:1px;font-family:Courier,monospace;line-height:1.2;margin-top:2px;\">" + escapeHtml(booking.getBookingCode()) + "</span>"
                + "</td>"
                + "<td valign=\"middle\" align=\"right\">"
                + "<div style=\"display:inline-block;background:" + statusBadgeBg + ";color:#ffffff;font-size:11px;font-weight:700;padding:6px 14px;border-radius:8px;text-transform:uppercase;\">" + status + "</div>"
                + "</td>"
                + "</tr></table>"
                + "</td></tr></table>"
                + "<p style=\"margin:0 0 16px;font-size:15px;line-height:1.6;\">" + intro + "</p>"
                + "<table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" style=\"font-size:13px;border-top:1px solid #ece8df;border-bottom:1px solid #ece8df;margin-bottom:18px;\">"
                + row("Ingreso (Check-in)", booking.getCheckIn().format(DATE_FORMATTER) + " <span style=\"font-weight:400;color:#8c877a;\">(a partir de 14:00 hs)</span>")
                + row("Egreso (Check-out)", booking.getCheckOut().format(DATE_FORMATTER) + " <span style=\"font-weight:400;color:#8c877a;\">(hasta 10:00 hs)</span>")
                + row("Noches", String.valueOf(booking.getTotalNights()) + " noches")
                + row("Huéspedes", String.valueOf(booking.getGuestCount()) + " personas")
                + guestBlock
                + hostContactBlock
                + row("Alojamiento", accommodationName + "<br><span style=\"font-weight:400;color:#6b665c;\">" + address + " · " + locality + "</span>")
                + (coordinates.isEmpty() ? "" : row("Ubicación", coordinates))
                + "</table>"
                + "<table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" style=\"background:#faf8f5;border:1px solid #ece8df;border-radius:12px;padding:14px 18px;margin-bottom:20px;font-size:14px;\">"
                + row("Total de la estadía", formatMoney(booking.getTotalAmount()))
                + row("Seña requerida (" + booking.getDownPaymentPercentage() + "%)", "<span style=\"color:#c95627;\">" + formatMoney(booking.getDownPaymentAmount()) + "</span>")
                + row("Saldo restante", formatMoney(booking.getRemainingBalance()))
                + "</table>"
                + bankBlock
                + "<p style=\"margin:0 0 18px;font-size:13px;color:#6b665c;line-height:1.6;\">" + guidanceText + "</p>"
                + "<div style=\"text-align:center;margin-bottom:16px;\">"
                + "<a href=\"" + actionUrl + "\" style=\"display:inline-block;background:#22201e;color:#ffffff;text-decoration:none;font-weight:700;font-size:13px;padding:12px 24px;border-radius:6px;letter-spacing:0.3px;\">"
                + actionLabel + " &rarr;</a>"
                + "</div>"
                + "</td></tr>"
                + "<tr><td style=\"background:#0d2f22;padding:20px 28px;color:#d6e7c0;font-size:11px;line-height:1.6;border-top:1px solid #1c8b68;\">"
                + "<strong style=\"color:#ffffff;font-size:12px;display:block;margin-bottom:3px;\">Cámara de Turismo de Capilla del Monte</strong>"
                + "Córdoba, Argentina · Portal Oficial de Promoción y Reservas<br>"
                + "<span style=\"color:#a4c28d;font-size:10px;display:block;margin-top:4px;\">Desarrollado en articulación académica por la Universidad Provincial de Córdoba (Sede Capilla del Monte).</span>"
                + "<span style=\"color:#749b3f;font-size:10px;display:block;margin-top:6px;\">Este es un mensaje institucional automático. Para consultas o asistencia, comunicate directamente con el prestador o la oficina de turismo local.</span>"
                + "</td></tr>"
                + "</table></td></tr></table></body></html>";
    }

    private String buildInvitationHtml(String recipientEmail, String token, LocalDateTime expiresAt) {
        String cleanDashboardUrl = (dashboardBaseUrl != null ? dashboardBaseUrl : "http://localhost:5174").replaceAll("/+$", "");
        String registerUrl = cleanDashboardUrl + "/registro-prestador?token=" + encode(token);
        String expirationStr = expiresAt != null ? expiresAt.format(DATE_TIME_FORMATTER) : "7 días";

        return "<!doctype html>"
                + "<html lang=\"es\"><head><meta charset=\"utf-8\">"
                + "<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">"
                + "<title>Invitación a Prestador</title></head>"
                + "<body style=\"margin:0;padding:0;background:#f4f0e8;font-family:Arial,Helvetica,sans-serif;color:#22201e;\">"
                + "<table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" style=\"background:#f4f0e8;padding:24px 12px;\">"
                + "<tr><td align=\"center\">"
                + "<table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" style=\"max-width:580px;background:#ffffff;border:1px solid #ded8cb;border-radius:18px;overflow:hidden;\">"
                + "<tr><td style=\"background:#0d2f22;padding:22px 28px;\">"
                + "<span style=\"display:block;color:#d6e7c0;font-size:11px;letter-spacing:2px;text-transform:uppercase;font-weight:700;\">Secretaría y Cámara de Turismo</span>"
                + "<span style=\"display:block;color:#ffffff;font-size:22px;font-weight:800;margin-top:6px;\">Invitación Oficial a Prestador</span>"
                + "</td></tr>"
                + "<tr><td style=\"padding:26px 28px;\">"
                + "<p style=\"margin:0 0 14px;font-size:15px;line-height:1.6;\">Hola,</p>"
                + "<p style=\"margin:0 0 16px;font-size:14px;color:#4a4640;line-height:1.6;\">La administración del <strong>Portal Oficial de Turismo de Capilla del Monte</strong> habilitó una invitación única para el correo <strong>" + escapeHtml(recipientEmail) + "</strong> con el fin de registrar y gestionar tus alojamientos en el padrón oficial.</p>"
                + "<p style=\"margin:0 0 22px;font-size:13px;color:#6b665c;\">Este enlace es personal, de un solo uso y tiene vigencia hasta el <strong>" + escapeHtml(expirationStr) + "</strong>.</p>"
                + "<a href=\"" + registerUrl + "\" style=\"display:inline-block;background:#e06d39;color:#ffffff;text-decoration:none;font-weight:700;font-size:14px;padding:13px 24px;border-radius:10px;\">Completar Registro de Prestador</a>"
                + "<p style=\"margin:20px 0 0;font-size:12px;color:#8c877a;line-height:1.5;word-break:break-all;\">Si el botón no funciona, copiá y pegá este enlace en tu navegador:<br>" + escapeHtml(registerUrl) + "</p>"
                + "</td></tr>"
                + "<tr><td style=\"background:#faf8f5;padding:16px 28px;border-top:1px solid #ece8df;color:#8c877a;font-size:11px;line-height:1.5;\">"
                + "Cámara de Turismo de Capilla del Monte · Córdoba, Argentina"
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
        String baseUrl = (portalBaseUrl != null ? portalBaseUrl : "http://localhost:5173").replaceAll("/+$", "");
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
