# MentxuApp 🎣

**MentxuApp** es una aplicación móvil educativa y turística diseñada para enriquecer la experiencia de los visitantes en un entorno cultural y natural. A través de una serie de paradas interactivas, juegos educativos y guías de audio, los usuarios descubren la historia y el entorno de una manera divertida y envolvente.

La aplicación combina geolocalización, realidad aumentada (o juegos visuales), y contenido multimedia para ofrecer una experiencia completa.

## 🚀 Características Principales

*   **Ruta Interactiva**: Un mapa (integrado con Mapbox) que guía al usuario a través de diferentes paradas.
*   **Audioguías**: Narraciones inmersivas disponibles en cada parada (Parada 1, 3, 4, etc.).
*   **Minijuegos Educativos**:
    *   **Sopa de Letras**: Encuentra palabras clave relacionadas con el entorno.
    *   **Encuentra las Diferencias**: Observa y aprende detalles visuales.
    *   **Relacionar Conceptos**: Asocia imágenes o términos históricos.
    *   **Juego de Recogida**: Interactúa con el entorno virtual.
    *   **Simulador de Pesca**: "Arrantzaren prozesua" (Parada 5).
    *   **Puzles y Quiz**: Pon a prueba tus conocimientos (Parada 6).
*   **Sistema de Logros y Ranking**: Seguimiento del progreso y puntuaciones.
*   **Multilenguaje**: Soporte para Euskera (idioma principal) y Castellano.

## 🛠️ Stack Tecnológico

El proyecto está construido utilizando tecnologías modernas de desarrollo Android:

*   **Lenguaje**: Kotlin
*   **Arquitectura**: MVVM (Model-View-ViewModel) recomendado.
*   **UI**: XML Layouts / Material Design.
*   **Mapas**: Mapbox Maps SDK v11.
*   **Red**: Retrofit 2 + OkHttp (para comunicación con el backend).
*   **Base de Datos Local**: Room.
*   **Carga de Imágenes**: Coil.
*   **Asincronía**: Kotlin Coroutines.
*   **Inyección de Dependencias**: Manual / Android ViewModel.

## 📋 Requisitos Previos

Para ejecutar este proyecto, necesitas configurar tu entorno de desarrollo con:

*   **Android Studio**: Ladybug (o superior recomendado).
*   **JDK**: Versión 21 (Requerido por el proyecto).
    *   Asegúrate de configurar `org.gradle.java.home` si usas una ruta personalizada.
*   **Android SDK**:
    *   `minSdk`: 29
    *   `targetSdk`: 35
    *   `compileSdk`: 35

## ⚙️ Configuración del Proyecto (IMPORTANTE)

Para que el proyecto compile y ejecute correctamente, **ES NECESARIO** configurar los tokens de acceso de Mapbox y la ruta del SDK de Android en el archivo `local.properties`.

### Crear/Editar `local.properties`

Ubica el archivo `local.properties` en la raíz del proyecto (este archivo **NO** debe subirse al control de versiones). Añade las siguientes líneas:

```properties
# Ruta a tu SDK de Android (se suele configurar automáticamente por Android Studio)
sdk.dir=C\:\\Users\\TU_USUARIO\\AppData\\Local\\Android\\Sdk

# TOKENS DE MAPBOX (Requeridos para el mapa)
# Debes obtener estos tokens de tu cuenta de Mapbox o solicitarlos al administrador del proyecto.

# Token público (comienza con pk.)
MAPBOX_ACCESS_TOKEN=pk.eyJ1IjoieGlrZXI...

# Token secreto/descargas (comienza con sk.) - Necesario para descargar el SDK
MAPBOX_DOWNLOADS_TOKEN=sk.eyJ1IjoieGlrZXI...
```

> **Nota**: Sin el `MAPBOX_DOWNLOADS_TOKEN` configurado correctamente, Gradle fallará al intentar descargar las dependencias de Mapbox.

## 📲 Instalación y Ejecución

1.  **Clonar el repositorio**:
    ```bash
    git clone <URL_DEL_REPOSITORIO>
    ```
2.  **Abrir en Android Studio**:
    *   Selecciona `File` > `Open` y busca la carpeta `MentxuApp`.
3.  **Configurar `local.properties`**:
    *   Sigue los pasos de la sección de [Configuración](#%EF%B8%8F-configuracion-del-proyecto-importante).
4.  **Sincronizar Gradle**:
    *   Presiona el botón "Sync Now" o el icono de elefante en Android Studio.
5.  **Ejecutar**:
    *   Conecta un dispositivo Android (con depuración USB activa) o inicia un emulador.
    *   Presiona el botón "Run" (Triángulo verde ▶️).

## 📄 Estructura del Proyecto

*   `app/src/main/java/com/gaizkafrost/mentxuapp`: Código fuente Kotlin.
    *   Organizado por "Paradas" (módulos funcionales: `Parada1`, `Parada2`, `Mapa`, etc.).
*   `app/src/main/res`: Recursos (layouts, imágenes, strings).
*   `app/src/main/AndroidManifest.xml`: Manifiesto de la aplicación.

---
**MentxuApp** - 2026
