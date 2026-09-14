# Guía Rápida de Inicio

## Instalación Local

### 1. Requisitos Previos
- **Java 17+**
- **Maven 3.8+**
- **MySQL 8.0+**
- **Git**

Verificar instalación:
```bash
java -version
mvn -version
mysql --version
```

### 2. Clonar el repositorio
```bash
git clone <URL_REPOSITORIO>
cd sistema-inventario-equipos
```

### 3. Crear la base de datos

#### Opción A: Línea de comandos
```bash
mysql -u root -p < src/main/resources/db/migration/V1__schema_inicial.sql
```

#### Opción B: Desde MySQL Workbench
1. Abrir MySQL Workbench
2. Conectarse al servidor
3. Copiar el contenido de `V1__schema_inicial.sql` en una consulta
4. Ejecutar

### 4. Configurar variables de entorno

Copiar el archivo de ejemplo:
```bash
cp .env.example .env
```

Editar `.env` con tus credenciales:
```env
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/inventario_equipos?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=tu_password
JWT_SECRET=tu_clave_secreta_minimo_256_caracteres
```

### 5. Compilar el proyecto
```bash
mvn clean install
```

### 6. Ejecutar la aplicación

#### Desarrollo local
```bash
mvn spring-boot:run
```

#### Empaquetar como JAR
```bash
mvn clean package
java -jar target/sistema-inventario-equipos-1.0.0.jar
```

La aplicación estará disponible en: **http://localhost:8080/api**

---

## Uso con Docker

### Requisitos
- Docker
- Docker Compose

### Iniciar con Docker Compose
```bash
docker-compose up -d
```

### Detener
```bash
docker-compose down
```

### Ver logs
```bash
docker-compose logs -f app
```

---

## Credenciales de Acceso

**Usuario de prueba:**
- **Usuario**: `admin`
- **Contraseña**: `admin123`
- **Rol**: `ADMIN`

Cambiar en producción en `.env` o configurar nuevos usuarios.

---

## Endpoints Principales

### Autenticación
```bash
# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"nombre":"admin","password":"admin123"}'

# Respuesta
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tipo": "Bearer",
  "id": 1,
  "nombre": "admin",
  "rol": "ADMIN"
}
```

### Usar el Token
Agregar en el header `Authorization`:
```bash
curl -X GET http://localhost:8080/api/equipos \
  -H "Authorization: Bearer <TU_TOKEN>"
```

### Equipos
```bash
# Listar todos
GET /equipos

# Obtener por ID
GET /equipos/{id}

# Crear
POST /equipos
Body: {
  "codigo": "117001",
  "descripcion": "Pulidora 7\"",
  "categoriaId": 1,
  "esPropio": true,
  "estadoId": 1,
  "ubicacion": "Almacén"
}

# Actualizar
PUT /equipos/{id}

# Buscar
GET /equipos/buscar?termino=pulidora

# Equipos propios
GET /equipos/propios

# Equipos externos
GET /equipos/externos
```

### Clientes
```bash
# Listar
GET /clientes

# Crear
POST /clientes
Body: {
  "nombre": "Nuevo Cliente",
  "telefono": "+57...",
  "email": "cliente@example.com"
}
```

### Mantenimientos
```bash
# Listar todos
GET /mantenimientos

# Por equipo
GET /mantenimientos/equipo/{equipoId}

# Por rango de fechas
GET /mantenimientos/rango?desde=2026-01-01&hasta=2026-12-31

# Crear
POST /mantenimientos
Body: {
  "equipoId": 1,
  "tipo": "CORRECTIVO",
  "descripcionTrabajo": "Reparación...",
  "diagnostico": "...",
  "estadoId": 1,
  "costoManoObra": 50000,
  "costoRepuestos": 30000
}
```

---

## Testing

### Ejecutar tests
```bash
mvn test
```

### Coverage de tests
```bash
mvn clean test jacoco:report
```

---

## Troubleshooting

### "Connection refused" a MySQL
```bash
# Verificar que MySQL está corriendo
mysql -u root -p

# Windows: Iniciar servicio
net start MySQL80

# Linux:
sudo systemctl start mysql
```

### "JWT token expired"
El token expira cada 24 horas. Generar uno nuevo haciendo login.

### "Access Denied" a la base de datos
Verificar credenciales en `.env` o `application.yml`

### Puerto 8080 ya en uso
```bash
# Cambiar en application.yml
server.port: 8081
```

---

## Build y Despliegue

### Crear imagen Docker
```bash
docker build -t inventario-equipos:latest .
```

### Push a repositorio
```bash
docker tag inventario-equipos:latest <TU_REGISTRY>/inventario-equipos:latest
docker push <TU_REGISTRY>/inventario-equipos:latest
```

### Desplegar en producción
Cambiar en `application.yml`:
```yaml
spring.jpa.hibernate.ddl-auto: validate
spring.profiles.active: prod
```

---

## Soporte y Contacto

Para reportar problemas: issues@inventario.local

Documentación completa: Ver `README.md` y `ARCHITECTURE.md`
