package ajedrez.modelo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

public class PosicionTest {

    @Test
    public void sumarDesplazaFilaYColumna() {
        assertEquals(new Posicion(3, 4), new Posicion(2, 3).sumar(1, 1));
    }

    @Test
    public void sumarPuedeSalirDelTablero() {
        Posicion fuera = new Posicion(0, 0).sumar(-1, 0);
        assertEquals(-1, fuera.fila());
        assertEquals(0, fuera.columna());
    }

    @Test
    public void igualdadDeRecord() {
        assertEquals(new Posicion(1, 2), new Posicion(1, 2));
        assertNotEquals(new Posicion(1, 2), new Posicion(2, 1));
    }

    @Test
    public void toStringUsaNotacionAlgebraica() {
        assertEquals("a1", new Posicion(0, 0).toString());
        assertEquals("e4", new Posicion(3, 4).toString());
    }
}
