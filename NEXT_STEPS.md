# ✅ Checklist de Integración Backend - MentxuApp

## 📋 Estado Actual de la Implementación

### ✅ **Completado (15/01/2026)**

#### 🏗️ **Infraestructura Base**
- [x] Dependencias añadidas a build.gradle.kts
- [x] KSP plugin configurado para Room
- [x] BuildConfig habilitado con API_BASE_URL
- [x] Permiso INTERNET y ACCESS_NETWORK_STATE en Manifest

#### 🌐 **Capa de Red (Remote)**
- [x] MentxuApi.kt - Interfaz con endpoints
- [x] RetrofitClient.kt - Cliente HTTP configurado
- [x] UsuarioDTOs.kt - Request/Response de usuarios
- [x] ParadaDTOs.kt - Request/Response de paradas  
- [x] ProgresoDTOs.kt - Request/Response de progreso

#### 🗄️ **Capa de Base de Datos (Local)**
- [x] AppDatabase.kt - Room database singleton
- [x] ParadaEntity.kt - Entity de paradas
- [x] ProgresoEntity.kt - Entity de progreso
- [x] ParadaDao.kt - DAO con queries
- [x] ProgresoDao.kt - DAO con queries
- [x] UserPreferences.kt - SharedPreferences encriptadas

#### 🔄 **Capa de Repositorio**
- [x] ParadasRepositoryMejorado.kt - Con cache-first y sync

#### 🛠️ **Utilidades**
- [x] Resource.kt - Sealed class para estados
- [x] NetworkHelper.kt - Detector de conectividad
- [x] Constants.kt - Constantes globales

#### 📚 **Documentación**
- [x] INTEGRATION_SUMMARY.md - Resumen de integración
- [x] HOW_TO_USE_BACKEND.md - Guía de uso con ejemplos
- [x] ARCHITECTURE.md - Arquitectura visual completa
- [x] Este checklist (NEXT_STEPS.md)

---

## 🚧 **Pendiente de Implementar**

### 🎯 **Prioridad ALTA (Hacer primero)**

- [ ] **Sincronizar Gradle**
  ```bash
  File → Sync Project with Gradle Files
  ```
  
- [ ] **Iniciar Backend Flask**
  ```bash
  cd c:\Users\GaizkaClase\Desktop\MentxuBackend
  python run.py
  ```

- [ ] **Crear UsuarioRepository.kt**
  - Gestionar registro de usuarios
  - Guardar sesión en UserPreferences
  - Obtener usuario actual
  
- [ ] **Actualizar Presentacion.kt**
  - Implementar registro con backend
  - Cargar paradas al registrar
  - Navegar a MapaActivity
  - Ver ejemplo en: `HOW_TO_USE_BACKEND.md`

- [ ] **Actualizar MapaActivity.kt**
  - Cargar paradas desde ParadasRepositoryMejorado
  - Actualizar marcadores con estados (verde/rojo/violeta)
  - Verificar estado antes de permitir click
  - Ver ejemplo en: `HOW_TO_USE_BACKEND.md`

- [ ] **Actualizar cada mini-juego (6 activities)**
  - Sopa de Letras (Parada 1)
  - Diferencias (Parada 2)
  - Relacionar (Parada 3)
  - Basura (Parada 4)
  - Pesca (Parada 5)
  - Puzzle (Parada 6)
  
  **En cada una:**
  ```kotlin
  // Al completar juego:
  private fun onJuegoCompletado() {
      lifecycleScope.launch {
          val repository = ParadasRepositoryMejorado(this@Activity)
          val resultado = repository.completarParada(
              usuarioId = UserPreferences(this@Activity).userId,
              paradaId = PARADA_ID, // 1, 2, 3, 4, 5 o 6
              puntuacion = 100,
              tiempoEmpleado = tiempoEnSegundos
          )
          
          when (resultado) {
              is Resource.Success -> {
                  Toast.makeText(this@Activity, "¡Completada!", Toast.LENGTH_SHORT).show()
                  finish()
              }
              else -> {}
          }
      }
  }
  ```

---

### 🎯 **Prioridad MEDIA (Mejoras)**

- [ ] **Crear ViewModels**
  - `PresentacionViewModel.kt`
  - `MapaViewModel.kt`
  - Separar lógica de Activities

- [ ] **Migrar ParadasRepository.kt antiguo**
  - Reemplazar objeto singleton por ParadasRepositoryMejorado
  - Actualizar referencias en toda la app

- [ ] **Añadir Loading States**
  - ProgressBar al cargar paradas
  - Dialog al registrar usuario
  - Shimmer effect (opcional)

- [ ] **WorkManager para Sincronización**
  - Crear `SyncWorker.kt`
  - Configurar sincronización periódica (cada 6 horas)
  - Sincronizar progreso pendiente

- [ ] **Mejorar manejo de errores**
  - Mensajes más descriptivos
  - Dialogs en lugar de Toasts
  - Retry buttons

---

### 🎯 **Prioridad BAJA (Opcional)**

- [ ] **Unit Tests**
  - Tests para Repositories
  - Tests para DAOs
  - Tests para API (mock)

- [ ] **UI Tests**
  - Espresso tests para flujo completo
  - Test de registro
  - Test de completar parada

- [ ] **Analytics**
  - Firebase Analytics
  - Trackear eventos (parada completada, etc.)

- [ ] **Optimizaciones**
  - Paginación en lista de usuarios (backend)
  - Compresión de imágenes
  - Caché de imágenes con Coil

