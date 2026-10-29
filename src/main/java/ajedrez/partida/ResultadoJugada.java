package ajedrez.partida;

import ajedrez.modelo.Color;
import ajedrez.validacion.MotivoFin;
import ajedrez.validacion.ResultadoPosicion;
import java.util.Optional;

/** Lo que Juego averigua despues de mover y entrega al estado para que decida el siguiente. */
public record ResultadoJugada(ResultadoPosicion posicion, Optional<MotivoFin> tablas, Color quienMovio) {

    /** Primer motivo que termina la partida: mate, ahogado o tablas. */
    public Optional<MotivoFin> motivoFinal() {
        if (posicion == ResultadoPosicion.JAQUE_MATE) return Optional.of(MotivoFin.JAQUE_MATE);
        if (posicion == ResultadoPosicion.AHOGADO) return Optional.of(MotivoFin.AHOGADO);
        return tablas;
    }
}
