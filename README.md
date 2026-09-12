# Turismo Capilla del Monte - Backend API

<p align="center">
  <img src="https://img.shields.io/badge/Java-17-orange?style=for-the-badge&logo=openjdk" alt="Java 17">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.5.14-brightgreen?style=for-the-badge&logo=springboot" alt="Spring Boot 3.5.14">
  <img src="https://img.shields.io/badge/MySQL-8.0+-blue?style=for-the-badge&logo=mysql" alt="MySQL">
  <img src="https://img.shields.io/badge/Maven-Build-red?style=for-the-badge&logo=apachemaven" alt="Maven">
</p>

<p align="center">
  <strong>Capa de persistencia, servicios y API REST para la Cámara de Turismo de Capilla del Monte.</strong><br>
  Plataforma para centralizar hospedajes, prestadores, paseos locales y reservas directas.
</p>

---

## Contexto Académico

Proyecto desarrollado en el marco de la **Tecnicatura Superior en Desarrollo de Software** de la **Universidad Provincial de Córdoba (Sede Regional Capilla del Monte)** para la cátedra de **Programación III** (articulado con Práctica Profesionalizante II e Ingeniería de Software).

---

## Sobre el Proyecto

Este repositorio contiene la arquitectura backend y la base de datos del **Portal de Turismo de Capilla del Monte**.

El sistema nuclea a los prestadores y socios de la cámara de turismo local (~40 establecimientos iniciales) y conecta a turistas con hospedajes (cabañas, complejos), excursiones y paseos turísticos, facilitando la comunicación directa vía WhatsApp, visualización geográfica en mapas y gestión de reservas con control de check-in.

### Roles del Sistema

* **Turista:** Explora hospedajes y paseos en el mapa interactivo, filtra comodidades, consulta vía WhatsApp y solicita reservas con cálculo automático de estadía.
* **Cabañero / Prestador (Dueño):** Administra su complejo o cabaña, carga hasta 10 fotos, define su ficha técnica (capacidad, horarios de check-in/out) y gestiona los servicios ofrecidos. Toda edición queda sujeta a revisión.
* **Comisión de la Cámara (Admin):** Dashboard de moderación para habilitar socios, aprobar, rechazar o mantener en revisión publicaciones de cabañas y paseos antes de su exposición en el catálogo público.

---

## Modelo de Dominio y Entidades (JPA)

El modelo de datos cumple con los tres tipos de cardinalidades requeridas (`1:1`, `1:N`, `N:M`):

1. **`Usuario`:** Actores del sistema con control de acceso (`TURISTA`, `DUENO`, `ADMIN_COMISION`).
2. **`Hospedaje`:** Cabañas y complejos turísticos con geolocalización (`latitud`, `longitud`), precio por noche y estado de publicación (`EN_REVISION`, `APROBADO`, `RECHAZADO`).
3. **`FichaTecnica`:** Especificaciones operativas de la cabaña (capacidad, habitaciones, baños, horarios de check-in/out, mascotas). Relación **`1:1`** con `Hospedaje`.
4. **`Foto`:** Galería de imágenes (hasta 10 fotos por hospedaje con designación de portada). Relación **`N:1`** con `Hospedaje`.
5. **`Servicio`:** Catálogo de amenidades para filtrado (pileta, WiFi, asador, cochera, vista al cerro). Relación **`N:M`** con `Hospedaje` mediante la tabla intermedia `hospedaje_servicio`.
6. **`Paseo`:** Atractivos, circuitos y excursiones locales (dificultad, duración, ubicación, contacto y moderación por comisión). Relación **`N:1`** con `Usuario`.
7. **`Reserva`:** Gestión de solicitudes de estadía, cálculo de montos y seguimiento del check-in (`checkInRealizado`). Relación **`N:1`** con `Usuario` y `Hospedaje`.

---

## Tecnologías Utilizadas

* **Lenguaje:** Java 17 (LTS).
* **Framework:** Spring Boot 3.5.14 (Starter Web, Starter Data JPA, Validation, DevTools).
* **Persistencia & ORM:** Hibernate / JPA.
* **Base de Datos:** MySQL 8.0+ (`mysql-connector-j`).
* **Documentación de API:** Springdoc OpenAPI / Swagger UI 2.8.9.
* **Productividad:** Project Lombok[cite: 1].
* **Gestor de Construcción:** Apache Maven[cite: 1].
* **Diseño y Modelado:** Draw.io (UML)[cite: 1] y MySQL Workbench (Diagramas EER mediante Reverse Engineer)[cite: 1].

## 📚 Documentación y Recursos

- **Wiki del repositorio:** Detalla los aspectos del marco PMI, arquitectura de backend, actas y planificación técnica.
- **Google Drive:** Almacena la documentación general del proyecto.  
  [Acceder a la carpeta del proyecto en Google Drive](https://drive.google.com/drive/u/1/folders/1KQLWydgsWH7hCD0RqfqIrFO5AzJRqB5E)
---

## Estructura del Proyecto

```text
src/main/java/com/upc/demo/
├── config/             # Configuraciones globales (CORS, OpenAPI, seguridad)
├── controlador/        # Endpoints REST (Controladores HTTP - Fase TP 2)
├── dto/                # Objetos de transferencia de datos con validaciones
├── entidad/            # Clases del modelo con anotaciones JPA (@Entity)
│   ├── FichaTecnica.java
│   ├── Foto.java
│   ├── Hospedaje.java
│   ├── Paseo.java
│   ├── Reserva.java
│   ├── Servicio.java
│   └── Usuario.java
├── repositorio/        # Interfaces Spring Data JPA (*Repositorio)
│   ├── FichaTecnicaRepositorio.java
│   ├── FotoRepositorio.java
│   ├── HospedajeRepositorio.java
│   ├── PaseoRepositorio.java
│   ├── ReservaRepositorio.java
│   ├── ServicioRepositorio.java
│   └── UsuarioRepositorio.java
└── servicio/           # Lógica de negocio y transacciones


