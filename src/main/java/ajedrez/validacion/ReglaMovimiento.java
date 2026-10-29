package ajedrez.validacion;

import ajedrez.modelo.Jugada;
import ajedrez.modelo.TableroConsulta;

public interface ReglaMovimiento {
    void validar(TableroConsulta tablero, Jugada jugada);
}
