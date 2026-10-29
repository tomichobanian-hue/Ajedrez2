package ajedrez;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import ajedrez.modelo.AlmacenPorMapa;
import ajedrez.modelo.Color;
import ajedrez.modelo.Movimiento;
import ajedrez.modelo.Pieza;
import ajedrez.modelo.Tablero;
import ajedrez.modelo.TipoMovimiento;
import ajedrez.modelo.TipoPieza;
import ajedrez.validacion.MotivoFin;
import ajedrez.validacion.ReglaCincuentaMovimientos;
import ajedrez.validacion.ValidadorTablas;
import org.junit.Test;

public class ReglasTablasTest {

    private Movimiento mov(Tablero t, TipoPieza tipo, boolean captura) {
        Pieza pieza = t.crearPieza(tipo, Color.BLANCO);
        Pieza capturada = captura ? t.crearPieza(TipoPieza.PEON, Color.NEGRO) : null;
        return new Movimiento(Utiles.p("a1"), Utiles.p("a2"), pieza, capturada, TipoMovimiento.NORMAL,
                null, null, false, false, null);
    }

    @Test
    public void cincuentaMovimientosSeCumpleA100SemijugadasYSeReiniciaConCaptura() {
        Tablero t = Utiles.tableroDesdeFen(new AlmacenPorMapa(), "4k3/8/8/8/8/8/8/4K3 w - - 0 1");
        ReglaCincuentaMovimientos regla = new ReglaCincuentaMovimientos();
        regla.iniciar(t, Color.BLANCO);
        for (int i = 0; i < 99; i++) regla.registrar(mov(t, TipoPieza.TORRE, false), t, Color.NEGRO);
        assertFalse(regla.estaCumplida(t));
        regla.registrar(mov(t, TipoPieza.TORRE, false), t, Color.NEGRO);
        assertTrue(regla.estaCumplida(t));
        assertEquals(MotivoFin.CINCUENTA_MOVIMIENTOS, regla.getMotivo());
        regla.deshacer();
        assertFalse(regla.estaCumplida(t));
        regla.registrar(mov(t, TipoPieza.TORRE, true), t, Color.NEGRO);      // captura: reinicia
        assertFalse(regla.estaCumplida(t));
        for (int i = 0; i < 99; i++) regla.registrar(mov(t, TipoPieza.TORRE, false), t, Color.NEGRO);
        assertFalse(regla.estaCumplida(t));
        regla.registrar(mov(t, TipoPieza.PEON, false), t, Color.NEGRO);      // peon: reinicia
        regla.registrar(mov(t, TipoPieza.TORRE, false), t, Color.NEGRO);
        assertFalse(regla.estaCumplida(t));
    }

    @Test
    public void puedeGanarSegunMaterial() {
        ValidadorTablas v = Utiles.validadorTablas();
        Tablero solo = Utiles.tableroDesdeFen(new AlmacenPorMapa(), "4k3/8/8/8/8/8/8/2B1K3 w - - 0 1");
        assertFalse(v.puedeGanar(solo, Color.BLANCO));       // rey y alfil
        assertFalse(v.puedeGanar(solo, Color.NEGRO));        // rey solo
        Tablero caballo = Utiles.tableroDesdeFen(new AlmacenPorMapa(), "4k3/8/8/8/8/8/8/1N2K3 w - - 0 1");
        assertFalse(v.puedeGanar(caballo, Color.BLANCO));
        Tablero dos = Utiles.tableroDesdeFen(new AlmacenPorMapa(), "4k3/8/8/8/8/8/8/1NB1K3 w - - 0 1");
        assertTrue(v.puedeGanar(dos, Color.BLANCO));         // alfil + caballo
        for (String fen : new String[] {"4k3/8/8/8/8/8/P7/4K3 w - - 0 1", "4k3/8/8/8/8/8/8/R3K3 w - - 0 1",
            "4k3/8/8/8/8/8/8/3QK3 w - - 0 1"}) {
            assertTrue(fen, v.puedeGanar(Utiles.tableroDesdeFen(new AlmacenPorMapa(), fen), Color.BLANCO));
        }
    }

    @Test
    public void valoresDeLasPiezas() {
        assertEquals(1, TipoPieza.PEON.getValor());
        assertEquals(3, TipoPieza.CABALLO.getValor());
        assertEquals(3, TipoPieza.ALFIL.getValor());
        assertEquals(5, TipoPieza.TORRE.getValor());
        assertEquals(9, TipoPieza.REINA.getValor());
        assertEquals(0, TipoPieza.REY.getValor());
        Tablero t = Utiles.tableroDesdeFen(new AlmacenPorMapa(), "4k3/8/8/8/8/8/8/3QK3 w - - 0 1");
        assertEquals(9, t.obtenerPieza(Utiles.p("d1")).getValor());
    }
}
