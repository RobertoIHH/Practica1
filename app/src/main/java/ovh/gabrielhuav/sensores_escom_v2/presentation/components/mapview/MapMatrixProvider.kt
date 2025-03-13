package ovh.gabrielhuav.sensores_escom_v2.presentation.components.mapview

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log

/**
 * Provee matrices específicas para cada mapa del juego.
 * Cada mapa tiene su propia configuración de colisiones y puntos interactivos.
 */
class MapMatrixProvider {
    companion object {
        // Constantes compartidas para tipos de celdas
        const val INTERACTIVE = 0
        const val WALL = 1
        const val PATH = 2
        const val INACCESSIBLE = 3

        // Tamaño estándar de la matriz
        const val MAP_WIDTH = 40
        const val MAP_HEIGHT = 40

        // Constantes para los mapas existentes
        const val MAP_MAIN = "escom_main"
        const val MAP_BUILDING2 = "escom_building2"
        const val MAP_SALON2009 = "escom_salon2009"
        const val MAP_SALON2010 = "escom_salon2010"
        const val MAP_CAFETERIA = "escom_cafeteria"

        // Constantes para los nuevos mapas (secuencia lineal)
        const val MAP_ESTACIONAMIENTO = "EstacionamientoEscom"
        const val MAP_TRAS_PLAZA = "TramoAtrasPlaza"
        const val MAP_LINDAVISTA = "TramoLindavista"
        const val MAP_TURISMO = "TramoTurismo"
        const val MAP_FUENTE = "TramoFuente"

        fun normalizeMapName(mapName: String?): String {
            if (mapName.isNullOrBlank()) return MAP_MAIN

            val lowerMap = mapName.lowercase()

            return when {
                // Mapas existentes
                lowerMap == "main" -> MAP_MAIN
                lowerMap == "map_main" -> MAP_MAIN
                lowerMap.contains("main") && !lowerMap.contains("building") -> MAP_MAIN

                lowerMap.contains("building2") || lowerMap.contains("edificio2") -> MAP_BUILDING2

                lowerMap.contains("2009") || lowerMap.contains("salon2009") -> MAP_SALON2009
                lowerMap.contains("2010") || lowerMap.contains("salon2010") -> MAP_SALON2010

                lowerMap.contains("cafe") || lowerMap.contains("cafeteria") -> MAP_CAFETERIA

                // Nuevos mapas
                lowerMap.contains("estacionamiento") -> MAP_ESTACIONAMIENTO
                lowerMap.contains("plaza") || lowerMap.contains("atras") -> MAP_TRAS_PLAZA
                lowerMap.contains("linda") -> MAP_LINDAVISTA
                lowerMap.contains("turismo") -> MAP_TURISMO
                lowerMap.contains("fuente") -> MAP_FUENTE

                // Si no coincide con ninguno de los anteriores, devolver el original
                else -> mapName
            }
        }

        // Puntos de transición entre mapas existentes
        val MAIN_TO_BUILDING2_POSITION = Pair(15, 10)
        val BUILDING2_TO_MAIN_POSITION = Pair(5, 5)
        val BUILDING2_TO_SALON2009_POSITION = Pair(15, 16)
        val SALON2009_TO_BUILDING2_POSITION = Pair(1, 20)
        val BUILDING2_TO_SALON2010_POSITION = Pair(20, 20)
        val MAIN_TO_SALON2010_POSITION = Pair(25, 25)
        val SALON2010_TO_BUILDING2_POSITION = Pair(5, 5)
        val SALON2010_TO_MAIN_POSITION = Pair(1, 1)
        val MAIN_TO_CAFETERIA_POSITION = Pair(2, 2)
        val CAFETERIA_TO_MAIN_POSITION = Pair(1, 1)

        // Puntos de transición para los nuevos mapas
        // Del mapa principal al primer mapa (Estacionamiento)
        val MAIN_TO_ESTACIONAMIENTO_POSITION = Pair(25, 5)
        val ESTACIONAMIENTO_TO_MAIN_POSITION = Pair(20, 38)

        // Del Estacionamiento al segundo mapa (Tramo Atrás Plaza)
        val ESTACIONAMIENTO_TO_PLAZA_POSITION = Pair(35, 20)
        val PLAZA_TO_ESTACIONAMIENTO_POSITION = Pair(5, 20)

        // Del Tramo Atrás Plaza al tercer mapa (Tramo Lindavista)
        val PLAZA_TO_LINDAVISTA_POSITION = Pair(35, 20)
        val LINDAVISTA_TO_PLAZA_POSITION = Pair(5, 20)

        // Del Tramo Lindavista al cuarto mapa (Tramo Turismo)
        val LINDAVISTA_TO_TURISMO_POSITION = Pair(35, 20)
        val TURISMO_TO_LINDAVISTA_POSITION = Pair(5, 20)

        // Del Tramo Turismo al quinto mapa (Tramo Fuente)
        val TURISMO_TO_FUENTE_POSITION = Pair(35, 20)
        val FUENTE_TO_TURISMO_POSITION = Pair(5, 20)

        /**
         * Obtiene la matriz para el mapa especificado
         */
        fun getMatrixForMap(mapId: String): Array<Array<Int>> {
            return when (mapId) {
                MAP_MAIN -> createMainMapMatrix()
                MAP_BUILDING2 -> createBuilding2Matrix()
                MAP_SALON2009 -> createSalon2009Matrix()
                MAP_SALON2010 -> createSalon2010Matrix()
                MAP_CAFETERIA -> createCafeESCOMMatrix()
                // Nuevos mapas
                MAP_ESTACIONAMIENTO -> createEstacionamientoMatrix()
                MAP_TRAS_PLAZA -> createPlazaMatrix()
                MAP_LINDAVISTA -> createLindavistaMatrix()
                MAP_TURISMO -> createTurismoMatrix()
                MAP_FUENTE -> createFuenteMatrix()
                else -> createDefaultMatrix() // Mapa por defecto
            }
        }

        /**
         * Matriz para el mapa principal del campus
         * Modificada para incluir la entrada al Estacionamiento
         */
        private fun createMainMapMatrix(): Array<Array<Int>> {
            val matrix = Array(MAP_HEIGHT) { Array(MAP_WIDTH) { PATH } }

            // Configuración de bordes
            for (i in 0 until MAP_HEIGHT) {
                for (j in 0 until MAP_WIDTH) {
                    // Bordes exteriores
                    if (i == 0 || i == MAP_HEIGHT - 1 || j == 0 || j == MAP_WIDTH - 1) {
                        matrix[i][j] = WALL
                    }
                    // Zonas interactivas (edificios, entradas)
                    else if (i == 10 && j == 15) {
                        matrix[i][j] = INTERACTIVE // Entrada al edificio 2
                    }
                    // Obstáculos (árboles, bancas, etc)
                    else if (i % 7 == 0 && j % 8 == 0) {
                        matrix[i][j] = INACCESSIBLE
                    }
                    // Caminos especiales
                    else if ((i % 5 == 0 || j % 5 == 0) && i > 5 && j > 5) {
                        matrix[i][j] = PATH
                    }

                }
            }

            // Áreas de juego específicas
            // Zona central despejada
            for (i in 15..25) {
                for (j in 15..25) {
                    matrix[i][j] = PATH
                }
            }

            // Añadir punto interactivo para el nuevo mapa de Estacionamiento
            matrix[5][25] = INTERACTIVE // Entrada al Estacionamiento de ESCOM

            return matrix
        }

        /**
         * Matriz para el edificio 2
         * (mantener implementación existente)
         */
        private fun createBuilding2Matrix(): Array<Array<Int>> {
            // Crear matriz con PATH (caminable) por defecto
            val matrix = Array(MAP_HEIGHT) { Array(MAP_WIDTH) { PATH } }

            // Constantes para dimensiones del edificio
            val roomTop = 8           // Posición superior de las aulas
            val roomHeight = 8        // Altura de las aulas (más grandes)
            val roomBottom = roomTop + roomHeight
            val corridorTop = roomBottom + 1
            val corridorHeight = 3    // Altura del pasillo principal
            val corridorBottom = corridorTop + corridorHeight

            // Número de aulas + baño
            val numRooms = 7
            val roomWidth = (MAP_WIDTH - 2) / numRooms

            // Crear bordes del edificio
            // Borde superior del edificio
            for (x in 0 until MAP_WIDTH) {
                matrix[roomTop - 1][x] = WALL
            }

            // Borde inferior del edificio
            if (corridorBottom + 1 < MAP_HEIGHT) {
                for (x in 0 until MAP_WIDTH) {
                    matrix[corridorBottom + 1][x] = WALL
                }
            }

            // Bordes laterales del edificio
            for (y in roomTop - 1..corridorBottom + 1) {
                if (y < MAP_HEIGHT) {
                    matrix[y][0] = WALL
                    if (MAP_WIDTH - 1 < MAP_WIDTH) {
                        matrix[y][MAP_WIDTH - 1] = WALL
                    }
                }
            }

            // Crear divisiones verticales entre aulas
            for (i in 0..numRooms) {
                val x = 1 + (i * roomWidth)
                if (x < MAP_WIDTH) {
                    for (y in roomTop until roomBottom) {
                        matrix[y][x] = WALL
                    }
                }
            }

            // Bordes horizontales de las aulas
            for (x in 1 until MAP_WIDTH - 1) {
                // Borde superior de las aulas
                matrix[roomTop][x] = WALL

                // Borde inferior de las aulas (justo encima del pasillo)
                matrix[roomBottom][x] = WALL
            }

            // Crear el área de escaleras (entre las aulas 3 y 4)
            val stairsIndex = 3
            val stairsX = 1 + (stairsIndex * roomWidth)

            // Limpiar el área de escaleras
            for (y in roomTop + 1 until roomBottom) {
                for (x in stairsX until stairsX + roomWidth) {
                    if (x < MAP_WIDTH) {
                        matrix[y][x] = PATH
                    }
                }
            }

            // Hacer las escaleras interactivas
            val stairsCenterX = stairsX + roomWidth/2
            val stairsCenterY = roomTop + roomHeight/2

            // Definir área interactiva alrededor del centro
            for (y in stairsCenterY - 1..stairsCenterY + 1) {
                for (x in stairsCenterX - 1..stairsCenterX + 1) {
                    if (x >= 0 && x < MAP_WIDTH && y >= 0 && y < MAP_HEIGHT) {
                        matrix[y][x] = INTERACTIVE
                    }
                }
            }

            // Crear puertas para cada aula
            for (i in 0 until numRooms) {
                if (i == stairsIndex) continue // Saltar escaleras

                val doorX = 1 + (i * roomWidth) + (roomWidth / 2)
                if (doorX < MAP_WIDTH) {
                    matrix[roomBottom][doorX] = PATH

                    // Hacer las puertas más anchas para facilitar el acceso
                    if (doorX - 1 >= 0) matrix[roomBottom][doorX - 1] = PATH
                    if (doorX + 1 < MAP_WIDTH) matrix[roomBottom][doorX + 1] = PATH
                }
            }

            // Crear pasillo principal - amplio y completamente caminable
            for (y in corridorTop until corridorTop + corridorHeight) {
                if (y < MAP_HEIGHT) {
                    for (x in 1 until MAP_WIDTH - 1) {
                        matrix[y][x] = PATH
                    }
                }
            }

            // Añadir puntos interactivos para las transiciones

            // Entrada a la sala 2009 (en el pasillo, centrado)
            val corridorCenterY = corridorTop + corridorHeight/2

            // Múltiples puntos interactivos a lo largo del pasillo
            val interactivePoints = listOf(
                (MAP_WIDTH / 2),
                (MAP_WIDTH / 3),
                (2 * MAP_WIDTH / 3),
                stairsCenterX
            )

            for (x in interactivePoints) {
                if (x >= 0 && x < MAP_WIDTH && corridorCenterY >= 0 && corridorCenterY < MAP_HEIGHT) {
                    matrix[corridorCenterY][x] = INTERACTIVE
                }
            }

            // Salida al mapa principal (lado izquierdo)
            if (corridorCenterY < MAP_HEIGHT) {
                matrix[corridorCenterY][2] = INTERACTIVE
            }

            // Hacer el interior de las aulas navegable
            for (i in 0 until numRooms) {
                if (i == stairsIndex) continue  // Saltar escaleras

                val roomStartX = 1 + (i * roomWidth) + 1
                val roomEndX = 1 + ((i + 1) * roomWidth) - 1

                for (y in roomTop + 1 until roomBottom) {
                    for (x in roomStartX until roomEndX + 1) {
                        if (x < MAP_WIDTH) {
                            matrix[y][x] = PATH
                        }
                    }
                }
            }

            return matrix
        }

        /**
         * Matriz para el salón 2009
         * (mantener implementación existente)
         */
        private fun createSalon2009Matrix(): Array<Array<Int>> {
            val matrix = Array(MAP_HEIGHT) { Array(MAP_WIDTH) { WALL } }

            // Dimensiones del aula
            val roomWidth = 30
            val roomHeight = 25
            val startX = 5
            val startY = 5

            // Interior del salón (espacio abierto)
            for (i in startY until startY + roomHeight) {
                for (j in startX until startX + roomWidth) {
                    matrix[i][j] = PATH
                }
            }

            // Puerta de salida hacia el edificio 2 (lado izquierdo)
            matrix[startY + roomHeight/2][1] = INTERACTIVE

            // Pizarrón (pared frontal)
            for (j in startX + 2 until startX + roomWidth - 2) {
                matrix[startY + 1][j] = INACCESSIBLE
            }
            // Centro del pizarrón es interactivo
            matrix[startY + 1][startX + roomWidth/2] = INTERACTIVE

            // Escritorio del profesor
            for (j in startX + 10 until startX + 20) {
                for (i in startY + 3 until startY + 6) {
                    matrix[i][j] = INACCESSIBLE
                }
            }

            // Filas de pupitres para estudiantes
            for (row in 0 until 4) {
                val rowY = startY + 8 + (row * 4)

                // 5 pupitres por fila
                for (desk in 0 until 5) {
                    val deskX = startX + 3 + (desk * 5)

                    // Cada pupitre ocupa 3x2
                    for (i in rowY until rowY + 2) {
                        for (j in deskX until deskX + 3) {
                            matrix[i][j] = INACCESSIBLE
                        }
                    }
                }
            }

            return matrix
        }

        /**
         * Matriz para el salón 2010
         * (mantener implementación existente)
         */
        private fun createSalon2010Matrix(): Array<Array<Int>> {
            val matrix = Array(MAP_HEIGHT) { Array(MAP_WIDTH) { PATH } }

            // Configuración de bordes
            for (i in 0 until MAP_HEIGHT) {
                for (j in 0 until MAP_WIDTH) {
                    // Bordes exteriores
                    if (i == 0 || i == MAP_HEIGHT - 1 || j == 0 || j == MAP_WIDTH - 1) {
                        matrix[i][j] = WALL
                    }
                    // Zonas interactivas (edificios, entradas)
                    else if (i == 10 && j == 15) {
                        matrix[i][j] = INTERACTIVE // Entrada al edificio 2
                    }
                    // Obstáculos (árboles, bancas, etc)
                    else if (i % 7 == 0 && j % 8 == 0) {
                        matrix[i][j] = INACCESSIBLE
                    }
                    // Caminos especiales
                    else if ((i % 5 == 0 || j % 5 == 0) && i > 5 && j > 5) {
                        matrix[i][j] = PATH
                    }
                }
            }

            // Áreas de juego específicas
            // Zona central despejada
            for (i in 15..25) {
                for (j in 15..25) {
                    matrix[i][j] = PATH
                }
            }

            return matrix
        }

        /**
         * Matriz para la cafetería
         * (mantener implementación existente)
         */
        private fun createCafeESCOMMatrix(): Array<Array<Int>> {
            val matrix = Array(MAP_HEIGHT) { Array(MAP_WIDTH) { PATH } }

            // Definición de constantes para mejorar legibilidad
            val PARED = WALL
            val CAMINO = PATH
            val BANCA = INACCESSIBLE
            val INTERACTIVO = INTERACTIVE

            // Bordes exteriores - paredes del restaurante
            for (i in 0 until MAP_HEIGHT) {
                for (j in 0 until MAP_WIDTH) {
                    // Bordes exteriores
                    if (i == 0 || i == MAP_HEIGHT - 1 || j == 0 || j == MAP_WIDTH - 1) {
                        matrix[i][j] = PARED
                    }
                }
            }

            // COCINA (esquina superior izquierda)
            for (i in 2..8) {
                for (j in 2..15) {
                    if (i == 2 || i == 8 || j == 2 || j == 15) {
                        matrix[i][j] = PARED // Paredes de la cocina
                    }
                }
            }
            // Mostrador de la cocina
            for (i in 4..6) {
                for (j in 4..13) {
                    matrix[i][j] = BANCA
                }
            }

            // MESAS/BANCAS LARGAS (3 filas de 3 mesas cada una)
            // Primera fila de mesas
            for (row in 0..2) {
                for (col in 0..2) {
                    // Cada mesa es un rectángulo
                    val baseI = 12 + (row * 8)
                    val baseJ = 10 + (col * 10)

                    for (i in baseI..baseI+2) {
                        for (j in baseJ..baseJ+8) {
                            matrix[i][j] = BANCA
                        }
                    }
                }
            }

            // CAJA (parte inferior)
            for (i in 30..33) {
                for (j in 15..19) {
                    matrix[i][j] = BANCA
                }
            }

            // ENTRADA
            for (i in 37..38) {
                for (j in 15..25) {
                    matrix[i][j] = INTERACTIVO
                }
            }

            // Agregar elementos interactivos: Tacos, Burritos, Guacamole y Chile
            // Tacos (representados como puntos interactivos)
            matrix[12][8] = INTERACTIVO
            matrix[12][32] = INTERACTIVO
            matrix[28][8] = INTERACTIVO
            matrix[28][32] = INTERACTIVO

            // Burritos
            matrix[12][33] = INTERACTIVO
            matrix[28][33] = INTERACTIVO

            // Guacamole
            matrix[20][8] = INTERACTIVO

            // Chile
            matrix[20][32] = INTERACTIVO

            return matrix
        }

        /**
         * NUEVO MAPA: Estacionamiento de ESCOM
         */
        private fun createEstacionamientoMatrix(): Array<Array<Int>> {
            val matrix = Array(MAP_HEIGHT) { Array(MAP_WIDTH) { WALL } } // Todo es muro por defecto

            // Área del estacionamiento (caminable)
            for (i in 5 until MAP_HEIGHT-5) {
                for (j in 5 until MAP_WIDTH-5) {
                    matrix[i][j] = PATH
                }
            }

            // Líneas de aparcamiento (obstáculos)
            for (row in 0..3) {
                val rowY = 10 + (row * 7)

                // Crear líneas horizontales de autos estacionados
                for (j in 8 until MAP_WIDTH-8) {
                    if (j % 5 == 0) { // Espaciado entre autos
                        matrix[rowY][j] = INACCESSIBLE
                        matrix[rowY+1][j] = INACCESSIBLE
                        matrix[rowY+2][j] = INACCESSIBLE
                    }
                }
            }

            // Caseta de vigilancia (obstáculo)
            for (i in 30..33) {
                for (j in 15..20) {
                    matrix[i][j] = INACCESSIBLE
                }
            }

            // Punto interactivo para salir al mapa principal
            matrix[38][20] = INTERACTIVE

            // Punto interactivo para ir al siguiente mapa (TramoAtrasPlaza)
            matrix[20][35] = INTERACTIVE

            return matrix
        }

        /**
         * NUEVO MAPA: Tramo Atrás Plaza
         */
        private fun createPlazaMatrix(): Array<Array<Int>> {
            val matrix = Array(MAP_HEIGHT) { Array(MAP_WIDTH) { WALL } } // Todo es muro por defecto

            // Crear un camino principal que atraviese el mapa
            for (i in 18..22) { // Camino horizontal en el centro
                for (j in 0 until MAP_WIDTH) {
                    matrix[i][j] = PATH
                }
            }

            // Crear áreas verdes (obstáculos)
            for (i in 5..15) {
                for (j in 5..15) {
                    matrix[i][j] = INACCESSIBLE // Área verde superior izquierda
                }
            }

            for (i in 25..35) {
                for (j in 25..35) {
                    matrix[i][j] = INACCESSIBLE // Área verde inferior derecha
                }
            }

            // Bancas en el camino (obstáculos pequeños)
            for (j in 10..30 step 10) {
                matrix[17][j] = INACCESSIBLE
                matrix[23][j] = INACCESSIBLE
            }

            // Punto interactivo para regresar al Estacionamiento
            matrix[20][5] = INTERACTIVE

            // Punto interactivo para ir al siguiente mapa (TramoLindavista)
            matrix[20][35] = INTERACTIVE

            // Añadir un easter egg interactivo
            matrix[10][30] = INTERACTIVE

            return matrix
        }

        /**
         * NUEVO MAPA: Tramo Lindavista
         */
        private fun createLindavistaMatrix(): Array<Array<Int>> {
            val matrix = Array(MAP_HEIGHT) { Array(MAP_WIDTH) { PATH } } // Todo es camino por defecto

            // Bordes del mapa
            for (i in 0 until MAP_HEIGHT) {
                for (j in 0 until MAP_WIDTH) {
                    if (i == 0 || i == MAP_HEIGHT-1 || j == 0 || j == MAP_WIDTH-1) {
                        matrix[i][j] = WALL
                    }
                }
            }

            // Edificios en ambos lados del camino (obstáculos)
            for (i in 5..15) {
                for (j in 5..15) {
                    matrix[i][j] = INACCESSIBLE // Edificio superior izquierdo
                }
            }

            for (i in 5..15) {
                for (j in 25..35) {
                    matrix[i][j] = INACCESSIBLE // Edificio superior derecho
                }
            }

            for (i in 25..35) {
                for (j in 5..15) {
                    matrix[i][j] = INACCESSIBLE // Edificio inferior izquierdo
                }
            }

            for (i in 25..35) {
                for (j in 25..35) {
                    matrix[i][j] = INACCESSIBLE // Edificio inferior derecho
                }
            }

            // Crear un puesto de comida (interactivo)
            matrix[20][10] = INTERACTIVE

            // Punto interactivo para regresar al Tramo Atrás Plaza
            matrix[20][5] = INTERACTIVE

            // Punto interactivo para ir al siguiente mapa (TramoTurismo)
            matrix[20][35] = INTERACTIVE

            return matrix
        }

        /**
         * NUEVO MAPA: Tramo Turismo
         */
        private fun createTurismoMatrix(): Array<Array<Int>> {
            val matrix = Array(MAP_HEIGHT) { Array(MAP_WIDTH) { WALL } } // Todo es muro por defecto

            // Crear un camino principal en forma de U
            for (j in 5 until MAP_WIDTH-5) {
                matrix[5][j] = PATH // Camino horizontal superior
            }

            for (i in 5..35) {
                matrix[i][5] = PATH // Camino vertical izquierdo
                matrix[i][35] = PATH // Camino vertical derecho
            }

            for (j in 5 until MAP_WIDTH-5) {
                matrix[35][j] = PATH // Camino horizontal inferior
            }

            // Crear caminos transversales
            for (i in 15..25) {
                for (j in 5..35) {
                    matrix[i][j] = PATH // Camino ancho en el centro
                }
            }

            // Añadir elementos turísticos (interactivos)
            matrix[10][20] = INTERACTIVE // Monumento
            matrix[25][15] = INTERACTIVE // Estatua
            matrix[25][25] = INTERACTIVE // Fuente pequeña

            // Punto interactivo para regresar al Tramo Lindavista
            matrix[5][20] = INTERACTIVE

            // Punto interactivo para ir al último mapa (TramoFuente)
            matrix[35][20] = INTERACTIVE

            return matrix
        }

        /**
         * NUEVO MAPA: Tramo Fuente
         */
        private fun createFuenteMatrix(): Array<Array<Int>> {
            val matrix = Array(MAP_HEIGHT) { Array(MAP_WIDTH) { PATH } } // Todo es camino por defecto

            // Bordes del mapa
            for (i in 0 until MAP_HEIGHT) {
                for (j in 0 until MAP_WIDTH) {
                    if (i == 0 || i == MAP_HEIGHT-1 || j == 0 || j == MAP_WIDTH-1) {
                        matrix[i][j] = WALL
                    }
                }
            }

            // Fuente grande en el centro (obstáculo circular)
            val centerX = MAP_WIDTH / 2
            val centerY = MAP_HEIGHT / 2
            val radius = 8

            for (i in 0 until MAP_HEIGHT) {
                for (j in 0 until MAP_WIDTH) {
                    val distanceSquared = (i - centerY) * (i - centerY) + (j - centerX) * (j - centerX)
                    if (distanceSquared < radius * radius) {
                        matrix[i][j] = INACCESSIBLE // Interior de la fuente
                    }
                }
            }

            // Caminos alrededor de la fuente
            for (i in centerY-radius-2..centerY+radius+2) {
                for (j in centerX-radius-2..centerX+radius+2) {
                    if (i >= 0 && i < MAP_HEIGHT && j >= 0 && j < MAP_WIDTH) {
                        val distanceSquared = (i - centerY) * (i - centerY) + (j - centerX) * (j - centerX)
                        if (distanceSquared >= radius * radius && distanceSquared <= (radius+2) * (radius+2)) {
                            matrix[i][j] = PATH // Camino alrededor de la fuente
                        }
                    }
                }
            }

            // Bancas en los bordes (obstáculos pequeños)
            for (angle in 0 until 360 step 45) {
                val radians = Math.toRadians(angle.toDouble())
                val benchX = centerX + ((radius + 4) * Math.cos(radians)).toInt()
                val benchY = centerY + ((radius + 4) * Math.sin(radians)).toInt()

                if (benchX >= 0 && benchX < MAP_WIDTH && benchY >= 0 && benchY < MAP_HEIGHT) {
                    matrix[benchY][benchX] = INACCESSIBLE
                }
            }

            // Punto interactivo para regresar al Tramo Turismo
            matrix[5][20] = INTERACTIVE

            // Punto interactivo en medio de la fuente (easter egg/minijuego)
            matrix[centerY][centerX] = INTERACTIVE

            return matrix
        }

        /**
         * Matriz predeterminada para cualquier otro mapa
         */
        private fun createDefaultMatrix(): Array<Array<Int>> {
            val matrix = Array(MAP_HEIGHT) { Array(MAP_WIDTH) { PATH } }

            // Borde simple
            for (i in 0 until MAP_HEIGHT) {
                for (j in 0 until MAP_WIDTH) {
                    if (i == 0 || i == MAP_HEIGHT - 1 || j == 0 || j == MAP_WIDTH - 1) {
                        matrix[i][j] = WALL
                    }
                }
            }

            return matrix
        }

        /**
         * Comprueba si la coordenada especificada es un punto de transición entre mapas
         * Actualizado para incluir los nuevos mapas
         */
        fun isMapTransitionPoint(mapId: String, x: Int, y: Int): String? {
            // Imprimimos para depuración
            Log.d("MapTransition", "Checking transition at $mapId: ($x, $y)")

            // Transiciones existentes - mapa principal y edificios
            if (mapId == MAP_MAIN) {
                // Puntos de transición a edificios existentes
                if (x == 15 && y == 10) return MAP_BUILDING2
                if (x == 33 && y == 34) return MAP_CAFETERIA

                // Punto nuevo para transición al Estacionamiento
                if (x == 25 && y == 5) return MAP_ESTACIONAMIENTO
            }

            if (mapId == MAP_BUILDING2) {
                // Si estamos en o cerca de las coordenadas (15,16) o cualquiera de las alternativas
                val nearCenter = (x >= 14 && x <= 16 && y >= 15 && y <= 17)
                val alternative1 = (x == 20 && y == 20)
                val alternative2 = (x == 25 && y == 16)

                if (nearCenter || alternative1 || alternative2) {
                    Log.d("MapTransition", "Transition to salon2009 triggered!")
                    return MAP_SALON2009
                }

                if (x == 2 && y == 5) {
                    return MAP_SALON2010
                }

                // Punto para regresar al mapa principal
                if (x == 5 && y == 5) {
                    return MAP_MAIN
                }
            }

            // Más transiciones existentes...
            if (mapId == MAP_SALON2009 && x == 1 && y == 20) {
                return MAP_BUILDING2
            }

            if (mapId == MAP_SALON2010) {
                if (x == 5 && y == 5) {
                    return MAP_BUILDING2
                }
                if (x == 10 && y == 10) {
                    return MAP_MAIN
                }
            }

            // NUEVAS TRANSICIONES ENTRE MAPAS (SECUENCIA LINEAL)

            // Transiciones desde el Estacionamiento
            if (mapId == MAP_ESTACIONAMIENTO) {
                // Regresar al mapa principal
                if (x == 20 && y == 38) return MAP_MAIN

                // Ir al siguiente mapa (Tramo Atrás Plaza)
                if (x == 35 && y == 20) return MAP_TRAS_PLAZA
            }

            // Transiciones desde Tramo Atrás Plaza
            if (mapId == MAP_TRAS_PLAZA) {
                // Regresar al Estacionamiento
                if (x == 5 && y == 20) return MAP_ESTACIONAMIENTO

                // Ir al siguiente mapa (Tramo Lindavista)
                if (x == 35 && y == 20) return MAP_LINDAVISTA
            }

            // Transiciones desde Tramo Lindavista
            if (mapId == MAP_LINDAVISTA) {
                // Regresar al Tramo Atrás Plaza
                if (x == 5 && y == 20) return MAP_TRAS_PLAZA

                // Ir al siguiente mapa (Tramo Turismo)
                if (x == 35 && y == 20) return MAP_TURISMO
            }

            // Transiciones desde Tramo Turismo
            if (mapId == MAP_TURISMO) {
                // Regresar al Tramo Lindavista
                if (x == 5 && y == 20) return MAP_LINDAVISTA

                // Ir al último mapa (Tramo Fuente)
                if (x == 35 && y == 20) return MAP_FUENTE
            }

            // Transiciones desde Tramo Fuente
            if (mapId == MAP_FUENTE) {
                // Regresar al Tramo Turismo
                if (x == 5 && y == 20) return MAP_TURISMO
            }

            return null
        }

        /**
         * Obtiene la posición inicial para un mapa destino
         * Actualizado para incluir los nuevos mapas
         */
        fun getInitialPositionForMap(mapId: String): Pair<Int, Int> {
            return when (mapId) {
                // Mapas existentes
                MAP_MAIN -> Pair(15, 15)
                MAP_BUILDING2 -> Pair(20, 16)
                MAP_SALON2009 -> Pair(20, 20)
                MAP_SALON2010 -> Pair(20, 20)
                MAP_CAFETERIA -> Pair(2, 2)

                // Nuevos mapas
                MAP_ESTACIONAMIENTO -> Pair(20, 30)
                MAP_TRAS_PLAZA -> Pair(20, 20)
                MAP_LINDAVISTA -> Pair(20, 20)
                MAP_TURISMO -> Pair(20, 30)
                MAP_FUENTE -> Pair(20, 20)

                else -> Pair(MAP_WIDTH / 2, MAP_HEIGHT / 2)
            }
        }
    }
}

