package ajedrez.validacion;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import ajedrez.modelo.Color;
import ajedrez.modelo.Jugada;
import ajedrez.modelo.Movimiento;
import ajedrez.modelo.Tablero;
import ajedrez.soporte.Fixtures;
import org.junit.Test;

public class ReglaRepeticionTripleTest {

    private void registrarTras(ReglaRepeticionTriple regla, Tablero t, Jugada j, Color turnoTras) {
        Movimiento m = t.mover(j);
        regla.registrar(m, t, turnoTras);
    }

    @Test
    public void dosAparicionesNoCumplen() {
        Tablero t = Fixtures.tableroDesdeFen("4k3/8/8/8/8/8/8/4K3 w - - 0 1");
        ReglaRepeticionTriple regla = new ReglaRepeticionTriple();
        regla.iniciar(t, Color.BLANCO); // 1
        registrarTras(regla, t, new Jugada(Fixtures.p("e1"), Fixtures.p("e2")), Color.NEGRO);
        registrarTras(regla, t, new Jugada(Fixtures.p("e8"), Fixtures.p("e7")), Color.BLANCO);
        registrarTras(regla, t, new Jugada(Fixtures.p("e2"), Fixtures.p("e1")), Color.NEGRO);
        registrarTras(regla, t, new Jugada(Fixtures.p("e7"), Fixtures.p("e8")), Color.BLANCO); // 2
        assertFalse(regla.estaCumplida(t));
    }

    @Test
    public void terceraRepeticionCumple() {
        Tablero t = Fixtures.tableroDesdeFen("4k3/8/8/8/8/8/8/4K3 w - - 0 1");
        ReglaRepeticionTriple regla = new ReglaRepeticionTriple();
        regla.iniciar(t, Color.BLANCO); // 1
        // ida y vuelta 1
        registrarTras(regla, t, new Jugada(Fixtures.p("e1"), Fixtures.p("e2")), Color.NEGRO);
        registrarTras(regla, t, new Jugada(Fixtures.p("e8"), Fixtures.p("e7")), Color.BLANCO);
        registrarTras(regla, t, new Jugada(Fixtures.p("e2"), Fixtures.p("e1")), Color.NEGRO);
        registrarTras(regla, t, new Jugada(Fixtures.p("e7"), Fixtures.p("e8")), Color.BLANCO); // 2
        // ida y vuelta 2
        registrarTras(regla, t, new Jugada(Fixtures.p("e1"), Fixtures.p("e2")), Color.NEGRO);
        registrarTras(regla, t, new Jugada(Fixtures.p("e8"), Fixtures.p("e7")), Color.BLANCO);
        registrarTras(regla, t, new Jugada(Fixtures.p("e2"), Fixtures.p("e1")), Color.NEGRO);
        registrarTras(regla, t, new Jugada(Fixtures.p("e7"), Fixtures.p("e8")), Color.BLANCO); // 3
        assertTrue(regla.estaCumplida(t));
    }

    @Test
    public void elTurnoFormaParteDeLaClave() {
        Tablero t = Fixtures.tableroDesdeFen("4k3/8/8/8/8/8/8/4K3 w - - 0 1");
        ReglaRepeticionTriple regla = new ReglaRepeticionTriple();
        regla.iniciar(t, Color.BLANCO);
        // Misma posición de piezas pero turno negro: no es la misma clave
        registrarTras(regla, t, new Jugada(Fixtures.p("e1"), Fixtures.p("e2")), Color.NEGRO);
        assertFalse(regla.estaCumplida(t));
        // Volver a piezas iniciales con turno negro no cuenta como la posición inicial (turno blanco)
        registrarTras(regla, t, new Jugada(Fixtures.p("e8"), Fixtures.p("e7")), Color.BLANCO);
        registrarTras(regla, t, new Jugada(Fixtures.p("e2"), Fixtures.p("e1")), Color.NEGRO);
        // piezas: K e1, k e7 — distinto
        assertFalse(regla.estaCumplida(t));
    }

