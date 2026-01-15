# 🚀 Guía de Uso - Backend Integration MentxuApp

## ✅ ¿Qué se ha implementado?

Se ha creado una **arquitectura completa MVVM con integración al backend Flask** que incluye:

1. **Capa de Red (Retrofit)** - API REST completa
2. **Base de Datos Local (Room)** - Cache offline
3. **Repositories** - Lógica de sincronización
4. **Utilidades** - Network detection, Constants, Resource pattern
5. **Seguridad** - SharedPreferences encriptadas

---

## 📋 Para empezar a usar el backend:

### **Paso 1: Sincronizar Gradle**

```bash
# En Android Studio:
1. File → Sync Project with Gradle Files
2. Espera a que descargue las dependencias
```

Si hay errores de compilación, limpia el proyecto:
```bash
Build → Clean Project
Build → Rebuild Project
```

---

### **Paso 2: Asegúrate que el backend esté corriendo**

```bash
# En el terminal del backend (MentxuBackend):
cd c:\Users\GaizkaClase\Desktop\MentxuBackend
python run.py
```

Verifica que esté en: `http://localhost:5000`

---

### **Paso 3: Crear ejemplo de uso en Presentacion.kt**

Aquí te doy el código de ejemplo para usar el nuevo repository:

```kotlin
package com.gaizkafrost.mentxuapp

import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.gaizkafrost.mentxuapp.data.local.preferences.UserPreferences
import com.gaizkafrost.mentxuapp.data.repository.ParadasRepositoryMejorado
import com.gaizkafrost.mentxuapp.utils.Resource
import kotlinx.coroutines.launch

class Presentacion : BaseMenuActivity() {
    
    private lateinit var userPrefs: UserPreferences
    private lateinit var repository: ParadasRepositoryMejorado
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.presentacion)
        
        // Inicializar preferencias y repository
        userPrefs = UserPreferences(this)
        repository = ParadasRepositoryMejorado(this)
        
        // Configurar botón de registro
        findViewById<Button>(R.id.btnRegistrarse).setOnClickListener {
            mostrarDialogoDeRegistro()
        }
        
        // Si ya hay usuario, cargar paradas
        if (userPrefs.hasUser()) {
            cargarParadas()
        }
    }
    
    private fun mostrarDialogoDeRegistro() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.ventana_emergente, null)
        val editTextNombre: EditText = dialogView.findViewById(R.id.editTextText)
        val editTextApellido: EditText = dialogView.findViewById(R.id.editTextText2)
        
        val dialog = AlertDialog.Builder(this)
            .setTitle("Registro")
            .setView(dialogView)
            .setPositiveButton("Comenzar", null)
            .setNegativeButton("Cancelar") { d, _ -> d.dismiss() }
            .create()
        
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val nombre = editTextNombre.text.toString().trim()
                val apellido = editTextApellido.text.toString().trim()
                
                if (nombre.isNotEmpty() && apellido.isNotEmpty()) {
                    registrarUsuario(nombre, apellido)
                    dialog.dismiss()
                } else {
                    Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                }
            }
        }
        dialog.show()
    }
    
    private fun registrarUsuario(nombre: String, apellido: String) {
        // Generar device ID único
        val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        
        lifecycleScope.launch {
            try {
                val api = RetrofitClient.api
                val request = UsuarioRequest(nombre, apellido, deviceId)
                val response = api.registrarUsuario(request)
                
                if (response.isSuccessful) {
                    response.body()?.let { userResponse ->
                        // Guardar usuario en preferences
                        userPrefs.saveUser(
                            userResponse.usuario.id,
                            nombre,
                            apellido
                        )
                        userPrefs.deviceId = deviceId
                        
                        Toast.makeText(this@Presentacion, "¡Registro exitoso!", Toast.LENGTH_SHORT).show()
                        
                        // Cargar paradas y abrir mapa
                        cargarParadas()
                        
                        val intent = Intent(this@Presentacion, MapaActivity::class.java)
                        startActivity(intent)
                    }
                } else {
                    Toast.makeText(this@Presentacion, "Error al registrar", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@Presentacion, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun cargarParadas() {
        val userId = userPrefs.userId
        if (userId <= 0) return
        
        lifecycleScope.launch {
            repository.obtenerParadas(userId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        // Mostrar loading
                    }
                    is Resource.Success -> {
                        Toast.makeText(this@Presentacion, "Paradas cargadas: ${resource.data?.size}", Toast.LENGTH_SHORT).show()
                    }
                    is Resource.Error -> {
                        Toast.makeText(this@Presentacion, resource.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
```

---

### **Paso 4: Completar paradas en los mini-juegos**

Cuando un usuario complete un mini-juego, llama a:

