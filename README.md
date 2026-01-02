# 🛒 Dawazon

<div align="center">

![Java](https://img.shields.io/badge/Java-25-orange?style=for-the-badge&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.8-brightgreen?style=for-the-badge&logo=spring)
![Gradle](https://img.shields.io/badge/Gradle-9.1.0-02303A?style=for-the-badge&logo=gradle)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-12-316192?style=for-the-badge&logo=postgresql)
![MongoDB](https://img.shields.io/badge/MongoDB-5.0-47A248?style=for-the-badge&logo=mongodb)
![Redis](https://img.shields.io/badge/Redis-7-DC382D?style=for-the-badge&logo=redis)

**Tienda basada en Amazon bien bacana - Plataforma de e-commerce con arquitectura híbrida**

[🚀 Características](#-características) • [📖 Instalación](#-instalación-y-configuración) • [🐳 Docker](#-despliegue-con-docker) • [🔌 API](#-endpoints-de-la-aplicación)

</div>

---

## 📋 Tabla de Contenidos

- [Características](#-características)
- [Stack Tecnológico](#️-stack-tecnológico)
- [Requisitos Previos](#-requisitos-previos)
- [Instalación y Configuración](#-instalación-y-configuración)
- [Despliegue con Docker](#-despliegue-con-docker)
- [Arquitectura](#️-arquitectura)
- [Roles y Funcionalidades](#-roles-y-funcionalidades)
- [Endpoints de la Aplicación](#-endpoints-de-la-aplicación)
- [Estructura del Proyecto](#-estructura-del-proyecto)

---

## ✨ Características

### 🛍️ Gestión de Usuarios
- ✅ Autenticación con Spring Security
- ✅ Registro y login seguro
- ✅ Gestión de perfiles
- ✅ Sistema multi-rol (USER, MANAGER, ADMIN)
- ✅ Cambio de contraseña
- ✅ Subida de avatar

### 📦 Gestión de Productos
- ✅ Catálogo completo con MongoDB
- ✅ Imágenes multimedia
- ✅ Búsqueda y filtros
- ✅ Filtros por categoría
- ✅ Sistema de comentarios

### 🛒 Gestión de Carrito y Ventas
- ✅ Carrito de compras
- ✅ Historial de ventas
- ✅ Panel de administración de ventas
- ✅ Cancelación de pedidos
- ✅ Edición de líneas de venta (ADMIN)
- ✅ Cálculo de ganancias

### 🔧 Características Técnicas
- ✅ Caché con Redis
- ✅ Docker Compose multi-servicio
- ✅ Testing con Testcontainers
- ✅ Cobertura de código con JaCoCo
- ✅ Documentación con Dokka
- ✅ Plantillas con Pebble
- ✅ Validación de datos

---

## 🛠️ Stack Tecnológico

```
┌─────────────────────────────────────────────────────────────┐
│                                                             │
│  ☕ Java 25      🌱 Spring Boot 3.5.8    🔧 Gradle 9.1.0  │
│                                                             │
│  🐘 PostgreSQL   🍃 MongoDB   🔴 Redis   🐳 Docker        │
│                                                             │
│  🍂 Pebble       📊 JaCoCo    📖 Dokka   🔒 JWT           │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### Tecnologías Principales

| Categoría | Tecnologías |
|-----------|-------------|
| **Backend** | Java 25, Spring Boot 3.5.8, Spring Security, Spring Data JPA |
| **Frontend** | Pebble 3.2.2, HTML5, CSS3, JavaScript |
| **Bases de Datos** | PostgreSQL 12 (Usuarios, Carritos), MongoDB 5.0 (Productos), Redis 7 (Caché) |
| **Autenticación** | JWT 4.4.0, Spring Security 6 |
| **API** | REST con Spring MVC |
| **Pagos** | Stripe Java SDK 24.0.0 |
| **Build & Deploy** | Gradle 9.1.0, Docker, Docker Compose |
| **Testing** | JUnit 5, Testcontainers, Mockito |
| **Documentación** | Dokka 2.1.0, JaCoCo |
| **Email** | Spring Mail |

---

## 📋 Requisitos Previos

Antes de comenzar, asegúrate de tener instalado:

- ☕ **Java JDK 25** ([Descargar](https://www.oracle.com/java/technologies/downloads/))
- 🔧 **Gradle 9.1.0** (o usa el wrapper `./gradlew`)
- 🐳 **Docker** (>= 20.x) y **Docker Compose** (>= 2.x)
- 🐘 **Git**

### 🔍 Verificar instalación

```bash
java --version    # Debe mostrar Java 25
gradle --version  # Debe mostrar Gradle 9.1.0
docker --version  # Verificar Docker
docker-compose --version  # Verificar Docker Compose
```

---

## 🚀 Instalación y Configuración

### 1️⃣ Clonar el repositorio

```bash
git clone https://github.com/Aragorn7372/dawazon.git
cd dawazon
```

### 2️⃣ Configurar Variables de Entorno

Crea un archivo `.env` en la raíz del proyecto:

```env
# Database Configuration
DATABASE_USER=admin
DATABASE_PASSWORD=admin123

# PostgreSQL
POSTGRES_DATABASE=dawazon_db
POSTGRES_PORT=5432

# MongoDB
MONGO_DATABASE=dawazon_mongo
MONGO_PORT=27017

# Redis
REDIS_PASSWORD=redis_secure_password
REDIS_PORT=6379

# JWT
JWT_SECRET=tu_secreto_super_seguro_aqui
JWT_EXPIRATION=86400000

# Stripe
STRIPE_API_KEY=sk_test_tu_clave_aqui
STRIPE_WEBHOOK_SECRET=whsec_tu_webhook_secret

# Email Configuration
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=tu_email@gmail.com
MAIL_PASSWORD=tu_app_password
```

### 3️⃣ Compilar el proyecto

```bash
# Con Gradle Wrapper (recomendado)
./gradlew clean build

# O con Gradle instalado globalmente
gradle clean build
```

### 4️⃣ Ejecutar tests

```bash
./gradlew test
```

### 5️⃣ Generar reporte de cobertura

```bash
./gradlew jacocoTestReport
```

📊 **Reporte disponible en**:  `build/reports/jacoco/test/html/index.html`

### 6️⃣ Generar documentación

```bash
./gradlew dokkaHtml
```

📖 **Documentación disponible en**: `build/dokka/html/index.html`

---

## 🐳 Despliegue con Docker

### 🚀 Inicio Rápido

```bash
# Construir y levantar todos los servicios
docker-compose up -d

# Ver logs en tiempo real
docker-compose logs -f

# Detener servicios
docker-compose down

# Detener y eliminar volúmenes
docker-compose down -v
```

### 🌐 Servicios Disponibles

Una vez desplegado, accede a:

| Servicio | URL | Descripción | Puerto |
|----------|-----|-------------|--------|
| 🏠 **Frontend Principal** | `http://localhost` | Aplicación web | 80 |
| 🚀 **API REST** | `http://localhost/` | Endpoints REST y vistas | - |
| 📊 **JaCoCo Reports** | Dentro del contenedor | Cobertura de código | - |
| 📖 **Documentación Dokka** | Dentro del contenedor | Docs técnicas | - |
| 🐘 **PostgreSQL** | `localhost:5432` | Base de datos SQL | 5432 |
| 🍃 **MongoDB** | `localhost:27017` | Base de datos NoSQL | 27017 |
| 🔴 **Redis** | `localhost:6379` | Caché y sesiones | 6379 |

---

## 🏗️ Arquitectura

```
┌─────────────────────────────────────────────────────────────┐
│                    Spring Boot Application                   │
│                         (Puerto 80)                          │
└────────┬─────────────────────────────────────────────────────┘
         │
    ┌────▼────────────────────────────────────────────┐
    │         Spring Boot MVC Application             │
    │  ┌──────────┐  ┌──────────┐  ┌──────────┐      │
    │  │Controller│  │ Service  │  │Repository│      │
    │  └─────┬────┘  └─────┬────┘  └─────┬────┘      │
    │  ┌─────▼─────────────▼─────────────▼────┐      │
    │  │    Security Layer (Spring Security)  │      │
    │  └──────────────────────────────────────┘      │
    │  ┌──────────┐  ┌──────────┐  ┌─────────┐       │
    │  │  Pebble  │  │  Cache   │  │ Storage │       │
    │  │Templates │  │  Manager │  │ Service │       │
    │  └──────────┘  └──────────┘  └─────────┘       │
    └─────┬────────────┬────────────┬────────────────┘
          │            │            │
    ┌─────▼─────┐ ┌───▼──────┐ ┌──▼─────┐
    │PostgreSQL │ │ MongoDB  │ │ Redis  │
    │(Usuarios, │ │(Productos│ │(Cache) │
    │ Carritos) │ │Comentario│ │        │
    └───────────┘ └──────────┘ └────────┘
```

### 🔄 Flujo de Datos

1. **Cliente** → `Spring Boot MVC` (puerto 80)
2. **Spring Boot** → Procesamiento con arquitectura en capas:
    - `Controller` → Recibe HTTP requests y renderiza vistas Pebble
    - `Service` → Lógica de negocio y validaciones
    - `Repository` → Acceso a datos
3. **Cache Layer** → Redis intercepta consultas frecuentes
4. **Data Layer** →
    - PostgreSQL para usuarios y carritos
    - MongoDB para productos y comentarios
5. **Response** → Renderizado de plantillas Pebble → Cliente

---

## 👥 Roles y Funcionalidades

### 🛍️ Usuario (USER)

#### Autenticación
- ✅ Registro con validación (`/auth/signup`)
- ✅ Login seguro (`/auth/signin`)
- ✅ Cambio de contraseña (`/auth/me/changepassword`)

#### Perfil
- ✅ Ver perfil (`/auth/me`)
- ✅ Editar perfil (`/auth/me/edit`)
- ✅ Actualizar avatar
- ✅ Gestionar datos personales

#### Productos
- ✅ Ver catálogo completo (`/`, `/products`)
- ✅ Búsqueda y filtros por nombre y categoría
- ✅ Ver detalles de productos (`/products/{id}`)
- ✅ Añadir comentarios (`/products/{id}/comentarios`)
- ✅ Gestión de favoritos

#### Carrito y Compras
- ✅ Gestión de carrito de compras
- ✅ Ver historial de compras

---

### 📊 Manager (MANAGER)

#### Todas las funcionalidades de USER +

#### Gestión de Productos
- ✅ Acceso a funcionalidades de gestión de inventario

---

### ⚙️ Administrador (ADMIN)

#### Todas las funcionalidades de MANAGER +

#### Gestión de Productos
- ✅ Editar productos (`/products/edit/{id}`)
- ✅ Actualizar información de productos
- ✅ Subir imágenes de productos

#### Gestión de Ventas
- ✅ Ver todas las ventas (`/admin/ventas`)
- ✅ Ver detalles de venta (`/admin/ventas/{ventaId}/{productId}`)
- ✅ Cancelar ventas (`/admin/ventas/cancel/{ventaId}/{productId}`)
- ✅ Editar líneas de venta (`/admin/ventas/edit/{ventaId}/{productId}`)
- ✅ Calcular ganancias totales

#### Gestión de Usuarios
- ✅ Ver todos los usuarios (`/admin/users`)
- ✅ Crear usuarios
- ✅ Editar usuarios
- ✅ Eliminar usuarios
- ✅ Asignar roles

---

## 🔌 Endpoints de la Aplicación

### 🔐 Autenticación

```http
GET    /auth/signin                # Formulario de login
GET    /auth/signup                # Formulario de registro
POST   /auth/signup                # Procesar registro
GET    /auth/me/changepassword     # Formulario cambio de contraseña [USER]
POST   /auth/me/changepassword     # Procesar cambio de contraseña [USER]
```

---

### 👤 Usuarios

```http
# Perfil propio
GET    /auth/me                    # Ver mi perfil [USER]
GET    /auth/me/edit               # Formulario editar perfil [USER]
POST   /auth/me/edit               # Actualizar mi perfil [USER]
GET    /auth/me/delete             # Eliminar mi cuenta [USER]

# Favoritos
GET    /auth/me/fav                # Mis favoritos [USER]
POST   /auth/me/fav/{id}           # Añadir a favoritos [USER]
DELETE /auth/me/fav/{id}           # Quitar de favoritos [USER]

# Historial de compras
GET    /auth/me/purchase           # Mis compras [USER]
GET    /auth/me/purchase/{id}      # Detalle de compra [USER]

# Administración (ADMIN)
GET    /admin/users                # Listar usuarios [ADMIN]
GET    /admin/users/{id}           # Ver usuario [ADMIN]
POST   /admin/users                # Crear usuario [ADMIN]
POST   /admin/users/edit           # Actualizar usuario [ADMIN]
GET    /admin/users/delete/{id}    # Eliminar usuario [ADMIN]
```

---

### 📦 Productos

```http
# Público
GET    /                           # Página principal con productos
GET    /products                   # Listar productos
GET    /products/                  # Listar productos (alternativo)
GET    /products/{id}              # Ver detalle de producto

# Búsqueda y filtros
GET    /products? name=             # Búsqueda por nombre
GET    /products?categoria=        # Filtrar por categoría
GET    /products?page=&size=       # Paginación
GET    /products? sortBy=&direction= # Ordenación

# Comentarios (USER)
POST   /products/{id}/comentarios  # Añadir comentario [USER]

# Administración (ADMIN)
GET    /products/edit/{id}         # Formulario editar producto [ADMIN]
POST   /products/edit/             # Actualizar producto [ADMIN]
```

---

### 🛒 Carrito

```http
GET    /cart                       # Ver mi carrito [USER]
POST   /cart/add                   # Añadir producto [USER]
POST   /cart/update                # Actualizar cantidad [USER]
POST   /cart/remove                # Eliminar producto [USER]
POST   /cart/checkout              # Finalizar compra [USER]
```

---

### 📋 Ventas (Admin)

```http
GET    /admin/ventas               # Listar todas las ventas [ADMIN]
GET    /admin/ventas/{ventaId}/{productId}  # Detalle de venta [ADMIN]
GET    /admin/ventas/cancel/{ventaId}/{productId}  # Cancelar venta [ADMIN]
GET    /admin/ventas/edit/{ventaId}/{productId}    # Formulario editar venta [ADMIN]
POST   /admin/venta/edit           # Actualizar línea de venta [ADMIN]
```

---

### 📁 Almacenamiento

```http
GET    /files/{filename}           # Obtener archivo subido (imágenes, etc.)
```

---

## 📁 Estructura del Proyecto

```
dawazon/
├── 📁 src/
│   ├── 📁 main/
│   │   ├── 📁 java/dev/luisvives/dawazon/
│   │   │   ├── 📁 cart/                      # Módulo de Carrito
│   │   │   │   ├── 📁 controller/
│   │   │   │   │   └── AdminPurchasedController.java
│   │   │   │   ├── 📁 dto/
│   │   │   │   ├── 📁 mapper/
│   │   │   │   ├── 📁 models/
│   │   │   │   ├── 📁 repository/
│   │   │   │   └── 📁 service/
│   │   │   │       └── CartServiceImpl.java
│   │   │   │
│   │   │   ├── 📁 common/                    # Componentes comunes
│   │   │   │   ├── 📁 controller/
│   │   │   │   │   └── GlobalFuncionController.java  # ControllerAdvice
│   │   │   │   └── 📁 storage/
│   │   │   │       ├── 📁 controller/
│   │   │   │       │   └── StorageController.java
│   │   │   │       └── 📁 service/
│   │   │   │           └── StorageService. java
│   │   │   │
│   │   │   ├── 📁 config/                    # Configuración
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   ├── CacheConfig.java
│   │   │   │   └── ... 
│   │   │   │
│   │   │   ├── 📁 products/                  # Módulo de Productos
│   │   │   │   ├── 📁 controller/
│   │   │   │   │   └── ProductsController.java
│   │   │   │   ├── 📁 dto/
│   │   │   │   │   └── PostProductRequestDto.java
│   │   │   │   ├── 📁 mapper/
│   │   │   │   │   └── ProductMapper.java
│   │   │   │   ├── 📁 models/
│   │   │   │   │   ├── Product.java
│   │   │   │   │   └── Comment.java
│   │   │   │   ├── 📁 repository/
│   │   │   │   └── 📁 service/
│   │   │   │       ├── ProductService.java
│   │   │   │       └── ProductServiceImpl.java
│   │   │   │
│   │   │   ├── 📁 users/                     # Módulo de Usuarios
│   │   │   │   ├── 📁 controller/
│   │   │   │   │   ├── AuthController.java
│   │   │   │   │   └── UserController.java
│   │   │   │   ├── 📁 dto/
│   │   │   │   │   ├── UserRegisterDto.java
│   │   │   │   │   ├── UserRequestDto.java
│   │   │   │   │   └── UserChangePasswordDto.java
│   │   │   │   ├── 📁 mapper/
│   │   │   │   │   └── UserMapper.java
│   │   │   │   ├── 📁 models/
│   │   │   │   │   ├── User.java
│   │   │   │   │   └── Role.java
│   │   │   │   ├── 📁 repository/
│   │   │   │   ├── 📁 service/
│   │   │   │   │   ├── AuthService.java
│   │   │   │   │   ├── UserService.java
│   │   │   │   │   └── FavService.java
│   │   │   │   └── 📁 exceptions/
│   │   │   │
│   │   │   └── DawazonApplication.java       # Clase principal
│   │   │
│   │   └── 📁 resources/
│   │       ├── application.properties
│   │       ├── application. yml
│   │       └── 📁 templates/                 # Plantillas Pebble
│   │           └── 📁 web/
│   │               ├── 📁 auth/
│   │               ├── 📁 cart/
│   │               ├── 📁 productos/
│   │               └── 📁 user/
│   │
│   └── 📁 test/
│       └── 📁 java/
│           └── (tests con Testcontainers)
│
├── 📁 database/                              # Scripts DB
│   ├── init. sql                              # Schema PostgreSQL
│   └── tienda.js                             # Collections MongoDB
│
├── 📁 storage-dir/                           # Almacenamiento
│   └── 📁 uploads/                           # Imágenes subidas
│
├── 📁 custom/                                # Personalizaciones
│   └── 📁 report/
│       └── report.css                        # CSS personalizado JaCoCo
│
├── 📁 proxy/                                 # Configuración NGINX (si existe)
├── 📁 gradle/                                # Gradle wrapper
├── 📄 docker-compose.yml                     # Orquestación Docker
├── 📄 dockerfile                             # Dockerfile multi-stage
├── 📄 build.gradle. kts                       # Build Gradle
├── 📄 settings.gradle.kts                    # Settings Gradle
├── 📄 gradlew                                # Gradle wrapper script
├── 📄 . env                                   # Variables de entorno
├── 📄 . gitignore
├── 📄 LICENCE
├── 📄 TERMS_OF_USE. md
└── 📄 README.md
```

---

## 🧪 Testing

### Ejecutar tests unitarios

```bash
# Todos los tests
./gradlew test

# Con reporte de cobertura
./gradlew test jacocoTestReport
```

### Tests con Testcontainers

El proyecto utiliza **Testcontainers** para levantar contenedores reales de PostgreSQL, MongoDB y Redis durante los tests.

### 📊 Reporte de Cobertura (JaCoCo)

```bash
# Generar reporte
./gradlew test jacocoTestReport

# Abrir en navegador
open build/reports/jacoco/test/html/index.html  # macOS
xdg-open build/reports/jacoco/test/html/index.html  # Linux
start build/reports/jacoco/test/html/index.html  # Windows
```

#### Exclusiones configuradas

Los siguientes paquetes están excluidos del reporte:
- `config/**` - Configuraciones de Spring
- `email/**` - Servicios de email
- `notificaciones/**` - Sistema de notificaciones
- `handler/**` - Exception handlers

---

## 📄 Licencia

Este proyecto está bajo la licencia especificada en el archivo `LICENCE`.

---

## 📞 Contacto

**Repositorio**:  [https://github.com/Aragorn7372/dawazon](https://github.com/Aragorn7372/dawazon)

---

## 🎯 Notas

Este README ha sido actualizado para reflejar con precisión:
- ✅ Los endpoints **realmente implementados** en los controladores
- ✅ La estructura de carpetas **real** del proyecto
- ✅ Las tecnologías **realmente utilizadas** según `build.gradle.kts`
- ✅ Las funcionalidades **disponibles** actualmente

