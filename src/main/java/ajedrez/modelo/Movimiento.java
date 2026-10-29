package ajedrez.modelo;

/** Lo que realmente ocurrio al ejecutar una jugada, con los datos necesarios para deshacerla. */
public record Movimiento(
        Posicion origen,
        Posicion destino,
        Pieza pieza,
        Pieza capturada,
        TipoMovimiento tipo,
        TipoPieza promocion,
        Pieza torre,
        boolean piezaHabiaMovido,
        boolean torreHabiaMovido,
        Movimiento ultimoAnterior) {

    public Jugada comoJugada() {
        return new Jugada(origen, destino, promocion);
    }
}
