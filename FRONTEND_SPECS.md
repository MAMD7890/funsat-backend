# Especificación del Frontend - Sistema de Inventario de Equipos

**Frontend:** Angular 17  
**Backend:** Spring Boot  
**Autenticación:** JWT (JSON Web Token)

---

## 📑 Tabla de Contenidos

1. [Estructura del Proyecto Angular](#estructura-del-proyecto-angular)
2. [Sistema de Autenticación](#sistema-de-autenticación)
3. [Vistas Requeridas](#vistas-requeridas)
4. [Rutas (Routing)](#rutas-routing)
5. [Endpoints y JSON de APIs](#endpoints-y-json-de-apis)
6. [Servicios Angular](#servicios-angular)
7. [Guardias de Autenticación](#guardias-de-autenticación)
8. [Interceptores](#interceptores)

---

## 🏗️ Estructura del Proyecto Angular

```
src/
├── app/
│   ├── core/
│   │   ├── guards/
│   │   │   └── auth.guard.ts
│   │   ├── interceptors/
│   │   │   └── jwt.interceptor.ts
│   │   └── services/
│   │       ├── auth.service.ts
│   │       ├── cliente.service.ts
│   │       ├── equipo.service.ts
│   │       └── mantenimiento.service.ts
│   ├── shared/
│   │   ├── components/
│   │   │   ├── header/
│   │   │   ├── sidebar/
│   │   │   └── footer/
│   │   └── models/
│   │       ├── auth.model.ts
│   │       ├── cliente.model.ts
│   │       ├── equipo.model.ts
│   │       └── mantenimiento.model.ts
│   ├── features/
│   │   ├── auth/
│   │   │   ├── components/
│   │   │   │   ├── login/
│   │   │   │   └── register/
│   │   │   └── auth.module.ts
│   │   ├── dashboard/
│   │   │   ├── components/
│   │   │   │   └── dashboard/
│   │   │   └── dashboard.module.ts
│   │   ├── clientes/
│   │   │   ├── components/
│   │   │   │   ├── listado/
│   │   │   │   ├── detalle/
│   │   │   │   ├── crear-editar/
│   │   │   │   └── eliminar/
│   │   │   └── clientes.module.ts
│   │   ├── equipos/
│   │   │   ├── components/
│   │   │   │   ├── listado/
│   │   │   │   ├── detalle/
│   │   │   │   ├── crear-editar/
│   │   │   │   ├── filtros/
│   │   │   │   └── eliminar/
│   │   │   └── equipos.module.ts
│   │   └── mantenimientos/
│   │       ├── components/
│   │       │   ├── listado/
│   │       │   ├── detalle/
│   │       │   ├── crear-editar/
│   │       │   ├── historial/
│   │       │   ├── fotos/
│   │       │   └── eliminar/
│   │       └── mantenimientos.module.ts
│   ├── app-routing.module.ts
│   └── app.module.ts
├── assets/
├── environments/
└── styles/
```

---

## 🔐 Sistema de Autenticación

### Flujo de Autenticación

```
1. Usuario ingresa credenciales en Login
2. Frontend envía POST /auth/login
3. Backend retorna JWT token
4. Frontend almacena token en localStorage
5. Token se envía en cada petición en header Authorization
6. Si token expira, usuario es redirigido a login
```

### Almacenamiento del Token

```typescript
// localStorage
localStorage.setItem('token', 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...');
localStorage.setItem('user', JSON.stringify({
  id: 1,
  nombre: 'usuario',
  rol: 'ADMIN'
}));
```

---

## 🎨 Vistas Requeridas

### 1. **Vista de Login (Pública)**
**Ruta:** `/login`  
**Autenticación:** No requerida  
**Descripción:** Formulario para que el usuario ingrese sus credenciales

**Elementos:**
- Campo de entrada: Nombre de usuario
- Campo de entrada: Contraseña
- Botón: Iniciar Sesión
- Botón: Registrarse (link a registro)
- Validaciones de formulario en tiempo real

---

### 2. **Vista de Registro (Pública)**
**Ruta:** `/register`  
**Autenticación:** No requerida  
**Descripción:** Formulario para registrar un nuevo usuario

**Elementos:**
- Campo de entrada: Nombre de usuario
- Campo de entrada: Contraseña
- Campo de entrada: Confirmar Contraseña
- Validaciones:
  - Nombre único
  - Contraseña mínimo 8 caracteres
  - Las contraseñas deben coincidir
- Botón: Registrar
- Link: Volver a Login

---

### 3. **Vista de Dashboard (Protegida)**
**Ruta:** `/dashboard`  
**Autenticación:** Requerida  
**Roles:** ADMIN, TECNICO, CONSULTA  
**Descripción:** Panel principal con estadísticas e información general

**Elementos:**
- Bienvenida con nombre del usuario
- Tarjetas con estadísticas:
  - Total de equipos
  - Equipos propios
  - Equipos externos
  - Mantenimientos activos
  - Mantenimientos completados
- Gráfico de equipos por categoría (opcional)
- Gráfico de mantenimientos por estado (opcional)
- Última actividad reciente
- Botones de acceso rápido a módulos principales

---

### 4. **Vista Listado de Clientes (Protegida)**
**Ruta:** `/clientes`  
**Autenticación:** Requerida  
**Roles:** ADMIN, TECNICO, CONSULTA  
**Descripción:** Tabla con todos los clientes registrados

**Elementos:**
- Tabla con columnas:
  - ID
  - Nombre
  - Teléfono
  - Email
  - NIT/CC
  - Acciones (ver, editar, eliminar)
- Búsqueda por nombre, email, teléfono
- Paginación
- Botón: Crear nuevo cliente (ADMIN, TECNICO)
- Botón: Actualizar lista
- Opciones de filtro/ordenamiento

---

### 5. **Vista Detalle de Cliente (Protegida)**
**Ruta:** `/clientes/:id`  
**Autenticación:** Requerida  
**Roles:** ADMIN, TECNICO, CONSULTA  
**Descripción:** Vista detallada de un cliente

**Elementos:**
- Información del cliente:
  - Nombre
  - Teléfono
  - Email
  - NIT/CC
  - Notas
- Equipos asociados al cliente (lista)
- Mantenimientos realizados (lista)
- Botones: Editar (ADMIN, TECNICO), Volver atrás
- Botón: Eliminar (solo ADMIN)

---

### 6. **Vista Crear/Editar Cliente (Protegida)**
**Ruta:** `/clientes/crear` o `/clientes/:id/editar`  
**Autenticación:** Requerida  
**Roles:** ADMIN, TECNICO  
**Descripción:** Formulario para crear o editar un cliente

**Elementos:**
- Formulario con campos:
  - Nombre (requerido)
  - Teléfono
  - Email (validación de email)
  - NIT/CC (requerido)
  - Notas (textarea)
- Validaciones en tiempo real
- Botones: Guardar, Cancelar
- Mensaje de confirmación tras guardar

---

### 7. **Vista Listado de Equipos (Protegida)**
**Ruta:** `/equipos`  
**Autenticación:** Requerida  
**Roles:** ADMIN, TECNICO, CONSULTA  
**Descripción:** Tabla con todos los equipos

**Elementos:**
- Tabla con columnas:
  - Código
  - Descripción
  - Marca
  - Modelo
  - Categoría
  - Cliente (si es externo)
  - Estado
  - Acciones
- Filtros por:
  - Tipo (propios/externos)
  - Categoría
  - Estado
  - Cliente (solo si externo)
- Búsqueda por código, descripción, serie, modelo
- Paginación
- Botón: Crear equipo (ADMIN, TECNICO)
- Botón: Exportar a Excel (opcional)
- Vista de cards alternativa (opcional)

---

### 8. **Vista Detalle de Equipo (Protegida)**
**Ruta:** `/equipos/:id`  
**Autenticación:** Requerida  
**Roles:** ADMIN, TECNICO, CONSULTA  
**Descripción:** Vista detallada completa de un equipo

**Elementos:**
- Información del equipo:
  - Código, Descripción, Número de serie
  - Marca, Modelo, Categoría, Tipo de motor
  - Cliente (si es externo), Ubicación
  - Estado actual
  - Checks: MTT, HV, FT
  - Accesorios
  - Registrado por, Fecha de registro
  - Fecha de ingreso al taller
  - Fechas de creación/actualización
- Historial de mantenimientos (tabla/lista)
- Acciones:
  - Editar (ADMIN, TECNICO)
  - Eliminar (ADMIN)
  - Crear mantenimiento
  - Volver atrás

---

### 9. **Vista Crear/Editar Equipo (Protegida)**
**Ruta:** `/equipos/crear` o `/equipos/:id/editar`  
**Autenticación:** Requerida  
**Roles:** ADMIN, TECNICO  
**Descripción:** Formulario para crear o editar equipo

**Elementos (Sección 1 - Información Básica):**
- Código (requerido)
- Descripción (requerido)
- Número de serie
- Marca (requerido)
- Modelo (requerido)

**Elementos (Sección 2 - Categorización):**
- Categoría (dropdown, requerido)
- Tipo de motor (dropdown)
- Es propio (checkbox)
- Cliente (si es externo, dropdown de clientes)

**Elementos (Sección 3 - Ubicación y Estado):**
- Ubicación
- Estado (dropdown)
- Fecha de ingreso al taller

**Elementos (Sección 4 - Checks y Accesorios):**
- Check MTT (checkbox)
- Check HV (checkbox)
- Check FT (checkbox)
- Accesorios (textarea)

**Elementos (Sección 5 - Datos Generales):**
- Registrado por (readonly, usuario actual)
- Fecha de registro (readonly, fecha actual)

**Validaciones:**
- Campos requeridos
- Formato de datos
- Validación de categoría y tipo de motor

**Botones:** Guardar, Cancelar

---

### 10. **Vista Listado de Mantenimientos (Protegida)**
**Ruta:** `/mantenimientos`  
**Autenticación:** Requerida  
**Roles:** ADMIN, TECNICO, CONSULTA  
**Descripción:** Tabla con todos los mantenimientos

**Elementos:**
- Tabla con columnas:
  - ID, Código del equipo, Descripción del equipo
  - Tipo de mantenimiento
  - Estado
  - Técnico asignado
  - Fecha de apertura, Fecha de cierre
  - Costo total
  - Acciones
- Filtros por:
  - Estado (abierto, en proceso, completado, etc.)
  - Técnico asignado
  - Rango de fechas
  - Equipo
- Búsqueda por código de equipo, descripción
- Paginación
- Botón: Crear mantenimiento (ADMIN, TECNICO)
- Botón: Exportar a Excel (opcional)
- Indicador visual de estado (colores)

---

### 11. **Vista Detalle de Mantenimiento (Protegida)**
**Ruta:** `/mantenimientos/:id`  
**Autenticación:** Requerida  
**Roles:** ADMIN, TECNICO, CONSULTA  
**Descripción:** Vista detallada de un mantenimiento

**Elementos (Información General):**
- ID, Código del equipo, Descripción del equipo
- Tipo de mantenimiento
- Descripción del trabajo
- Diagnóstico
- Observaciones
- ¿Rotulado? (sí/no)

**Elementos (Fechas):**
- Fecha de apertura
- Fecha de diagnóstico
- Fecha de inicio
- Fecha de cierre

**Elementos (Personal y Estado):**
- Técnico asignado
- Estado actual

**Elementos (Costos):**
- Costo de mano de obra
- Costo de repuestos
- Costo total (cálculo automático)

**Elementos (Repuestos Utilizados):**
- Tabla con:
  - Descripción
  - Referencia
  - Cantidad
  - Costo unitario
  - Costo total
- Botón: Agregar repuesto (TECNICO, ADMIN)
- Botón: Editar/Eliminar repuesto (TECNICO, ADMIN)

**Elementos (Fotos/Evidencia):**
- Galería de fotos del mantenimiento
- Botón: Subir foto (TECNICO, ADMIN)
- Botón: Eliminar foto (TECNICO, ADMIN)

**Acciones:**
- Editar (ADMIN, TECNICO)
- Eliminar (ADMIN)
- Generar reporte (ADMIN, TECNICO, CONSULTA)
- Volver atrás

---

### 12. **Vista Crear/Editar Mantenimiento (Protegida)**
**Ruta:** `/mantenimientos/crear` o `/mantenimientos/:id/editar`  
**Autenticación:** Requerida  
**Roles:** ADMIN, TECNICO  
**Descripción:** Formulario para crear o editar mantenimiento

**Elementos (Sección 1 - Información del Equipo):**
- Seleccionar equipo (dropdown/búsqueda)
- Código del equipo (readonly)
- Descripción del equipo (readonly)

**Elementos (Sección 2 - Tipo y Descripción):**
- Tipo de mantenimiento (dropdown)
- Descripción del trabajo (textarea, requerido)
- Diagnóstico (textarea)
- Observaciones (textarea)

**Elementos (Sección 3 - Fechas y Personal):**
- Fecha de apertura (date picker, requerido)
- Fecha de diagnóstico (date picker)
- Fecha de inicio (date picker)
- Fecha de cierre (date picker)
- Técnico asignado (dropdown, requerido)
- Estado (dropdown)

**Elementos (Sección 4 - Costos):**
- Costo de mano de obra (número)
- Costo de repuestos (número, calculado)
- Costo total (readonly, cálculo automático)
- ¿Rotulado? (checkbox)

**Elementos (Sección 5 - Repuestos):**
- Tabla/Lista de repuestos utilizados
- Opción para agregar repuestos
- Campos para cada repuesto:
  - Descripción
  - Referencia
  - Cantidad
  - Costo unitario
  - Costo total (auto-calculado)

**Validaciones:**
- Campos requeridos
- Fechas válidas (no futuras en apertura)
- Costos numéricos positivos
- Costo total = mano de obra + repuestos

**Botones:** Guardar, Cancelar

---

### 13. **Vista Historial de Mantenimientos (Protegida)**
**Ruta:** `/equipos/:id/mantenimientos` o `/mantenimientos/historial/:equipoId`  
**Autenticación:** Requerida  
**Roles:** ADMIN, TECNICO, CONSULTA  
**Descripción:** Historial de todos los mantenimientos de un equipo

**Elementos:**
- Información del equipo en encabezado
- Timeline/Tabla cronológica de mantenimientos:
  - Fecha (abierto, diagnóstico, inicio, cierre)
  - Tipo
  - Estado
  - Técnico
  - Descripción corta
  - Costo total
- Acciones: Ver detalle, Editar (TECNICO, ADMIN)
- Estadísticas:
  - Total de mantenimientos
  - Últimas 3 semanas
  - Costo total de mantenimientos
- Filtros por rango de fechas

---

### 14. **Vista de Fotos de Mantenimiento (Protegida)**
**Ruta:** `/mantenimientos/:id/fotos`  
**Autenticación:** Requerida  
**Roles:** ADMIN, TECNICO, CONSULTA  
**Descripción:** Galería de fotos de un mantenimiento

**Elementos:**
- Galería de imágenes (grid)
- Cada imagen con:
  - Thumbnail
  - Fecha de carga
  - Botón: Ver en grande (modal/lightbox)
  - Botón: Descargar (ADMIN, TECNICO, CONSULTA)
  - Botón: Eliminar (ADMIN, TECNICO)
- Botón: Subir nueva foto (ADMIN, TECNICO)
- Drag & drop para subir fotos
- Validación: Solo jpg, png, max 5MB
- Mensaje si no hay fotos

---

### 15. **Vista de Búsqueda Avanzada (Protegida)**
**Ruta:** `/busqueda`  
**Autenticación:** Requerida  
**Roles:** ADMIN, TECNICO, CONSULTA  
**Descripción:** Búsqueda global en clientes, equipos y mantenimientos

**Elementos:**
- Campo de búsqueda principal
- Filtros:
  - Tipo de objeto (cliente, equipo, mantenimiento)
  - Rango de fechas
  - Estado (para equipos/mantenimientos)
- Resultados en tabs:
  - Clientes encontrados
  - Equipos encontrados
  - Mantenimientos encontrados
- Cada resultado es clickeable y lleva a su vista detalle

---

### 16. **Vista de Reportes (Protegida)**
**Ruta:** `/reportes`  
**Autenticación:** Requerida  
**Roles:** ADMIN, TECNICO  
**Descripción:** Generación de reportes varios

**Opciones de Reporte:**
1. **Reporte de Equipos:**
   - Filtros: Estado, Categoría, Tipo (propio/externo), Cliente
   - Exportar a PDF/Excel
   - Columnas: Código, Descripción, Marca, Estado, Cliente, Fecha

2. **Reporte de Mantenimientos:**
   - Filtros: Estado, Técnico, Rango de fechas, Equipo
   - Exportar a PDF/Excel
   - Columnas: ID, Equipo, Tipo, Estado, Técnico, Fechas, Costo

3. **Reporte de Clientes:**
   - Filtros: Nada o texto libre
   - Exportar a PDF/Excel
   - Columnas: Nombre, Teléfono, Email, NIT, Equipos

4. **Reporte de Costos:**
   - Rango de fechas
   - Filtros: Por técnico, por equipo
   - Gráficos: Costos por mes, por técnico, por tipo

---

### 17. **Vista de Perfil de Usuario (Protegida)**
**Ruta:** `/perfil`  
**Autenticación:** Requerida  
**Roles:** ADMIN, TECNICO, CONSULTA  
**Descripción:** Perfil del usuario logueado

**Elementos:**
- Información del usuario:
  - Nombre de usuario (readonly)
  - Rol (readonly)
  - Fecha de creación de cuenta
- Botón: Cambiar contraseña
- Botón: Cerrar sesión
- Información adicional (opcional):
  - Últimas actividades
  - Equipos trabajados (solo TECNICO)
  - Mantenimientos realizados (solo TECNICO)

---

### 18. **Vista de Administración (Protegida)**
**Ruta:** `/admin`  
**Autenticación:** Requerida  
**Roles:** ADMIN  
**Descripción:** Panel de administración

**Secciones:**
1. **Gestión de Usuarios:**
   - Tabla de usuarios
   - Roles: ADMIN, TECNICO, CONSULTA
   - Estados: Activo, Inactivo
   - Acciones: Editar, Desactivar/Activar, Eliminar

2. **Gestión de Categorías de Equipos:**
   - Tabla de categorías
   - Acciones: Crear, Editar, Eliminar

3. **Gestión de Tipos de Motor:**
   - Tabla de tipos de motor
   - Acciones: Crear, Editar, Eliminar

4. **Gestión de Estados:**
   - Estados de equipos
   - Estados de mantenimientos
   - No permitir edición (datos del sistema)

5. **Auditoría/Logs (opcional):**
   - Historial de cambios
   - Filtros: Usuario, Fecha, Tipo de cambio
   - Ver detalle de qué cambió

---

## 🛣️ Rutas (Routing)

```typescript
const routes: Routes = [
  {
    path: '',
    redirectTo: '/dashboard',
    pathMatch: 'full'
  },
  {
    path: 'login',
    component: LoginComponent
  },
  {
    path: 'register',
    component: RegisterComponent
  },
  {
    path: 'dashboard',
    component: DashboardComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'clientes',
    canActivate: [AuthGuard],
    children: [
      { path: '', component: ClienteListadoComponent },
      { path: 'crear', component: ClienteCrearEditarComponent },
      { path: ':id', component: ClienteDetalleComponent },
      { path: ':id/editar', component: ClienteCrearEditarComponent }
    ]
  },
  {
    path: 'equipos',
    canActivate: [AuthGuard],
    children: [
      { path: '', component: EquipoListadoComponent },
      { path: 'crear', component: EquipoCrearEditarComponent },
      { path: ':id', component: EquipoDetalleComponent },
      { path: ':id/editar', component: EquipoCrearEditarComponent },
      { path: ':id/mantenimientos', component: MantenimientoHistorialComponent }
    ]
  },
  {
    path: 'mantenimientos',
    canActivate: [AuthGuard],
    children: [
      { path: '', component: MantenimientoListadoComponent },
      { path: 'crear', component: MantenimientoCrearEditarComponent },
      { path: ':id', component: MantenimientoDetalleComponent },
      { path: ':id/editar', component: MantenimientoCrearEditarComponent },
      { path: ':id/fotos', component: MantenimientoFotosComponent },
      { path: 'historial/:equipoId', component: MantenimientoHistorialComponent }
    ]
  },
  {
    path: 'busqueda',
    component: BusquedaComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'reportes',
    component: ReportesComponent,
    canActivate: [AuthGuard],
    data: { roles: ['ADMIN', 'TECNICO'] }
  },
  {
    path: 'perfil',
    component: PerfilComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'admin',
    component: AdminComponent,
    canActivate: [AuthGuard],
    data: { roles: ['ADMIN'] }
  },
  {
    path: '**',
    redirectTo: '/dashboard'
  }
];
```

---

## 🔌 Endpoints y JSON de APIs

### Base URL
```
http://localhost:8080/api
```

---

### **AUTENTICACIÓN**

#### 1. Login
**Método:** `POST`  
**Endpoint:** `/auth/login`  
**Autenticación:** No requerida  
**Permisos:** Público

**Request:**
```json
{
  "nombre": "usuario1",
  "password": "password123"
}
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c3VhcmlvMSIsImlhdCI6MTcyNDI0MzE2MCwiZXhwIjoxNzI0MzI5NTYwfQ.abc123...",
  "tipo": "Bearer",
  "id": 1,
  "nombre": "usuario1",
  "rol": "ADMIN"
}
```

**Response (401 Unauthorized):**
```json
"Credenciales inválidas"
```

---

#### 2. Register
**Método:** `POST`  
**Endpoint:** `/auth/register`  
**Autenticación:** No requerida  
**Permisos:** Público

**Request:**
```json
{
  "nombre": "nuevoUsuario",
  "password": "password123"
}
```

**Response (200 OK):**
```json
"Usuario creado exitosamente"
```

**Response (400 Bad Request):**
```json
"El usuario ya existe"
```

**Response (500 Internal Server Error):**
```json
"Error al crear usuario: mensaje del error"
```

---

### **CLIENTES**

#### 3. Listar Todos los Clientes
**Método:** `GET`  
**Endpoint:** `/clientes`  
**Autenticación:** Requerida (Bearer token)  
**Permisos:** ADMIN, TECNICO, CONSULTA

**Request Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "nombre": "Cliente ABC",
    "telefono": "3105555555",
    "email": "contacto@cliente.com",
    "nitCc": "901234567",
    "notas": "Cliente VIP"
  },
  {
    "id": 2,
    "nombre": "Empresa XYZ",
    "telefono": "3105555556",
    "email": "info@empresa.com",
    "nitCc": "812345678",
    "notas": "Contrato anual"
  }
]
```

**Response (403 Forbidden):**
```json
"Access Denied"
```

---

#### 4. Obtener Cliente por ID
**Método:** `GET`  
**Endpoint:** `/clientes/{id}`  
**Autenticación:** Requerida  
**Permisos:** ADMIN, TECNICO, CONSULTA

**Request Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Example:** `GET /clientes/1`

**Response (200 OK):**
```json
{
  "id": 1,
  "nombre": "Cliente ABC",
  "telefono": "3105555555",
  "email": "contacto@cliente.com",
  "nitCc": "901234567",
  "notas": "Cliente VIP"
}
```

**Response (404 Not Found):**
```
(sin cuerpo)
```

---

#### 5. Crear Cliente
**Método:** `POST`  
**Endpoint:** `/clientes`  
**Autenticación:** Requerida  
**Permisos:** ADMIN, TECNICO

**Request Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json
```

**Request:**
```json
{
  "nombre": "Nuevo Cliente",
  "telefono": "3105555555",
  "email": "nuevo@cliente.com",
  "nitCc": "901234567",
  "notas": "Nota adicional"
}
```

**Response (201 Created):**
```json
{
  "id": 3,
  "nombre": "Nuevo Cliente",
  "telefono": "3105555555",
  "email": "nuevo@cliente.com",
  "nitCc": "901234567",
  "notas": "Nota adicional"
}
```

**Response (400 Bad Request):**
```
(sin cuerpo)
```

---

#### 6. Actualizar Cliente
**Método:** `PUT`  
**Endpoint:** `/clientes/{id}`  
**Autenticación:** Requerida  
**Permisos:** ADMIN, TECNICO

**Request Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json
```

**Example:** `PUT /clientes/1`

**Request:**
```json
{
  "id": 1,
  "nombre": "Cliente ABC Actualizado",
  "telefono": "3105555555",
  "email": "nuevo@cliente.com",
  "nitCc": "901234567",
  "notas": "Nota actualizada"
}
```

**Response (200 OK):**
```json
{
  "id": 1,
  "nombre": "Cliente ABC Actualizado",
  "telefono": "3105555555",
  "email": "nuevo@cliente.com",
  "nitCc": "901234567",
  "notas": "Nota actualizada"
}
```

**Response (404 Not Found):**
```
(sin cuerpo)
```

---

#### 7. Eliminar Cliente
**Método:** `DELETE`  
**Endpoint:** `/clientes/{id}`  
**Autenticación:** Requerida  
**Permisos:** ADMIN

**Request Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Example:** `DELETE /clientes/1`

**Response (204 No Content):**
```
(sin cuerpo)
```

**Response (404 Not Found):**
```
(sin cuerpo)
```

---

### **EQUIPOS**

#### 8. Listar Todos los Equipos
**Método:** `GET`  
**Endpoint:** `/equipos`  
**Autenticación:** Requerida  
**Permisos:** ADMIN, TECNICO, CONSULTA

**Request Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "codigo": "EQ-001",
    "descripcion": "Compresor de aire",
    "numeroSerie": "SN12345",
    "modelo": "AC-3000",
    "marca": "CompAir",
    "categoriaId": 1,
    "categoriaNombre": "Compresores",
    "tipoMotorId": 1,
    "tipoMotorNombre": "Motor eléctrico",
    "esPropio": true,
    "clienteId": null,
    "clienteNombre": null,
    "ubicacion": "Taller - Estante 1",
    "estadoId": 1,
    "estadoNombre": "Operativo",
    "checkMtto": true,
    "checkHv": false,
    "checkFt": true,
    "accesorios": "Manómetro, Válvula de seguridad",
    "registradoPorId": 1,
    "registradoPorNombre": "Admin User",
    "fechaRegistro": "2024-08-20",
    "fechaIngresoTaller": "2024-08-20",
    "creadoEn": "2024-08-20T10:30:00",
    "actualizadoEn": "2024-08-20T10:30:00"
  },
  {
    "id": 2,
    "codigo": "EQ-002",
    "descripcion": "Bomba de agua",
    "numeroSerie": "SN54321",
    "modelo": "BP-500",
    "marca": "Grundfos",
    "categoriaId": 2,
    "categoriaNombre": "Bombas",
    "tipoMotorId": 2,
    "tipoMotorNombre": "Motor de combustión",
    "esPropio": false,
    "clienteId": 1,
    "clienteNombre": "Cliente ABC",
    "ubicacion": "Bodega del cliente",
    "estadoId": 2,
    "estadoNombre": "En mantenimiento",
    "checkMtto": false,
    "checkHv": true,
    "checkFt": false,
    "accesorios": "Tuberías de entrada y salida",
    "registradoPorId": 2,
    "registradoPorNombre": "Técnico 1",
    "fechaRegistro": "2024-08-19",
    "fechaIngresoTaller": "2024-08-22",
    "creadoEn": "2024-08-19T14:15:00",
    "actualizadoEn": "2024-08-22T09:00:00"
  }
]
```

---

#### 9. Listar Equipos Propios
**Método:** `GET`  
**Endpoint:** `/equipos/propios`  
**Autenticación:** Requerida  
**Permisos:** ADMIN, TECNICO, CONSULTA

**Request Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "codigo": "EQ-001",
    "descripcion": "Compresor de aire",
    "numeroSerie": "SN12345",
    "modelo": "AC-3000",
    "marca": "CompAir",
    "categoriaId": 1,
    "categoriaNombre": "Compresores",
    "tipoMotorId": 1,
    "tipoMotorNombre": "Motor eléctrico",
    "esPropio": true,
    "clienteId": null,
    "clienteNombre": null,
    "ubicacion": "Taller - Estante 1",
    "estadoId": 1,
    "estadoNombre": "Operativo",
    "checkMtto": true,
    "checkHv": false,
    "checkFt": true,
    "accesorios": "Manómetro, Válvula de seguridad",
    "registradoPorId": 1,
    "registradoPorNombre": "Admin User",
    "fechaRegistro": "2024-08-20",
    "fechaIngresoTaller": "2024-08-20",
    "creadoEn": "2024-08-20T10:30:00",
    "actualizadoEn": "2024-08-20T10:30:00"
  }
]
```

---

#### 10. Listar Equipos Externos
**Método:** `GET`  
**Endpoint:** `/equipos/externos`  
**Autenticación:** Requerida  
**Permisos:** ADMIN, TECNICO, CONSULTA

**Request Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response (200 OK):**
```json
[
  {
    "id": 2,
    "codigo": "EQ-002",
    "descripcion": "Bomba de agua",
    "numeroSerie": "SN54321",
    "modelo": "BP-500",
    "marca": "Grundfos",
    "categoriaId": 2,
    "categoriaNombre": "Bombas",
    "tipoMotorId": 2,
    "tipoMotorNombre": "Motor de combustión",
    "esPropio": false,
    "clienteId": 1,
    "clienteNombre": "Cliente ABC",
    "ubicacion": "Bodega del cliente",
    "estadoId": 2,
    "estadoNombre": "En mantenimiento",
    "checkMtto": false,
    "checkHv": true,
    "checkFt": false,
    "accesorios": "Tuberías de entrada y salida",
    "registradoPorId": 2,
    "registradoPorNombre": "Técnico 1",
    "fechaRegistro": "2024-08-19",
    "fechaIngresoTaller": "2024-08-22",
    "creadoEn": "2024-08-19T14:15:00",
    "actualizadoEn": "2024-08-22T09:00:00"
  }
]
```

---

#### 11. Buscar Equipos
**Método:** `GET`  
**Endpoint:** `/equipos/buscar?termino={termino}`  
**Autenticación:** Requerida  
**Permisos:** ADMIN, TECNICO, CONSULTA

**Request Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Example:** `GET /equipos/buscar?termino=compresor`

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "codigo": "EQ-001",
    "descripcion": "Compresor de aire",
    "numeroSerie": "SN12345",
    "modelo": "AC-3000",
    "marca": "CompAir",
    "categoriaId": 1,
    "categoriaNombre": "Compresores",
    "tipoMotorId": 1,
    "tipoMotorNombre": "Motor eléctrico",
    "esPropio": true,
    "clienteId": null,
    "clienteNombre": null,
    "ubicacion": "Taller - Estante 1",
    "estadoId": 1,
    "estadoNombre": "Operativo",
    "checkMtto": true,
    "checkHv": false,
    "checkFt": true,
    "accesorios": "Manómetro, Válvula de seguridad",
    "registradoPorId": 1,
    "registradoPorNombre": "Admin User",
    "fechaRegistro": "2024-08-20",
    "fechaIngresoTaller": "2024-08-20",
    "creadoEn": "2024-08-20T10:30:00",
    "actualizadoEn": "2024-08-20T10:30:00"
  }
]
```

---

#### 12. Obtener Equipo por ID
**Método:** `GET`  
**Endpoint:** `/equipos/{id}`  
**Autenticación:** Requerida  
**Permisos:** ADMIN, TECNICO, CONSULTA

**Request Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Example:** `GET /equipos/1`

**Response (200 OK):**
```json
{
  "id": 1,
  "codigo": "EQ-001",
  "descripcion": "Compresor de aire",
  "numeroSerie": "SN12345",
  "modelo": "AC-3000",
  "marca": "CompAir",
  "categoriaId": 1,
  "categoriaNombre": "Compresores",
  "tipoMotorId": 1,
  "tipoMotorNombre": "Motor eléctrico",
  "esPropio": true,
  "clienteId": null,
  "clienteNombre": null,
  "ubicacion": "Taller - Estante 1",
  "estadoId": 1,
  "estadoNombre": "Operativo",
  "checkMtto": true,
  "checkHv": false,
  "checkFt": true,
  "accesorios": "Manómetro, Válvula de seguridad",
  "registradoPorId": 1,
  "registradoPorNombre": "Admin User",
  "fechaRegistro": "2024-08-20",
  "fechaIngresoTaller": "2024-08-20",
  "creadoEn": "2024-08-20T10:30:00",
  "actualizadoEn": "2024-08-20T10:30:00"
}
```

**Response (404 Not Found):**
```
(sin cuerpo)
```

---

#### 13. Crear Equipo
**Método:** `POST`  
**Endpoint:** `/equipos`  
**Autenticación:** Requerida  
**Permisos:** ADMIN, TECNICO

**Request Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json
```

**Request:**
```json
{
  "codigo": "EQ-003",
  "descripcion": "Generador eléctrico",
  "numeroSerie": "SN99999",
  "modelo": "GEN-5000",
  "marca": "Caterpillar",
  "categoriaId": 3,
  "tipoMotorId": 1,
  "esPropio": true,
  "clienteId": null,
  "ubicacion": "Bodega principal",
  "estadoId": 1,
  "checkMtto": false,
  "checkHv": true,
  "checkFt": false,
  "accesorios": "Cable de alimentación 50m"
}
```

**Response (201 Created):**
```json
{
  "id": 3,
  "codigo": "EQ-003",
  "descripcion": "Generador eléctrico",
  "numeroSerie": "SN99999",
  "modelo": "GEN-5000",
  "marca": "Caterpillar",
  "categoriaId": 3,
  "categoriaNombre": "Generadores",
  "tipoMotorId": 1,
  "tipoMotorNombre": "Motor eléctrico",
  "esPropio": true,
  "clienteId": null,
  "clienteNombre": null,
  "ubicacion": "Bodega principal",
  "estadoId": 1,
  "estadoNombre": "Operativo",
  "checkMtto": false,
  "checkHv": true,
  "checkFt": false,
  "accesorios": "Cable de alimentación 50m",
  "registradoPorId": 1,
  "registradoPorNombre": "Admin User",
  "fechaRegistro": "2024-08-25",
  "fechaIngresoTaller": null,
  "creadoEn": "2024-08-25T11:00:00",
  "actualizadoEn": "2024-08-25T11:00:00"
}
```

**Response (400 Bad Request):**
```
(sin cuerpo)
```

---

#### 14. Actualizar Equipo
**Método:** `PUT`  
**Endpoint:** `/equipos/{id}`  
**Autenticación:** Requerida  
**Permisos:** ADMIN, TECNICO

**Request Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json
```

**Example:** `PUT /equipos/1`

**Request:**
```json
{
  "id": 1,
  "codigo": "EQ-001-UPDATED",
  "descripcion": "Compresor de aire actualizado",
  "numeroSerie": "SN12345",
  "modelo": "AC-3000",
  "marca": "CompAir",
  "categoriaId": 1,
  "tipoMotorId": 1,
  "esPropio": true,
  "clienteId": null,
  "ubicacion": "Taller - Estante 2",
  "estadoId": 1,
  "checkMtto": true,
  "checkHv": true,
  "checkFt": true,
  "accesorios": "Manómetro, Válvula de seguridad, Filtro nuevo"
}
```

**Response (200 OK):**
```json
{
  "id": 1,
  "codigo": "EQ-001-UPDATED",
  "descripcion": "Compresor de aire actualizado",
  "numeroSerie": "SN12345",
  "modelo": "AC-3000",
  "marca": "CompAir",
  "categoriaId": 1,
  "categoriaNombre": "Compresores",
  "tipoMotorId": 1,
  "tipoMotorNombre": "Motor eléctrico",
  "esPropio": true,
  "clienteId": null,
  "clienteNombre": null,
  "ubicacion": "Taller - Estante 2",
  "estadoId": 1,
  "estadoNombre": "Operativo",
  "checkMtto": true,
  "checkHv": true,
  "checkFt": true,
  "accesorios": "Manómetro, Válvula de seguridad, Filtro nuevo",
  "registradoPorId": 1,
  "registradoPorNombre": "Admin User",
  "fechaRegistro": "2024-08-20",
  "fechaIngresoTaller": "2024-08-20",
  "creadoEn": "2024-08-20T10:30:00",
  "actualizadoEn": "2024-08-25T14:30:00"
}
```

**Response (404 Not Found):**
```
(sin cuerpo)
```

---

#### 15. Eliminar Equipo
**Método:** `DELETE`  
**Endpoint:** `/equipos/{id}`  
**Autenticación:** Requerida  
**Permisos:** ADMIN

**Request Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Example:** `DELETE /equipos/1`

**Response (204 No Content):**
```
(sin cuerpo)
```

**Response (404 Not Found):**
```
(sin cuerpo)
```

---

### **MANTENIMIENTOS**

#### 16. Listar Todos los Mantenimientos
**Método:** `GET`  
**Endpoint:** `/mantenimientos`  
**Autenticación:** Requerida  
**Permisos:** ADMIN, TECNICO, CONSULTA

**Request Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "equipoId": 1,
    "equipoCodigo": "EQ-001",
    "equipoDescripcion": "Compresor de aire",
    "tipo": "Preventivo",
    "descripcionTrabajo": "Revisión y mantenimiento general",
    "diagnostico": "Equipo en buen estado, cambio de aceite realizado",
    "observaciones": "Se recomienda revisión cada 3 meses",
    "fechaApertura": "2024-08-20",
    "fechaDiagnostico": "2024-08-20",
    "fechaInicio": "2024-08-20",
    "fechaCierre": "2024-08-21",
    "estadoId": 3,
    "estadoNombre": "Completado",
    "tecnicoId": 2,
    "tecnicoNombre": "Técnico 1",
    "costoManoObra": "150000.00",
    "costoRepuestos": "45000.00",
    "costoTotal": "195000.00",
    "rotulado": true
  },
  {
    "id": 2,
    "equipoId": 2,
    "equipoCodigo": "EQ-002",
    "equipoDescripcion": "Bomba de agua",
    "tipo": "Correctivo",
    "descripcionTrabajo": "Reparación de fugas en cuerpo",
    "diagnostico": "Fugas detectadas en juntas",
    "observaciones": "Requiere repuestos especiales",
    "fechaApertura": "2024-08-22",
    "fechaDiagnostico": "2024-08-22",
    "fechaInicio": "2024-08-23",
    "fechaCierre": null,
    "estadoId": 2,
    "estadoNombre": "En proceso",
    "tecnicoId": 2,
    "tecnicoNombre": "Técnico 1",
    "costoManoObra": "200000.00",
    "costoRepuestos": "120000.00",
    "costoTotal": "320000.00",
    "rotulado": false
  }
]
```

---

#### 17. Obtener Mantenimiento por ID
**Método:** `GET`  
**Endpoint:** `/mantenimientos/{id}`  
**Autenticación:** Requerida  
**Permisos:** ADMIN, TECNICO, CONSULTA

**Request Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Example:** `GET /mantenimientos/1`

**Response (200 OK):**
```json
{
  "id": 1,
  "equipoId": 1,
  "equipoCodigo": "EQ-001",
  "equipoDescripcion": "Compresor de aire",
  "tipo": "Preventivo",
  "descripcionTrabajo": "Revisión y mantenimiento general",
  "diagnostico": "Equipo en buen estado, cambio de aceite realizado",
  "observaciones": "Se recomienda revisión cada 3 meses",
  "fechaApertura": "2024-08-20",
  "fechaDiagnostico": "2024-08-20",
  "fechaInicio": "2024-08-20",
  "fechaCierre": "2024-08-21",
  "estadoId": 3,
  "estadoNombre": "Completado",
  "tecnicoId": 2,
  "tecnicoNombre": "Técnico 1",
  "costoManoObra": "150000.00",
  "costoRepuestos": "45000.00",
  "costoTotal": "195000.00",
  "rotulado": true
}
```

**Response (404 Not Found):**
```
(sin cuerpo)
```

---

#### 18. Obtener Historial de Mantenimientos por Equipo
**Método:** `GET`  
**Endpoint:** `/mantenimientos/equipo/{equipoId}`  
**Autenticación:** Requerida  
**Permisos:** ADMIN, TECNICO, CONSULTA

**Request Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Example:** `GET /mantenimientos/equipo/1`

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "equipoId": 1,
    "equipoCodigo": "EQ-001",
    "equipoDescripcion": "Compresor de aire",
    "tipo": "Preventivo",
    "descripcionTrabajo": "Revisión y mantenimiento general",
    "diagnostico": "Equipo en buen estado, cambio de aceite realizado",
    "observaciones": "Se recomienda revisión cada 3 meses",
    "fechaApertura": "2024-08-20",
    "fechaDiagnostico": "2024-08-20",
    "fechaInicio": "2024-08-20",
    "fechaCierre": "2024-08-21",
    "estadoId": 3,
    "estadoNombre": "Completado",
    "tecnicoId": 2,
    "tecnicoNombre": "Técnico 1",
    "costoManoObra": "150000.00",
    "costoRepuestos": "45000.00",
    "costoTotal": "195000.00",
    "rotulado": true
  }
]
```

---

#### 19. Obtener Mantenimientos por Rango de Fechas
**Método:** `GET`  
**Endpoint:** `/mantenimientos/rango?desde={desde}&hasta={hasta}`  
**Autenticación:** Requerida  
**Permisos:** ADMIN, TECNICO, CONSULTA

**Request Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Example:** `GET /mantenimientos/rango?desde=2024-08-01&hasta=2024-08-31`

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "equipoId": 1,
    "equipoCodigo": "EQ-001",
    "equipoDescripcion": "Compresor de aire",
    "tipo": "Preventivo",
    "descripcionTrabajo": "Revisión y mantenimiento general",
    "diagnostico": "Equipo en buen estado, cambio de aceite realizado",
    "observaciones": "Se recomienda revisión cada 3 meses",
    "fechaApertura": "2024-08-20",
    "fechaDiagnostico": "2024-08-20",
    "fechaInicio": "2024-08-20",
    "fechaCierre": "2024-08-21",
    "estadoId": 3,
    "estadoNombre": "Completado",
    "tecnicoId": 2,
    "tecnicoNombre": "Técnico 1",
    "costoManoObra": "150000.00",
    "costoRepuestos": "45000.00",
    "costoTotal": "195000.00",
    "rotulado": true
  },
  {
    "id": 2,
    "equipoId": 2,
    "equipoCodigo": "EQ-002",
    "equipoDescripcion": "Bomba de agua",
    "tipo": "Correctivo",
    "descripcionTrabajo": "Reparación de fugas en cuerpo",
    "diagnostico": "Fugas detectadas en juntas",
    "observaciones": "Requiere repuestos especiales",
    "fechaApertura": "2024-08-22",
    "fechaDiagnostico": "2024-08-22",
    "fechaInicio": "2024-08-23",
    "fechaCierre": null,
    "estadoId": 2,
    "estadoNombre": "En proceso",
    "tecnicoId": 2,
    "tecnicoNombre": "Técnico 1",
    "costoManoObra": "200000.00",
    "costoRepuestos": "120000.00",
    "costoTotal": "320000.00",
    "rotulado": false
  }
]
```

---

#### 20. Crear Mantenimiento
**Método:** `POST`  
**Endpoint:** `/mantenimientos`  
**Autenticación:** Requerida  
**Permisos:** ADMIN, TECNICO

**Request Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json
```

