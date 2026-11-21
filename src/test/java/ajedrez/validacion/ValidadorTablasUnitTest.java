package ajedrez.validacion;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import ajedrez.modelo.Color;
import ajedrez.modelo.Jugada;
import ajedrez.modelo.Tablero;
import ajedrez.soporte.Fixtures;
import java.util.List;
import java.util.Optional;
import org.junit.Test;

public class ValidadorTablasUnitTest {

    @Test
    public void materialInsuficienteReyYAlfil() {
        ValidadorTablas v = Fixtures.validadorTablas();
        Tablero t = Fixtures.tableroDesdeFen("4k3/8/8/8/8/8/8/2B1K3 w - - 0 1");
        assertFalse(v.puedeGanar(t, Color.BLANCO));
        assertFalse(v.puedeGanar(t, Color.NEGRO));
        v.iniciar(t, Color.BLANCO);
        t.mover(new Jugada(Fixtures.p("e1"), Fixtures.p("e2")));
        v.validar(t, new Jugada(Fixtures.p("e1"), Fixtures.p("e2")));
        assertEquals(Optional.of(MotivoFin.MATERIAL_INSUFICIENTE), v.motivoDeTablas(t));
    }

    @Test
    public void conTorrePuedeGanar() {
        ValidadorTablas v = Fixtures.validadorTablas();
        Tablero t = Fixtures.tableroDesdeFen("4k3/8/8/8/8/8/8/R3K3 w - - 0 1");
        assertTrue(v.puedeGanar(t, Color.BLANCO));
        assertFalse(v.motivoDeTablas(t).isPresent());
    }

    @Test
    public void conPeonPuedeGanar() {
        ValidadorTablas v = Fixtures.validadorTablas();
        Tablero t = Fixtures.tableroDesdeFen("4k3/8/8/8/8/8/P7/4K3 w - - 0 1");
        assertTrue(v.puedeGanar(t, Color.BLANCO));
    }

    @Test
    public void reyYCaballoContraReyNoPuedeGanarSegunSimplificacion() {
        ValidadorTablas v = Fixtures.validadorTablas();
        Tablero t = Fixtures.tableroDesdeFen("4k3/8/8/8/8/8/8/1N2K3 w - - 0 1");
        assertFalse(v.puedeGanar(t, Color.BLANCO));
        assertFalse(v.puedeGanar(t, Color.NEGRO));
    }

    @Test
    public void reyCaballoContraReyAlfilEsTablasPorLaSimplificacionActual() {
        ValidadorTablas v = Fixtures.validadorTablas();
        Tablero t = Fixtures.tableroDesdeFen("4k1n1/8/8/8/8/8/8/2B1K3 w - - 0 1");
        assertFalse(v.puedeGanar(t, Color.BLANCO));
        assertFalse(v.puedeGanar(t, Color.NEGRO));
        assertEquals(Optional.of(MotivoFin.MATERIAL_INSUFICIENTE), v.motivoDeTablas(t));
    }

    @Test
    public void motivoDeCincuentaMovimientosCuandoLaReglaSeCumple() {
        Tablero limpio = Fixtures.tableroDesdeFen("4k3/8/8/8/8/8/8/4K3 w - - 0 1");
        ValidadorTablas vt = new ValidadorTablas(List.of(new ReglaCincuentaMovimientos()));
        vt.iniciar(limpio, Color.BLANCO);
        for (int i = 0; i < 50; i++) {
            moverYRegistrar(vt, limpio, "e1", "e2");
            moverYRegistrar(vt, limpio, "e8", "e7");
            moverYRegistrar(vt, limpio, "e2", "e1");
            moverYRegistrar(vt, limpio, "e7", "e8");
        }
        assertEquals(Optional.of(MotivoFin.CINCUENTA_MOVIMIENTOS), vt.motivoDeTablas(limpio));
    }

    private static void moverYRegistrar(ValidadorTablas v, Tablero t, String o, String d) {
        Jugada j = new Jugada(Fixtures.p(o), Fixtures.p(d));
        t.mover(j);
        v.validar(t, j);
    }
}
