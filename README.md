# Turismo Capilla del Monte - Backend API

<p align="center">
  <img src="https://img.shields.io/badge/Java-17-orange?style=for-the-badge&logo=openjdk" alt="Java 17">
  <img src="https://img.shields.io/badge/Spring%20Boot-3-brightgreen?style=for-the-badge&logo=springboot" alt="Spring Boot 3">
  <img src="https://img.shields.io/badge/PostgreSQL-18-blue?style=for-the-badge&logo=postgresql" alt="PostgreSQL 18">
  <img src="https://img.shields.io/badge/Spring%20Security-JWT-black?style=for-the-badge&logo=jsonwebtokens" alt="JWT Security">
  <img src="https://img.shields.io/badge/Cloudinary-CDN%20Storage-3448C5?style=for-the-badge&logo=cloudinary" alt="Cloudinary">
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
* **Cabañero / Prestador (`HOST`):** Se registra exclusivamente a través de un token de invitación validado; administra sus establecimientos (alta, edición, baja, comodidades, fotos a Cloudinary), configura inventario multi-unidad (habitaciones y planes tarifarios) y gestiona el ciclo de vida de las reservas recibidas (confirmar, cancelar, completar).
* **Comisión de Turismo (`ADMIN`):** Administra el portal, modera prestadores con facultad de suspensión preventiva, gestiona atractivos y paseos serranos, emite alertas meteorológicas de Defensa Civil, coordina la agenda cultural y emite tokens de invitación seguros con validez de 7 días.

---

## Modelo de Dominio y Entidades (JPA)

El modelo de datos implementa identificación descentralizada mediante **`UUID`** nativo de PostgreSQL, integridad referencial y transacciones ACID:

1. **`User` (`Usuario`):** Actores del sistema (`ADMIN`, `HOST`, `TOURIST`) con autenticación mediante contraseñas cifradas en BCrypt, control de acceso RBAC y flag `isActive` para suspensión preventiva.
2. **`Accommodation` (`Alojamiento / Hospedaje`):** Complejos y cabañas con geolocalización (`latitud`, `longitud`), tipo (`CABIN`, `HOTEL`, `APARTMENT`, `HOSTEL`, `CAMPING`), capacidad de huéspedes, precio base, porcentaje de seña y datos bancarios (CBU / Alias). Relación **`N:1`** con `User` (`host`).
3. **`RoomType` & `RatePlan` (`Inventario Multi-unidad`):** Habitaciones específicas (Standard, Superior, Suite) con cupo de unidades y planes de tarifa (flexible o desayuno incluido) para hoteles y hosterías.
4. **`AccommodationImage` (`Foto de Alojamiento`):** Galería de imágenes alojadas en Cloudinary CDN. Relación **`N:1`** con `Accommodation`.
5. **`Attraction` (`Atractivo Turístico / Paseo`):** Circuitos y paseos clasificados por categorías serranas (`HILL`, `RIVER_BEACH`, `CULTURAL`, `NIGHT`, `NATURE_TRAIL`), nivel de dificultad, coordenadas, si requiere guía y estado operativo (`OPEN`, `WEATHER_RESTRICTED`, `CLOSED`).
6. **`AttractionImage` (`Foto de Atractivo`):** Galería de fotos vinculadas al atractivo en Cloudinary. Relación **`N:1`** con `Attraction`.
7. **`Booking` (`Reserva`):** Motor de reservas transaccional con prevención anti-overbooking (`checkIn < existingCheckOut && checkOut > existingCheckIn`), cálculo automático de noches y monto total, código único (`CAP-YYYY-XXXX`) y máquina de estados (`PENDING`, `CONFIRMED`, `CANCELLED`, `COMPLETED`).
8. **`Alert` (`Alerta de Seguridad`):** Avisos de Defensa Civil / Bomberos con niveles de severidad (`INFO`, `WARNING`, `DANGER`) y fecha de caducidad.
9. **`Event` (`Evento Cultural`):** Agenda de festividades (Festival del Alienígena, Astroturismo, Ferias) con fechas, horarios, ubicación física y precio.
10. **`InvitationToken` (`Token de Invitación`):** Mecanismo de onboarding seguro para prestadores con caducidad a los 7 días y consumo atómico en un único uso.

---

## Tecnologías Utilizadas

* **Lenguaje:** Java 17 (LTS - Oracle OpenJDK 17.0.2).
* **Framework:** Spring Boot 3 (Starter Web, Starter Data JPA, Validation, DevTools).
* **Seguridad:** Spring Security 6 + JJWT (JSON Web Token) Stateless.
* **Persistencia & ORM:** Hibernate ORM / Spring Data JPA.
* **Base de Datos:** PostgreSQL 18 (`org.postgresql:postgresql`).
* **Almacenamiento Multimedia:** Cloudinary Java SDK (auto-compresión WebP, CDN global).
* **Documentación Interactiva:** Springdoc OpenAPI / Swagger UI 2.8.5.
* **Productividad:** Project Lombok.
* **Gestor de Construcción:** Apache Maven (`mvnw`).

---

## Estructura del Proyecto

