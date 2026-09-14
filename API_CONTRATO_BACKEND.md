# Contrato de API — Backend Sistema Inventario Equipos

> Este documento describe **exactamente lo que el backend (Spring Boot 3.3, Java 17) implementa hoy**, verificado leyendo el código fuente (controllers, DTOs, entidades, seguridad). No es un documento de diseño ni de intenciones.
>
> ⚠️ El repo también tiene `FRONTEND_SPECS.md`, que describe una app más ambiciosa (gestión de usuarios, repuestos, fotos, categorías editables, paginación, etc.). **Gran parte de eso NO existe en el backend real.** Si hay conflicto entre ambos documentos, este (`API_CONTRATO_BACKEND.md`) es la fuente de verdad para lo que se puede consumir hoy. Al final hay una sección "Qué NO existe todavía" para que no se intente llamar a endpoints que no están implementados.

---

## 1. Base URL y configuración

```
http://localhost:8080/api
```

- `server.port=8080`
- `server.servlet.context-path=/api` (todas las rutas de abajo ya incluyen este prefijo)
- No hay paginación server-side en ningún endpoint. Todo listado devuelve un **array JSON plano** (`[ {...}, {...} ]`), no un objeto `{content, totalPages, ...}`. Si se necesita paginación en el front, debe hacerse client-side sobre el array completo.
- No existe `PATCH` en ningún endpoint, solo `GET`, `POST`, `PUT`, `DELETE`.

### CORS (importante para desarrollo con Angular en `localhost:4200`)

La configuración actual de CORS en el backend tiene un bug: el mapping está hecho sobre `/api/**`, pero como `context-path` ya es `/api`, Spring recorta ese prefijo antes de evaluar el CORS matcher, por lo que la regla nunca aplica realmente. **Es posible que aparezcan errores de CORS en el navegador al consumir la API desde Angular en dev.** Si esto ocurre, es un problema del backend a corregir (cambiar el mapping a `/**` o registrar un `CorsConfigurationSource` explícito), no algo que se pueda arreglar solo desde el front.

---

## 2. Autenticación

### Flujo

1. `POST /api/auth/login` (o `/api/auth/register` para crear cuenta) → se recibe un JWT.
2. Guardar el token (ej. en `localStorage` o un servicio de auth).
3. En cada request subsecuente que requiera autenticación, mandar el header:
   ```
   Authorization: Bearer <token>
   ```
   (literal `"Bearer "` con un solo espacio, sensible a mayúsculas).
4. El token expira en **24 horas** (`jwt.expiration=86400000` ms). **No existe endpoint de refresh token.** Cuando expire, el usuario debe volver a hacer login (el front debe interceptar 401/403 y redirigir a `/login`).

### `POST /api/auth/login`

**Request body:**
```json
{
  "nombre": "admin",
  "password": "admin123"
}
```
No hay validaciones server-side (`@NotBlank`, etc.) en estos campos — si se manda vacío, puede resultar en un error genérico. Validar en el front (`Validators.required`).

