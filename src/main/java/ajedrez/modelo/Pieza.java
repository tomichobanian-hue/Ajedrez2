package ajedrez.modelo;

import java.util.List;

public class Pieza {
    private final Color color;
    private final TipoPieza tipo;
    private final EstrategiaMovimiento estrategia;
    private boolean haMovido;

    public Pieza(Color color, TipoPieza tipo, EstrategiaMovimiento estrategia) {
        this.color = color;
        this.tipo = tipo;
        this.estrategia = estrategia;
        this.haMovido = false;
    }

    public Color getColor() {
        return color;
    }

    public TipoPieza getTipo() {
        return tipo;
    }

    public int getValor() {
        return tipo.getValor();
    }

    public boolean haMovido() {
        return haMovido;
    }

    public void marcarMovida() {
        haMovido = true;
    }

    public void restaurarMovida(boolean valor) {
        haMovido = valor;
    }

    public List<Posicion> movimientosPosibles(Posicion origen, TableroConsulta tablero) {
        return estrategia.movimientos(this, origen, tablero);
    }

    public List<Posicion> casillasAtacadas(Posicion origen, TableroConsulta tablero) {
        return estrategia.casillasAtacadas(this, origen, tablero);
    }

    /** Copia independiente: misma estrategia (no tiene estado), estado propio de haMovido. */
    public Pieza copiar() {
        Pieza copia = new Pieza(color, tipo, estrategia);
        copia.haMovido = haMovido;
        return copia;
    }

    @Override
    public String toString() {
        return color + " " + tipo;
    }
}