- [ ] **Features Adicionales**
  - Sistema de logros
  - Leaderboard
  - Compartir progreso en redes sociales
  - Notificaciones push

---

## 🔍 **Cómo Verificar que Funciona**

### **Test 1: Registro de Usuario**
```
1. Abrir app
2. Hacer clic en "Registrarse"
3. Ingresar nombre y apellido
4. Verificar en Logcat:
   - "Registro exitoso"
   - UserPreferences guardadas
5. Verificar en backend:
   - http://localhost:5000/admin
   - Ver usuario en tabla de usuarios
```

### **Test 2: Cargar Paradas**
```
1. Después de registrarse
2. Verificar en Logcat:
   - "Paradas cargadas: 6"
3. En Device Explorer (Android Studio):
   - data/data/com.gaizkafrost.mentxuapp/databases/
   - Ver mentxu_database con tabla paradas
```

### **Test 3: Completar Parada**
```
1. Click en parada ACTIVA (roja)
2. Completar mini-juego
3. Verificar en Logcat:
   - "Parada X marcada como completada"
   - "Parada Y activada"
4. Volver al mapa
5. Verificar colores:
   - Parada completada → Verde
   - Parada activa → Roja
```

### **Test 4: Modo Offline**
```
1. Activar modo avión
2. Completar una parada
3. Verificar:
   - Se guarda localmente
   - Toast: "sin conexión, se sincronizará más tarde"
4. Desactivar modo avión
5. Llamar a sincronizarProgresosPendientes()
6. Verificar en backend que se actualizó
```

---

## 📊 **Progreso Estimado**

```
Infraestructura:     ████████████████████ 100%
Capa de Datos:       ████████████████████ 100%
Capa de Red:         ████████████████████ 100%
Repositories:        ████████████████████ 100%
Utilidades:          ████████████████████ 100%
Documentación:       ████████████████████ 100%
                     ─────────────────────
UI Integration:      ░░░░░░░░░░░░░░░░░░░░   0%  ← PRÓXIMO PASO
ViewModels:          ░░░░░░░░░░░░░░░░░░░░   0%
Tests:               ░░░░░░░░░░░░░░░░░░░░   0%
                     ─────────────────────
TOTAL:               ████████████░░░░░░░░  60%
```

---

## ⏱️ **Estimación de Tiempo**

| Tarea | Tiempo Estimado |
|-------|----------------|
| Sincronizar Gradle | 2 min |
| Actualizar Presentacion.kt | 30 min |
| Actualizar MapaActivity.kt | 45 min |
| Actualizar 6 mini-juegos | 2 horas |
| Crear ViewModels | 1.5 horas |
| WorkManager sync | 1 hora |
| Testing manual | 1 hora |
| **TOTAL** | **~7 horas** |

---

## 🎯 **Objetivos por Día**

### **Día 1: Integración Básica** (3-4 horas)
- [ ] Sync Gradle
- [ ] Actualizar Presentacion.kt
- [ ] Actualizar MapaActivity.kt
- [ ] Probar flujo: Registro → Cargar paradas → Ver mapa

### **Día 2: Mini-Juegos** (3-4 horas)
- [ ] Actualizar las 6 activities de mini-juegos
- [ ] Probar flujo completo: Jugar → Completar → Siguiente activa
- [ ] Verificar sincronización con backend

### **Día 3: Mejoras** (2-3 horas)
- [ ] Crear ViewModels
- [ ] Añadir loading states
- [ ] Mejorar manejo de errores
- [ ] Testing exhaustivo

---

## 🚨 **Errores Comunes y Soluciones**

### **Error: "Cannot resolve symbol 'BuildConfig'"**
```kotlin
// Solución:
1. File → Sync Project with Gradle Files
2. Build → Clean Project
3. Build → Rebuild Project
```

### **Error: "Failed to connect to /10.0.2.2:5000"**
```kotlin
// Solución:
1. Verificar que el backend esté corriendo
2. En terminal: python run.py
3. Verificar URL en browser: http://localhost:5000
```

### **Error: "Unresolved reference: UserPreferences"**
```kotlin
// Solución:
1. Build → Make Project
2. Invalidate Caches → Restart
```

### **Error de Room: "Cannot find implementation for AppDatabase"**
```kotlin
// Solución:
1. Verificar que KSP plugin esté en build.gradle.kts
2. Build → Clean → Rebuild
3. Verificar que las entities tengan @Entity
```

---

## 📧 **Contacto y Soporte**

Si necesitas ayuda:
1. Revisar `HOW_TO_USE_BACKEND.md` para ejemplos
2. Revisar `ARCHITECTURE.md` para entender flujos
3. Ver Logcat con filtros: RetrofitClient, ParadasRepository
4. Verificar Database Inspector en Android Studio

---

## 🎉 **¡Próximo Paso Inmediato!**

```bash
# 1. Sincronizar Gradle (MUY IMPORTANTE)
File → Sync Project with Gradle Files

# 2. Verificar que compila
Build → Make Project

# 3. Iniciar backend
cd c:\Users\GaizkaClase\Desktop\MentxuBackend
python run.py

# 4. Seguir los ejemplos en HOW_TO_USE_BACKEND.md
```

---

**Fecha de creación: 15/01/2026**
**Última actualización: 15/01/2026 23:15**

---

¡Todo listo para empezar la integración! 🚀
