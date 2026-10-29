package ajedrez.validacion;

import ajedrez.modelo.Jugada;
import ajedrez.modelo.TableroConsulta;

/** Contrato comun de los validadores: solo leen el tablero y no cambian nada. */
public interface ValidadorMovimiento {
    void validar(TableroConsulta tablero, Jugada jugada);
}
