# Navegación y Sincronización Multijugador en Mapas Interactivos de ESCOM

## Descripción del Proyecto

Este proyecto es una aplicación móvil para Android que permite a los usuarios navegar interactivamente por un mapa virtual del campus de ESCOM (Escuela Superior de Cómputo) del IPN. La aplicación incorpora características avanzadas como:

- **Sistema multijugador** con sincronización en tiempo real mediante Bluetooth y un servidor Node.js
- **Múltiples mapas interconectados** que permiten explorar diferentes zonas del campus
- **Transiciones fluidas** entre mapas al alcanzar puntos específicos
- **Minijuegos y elementos interactivos** distribuidos en los diferentes mapas
- **Sincronización de posiciones** entre jugadores que se encuentran en la misma zona

La aplicación está diseñada siguiendo la arquitectura MVVM (Model-View-ViewModel) e implementa patrones de diseño para gestionar las conexiones y la comunicación entre dispositivos.

## Características Principales

### Sistema de Mapas Interconectados

La aplicación está compuesta por diversos mapas interconectados que representan diferentes áreas del campus de ESCOM:

1. **Mapa Principal (GameplayActivity)**: Representa la vista general del campus con acceso a edificios.
2. **Edificio 2 (BuildingNumber2)**: Permite explorar el interior del edificio principal con salones.
3. **Salones 2009 y 2010**: Mapas detallados del interior de los salones.
4. **Cafetería ESCOM**: Incluye un minijuego del "zombie" que persigue al jugador.

### Secuencia Lineal de Mapas Exteriores

Una característica destacada es la secuencia lineal de mapas exteriores que permiten recorrer los alrededores del campus:

1. **EstacionamientoEscom**: Área de estacionamiento con visualización de vehículos y caseta.
2. **TramoAtrasPlaza**: Zona de tránsito con áreas verdes y bancas.
3. **TramoLindavista**: Área con edificios y punto interactivo de venta de comida.
4. **TramoTurismo**: Zona con monumentos y estatuas interactivas.
5. **TramoFuente**: Área final con una fuente central interactiva y un minijuego de "fuente de los deseos".

### Comunicación y Sincronización

- **Bluetooth**: Permite la comunicación directa entre dispositivos cercanos.
- **Servidor Node.js**: Sincroniza las posiciones de todos los jugadores conectados globalmente.
- **Sincronización entre mapas**: Los jugadores pueden verse entre sí cuando están en el mismo mapa.

### Elementos Interactivos

- **Puntos de transición**: Permiten pasar de un mapa a otro al presionar el botón A.
- **Minijuego del Zombie**: En la Cafetería, debes evitar ser atrapado por un zombie durante 60 segundos.
- **Fuente de los Deseos**: En el TramoFuente puedes pedir un deseo y recibir una respuesta personalizada.
- **Monumentos y estatuas**: Puntos de interés con información histórica sobre ESCOM.

## Instrucciones de Instalación y Ejecución

### Requisitos Previos

- Android Studio (versión 2023.3.1 o superior)
- SDK de Android con API nivel 35 o superior
- Dispositivo Android con Bluetooth habilitado y permisos de ubicación (para pruebas reales)
- Java JDK 21 o superior

### Pasos para la Configuración

1. **Clonar el repositorio**:
   ```bash
   git clone https://github.com/tu-usuario/Sensores_ESCOM_V2.git
   cd Sensores_ESCOM_V2
   ```

2. **Abrir el proyecto en Android Studio**:
   - Inicia Android Studio
   - Selecciona "Abrir un proyecto existente"
   - Navega hasta la carpeta del repositorio clonado y selecciona "OK"

3. **Configurar la dirección IP del servidor**:
   - Abre el archivo `OnlineServerManager.kt` ubicado en: 
     `app/src/main/java/ovh/gabrielhuav/sensores_escom_v2/data/map/OnlineServer/OnlineServerManager.kt`
   - Busca la variable `private val serverUrl` y actualiza la dirección IP para que coincida con la de tu servidor Node.js:
     ```kotlin
     private val serverUrl = "ws://TU_IP_AQUI:3000"
     ```

4. **Compilar y ejecutar el proyecto**:
   - Conecta tu dispositivo Android o configura un emulador
   - Haz clic en "Run" (▶️) en Android Studio
   - Selecciona tu dispositivo en la lista

### Modo Multijugador

Para probar el modo multijugador, hay dos opciones:

1. **Solo con Servidor Node.js**:
   - Instala y configura el servidor Node.js (código no incluido en este repositorio)
   - Inicia el servidor con WebSocket activado
   - Múltiples clientes se conectarán al mismo servidor

2. **Con Bluetooth**:
   - En un dispositivo, inicia la aplicación y selecciona "Iniciar Juego"
   - En otro dispositivo, selecciona "Conexión Bluetooth"
   - Selecciona el primer dispositivo de la lista para conectarte

## Uso de la Aplicación

### Navegación Básica

- Usa los botones de dirección (N, S, E, W) para moverte por el mapa
- Presiona el botón A para interactuar con elementos especiales o cambiar de mapa
- El botón B1 puede activar funciones especiales dependiendo del mapa (como iniciar un minijuego)
- El botón BCK o el botón "Home" te permiten regresar al mapa anterior

### Exploración del Campus

