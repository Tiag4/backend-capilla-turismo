# Turismo Capilla del Monte - Backend API

<p align="center">
  <img src="https://img.shields.io/badge/Java-17-orange?style=for-the-badge&logo=openjdk" alt="Java 17">
  <img src="https://img.shields.io/badge/Spring%20Boot-3-brightgreen?style=for-the-badge&logo=springboot" alt="Spring Boot 3">
  <img src="https://img.shields.io/badge/PostgreSQL-18-blue?style=for-the-badge&logo=postgresql" alt="PostgreSQL 18">
  <img src="https://img.shields.io/badge/Spring%20Security-JWT-black?style=for-the-badge&logo=jsonwebtokens" alt="JWT Security">
  <img src="https://img.shields.io/badge/OpenAPI-Swagger%20UI-darkgreen?style=for-the-badge&logo=swagger" alt="Swagger UI">
  <img src="https://img.shields.io/badge/Maven-Build-red?style=for-the-badge&logo=apachemaven" alt="Maven">
</p>

<p align="center">
  <strong>Capa de persistencia, servicios y API REST para la Comisión de Turismo de Capilla del Monte.</strong><br>
  Plataforma para centralizar hospedajes, prestadores, paseos locales y reservas directas con prevención estricta de overbooking.
</p>

---

## Contexto Académico

Proyecto desarrollado en el marco de la **Tecnicatura Superior en Desarrollo de Software** de la **Universidad Provincial de Córdoba (Sede Regional Capilla del Monte)** para la cátedra de **Programación III** (articulado con Práctica Profesionalizante II e Ingeniería de Software).

---

## Sobre el Proyecto

Este repositorio contiene la arquitectura backend, lógica de negocio y base de datos del **Portal de Turismo de Capilla del Monte**.

El sistema nuclea a los prestadores formalmente adheridos y conecta a turistas con hospedajes (cabañas, hoteles, departamentos, campings), paseos y excursiones turísticas, facilitando la visualización geográfica, filtrado en catálogo y gestión transaccional de reservas con emisión de código único auditado (`CAP-YYYY-XXXX`).

### Roles del Sistema

* **Turista (`TOURIST`):** Explora hospedajes y atractivos turísticos en catálogo, filtra por disponibilidad de fechas (`checkIn`/`checkOut`), precio, capacidad y comodidades; realiza reservas directas y consulta el estado de su estadía de forma pública (código y correo) o desde su panel autenticado.
* **Cabañero / Prestador (`HOST`):** Se registra exclusivamente a través de un token de invitación validado; administra sus establecimientos (alta, edición, baja, comodidades, fotos) y gestiona el ciclo de vida de las reservas recibidas (confirmar, cancelar, completar).
* **Comisión de Turismo (`ADMIN`):** Administra el portal, modera y gestiona atractivos turísticos, supervisa usuarios y emite tokens de invitación seguros con validez de 7 días para habilitar a prestadores adheridos.

---

## Modelo de Dominio y Entidades (JPA)

El modelo de datos implementa identificación descentralizada mediante **`UUID`** nativo de PostgreSQL, integridad referencial y las cardinalidades requeridas:

1. **`User` (`Usuario`):** Actores del sistema (`ADMIN`, `HOST`, `TOURIST`) con autenticación mediante contraseñas cifradas en BCrypt y control de acceso RBAC.
2. **`Accommodation` (`Alojamiento / Hospedaje`):** Complejos y cabañas con geolocalización (`latitud`, `longitud`), tipo de alojamiento (`CABIN`, `HOTEL`, `APARTMENT`, `HOSTEL`, `CAMPING`), capacidad de huéspedes, horarios de check-in/check-out y precio por noche. Relación **`N:1`** con `User` (`host`).
3. **`AccommodationImage` (`Foto de Alojamiento`):** Galería de imágenes asociadas a cada establecimiento. Relación **`N:1`** con `Accommodation`.
4. **`Attraction` (`Atractivo Turístico / Paseo`):** Circuitos, senderos y excursiones locales clasificados por categorías serranas (`HILL`, `RIVER_BEACH`, `CULTURAL`, `NIGHT`, `NATURE_TRAIL`), nivel de dificultad, coordenadas y si requiere guía habilitado.
5. **`AttractionImage` (`Foto de Atractivo`):** Galería de fotos vinculadas al atractivo. Relación **`N:1`** con `Attraction`.
6. **`Booking` (`Reserva`):** Motor de reservas transaccional con prevención anti-overbooking (`checkIn < existingCheckOut && checkOut > existingCheckIn`), cálculo automático de noches y monto total, código único (`CAP-YYYY-XXXX`) y máquina de estados (`PENDING`, `CONFIRMED`, `CANCELLED`, `COMPLETED`). Relación **`N:1`** con `Accommodation` y **`N:1`** opcional con `User` (`tourist`).
7. **`InvitationToken` (`Token de Invitación`):** Mecanismo de onboarding seguro para prestadores con caducidad a los 7 días y consumo atómico en un único uso. Relación **`N:1`** con `User` (`createdBy`).

