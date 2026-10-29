package ajedrez.modelo;

public enum TipoPieza {
    PEON(1), CABALLO(3), ALFIL(3), TORRE(5), REINA(9), REY(0);

    private final int valor;

    TipoPieza(int valor) {
        this.valor = valor;
    }

    public int getValor() {
        return valor;
    }
}
