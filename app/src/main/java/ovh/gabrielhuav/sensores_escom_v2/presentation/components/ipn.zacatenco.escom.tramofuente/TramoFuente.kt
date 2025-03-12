package ovh.gabrielhuav.sensores_escom_v2.presentation.components

import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import ovh.gabrielhuav.sensores_escom_v2.R
import ovh.gabrielhuav.sensores_escom_v2.data.map.BluetoothWebSocketBridge
import ovh.gabrielhuav.sensores_escom_v2.data.map.Bluetooth.BluetoothGameManager
import ovh.gabrielhuav.sensores_escom_v2.data.map.OnlineServer.OnlineServerManager
import ovh.gabrielhuav.sensores_escom_v2.presentation.components.mapview.*

/**
 * Activity para el mapa del Tramo Fuente (mapa final de la secuencia)
 */
class TramoFuente : AppCompatActivity(),
    BluetoothManager.BluetoothManagerCallback,
    BluetoothGameManager.ConnectionListener,
    OnlineServerManager.WebSocketListener,
    MapView.MapTransitionListener {

    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var movementManager: MovementManager
    private lateinit var serverConnectionManager: ServerConnectionManager
    private lateinit var mapView: MapView

    // Componentes de UI
    private lateinit var btnNorth: Button
    private lateinit var btnSouth: Button
    private lateinit var btnEast: Button
    private lateinit var btnWest: Button
    private lateinit var btnBackToHome: Button
    private lateinit var tvBluetoothStatus: TextView
    private lateinit var buttonA: Button

    private lateinit var playerName: String
    private lateinit var bluetoothBridge: BluetoothWebSocketBridge

    // Reutilizamos la misma estructura de GameState que BuildingNumber2
    private var gameState = BuildingNumber2.GameState()

    // Variables para el mini-juego de la fuente
    private var hasCompletedWishingWell = false
    private var activeWish = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fuente)

        try {
            // Inicializar el mapView
            mapView = MapView(
                context = this,
                mapResourceId = R.drawable.tramo_fuente // Usamos la imagen del Tramo Fuente
            )
            findViewById<FrameLayout>(R.id.map_container).addView(mapView)

            // Inicializar componentes
            initializeComponents(savedInstanceState)

            // Esperar a que el mapView esté listo
            mapView.post {
                // Configurar el mapa para el Tramo Fuente
                mapView.setCurrentMap(MapMatrixProvider.MAP_FUENTE, R.drawable.tramo_fuente)

                // Configurar el playerManager
                mapView.playerManager.apply {
                    setCurrentMap(MapMatrixProvider.MAP_FUENTE)
                    localPlayerId = playerName
                    updateLocalPlayerPosition(gameState.playerPosition)
                }

                Log.d(TAG, "Set map to: " + MapMatrixProvider.MAP_FUENTE)

                // Importante: Enviar un update inmediato para que otros jugadores sepan dónde estamos
                if (gameState.isConnected) {
                    serverConnectionManager.sendUpdateMessage(playerName, gameState.playerPosition, MapMatrixProvider.MAP_FUENTE)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en onCreate: ${e.message}")
            Toast.makeText(this, "Error inicializando la actividad.", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun initializeComponents(savedInstanceState: Bundle?) {
        // Obtener datos desde Intent o restaurar el estado guardado
        playerName = intent.getStringExtra("PLAYER_NAME") ?: run {
            Toast.makeText(this, "Nombre de jugador no encontrado.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        if (savedInstanceState == null) {
            // Inicializar desde el Intent
            gameState.isServer = intent.getBooleanExtra("IS_SERVER", false)
            gameState.isConnected = intent.getBooleanExtra("IS_CONNECTED", false) // Preservar estado de conexión
            gameState.playerPosition = intent.getSerializableExtra("INITIAL_POSITION") as? Pair<Int, Int>
                ?: Pair(20, 20)
            hasCompletedWishingWell = false
        } else {
            restoreState(savedInstanceState)
        }

        // Inicializar vistas y gestores de lógica
        initializeViews()
        initializeManagers()
        setupButtonListeners()

        // Inicializar el jugador
        mapView.playerManager.localPlayerId = playerName
        updatePlayerPosition(gameState.playerPosition)

        // Asegurarnos de que nos reconectamos al servidor online
        connectToOnlineServer()
    }

    private fun connectToOnlineServer() {
        // Mostrar estado de conexión
        updateBluetoothStatus("Conectando al servidor online...")

        serverConnectionManager.connectToServer { success ->
            runOnUiThread {
                gameState.isConnected = success

                if (success) {
                    // Enviar mensaje de unión y posición actual
                    serverConnectionManager.onlineServerManager.sendJoinMessage(playerName)

                    // Enviar inmediatamente la posición actual con el mapa correcto
                    serverConnectionManager.sendUpdateMessage(
                        playerName,
                        gameState.playerPosition,
                        MapMatrixProvider.MAP_FUENTE
                    )

                    // Solicitar actualizaciones de posición
                    serverConnectionManager.onlineServerManager.requestPositionsUpdate()

                    updateBluetoothStatus("Conectado al servidor online - Tramo Fuente")
                } else {
                    updateBluetoothStatus("Error al conectar al servidor online")
                }
            }
        }
    }

    private fun initializeViews() {
        // Obtener referencias a los botones de movimiento
        btnNorth = findViewById(R.id.button_north)
        btnSouth = findViewById(R.id.button_south)
        btnEast = findViewById(R.id.button_east)
        btnWest = findViewById(R.id.button_west)
        btnBackToHome = findViewById(R.id.button_back_to_home)
        tvBluetoothStatus = findViewById(R.id.tvBluetoothStatus)
        buttonA = findViewById(R.id.button_a)

        // Cambiar el título para indicar dónde estamos
        tvBluetoothStatus.text = "Tramo Fuente - Conectando..."
    }

    private fun initializeManagers() {
        bluetoothManager = BluetoothManager.getInstance(this, tvBluetoothStatus).apply {
            setCallback(this@TramoFuente)
        }

        bluetoothBridge = BluetoothWebSocketBridge.getInstance()

        val onlineServerManager = OnlineServerManager.getInstance(this).apply {
            setListener(this@TramoFuente)
        }

        serverConnectionManager = ServerConnectionManager(
            context = this,
            onlineServerManager = onlineServerManager
        )

        // Configurar el MovementManager
        movementManager = MovementManager(
            mapView = mapView
        ) { position -> updatePlayerPosition(position) }

        // Configurar listener de transición
        mapView.setMapTransitionListener(this)
    }

    private fun setupButtonListeners() {
        // Configurar los botones de movimiento
        btnNorth.setOnTouchListener { _, event -> handleMovement(event, 0, -1); true }
        btnSouth.setOnTouchListener { _, event -> handleMovement(event, 0, 1); true }
        btnEast.setOnTouchListener { _, event -> handleMovement(event, 1, 0); true }
        btnWest.setOnTouchListener { _, event -> handleMovement(event, -1, 0); true }

        // Botón para volver al mapa anterior
        btnBackToHome.setOnClickListener {
            returnToPreviousMap()
        }

        // Configurar el botón BCK si existe
        findViewById<Button?>(R.id.button_small_2)?.setOnClickListener {
            returnToPreviousMap()
        }

        // Botón A para interactuar
        buttonA.setOnClickListener {
            checkForInteraction()
        }
    }

    private fun checkForInteraction() {
        val currentPosition = gameState.playerPosition
        val transitionPoint = mapView.getMapTransitionPoint(currentPosition.first, currentPosition.second)

        if (transitionPoint != null) {
            // Si hay una transición disponible, iniciarla
            mapView.initiateMapTransition(transitionPoint)
        } else if (isAtFountainCenter(currentPosition)) {
            // El jugador está en el centro de la fuente - activar minijuego
            startWishingWellMinigame()
        } else if (mapView.isInteractivePosition(currentPosition.first, currentPosition.second)) {
            // Alguna otra interacción disponible en esta posición
            Toast.makeText(this, "Interactuando en posición ${currentPosition.first},${currentPosition.second}", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "No hay interacción disponible aquí", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isAtFountainCenter(position: Pair<Int, Int>): Boolean {
        // Centro de la fuente - coordenadas definidas en MapMatrixProvider
        val centerX = MapMatrixProvider.MAP_WIDTH / 2
        val centerY = MapMatrixProvider.MAP_HEIGHT / 2

        // Verificar si estamos en o cerca del centro
        return position.first == centerX && position.second == centerY
    }

    private fun startWishingWellMinigame() {
        if (hasCompletedWishingWell) {
            Toast.makeText(this, "Ya has pedido un deseo en esta fuente.", Toast.LENGTH_SHORT).show()
            return
        }

        // Iniciar el minijuego de la fuente de los deseos
        val options = arrayOf(
            "Desear éxito en los estudios",
            "Desear buena salud",
            "Desear encontrar el amor",
            "Desear un buen trabajo"
        )

        AlertDialog.Builder(this)
            .setTitle("Fuente de los Deseos")
            .setMessage("Has llegado a la fuente principal del campus. Puedes pedir un deseo lanzando una moneda.")
            .setItems(options) { _, which ->
                activeWish = true

                // Animación simple para simular que se lanza una moneda
                Toast.makeText(this, "Lanzando moneda...", Toast.LENGTH_SHORT).show()

                // Usar MovementManager para programar la acción diferida
                movementManager.scheduleDelayedAction(1500) {
                    runOnUiThread {
                        completeMiniGame(options[which])
                    }
                }
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun completeMiniGame(wish: String) {
        if (!activeWish) return

        activeWish = false
        hasCompletedWishingWell = true

        // Generar una respuesta aleatoria
        val responses = arrayOf(
            "La fuente brilla brevemente. Tu deseo de $wish ha sido escuchado.",
            "Sientes una energía especial emanando de la fuente. Tu deseo de $wish está en camino.",
            "Un suave resplandor emerge del agua. El deseo de $wish parece prometedor.",
            "Una brisa ligera sopla mientras la moneda toca el agua. Tu deseo de $wish ha sido aceptado."
        )

        val randomResponse = responses.random()

        AlertDialog.Builder(this)
            .setTitle("¡Deseo Completado!")
            .setMessage(randomResponse)
            .setPositiveButton("¡Genial!") { dialog, _ ->
                dialog.dismiss()

                // Recompensa por completar el minijuego
                Toast.makeText(this, "¡Has completado tu recorrido por ESCOM! Felicidades.", Toast.LENGTH_LONG).show()
            }
            .show()
    }

    private fun returnToPreviousMap() {
        // Volver al Tramo Turismo
        val intent = Intent(this, TramoTurismo::class.java).apply {
            putExtra("PLAYER_NAME", playerName)
            putExtra("IS_SERVER", gameState.isServer)
            putExtra("IS_CONNECTED", gameState.isConnected) // Pasar el estado de conexión
            putExtra("INITIAL_POSITION", Pair(35, 20)) // Posición cercana a la salida hacia este mapa
            putExtra("PREVIOUS_POSITION", intent.getSerializableExtra("PREVIOUS_POSITION"))
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        // Limpiar datos
        mapView.playerManager.cleanup()
        startActivity(intent)
        finish()
    }

    private fun handleMovement(event: MotionEvent, deltaX: Int, deltaY: Int) {
        movementManager.handleMovement(event, deltaX, deltaY)
    }

    private fun updatePlayerPosition(position: Pair<Int, Int>) {
        runOnUiThread {
            gameState.playerPosition = position

            // Actualizar posición del jugador y forzar centrado
            mapView.updateLocalPlayerPosition(position)
            mapView.forceRecenterOnPlayer() // Forzar explícitamente el centrado

            // Enviar actualización a otros jugadores con el mapa específico
            if (gameState.isConnected) {
                // Enviar la posición con el nombre del mapa correcto
                serverConnectionManager.sendUpdateMessage(playerName, position, MapMatrixProvider.MAP_FUENTE)
            }

            // Comprobar si estamos en un punto de transición
            checkForTransition(position)
        }
    }

    private fun checkForTransition(position: Pair<Int, Int>) {
        val transitionMap = mapView.getMapTransitionPoint(position.first, position.second)

        if (transitionMap != null) {
            // Mostrar información sobre la transición disponible
            when (transitionMap) {
                MapMatrixProvider.MAP_TURISMO -> {
                    Toast.makeText(this, "Presiona A para volver al Tramo Turismo", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun restoreState(savedInstanceState: Bundle) {
        gameState.apply {
            isServer = savedInstanceState.getBoolean("IS_SERVER", false)
            isConnected = savedInstanceState.getBoolean("IS_CONNECTED", false)
            playerPosition = savedInstanceState.getSerializable("PLAYER_POSITION") as? Pair<Int, Int>
                ?: Pair(20, 20)
            @Suppress("UNCHECKED_CAST")
            remotePlayerPositions = (savedInstanceState.getSerializable("REMOTE_PLAYER_POSITIONS")
                    as? HashMap<String, BuildingNumber2.GameState.PlayerInfo>)?.toMap() ?: emptyMap()
            remotePlayerName = savedInstanceState.getString("REMOTE_PLAYER_NAME")
        }

        // Restaurar estado del minijuego
        hasCompletedWishingWell = savedInstanceState.getBoolean("HAS_COMPLETED_WISHING_WELL", false)

        // Reconectar si es necesario
        if (gameState.isConnected) {
            connectToOnlineServer()
        }
    }

    // Implementación MapTransitionListener
    override fun onMapTransitionRequested(targetMap: String, initialPosition: Pair<Int, Int>) {
        when (targetMap) {
            MapMatrixProvider.MAP_TURISMO -> {
                returnToPreviousMap()
            }
            else -> {
                Log.d(TAG, "Mapa destino no reconocido: $targetMap")
            }
        }
    }

    // Callbacks de Bluetooth
    override fun onBluetoothDeviceConnected(device: BluetoothDevice) {
        gameState.remotePlayerName = device.name
        updateBluetoothStatus("Conectado a ${device.name}")
    }

    override fun onBluetoothConnectionFailed(error: String) {
        updateBluetoothStatus("Error: $error")
        showToast(error)
    }

    override fun onConnectionComplete() {
        updateBluetoothStatus("Conexión establecida completamente.")
    }

    override fun onConnectionFailed(message: String) {
        onBluetoothConnectionFailed(message)
    }

    override fun onDeviceConnected(device: BluetoothDevice) {
        gameState.remotePlayerName = device.name
    }

    override fun onPositionReceived(device: BluetoothDevice, x: Int, y: Int) {
        runOnUiThread {
            val deviceName = device.name ?: "Unknown"
            mapView.updateRemotePlayerPosition(deviceName, Pair(x, y), MapMatrixProvider.MAP_FUENTE)
            mapView.invalidate()
        }
    }

    // Implementación WebSocketListener
    override fun onMessageReceived(message: String) {
        runOnUiThread {
            try {
                Log.d(TAG, "WebSocket message received: $message")
                val jsonObject = JSONObject(message)

                when (jsonObject.getString("type")) {
                    "positions" -> {
                        val players = jsonObject.getJSONObject("players")
                        players.keys().forEach { playerId ->
                            if (playerId != playerName) {
                                val playerData = players.getJSONObject(playerId.toString())
                                val position = Pair(
                                    playerData.getInt("x"),
                                    playerData.getInt("y")
                                )

                                // Obtener y normalizar el mapa
                                val mapStr = playerData.optString("map", playerData.optString("currentMap", "main"))
                                val normalizedMap = MapMatrixProvider.normalizeMapName(mapStr)

                                // Actualizar el estado
                                gameState.remotePlayerPositions = gameState.remotePlayerPositions +
                                        (playerId to BuildingNumber2.GameState.PlayerInfo(position, normalizedMap))

                                // Obtener el mapa actual normalizado para comparar
                                val currentMap = MapMatrixProvider.normalizeMapName(MapMatrixProvider.MAP_FUENTE)

                                // Solo mostrar jugadores en el mismo mapa
                                if (normalizedMap == currentMap) {
                                    mapView.updateRemotePlayerPosition(playerId, position, normalizedMap)
                                    Log.d(TAG, "Updated remote player $playerId in map $normalizedMap")
                                }
                            }
                        }
                    }
                    "update" -> {
                        val playerId = jsonObject.getString("id")
                        if (playerId != playerName) {
                            val position = Pair(
                                jsonObject.getInt("x"),
                                jsonObject.getInt("y")
                            )

                            // Obtener y normalizar el mapa
                            val mapStr = jsonObject.optString("map", jsonObject.optString("currentmap", "main"))
                            val normalizedMap = MapMatrixProvider.normalizeMapName(mapStr)

                            // Actualizar el estado
                            gameState.remotePlayerPositions = gameState.remotePlayerPositions +
                                    (playerId to BuildingNumber2.GameState.PlayerInfo(position, normalizedMap))

                            // Obtener el mapa actual normalizado para comparar
                            val currentMap = MapMatrixProvider.normalizeMapName(MapMatrixProvider.MAP_FUENTE)

                            // Solo mostrar jugadores en el mismo mapa
                            if (normalizedMap == currentMap) {
                                mapView.updateRemotePlayerPosition(playerId, position, normalizedMap)
                                Log.d(TAG, "Updated remote player $playerId in map $normalizedMap")
                            }
                        }
                    }
                    "join" -> {
                        // Un jugador se unió, solicitar actualización de posiciones
                        serverConnectionManager.onlineServerManager.requestPositionsUpdate()

                        // Enviar nuestra posición actual para que el nuevo jugador nos vea
                        serverConnectionManager.sendUpdateMessage(
                            playerName,
                            gameState.playerPosition,
                            MapMatrixProvider.MAP_FUENTE
                        )
                    }
                    "disconnect" -> {
                        // Manejar desconexión de jugador
                        val disconnectedId = jsonObject.getString("id")
                        if (disconnectedId != playerName) {
                            gameState.remotePlayerPositions = gameState.remotePlayerPositions - disconnectedId
                            mapView.removeRemotePlayer(disconnectedId)
                            Log.d(TAG, "Player disconnected: $disconnectedId")
                        }
                    }
                }
                mapView.invalidate()
            } catch (e: Exception) {
                Log.e(TAG, "Error processing message: ${e.message}")
            }
        }
    }

    private fun updateBluetoothStatus(status: String) {
        runOnUiThread {
            tvBluetoothStatus.text = status
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.apply {
            putBoolean("IS_SERVER", gameState.isServer)
            putBoolean("IS_CONNECTED", gameState.isConnected)
            putSerializable("PLAYER_POSITION", gameState.playerPosition)
            putSerializable("REMOTE_PLAYER_POSITIONS", HashMap(gameState.remotePlayerPositions))
            putString("REMOTE_PLAYER_NAME", gameState.remotePlayerName)
            putBoolean("HAS_COMPLETED_WISHING_WELL", hasCompletedWishingWell)
        }
    }

    override fun onResume() {
        super.onResume()
        movementManager.setPosition(gameState.playerPosition)

        // Simplemente intentar reconectar si estamos en estado conectado
        if (gameState.isConnected) {
            connectToOnlineServer()
        }

        // Reenviar nuestra posición para asegurar que todos nos vean
        if (gameState.isConnected) {
            serverConnectionManager.sendUpdateMessage(
                playerName,
                gameState.playerPosition,
                MapMatrixProvider.MAP_FUENTE
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bluetoothManager.cleanup()
    }

    override fun onPause() {
        super.onPause()
        movementManager.stopMovement()
    }

    companion object {
        private const val TAG = "TramoFuente"
    }
}