package ajedrez.ia;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import ajedrez.modelo.Color;
import ajedrez.modelo.Jugada;
import ajedrez.modelo.Tablero;
import ajedrez.modelo.TipoPieza;
import ajedrez.soporte.Fixtures;
import ajedrez.validacion.ValidadorJaque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Test;

public class GeneradorJugadasTest {
    private final GeneradorJugadas generador =
            new GeneradorJugadas(Fixtures.validadorJugada(), new ValidadorJaque());

    @Test
    public void posicionInicialTieneVeinteLegales() {
        Tablero t = Fixtures.tableroDesdeFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
        assertEquals(20, generador.legales(t, Color.BLANCO).size());
    }

    @Test
    public void posicionDeMateNoTieneLegales() {
        Tablero t = Fixtures.tableroDesdeFen("R5k1/5ppp/8/8/8/8/8/6K1 b - - 0 1");
        assertTrue(generador.legales(t, Color.NEGRO).isEmpty());
    }

    @Test
    public void promocionGeneraLasCuatroOpciones() {
        Tablero t = Fixtures.tableroDesdeFen("4k3/P7/8/8/8/8/8/4K3 w - - 0 1");
        List<Jugada> legales = generador.legales(t, Color.BLANCO);
        Set<TipoPieza> promos = new HashSet<>();
        for (Jugada j : legales) {
            if (j.origen().equals(Fixtures.p("a7")) && j.destino().equals(Fixtures.p("a8"))) {
                promos.add(j.promocion());
            }
        }
        assertEquals(Set.of(TipoPieza.REINA, TipoPieza.TORRE, TipoPieza.ALFIL, TipoPieza.CABALLO), promos);
    }

    @Test
    public void noGeneraMovimientosQueDejanAlReyEnJaque() {
        Tablero t = Fixtures.tableroDesdeFen("4k3/4r3/8/8/8/8/4B3/4K3 w - - 0 1");
        List<Jugada> legales = generador.legales(t, Color.BLANCO);
        for (Jugada j : legales) {
            if (j.origen().equals(Fixtures.p("e2"))) {
                org.junit.Assert.fail("el alfil clavado no debería tener jugadas legales: " + j);
            }
        }
    }
}
