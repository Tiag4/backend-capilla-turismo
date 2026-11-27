package com.upc.demo.servicio;

import com.upc.demo.entidad.Accommodation;
import com.upc.demo.entidad.Booking;
import com.upc.demo.entidad.User;
import com.upc.demo.entidad.enums.BookingStatus;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Properties;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private ObjectProvider<JavaMailSender> mailSenderProvider;

    @Mock
    private JavaMailSender mailSender;

    private EmailService emailService;

    @BeforeEach
    void setUp() {
        AsyncMailDispatcher dispatcher = new AsyncMailDispatcher(mailSenderProvider);
        emailService = new EmailService(mailSenderProvider, dispatcher);
    }

    @Test
    @DisplayName("sendVoucherToGuest debe enviar el email cuando el servicio está habilitado")
    void sendVoucherToGuest_enabled_sendsEmail() {
        enableMailService();
        when(mailSender.createMimeMessage())
                .thenReturn(new MimeMessage(Session.getDefaultInstance(new Properties())));
        Booking booking = buildBooking();

        boolean sent = emailService.sendVoucherToGuest(booking);

        assertThat(sent).isTrue();
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("sendVoucherToGuest no debe enviar email cuando el servicio está deshabilitado")
    void sendVoucherToGuest_disabled_skipsEmail() {
        ReflectionTestUtils.setField(emailService, "mailEnabled", false);

        boolean sent = emailService.sendVoucherToGuest(buildBooking());

        assertThat(sent).isFalse();
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("sendVoucherEmails debe enviar notificaciones bidireccionales al confirmar reserva")
    void sendVoucherEmails_confirmed_sendsToGuestAndHost() {
        enableMailService();
        when(mailSender.createMimeMessage())
                .thenAnswer(inv -> new MimeMessage(Session.getDefaultInstance(new Properties())));
        Booking booking = buildBooking();
        booking.setStatus(BookingStatus.CONFIRMED);

        boolean sent = emailService.sendVoucherEmails(booking);

        assertThat(sent).isTrue();
        verify(mailSender, times(2)).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("sendInvitationEmail debe despachar invitación al prestador cuando está habilitado")
    void sendInvitationEmail_enabled_sendsEmail() {
        enableMailService();
        when(mailSender.createMimeMessage())
                .thenReturn(new MimeMessage(Session.getDefaultInstance(new Properties())));

        emailService.sendInvitationEmail("nuevo.prestador@capilla.com", "inv_abc123", LocalDateTime.now().plusDays(7));

        verify(mailSender).send(any(MimeMessage.class));
    }

    private void enableMailService() {
        ReflectionTestUtils.setField(emailService, "mailEnabled", true);
        ReflectionTestUtils.setField(emailService, "fromAddress", "no-reply@capilladelmonte.gov.ar");
        ReflectionTestUtils.setField(emailService, "fromName", "Cámara de Turismo de Capilla del Monte");
        ReflectionTestUtils.setField(emailService, "portalBaseUrl", "http://localhost:5173");
        ReflectionTestUtils.setField(emailService, "dashboardBaseUrl", "http://localhost:5174");
        when(mailSenderProvider.getIfAvailable()).thenReturn(mailSender);
    }

    private Booking buildBooking() {
        User host = User.builder()
                .id(UUID.randomUUID())
                .name("Carla")
                .lastName("Pérez")
                .email("host@capilla.com")
                .build();
        Accommodation accommodation = Accommodation.builder()
                .id(UUID.randomUUID())
                .name("Cabañas del Uritorco")
                .address("Ruta 38 km 2")
                .locality("Capilla del Monte")
                .host(host)
                .latitude(-30.85)
                .longitude(-64.52)
                .build();

        return Booking.builder()
                .id(UUID.randomUUID())
                .bookingCode("CAP-2026-7788")
                .accommodation(accommodation)
                .checkIn(LocalDate.now().minusDays(2))
                .checkOut(LocalDate.now().plusDays(2))
                .totalNights(4)
                .guestCount(2)
                .guestName("Martina Gómez")
                .guestEmail("turista@email.com")
                .guestPhone("+54 9 351 555-1234")
                .pricePerNight(BigDecimal.valueOf(95000))
                .totalAmount(BigDecimal.valueOf(380000))
                .downPaymentPercentage(50)
                .downPaymentAmount(BigDecimal.valueOf(190000))
                .remainingBalance(BigDecimal.valueOf(190000))
                .status(BookingStatus.PENDING)
                .build();
    }
}
