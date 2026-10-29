package ajedrez.partida;

import ajedrez.modelo.Color;

public class Turnero {
    private Color turnoActual = Color.BLANCO;

    public Color getTurnoActual() {
        return turnoActual;
    }

    public boolean esTurnoDe(Color color) {
        return turnoActual == color;
    }

    public void pasarTurno() {
        turnoActual = turnoActual.opuesto();
    }
}
