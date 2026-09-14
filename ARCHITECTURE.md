# Sistema de Inventario General de Equipos - Estructura de Carpetas

```
sistema-inventario-equipos/
│
├── src/
│   ├── main/
│   │   ├── java/com/inventario/
│   │   │   ├── Application.java              # Clase principal de Spring Boot
│   │   │   │
│   │   │   ├── config/                       # Configuraciones
│   │   │   │   ├── JwtTokenProvider.java     # Proveedor de tokens JWT
│   │   │   │   ├── PasswordEncoderConfig.java
│   │   │   │   ├── SecurityConfig.java       # Configuración de seguridad
│   │   │   │   └── CorsConfig.java
│   │   │   │
│   │   │   ├── security/                     # Seguridad
│   │   │   │   └── JwtAuthenticationFilter.java
│   │   │   │
│   │   │   ├── entity/                       # Entidades JPA
│   │   │   │   ├── Usuario.java
│   │   │   │   ├── Equipo.java
│   │   │   │   ├── Mantenimiento.java
│   │   │   │   ├── MantenimientoRepuesto.java
│   │   │   │   ├── MantenimientoFoto.java
│   │   │   │   ├── Cliente.java
│   │   │   │   ├── CategoriaEquipo.java
│   │   │   │   ├── EstadoEquipo.java
│   │   │   │   ├── TipoMotor.java
│   │   │   │   └── EstadoMantenimiento.java
│   │   │   │
│   │   │   ├── repository/                   # Data Access Layer
│   │   │   │   ├── UsuarioRepository.java
│   │   │   │   ├── EquipoRepository.java
│   │   │   │   ├── MantenimientoRepository.java
│   │   │   │   ├── ClienteRepository.java
│   │   │   │   ├── CategoriaEquipoRepository.java
│   │   │   │   ├── EstadoEquipoRepository.java
│   │   │   │   ├── MantenimientoRepuestoRepository.java
│   │   │   │   └── MantenimientoFotoRepository.java
│   │   │   │
│   │   │   ├── service/                      # Business Logic
│   │   │   │   ├── UsuarioService.java
│   │   │   │   ├── EquipoService.java
│   │   │   │   ├── MantenimientoService.java (próximo)
│   │   │   │   └── ClienteService.java
│   │   │   │
│   │   │   ├── controller/                   # REST API Layer
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── EquipoController.java
│   │   │   │   ├── MantenimientoController.java (próximo)
│   │   │   │   └── ClienteController.java
│   │   │   │
│   │   │   ├── dto/                          # Data Transfer Objects
│   │   │   │   ├── UsuarioDTO.java
│   │   │   │   ├── EquipoDTO.java
│   │   │   │   ├── MantenimientoDTO.java
│   │   │   │   ├── MantenimientoRepuestoDTO.java
│   │   │   │   ├── ClienteDTO.java
│   │   │   │   ├── CategoriaEquipoDTO.java
│   │   │   │   ├── LoginRequest.java
│   │   │   │   └── LoginResponse.java
│   │   │   │
│   │   │   └── exception/                    # Exception Handling (opcional)
│   │   │       └── GlobalExceptionHandler.java
│   │   │
│   │   └── resources/
│   │       ├── application.yml               # Configuración principal
│   │       ├── application-prod.yml          # Configuración producción
│   │       └── db/migration/
│   │           └── V1__schema_inicial.sql    # Scripts Flyway
│   │
│   └── test/
│       ├── java/com/inventario/
│       │   ├── repository/                   # Test repositorios
│       │   ├── service/                      # Test servicios
│       │   └── controller/                   # Test controladores
│       └── resources/
│           └── application-test.yml
│
├── pom.xml                                   # Maven POM
├── Dockerfile                                # Docker build
├── docker-compose.yml                        # Docker Compose para desarrollo
├── .gitignore                                # Git ignore
├── .env.example                              # Ejemplo variables de entorno
├── README.md                                 # Documentación
└── ARCHITECTURE.md                           # Arquitectura (próximo)
```

## Descripción por Capas

### 1. **Entity Layer** (`entity/`)
Define las entidades JPA mapeadas a las tablas MySQL. Cada entidad tiene:
- Validaciones básicas con anotaciones JPA
- Relaciones con otras entidades
- Métodos `@PrePersist` y `@PreUpdate` para auditoría

### 2. **Repository Layer** (`repository/`)
Interfaces que heredan de `JpaRepository` para CRUD básico y consultas personalizadas.
Evita lógica de negocio; solo acceso a datos.

### 3. **Service Layer** (`service/`)
Lógica de negocio centralizada:
- Validaciones de reglas
- Transformaciones entre entidades y DTOs
- Transacciones
- Orquestación de repositorios

### 4. **Controller Layer** (`controller/`)
Endpoints REST que:
- Reciben DTOs
- Delegan en servicios
- Retornan DTOs o estados HTTP
- Manejo de excepciones

### 5. **DTO Layer** (`dto/`)
Objetos de transferencia que:
- Desacoplan la API de las entidades
- Validan entrada/salida
- Ofrecen vistas específicas de datos

### 6. **Security Layer** (`config/` + `security/`)
- Autenticación con JWT
- Autorización basada en roles
- Encriptación de contraseñas (BCrypt)

## Próximos Pasos

1. ✅ Crear servicios para Mantenimiento
2. ✅ Crear controladores para Mantenimiento
3. ✅ Implementar búsquedas avanzadas
4. ✅ Validación en DTOs
5. ✅ Tests unitarios e integración
6. ✅ Documentación OpenAPI/Swagger
7. ✅ Logs y auditoría
8. ✅ Manejo centralizado de excepciones