**Respuesta exitosa — `200 OK`:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9....",
  "tipo": "Bearer",
  "id": 1,
  "nombre": "admin",
  "rol": "ADMIN"
}
```
- `rol` es uno de: `"ADMIN" | "TECNICO" | "CONSULTA"`.

**Respuestas de error — `401 Unauthorized`:**
El cuerpo es un **string JSON plano**, no un objeto:
```json
"Contraseña incorrecta"
```
o
```json
"Credenciales inválidas"
```
(usuario no existe o está inactivo). El front debe manejar este caso especial: `error.error` será directamente el string, no `error.error.message`.

### `POST /api/auth/register`

**Request body:** mismo shape que login:
```json
{
  "nombre": "juan.perez",
  "password": "unaClaveSegura"
}
```
- ⚠️ **Siempre se crea con rol `TECNICO`**, hardcodeado en el backend. No hay forma de elegir rol desde el registro — el formulario de registro **no debe** mostrar selector de rol.

**Respuesta exitosa — `200 OK`:** string plano:
```json
"Usuario creado exitosamente"
```

**Error — `400 Bad Request`:** string plano:
```json
"El usuario ya existe"
```

**Error — `500`:** string plano `"Error al crear usuario: <detalle>"`.

### `POST /api/auth/change-password` (requiere JWT — ya NO es público)

⚠️ Este endpoint necesita estar autenticado (identifica al usuario por el `sub` del JWT), por eso `/auth/**` dejó de ser `permitAll()` — solo `/auth/login` y `/auth/register` siguen siendo públicos.

**Request body:**
```json
{ "currentPassword": "actual123", "newPassword": "nuevaClaveSegura" }
```
**Respuesta exitosa — `200 OK`:**
```json
{ "message": "Contraseña actualizada exitosamente" }
```
**Error — `400 Bad Request`:** string plano (`"La contraseña actual es incorrecta"`, `"La nueva contraseña debe tener al menos 6 caracteres"`, etc.).

### Roles disponibles (`Usuario.Rol`)
```
ADMIN | TECNICO | CONSULTA
```

### `GET /api/usuarios?rol=` (ADMIN/TECNICO/CONSULTA)

Lista usuarios **activos**, opcionalmente filtrados por rol (`?rol=TECNICO`). Pensado para poblar el combo de "técnico" al crear/editar un mantenimiento. No expone `contrasenaHash`.

Respuesta `200`:
```json
[{ "id": 2, "nombre": "jperez", "rol": "TECNICO", "activo": true }]
```

No hay CRUD de usuarios (crear/editar/desactivar) desde esta API — solo lectura vía este endpoint y creación implícita vía `/auth/register` (siempre rol `TECNICO`).

---

## 3. Forma de las respuestas de error (para el interceptor HTTP de Angular)

Hay **tres formas distintas** de error, según el endpoint y el tipo de fallo. El `HttpInterceptor` de Angular debe contemplar los tres casos:

1. **Auth (login/register) en fallo esperado** → cuerpo es un **string JSON plano** (ej. `"Credenciales inválidas"`).
2. **Clientes / Equipos / Mantenimientos en fallo esperado (404, 400)** → cuerpo **vacío** (sin JSON). No intentar leer `error.message`, no existe.
3. **Error no controlado / inesperado** (ej. excepción no capturada) → objeto JSON:
   ```json
   {
     "timestamp": "2026-07-05T10:15:30.123456",
     "status": 500,
     "error": "Error Interno",
     "message": "<mensaje de la excepción o null>"
   }
   ```
   Variante para `IllegalArgumentException`:
   ```json
   {
     "timestamp": "2026-07-05T10:15:30.123456",
     "status": 400,
     "error": "Solicitud Inválida",
     "message": "<mensaje>"
   }
   ```
4. **401/403 de Spring Security** (token ausente/expirado/inválido, o rol insuficiente para `@PreAuthorize`) → respuesta por defecto de Spring Security, normalmente **sin cuerpo JSON**. Solo se puede confiar en el status code.

**Recomendación práctica para el front:** en el interceptor, ante cualquier error HTTP, mostrar un mensaje genérico salvo que `error.error` sea un string (mostrar tal cual) o un objeto con `message` (mostrar `error.error.message`). No asumir que siempre hay `message`.

---

## 4. Seguridad por endpoint (roles requeridos)

| Recurso | Base | Público | GET | POST | PUT | DELETE |
|---|---|---|---|---|---|---|
| Auth | `/api/auth` | Solo login/register | — | — | — | — |
| Usuarios | `/api/usuarios` | No | ADMIN/TECNICO/CONSULTA | — | — | — |
| Clientes | `/api/clientes` | No | ADMIN/TECNICO/CONSULTA | ADMIN/TECNICO | ADMIN/TECNICO | solo ADMIN |
| Equipos | `/api/equipos` | No | ADMIN/TECNICO/CONSULTA | ADMIN/TECNICO | ADMIN/TECNICO | solo ADMIN |
| Mantenimientos | `/api/mantenimientos` | No | ADMIN/TECNICO/CONSULTA | ADMIN/TECNICO | ADMIN/TECNICO | solo ADMIN |
| Repuestos mant. | `/api/mantenimientos/{id}/repuestos` | No | ADMIN/TECNICO/CONSULTA | ADMIN/TECNICO | — | ADMIN/TECNICO |
| Fotos mant. | `/api/mantenimientos/{id}/fotos` | No | ADMIN/TECNICO/CONSULTA | ADMIN/TECNICO | — | ADMIN/TECNICO |

Implicación para el front: el botón/acción de "Eliminar" en cualquier vista (clientes, equipos, mantenimientos) solo debería mostrarse/habilitarse si `rol === 'ADMIN'`. Los formularios de creación/edición solo para `ADMIN` o `TECNICO`. Vistas de solo lectura son accesibles también para `CONSULTA`.

---

## 5. Clientes

Base: `/api/clientes`

### Modelo (`ClienteDTO`, se usa igual para request y response)
```ts
{
  id: number;          // solo en response, no enviar en create
  nombre: string;       // único campo obligatorio a nivel de BD
  telefono: string | null;
  email: string | null;
  nitCc: string | null;
  notas: string | null;
}
```
⚠️ No hay validaciones server-side (ni `@NotBlank` ni formato de email). Toda la validación de formulario (requerido, formato email, etc.) debe implementarse en Angular con `Validators`, porque el backend no la va a rechazar con un 400 claro — si falta `nombre` puede resultar en un error 500 crudo (constraint de BD).

### Endpoints

**`GET /api/clientes`** — lista completa.
Respuesta `200`: `ClienteDTO[]`

**`GET /api/clientes/buscar?termino=xxx`** — búsqueda case-insensitive sobre `nombre`, `email` o `nitCc`. Query param `termino` obligatorio. `200`: `ClienteDTO[]`

**`GET /api/clientes/{id}`**
Respuesta `200`: `ClienteDTO`
Error `404`: cuerpo vacío.

**`POST /api/clientes`** (requiere ADMIN o TECNICO)
Body:
```json
{ "nombre": "Constructora XYZ", "telefono": "3001234567", "email": "contacto@xyz.com", "nitCc": "900123456-7", "notas": "Cliente frecuente" }
```
Respuesta `201 Created`: `ClienteDTO` (con `id` asignado).
Error `400`: cuerpo vacío.

**`PUT /api/clientes/{id}`** (requiere ADMIN o TECNICO)
Body: `ClienteDTO` parcial — el service solo aplica los campos que vengan no-nulos (se puede mandar solo el campo que cambia).
Respuesta `200`: `ClienteDTO` actualizado.
Error `404`: cuerpo vacío.

**`DELETE /api/clientes/{id}`** (requiere ADMIN)
Respuesta `204 No Content` (sin cuerpo).
Error `404`: cuerpo vacío.

---

## 6. Equipos

Base: `/api/equipos`

### Modelo (`EquipoDTO`, request y response comparten shape)
```ts
{
  id: number;                    // solo response
  codigo: string | null;
  descripcion: string | null;
  numeroSerie: string | null;
  modelo: string | null;
  marca: string | null;
  categoriaId: number;           // requerido en creación (FK válida)
  categoriaNombre: string | null;   // solo lectura, lo llena el backend
  tipoMotorId: number | null;
  tipoMotorNombre: string | null;   // solo lectura
  esPropio: boolean;
  clienteId: number | null;      // requerido si esPropio = false (equipo externo)
  clienteNombre: string | null;     // solo lectura
  ubicacion: string | null;
  estadoId: number;              // requerido en creación (FK válida)
  estadoNombre: string | null;      // solo lectura
  checkMtto: boolean | null;
  checkHv: boolean | null;
  checkFt: boolean | null;
  accesorios: string | null;
  registradoPorId: number | null;    // actualmente el backend nunca lo llena, siempre null
  registradoPorNombre: string | null; // idem, siempre null
  fechaRegistro: string | null;      // "yyyy-MM-dd", lo pone el backend al crear (hoy), no enviar
  fechaIngresoTaller: string | null; // idem, solo se llena si esPropio=false y hay clienteId
  creadoEn: string | null;          // ISO datetime, solo lectura
  actualizadoEn: string | null;     // ISO datetime, solo lectura
}
```

`categoriaId` y `estadoId` deben ser IDs válidos existentes (ver sección 8, catálogos). Si no son válidos, el create devuelve `400` con **cuerpo vacío** (no dice cuál campo falló).

### Endpoints

**`GET /api/equipos`** — todos los equipos. `200`: `EquipoDTO[]`

**`GET /api/equipos/propios`** — solo donde `esPropio = true`. `200`: `EquipoDTO[]`

**`GET /api/equipos/externos`** — solo donde `esPropio = false`. `200`: `EquipoDTO[]`

**`GET /api/equipos/buscar?termino=xxx`** — búsqueda case-insensitive sobre `descripcion`, `numeroSerie` o `codigo`. Query param `termino` obligatorio. `200`: `EquipoDTO[]`

**`GET /api/equipos/{id}`** — `200`: `EquipoDTO`. Error `404`: cuerpo vacío.

**`POST /api/equipos`** (ADMIN/TECNICO)
Body ejemplo:
```json
{
  "codigo": "EQ-001",
  "descripcion": "Compresor industrial",
  "numeroSerie": "SN123456",
  "modelo": "X200",
  "marca": "Bosch",
  "categoriaId": 1,
  "tipoMotorId": 3,
  "esPropio": true,
  "clienteId": null,
  "ubicacion": "Bodega principal",
  "estadoId": 1,
  "checkMtto": false,
  "checkHv": false,
  "checkFt": false,
  "accesorios": "Manguera, filtro"
}
```
Respuesta `201`: `EquipoDTO` completo (con `id`, `categoriaNombre`, `estadoNombre`, `fechaRegistro`, etc. ya calculados).
Error `400`: cuerpo vacío (IDs de categoría/estado inválidos, o error de datos).

**`PUT /api/equipos/{id}`** (ADMIN/TECNICO)
⚠️ **Importante:** aunque se mande el `EquipoDTO` completo, el backend **solo persiste realmente `descripcion`, `ubicacion` y `estadoId`**. Cualquier otro campo (`codigo`, `marca`, `modelo`, `categoriaId`, `checkMtto/Hv/Ft`, `accesorios`, etc.) se ignora silenciosamente aunque se envíe. Si la pantalla de "Editar equipo" necesita cambiar otros campos, hoy no tiene efecto — hay que avisar al equipo de backend antes de construir esa UI con expectativas distintas.

Body mínimo útil:
```json
{ "descripcion": "Compresor industrial (revisado)", "ubicacion": "Bodega 2", "estadoId": 2 }
```
Respuesta `200`: `EquipoDTO` actualizado. Error `404`: cuerpo vacío.

**`DELETE /api/equipos/{id}`** (ADMIN) — `204`. Error `404`: cuerpo vacío.

---

## 7. Mantenimientos

Base: `/api/mantenimientos`

### Modelo (`MantenimientoDTO`)
```ts
{
  id: number;                       // solo response
  equipoId: number;                 // requerido en creación
  equipoCodigo: string | null;         // solo lectura
  equipoDescripcion: string | null;    // solo lectura
  tipo: "PREVENTIVO" | "CORRECTIVO" | "PREDICTIVO"; // exacto, mayúsculas — ver nota abajo
  descripcionTrabajo: string;        // requerido (NOT NULL en BD)
  diagnostico: string | null;
  observaciones: string | null;
  fechaApertura: string | null;      // "yyyy-MM-dd"
  fechaDiagnostico: string | null;
  fechaInicio: string | null;
  fechaCierre: string | null;
  estadoId: number;                  // requerido, FK a cat_estado_mant
  estadoNombre: string | null;          // solo lectura
  tecnicoId: number | null;
  tecnicoNombre: string | null;         // solo lectura
  costoManoObra: number | null;      // BigDecimal
  costoRepuestos: number | null;     // BigDecimal
  costoTotal: number | null;         // solo lectura, = costoManoObra + costoRepuestos, lo calcula el backend
  rotulado: boolean | null;
}
```

⚠️ **`tipo` es sensible a mayúsculas y debe ser exactamente uno de** `"PREVENTIVO"`, `"CORRECTIVO"`, `"PREDICTIVO"`. Si el `<select>` del front manda cualquier otro valor (ej. `"Preventivo"` con minúscula), el backend lanza una excepción y responde `400` con **cuerpo vacío**. Usar un `<select>` con esos 3 valores exactos, no texto libre.

### Endpoints

**`GET /api/mantenimientos`** — todos. `200`: `MantenimientoDTO[]`

**`GET /api/mantenimientos/{id}`** — `200`: `MantenimientoDTO`. Error `404`: cuerpo vacío.

**`GET /api/mantenimientos/equipo/{equipoId}`** — historial de mantenimiento de un equipo, ordenado por `fechaApertura` descendente. `200`: `MantenimientoDTO[]` (si el equipo no existe, devuelve `[]`, no 404).

**`GET /api/mantenimientos/rango?desde=yyyy-MM-dd&hasta=yyyy-MM-dd`** — ambos query params obligatorios, formato ISO. `200`: `MantenimientoDTO[]` ordenado por `fechaApertura` descendente, rango inclusivo.

**`POST /api/mantenimientos`** (ADMIN/TECNICO)
Body ejemplo:
```json
{
  "equipoId": 5,
  "tipo": "PREVENTIVO",
  "descripcionTrabajo": "Cambio de aceite y filtros",
  "diagnostico": null,
  "observaciones": "Revisión de rutina",
  "fechaApertura": "2026-07-05",
  "estadoId": 1,
  "tecnicoId": 2,
  "costoManoObra": 50000,
  "costoRepuestos": 30000,
  "rotulado": false
}
```
Respuesta `201`: `MantenimientoDTO` con `costoTotal` calculado y nombres resueltos.
Error `400`: cuerpo vacío (ej. `tipo` inválido, `equipoId`/`estadoId` inexistente).

**`PUT /api/mantenimientos/{id}`** (ADMIN/TECNICO)
⚠️ Igual que Equipos: **solo se persisten realmente** `diagnostico`, `observaciones`, `fechaCierre`, `costoManoObra`, `costoRepuestos`, `estadoId`. Los campos `tipo`, `tecnicoId`, `fechaApertura`, `fechaDiagnostico`, `fechaInicio`, `descripcionTrabajo`, `rotulado` se ignoran si se mandan en un update, aunque estén en el DTO.

Body típico de "cerrar mantenimiento":
```json
{ "diagnostico": "Correa desgastada", "observaciones": "Se reemplazó", "fechaCierre": "2026-07-10", "costoManoObra": 60000, "costoRepuestos": 45000, "estadoId": 4 }
```
Respuesta `200`: `MantenimientoDTO` actualizado. Error `404`: cuerpo vacío.

**`DELETE /api/mantenimientos/{id}`** (ADMIN) — `204`. Error `404`: cuerpo vacío.

### Repuestos de un mantenimiento

Modelo (`MantenimientoRepuestoDTO`):
```ts
{
  id: number;              // solo response
  mantenimientoId: number; // solo response
  descripcion: string;
  referencia: string | null;
  cantidad: number;        // BigDecimal, default 1
  costoUnitario: number;   // BigDecimal, default 0
  costoTotal: number;      // solo lectura = cantidad * costoUnitario
}
```

**`GET /api/mantenimientos/{mantenimientoId}/repuestos`** (ADMIN/TECNICO/CONSULTA) — `200`: `MantenimientoRepuestoDTO[]`

**`POST /api/mantenimientos/{mantenimientoId}/repuestos`** (ADMIN/TECNICO) — Body: `{ descripcion, referencia, cantidad, costoUnitario }`. Respuesta `201`: `MantenimientoRepuestoDTO`. Error `400`: cuerpo vacío (ej. `mantenimientoId` inexistente).

**`DELETE /api/mantenimientos/{mantenimientoId}/repuestos/{repuestoId}`** (ADMIN/TECNICO — no requiere ADMIN a diferencia del resto de deletes, porque es un ítem de línea, no el registro completo) — `204`. Error `404`: cuerpo vacío (incluye el caso en que el repuesto exista pero pertenezca a otro mantenimiento).

### Fotos de un mantenimiento

Modelo (`MantenimientoFotoDTO`):
```ts
{
  id: number;
  mantenimientoId: number;
  momento: "ANTES" | "DURANTE" | "DESPUES"; // default ANTES si no se envía
  descripcion: string | null;
  subidaEn: string;   // ISO datetime, solo lectura
  url: string;        // ruta relativa para el <img>, ej. "/mantenimientos/5/fotos/12/archivo"
}
```
⚠️ `url` ya viene con el prefijo `/mantenimientos/...` (relativo a la raíz de la API, es decir, al `context-path /api`), así que el front debe concatenar `environment.apiBaseUrl + foto.url` para armar la URL completa de la imagen — **no** volver a anteponerle `/api` a mano.

**`GET /api/mantenimientos/{mantenimientoId}/fotos`** (ADMIN/TECNICO/CONSULTA) — `200`: `MantenimientoFotoDTO[]`

**`POST /api/mantenimientos/{mantenimientoId}/fotos`** (ADMIN/TECNICO) — `multipart/form-data` con campos: `archivo` (File, requerido), `momento` (opcional: `ANTES`|`DURANTE`|`DESPUES`), `descripcion` (opcional). Respuesta `201`: `MantenimientoFotoDTO`. Error `400`: cuerpo vacío si el archivo no es una imagen JPEG/PNG/WEBP/GIF, si está vacío, o si `mantenimientoId` no existe.
- Límite de tamaño: `10MB` (`spring.servlet.multipart.max-file-size`). Un archivo más grande devuelve un error 500 (excepción de Spring, no manejada explícitamente).

**`GET /api/mantenimientos/{mantenimientoId}/fotos/{fotoId}/archivo`** (ADMIN/TECNICO/CONSULTA) — devuelve los bytes de la imagen con el `Content-Type` correcto (`image/jpeg`, `image/png`, etc.). Esta es la URL a usar en `<img [src]>` (con el token ya adjunto por el interceptor, igual que cualquier otro request autenticado).

**`DELETE /api/mantenimientos/{mantenimientoId}/fotos/{fotoId}`** (ADMIN/TECNICO) — borra el registro y el archivo en disco. `204`. Error `404`: cuerpo vacío.

Nota de implementación: los archivos se guardan en el filesystem del servidor bajo `app.uploads.dir` (por defecto `uploads/mantenimientos/`, configurable con la env var `UPLOADS_DIR`), no en la base de datos ni en un bucket externo. En un despliegue con múltiples instancias sin disco compartido esto no funcionaría — server con estado local.

---

## 8. Catálogos / listas para dropdowns (categorías, estados, tipo de motor)

⚠️ **Ninguno de estos catálogos tiene endpoint REST propio hoy.** No existe `GET /api/categorias`, ni `/api/estados-equipo`, ni `/api/tipos-motor`, ni `/api/estados-mantenimiento`. Son tablas de referencia usadas solo como FK. Mientras el backend no exponga controllers para esto, el front tiene dos opciones:

1. **Hardcodear** los IDs y nombres siguientes en el front (como constantes/enum de Angular), basado en el seed real de la BD (`V1__schema_inicial.sql`).
2. Pedir al equipo de backend que agregue endpoints `GET` simples para estas 4 tablas (recomendado si los valores pueden cambiar).

### Categoría de equipo (`cat_categoria`)
| id | nombre |
|---|---|
| 1 | Equipo Menor |
| 2 | Equipo Mayor |
| 3 | Vehículo |
| 4 | Tráiler |
| 5 | Maquinaria |

### Estado de equipo (`cat_estado_equipo`)
| id | nombre |
|---|---|
| 1 | Operativo |
| 2 | En mantenimiento |
| 3 | Pendiente por diagnóstico |
| 4 | Por iniciar servicio |
| 5 | Reparado - por retirar |
| 6 | Sin cancelar - reparado |
| 7 | Por aprobar |
| 8 | Stand by |
| 9 | Por definir |
| 10 | Dado de baja |

### Tipo de motor (`cat_tipo_motor`)
| id | nombre |
|---|---|
| 1 | Gasolina 2T |
| 2 | Gasolina 4T |
| 3 | Diesel |
| 4 | Eléctrico 110v |
| 5 | Eléctrico 220v |
| 6 | Hidráulico |
| 7 | Sin motor |

### Estado de mantenimiento (`cat_estado_mant`)
| id | nombre |
|---|---|
| 1 | Abierta |
| 2 | En proceso |
| 3 | Pendiente repuesto |
| 4 | Finalizada |
| 5 | Cancelada |

> ⚠️ Confirmar estos IDs contra la BD real antes de hardcodear en producción (revisar `src/main/resources/db/migration/V1__schema_inicial.sql`), por si hay migraciones posteriores que los alteren.

---

## 9. Qué NO existe todavía (no construir pantallas que dependan de esto sin avisar antes)

- **Gestión de usuarios** (crear con rol arbitrario, editar, desactivar): solo hay lectura (`GET /api/usuarios`) y creación implícita vía `/auth/register` (siempre rol `TECNICO`). También existen dos endpoints de admin de bajo nivel (`/api/admin/init-admin`, `/api/admin/create-user`) que **no tienen restricción de rol propia** (cualquier usuario autenticado los puede llamar) — no construir UI pública sobre esto sin corregir el backend primero.
- **CRUD de catálogos** (categorías, estados, tipo de motor): no editable desde API, ver sección 8.
- **Refresh token**: no existe, al expirar el JWT (24h) el usuario debe volver a loguearse.
- **Paginación**: no existe en ningún listado.
- **Búsqueda/filtros avanzados**: existe `GET /api/equipos/buscar?termino=` y `GET /api/clientes/buscar?termino=`. Mantenimientos no tiene búsqueda por texto libre, pero sí filtro por rango de fechas y por equipo.
- **Almacenamiento de fotos en la nube**: las fotos de mantenimiento se guardan en disco local del servidor (ver sección 7), no en S3/GCS/etc.

---

## 10. Resumen rápido de endpoints

| Método | Ruta | Auth | Body request | Body response éxito |
|---|---|---|---|---|
| POST | `/api/auth/login` | público | `{nombre, password}` | `{token, tipo, id, nombre, rol}` |
| POST | `/api/auth/register` | público | `{nombre, password}` | string `"Usuario creado exitosamente"` |
| POST | `/api/auth/change-password` | JWT (cualquier rol) | `{currentPassword, newPassword}` | `{message}` |
| GET | `/api/usuarios?rol=` | ADMIN/TECNICO/CONSULTA | — | `UsuarioDTO[]` |
| GET | `/api/clientes` | ADMIN/TECNICO/CONSULTA | — | `ClienteDTO[]` |
| GET | `/api/clientes/buscar?termino=` | ADMIN/TECNICO/CONSULTA | — | `ClienteDTO[]` |
| GET | `/api/clientes/{id}` | ADMIN/TECNICO/CONSULTA | — | `ClienteDTO` |
| POST | `/api/clientes` | ADMIN/TECNICO | `ClienteDTO` | `ClienteDTO` (201) |
| PUT | `/api/clientes/{id}` | ADMIN/TECNICO | `ClienteDTO` parcial | `ClienteDTO` |
| DELETE | `/api/clientes/{id}` | ADMIN | — | 204 |
| GET | `/api/equipos` | ADMIN/TECNICO/CONSULTA | — | `EquipoDTO[]` |
| GET | `/api/equipos/propios` | ADMIN/TECNICO/CONSULTA | — | `EquipoDTO[]` |
| GET | `/api/equipos/externos` | ADMIN/TECNICO/CONSULTA | — | `EquipoDTO[]` |
| GET | `/api/equipos/buscar?termino=` | ADMIN/TECNICO/CONSULTA | — | `EquipoDTO[]` |
| GET | `/api/equipos/{id}` | ADMIN/TECNICO/CONSULTA | — | `EquipoDTO` |
| POST | `/api/equipos` | ADMIN/TECNICO | `EquipoDTO` | `EquipoDTO` (201) |
| PUT | `/api/equipos/{id}` | ADMIN/TECNICO | `EquipoDTO` (solo 3 campos aplican) | `EquipoDTO` |
| DELETE | `/api/equipos/{id}` | ADMIN | — | 204 |
| GET | `/api/mantenimientos` | ADMIN/TECNICO/CONSULTA | — | `MantenimientoDTO[]` |
| GET | `/api/mantenimientos/{id}` | ADMIN/TECNICO/CONSULTA | — | `MantenimientoDTO` |
| GET | `/api/mantenimientos/equipo/{equipoId}` | ADMIN/TECNICO/CONSULTA | — | `MantenimientoDTO[]` |
| GET | `/api/mantenimientos/rango?desde=&hasta=` | ADMIN/TECNICO/CONSULTA | — | `MantenimientoDTO[]` |
| POST | `/api/mantenimientos` | ADMIN/TECNICO | `MantenimientoDTO` | `MantenimientoDTO` (201) |
| PUT | `/api/mantenimientos/{id}` | ADMIN/TECNICO | `MantenimientoDTO` (solo 6 campos aplican) | `MantenimientoDTO` |
| DELETE | `/api/mantenimientos/{id}` | ADMIN | — | 204 |
| GET | `/api/mantenimientos/{id}/repuestos` | ADMIN/TECNICO/CONSULTA | — | `MantenimientoRepuestoDTO[]` |
| POST | `/api/mantenimientos/{id}/repuestos` | ADMIN/TECNICO | `MantenimientoRepuestoDTO` | `MantenimientoRepuestoDTO` (201) |
| DELETE | `/api/mantenimientos/{id}/repuestos/{repuestoId}` | ADMIN/TECNICO | — | 204 |
| GET | `/api/mantenimientos/{id}/fotos` | ADMIN/TECNICO/CONSULTA | — | `MantenimientoFotoDTO[]` |
| POST | `/api/mantenimientos/{id}/fotos` | ADMIN/TECNICO | multipart (`archivo`, `momento?`, `descripcion?`) | `MantenimientoFotoDTO` (201) |
| GET | `/api/mantenimientos/{id}/fotos/{fotoId}/archivo` | ADMIN/TECNICO/CONSULTA | — | bytes de imagen |
| DELETE | `/api/mantenimientos/{id}/fotos/{fotoId}` | ADMIN/TECNICO | — | 204 |

---

## 11. Notas para construir el cliente Angular 17

- Usar `HttpClient` con un `HttpInterceptorFn` (Angular 17 funcional interceptors) para:
  - Adjuntar `Authorization: Bearer <token>` desde un `AuthService` (señal/BehaviorSubject con el token guardado tras login).
  - Capturar 401/403 → limpiar sesión y redirigir a `/login`.
  - Normalizar errores según la sección 3 (string plano vs objeto vs vacío) antes de mostrarlos en UI.
- Los guards de ruta (`CanActivateFn`) deben chequear tanto "¿hay token?" como "¿el rol alcanza para esta ruta?" usando el `rol` devuelto en el login (guardarlo junto al token).
- Para los `<select>` de categoría/estado/tipo de motor/estado de mantenimiento, usar los IDs de la sección 8 como constantes tipadas (`interface CatalogoItem { id: number; nombre: string }`) hasta que el backend exponga endpoints reales.
- Para el formulario de "Editar equipo" y "Editar/cerrar mantenimiento", considerar deshabilitar o dejar en solo-lectura los campos que el backend ignora en el `PUT` (ver secciones 6 y 7), para no confundir al usuario con cambios que no se van a guardar.
