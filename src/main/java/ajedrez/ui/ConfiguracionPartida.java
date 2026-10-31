package ajedrez.ui;

import ajedrez.ia.NivelIA;
import ajedrez.modelo.Color;

/**
 * Opciones de la partida elegidas por los jugadores. colorComputadora es null si juegan dos
 * personas en la misma computadora; si no, es el color con el que juega la computadora.
 */
public record ConfiguracionPartida(int minutos, int incrementoSegundos, boolean girarTablero,
                                   Color colorComputadora, NivelIA nivel) {

    /** Dos personas en la misma computadora. */
    public ConfiguracionPartida(int minutos, int incrementoSegundos, boolean girarTablero) {
        this(minutos, incrementoSegundos, girarTablero, null, NivelIA.MEDIO);
    }

    public boolean contraComputadora() {
        return colorComputadora != null;
    }
}
