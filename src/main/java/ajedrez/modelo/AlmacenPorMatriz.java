package ajedrez.modelo;

import java.util.HashMap;
import java.util.Map;

public class AlmacenPorMatriz implements AlmacenPiezas {
    private final Pieza[][] casillas = new Pieza[8][8];

    @Override
    public Pieza obtener(Posicion posicion) {
        return casillas[posicion.fila()][posicion.columna()];
    }

    @Override
    public void colocar(Posicion posicion, Pieza pieza) {
        casillas[posicion.fila()][posicion.columna()] = pieza;
    }

    @Override
    public Pieza quitar(Posicion posicion) {
        Pieza anterior = casillas[posicion.fila()][posicion.columna()];
        casillas[posicion.fila()][posicion.columna()] = null;
        return anterior;
    }

    @Override
    public Map<Posicion, Pieza> piezasDe(Color color) {
        Map<Posicion, Pieza> resultado = new HashMap<>();
        for (int f = 0; f < 8; f++) {
            for (int c = 0; c < 8; c++) {
                Pieza p = casillas[f][c];
                if (p != null && p.getColor() == color) resultado.put(new Posicion(f, c), p);
            }
        }
        return resultado;
    }

    @Override
    public AlmacenPiezas copiar() {
        AlmacenPorMatriz copia = new AlmacenPorMatriz();
        for (int f = 0; f < 8; f++) {
            for (int c = 0; c < 8; c++) {
                if (casillas[f][c] != null) copia.casillas[f][c] = casillas[f][c].copiar();
            }
        }
        return copia;
    }
}