---

## Tecnologías Utilizadas

* **Lenguaje:** Java 17 (LTS - Oracle OpenJDK 17.0.2).
* **Framework:** Spring Boot 3 (Starter Web, Starter Data JPA, Validation, DevTools).
* **Seguridad:** Spring Security 6 + JJWT (JSON Web Token) Stateless.
* **Persistencia & ORM:** Hibernate ORM / Spring Data JPA.
* **Base de Datos:** PostgreSQL 18 (`org.postgresql:postgresql`).
* **Documentación Interactiva:** Springdoc OpenAPI / Swagger UI 2.8.5.
* **Productividad:** Project Lombok.
* **Gestor de Construcción:** Apache Maven (`mvnw`).
* **Modelado & Herramientas:** Draw.io (UML), DBeaver / pgAdmin.

---

## Estructura del Proyecto

```text
src/main/java/com/upc/demo/
├── config/             # Seguridad Spring Security, JWT Filter, OpenAPI, CORS y Logger
│   ├── exception/      # GlobalExceptionHandler y excepciones de negocio semánticas
├── controlador/        # Controladores REST (/api/v1/*)
│   ├── AccommodationController.java
│   ├── AttractionController.java
│   ├── AuthController.java
│   ├── BookingController.java
│   ├── InvitationsController.java
│   └── UsersController.java
├── dto/                # Data Transfer Objects agrupados por módulo y validados con Jakarta
│   ├── accommodation/
│   ├── attraction/
│   ├── auth/
│   ├── booking/
│   ├── common/
│   ├── invitation/
│   └── user/
├── entidad/            # Entidades JPA del dominio y tipos enumerados
│   ├── enums/          # Role, AccommodationType, AttractionCategory, BookingStatus
│   ├── Accommodation.java
│   ├── AccommodationImage.java
│   ├── Attraction.java
│   ├── AttractionImage.java
│   ├── Booking.java
│   ├── InvitationToken.java
│   └── User.java
├── repositorio/        # Interfaces Spring Data JPA con consultas JPQL especializadas
│   ├── AccommodationImageRepository.java
│   ├── AccommodationRepository.java
│   ├── AttractionImageRepository.java
│   ├── AttractionRepository.java
│   ├── BookingRepository.java
│   ├── InvitationTokenRepository.java
│   └── UserRepository.java
└── servicio/           # Servicios transaccionales y lógica de negocio
    ├── AccommodationService.java
    ├── AttractionService.java
    ├── AuthService.java
    ├── BookingService.java
    ├── InvitationService.java
    └── UserService.java
```

---

## Documentación de la API (Swagger UI)

Al levantar el servidor backend (`./mvnw spring-boot:run`), los enlaces interactivos se imprimen automáticamente en la terminal:

* **Swagger UI interactivo:**  
  👉 [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) *(o `http://localhost:8080/swagger-ui/index.html`)*
* **Especificación OpenAPI en JSON:**  
  👉 [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

> **Nota:** La interfaz cuenta con botón `Authorize` para ingresar tokens JWT (`Bearer <token>`) y probar endpoints protegidos de anfitriones y administradores.

---

## 📚 Documentación y Recursos

- **Wiki del repositorio:** Detalla los aspectos del marco PMI, arquitectura de backend, actas y planificación técnica.
- **Google Drive:** Almacena la documentación general del proyecto.  
  [Acceder a la carpeta del proyecto en Google Drive](https://drive.google.com/drive/u/1/folders/1KQLWydgsWH7hCD0RqfqIrFO5AzJRqB5E)

---

## Equipo

Proyecto desarrollado por:

| Integrante              |
| ----------------------- |
| **Tiago Nicolitsis**    |
| **Martino Costigliolo** |
| **Juan Larcher**        |
