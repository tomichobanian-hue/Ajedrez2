package ajedrez.modelo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class AlmacenPiezasTest {
    private final Supplier<AlmacenPiezas> factory;
    private final String nombre;

    public AlmacenPiezasTest(String nombre, Supplier<AlmacenPiezas> factory) {
        this.nombre = nombre;
        this.factory = factory;
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> implementaciones() {
        return Arrays.asList(new Object[][] {
                {"mapa", (Supplier<AlmacenPiezas>) AlmacenPorMapa::new},
                {"matriz", (Supplier<AlmacenPiezas>) AlmacenPorMatriz::new}
        });
    }

    @Test
    public void colocarYObtener() {
        AlmacenPiezas almacen = factory.get();
        Pieza peon = new Pieza(Color.BLANCO, TipoPieza.PEON, new MovimientoPeon());
        Posicion pos = new Posicion(1, 0);
        almacen.colocar(pos, peon);
        assertEquals(peon, almacen.obtener(pos));
    }

    @Test
    public void quitarDevuelveYDejaVacio() {
        AlmacenPiezas almacen = factory.get();
        Pieza peon = new Pieza(Color.NEGRO, TipoPieza.PEON, new MovimientoPeon());
        Posicion pos = new Posicion(6, 3);
        almacen.colocar(pos, peon);
        assertEquals(peon, almacen.quitar(pos));
        assertNull(almacen.obtener(pos));
    }

    @Test
    public void piezasDeFiltraPorColor() {
        AlmacenPiezas almacen = factory.get();
        almacen.colocar(new Posicion(0, 0), new Pieza(Color.BLANCO, TipoPieza.TORRE, new MovimientoHorizontalVertical()));
        almacen.colocar(new Posicion(7, 0), new Pieza(Color.NEGRO, TipoPieza.TORRE, new MovimientoHorizontalVertical()));
        Map<Posicion, Pieza> blancas = almacen.piezasDe(Color.BLANCO);
        assertEquals(nombre, 1, blancas.size());
        assertTrue(blancas.containsKey(new Posicion(0, 0)));
    }

    @Test
    public void copiarNoComparteEstadoMutable() {
        AlmacenPiezas almacen = factory.get();
        Posicion pos = new Posicion(0, 4);
        Pieza rey = new Pieza(Color.BLANCO, TipoPieza.REY, new MovimientoRey());
        almacen.colocar(pos, rey);
        AlmacenPiezas copia = almacen.copiar();
        assertNotSame(almacen, copia);
        assertNotNull(copia.obtener(pos));
        assertNotSame(rey, copia.obtener(pos));
        rey.marcarMovida();
        assertTrue(rey.haMovido());
        org.junit.Assert.assertFalse(copia.obtener(pos).haMovido());
        almacen.quitar(pos);
        assertNotNull(copia.obtener(pos));
    }
}
