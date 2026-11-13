package com.upc.demo.config;

import com.upc.demo.entidad.Accommodation;
import com.upc.demo.entidad.AccommodationImage;
import com.upc.demo.entidad.Alert;
import com.upc.demo.entidad.Attraction;
import com.upc.demo.entidad.AttractionImage;
import com.upc.demo.entidad.Event;
import com.upc.demo.entidad.HealthGuardNotice;
import com.upc.demo.entidad.Pharmacy;
import com.upc.demo.entidad.RatePlan;
import com.upc.demo.entidad.RoomType;
import com.upc.demo.entidad.TourismSeason;
import com.upc.demo.entidad.User;
import com.upc.demo.entidad.enums.SeasonType;
import com.upc.demo.entidad.enums.AccommodationType;
import com.upc.demo.entidad.enums.AlertSeverity;
import com.upc.demo.entidad.enums.AttractionCategory;
import com.upc.demo.entidad.enums.AttractionStatus;
import com.upc.demo.entidad.enums.EventCategory;
import com.upc.demo.entidad.enums.Role;
import com.upc.demo.repositorio.AccommodationRepository;
import com.upc.demo.repositorio.AlertRepository;
import com.upc.demo.repositorio.AttractionRepository;
import com.upc.demo.repositorio.EventRepository;
import com.upc.demo.repositorio.HealthGuardNoticeRepository;
import com.upc.demo.repositorio.PharmacyRepository;
import com.upc.demo.repositorio.RatePlanRepository;
import com.upc.demo.repositorio.RoomTypeRepository;
import com.upc.demo.repositorio.TourismSeasonRepository;
import com.upc.demo.repositorio.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private final AttractionRepository attractionRepository;
    private final AlertRepository alertRepository;
    private final EventRepository eventRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final RatePlanRepository ratePlanRepository;
    private final PharmacyRepository pharmacyRepository;
    private final HealthGuardNoticeRepository healthGuardNoticeRepository;
    private final TourismSeasonRepository tourismSeasonRepository;

    @Override
    @Transactional
    public void run(String... args) {
        initAdmin();
        initHost();
        initAttractions();
        initAlerts();
        initEvents();
        initEmergencySanitary();
        initSeasons();
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

        // Inventario multi-unidad de la hostería: se siembra una sola vez por alojamiento
        userRepository.findByEmail(host2Email)
                .flatMap(host2Saved -> accommodationRepository.findByHostId(host2Saved.getId()).stream()
                        .filter(a -> "Hostería Boutique Los Nogales".equals(a.getName()))
                        .findFirst())
                .ifPresent(this::initRoomTypesForHotel);
    }

    private void initRoomTypesForHotel(Accommodation hotel) {
        if (roomTypeRepository.countByAccommodationId(hotel.getId()) > 0) {
            return;
        }

        RoomType standardDoble = RoomType.builder()
                .accommodation(hotel)
                .name("Standard Doble")
                .description("Habitación doble con baño privado, ideal para parejas o viajeros solos.")
                .baseCapacity(2)
                .maxCapacity(2)
                .beds(2)
                .totalUnits(6)
                .basePricePerNight(new BigDecimal("120000.00"))
                .isActive(true)
                .amenities(new ArrayList<>(Arrays.asList("Wi-Fi", "TV", "Baño privado")))
                .ratePlans(new ArrayList<>())
                .build();

        RatePlan tarifaFlexible = RatePlan.builder()
                .roomType(standardDoble)
                .name("Tarifa flexible")
                .description("Cancelación sin cargo hasta 48 horas antes del check-in.")
                .pricePerNight(new BigDecimal("120000.00"))
                .includesBreakfast(false)
                .freeCancellation(true)
                .minNights(1)
                .isActive(true)
                .build();

        RatePlan desayunoIncluido = RatePlan.builder()
                .roomType(standardDoble)
                .name("Desayuno incluido")
                .description("Incluye desayuno serrano casero servido en el parque.")
                .pricePerNight(new BigDecimal("135000.00"))
                .includesBreakfast(true)
                .freeCancellation(true)
                .minNights(1)
                .isActive(true)
                .build();

        standardDoble.getRatePlans().add(tarifaFlexible);
        standardDoble.getRatePlans().add(desayunoIncluido);

        RoomType superior = RoomType.builder()
                .accommodation(hotel)
                .name("Superior")
                .description("Habitación amplia con vista al cerro y espacio para una cama adicional.")
                .baseCapacity(2)
                .maxCapacity(3)
                .beds(2)
                .totalUnits(3)
                .basePricePerNight(new BigDecimal("165000.00"))
                .isActive(true)
                .amenities(new ArrayList<>(Arrays.asList("Wi-Fi", "TV", "Baño privado", "Vista al cerro")))
                .ratePlans(new ArrayList<>())
                .build();

        RoomType suite = RoomType.builder()
                .accommodation(hotel)
                .name("Suite")
                .description("Suite con jacuzzi, vista al cerro y sector de estar independiente.")
                .baseCapacity(3)
                .maxCapacity(4)
                .beds(3)
                .totalUnits(1)
                .basePricePerNight(new BigDecimal("220000.00"))
                .isActive(true)
                .amenities(new ArrayList<>(Arrays.asList("Wi-Fi", "TV", "Baño privado", "Vista al cerro", "Jacuzzi")))
                .ratePlans(new ArrayList<>())
                .build();

        roomTypeRepository.saveAll(List.of(standardDoble, superior, suite));
        ratePlanRepository.saveAll(List.of(tarifaFlexible, desayunoIncluido));

        log.info("✔ Inventario multi-unidad sembrado para la Hostería Boutique Los Nogales (3 tipos de habitación)");
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

    private void initEvents() {
        if (eventRepository.count() == 0) {
            List<Event> events = List.of(
                Event.builder()
                    .title("Festival del Alienígena 2026")
                    .description("La fiesta temática de ovnilogía y cultura pop más convocante del Valle de Punilla, con desfile de disfraces, música en vivo y conferencias.")
                    .category(EventCategory.FESTIVAL)
                    .startDate(LocalDate.now().plusDays(5))
                    .endDate(LocalDate.now().plusDays(7))
                    .timeString("19:00 a 02:00 hs")
                    .location("Predio Gabriel Suárez y Calle Techada")
                    .isFree(true)
                    .imageUrl("https://images.unsplash.com/photo-1514525253161-7a46d19cd819?auto=format&fit=crop&w=1200&q=80")
                    .isActive(true)
                    .isFeatured(true)
                    .build(),
                Event.builder()
                    .title("Noche de Astroturismo y Leyendas Serranas")
                    .description("Observación guiada con telescopios de cielo profundo, astrofotografía y relatos ancestrales de la cultura comechingón.")
                    .category(EventCategory.COMMUNITY)
                    .startDate(LocalDate.now().plusDays(10))
                    .timeString("20:30 hs")
                    .location("Base del Cerro Uritorco")
                    .isFree(false)
                    .price(BigDecimal.valueOf(15000))
                    .imageUrl("https://images.unsplash.com/photo-1506703719100-a0f3a48c0f86?auto=format&fit=crop&w=1200&q=80")
                    .isActive(true)
                    .isFeatured(false)
                    .build(),
                Event.builder()
                    .title("Feria de Sabores y Artesanías del Uritorco")
                    .description("Paseo gastronómico con dulces regionales, miel serrana, cervecerías artesanales y feria de artesanos locales.")
                    .category(EventCategory.FAIR)
                    .startDate(LocalDate.now().plusDays(2))
                    .endDate(LocalDate.now().plusDays(4))
                    .timeString("11:00 a 21:00 hs")
                    .location("Plaza San Martín")
                    .isFree(true)
                    .imageUrl("https://images.unsplash.com/photo-1533900298318-6b8da08a523e?auto=format&fit=crop&w=1200&q=80")
                    .isActive(true)
                    .isFeatured(false)
                    .build()
            );

            eventRepository.saveAll(events);
            log.info("✔ Eventos culturales iniciales sembrados con éxito (3 eventos)");
        }
    }

    private void initEmergencySanitary() {
        if (pharmacyRepository.count() == 0) {
            List<Pharmacy> defaultPharmacies = List.of(
                    Pharmacy.builder()
                            .name("Farmacia Pasteur")
                            .address("Diagonal Buenos Aires 45")
                            .phone("+54 3548 48-1250")
                            .whatsapp("+54 9 3548 481250")
                            .dutyHours("Guardia 24 Horas")
                            .validFrom(LocalDate.now().toString())
                            .validTo(LocalDate.now().plusDays(7).toString())
                            .isOnDutyToday(true)
                            .notes("Atención nocturna por ventanilla permanente sobre Diagonal Buenos Aires.")
                            .googleMapsUrl("https://maps.google.com/?q=Diagonal+Buenos+Aires+45,+Capilla+del+Monte")
                            .build(),
                    Pharmacy.builder()
                            .name("Farmacia Capilla")
                            .address("Pueyrredón 210")
                            .phone("+54 3548 48-2100")
                            .whatsapp("+54 9 3548 482100")
                            .dutyHours("08:30 a 13:00 y 16:30 a 21:00 hs")
                            .isOnDutyToday(false)
                            .notes("A 50 metros de Plaza San Martín y Calle Techada.")
                            .googleMapsUrl("https://maps.google.com/?q=Pueyrredon+210,+Capilla+del+Monte")
                            .build(),
                    Pharmacy.builder()
                            .name("Farmacia del Valle")
                            .address("Deán Funes 320")
                            .phone("+54 3548 48-1800")
                            .whatsapp("+54 9 3548 481800")
                            .dutyHours("08:30 a 13:00 y 17:00 a 21:30 hs")
                            .isOnDutyToday(false)
                            .notes("A 2 cuadras del Hospital Municipal Dr. Amancio Rodríguez.")
                            .googleMapsUrl("https://maps.google.com/?q=Dean+Funes+320,+Capilla+del+Monte")
                            .build(),
                    Pharmacy.builder()
                            .name("Farmacia San Antonio")
                            .address("Hipólito Yrigoyen 412")
                            .phone("+54 3548 48-1555")
                            .dutyHours("09:00 a 13:00 y 17:00 a 21:00 hs")
                            .isOnDutyToday(false)
                            .notes("Frente a la Terminal de Ómnibus de Capilla del Monte.")
                            .googleMapsUrl("https://maps.google.com/?q=Hipolito+Yrigoyen+412,+Capilla+del+Monte")
                            .build()
            );
            pharmacyRepository.saveAll(defaultPharmacies);
            log.info("✔ Farmacias iniciales del ejido urbano sembradas con éxito (4 farmacias)");
        }

        if (healthGuardNoticeRepository.count() == 0) {
            HealthGuardNotice defaultHospital = HealthGuardNotice.builder()
                    .facilityName("Hospital Municipal Dr. Amancio Rodríguez")
                    .emergencyPhone("107")
                    .directPhone("+54 3548 481232")
                    .address("Deán Funes 530")
                    .activeGuards(List.of(
                            "Guardia Médica Clínica Permanente (24 Horas)",
                            "Shock Room de Traumatología y Urgencias",
                            "Provisión Inmediata de Suero Antiofídico y Antiescorpiónico",
                            "Guardia Pediátrica de Refuerzo"
                    ))
                    .specialNotice("Guardia médica activa las 24 horas todos los días del año. Ante mordeduras de ofidios o picaduras de alacrán, no realizar torniquetes y concurrir inmediatamente a la guardia.")
                    .updatedAt(LocalDateTime.now())
                    .build();
            healthGuardNoticeRepository.save(defaultHospital);
            log.info("✔ Ficha oficial de guardia médica del Hospital Municipal sembrada con éxito");
        }
    }

    private void initSeasons() {
        if (tourismSeasonRepository.count() == 0) {
            List<TourismSeason> defaultSeasons = List.of(
                    TourismSeason.builder()
                            .name("Temporada Alta de Verano")
                            .type(SeasonType.HIGH)
                            .startDate(LocalDate.of(2027, 1, 1))
                            .endDate(LocalDate.of(2027, 2, 28))
                            .suggestedMultiplier(1.4)
                            .description("Afluencia masiva a balnearios (La Toma, Calabalumba) y ascenso nocturno al Uritorco.")
                            .isActive(true)
                            .build(),
                    TourismSeason.builder()
                            .name("Festival Alienígena & Encuentro OVNI")
                            .type(SeasonType.SPECIAL_EVENT)
                            .startDate(LocalDate.of(2027, 2, 12))
                            .endDate(LocalDate.of(2027, 2, 16))
                            .suggestedMultiplier(1.6)
                            .description("Fiesta oficial en la Calle Techada con desfile de disfraces, conferencias y ocupación al 100%.")
                            .isActive(true)
                            .build(),
                    TourismSeason.builder()
                            .name("Semana Santa Serrana")
                            .type(SeasonType.HIGH)
                            .startDate(LocalDate.of(2027, 3, 25))
                            .endDate(LocalDate.of(2027, 3, 29))
                            .suggestedMultiplier(1.5)
                            .description("Fin de semana largo de turismo religioso, senderismo en Los Terrones y gastronomía regional.")
                            .isActive(true)
                            .build(),
                    TourismSeason.builder()
                            .name("Vacaciones de Invierno")
                            .type(SeasonType.MEDIUM)
                            .startDate(LocalDate.of(2026, 7, 6))
                            .endDate(LocalDate.of(2026, 7, 26))
                            .suggestedMultiplier(1.3)
                            .description("Receso invernal escolar nacional con turismo familiar y ecoturismo.")
                            .isActive(true)
                            .build()
            );
            tourismSeasonRepository.saveAll(defaultSeasons);
            log.info("Temporadas turisticas iniciales sembradas con exito (4 temporadas)");
        }
    }
}
