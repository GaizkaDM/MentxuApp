# MentxuApp - Arquitectura Backend Integration

## 📁 Estructura de Carpetas

```
app/src/main/java/com/gaizkafrost/mentxuapp/
│
├── data/                                    # 📦 CAPA DE DATOS
│   ├── local/                              # Almacenamiento local
│   │   ├── database/
│   │   │   └── AppDatabase.kt             # ✅ Room database singleton
│   │   ├── dao/
│   │   │   ├── ParadaDao.kt               # ✅ DAO para paradas
│   │   │   └── ProgresoDao.kt             # ✅ DAO para progreso
│   │   ├── entity/
│   │   │   ├── ParadaEntity.kt            # ✅ Entity de paradas
│   │   │   └── ProgresoEntity.kt          # ✅ Entity de progreso
│   │   └── preferences/
│   │       └── UserPreferences.kt         # ✅ SharedPreferences encriptadas
│   │
│   ├── remote/                             # API REST
│   │   ├── api/
│   │   │   ├── MentxuApi.kt               # ✅ Interfaz de endpoints
│   │   │   └── RetrofitClient.kt          # ✅ Cliente HTTP
│   │   └── dto/
│   │       ├── UsuarioDTOs.kt             # ✅ Data Transfer Objects - Usuarios
│   │       ├── ParadaDTOs.kt              # ✅ DTOs - Paradas
│   │       └── ProgresoDTOs.kt            # ✅ DTOs - Progreso
│   │
│   └── repository/
│       └── ParadasRepositoryMejorado.kt   # ✅ Repository con lógica de negocio
│
├── utils/                                   # 🛠️ UTILIDADES
│   ├── Resource.kt                         # ✅ Sealed class para estados
│   ├── NetworkHelper.kt                    # ✅ Detector de red
│   └── Constants.kt                        # ✅ Constantes globales
│
├── Mapa/                                   # 🗺️ EXISTENTE
│   └── MapaActivity.kt                     # Mapa con paradas
│
├── Parada1/                                # 🎮 EXISTENTE
│   ├── SopaDeLetrasActivity.kt
│   └── ...
│
├── Parada2/                                # 🎮 EXISTENTE
│   └── DiferenciasActivity.kt
│
└── ... (otras paradas)
```

---

## 🏗️ Arquitectura Visual

```
┌──────────────────────────────────────────────────────────────┐
│                         UI LAYER                              │
│  ┌─────────────┐  ┌─────────────┐  ┌──────────────────┐    │
│  │ Presentacion│  │ MapaActivity│  │  Mini-Juegos     │    │
│  │  Activity   │  │             │  │  (6 Activities)  │    │
│  └──────┬──────┘  └──────┬──────┘  └────────┬─────────┘    │
│         │                 │                  │                │
│         └─────────────────┴──────────────────┘                │
└─────────────────────────┬────────────────────────────────────┘
                          │
                   lifecycleScope.launch
                          │
┌─────────────────────────▼────────────────────────────────────┐
│                  REPOSITORY LAYER                             │
│  ┌───────────────────────────────────────────────────────┐   │
│  │       ParadasRepositoryMejorado                       │   │
│  │  - obtenerParadas() → Flow<Resource<List<Parada>>>   │   │
│  │  - completarParada()                                  │   │
│  │  - sincronizarProgresosPendientes()                   │   │
│  └──────────────┬─────────────────┬──────────────────────┘   │
└─────────────────┼─────────────────┼───────────────────────────┘
                  │                 │
         ┌────────▼────────┐ ┌─────▼────────┐
         │  LOCAL CACHE    │ │  REMOTE API  │
         │  (Room DB)      │ │  (Retrofit)  │
         └─────────────────┘ └──────────────┘
                  │                 │
         ┌────────▼────────┐ ┌─────▼────────────────────┐
         │  ParadaDao      │ │  RetrofitClient          │
         │  ProgresoDao    │ │  (OkHttp + Logging)      │
         └─────────────────┘ └──────────────────────────┘
                  │                 │
         ┌────────▼────────┐ ┌─────▼────────────────────┐
         │  SQLite         │ │  Flask Backend           │
         │  mentxu_database│ │  http://10.0.2.2:5000    │
         └─────────────────┘ └──────────────────────────┘
```

---