```text
src/main/java/com/upc/demo/
├── config/             # Seguridad Spring Security, JWT Filter, OpenAPI, CORS y StorageConfig
│   ├── exception/      # GlobalExceptionHandler y excepciones semánticas (BadRequest, Conflict, Forbidden)
│   └── DataInitializer.java # Seeding automático de datos de prueba
├── controlador/        # Controladores REST (/api/v1/*)
│   ├── AccommodationController.java
│   ├── AlertController.java
│   ├── AttractionController.java
│   ├── AuthController.java
│   ├── BookingController.java
│   ├── EventController.java
│   ├── InvitationsController.java
│   ├── MediaController.java
│   └── UsersController.java
├── dto/                # Data Transfer Objects agrupados por módulo y validados con Jakarta
├── entidad/            # Entidades JPA del dominio y tipos enumerados
│   ├── enums/          # Role, AccommodationType, AttractionCategory, BookingStatus, AlertSeverity
│   ├── Accommodation.java
│   ├── AccommodationImage.java
│   ├── Alert.java
│   ├── Attraction.java
│   ├── Booking.java
│   ├── Event.java
│   ├── InvitationToken.java
│   ├── RatePlan.java
│   ├── RoomType.java
│   └── User.java
├── repositorio/        # Interfaces Spring Data JPA con consultas JPQL especializadas
└── servicio/           # Servicios transaccionales y lógica de negocio
    ├── storage/        # Adaptador StorageService (Cloudinary / Local)
    ├── AccommodationService.java
    ├── AlertService.java
    ├── AttractionService.java
    ├── AuthService.java
    ├── BookingService.java
    ├── EventService.java
    ├── InvitationService.java
    └── UserService.java
```

---

## Puesta en Marcha Local

### 1. Variables de Entorno (`.env`)
Crear un archivo `.env` en la raíz de este repositorio:

```ini
DB_URL=jdbc:postgresql://localhost:5432/portal_turismo
DB_USERNAME=postgres
DB_PASSWORD=root

# Seguridad JWT (Obligatorio - mínimo 32 caracteres / 256 bits)
JWT_SECRET=tu_clave_secreta_jwt_de_al_menos_32_caracteres_o_hash_256_bits

# Almacenamiento Cloudinary Activo
STORAGE_PROVIDER=cloudinary
CLOUDINARY_URL=cloudinary://476644815221196:x099anE0a2euhAxBy5mxP8yF3Sc@wgmfpnwz
CLOUDINARY_CLOUD_NAME=wgmfpnwz
CLOUDINARY_API_KEY=476644815221196
CLOUDINARY_API_SECRET=x099anE0a2euhAxBy5mxP8yF3Sc
```

### 2. Ejecutar la Aplicación
```powershell
# Opción recomendada (carga automáticamente el .env local)
.\dev.ps1

# O mediante Maven Wrapper directo:
.\mvnw.cmd spring-boot:run
```

* **Swagger UI interactivo:**  
  👉 [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
* **Especificación OpenAPI en JSON:**  
  👉 [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

---

## Credenciales de Acceso Oficial y Seeding (`DataInitializer`)

Al iniciar la aplicación, `DataInitializer` verifica y puebla automáticamente en PostgreSQL las cuentas de prueba:

| Rol | Correo Electrónico | Contraseña | Perfil Inicial | Propósito / Permisos |
| :--- | :--- | :--- | :--- | :--- |
| **`ADMIN`** | `admin@capilladelmonte.gov.ar` | `AdminCapilla2026!` | Comisión Oficial Turismo Capilla | Acceso total: prestadores, suspensión, tokens, agenda cultural, alertas y temporadas. |
| **`HOST` (Cabaña)** | `host@capilladelmonte.gov.ar` | `HostCapilla2026!` | Roberto Gómez (*Cabañas Pircas del Uritorco*) | Cabaña individual al pie del cerro ($85.000/noche, seña 50%). |
| **`HOST` (Hostería)** | `marcela.rossi@capillaturismo.com.ar` | `HostCapilla2026!` | Marcela Rossi (*Hostería Los Nogales*) | Inventario multi-unidad (habitaciones Standard, Superior y Suite, seña 30%). |
| **`HOST` (Refugio)** | `carlos.benitez@capillaturismo.com.ar` | `HostCapilla2026!` | Carlos Benítez (*Refugio Los Alazanes*) | Monoambiente de montaña con deck privado ($65.000/noche, seña 40%). |

> [!TIP]
> Para el catálogo completo de cuentas, paseos, alertas, eventos y datos bancarios, consultá el archivo centralizado [`docs/test-credentials.md`](../Turismo-Capilla/docs/test-credentials.md). Para la guía interactiva, revisá el [`Manual de Usuario Oficial`](../Turismo-Capilla/docs/user-manual.md).

---

## 📚 Documentación y Recursos

- **Manual de Usuario Oficial:** [docs/user-manual.md](../Turismo-Capilla/docs/user-manual.md)
- **Credenciales y Datos de Prueba:** [docs/test-credentials.md](../Turismo-Capilla/docs/test-credentials.md)
- **Referencia Oficial de Endpoints:** [docs/api-reference.md](../Turismo-Capilla/docs/api-reference.md)
- **Google Drive:** Almacena la documentación general del proyecto.  
  [Acceder a la carpeta del proyecto en Google Drive](https://drive.google.com/drive/u/1/folders/1KQLWydgsWH7hCD0RqfqIrFO5AzJRqB5E)

---

## Equipo

* **Tiago Nicolitsis**
* **Martino Costigliolo**
* **Juan Larcher**
