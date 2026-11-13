package com.upc.demo.config;

import com.upc.demo.entidad.Accommodation;
import com.upc.demo.entidad.AccommodationImage;
import com.upc.demo.entidad.Alert;
import com.upc.demo.entidad.Attraction;
import com.upc.demo.entidad.AttractionImage;
import com.upc.demo.entidad.Booking;
import com.upc.demo.entidad.Event;
import com.upc.demo.entidad.HealthGuardNotice;
import com.upc.demo.entidad.Pharmacy;
import com.upc.demo.entidad.RatePlan;
import com.upc.demo.entidad.Review;
import com.upc.demo.entidad.RoomType;
import com.upc.demo.entidad.User;
import com.upc.demo.entidad.enums.AccommodationType;
import com.upc.demo.entidad.enums.AlertSeverity;
import com.upc.demo.entidad.enums.AttractionCategory;
import com.upc.demo.entidad.enums.AttractionStatus;
import com.upc.demo.entidad.enums.BookingStatus;
import com.upc.demo.entidad.enums.EventCategory;
import com.upc.demo.entidad.enums.ReviewStatus;
import com.upc.demo.entidad.enums.Role;
import com.upc.demo.repositorio.AccommodationRepository;
import com.upc.demo.repositorio.AlertRepository;
import com.upc.demo.repositorio.AttractionRepository;
import com.upc.demo.repositorio.BookingRepository;
import com.upc.demo.repositorio.EventRepository;
import com.upc.demo.repositorio.HealthGuardNoticeRepository;
import com.upc.demo.repositorio.PharmacyRepository;
import com.upc.demo.repositorio.RatePlanRepository;
import com.upc.demo.repositorio.ReviewRepository;
import com.upc.demo.repositorio.RoomTypeRepository;
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
    private final BookingRepository bookingRepository;
    private final ReviewRepository reviewRepository;

    @Override
    @Transactional
    public void run(String... args) {
        initAdmin();
        initHost();
        initAttractions();
        initAlerts();
        initEvents();
        initEmergencySanitary();
        initDemoBookingsAndReviews();
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
        // Garantizar que todos los usuarios tengan isActive = true para que isHostEnabled no los descarte
        userRepository.findAll().forEach(u -> {
            if (u.getIsActive() == null || !u.getIsActive()) {
                u.setIsActive(true);
                userRepository.save(u);
            }
        });

        // ==========================================
        // Host 1: Roberto Gómez - Cabañas El Aguaribay
        // ==========================================
        String host1Email = "host@capilladelmonte.gov.ar";
        User host1 = userRepository.findByEmail(host1Email).orElseGet(() ->
                userRepository.save(User.builder()
                        .email(host1Email)
                        .password(passwordEncoder.encode("HostCapilla2026!"))
                        .name("Roberto")
                        .lastName("Gómez (Cabañas El Aguaribay)")
                        .role(Role.HOST)
                        .phone("+54 9 3548 481234")
                        .isActive(true)
                        .build())
        );
        host1.setName("Roberto");
        host1.setLastName("Gómez (Cabañas El Aguaribay)");
        host1.setIsActive(true);
        userRepository.save(host1);

        Accommodation aguaribay = accommodationRepository.findByHostId(host1.getId()).stream()
                .findFirst()
                .orElseGet(() -> Accommodation.builder().host(host1).build());

        aguaribay.setName("Cabañas El Aguaribay");
        aguaribay.setDescription("Cabaña serrana de tronco y piedra al pie de las sierras con parque autóctono privado.\n\nEl predio cuenta con piscina cercada con solárium, cocheras cubiertas, asadores individuales y vista abierta al Cerro Uritorco.\n\nAtención personalizada por sus dueños durante toda la estadía, con asesoramiento para excursiones y trekking.");
        aguaribay.setType(AccommodationType.CABIN);
        aguaribay.setAddress("Calle Los Terrones 450");
        aguaribay.setLocality("Capilla del Monte");
        aguaribay.setLatitude(-30.8640);
        aguaribay.setLongitude(-64.5310);
        aguaribay.setPricePerNight(BigDecimal.valueOf(85000.00));
        aguaribay.setMaxGuests(5);
        aguaribay.setUnitTitle("Cabaña de Tronco y Piedra 2 Ambientes");
        aguaribay.setSurfaceSqm(60);
        aguaribay.setBedroomsSummary("Dormitorio 1: 1 cama matrimonial sommier | Dormitorio 2: 3 camas individuales");
        aguaribay.setPhone("+54 9 3548 481234");
        aguaribay.setCheckInTime("14:00 hs");
        aguaribay.setCheckOutTime("10:00 hs");
        aguaribay.setPetPolicy("Mascotas medianas y pequeñas bienvenidas con aviso previo en parque.");
        aguaribay.setCancellationPolicy("Cancelación flexible hasta 7 días antes de la fecha de ingreso.");
        aguaribay.setLicenseNumber("HAB-2024/089");
        aguaribay.setResponseTime("Responde habitualmente en menos de 1 hora");
        aguaribay.setHasBreakfast(false);
        aguaribay.setAmenities(new ArrayList<>(Arrays.asList("Piscina", "Wi-Fi", "Asador individual", "Cochera techada", "Parque autóctono", "Aire acondicionado", "Cocina equipada")));
        aguaribay.setDownPaymentPercentage(50);
        aguaribay.setBankAccountHolder("Roberto Gómez - Cabañas El Aguaribay");
        aguaribay.setBankCbu("0110485530048500123456");
        aguaribay.setBankAlias("AGUARIBAY.CAPILLA.RIO");
        aguaribay.setBankName("Banco de la Nación Argentina");
        aguaribay.setIsActive(true);
        aguaribay.setHost(host1);
        if (aguaribay.getImages() == null) aguaribay.setImages(new ArrayList<>());
        aguaribay.getImages().clear();
        List<String> aguaribayImgs = List.of(
                "/images/accommodations/el-aguaribay/el-aguaribay-01.jpg",
                "/images/accommodations/el-aguaribay/el-aguaribay-02.jpg",
                "/images/accommodations/el-aguaribay/el-aguaribay-03.jpg",
                "/images/accommodations/el-aguaribay/el-aguaribay-04.jpg",
                "/images/accommodations/el-aguaribay/el-aguaribay-05.jpg",
                "/images/accommodations/el-aguaribay/el-aguaribay-06.jpg",
                "/images/accommodations/el-aguaribay/el-aguaribay-07.jpg"
        );
        for (int i = 0; i < aguaribayImgs.size(); i++) {
            aguaribay.getImages().add(AccommodationImage.builder()
                    .url(aguaribayImgs.get(i))
                    .isMain(i == 0)
                    .accommodation(aguaribay)
                    .build());
        }
        accommodationRepository.save(aguaribay);
        log.info("✔ Alojamiento 1 sincronizado: Cabañas El Aguaribay");

        // ==========================================
        // Host 2: Esteban Morales - Cabañas Alpa Sierras
        // ==========================================
        String host2Email = "host.alpasierras@capillaturismo.com.ar";
        User host2 = userRepository.findByEmail(host2Email).orElseGet(() ->
                userRepository.save(User.builder()
                        .email(host2Email)
                        .password(passwordEncoder.encode("HostCapilla2026!"))
                        .name("Esteban")
                        .lastName("Morales (Cabañas Alpa Sierras)")
                        .role(Role.HOST)
                        .phone("+54 9 3548 591244")
                        .isActive(true)
                        .build())
        );
        host2.setName("Esteban");
        host2.setLastName("Morales (Cabañas Alpa Sierras)");
        host2.setIsActive(true);
        userRepository.save(host2);

        Accommodation alpa = accommodationRepository.findByHostId(host2.getId()).stream()
                .findFirst()
                .orElseGet(() -> Accommodation.builder().host(host2).build());

        alpa.setName("Cabañas Alpa Sierras");
        alpa.setDescription("Chalet de montaña inmerso en la tranquilidad del Barrio Las Gemelas.\n\nConstrucción de excelente calidad con deck privado, piscina con vista abierta a las cumbres y asador techado.\n\nUn espacio pensado para el descanso profundo y la reconexión con la naturaleza serrana de Capilla del Monte.");
        alpa.setType(AccommodationType.CABIN);
        alpa.setAddress("Camino a Las Gemelas s/n");
        alpa.setLocality("Capilla del Monte");
        alpa.setLatitude(-30.8525);
        alpa.setLongitude(-64.5180);
        alpa.setPricePerNight(BigDecimal.valueOf(95000.00));
        alpa.setMaxGuests(4);
        alpa.setUnitTitle("Chalet Serrano con Vista Panorámica al Uritorco");
        alpa.setSurfaceSqm(55);
        alpa.setBedroomsSummary("Dormitorio Principal: 1 cama King Size | Living/Estar: 2 camas individuales sommier");
        alpa.setPhone("+54 9 3548 591244");
        alpa.setCheckInTime("15:00 hs");
        alpa.setCheckOutTime("11:00 hs");
        alpa.setPetPolicy("Mascotas medianas educadas permitidas previa consulta.");
        alpa.setCancellationPolicy("Cancelación flexible hasta 5 días antes de la llegada.");
        alpa.setLicenseNumber("HAB-2023/142");
        alpa.setResponseTime("Responde habitualmente en el día");
        alpa.setHasBreakfast(false);
        alpa.setAmenities(new ArrayList<>(Arrays.asList("Piscina con solárium", "Vista al Cerro Uritorco", "Wi-Fi alta velocidad", "Asador techado", "Cochera cubierta", "Calefacción tiro balanceado")));
        alpa.setDownPaymentPercentage(40);
        alpa.setBankAccountHolder("Esteban Morales");
        alpa.setBankCbu("0110234588000034567892");
        alpa.setBankAlias("ALPA.SIERRAS.CAPILLA");
        alpa.setBankName("Banco de la Nación Argentina");
        alpa.setIsActive(true);
        alpa.setHost(host2);
        if (alpa.getImages() == null) alpa.setImages(new ArrayList<>());
        alpa.getImages().clear();
        List<String> alpaImgs = List.of(
                "/images/accommodations/alpa-sierras/alpa-sierras-01.jpg",
                "/images/accommodations/alpa-sierras/alpa-sierras-02.jpg",
                "/images/accommodations/alpa-sierras/alpa-sierras-03.jpg",
                "/images/accommodations/alpa-sierras/alpa-sierras-04.jpg",
                "/images/accommodations/alpa-sierras/alpa-sierras-05.jpg",
                "/images/accommodations/alpa-sierras/alpa-sierras-06.jpg",
                "/images/accommodations/alpa-sierras/alpa-sierras-07.jpg"
        );
        for (int i = 0; i < alpaImgs.size(); i++) {
            alpa.getImages().add(AccommodationImage.builder()
                    .url(alpaImgs.get(i))
                    .isMain(i == 0)
                    .accommodation(alpa)
                    .build());
        }
        accommodationRepository.save(alpa);
        log.info("✔ Alojamiento 2 sincronizado: Cabañas Alpa Sierras");

        // ==========================================
        // Host 3: Adriana Valenzuela - Hostería Adrianita
        // ==========================================
        String host3Email = "host.adrianita@capillaturismo.com.ar";
        User host3 = userRepository.findByEmail(host3Email).orElseGet(() ->
                userRepository.save(User.builder()
                        .email(host3Email)
                        .password(passwordEncoder.encode("HostCapilla2026!"))
                        .name("Adriana")
                        .lastName("Valenzuela (Hostería Adrianita)")
                        .role(Role.HOST)
                        .phone("+54 9 3548 421890")
                        .isActive(true)
                        .build())
        );
        host3.setName("Adriana");
        host3.setLastName("Valenzuela (Hostería Adrianita)");
        host3.setIsActive(true);
        userRepository.save(host3);

        Accommodation adrianita = accommodationRepository.findByHostId(host3.getId()).stream()
                .findFirst()
                .orElseGet(() -> Accommodation.builder().host(host3).build());

        adrianita.setName("Hostería Adrianita");
        adrianita.setDescription("Hostería tradicional atendida por sus propios dueños en el corazón urbano de Capilla del Monte.\n\nHabitaciones confortables con baño privado, aire acondicionado, calefacción y atención permanente las 24 horas.\n\nCada mañana se sirve desayuno serrano con dulces de la región y panificados artesanales para comenzar el día.");
        adrianita.setType(AccommodationType.HOTEL);
        adrianita.setAddress("Hipólito Yrigoyen 245");
        adrianita.setLocality("Capilla del Monte");
        adrianita.setLatitude(-30.8585);
        adrianita.setLongitude(-64.5245);
        adrianita.setPricePerNight(BigDecimal.valueOf(70000.00));
        adrianita.setMaxGuests(3);
        adrianita.setUnitTitle("Habitación Doble / Triple con Desayuno Serrano");
        adrianita.setSurfaceSqm(32);
        adrianita.setBedroomsSummary("Habitación: 1 cama matrimonial sommier + 1 cama individual");
        adrianita.setPhone("+54 9 3548 421890");
        adrianita.setCheckInTime("13:00 hs");
        adrianita.setCheckOutTime("10:00 hs");
        adrianita.setPetPolicy("No se admiten mascotas en las habitaciones.");
        adrianita.setCancellationPolicy("Cancelación flexible hasta 48 horas antes de la llegada.");
        adrianita.setLicenseNumber("HAB-2022/056");
        adrianita.setResponseTime("Responde en menos de 30 minutos");
        adrianita.setHasBreakfast(true);
        adrianita.setBreakfastType("Desayuno serrano casero");
        adrianita.setAmenities(new ArrayList<>(Arrays.asList("Desayuno incluido", "Wi-Fi", "Ubicación céntrica", "Recepción 24hs", "Aire acondicionado", "Baño privado", "Calefacción")));
        adrianita.setDownPaymentPercentage(30);
        adrianita.setBankAccountHolder("Adriana Valenzuela");
        adrianita.setBankCbu("0720234588000034567893");
        adrianita.setBankAlias("ADRIANITA.CAPILLA");
        adrianita.setBankName("Banco Santander");
        adrianita.setIsActive(true);
        adrianita.setHost(host3);
        if (adrianita.getImages() == null) adrianita.setImages(new ArrayList<>());
        adrianita.getImages().clear();
        List<String> adrianitaImgs = List.of(
                "/images/accommodations/adrianita-hosteria/adrianita-01.jpg",
                "/images/accommodations/adrianita-hosteria/adrianita-02.jpg",
                "/images/accommodations/adrianita-hosteria/adrianita-03.jpg",
                "/images/accommodations/adrianita-hosteria/adrianita-04.jpg",
                "/images/accommodations/adrianita-hosteria/adrianita-05.jpg"
        );
        for (int i = 0; i < adrianitaImgs.size(); i++) {
            adrianita.getImages().add(AccommodationImage.builder()
                    .url(adrianitaImgs.get(i))
                    .isMain(i == 0)
                    .accommodation(adrianita)
                    .build());
        }
        accommodationRepository.save(adrianita);
        log.info("✔ Alojamiento 3 sincronizado: Hostería Adrianita");

        // ==========================================
        // Host 4: Marcela Rossi - Hostería La Atalaya
        // ==========================================
        String host4Email = "marcela.rossi@capillaturismo.com.ar";
        User host4 = userRepository.findByEmail(host4Email).orElseGet(() ->
                userRepository.save(User.builder()
                        .email(host4Email)
                        .password(passwordEncoder.encode("HostCapilla2026!"))
                        .name("Marcela")
                        .lastName("Rossi (Hostería La Atalaya)")
                        .role(Role.HOST)
                        .phone("+54 9 3548 643210")
                        .isActive(true)
                        .build())
        );
        host4.setName("Marcela");
        host4.setLastName("Rossi (Hostería La Atalaya)");
        host4.setIsActive(true);
        userRepository.save(host4);

        Accommodation atalaya = accommodationRepository.findByHostId(host4.getId()).stream()
                .findFirst()
                .orElseGet(() -> Accommodation.builder().host(host4).build());

        atalaya.setName("Hostería La Atalaya");
        atalaya.setDescription("Emblemática hostería en el Barrio La Banda que conjuga arquitectura serrana con un parque añoso de frutales.\n\nDeck panorámico para contemplar la caída del sol, piscina y desayuno buffet casero elaborado a diario.\n\nUbicada a pasos de los senderos del perilago y del paseo hacia El Zapato.");
        atalaya.setType(AccommodationType.HOTEL);
        atalaya.setAddress("Diagonal Buenos Aires 820");
        atalaya.setLocality("Capilla del Monte");
        atalaya.setLatitude(-30.8670);
        atalaya.setLongitude(-64.5360);
        atalaya.setPricePerNight(BigDecimal.valueOf(110000.00));
        atalaya.setMaxGuests(4);
        atalaya.setUnitTitle("Suite Serrana con Vista Panorámica y Parque");
        atalaya.setSurfaceSqm(42);
        atalaya.setBedroomsSummary("Dormitorio Principal: 1 sommier King Size | Sala de Estar: 1 diván cama doble");
        atalaya.setPhone("+54 9 3548 643210");
        atalaya.setCheckInTime("14:00 hs");
        atalaya.setCheckOutTime("11:00 hs");
        atalaya.setPetPolicy("Consultar admisión de mascotas previamente.");
        atalaya.setCancellationPolicy("Cancelación flexible hasta 7 días antes de la llegada.");
        atalaya.setLicenseNumber("HAB-2023/210");
        atalaya.setResponseTime("Responde habitualmente en el día");
        atalaya.setHasBreakfast(true);
        atalaya.setBreakfastType("Desayuno buffet serrano casero");
        atalaya.setAmenities(new ArrayList<>(Arrays.asList("Desayuno buffet serrano", "Piscina", "Parque arbolado", "Wi-Fi Fibra", "Deck con vista panorámica", "Estacionamiento privado")));
        atalaya.setDownPaymentPercentage(30);
        atalaya.setBankAccountHolder("Marcela Rossi");
        atalaya.setBankCbu("0720234588000034567891");
        atalaya.setBankAlias("ATALAYA.CAPILLA.BOU");
        atalaya.setBankName("Banco Santander");
        atalaya.setIsActive(true);
        atalaya.setHost(host4);
        if (atalaya.getImages() == null) atalaya.setImages(new ArrayList<>());
        atalaya.getImages().clear();
        List<String> atalayaImgs = List.of(
                "/images/accommodations/la-atalaya/la-atalaya-01.jpg",
                "/images/accommodations/la-atalaya/la-atalaya-02.jpg",
                "/images/accommodations/la-atalaya/la-atalaya-03.jpg",
                "/images/accommodations/la-atalaya/la-atalaya-04.jpg",
                "/images/accommodations/la-atalaya/la-atalaya-05.jpg",
                "/images/accommodations/la-atalaya/la-atalaya-06.jpg",
                "/images/accommodations/la-atalaya/la-atalaya-07.jpg"
        );
        for (int i = 0; i < atalayaImgs.size(); i++) {
            atalaya.getImages().add(AccommodationImage.builder()
                    .url(atalayaImgs.get(i))
                    .isMain(i == 0)
                    .accommodation(atalaya)
                    .build());
        }
        accommodationRepository.save(atalaya);
        initRoomTypesForHotel(atalaya);
        log.info("✔ Alojamiento 4 sincronizado: Hostería La Atalaya");

        // ==========================================
        // Host 5: Carlos Benítez - Complejo Turístico Casablanca
        // ==========================================
        String host5Email = "carlos.benitez@capillaturismo.com.ar";
        User host5 = userRepository.findByEmail(host5Email).orElseGet(() ->
                userRepository.save(User.builder()
                        .email(host5Email)
                        .password(passwordEncoder.encode("HostCapilla2026!"))
                        .name("Carlos")
                        .lastName("Benítez (Complejo Casablanca)")
                        .role(Role.HOST)
                        .phone("+54 9 3548 765432")
                        .isActive(true)
                        .build())
        );
        host5.setName("Carlos");
        host5.setLastName("Benítez (Complejo Casablanca)");
        host5.setIsActive(true);
        userRepository.save(host5);

        Accommodation casablanca = accommodationRepository.findByHostId(host5.getId()).stream()
                .findFirst()
                .orElseGet(() -> Accommodation.builder().host(host5).build());

        casablanca.setName("Complejo Turístico Casablanca");
        casablanca.setDescription("Complejo vacacional de más de 5.000 m² de parque serrano con arboledas autóctonas.\n\nCabañas totalmente equipadas con cocina completa, cocheras cubiertas, asadores y pileta de natación olímpica.\n\nEl lugar perfecto para familias y grupos que buscan comodidad, naturaleza y espacio al aire libre en Capilla del Monte.");
        casablanca.setType(AccommodationType.CABIN);
        casablanca.setAddress("Ruta Nacional 38 Km 84");
        casablanca.setLocality("Capilla del Monte");
        casablanca.setLatitude(-30.8450);
        casablanca.setLongitude(-64.5290);
        casablanca.setPricePerNight(BigDecimal.valueOf(130000.00));
        casablanca.setMaxGuests(6);
        casablanca.setUnitTitle("Cabaña Familiar en Complejo con Gran Parque y Piscina");
        casablanca.setSurfaceSqm(75);
        casablanca.setBedroomsSummary("Dormitorio 1: 1 cama matrimonial King | Dormitorio 2: 2 sommiers individuales | Estar: 1 sofá cama doble");
        casablanca.setPhone("+54 9 3548 765432");
        casablanca.setCheckInTime("15:00 hs");
        casablanca.setCheckOutTime("10:30 hs");
        casablanca.setPetPolicy("Pet friendly en cabañas seleccionadas con costo adicional.");
        casablanca.setCancellationPolicy("Cancelación moderada hasta 10 días antes de la llegada.");
        casablanca.setLicenseNumber("HAB-2021/304");
        casablanca.setResponseTime("Responde en menos de 2 horas");
        casablanca.setHasBreakfast(false);
        casablanca.setAmenities(new ArrayList<>(Arrays.asList("Piscina olímpica cercada", "Quincho y asadores", "Parque de 5000 m²", "Cocheras cubiertas", "Wi-Fi", "Cocina completa")));
        casablanca.setDownPaymentPercentage(40);
        casablanca.setBankAccountHolder("Carlos Benítez");
        casablanca.setBankCbu("0070123430000045678902");
        casablanca.setBankAlias("CASABLANCA.CAPILLA");
        casablanca.setBankName("Mercado Pago / Banco Galicia");
        casablanca.setIsActive(true);
        casablanca.setHost(host5);
        if (casablanca.getImages() == null) casablanca.setImages(new ArrayList<>());
        casablanca.getImages().clear();
        List<String> casablancaImgs = List.of(
                "/images/accommodations/complejo-casablanca/casablanca-01.png",
                "/images/accommodations/complejo-casablanca/casablanca-02.jpg",
                "/images/accommodations/complejo-casablanca/casablanca-03.jpg",
                "/images/accommodations/complejo-casablanca/casablanca-04.jpg",
                "/images/accommodations/complejo-casablanca/casablanca-05.jpg",
                "/images/accommodations/complejo-casablanca/casablanca-06.jpg",
                "/images/accommodations/complejo-casablanca/casablanca-07.jpg",
                "/images/accommodations/complejo-casablanca/casablanca-08.jpg",
                "/images/accommodations/complejo-casablanca/casablanca-09.jpg",
                "/images/accommodations/complejo-casablanca/casablanca-10.jpg"
        );
        for (int i = 0; i < casablancaImgs.size(); i++) {
            casablanca.getImages().add(AccommodationImage.builder()
                    .url(casablancaImgs.get(i))
                    .isMain(i == 0)
                    .accommodation(casablanca)
                    .build());
        }
        accommodationRepository.save(casablanca);
        log.info("✔ Alojamiento 5 sincronizado: Complejo Turístico Casablanca");

        // ==========================================
        // Desactivar alojamientos legados que no pertenezcan al catálogo oficial
        // ==========================================
        java.util.Set<String> allowedNames = java.util.Set.of(
                "Cabañas El Aguaribay",
                "Cabañas Alpa Sierras",
                "Hostería Adrianita",
                "Hostería La Atalaya",
                "Complejo Turístico Casablanca"
        );
        accommodationRepository.findAll().forEach(acc -> {
            if (!allowedNames.contains(acc.getName()) && Boolean.TRUE.equals(acc.getIsActive())) {
                acc.setIsActive(false);
                accommodationRepository.save(acc);
                log.info("ℹ Alojamiento legado desactivado para el catálogo: {}", acc.getName());
            }
        });
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
                .estimatedDuration("7 a 8 horas ida y vuelta")
                .howToGet("A 3 km del centro por calle Sabattini hasta el Parador La Toma.")
                .requiresGuide(false)
                .admissionFee(BigDecimal.valueOf(15000))
                .latitude(-30.8490)
                .longitude(-64.4780)
                .status(AttractionStatus.OPEN)
                .statusNotice(null)
                .isFeatured(true)
                .images(new ArrayList<>())
                .build();
        List.of(
                "/images/attractions/uritorco/uritorco-01.jpg",
                "/images/attractions/uritorco/uritorco-02.jpg",
                "/images/attractions/uritorco/uritorco-03.jpg",
                "/images/attractions/uritorco/uritorco-04.jpg"
        ).forEach(url -> uritorco.getImages().add(AttractionImage.builder()
                .url(url)
                .publicId("uritorco_" + url.substring(url.lastIndexOf('/') + 1))
                .attraction(uritorco)
                .build()));
        list.add(uritorco);

        // 2. Dique Los Alazanes
        Attraction alazanes = Attraction.builder()
                .name("Dique Los Alazanes")
                .description("El embalse de mayor altura de Córdoba a 1.400 msnm entre quebradas vírgenes y arroyos de montaña.")
                .category(AttractionCategory.NATURE_TRAIL)
                .difficulty("MEDIA")
                .estimatedDuration("5 a 6 horas ida y vuelta")
                .howToGet("Acceso por senda peatonal serrana desde La Toma, bordeando el río Calabalumba.")
                .requiresGuide(false)
                .admissionFee(BigDecimal.ZERO)
                .latitude(-30.8480)
                .longitude(-64.4530)
                .status(AttractionStatus.OPEN)
                .statusNotice(null)
                .isFeatured(true)
                .images(new ArrayList<>())
                .build();
        List.of(
                "/images/attractions/dique-los-alazanes/dique-los-alazanes-01.jpg",
                "/images/attractions/dique-los-alazanes/dique-los-alazanes-02.jpg"
        ).forEach(url -> alazanes.getImages().add(AttractionImage.builder()
                .url(url)
                .publicId("alazanes_" + url.substring(url.lastIndexOf('/') + 1))
                .attraction(alazanes)
                .build()));
        list.add(alazanes);

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
                .latitude(-30.8655)
                .longitude(-64.5420)
                .status(AttractionStatus.OPEN)
                .statusNotice(null)
                .isFeatured(true)
                .images(new ArrayList<>())
                .build();
        zapato.getImages().add(AttractionImage.builder()
                .url("/images/attractions/el-zapato/el-zapato-01.jpg")
                .publicId("zapato_cover")
                .attraction(zapato)
                .build());
        list.add(zapato);

        // 4. Balneario Municipal Calabalumba
        Attraction balneario = Attraction.builder()
                .name("Balneario Municipal Calabalumba")
                .description("Piscina con agua de vertiente serrana, arboleda añosa y asadores junto al río Calabalumba.")
                .category(AttractionCategory.RIVER_BEACH)
                .difficulty("BAJA")
                .estimatedDuration("Paseo libre / Día completo")
                .howToGet("A 800 metros del centro cívico, sobre costanera del Río Calabalumba.")
                .requiresGuide(false)
                .admissionFee(BigDecimal.ZERO)
                .latitude(-30.8580)
                .longitude(-64.5320)
                .status(AttractionStatus.OPEN)
                .statusNotice(null)
                .isFeatured(true)
                .images(new ArrayList<>())
                .build();
        List.of(
                "/images/attractions/balneario-municipal/balneario-municipal-01.jpg",
                "/images/attractions/balneario-municipal/balneario-municipal-02.jpg",
                "/images/attractions/balneario-municipal/balneario-municipal-03.jpg"
        ).forEach(url -> balneario.getImages().add(AttractionImage.builder()
                .url(url)
                .publicId("balneario_" + url.substring(url.lastIndexOf('/') + 1))
                .attraction(balneario)
                .build()));
        list.add(balneario);

        // 5. Balneario El Paraíso
        Attraction paraiso = Attraction.builder()
                .name("Balneario El Paraíso")
                .description("Playitas de arena natural y ollas de agua transparente sobre el Río Dolores.")
                .category(AttractionCategory.RIVER_BEACH)
                .difficulty("BAJA")
                .estimatedDuration("Medio día")
                .howToGet("Siguiendo por la margen del Río Dolores al norte de Capilla del Monte.")
                .requiresGuide(false)
                .admissionFee(BigDecimal.ZERO)
                .latitude(-30.8490)
                .longitude(-64.5450)
                .status(AttractionStatus.OPEN)
                .statusNotice(null)
                .isFeatured(false)
                .images(new ArrayList<>())
                .build();
        paraiso.getImages().add(AttractionImage.builder()
                .url("/images/attractions/balneario-el-paraiso/balneario-el-paraiso-01.jpg")
                .publicId("paraiso_cover")
                .attraction(paraiso)
                .build());
        list.add(paraiso);

        // 6. Los Mogotes y Paso del Indio
        Attraction mogotes = Attraction.builder()
                .name("Los Mogotes y Paso del Indio")
                .description("Garganta natural de paredes rocosas de granito con senderos y zona de escalada deportiva.")
                .category(AttractionCategory.NATURE_TRAIL)
                .difficulty("MEDIA")
                .estimatedDuration("2 a 3 horas")
                .howToGet("A 4 km al noroeste del centro sobre el Río Dolores.")
                .requiresGuide(false)
                .admissionFee(BigDecimal.valueOf(3000))
                .latitude(-30.8350)
                .longitude(-64.5380)
                .status(AttractionStatus.OPEN)
                .statusNotice(null)
                .isFeatured(false)
                .images(new ArrayList<>())
                .build();
        mogotes.getImages().add(AttractionImage.builder()
                .url("/images/attractions/los-mogotes/los-mogotes-01.jpg")
                .publicId("mogotes_cover")
                .attraction(mogotes)
                .build());
        list.add(mogotes);

        // 7. Senderos del Valle y Huertas Malas
        Attraction senderosValle = Attraction.builder()
                .name("Senderos del Valle y Huertas Malas")
                .description("Quebradas agrestes, bosques de tabaquillo, arroyos de deshielo y cascadas secretas.")
                .category(AttractionCategory.NATURE_TRAIL)
                .difficulty("MEDIA")
                .estimatedDuration("4 horas")
                .howToGet("Camino a Huertas Malas, faldeo este.")
                .requiresGuide(true)
                .admissionFee(BigDecimal.valueOf(5000))
                .latitude(-30.8520)
                .longitude(-64.4950)
                .status(AttractionStatus.OPEN)
                .statusNotice(null)
                .isFeatured(false)
                .images(new ArrayList<>())
                .build();
        senderosValle.getImages().add(AttractionImage.builder()
                .url("/images/attractions/senderos-del-valle/senderos-del-valle-01.jpg")
                .publicId("senderos_valle_cover")
                .attraction(senderosValle)
                .build());
        list.add(senderosValle);

        for (Attraction a : list) {
            java.util.Optional<Attraction> existingOpt = attractionRepository.findByName(a.getName());
            if (existingOpt.isPresent()) {
                Attraction existing = existingOpt.get();
                boolean hasUnsplash = existing.getImages() == null || existing.getImages().isEmpty() ||
                        existing.getImages().stream().anyMatch(img -> img.getUrl() != null && img.getUrl().contains("unsplash.com"));
                if (hasUnsplash) {
                    existing.getImages().clear();
                    for (AttractionImage img : a.getImages()) {
                        existing.getImages().add(AttractionImage.builder()
                                .url(img.getUrl())
                                .publicId(img.getPublicId())
                                .attraction(existing)
                                .build());
                    }
                    attractionRepository.save(existing);
                    log.info("✔ Atractivo existente actualizado con imágenes reales locales: {}", existing.getName());
                }
            } else {
                attractionRepository.save(a);
                log.info("✔ Atractivo real registrado en base de datos: {}", a.getName());
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

    private void initDemoBookingsAndReviews() {
        List<Accommodation> allAccommodations = accommodationRepository.findAll();
        if (allAccommodations.isEmpty()) {
            return;
        }

        Accommodation cabin = allAccommodations.stream()
                .filter(a -> "Cabañas El Aguaribay".equals(a.getName()) || "Cabañas Pircas del Uritorco".equals(a.getName()))
                .findFirst()
                .orElse(allAccommodations.get(0));

        Accommodation hotel = allAccommodations.stream()
                .filter(a -> "Hostería La Atalaya".equals(a.getName()) || "Hostería Boutique Los Nogales".equals(a.getName()))
                .findFirst()
                .orElse(cabin);

        seedCompletedBookingWithReview(
                cabin,
                "CAP-2026-4812",
                "Mariana Gómez",
                "mariana.gomez@example.com",
                "+54 9 341 5123456",
                "Rosario, Santa Fe",
                LocalDate.now().minusDays(45),
                LocalDate.now().minusDays(42),
                "Septiembre 2026",
                5, 5, 5, 5,
                "Una experiencia hermosa al pie del Uritorco. La cabaña impecable, con todo lo necesario para cocinar y descansar. La pileta climatizada fue un diferencial enorme para disfrutar a la vuelta de las caminatas."
        );

        seedCompletedBookingWithReview(
                cabin,
                "CAP-2026-3904",
                "Federico Luján",
                "federico.lujan@example.com",
                "+54 9 11 49876543",
                "CABA, Buenos Aires",
                LocalDate.now().minusDays(70),
                LocalDate.now().minusDays(66),
                "Agosto 2026",
                5, 5, 5, 4,
                "Excelente atención de los dueños desde que llegamos. El predio es súper silencioso de noche, ideal para desconectar del ruido de la ciudad. El WiFi funcionó perfecto incluso para trabajar."
        );

        seedCompletedBookingWithReview(
                hotel,
                "CAP-2026-5102",
                "Roberto Echeverría",
                "roberto.echeverria@example.com",
                "+54 9 261 6123987",
                "Mendoza",
                LocalDate.now().minusDays(35),
                LocalDate.now().minusDays(32),
                "Septiembre 2026",
                5, 5, 5, 5,
                "La casona tiene un encanto histórico único y está conservada con muchísimo cariño. El desayuno serrano con pan casero y dulces regionales es espectacular."
        );

        // Reserva completada sin reseña previa para pruebas de envío real desde el portal
        if (bookingRepository.findByBookingCode("CAP-2026-9901").isEmpty()) {
            BigDecimal nightPrice = cabin.getPricePerNight() != null ? cabin.getPricePerNight() : BigDecimal.valueOf(85000.00);
            BigDecimal total = nightPrice.multiply(BigDecimal.valueOf(3));
            Booking pendingReviewBooking = Booking.builder()
                    .bookingCode("CAP-2026-9901")
                    .checkIn(LocalDate.now().minusDays(15))
                    .checkOut(LocalDate.now().minusDays(12))
                    .totalNights(3)
                    .guestCount(2)
                    .pricePerNight(nightPrice)
                    .totalAmount(total)
                    .downPaymentPercentage(50)
                    .downPaymentAmount(total.multiply(BigDecimal.valueOf(0.5)))
                    .remainingBalance(total.multiply(BigDecimal.valueOf(0.5)))
                    .status(BookingStatus.COMPLETED)
                    .guestName("Lucía Fernández")
                    .guestEmail("lucia.fernandez@example.com")
                    .guestPhone("+54 9 351 4567890")
                    .guestOrigin("Córdoba Capital")
                    .accommodation(cabin)
                    .build();
            bookingRepository.save(pendingReviewBooking);
            log.info("✔ Reserva completada de prueba lista para reseñar: CAP-2026-9901");
        }
    }

    private void seedCompletedBookingWithReview(
            Accommodation accommodation,
            String bookingCode,
            String guestName,
            String guestEmail,
            String guestPhone,
            String guestOrigin,
            LocalDate checkIn,
            LocalDate checkOut,
            String stayDate,
            int rating,
            int cleanliness,
            int hostRating,
            int locationRating,
            String comment
    ) {
        Booking booking = bookingRepository.findByBookingCode(bookingCode).orElse(null);
        if (booking == null) {
            long nights = Math.max(1, java.time.temporal.ChronoUnit.DAYS.between(checkIn, checkOut));
            BigDecimal nightPrice = accommodation.getPricePerNight() != null
                    ? accommodation.getPricePerNight()
                    : BigDecimal.valueOf(85000.00);
            BigDecimal total = nightPrice.multiply(BigDecimal.valueOf(nights));

            booking = Booking.builder()
                    .bookingCode(bookingCode)
                    .checkIn(checkIn)
                    .checkOut(checkOut)
                    .totalNights((int) nights)
                    .guestCount(2)
                    .pricePerNight(nightPrice)
                    .totalAmount(total)
                    .downPaymentPercentage(50)
                    .downPaymentAmount(total.multiply(BigDecimal.valueOf(0.5)))
                    .remainingBalance(total.multiply(BigDecimal.valueOf(0.5)))
                    .status(BookingStatus.COMPLETED)
                    .guestName(guestName)
                    .guestEmail(guestEmail)
                    .guestPhone(guestPhone)
                    .guestOrigin(guestOrigin)
                    .accommodation(accommodation)
                    .build();
            booking = bookingRepository.save(booking);
        }

        if (!reviewRepository.existsByBookingCodeIgnoreCase(bookingCode)) {
            Review review = Review.builder()
                    .booking(booking)
                    .accommodation(accommodation)
                    .bookingCode(bookingCode)
                    .guestName(guestName)
                    .guestOrigin(guestOrigin)
                    .stayDate(stayDate)
                    .rating(rating)
                    .cleanlinessRating(cleanliness)
                    .hostRating(hostRating)
                    .locationRating(locationRating)
                    .comment(comment)
                    .status(ReviewStatus.APPROVED)
                    .moderatedAt(LocalDateTime.now())
                    .build();
            reviewRepository.save(review);
        }
    }
}