    @Test
    public void perderDerechoDeEnroqueCambiaLaClave() {
        Tablero t = Fixtures.tableroDesdeFen("4k3/8/8/8/8/8/8/R3K2R w KQ - 0 1");
        ReglaRepeticionTriple regla = new ReglaRepeticionTriple();
        regla.iniciar(t, Color.BLANCO);
        // Mover y volver el rey pierde el derecho: la posición con piezas en e1/a1/h1 ya no es la misma
        registrarTras(regla, t, new Jugada(Fixtures.p("e1"), Fixtures.p("e2")), Color.NEGRO);
        registrarTras(regla, t, new Jugada(Fixtures.p("e8"), Fixtures.p("e7")), Color.BLANCO);
        registrarTras(regla, t, new Jugada(Fixtures.p("e2"), Fixtures.p("e1")), Color.NEGRO);
        registrarTras(regla, t, new Jugada(Fixtures.p("e7"), Fixtures.p("e8")), Color.BLANCO);
        assertFalse(regla.estaCumplida(t));
        // Segunda ida-vuelta: sigue sin ser triple de la inicial (clave distinta por enroque)
        registrarTras(regla, t, new Jugada(Fixtures.p("e1"), Fixtures.p("e2")), Color.NEGRO);
        registrarTras(regla, t, new Jugada(Fixtures.p("e8"), Fixtures.p("e7")), Color.BLANCO);
        registrarTras(regla, t, new Jugada(Fixtures.p("e2"), Fixtures.p("e1")), Color.NEGRO);
        registrarTras(regla, t, new Jugada(Fixtures.p("e7"), Fixtures.p("e8")), Color.BLANCO);
        assertFalse(regla.estaCumplida(t));
        // Tercera vez en la posición SIN derechos: sí puede cumplir (3 apariciones de esa clave)
        registrarTras(regla, t, new Jugada(Fixtures.p("e1"), Fixtures.p("e2")), Color.NEGRO);
        registrarTras(regla, t, new Jugada(Fixtures.p("e8"), Fixtures.p("e7")), Color.BLANCO);
        registrarTras(regla, t, new Jugada(Fixtures.p("e2"), Fixtures.p("e1")), Color.NEGRO);
        registrarTras(regla, t, new Jugada(Fixtures.p("e7"), Fixtures.p("e8")), Color.BLANCO);
        assertTrue(regla.estaCumplida(t));
    }

    @Test
    public void capturaAlPasoEnLaClaveDiferenciaPosiciones() {
        // Tras e2e4 e7e5 e4e5 d7d5 la clave incluye ep; si no hubiera ep sería otra
        Tablero t = Fixtures.tableroDesdeFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
        ReglaRepeticionTriple regla = new ReglaRepeticionTriple();
        regla.iniciar(t, Color.BLANCO);
        registrarTras(regla, t, new Jugada(Fixtures.p("e2"), Fixtures.p("e4")), Color.NEGRO);
        registrarTras(regla, t, new Jugada(Fixtures.p("a7"), Fixtures.p("a6")), Color.BLANCO);
        registrarTras(regla, t, new Jugada(Fixtures.p("e4"), Fixtures.p("e5")), Color.NEGRO);
        registrarTras(regla, t, new Jugada(Fixtures.p("d7"), Fixtures.p("d5")), Color.BLANCO);
        // Posición con ep posible: no debe confundirse con la misma sin ep (siguiente jugada distinta)
        assertFalse(regla.estaCumplida(t));
        // Si las blancas no capturan al paso y mueven otra cosa, la clave deja de tener ep
        registrarTras(regla, t, new Jugada(Fixtures.p("a2"), Fixtures.p("a3")), Color.NEGRO);
        assertFalse(regla.estaCumplida(t));
    }
}