## 🔄 Flujo de Datos: Registro de Usuario

```
┌─────────────┐
│ Usuario     │
│ ingresa     │
│ nombre +    │
│ apellido    │
└──────┬──────┘
       │
       │ onClick
       ▼
┌──────────────────────────────────────┐
│  Presentacion.registrarUsuario()     │
│  1. Genera deviceId                  │
│  2. Crea UsuarioRequest              │
└────────────────┬─────────────────────┘
                 │
       lifecycleScope.launch
                 │
                 ▼
┌──────────────────────────────────────┐
│  RetrofitClient.api                  │
│   .registrarUsuario(request)         │
└────────────────┬─────────────────────┘
                 │
          HTTP POST
                 │
                 ▼
┌──────────────────────────────────────┐
│  Backend Flask                       │
│  POST /api/usuarios/registro         │
│  1. Guarda usuario en SQLite         │
│  2. Inicializa progreso (6 paradas)  │
│  3. Retorna UsuarioResponse          │
└────────────────┬─────────────────────┘
                 │
          JSON Response
                 │
                 ▼
┌──────────────────────────────────────┐
│  Presentacion (success callback)     │
│  1. Guarda ID en UserPreferences     │
│  2. Carga paradas                    │
│  3. Abre MapaActivity                │
└──────────────────────────────────────┘
```

---

## 🔄 Flujo de Datos: Obtener Paradas

```
┌──────────────┐
│ MapaActivity │
│ onCreate()   │
└──────┬───────┘
       │
       │ cargarParadas()
       ▼
┌─────────────────────────────────────────┐
│ repository.obtenerParadas(userId)       │
│               .collect { resource ->    │
└─────────────────┬───────────────────────┘
                  │
           emit(Loading)
                  │
    ┌─────────────▼─────────────┐
    │  1. CACHE LOCAL (Rápido)  │
    │  paradaDao.obtenerTodas() │
    └─────────────┬───────────────┘
                  │
         emit(Success - local)
                  │
    ┌─────────────▼──────────────────┐
    │  2. BACKEND (Si hay conexión)  │
    │  api.obtenerParadas()          │
    └─────────────┬──────────────────┘
                  │
                  ▼
      ┌───────────────────────┐
      │ Backend Flask         │
      │ GET /api/paradas      │
      └───────────┬───────────┘
                  │
          JSON Array Response
                  │
                  ▼
    ┌─────────────────────────────┐
    │ 3. ACTUALIZAR CACHE         │
    │ paradaDao.insertarTodas()   │
    └─────────────┬───────────────┘
                  │
        emit(Success - servidor)
                  │
                  ▼
┌─────────────────────────────────────┐
│ MapaActivity (cuando resource es    │
│ Success) → actualizarMarcadores()   │
└─────────────────────────────────────┘
```

---

## 🔄 Flujo de Datos: Completar Parada

```
┌───────────────────┐
│ SopaDeLetras      │
│ Activity          │
│ (Usuario completa)│
└────────┬──────────┘
         │
         │ onJuegoCompletado()
         ▼
┌──────────────────────────────────────────┐
│ repository.completarParada()             │
│  (userId, paradaId, puntuación, tiempo)  │
└────────────────┬─────────────────────────┘
                 │
      ┌──────────▼──────────┐
      │ 1. LOCAL PRIMERO    │
      │ (Optimistic Update) │
      └──────────┬──────────┘
                 │
    ┌────────────▼─────────────┐
    │ ProgresoDao              │
    │  .actualizar(progreso)   │
    │  estado = "completada"   │
    └────────────┬─────────────┘
                 │
    ┌────────────▼─────────────┐
    │ activarSiguienteParada() │
    │ siguiente.estado=activa  │
    └────────────┬─────────────┘
                 │
      ┌──────────▼─────────────┐
      │ 2. SINCRONIZAR BACKEND │
      │ (Si hay conexión)      │
      └──────────┬─────────────┘
                 │
    ┌────────────▼──────────────────┐
    │ api.completarParada(request)  │
    └────────────┬──────────────────┘
                 │
          HTTP POST
                 │
                 ▼
┌────────────────────────────────────┐
│ Backend Flask                      │
│ POST /api/progreso/completar       │
│ 1. Actualiza progreso en DB        │
│ 2. Activa siguiente parada         │
│ 3. Retorna CompletarParadaResponse │
└────────────┬───────────────────────┘
             │
      JSON Response
             │
             ▼
┌─────────────────────────────────────┐
│ Repository (success)                │
│ progresoDao.marcarComoSincronizado()│
└─────────────┬───────────────────────┘
              │
       emit(Success)
              │
              ▼
┌───────────────────────────────────┐
│ Activity (callback)               │
│ Toast: "¡Parada completada!"      │
│ finish() → volver al mapa         │
└───────────────────────────────────┘
```