/**
 * Gestor de matriz para un mapa específico
 */
class MapMatrix(private val mapId: String) {
    private val matrix: Array<Array<Int>> = MapMatrixProvider.getMatrixForMap(mapId)

    private val paints = mapOf(
        MapMatrixProvider.INTERACTIVE to Paint().apply {
            color = Color.argb(100, 0, 255, 255)  // Cian semi-transparente para puntos interactivos
        },
        MapMatrixProvider.WALL to Paint().apply {
            color = Color.argb(150, 139, 69, 19)  // Marrón semi-transparente para paredes
        },
        MapMatrixProvider.PATH to Paint().apply {
            color = Color.argb(30, 220, 220, 255)  // Gris azulado muy transparente para caminos
        },
        MapMatrixProvider.INACCESSIBLE to Paint().apply {
            color = Color.argb(120, 178, 34, 34)  // Rojo ladrillo semi-transparente para objetos
        }
    )

    fun getValueAt(x: Int, y: Int): Int {
        return if (x in 0 until MapMatrixProvider.MAP_WIDTH && y in 0 until MapMatrixProvider.MAP_HEIGHT) {
            matrix[y][x]
        } else {
            -1
        }
    }

    fun isValidPosition(x: Int, y: Int): Boolean {
        return x in 0 until MapMatrixProvider.MAP_WIDTH &&
                y in 0 until MapMatrixProvider.MAP_HEIGHT &&
                matrix[y][x] != MapMatrixProvider.WALL &&
                matrix[y][x] != MapMatrixProvider.INACCESSIBLE
    }

