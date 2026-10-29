# Ajedrez (nucleo + interfaz JavaFX)

Juego de ajedrez para dos jugadores en la misma computadora.

## Requisitos
- JDK 17 o superior
- Maven 3.9+

## Ejecutar
    mvn javafx:run

## Probar el nucleo
    mvn test

## Estructura
- `ajedrez.modelo`      tablero, piezas, estrategias de movimiento, almacen de piezas
- `ajedrez.validacion`  validadores (jugada, jaque, tablas) y reglas
- `ajedrez.partida`     Juego, estados, reloj, historial (Command) y ControladorJuego
- `ajedrez.ui`          interfaz JavaFX (solo usa ControladorJuego)
