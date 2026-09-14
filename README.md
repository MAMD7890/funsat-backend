# Sistema de Inventario General de Equipos

Sistema completo de gestión de inventario para equipos menores, mayores, vehículos, tráilers y maquinaria pesada, con control de mantenimientos, repuestos y evidencia fotográfica.

## Stack Tecnológico

- **Backend**: Java 17, Spring Boot 3.3, Spring Security
- **Base de Datos**: MySQL 8.0+
- **ORM**: JPA/Hibernate
- **Autenticación**: JWT (JSON Web Tokens)
- **Build**: Maven
- **Migraciones**: Flyway

## Requisitos Previos

- Java 17+
- Maven 3.8+
- MySQL 8.0+
- Git

## Instalación

### 1. Clonar el repositorio

```bash
git clone <tu-repo>
cd sistema-inventario-equipos
```

### 2. Crear la base de datos

```bash
mysql -u root -p < src/main/resources/db/migration/V1__schema_inicial.sql
```

### 3. Configurar variables de entorno

Crear un archivo `.env` en la raíz del proyecto:

```env
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/inventario_equipos?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=tu_password
JWT_SECRET=tu_clave_muy_larga_y_segura_de_al_menos_256_bits
JWT_EXPIRATION=86400000
```

### 4. Compilar y ejecutar

```bash
mvn clean install
mvn spring-boot:run
```

La aplicación estará disponible en: `http://localhost:8080/api`

## Estructura del Proyecto

```
src/
├── main/
│   ├── java/com/inventario/
│   │   ├── config/          # Configuración (Security, JWT, etc)
│   │   ├── controller/      # Controladores REST
│   │   ├── dto/             # Data Transfer Objects
│   │   ├── entity/          # Entidades JPA
│   │   ├── repository/      # Interfaces de acceso a datos
│   │   ├── service/         # Lógica de negocio
│   │   └── Application.java # Main class
│   └── resources/
│       ├── application.yml   # Configuración Spring
│       └── db/migration/     # Scripts SQL Flyway
└── test/
    └── java/com/inventario/ # Tests unitarios e integración
```

## Módulos Principales

### 1. **Gestión de Usuarios**
- Autenticación con JWT
- Roles: ADMIN, TÉCNICO, CONSULTA
- Control de acceso basado en roles (RBAC)

### 2. **Catálogos**
- Categorías de equipos
- Estados de equipos
- Tipos de motores
- Estados de mantenimiento

### 3. **Equipos**
- Registro de equipos propios y externos
- Búsqueda y filtrado avanzado
- Historial completo de cambios

### 4. **Mantenimientos**
- Registro de mantenimientos preventivos, correctivos y predictivos
- Cálculo automático de costos
- Seguimiento de estados

### 5. **Repuestos**
- Gestión de repuestos por mantenimiento
- Costo unitario y total

### 6. **Evidencia Fotográfica**
- Almacenamiento de fotos (antes, durante, después)
- Integración con mantenimientos

## API Endpoints

### Autenticación
```
POST   /auth/login          - Iniciar sesión
POST   /auth/register       - Registrar nuevo usuario
POST   /auth/refresh        - Renovar token JWT
```

### Equipos
```
GET    /equipos             - Listar todos los equipos
GET    /equipos/{id}        - Obtener detalle del equipo
POST   /equipos             - Crear nuevo equipo
PUT    /equipos/{id}        - Actualizar equipo
DELETE /equipos/{id}        - Eliminar equipo
GET    /equipos/search      - Búsqueda avanzada
```

### Mantenimientos
```
GET    /mantenimientos      - Listar mantenimientos
GET    /mantenimientos/{id} - Obtener detalle
POST   /mantenimientos      - Crear mantenimiento
PUT    /mantenimientos/{id} - Actualizar
```

### Clientes
```
GET    /clientes            - Listar clientes
POST   /clientes            - Crear cliente
PUT    /clientes/{id}       - Actualizar
```

## Modelos de Datos

### Tablas Principales

#### `equipo`
- Identificación completa (código, serie, modelo, marca)
- Clasificación (categoría, tipo de motor)
- Propiedad (propio o externo)
- Estado y ubicación
- Checks del inventario master

#### `mantenimiento`
- Tipo (preventivo, correctivo, predictivo)
- Descripción del trabajo y diagnóstico
- Fechas de apertura, inicio y cierre
- Costos (mano de obra, repuestos, total calculado)

#### `usuario`
- Rol basado (admin, técnico, consulta)
- Contraseña hasheada con bcrypt
- Estado de activación

## Vistas Predefinidas

- `v_equipos` - Búsqueda unificada de equipos con resumen de mantenimientos
- `v_historial_mantenimientos` - Historial completo con detalles
- `v_mantenimientos_abiertos` - Mantenimientos pendientes

## Seguridad

- ✅ Autenticación con JWT
- ✅ Control de acceso por roles
- ✅ Contraseñas hasheadas (BCrypt)
- ✅ CORS configurado
- ✅ SQL Injection prevention (prepared statements)
- ✅ HTTPS ready

## Desarrollo

### Ejecutar tests
```bash
mvn test
```

### Generar JAR
```bash
mvn clean package
```

### Ejecutar en modo desarrollo
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--debug"
```

## Despliegue

### Docker
```bash
docker build -t inventario-equipos .
docker run -p 8080:8080 --env-file .env inventario-equipos
```

### Producción
- Cambiar `ddl-auto` a `validate` en `application.yml`
- Configurar JWT_SECRET con valor seguro
- Usar HTTPS
- Configurar pool de conexiones MySQL
- Implementar logging centralizado

## Troubleshooting

**Error: "Cannot connect to database"**
- Verificar que MySQL está corriendo
- Confirmar credenciales en `.env`
- Ejecutar script de inicialización de BD

**Error: "JWT token expired"**
- Renovar token usando endpoint `/auth/refresh`
- Aumentar `JWT_EXPIRATION` si es necesario

## Contribución

1. Crear rama feature: `git checkout -b feature/nueva-funcionalidad`
2. Commit cambios: `git commit -am 'Agregar feature'`
3. Push: `git push origin feature/nueva-funcionalidad`
4. Pull Request

## Licencia

Propietario - Todos los derechos reservados

## Contacto

Para preguntas o sugerencias, contactar al equipo de desarrollo.

---

**Última actualización**: 2026-06-09