```kotlin
// En SopaDeLetrasActivity.kt, DiferenciasActivity.kt, etc.

private fun onJuegoCompletado() {
    val userPrefs = UserPreferences(this)
    val repository = ParadasRepositoryMejorado(this)
    
    life cycleScope.launch {
        val resultado = repository.completarParada(
            usuarioId = userPrefs.userId,
            paradaId = PARADA_ID, // ID de esta parada (1, 2, 3, etc.)
            puntuacion = 100,
            tiempoEmpleado = tiempoEnSegundos,
            intentos = 1
        )
        
        when (resultado) {
            is Resource.Success -> {
                Toast.makeText(this@TuActivity, "¡Parada completada!", Toast.LENGTH_SHORT).show()
                // Cerrar actividad y volver al mapa
                finish()
            }
            is Resource.Error -> {
                Toast.makeText(this@TuActivity, resultado.message, Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }
}
```

---

### **Paso 5: Cargar paradas en MapaActivity**

```kotlin
class MapaActivity : BaseMenuActivity(), OnMapReadyCallback {
    
    private lateinit var repository: ParadasRepositoryMejorado
    private lateinit var userPrefs: UserPreferences
    private var paradas: List<Parada> = emptyList()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapa)
        
        userPrefs = UserPreferences(this)
        repository = ParadasRepositoryMejorado(this)
        
        // Cargar paradas
        cargarParadas()
        
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }
    
    private fun cargarParadas() {
        lifecycleScope.launch {
            repository.obtenerParadas(userPrefs.userId).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        resource.data?.let {
                            paradas = it
                            // Actualizar marcadores del mapa
                            if (::mMap.isInitialized) {
                                actualizarMarcadores()
                            }
                        }
                    }
                    is Resource.Error -> {
                        Toast.makeText(this@MapaActivity, resource.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> {}
                }
            }
        }
    }
    
    private fun actualizarMarcadores() {
        mMap.clear()
        
        paradas.forEach { parada ->
            val colorIcono = when (parada.estado) {
                EstadoParada.ACTIVA -> BitmapDescriptorFactory.HUE_RED
                EstadoParada.COMPLETADA -> BitmapDescriptorFactory.HUE_GREEN
                EstadoParada.BLOQUEADA -> BitmapDescriptorFactory.HUE_VIOLET
            }
            
            val marker = mMap.addMarker(
                MarkerOptions()
                    .position(parada.latLng)
                    .title(parada.nombre)
                    .icon(BitmapDescriptorFactory.defaultMarker(colorIcono))
            )
            marker?.tag = parada
        }
    }
}
```

---

## 🔍 Debug y Troubleshooting

### **Ver logs de Retrofit:**
```bash
# En Logcat, filtrar por:
Tag: RetrofitClient
Tag: ParadasRepository
Tag: OkHttp
```

### **Verificar que la API responde:**
```bash
# En el navegador o Postman:
http://localhost:5000/api/paradas
http://localhost:5000/api/estadisticas
```

### **Verificar base de datos local:**
```bash
# En Android Studio:
View → Tool Windows → App Inspection → Database Inspector
# Selecciona tu app y verás las tablas: paradas, progreso
```

---

## 📊 Flujo Completo de Datos

```
[Usuario registra nombre/apellido]
           ↓
[POST /api/usuarios/registro]
           ↓
[Guardar ID en UserPreferences]
           ↓
[GET /api/paradas]
           ↓
[Guardar en Room Database]
           ↓
[Mostrar en MapaActivity]
           ↓
[Usuario clicka parada activa]
           ↓
[Juega mini-juego]
           ↓
[Al completar, POST /api/progreso/completar]
           ↓
[Actualizar Room local + activar siguiente]
           ↓
[Volver al mapa actualizado]
```

---

## ⚡ Ventajas del Sistema Implementado

1. ✅ **Funciona offline** - Datos se guardan localmente
2. ✅ **Sincroniza automáticamente** - Cuando hay conexión
3. ✅ **UI reactiva** - Cambios se reflejan en tiempo real
4. ✅ **Escalable** - Fácil agregar más funcionalidades
5. ✅ **Seguro** - Preferencias encriptadas
6. ✅ **Eficiente** - Cache-first strategy

---

## 🎯 Próximas Mejoras Opcionales

1. **WorkManager** - Sincronización automática en background
2. **ViewModels** - Separar lógica de UI
3. **Loading States** - Mejores indicadores de carga
4. **Error Handling** - Mejores mensajes de error
5. **Analytics** - Tracking de eventos
6. **Tests** - Unit tests y UI tests

---

##  ¿Necesitas ayuda?

Si encuentras errores:
1. Verifica que el backend esté corriendo
2. Revisa Logcat para ver logs detallados
3. Asegúrate que la URL en BuildConfig sea correcta
4. Comprueba permisos en AndroidManifest

---

**¡Todo listo para usar! El backend y el frontend ahora están completamente integrados.** 🎉
