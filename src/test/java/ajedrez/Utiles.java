package ajedrez;

import ajedrez.modelo.AlmacenPiezas;
import ajedrez.modelo.AlmacenPorMapa;
import ajedrez.modelo.Color;
import ajedrez.modelo.Pieza;
import ajedrez.modelo.Posicion;
import ajedrez.modelo.Tablero;
import ajedrez.modelo.TipoPieza;
import ajedrez.partida.Juego;
import ajedrez.partida.Reloj;
import ajedrez.validacion.ReglaCincuentaMovimientos;
import ajedrez.validacion.ReglaMovimiento;
import ajedrez.validacion.ReglaMovimientoPermitido;
import ajedrez.validacion.ReglaPosicionesValidas;
import ajedrez.validacion.ReglaPromocion;
import ajedrez.validacion.ReglaRepeticionTriple;
import ajedrez.validacion.ValidadorJaque;
import ajedrez.validacion.ValidadorJugada;
import ajedrez.validacion.ValidadorTablas;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.function.Supplier;

final class Utiles {
    private Utiles() {
    }

    static Posicion p(String casilla) {
        return new Posicion(casilla.charAt(1) - '1', casilla.charAt(0) - 'a');
    }

    static ValidadorJugada validadorJugada() {
        return new ValidadorJugada(List.<ReglaMovimiento>of(
                new ReglaPosicionesValidas(), new ReglaMovimientoPermitido(), new ReglaPromocion()));
    }

    static ValidadorTablas validadorTablas() {
        return new ValidadorTablas(List.of(new ReglaCincuentaMovimientos(), new ReglaRepeticionTriple()));
    }

    /** Carga las piezas de un FEN (solo el campo de piezas y los derechos de enroque). */
    static Tablero tableroDesdeFen(AlmacenPiezas almacen, String fen) {
        Tablero tablero = new Tablero(almacen);
        String[] partes = fen.split(" ");
        String[] filas = partes[0].split("/");
        for (int i = 0; i < 8; i++) {
            int fila = 7 - i;
            int columna = 0;
            for (char ch : filas[i].toCharArray()) {
                if (Character.isDigit(ch)) {
                    columna += ch - '0';
                    continue;
                }
                Color color = Character.isUpperCase(ch) ? Color.BLANCO : Color.NEGRO;
                TipoPieza tipo = switch (Character.toLowerCase(ch)) {
                    case 'p' -> TipoPieza.PEON;
                    case 'n' -> TipoPieza.CABALLO;
                    case 'b' -> TipoPieza.ALFIL;
                    case 'r' -> TipoPieza.TORRE;
                    case 'q' -> TipoPieza.REINA;
                    default -> TipoPieza.REY;
                };
                tablero.colocar(new Posicion(fila, columna), tablero.crearPieza(tipo, color));
                columna++;
            }
        }
        String derechos = partes.length > 2 ? partes[2] : "-";
        marcar(tablero, new Posicion(0, 4), derechos.contains("K") || derechos.contains("Q"));
        marcar(tablero, new Posicion(7, 4), derechos.contains("k") || derechos.contains("q"));
        marcar(tablero, new Posicion(0, 7), derechos.contains("K"));
        marcar(tablero, new Posicion(0, 0), derechos.contains("Q"));
        marcar(tablero, new Posicion(7, 7), derechos.contains("k"));
        marcar(tablero, new Posicion(7, 0), derechos.contains("q"));
        return tablero;
    }

    private static void marcar(Tablero tablero, Posicion pos, boolean conDerecho) {
        Pieza pieza = tablero.obtenerPieza(pos);
        if (pieza != null && !conDerecho) pieza.marcarMovida();
    }

    /** Partida sobre un tablero armado a mano, con reloj de mucho tiempo. */
    static Juego juegoDesdeFen(String fen) {
        return juegoDesdeFen(new AlmacenPorMapa(), fen, Instant::now);
    }

    static Juego juegoDesdeFen(AlmacenPiezas almacen, String fen, Supplier<Instant> reloj) {
        Tablero tablero = tableroDesdeFen(almacen, fen);
        Reloj r = new Reloj(Duration.ofHours(10), Duration.ZERO, reloj);
        return new Juego(tablero, validadorJugada(), new ValidadorJaque(), validadorTablas(), r, 10);
    }

    static Juego juegoInicial(AlmacenPiezas almacen) {
        return juegoDesdeFen(almacen, "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", Instant::now);
    }

    static void jugar(Juego juego, String... jugadas) {
        for (String j : jugadas) {
            TipoPieza promo = null;
            if (j.length() == 5) {
                promo = switch (j.charAt(4)) {
                    case 'q' -> TipoPieza.REINA;
                    case 'r' -> TipoPieza.TORRE;
                    case 'b' -> TipoPieza.ALFIL;
                    default -> TipoPieza.CABALLO;
                };
            }
            juego.mover(p(j.substring(0, 2)), p(j.substring(2, 4)), promo);
        }
    }
}
