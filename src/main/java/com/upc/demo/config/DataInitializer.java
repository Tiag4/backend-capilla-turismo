package com.upc.demo.config;

import com.upc.demo.entidad.Accommodation;
import com.upc.demo.entidad.AccommodationImage;
import com.upc.demo.entidad.User;
import com.upc.demo.entidad.enums.AccommodationType;
import com.upc.demo.entidad.enums.Role;
import com.upc.demo.repositorio.AccommodationRepository;
import com.upc.demo.repositorio.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccommodationRepository accommodationRepository;

    @Override
    @Transactional
    public void run(String... args) {
        initAdmin();
        initHost();
    }

    private void initAdmin() {
        String adminEmail = "admin@capilladelmonte.gov.ar";
        if (userRepository.findByEmail(adminEmail).isEmpty()) {
            User admin = User.builder()
                    .email(adminEmail)
                    .password(passwordEncoder.encode("AdminCapilla2026!"))
                    .name("Comisión Oficial")
                    .lastName("Turismo Capilla")
                    .role(Role.ADMIN)
                    .phone("+54 3548 481123")
                    .build();

            userRepository.save(admin);
            log.info("✔ Usuario ADMINISTRADOR inicial creado con éxito: {}", adminEmail);
        }
    }

    private void initHost() {
        String hostEmail = "host@capilladelmonte.gov.ar";
        User host = userRepository.findByEmail(hostEmail).orElse(null);

        if (host == null) {
            host = User.builder()
                    .email(hostEmail)
                    .password(passwordEncoder.encode("HostCapilla2026!"))
                    .name("Roberto")
                    .lastName("Gómez (Cabañas del Uritorco)")
                    .role(Role.HOST)
                    .phone("+54 9 3548 456789")
                    .build();

            host = userRepository.save(host);
            log.info("✔ Usuario CABAÑERO / PRESTADOR inicial creado con éxito: {}", hostEmail);
        }

        // Crear alojamiento de muestra para el cabañero si no tiene ninguno
        if (accommodationRepository.findByHostId(host.getId()).isEmpty()) {
            Accommodation cabin = Accommodation.builder()
                    .name("Cabañas Pircas del Uritorco")
                    .description("Cabaña serrana de piedra y tronco al pie del cerro con vistas panorámicas únicas y piscina climatizada.")
                    .type(AccommodationType.CABIN)
                    .address("Camino a Los Terrones Km 3.5")
                    .locality("Capilla del Monte")
                    .latitude(-30.8654)
                    .longitude(-64.5241)
                    .pricePerNight(BigDecimal.valueOf(85000.00))
                    .maxGuests(5)
                    .amenities(new ArrayList<>(Arrays.asList("Piscina", "Wi-Fi Starlink", "Cochera techada", "Parrilla individual", "Aire acondicionado")))
                    .isActive(true)
                    .host(host)
                    .images(new ArrayList<>())
                    .build();

            AccommodationImage img1 = AccommodationImage.builder()
                    .url("https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?auto=format&fit=crop&w=1200&q=80")
                    .isMain(true)
                    .accommodation(cabin)
                    .build();

            AccommodationImage img2 = AccommodationImage.builder()
                    .url("https://images.unsplash.com/photo-1512917774080-9991f1c4c750?auto=format&fit=crop&w=1200&q=80")
                    .isMain(false)
                    .accommodation(cabin)
                    .build();

            cabin.getImages().add(img1);
            cabin.getImages().add(img2);

            accommodationRepository.save(cabin);
            log.info("✔ Alojamiento de prueba asignado al cabañero: Cabañas Pircas del Uritorco");
        }
    }
}