**Request:**
```json
{
  "equipoId": 3,
  "tipo": "Preventivo",
  "descripcionTrabajo": "Mantenimiento preventivo de generador",
  "diagnostico": "",
  "observaciones": "Mantenimiento programado",
  "fechaApertura": "2024-08-25",
  "fechaDiagnostico": null,
  "fechaInicio": null,
  "fechaCierre": null,
  "estadoId": 1,
  "tecnicoId": 2,
  "costoManoObra": "0.00",
  "costoRepuestos": "0.00",
  "rotulado": false
}
```

**Response (201 Created):**
```json
{
  "id": 3,
  "equipoId": 3,
  "equipoCodigo": "EQ-003",
  "equipoDescripcion": "Generador eléctrico",
  "tipo": "Preventivo",
  "descripcionTrabajo": "Mantenimiento preventivo de generador",
  "diagnostico": "",
  "observaciones": "Mantenimiento programado",
  "fechaApertura": "2024-08-25",
  "fechaDiagnostico": null,
  "fechaInicio": null,
  "fechaCierre": null,
  "estadoId": 1,
  "estadoNombre": "Abierto",
  "tecnicoId": 2,
  "tecnicoNombre": "Técnico 1",
  "costoManoObra": "0.00",
  "costoRepuestos": "0.00",
  "costoTotal": "0.00",
  "rotulado": false
}
```

