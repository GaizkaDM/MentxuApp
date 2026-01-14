# Refactorización del Menú - BaseMenuActivity

## Descripción

Se ha refactorizado el código del menú para evitar duplicación en todas las actividades del proyecto. Anteriormente, cada actividad tenía su propia implementación de `onCreateOptionsMenu` y `onOptionsItemSelected` con código idéntico.

## Solución

Se ha creado una nueva clase base `BaseMenuActivity` que extiende de `AppCompatActivity` y centraliza toda la lógica del menú.

### Ubicación
```
app/src/main/java/com/gaizkafrost/mentxuapp/BaseMenuActivity.kt
```

### Características

- **Implementación centralizada del menú**: Todas las actividades que extienden de `BaseMenuActivity` heredan automáticamente la funcionalidad del menú.
- **Hook personalizable**: `onMenuCreated(menu: Menu)` permite a las actividades hijas personalizar el menú si es necesario (ej: ocultar elementos).
- **Acciones del menú**:
  - `action_mapa`: Navega a `MapaActivity`
  - `action_irten`: Cierra la aplicación completamente

### Uso

#### Caso básico (menú estándar)
```kotlin
class MiActividad : BaseMenuActivity() {
    // Tu código aquí
    // El menú se maneja automáticamente
}
```

#### Caso avanzado (personalizar menú)
```kotlin
class MapaActivity : BaseMenuActivity(), OnMapReadyCallback {
    
    override fun onMenuCreated(menu: Menu) {
        // Ocultar la opción "Mapa" porque ya estamos en el mapa
        menu.findItem(R.id.action_mapa)?.isVisible = false
    }
}
```

## Actividades Refactorizadas

Se han actualizado las siguientes actividades para usar `BaseMenuActivity`:

1. **Presentacion** - Oculta el botón de mapa
2. **MapaActivity** - Oculta el botón de mapa
3. **Huevo_Activity** (Parada 1)
4. **MenuAudio** (Parada 1)
5. **SopaDeLetrasActivity** (Parada 1)
6. **DiferenciasActivity** (Parada 2)
7. **MenuAudio3** (Parada 3)
8. **Relacionar** (Parada 3)
9. **FishingProcessActivity** (Parada 5)
10. **Parada6Activity** (Parada 6)

## Beneficios

✅ **Mantenibilidad**: Cambios en el menú solo requieren modificar un archivo  
✅ **Código limpio**: Reducción de ~20 líneas de código duplicado por actividad  
✅ **Flexibilidad**: Fácil de personalizar cuando sea necesario con `onMenuCreated()`  
✅ **Consistencia**: Comportamiento uniforme del menú en toda la aplicación  

## Estadísticas

- **Archivos creados**: 1 (`BaseMenuActivity.kt`)
- **Archivos modificados**: 10 actividades
- **Líneas de código eliminadas**: ~200 líneas duplicadas
- **Líneas de código de la clase base**: ~37 líneas

## Próximos Pasos

Si en el futuro necesitas:
- **Agregar una nueva opción al menú**: Modifica `BaseMenuActivity` y el archivo `res/menu/top_menu.xml`
- **Crear una nueva actividad con menú**: Simplemente extiende de `BaseMenuActivity`
- **Personalizar el menú en una actividad específica**: Override `onMenuCreated(menu: Menu)`
