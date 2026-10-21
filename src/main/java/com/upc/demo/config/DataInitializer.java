package com.upc.demo.config;

import com.upc.demo.entidad.Accommodation;
import com.upc.demo.entidad.AccommodationImage;
import com.upc.demo.entidad.User;
import com.upc.demo.entidad.enums.AccommodationType;
import com.upc.demo.entidad.enums.Role;
import com.upc.demo.entidad.Attraction;
import com.upc.demo.entidad.AttractionImage;
import com.upc.demo.entidad.enums.AttractionCategory;
import com.upc.demo.entidad.enums.AttractionStatus;
import com.upc.demo.repositorio.AttractionRepository;
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
import com.upc.demo.entidad.Alert;
import com.upc.demo.entidad.enums.AlertSeverity;
import com.upc.demo.repositorio.AlertRepository;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccommodationRepository accommodationRepository;
    private final AttractionRepository attractionRepository;
    private final AlertRepository alertRepository;

    @Override
    @Transactional
    public void run(String... args) {
        initAdmin();
        initHost();
        initAttractions();
        initAlerts();
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
                    .hasBreakfast(false)
                    .amenities(new ArrayList<>(Arrays.asList("Piscina", "Wi-Fi Starlink", "Cochera techada", "Parrilla individual", "Aire acondicionado")))
                    .downPaymentPercentage(50)
                    .bankAccountHolder("Eduardo Soria - Pircas del Uritorco")
                    .bankCbu("0110485530048500123456")
                    .bankAlias("PIRCAS.URITORCO.RIO")
                    .bankName("Banco de la Nación Argentina")
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

        // Host 2: Marcela Rossi
        String host2Email = "marcela.rossi@capillaturismo.com.ar";
        if (userRepository.findByEmail(host2Email).isEmpty()) {
            User host2 = User.builder()
                    .email(host2Email)
                    .password(passwordEncoder.encode("HostCapilla2026!"))
                    .name("Marcela")
                    .lastName("Rossi (Hostería Los Nogales)")
                    .role(Role.HOST)
                    .phone("+54 9 3548 512345")
                    .isActive(true)
                    .build();
            host2 = userRepository.save(host2);

            Accommodation hotel = Accommodation.builder()
                    .name("Hostería Boutique Los Nogales")
                    .description("Casona serrana con parque arbolado a 400 m de la Calle Techada con servicio de desayuno artesanal.")
                    .type(AccommodationType.HOTEL)
                    .address("Av. Pueyrredón 340")
                    .locality("Capilla del Monte")
                    .latitude(-30.8592)
                    .longitude(-64.5283)
                    .pricePerNight(BigDecimal.valueOf(120000.00))
                    .maxGuests(4)
                    .hasBreakfast(true)
                    .breakfastType("Desayuno serrano casero")
                    .amenities(new ArrayList<>(Arrays.asList("Desayuno serrano", "Wi-Fi", "Calefacción central", "Jardín con frutales")))
                    .downPaymentPercentage(30)
                    .bankAccountHolder("Marcela Rossi")
                    .bankCbu("0720234588000034567891")
                    .bankAlias("NOGALES.CAPILLA.BOU")
                    .bankName("Banco Santander")
                    .isActive(true)
                    .host(host2)
                    .images(new ArrayList<>())
                    .build();

            hotel.getImages().add(AccommodationImage.builder()
                    .url("https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=1200&q=80")
                    .isMain(true)
                    .accommodation(hotel)
                    .build());

            accommodationRepository.save(hotel);
            log.info("✔ Prestadora 2 registrada: Marcela Rossi - Hostería Boutique Los Nogales");
        }

        // Host 3: Carlos Benítez
        String host3Email = "carlos.benitez@capillaturismo.com.ar";
        if (userRepository.findByEmail(host3Email).isEmpty()) {
            User host3 = User.builder()
                    .email(host3Email)
                    .password(passwordEncoder.encode("HostCapilla2026!"))
                    .name("Carlos")
                    .lastName("Benítez (Refugio Los Alazanes)")
                    .role(Role.HOST)
                    .phone("+54 9 3548 678901")
                    .isActive(true)
                    .build();
            host3 = userRepository.save(host3);

            Accommodation cabin2 = Accommodation.builder()
                    .name("Refugio de Montaña Los Alazanes")
                    .description("Monoambiente de montaña con deck privado con vista al atardecer sobre el Dique El Cajón.")
                    .type(AccommodationType.APARTMENT)
                    .address("Barrio La Banda, Calle Los Quebrachos 12")
                    .locality("Capilla del Monte")
                    .latitude(-30.8711)
                    .longitude(-64.5389)
                    .pricePerNight(BigDecimal.valueOf(65000.00))
                    .maxGuests(2)
                    .hasBreakfast(false)
                    .amenities(new ArrayList<>(Arrays.asList("Wi-Fi", "Deck privado", "Parrilla", "Cocina equipada", "Pet friendly")))
                    .downPaymentPercentage(40)
                    .bankAccountHolder("Carlos Benítez")
                    .bankCbu("0070123430000045678902")
                    .bankAlias("ALAZANES.REFUGIO.MP")
                    .bankName("Mercado Pago / Banco Galicia")
                    .isActive(true)
                    .host(host3)
                    .images(new ArrayList<>())
                    .build();

            cabin2.getImages().add(AccommodationImage.builder()
                    .url("https://images.unsplash.com/photo-1510798831971-661eb04b3739?auto=format&fit=crop&w=1200&q=80")
                    .isMain(true)
                    .accommodation(cabin2)
                    .build());

            accommodationRepository.save(cabin2);
            log.info("✔ Prestador 3 registrado: Carlos Benítez - Refugio de Montaña Los Alazanes");
        }
    }

    private void initAttractions() {
        List<Attraction> list = new ArrayList<>();

            // 1. Cerro Uritorco
            Attraction uritorco = Attraction.builder()
                    .name("Cerro Uritorco")
                    .description("El pico más emblemático del Valle de Punilla con 1.979 msnm. Sendero de alta exigencia física.")
                    .category(AttractionCategory.HILL)
                    .difficulty("ALTA")
                    .estimatedDuration("7 horas ida y vuelta")
                    .howToGet("A 3 km del centro por calle Sabattini hasta el Parador La Toma.")
                    .requiresGuide(false)
                    .admissionFee(BigDecimal.valueOf(15000))
                    .latitude(-30.8522)
                    .longitude(-64.4789)
                    .status(AttractionStatus.OPEN)
                    .statusNotice(null)
                    .isFeatured(true)
                    .images(new ArrayList<>())
                    .build();
            uritorco.getImages().add(AttractionImage.builder()
                    .url("https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&w=1200&q=80")
                    .publicId("uritorco_cover")
                    .attraction(uritorco)
                    .build());
            list.add(uritorco);

            // 2. Parque Los Terrones
            Attraction terrones = Attraction.builder()
                    .name("Parque Los Terrones")
                    .description("Cañadones rojizos, laberintos geológicos esculpidos por viento y agua hace millones de años.")
                    .category(AttractionCategory.NATURE_TRAIL)
                    .difficulty("MEDIA")
                    .estimatedDuration("2.5 a 3 horas")
                    .howToGet("Ruta Nacional 38 Km 92, a 14 km al norte de Capilla del Monte.")
                    .requiresGuide(true)
                    .admissionFee(BigDecimal.valueOf(8000))
                    .latitude(-30.7711)
                    .longitude(-64.4822)
                    .status(AttractionStatus.OPEN)
                    .statusNotice(null)
                    .isFeatured(true)
                    .images(new ArrayList<>())
                    .build();
            terrones.getImages().add(AttractionImage.builder()
                    .url("https://images.unsplash.com/photo-1506744038136-46273834b3fb?auto=format&fit=crop&w=1200&q=80")
                    .publicId("terrones_cover")
                    .attraction(terrones)
                    .build());
            list.add(terrones);

            // 3. El Zapato
            Attraction zapato = Attraction.builder()
                    .name("El Zapato")
                    .description("Geoforma natural modelada por la erosión eólica con vistas panorámicas privilegiadas al lago del dique.")
                    .category(AttractionCategory.CULTURAL)
                    .difficulty("BAJA")
                    .estimatedDuration("1 hora")
                    .howToGet("A 1.5 km de la Plaza San Martín, acceso pavimentado por calle Hipólito Yrigoyen.")
                    .requiresGuide(false)
                    .admissionFee(BigDecimal.ZERO)
                    .latitude(-30.8625)
                    .longitude(-64.5386)
                    .status(AttractionStatus.OPEN)
                    .statusNotice(null)
                    .isFeatured(true)
                    .images(new ArrayList<>())
                    .build();
            zapato.getImages().add(AttractionImage.builder()
                    .url("https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?auto=format&fit=crop&w=1200&q=80")
                    .publicId("zapato_cover")
                    .attraction(zapato)
                    .build());
            list.add(zapato);

            // 4. Dique El Cajón
            Attraction cajon = Attraction.builder()
                    .name("Dique El Cajón")
                    .description("Espejo de agua ideal para pesca deportiva sin motor, avistaje de aves y paseos en kayak.")
                    .category(AttractionCategory.RIVER_BEACH)
                    .difficulty("BAJA")
                    .estimatedDuration("Paseo libre")
                    .howToGet("A 3.5 km del centro, siguiendo por Av. El Cajón.")
                    .requiresGuide(false)
                    .admissionFee(BigDecimal.ZERO)
                    .latitude(-30.8719)
                    .longitude(-64.5492)
                    .status(AttractionStatus.OPEN)
                    .statusNotice(null)
                    .isFeatured(false)
                    .images(new ArrayList<>())
                    .build();
            cajon.getImages().add(AttractionImage.builder()
                    .url("https://images.unsplash.com/photo-1501785888041-af3ef285b470?auto=format&fit=crop&w=1200&q=80")
                    .publicId("cajon_cover")
                    .attraction(cajon)
                    .build());
            list.add(cajon);

            // 5. Balneario La Toma
            Attraction laToma = Attraction.builder()
                    .name("Balneario La Toma")
                    .description("Ollas naturales, cascadas de agua de vertiente y frondosa arboleda autóctona sobre el Río Calabalumba.")
                    .category(AttractionCategory.RIVER_BEACH)
                    .difficulty("BAJA")
                    .estimatedDuration("Medio día")
                    .howToGet("Al final de la calle Sabattini, en la base oeste del Cerro Uritorco.")
                    .requiresGuide(false)
                    .admissionFee(BigDecimal.valueOf(2500))
                    .latitude(-30.8548)
                    .longitude(-64.5021)
                    .status(AttractionStatus.WEATHER_RESTRICTED)
                    .statusNotice("Precaución por caudal crecido en temporada de lluvias.")
                    .isFeatured(false)
                    .images(new ArrayList<>())
                    .build();
            laToma.getImages().add(AttractionImage.builder()
                    .url("https://images.unsplash.com/photo-1432405972618-c60b0225b8f9?auto=format&fit=crop&w=1200&q=80")
                    .publicId("latoma_cover")
                    .attraction(laToma)
                    .build());
            list.add(laToma);

            // 6. Los Mogotes
            Attraction mogotes = Attraction.builder()
                    .name("Los Mogotes y Paso del Indio")
                    .description("Garganta natural de paredes rocosas de granito con senderos y zona de escalada deportiva.")
                    .category(AttractionCategory.NATURE_TRAIL)
                    .difficulty("MEDIA")
                    .estimatedDuration("2 horas")
                    .howToGet("A 4 km al noroeste del centro sobre el Río Dolores.")
                    .requiresGuide(false)
                    .admissionFee(BigDecimal.valueOf(3000))
                    .latitude(-30.8405)
                    .longitude(-64.5498)
                    .status(AttractionStatus.OPEN)
                    .statusNotice(null)
                    .isFeatured(false)
                    .images(new ArrayList<>())
                    .build();
            mogotes.getImages().add(AttractionImage.builder()
                    .url("https://images.unsplash.com/photo-1469854523086-cc02fe5d8800?auto=format&fit=crop&w=1200&q=80")
                    .publicId("mogotes_cover")
                    .attraction(mogotes)
                    .build());
            list.add(mogotes);

            for (Attraction a : list) {
                if (!attractionRepository.existsByName(a.getName())) {
                    attractionRepository.save(a);
                    log.info("✔ Atractivo inicial registrado en base de datos: {}", a.getName());
                }
            }
    }

    private void initAlerts() {
        if (alertRepository.count() == 0) {
            Alert fireRiskAlert = Alert.builder()
                    .title("Alerta Meteorológica: Riesgo Extremo de Incendios Forestales")
                    .message("Rige índice extremo por vientos intensos y baja humedad relativa. Prohibido hacer fuego en todo el ejido municipal y zonas agrestes de Capilla del Monte.")
                    .severity(AlertSeverity.DANGER)
                    .linkUrl("https://capilladelmonte.gov.ar/defensa-civil")
                    .linkText("Protocolo de Prevención y Teléfonos de Emergencia")
                    .isActive(true)
                    .expiresAt(LocalDateTime.now().plusDays(3))
                    .build();
            alertRepository.save(fireRiskAlert);
            log.info("✔ Alerta de seguridad inicial sembrada: {}", fireRiskAlert.getTitle());
        }
    }
}