**Response (400 Bad Request):**
```
(sin cuerpo)
```

---

#### 21. Actualizar Mantenimiento
**Método:** `PUT`  
**Endpoint:** `/mantenimientos/{id}`  
**Autenticación:** Requerida  
**Permisos:** ADMIN, TECNICO

**Request Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json
```

**Example:** `PUT /mantenimientos/3`

**Request:**
```json
{
  "id": 3,
  "equipoId": 3,
  "tipo": "Preventivo",
  "descripcionTrabajo": "Mantenimiento preventivo de generador completado",
  "diagnostico": "Generador en excelente estado. Reemplazado filtro de aire",
  "observaciones": "Todo funciona correctamente",
  "fechaApertura": "2024-08-25",
  "fechaDiagnostico": "2024-08-25",
  "fechaInicio": "2024-08-25",
  "fechaCierre": "2024-08-25",
  "estadoId": 3,
  "tecnicoId": 2,
  "costoManoObra": "100000.00",
  "costoRepuestos": "35000.00",
  "rotulado": true
}
```

**Response (200 OK):**
```json
{
  "id": 3,
  "equipoId": 3,
  "equipoCodigo": "EQ-003",
  "equipoDescripcion": "Generador eléctrico",
  "tipo": "Preventivo",
  "descripcionTrabajo": "Mantenimiento preventivo de generador completado",
  "diagnostico": "Generador en excelente estado. Reemplazado filtro de aire",
  "observaciones": "Todo funciona correctamente",
  "fechaApertura": "2024-08-25",
  "fechaDiagnostico": "2024-08-25",
  "fechaInicio": "2024-08-25",
  "fechaCierre": "2024-08-25",
  "estadoId": 3,
  "estadoNombre": "Completado",
  "tecnicoId": 2,
  "tecnicoNombre": "Técnico 1",
  "costoManoObra": "100000.00",
  "costoRepuestos": "35000.00",
  "costoTotal": "135000.00",
  "rotulado": true
}
```

**Response (404 Not Found):**
```
(sin cuerpo)
```

---

#### 22. Eliminar Mantenimiento
**Método:** `DELETE`  
**Endpoint:** `/mantenimientos/{id}`  
**Autenticación:** Requerida  
**Permisos:** ADMIN

**Request Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Example:** `DELETE /mantenimientos/3`

**Response (204 No Content):**
```
(sin cuerpo)
```

**Response (404 Not Found):**
```
(sin cuerpo)
```

---

## 🛠️ Servicios Angular

### 1. AuthService

```typescript
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';
import { tap } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = 'http://localhost:8080/api/auth';
  private currentUserSubject: BehaviorSubject<any>;
  public currentUser: Observable<any>;

  constructor(private http: HttpClient) {
    this.currentUserSubject = new BehaviorSubject<any>(this.getUserFromStorage());
    this.currentUser = this.currentUserSubject.asObservable();
  }

  public get currentUserValue(): any {
    return this.currentUserSubject.value;
  }

  login(nombre: string, password: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, { nombre, password })
      .pipe(tap(response => {
        if (response && response.token) {
          localStorage.setItem('token', response.token);
          localStorage.setItem('user', JSON.stringify(response));
          this.currentUserSubject.next(response);
        }
        return response;
      }));
  }

  register(nombre: string, password: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/register`, { nombre, password });
  }

  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    this.currentUserSubject.next(null);
  }

  isAuthenticated(): boolean {
    return !!localStorage.getItem('token');
  }

  private getUserFromStorage(): any {
    const user = localStorage.getItem('user');
    return user ? JSON.parse(user) : null;
  }

  hasRole(role: string): boolean {
    return this.currentUserValue && this.currentUserValue.rol === role;
  }

  hasAnyRole(roles: string[]): boolean {
    return this.currentUserValue && roles.includes(this.currentUserValue.rol);
  }
}
```

---

### 2. ClienteService

```typescript
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ClienteDTO } from '../models/cliente.model';

@Injectable({
  providedIn: 'root'
})
export class ClienteService {
  private apiUrl = 'http://localhost:8080/api/clientes';

  constructor(private http: HttpClient) { }

  listarTodos(): Observable<ClienteDTO[]> {
    return this.http.get<ClienteDTO[]>(this.apiUrl);
  }

  obtenerPorId(id: number): Observable<ClienteDTO> {
    return this.http.get<ClienteDTO>(`${this.apiUrl}/${id}`);
  }

  crear(cliente: ClienteDTO): Observable<ClienteDTO> {
    return this.http.post<ClienteDTO>(this.apiUrl, cliente);
  }

  actualizar(id: number, cliente: ClienteDTO): Observable<ClienteDTO> {
    return this.http.put<ClienteDTO>(`${this.apiUrl}/${id}`, cliente);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
```

---

### 3. EquipoService

```typescript
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { EquipoDTO } from '../models/equipo.model';

@Injectable({
  providedIn: 'root'
})
export class EquipoService {
  private apiUrl = 'http://localhost:8080/api/equipos';

  constructor(private http: HttpClient) { }

  listarTodos(): Observable<EquipoDTO[]> {
    return this.http.get<EquipoDTO[]>(this.apiUrl);
  }

  listarPropios(): Observable<EquipoDTO[]> {
    return this.http.get<EquipoDTO[]>(`${this.apiUrl}/propios`);
  }

  listarExternos(): Observable<EquipoDTO[]> {
    return this.http.get<EquipoDTO[]>(`${this.apiUrl}/externos`);
  }

  buscar(termino: string): Observable<EquipoDTO[]> {
    const params = new HttpParams().set('termino', termino);
    return this.http.get<EquipoDTO[]>(`${this.apiUrl}/buscar`, { params });
  }

  obtenerPorId(id: number): Observable<EquipoDTO> {
    return this.http.get<EquipoDTO>(`${this.apiUrl}/${id}`);
  }

  crear(equipo: EquipoDTO): Observable<EquipoDTO> {
    return this.http.post<EquipoDTO>(this.apiUrl, equipo);
  }

  actualizar(id: number, equipo: EquipoDTO): Observable<EquipoDTO> {
    return this.http.put<EquipoDTO>(`${this.apiUrl}/${id}`, equipo);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
```

---

### 4. MantenimientoService

```typescript
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { MantenimientoDTO } from '../models/mantenimiento.model';

@Injectable({
  providedIn: 'root'
})
export class MantenimientoService {
  private apiUrl = 'http://localhost:8080/api/mantenimientos';

  constructor(private http: HttpClient) { }

  listarTodos(): Observable<MantenimientoDTO[]> {
    return this.http.get<MantenimientoDTO[]>(this.apiUrl);
  }

  obtenerPorId(id: number): Observable<MantenimientoDTO> {
    return this.http.get<MantenimientoDTO>(`${this.apiUrl}/${id}`);
  }

  obtenerHistorialEquipo(equipoId: number): Observable<MantenimientoDTO[]> {
    return this.http.get<MantenimientoDTO[]>(`${this.apiUrl}/equipo/${equipoId}`);
  }

  obtenerPorRangoFechas(desde: string, hasta: string): Observable<MantenimientoDTO[]> {
    const params = new HttpParams()
      .set('desde', desde)
      .set('hasta', hasta);
    return this.http.get<MantenimientoDTO[]>(`${this.apiUrl}/rango`, { params });
  }

  crear(mantenimiento: MantenimientoDTO): Observable<MantenimientoDTO> {
    return this.http.post<MantenimientoDTO>(this.apiUrl, mantenimiento);
  }

  actualizar(id: number, mantenimiento: MantenimientoDTO): Observable<MantenimientoDTO> {
    return this.http.put<MantenimientoDTO>(`${this.apiUrl}/${id}`, mantenimiento);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
```

---

## 🔐 Guardias de Autenticación

### AuthGuard

```typescript
import { Injectable } from '@angular/core';
import { Router, CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {
  constructor(private authService: AuthService, private router: Router) { }

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {
    if (!this.authService.isAuthenticated()) {
      this.router.navigate(['/login']);
      return false;
    }

    // Verificar roles si están especificados en la ruta
    if (route.data['roles'] && route.data['roles'].length > 0) {
      if (!this.authService.hasAnyRole(route.data['roles'])) {
        this.router.navigate(['/dashboard']);
        return false;
      }
    }

    return true;
  }
}
```

---

## 🔗 Interceptores

### JwtInterceptor

```typescript
import { Injectable } from '@angular/core';
import { HttpRequest, HttpHandler, HttpEvent, HttpInterceptor, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AuthService } from '../services/auth.service';
import { Router } from '@angular/router';

@Injectable()
export class JwtInterceptor implements HttpInterceptor {
  constructor(private authService: AuthService, private router: Router) { }

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    // Agregar token a las peticiones
    const token = localStorage.getItem('token');
    if (token) {
      request = request.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });
    }

    return next.handle(request).pipe(
      catchError((error: HttpErrorResponse) => {
        // Si el token expira (401), redirigir a login
        if (error.status === 401) {
          this.authService.logout();
          this.router.navigate(['/login']);
        }
        return throwError(() => error);
      })
    );
  }
}
```

---

## 📝 Modelos TypeScript

### ClienteDTO

```typescript
export interface ClienteDTO {
  id?: number;
  nombre: string;
  telefono: string;
  email: string;
  nitCc: string;
  notas?: string;
}
```

### EquipoDTO

```typescript
export interface EquipoDTO {
  id?: number;
  codigo: string;
  descripcion: string;
  numeroSerie: string;
  modelo: string;
  marca: string;
  categoriaId: number;
  categoriaNombre?: string;
  tipoMotorId?: number;
  tipoMotorNombre?: string;
  esPropio: boolean;
  clienteId?: number;
  clienteNombre?: string;
  ubicacion: string;
  estadoId: number;
  estadoNombre?: string;
  checkMtto: boolean;
  checkHv: boolean;
  checkFt: boolean;
  accesorios?: string;
  registradoPorId?: number;
  registradoPorNombre?: string;
  fechaRegistro?: Date;
  fechaIngresoTaller?: Date;
  creadoEn?: Date;
  actualizadoEn?: Date;
}
```

### MantenimientoDTO

```typescript
export interface MantenimientoDTO {
  id?: number;
  equipoId: number;
  equipoCodigo?: string;
  equipoDescripcion?: string;
  tipo: string;
  descripcionTrabajo: string;
  diagnostico?: string;
  observaciones?: string;
  fechaApertura: Date;
  fechaDiagnostico?: Date;
  fechaInicio?: Date;
  fechaCierre?: Date;
  estadoId: number;
  estadoNombre?: string;
  tecnicoId: number;
  tecnicoNombre?: string;
  costoManoObra: number;
  costoRepuestos: number;
  costoTotal?: number;
  rotulado: boolean;
}
```

### LoginRequest

```typescript
export interface LoginRequest {
  nombre: string;
  password: string;
}
```

### LoginResponse

```typescript
export interface LoginResponse {
  token: string;
  tipo: string;
  id: number;
  nombre: string;
  rol: string;
}
```

---

## 🎯 Consideraciones para el Frontend

### 1. Autenticación y Seguridad
- ✅ Almacenar token en localStorage
- ✅ Enviar token en header Authorization de cada petición
- ✅ Redirigir a login si token expira (401)
- ✅ Usar guards para proteger rutas
- ✅ Validar roles en el frontend

### 2. Diseño UI
- Usar componentes reutilizables (tablas, formularios, modales)
- Implementar loading spinners durante peticiones
- Mostrar mensajes de error claros
- Confirmaciones para acciones destructivas (eliminar)
- Validaciones en tiempo real en formularios

### 3. Gestión de Estado
- Considerar usar NgRx o Akita para estado global
- Cachear datos cuando sea posible
- Implementar paginación en listas largas

### 4. UX/UX
- Breadcrumbs para navegación
- Indicadores de carga
- Mensajes de confirmación y success/error
- Tooltips para campos complejos
- Respuesta rápida en búsquedas (debounce)

### 5. Optimización
- Lazy loading de módulos
- Optimizar peticiones HTTP (evitar llamadas innecesarias)
- Caché de respuestas GET
- Compresión de imágenes en galería de fotos

---

## ✅ Checklist de Implementación

- [ ] Configurar proyecto Angular 17
- [ ] Implementar AuthService y AuthGuard
- [ ] Implementar JwtInterceptor
- [ ] Crear vistas de Login y Registro
- [ ] Crear Dashboard
- [ ] Implementar CRUD de Clientes
- [ ] Implementar CRUD de Equipos
- [ ] Implementar CRUD de Mantenimientos
- [ ] Implementar búsqueda avanzada
- [ ] Implementar reportes
- [ ] Implementar gestión de fotos
- [ ] Implementar panel de administración
- [ ] Diseño responsive
- [ ] Testing

---

**Fin del documento**
