package ajedrez.validacion;

import ajedrez.excepciones.MovimientoInvalidoException;
import ajedrez.modelo.Color;
import ajedrez.modelo.Jugada;
import ajedrez.modelo.Pieza;
import ajedrez.modelo.TableroConsulta;
import ajedrez.modelo.TipoMovimiento;
import ajedrez.modelo.TipoPieza;

/** Si el peon llega a la ultima fila, la jugada debe traer la pieza elegida. */
public class ReglaPromocion implements ReglaMovimiento {
    @Override
    public void validar(TableroConsulta tablero, Jugada jugada) {
        if (tablero.clasificar(jugada) != TipoMovimiento.PROMOCION) return;
        TipoPieza elegida = jugada.promocion();
        if (elegida == null) {
            throw new MovimientoInvalidoException("Hay que elegir la pieza de la promocion");
        }
        if (elegida == TipoPieza.PEON || elegida == TipoPieza.REY) {
            throw new MovimientoInvalidoException("No se puede promocionar a " + elegida);
        }
    }
}
