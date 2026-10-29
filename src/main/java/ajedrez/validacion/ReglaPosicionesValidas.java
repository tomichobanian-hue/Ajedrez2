package ajedrez.validacion;

import ajedrez.excepciones.MovimientoInvalidoException;
import ajedrez.modelo.Jugada;
import ajedrez.modelo.TableroConsulta;

public class ReglaPosicionesValidas implements ReglaMovimiento {
    @Override
    public void validar(TableroConsulta tablero, Jugada jugada) {
        if (!tablero.estaDentro(jugada.origen()) || !tablero.estaDentro(jugada.destino())) {
            throw new MovimientoInvalidoException("La casilla esta fuera del tablero");
        }
        if (jugada.origen().equals(jugada.destino())) {
            throw new MovimientoInvalidoException("El origen y el destino son la misma casilla");
        }
    }
}
