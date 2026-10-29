package ajedrez.validacion;

import ajedrez.modelo.Jugada;
import ajedrez.modelo.TableroConsulta;
import java.util.List;

/** Responde: la pieza puede hacer ese movimiento? Corre las reglas en orden. */
public class ValidadorJugada implements ValidadorMovimiento {
    private final List<ReglaMovimiento> reglas;

    public ValidadorJugada(List<ReglaMovimiento> reglas) {
        this.reglas = List.copyOf(reglas);
    }

    @Override
    public void validar(TableroConsulta tablero, Jugada jugada) {
        for (ReglaMovimiento regla : reglas) {
            regla.validar(tablero, jugada);
        }
    }
}
