package ajedrez.validacion;

import ajedrez.excepciones.MovimientoInvalidoException;
import ajedrez.modelo.Jugada;
import ajedrez.modelo.Pieza;
import ajedrez.modelo.TableroConsulta;

/** El destino debe estar entre los movimientos de la pieza. No mira si el rey queda en jaque. */
public class ReglaMovimientoPermitido implements ReglaMovimiento {
    @Override
    public void validar(TableroConsulta tablero, Jugada jugada) {
        Pieza pieza = tablero.obtenerPieza(jugada.origen());
        if (pieza == null) {
            throw new MovimientoInvalidoException("No hay ninguna pieza en el origen");
        }
        if (!pieza.movimientosPosibles(jugada.origen(), tablero).contains(jugada.destino())) {
            throw new MovimientoInvalidoException(
                    "Esa pieza (" + pieza.getTipo() + ") no puede moverse a esa casilla");
        }
    }
}