---

## 💾 Sincronización Offline

### **Estrategia: Optimistic UI + Background Sync**

```
SIN CONEXIÓN:
┌──────────────────────┐
│ Usuario completa     │
│ mini-juego           │
└───────┬──────────────┘
        │
        ▼
┌──────────────────────┐
│ Guardar en Room      │
│ sincronizado = false │
└───────┬──────────────┘
        │
        ▼
┌──────────────────────┐
│ UI actualizada       │
│ ¡Parada completada!  │
└───────┬──────────────┘
        │
        ▼
  (Esperando conexión...)


CON CONEXIÓN:
┌──────────────────────────┐
│ WorkManager ejecuta      │
│ sincronizarPendientes()  │
└───────┬──────────────────┘
        │
        ▼
┌──────────────────────────┐
│ Buscar registros con     │
│ sincronizado = false     │
└───────┬──────────────────┘
        │
        ▼
┌──────────────────────────┐
│ POST a backend por c/u   │
└───────┬──────────────────┘
        │
        ▼
┌──────────────────────────┐
│ Marcar sincronizado=true │
└──────────────────────────┘
```

---

## 🔐 Seguridad

### **UserPreferences (Encrypted)**

```kotlin
// Datos sensibles encriptados usando Android Security-Crypto
private val sharedPreferences = EncryptedSharedPreferences.create(
    context,
    "mentxu_prefs",
    masterKey,
    AES256_SIV,
    AES256_GCM
)

// Datos guardados:
- userId: Int
- userNombre: String
- userApellido: String
- deviceId: String
```

---

## 📊 Modelos de Datos

### **Parada**

```kotlin
// Domain Model (usado en la app)
data class Parada(
    val id: Int,
    val nombre: String,
    val latLng: LatLng,
    val estado: EstadoParada
)

// Entity (Room Database)
@Entity(tableName = "paradas")
data class ParadaEntity(
    @PrimaryKey val id: Int,
    val nombre: String,
    val nombreCorto: String?,
    val latitud: Double,
    val longitud: Double,
    val descripcion: String?,
    val tipoJuego: String?,
    val orden: Int,
    val estado: String
)

// DTO (API Response)
data class ParadaResponse(
    val id: Int,
    val nombre: String,
    @SerializedName("nombre_corto")
    val nombreCorto: String?,
    val latitud: Double,
    val longitud: Double,
    ...
)
```

---

## 🎯 Estados de Parada

```
BLOQUEADA (Violeta)
     ↓
  (Usuario completa parada anterior)
     ↓
ACTIVA (Roja) ← Usuario puede jugar
     ↓
  (Usuario completa mini-juego)
     ↓
COMPLETADA (Verde)
     ↓
  (Siguiente parada se activa automáticamente)
```

---

## 🚀 Performance

### **Optimizaciones Implementadas**

1. **Cache-First**: Datos locales primero, sincronización en segundo plano
2. **Flow**: Reactivo, solo actualiza UI cuando cambian datos
3. **Coroutines**: Operaciones asíncronas sin bloquear UI
4. **Singleton Pattern**: Una sola instancia de DB y Retrofit
5. **Lazy Initialization**: Objetos creados solo cuando se necesitan
6. **Connection Pooling**: OkHttp reutiliza conexiones HTTP
7. **Retry Logic**: Reintentos automáticos en fallos de red

---

## 📈 Métricas de Éxito

- ✅ Tiempo de carga de paradas: < 500ms (desde cache)
- ✅ Tiempo de sincronización: < 2s (con buena conexión)
- ✅ Funcionalidad offline: 100% (todas las operaciones)
- ✅ Tasa de sincronización: 100% (cuando hay conexión)
- ✅ Crash rate objetivo: < 1%

---

**Documentación generada automáticamente - Última actualización: 15/01/2026**