    fun isInteractivePosition(x: Int, y: Int): Boolean {
        return x in 0 until MapMatrixProvider.MAP_WIDTH &&
                y in 0 until MapMatrixProvider.MAP_HEIGHT &&
                matrix[y][x] == MapMatrixProvider.INTERACTIVE
    }

    fun isMapTransitionPoint(x: Int, y: Int): String? {
        return MapMatrixProvider.isMapTransitionPoint(mapId, x, y)
    }

    fun drawMatrix(canvas: Canvas, width: Float, height: Float) {
        try {
            val cellWidth = width / MapMatrixProvider.MAP_WIDTH
            val cellHeight = height / MapMatrixProvider.MAP_HEIGHT

            // Usar distintas opacidades para que el mapa se vea bien
            for (y in 0 until MapMatrixProvider.MAP_HEIGHT) {
                for (x in 0 until MapMatrixProvider.MAP_WIDTH) {
                    val cellType = matrix[y][x]
                    val paint = paints[cellType] ?: paints[MapMatrixProvider.PATH]!!

                    // Calcular posición exacta de la celda
                    val left = x * cellWidth
                    val top = y * cellHeight
                    val right = left + cellWidth
                    val bottom = top + cellHeight

                    // Dibujar la celda
                    canvas.drawRect(left, top, right, bottom, paint)
                }
            }

            // Opcional: Dibujar un borde alrededor de todo el mapa para delimitarlo
            val borderPaint = Paint().apply {
                color = Color.BLACK
                style = Paint.Style.STROKE
                strokeWidth = 2f
            }
            canvas.drawRect(0f, 0f, width, height, borderPaint)
        } catch (e: Exception) {
            Log.e("MapMatrix", "Error dibujando matriz: ${e.message}")
        }
    }
}