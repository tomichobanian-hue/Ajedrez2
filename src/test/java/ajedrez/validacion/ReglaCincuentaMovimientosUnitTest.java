package ajedrez.validacion;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import ajedrez.modelo.Color;
import ajedrez.modelo.Movimiento;
import ajedrez.modelo.Pieza;
import ajedrez.modelo.Tablero;
import ajedrez.modelo.TipoMovimiento;
import ajedrez.modelo.TipoPieza;
import ajedrez.soporte.Fixtures;
import org.junit.Test;

/** Unitario de la regla; complementa ReglasTablasTest sin depender de Juego. */
public class ReglaCincuentaMovimientosUnitTest {

    private Movimiento mov(Tablero t, TipoPieza tipo, boolean captura) {
        Pieza pieza = t.crearPieza(tipo, Color.BLANCO);
        Pieza capturada = captura ? t.crearPieza(TipoPieza.PEON, Color.NEGRO) : null;
        return new Movimiento(Fixtures.p("a1"), Fixtures.p("a2"), pieza, capturada, TipoMovimiento.NORMAL,
                null, null, false, false, null);
    }

    @Test
    public void noventaYNueveSemijugadasNoCumple() {
        Tablero t = Fixtures.tableroDesdeFen("4k3/8/8/8/8/8/8/4K3 w - - 0 1");
        ReglaCincuentaMovimientos regla = new ReglaCincuentaMovimientos();
        regla.iniciar(t, Color.BLANCO);
        for (int i = 0; i < 99; i++) regla.registrar(mov(t, TipoPieza.TORRE, false), t, Color.NEGRO);
        assertFalse(regla.estaCumplida(t));
    }

    @Test
    public void cienSemijugadasCumple() {
        Tablero t = Fixtures.tableroDesdeFen("4k3/8/8/8/8/8/8/4K3 w - - 0 1");
        ReglaCincuentaMovimientos regla = new ReglaCincuentaMovimientos();
        regla.iniciar(t, Color.BLANCO);
        for (int i = 0; i < 100; i++) regla.registrar(mov(t, TipoPieza.TORRE, false), t, Color.NEGRO);
        assertTrue(regla.estaCumplida(t));
        assertEquals(MotivoFin.CINCUENTA_MOVIMIENTOS, regla.getMotivo());
    }

    @Test
    public void movimientoDePeonReinicia() {
        Tablero t = Fixtures.tableroDesdeFen("4k3/8/8/8/8/8/8/4K3 w - - 0 1");
        ReglaCincuentaMovimientos regla = new ReglaCincuentaMovimientos();
        regla.iniciar(t, Color.BLANCO);
        for (int i = 0; i < 50; i++) regla.registrar(mov(t, TipoPieza.TORRE, false), t, Color.NEGRO);
        regla.registrar(mov(t, TipoPieza.PEON, false), t, Color.NEGRO);
        for (int i = 0; i < 99; i++) regla.registrar(mov(t, TipoPieza.TORRE, false), t, Color.NEGRO);
        assertFalse(regla.estaCumplida(t));
    }

    @Test
    public void capturaReinicia() {
        Tablero t = Fixtures.tableroDesdeFen("4k3/8/8/8/8/8/8/4K3 w - - 0 1");
        ReglaCincuentaMovimientos regla = new ReglaCincuentaMovimientos();
        regla.iniciar(t, Color.BLANCO);
        for (int i = 0; i < 50; i++) regla.registrar(mov(t, TipoPieza.TORRE, false), t, Color.NEGRO);
        regla.registrar(mov(t, TipoPieza.TORRE, true), t, Color.NEGRO);
        assertFalse(regla.estaCumplida(t));
    }

    @Test
    public void deshacerRestauraContador() {
        Tablero t = Fixtures.tableroDesdeFen("4k3/8/8/8/8/8/8/4K3 w - - 0 1");
        ReglaCincuentaMovimientos regla = new ReglaCincuentaMovimientos();
        regla.iniciar(t, Color.BLANCO);
        for (int i = 0; i < 100; i++) regla.registrar(mov(t, TipoPieza.TORRE, false), t, Color.NEGRO);
        assertTrue(regla.estaCumplida(t));
        regla.deshacer();
        assertFalse(regla.estaCumplida(t));
    }
}
