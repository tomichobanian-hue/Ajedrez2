package ajedrez.ia;

import ajedrez.modelo.Color;
import ajedrez.modelo.Jugada;
import ajedrez.modelo.Movimiento;
import ajedrez.modelo.Pieza;
import ajedrez.modelo.Posicion;
import ajedrez.modelo.Tablero;
import ajedrez.modelo.TableroConsulta;
import ajedrez.validacion.ResultadoPosicion;
import ajedrez.validacion.ValidadorJaque;
import ajedrez.validacion.ValidadorJugada;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

/**
 * Motor de busqueda (Adaptee del Adapter): minimax en forma negamax con poda alfa-beta. Tiene su
 * propia interfaz: analiza a la profundidad que se le pida y no sabe de niveles ni de la partida;
 * el juego lo usa a traves de AdaptadorMinimax. Prueba cada jugada sobre el tablero que recibe y la
 * deshace: ese tablero tiene que ser una copia, nunca el de la partida.
 * Los puntajes estan en centipeones y siempre desde el punto de vista del que mueve.
 */
public class MotorMinimax {
    private static final int INFINITO = 1_000_000;
    private static final int MATE = 100_000;

    /** Una jugada con el puntaje que le dio la busqueda. */
    public record JugadaEvaluada(Jugada jugada, int puntaje) {
    }

    private final Random azar;
    private final ValidadorJaque validadorJaque = new ValidadorJaque();
    // Los destinos ya salen de movimientosPosibles: alcanza con mirar que el rey no quede en jaque.
    private final GeneradorJugadas generador = new GeneradorJugadas(new ValidadorJugada(List.of()), validadorJaque);

    public MotorMinimax() {
        this(new Random());
    }

    public MotorMinimax(Random azar) {
        this.azar = azar;
    }

    /** La mejor jugada mirando profundidad (1 o mas) jugadas hacia adelante; vacio si no hay ninguna. */
    public Optional<Jugada> mejorJugada(Tablero tablero, Color turno, int profundidad) {
        int mejor = -INFINITO;
        Jugada mejorJugada = null;
        for (Jugada jugada : candidatas(tablero, turno)) {
            int valor = puntuar(tablero, turno, jugada, profundidad, mejor);
            if (valor > mejor) {
                mejor = valor;
                mejorJugada = jugada;
            }
        }
        return Optional.ofNullable(mejorJugada);
    }

    /** Puntaje exacto de cada jugada legal, de la mejor a la peor. Mas lento: en la raiz no poda. */
    public List<JugadaEvaluada> evaluarTodas(Tablero tablero, Color turno, int profundidad) {
        List<JugadaEvaluada> evaluadas = new ArrayList<>();
        for (Jugada jugada : candidatas(tablero, turno)) {
            evaluadas.add(new JugadaEvaluada(jugada, puntuar(tablero, turno, jugada, profundidad, -INFINITO)));
        }
        evaluadas.sort(Comparator.comparingInt((JugadaEvaluada e) -> -e.puntaje()));
        return evaluadas;
    }

    /** Jugadas legales mezcladas (entre iguales, no siempre la misma) y ordenadas para podar mas. */
    private List<Jugada> candidatas(Tablero tablero, Color turno) {
        List<Jugada> jugadas = generador.legales(tablero, turno);
        Collections.shuffle(jugadas, azar);
        ordenar(tablero, jugadas);
        return jugadas;
    }

    /** Valor de la jugada; si no supera alfa, el resultado solo dice eso (puede ser una cota). */
    private int puntuar(Tablero tablero, Color turno, Jugada jugada, int profundidad, int alfa) {
        Movimiento m = tablero.mover(jugada);
        int valor = -negamax(tablero, turno.opuesto(), profundidad - 1, -INFINITO, -alfa, 1);
        tablero.deshacer(m);
        return valor;
    }

    private int negamax(Tablero tablero, Color turno, int profundidad, int alfa, int beta, int distancia) {
        if (profundidad == 0) return evaluar(tablero, turno);
        List<Jugada> jugadas = generador.legales(tablero, turno);
        if (jugadas.isEmpty()) {
            // Un mate mas cercano vale mas: asi busca el mas corto y demora el propio.
            boolean mate = validadorJaque.evaluarPosicion(tablero, turno) == ResultadoPosicion.JAQUE_MATE;
            return mate ? -MATE + distancia : 0;
        }
        ordenar(tablero, jugadas);
        for (Jugada jugada : jugadas) {
            Movimiento m = tablero.mover(jugada);
            int valor = -negamax(tablero, turno.opuesto(), profundidad - 1, -beta, -alfa, distancia + 1);
            tablero.deshacer(m);
            if (valor >= beta) return beta;
            if (valor > alfa) alfa = valor;
        }
        return alfa;
    }

    /** Primero las capturas de piezas mas valiosas y las promociones: la poda corta mucho mas. */
    private static void ordenar(TableroConsulta tablero, List<Jugada> jugadas) {
        jugadas.sort(Comparator.comparingInt((Jugada j) -> -prioridad(tablero, j)));
    }

    private static int prioridad(TableroConsulta tablero, Jugada jugada) {
        Pieza capturada = tablero.obtenerPieza(jugada.destino());
        int valor = capturada != null ? capturada.getValor() * 10 : 0;
        if (jugada.promocion() != null) valor += jugada.promocion().getValor() * 10;
        return valor;
    }

    // ---- evaluacion ----

    private static int evaluar(TableroConsulta tablero, Color turno) {
        return puntaje(tablero, turno) - puntaje(tablero, turno.opuesto());
    }

    private static int puntaje(TableroConsulta tablero, Color color) {
        int total = 0;
        for (Map.Entry<Posicion, Pieza> e : tablero.piezasDe(color).entrySet()) {
            Pieza pieza = e.getValue();
            total += pieza.getValor() * 100 + bonus(pieza, e.getKey());
        }
        return total;
    }

    /** Pequenos premios por ubicacion: piezas al centro, peones que avanzan, rey resguardado. */
    private static int bonus(Pieza pieza, Posicion p) {
        int avance = pieza.getColor() == Color.BLANCO ? p.fila() : 7 - p.fila();
        int distanciaFila = (Math.abs(2 * p.fila() - 7) - 1) / 2;
        int distanciaColumna = (Math.abs(2 * p.columna() - 7) - 1) / 2;
        int centro = 3 - Math.max(distanciaFila, distanciaColumna);   // 3 en el centro, 0 en el borde
        return switch (pieza.getTipo()) {
            case PEON -> avance * 5 + (distanciaColumna == 0 && avance >= 3 ? 20 : 0);
            case CABALLO -> centro * 10;
            case ALFIL -> centro * 5;
            case TORRE -> avance == 6 ? 20 : 0;
            case REINA -> centro * 2;
            case REY -> -centro * 10;
        };
    }
}
