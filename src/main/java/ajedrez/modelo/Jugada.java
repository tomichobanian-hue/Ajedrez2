package ajedrez.modelo;

/** Lo que un jugador propone. promocion es null si no corresponde. */
public record Jugada(Posicion origen, Posicion destino, TipoPieza promocion) {

    public Jugada(Posicion origen, Posicion destino) {
        this(origen, destino, null);
    }
}