La aplicación te permite explorar el campus de ESCOM a través de diversos mapas interconectados:

1. **Mapa Principal → Edificio 2**:
   - Dirígete a las coordenadas (15, 10) y presiona A

2. **Mapa Principal → Estacionamiento**:
   - Dirígete a las coordenadas (25, 5) y presiona A

3. **Secuencia de Mapas Exteriores**:
   - Desde el Estacionamiento, sigue hacia el este para alcanzar el TramoAtrasPlaza
   - Continúa en esa dirección para llegar a TramoLindavista, luego TramoTurismo y finalmente TramoFuente

### Mapas Exteriores en Detalle

#### EstacionamientoEscom
- **Características**: Visualización de espacios de estacionamiento y caseta de vigilancia
- **Elementos interactivos**: Puntos de transición al mapa principal y al TramoAtrasPlaza
- **Coordenadas clave**:
  - (20, 38): Transición al mapa principal
  - (35, 20): Transición al TramoAtrasPlaza

#### TramoAtrasPlaza
- **Características**: Áreas verdes y bancas para descanso
- **Elementos interactivos**:
  - Easter egg en coordenadas (10, 30): Historia sobre la plaza
- **Coordenadas clave**:
  - (5, 20): Regreso al Estacionamiento
  - (35, 20): Transición al TramoLindavista

#### TramoLindavista
- **Características**: Edificios y zona comercial
- **Elementos interactivos**:
  - Puesto de comida en (20, 10): Información sobre tacos de canasta
- **Coordenadas clave**:
  - (5, 20): Regreso al TramoAtrasPlaza
  - (35, 20): Transición al TramoTurismo

#### TramoTurismo
- **Características**: Monumentos y estatuas conmemorativas
- **Elementos interactivos**:
  - Monumento en (10, 20): Historia de ESCOM
  - Estatua en (25, 15): Información sobre innovación
  - Fuente pequeña en (25, 25): Descripción de área de descanso
- **Coordenadas clave**:
  - (5, 20): Regreso al TramoLindavista
  - (35, 20): Transición al TramoFuente

#### TramoFuente
- **Características**: Gran fuente central con bancas alrededor
- **Elementos interactivos**:
  - Centro de la fuente: Minijuego "Fuente de los Deseos"
- **Coordenadas clave**:
  - (5, 20): Regreso al TramoTurismo
  - Centro del mapa: Interacción con la fuente de los deseos

## Dificultades Encontradas y Soluciones

### Sincronización entre Dispositivos

**Problema**: La sincronización de posiciones entre dispositivos conectados por Bluetooth y servidor presentaba pérdidas de datos y desconexiones frecuentes.

**Solución**: Implementamos un sistema de mensajería asíncrona con cola de mensajes en `BluetoothGameManager.kt` y `OnlineServerManager.kt` que intenta retransmitir mensajes perdidos y mantener un registro de dispositivos conectados para reintentar conexiones automáticamente.

### Transiciones entre Mapas

**Problema**: Al cambiar entre mapas, se perdían las posiciones de los jugadores y las conexiones Bluetooth.

**Solución**: Creamos el sistema de persistencia en `GameState` que preserva:
- El estado de conexión actual
- La posición anterior del jugador
- Referencias a dispositivos conectados

Además, implementamos el método `onMapTransitionRequested` en `MapView` para gestionar las transiciones de forma limpia.

### Visualización de Jugadores en el Mapa Correcto

**Problema**: Jugadores de diferentes mapas se veían entre sí causando confusión visual.

**Solución**: Desarrollamos el sistema `MapMatrixProvider` con normalización de nombres de mapas y filtrado de jugadores para que sólo se muestren aquellos que están en el mismo mapa:

```kotlin
// En PlayerManager.kt
val normalizedCurrentMap = MapMatrixProvider.normalizeMapName(currentMap)
val playersToDraw = remotePlayerPositions.entries
    .filter {
        val normalizedPlayerMap = MapMatrixProvider.normalizeMapName(it.value.map)
        normalizedPlayerMap == normalizedCurrentMap
    }
```

### Implementación del Minijuego del Zombie

**Problema**: El zombie no se visualizaba correctamente y su comportamiento era errático durante las pruebas iniciales.

**Solución**: Creamos una clase especializada `ZombieController` que gestiona:
- El ciclo de vida del minijuego
- La lógica de persecución del zombie
- El dibujado del zombie como entidad especial en el mapa

Además, implementamos métodos de depuración para forzar la visualización del zombie y verificar su correcto funcionamiento.

## Conclusiones y Trabajo Futuro

Este proyecto demuestra la implementación exitosa de una aplicación interactiva de navegación por mapas con capacidades multijugador en Android. La arquitectura modular permite una fácil extensión del sistema con nuevos mapas y funcionalidades.

### Posibles mejoras futuras:

1. **Mejora del rendimiento** en dispositivos de gama baja optimizando el dibujado de mapas
2. **Ampliación de mapas** para incluir más zonas del campus
3. **Sistema de misiones** que guíe al jugador a través de los diferentes mapas
4. **Mejora de los minijuegos existentes** e implementación de nuevos minijuegos en diferentes ubicaciones
5. **Integración con servicios en la nube** para persistencia de datos y rankings globales

---


Desarrollado por Gabriel Huerta © 2025. ESCOM-IPN.
