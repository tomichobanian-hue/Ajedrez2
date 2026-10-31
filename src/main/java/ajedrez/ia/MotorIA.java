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
 * La computadora. Busca con minimax (forma negamax) y poda alfa-beta: prueba cada jugada sobre el
 * tablero que recibe y la deshace. Ese tablero tiene que ser una copia: nunca el de la partida.
 * Los puntajes estan en centipeones y siempre desde el punto de vista del que mueve.
 */
public class MotorIA {
    private static final int INFINITO = 1_000_000;
    private static final int MATE = 100_000;

    private final NivelIA nivel;
    private final Random azar;
    private final ValidadorJaque validadorJaque = new ValidadorJaque();
    // Los destinos ya salen de movimientosPosibles: alcanza con mirar que el rey no quede en jaque.
    private final GeneradorJugadas generador = new GeneradorJugadas(new ValidadorJugada(List.of()), validadorJaque);

    public MotorIA(NivelIA nivel) {
        this(nivel, new Random());
    }

    public MotorIA(NivelIA nivel, Random azar) {
        this.nivel = nivel;
        this.azar = azar;
    }

    /** La jugada elegida para el color que mueve; vacio si no tiene ninguna (mate o ahogado). */
    public Optional<Jugada> elegir(Tablero tablero, Color turno) {
        List<Jugada> jugadas = generador.legales(tablero, turno);
        if (jugadas.isEmpty()) return Optional.empty();
        Collections.shuffle(jugadas, azar);   // entre jugadas iguales, no siempre la misma
        ordenar(tablero, jugadas);

        List<Integer> valores = new ArrayList<>();
        int mejor = -INFINITO;
        Jugada mejorJugada = jugadas.get(0);
        for (Jugada jugada : jugadas) {
            // Con margen hace falta el valor exacto de cada jugada, asi que no se poda en la raiz.
            int alfa = nivel.getMargen() > 0 ? -INFINITO : mejor;
            Movimiento m = tablero.mover(jugada);
            int valor = -negamax(tablero, turno.opuesto(), nivel.getProfundidad() - 1, -INFINITO, -alfa, 1);
            tablero.deshacer(m);
            valores.add(valor);
            if (valor > mejor) {
                mejor = valor;
                mejorJugada = jugada;
            }
        }
        if (nivel.getMargen() == 0) return Optional.of(mejorJugada);

        List<Jugada> aceptables = new ArrayList<>();
        for (int i = 0; i < jugadas.size(); i++) {
            if (valores.get(i) >= mejor - nivel.getMargen()) aceptables.add(jugadas.get(i));
        }
        return Optional.of(aceptables.get(azar.nextInt(aceptables.size())));
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
