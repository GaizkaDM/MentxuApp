# Parada 2: Encuentra las Diferencias

## 📍 Ubicación
**Itsas-portua (Puerto de Santurtzi)**
- Coordenadas: 43.330417, -3.030722

## 🎮 Descripción del Juego
Un juego de "encontrar las diferencias" donde el jugador debe identificar 7 diferencias entre dos imágenes del puerto de Santurtzi.

## 🖼️ Imágenes
Las imágenes generadas muestran el puerto de Santurtzi con:
- Barcos pesqueros
- Faro con bandera
- Edificios coloridos del puerto
- Gaviotas volando
- Personas en el muelle (con perro)
- Pescadores con redes
- Montañas al fondo

### Diferencias implementadas:
1. **Bandera del faro** - Color diferente
2. **Gaviotas** - Número diferente de aves
3. **Ventana del edificio** - Una ventana faltante
4. **Perro en el muelle** - Color diferente
5. **Gorro del pescador** - Pescador sin gorro
6. **Nube** - Forma diferente (estrella vs. nube normal)
7. **Color del agua** - Área con tono púrpura vs. turquesa

## 📁 Archivos Creados

### Código Kotlin
- **`DiferenciasActivity.kt`**: Actividad principal que gestiona el juego
  - Contador de diferencias encontradas
  - Lógica de finalización del juego
  - Integración con ParadasRepository para marcar como completada

- **`DiferenciasView.kt`**: Vista personalizada que implementa el juego
  - Muestra ambas imágenes lado a lado
  - Detecta toques en las áreas de diferencias
  - Marca visualmente las diferencias encontradas con círculos verdes
  - Gestión de coordenadas relativas para diferentes tamaños de pantalla

### Layout XML
- **`activity_diferencias.xml`**: Layout de la actividad
  - Título del juego
  - Instrucciones para el jugador
  - Contador de diferencias
  - Vista del juego
  - Botón de pista (preparado para futuras implementaciones)

### Recursos
- **`santurtzi_original.png`**: Imagen original del puerto
- **`santurtzi_diferencias.png`**: Imagen con las 7 diferencias

### Configuración
- **`AndroidManifest.xml`**: Se añadió DiferenciasActivity
- **`colors.xml`**: Se añadieron colores necesarios
  - `rojo_activo`
  - `azul_secundario`
  - `fondo_app`
  - `texto_principal`
  - `texto_secundario`

## 🗺️ Integración con el Mapa
La Parada 2 está completamente integrada en `MapaActivity.kt`:
- Al hacer clic en el marcador de "Itsas-portua" (cuando esté activa), se lanza DiferenciasActivity
- Al completar el juego, la parada se marca como completada y se desbloquea la siguiente

## 🎯 Flujo del Juego
1. El jugador ve dos imágenes del puerto lado a lado
2. Debe tocar en la imagen de la derecha donde identifique diferencias
3. Cada diferencia encontrada se marca con un círculo verde
4. El contador se actualiza con cada diferencia encontrada
5. Al encontrar las 7 diferencias:
   - Se muestra mensaje de felicitación
   - La parada se marca como completada
   - Se desbloquea la siguiente parada
   - La actividad se cierra automáticamente después de 2 segundos

## 🔧 Características Técnicas
- **Áreas de toque ampliadas**: Las zonas clicables son generosas para facilitar la jugabilidad en dispositivos móviles
- **Coordenadas relativas**: Todas las áreas de diferencia usan coordenadas relativas (0.0-1.0) para adaptarse a diferentes tamaños de pantalla
- **Feedback visual**: Círculos verdes marcan las diferencias encontradas
- **Toast messages**: Retroalimentación inmediata al encontrar cada diferencia
- **Integración con sistema de paradas**: Usa ParadasRepository para gestionar el progreso

## 🎨 Diseño
- Interfaz limpia y clara
- Instrucciones visibles
- Contador destacado para seguimiento del progreso
- Separador visual entre las dos imágenes
- Colores consistentes con el tema de la app

## ✅ Estado
**✓ COMPLETAMENTE IMPLEMENTADA Y LISTA PARA USAR**

La Parada 2 está completamente funcional y enlazada con el sistema de mapa. El jugador puede acceder a ella desde el mapa cuando complete la Parada 1.
