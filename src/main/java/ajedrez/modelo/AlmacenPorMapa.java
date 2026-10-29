package ajedrez.modelo;

import java.util.HashMap;
import java.util.Map;

public class AlmacenPorMapa implements AlmacenPiezas {
    private final Map<Posicion, Pieza> piezas = new HashMap<>();

    @Override
    public Pieza obtener(Posicion posicion) {
        return piezas.get(posicion);
    }

    @Override
    public void colocar(Posicion posicion, Pieza pieza) {
        piezas.put(posicion, pieza);
    }

    @Override
    public Pieza quitar(Posicion posicion) {
        return piezas.remove(posicion);
    }

    @Override
    public Map<Posicion, Pieza> piezasDe(Color color) {
        Map<Posicion, Pieza> resultado = new HashMap<>();
        for (Map.Entry<Posicion, Pieza> e : piezas.entrySet()) {
            if (e.getValue().getColor() == color) resultado.put(e.getKey(), e.getValue());
        }
        return resultado;
    }

    @Override
    public AlmacenPiezas copiar() {
        AlmacenPorMapa copia = new AlmacenPorMapa();
        for (Map.Entry<Posicion, Pieza> e : piezas.entrySet()) {
            copia.piezas.put(e.getKey(), e.getValue().copiar());
        }
        return copia;
    }
}
