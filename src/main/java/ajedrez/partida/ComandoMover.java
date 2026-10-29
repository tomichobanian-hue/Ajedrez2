package ajedrez.partida;

import ajedrez.modelo.Jugada;
import ajedrez.modelo.Movimiento;

/** Una jugada ejecutada: sabe aplicarla y revertirla, y recuerda el estado anterior de la partida. */
public class ComandoMover implements Comando {
    private final Juego juego;
    private final Jugada jugada;
    private Movimiento movimiento;
    private EstadoJuego estadoAnterior;

    public ComandoMover(Juego juego, Jugada jugada) {
        this.juego = juego;
        this.jugada = jugada;
    }

    @Override
    public void ejecutar() {
        estadoAnterior = juego.getEstado();
        movimiento = juego.aplicarJugada(jugada);
    }

    @Override
    public void deshacer() {
        juego.revertirJugada(movimiento);
        juego.restaurarEstado(estadoAnterior);
    }

    @Override
    public Movimiento getMovimiento() {
        return movimiento;
    }
}
