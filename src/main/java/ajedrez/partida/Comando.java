package ajedrez.partida;

import ajedrez.modelo.Movimiento;

public interface Comando {
    void ejecutar();

    void deshacer();

    Movimiento getMovimiento();
}
