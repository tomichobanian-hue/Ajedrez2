package ajedrez.validacion;

import ajedrez.excepciones.ReyEnPeligroException;
import ajedrez.modelo.Color;
import ajedrez.modelo.Jugada;
import ajedrez.modelo.Pieza;
import ajedrez.modelo.Posicion;
import ajedrez.modelo.TableroConsulta;
import ajedrez.modelo.TipoMovimiento;
import ajedrez.modelo.TipoPieza;
import java.util.Map;

/** Responde: la jugada deja a mi rey en jaque? Ademas evalua como queda la posicion. */
public class ValidadorJaque implements ValidadorMovimiento {

    @Override
    public void validar(TableroConsulta tablero, Jugada jugada) {
        Pieza pieza = tablero.obtenerPieza(jugada.origen());
        if (pieza == null) return;
        Color color = pieza.getColor();
        TipoMovimiento tipo = tablero.clasificar(jugada);
        if (tipo == TipoMovimiento.ENROQUE_CORTO || tipo == TipoMovimiento.ENROQUE_LARGO) {
            if (estaEnJaque(tablero, color)) {
                throw new ReyEnPeligroException("No se puede enrocar estando en jaque");
            }
            int paso = tipo == TipoMovimiento.ENROQUE_CORTO ? 1 : -1;
            Posicion intermedia = jugada.origen().sumar(0, paso);
            if (estaAtacada(tablero, intermedia, color.opuesto())) {
                throw new ReyEnPeligroException("El rey no puede pasar por una casilla atacada");
            }
        }
        if (dejaEnJaque(tablero, jugada, color)) {
            throw new ReyEnPeligroException("La jugada deja al rey en jaque");
        }
    }

    /** Evalua la posicion para el color al que le toca mover. */
    public ResultadoPosicion evaluarPosicion(TableroConsulta tablero, Color color) {
        boolean enJaque = estaEnJaque(tablero, color);
        boolean hayJugada = tieneJugadaLegal(tablero, color);
        if (!hayJugada) return enJaque ? ResultadoPosicion.JAQUE_MATE : ResultadoPosicion.AHOGADO;
        return enJaque ? ResultadoPosicion.JAQUE : ResultadoPosicion.NORMAL;
    }

    private boolean estaAtacada(TableroConsulta tablero, Posicion posicion, Color colorAtacante) {
        for (Map.Entry<Posicion, Pieza> e : tablero.piezasDe(colorAtacante).entrySet()) {
            if (e.getValue().casillasAtacadas(e.getKey(), tablero).contains(posicion)) return true;
        }
        return false;
    }

    private boolean estaEnJaque(TableroConsulta tablero, Color color) {
        Posicion rey = tablero.buscarRey(color);
        return rey != null && estaAtacada(tablero, rey, color.opuesto());
    }

    private boolean dejaEnJaque(TableroConsulta tablero, Jugada jugada, Color color) {
        return estaEnJaque(tablero.simular(jugada), color);
    }

    private boolean tieneJugadaLegal(TableroConsulta tablero, Color color) {
        for (Map.Entry<Posicion, Pieza> e : tablero.piezasDe(color).entrySet()) {
            Posicion origen = e.getKey();
            Pieza pieza = e.getValue();
            for (Posicion destino : pieza.movimientosPosibles(origen, tablero)) {
                TipoPieza promocion = null;
                if (pieza.getTipo() == TipoPieza.PEON
                        && destino.fila() == (color == Color.BLANCO ? 7 : 0)) {
                    promocion = TipoPieza.REINA;
                }
                if (!dejaEnJaque(tablero, new Jugada(origen, destino, promocion), color)) return true;
            }
        }
        return false;
    }
}
