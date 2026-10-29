package ajedrez.modelo;

/** Una casilla: fila 0 es el lado de las blancas, columna 0 es la columna a. */
public record Posicion(int fila, int columna) {

    public Posicion sumar(int df, int dc) {
        return new Posicion(fila + df, columna + dc);
    }

    @Override
    public String toString() {
        return "" + (char) ('a' + columna) + (fila + 1);
    }
}
