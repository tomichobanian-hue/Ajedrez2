package ajedrez.partida;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import ajedrez.modelo.Color;
import ajedrez.soporte.RelojControlable;
import java.time.Duration;
import java.util.Optional;
import org.junit.Test;

public class RelojTest {

    @Test
    public void descuentaAlJugadorDeTurno() {
        RelojControlable reloj = new RelojControlable();
        Reloj r = new Reloj(Duration.ofMinutes(10), Duration.ZERO, reloj);
        r.iniciar();
        reloj.avanzar(Duration.ofSeconds(30));
        assertEquals(Duration.ofSeconds(10 * 60 - 30), r.getRestante(Color.BLANCO));
        assertEquals(Duration.ofMinutes(10), r.getRestante(Color.NEGRO));
    }

    @Test
    public void sumaIncrementoAlRegistrarJugada() {
        RelojControlable reloj = new RelojControlable();
        Reloj r = new Reloj(Duration.ofMinutes(10), Duration.ofSeconds(5), reloj);
        r.iniciar();
        reloj.avanzar(Duration.ofSeconds(20));
        r.registrarJugada(Color.BLANCO);
        assertEquals(Duration.ofSeconds(10 * 60 - 20 + 5), r.getRestante(Color.BLANCO));
    }

    @Test
    public void tiempoAgotado() {
        RelojControlable reloj = new RelojControlable();
        Reloj r = new Reloj(Duration.ofSeconds(5), Duration.ZERO, reloj);
        r.iniciar();
        assertTrue(r.tiempoAgotado().isEmpty());
        reloj.avanzar(Duration.ofSeconds(5));
        assertEquals(Optional.of(Color.BLANCO), r.tiempoAgotado());
    }

    @Test
    public void detenerCongelaElTiempo() {
        RelojControlable reloj = new RelojControlable();
        Reloj r = new Reloj(Duration.ofMinutes(1), Duration.ZERO, reloj);
        r.iniciar();
        reloj.avanzar(Duration.ofSeconds(10));
        r.detener();
        Duration restante = r.getRestante(Color.BLANCO);
        reloj.avanzar(Duration.ofMinutes(5));
        assertEquals(restante, r.getRestante(Color.BLANCO));
        assertTrue(r.tiempoAgotado().isEmpty());
    }

    @Test
    public void volverATurnoCambiaQuienCorreSinDevolverTiempo() {
        RelojControlable reloj = new RelojControlable();
        Reloj r = new Reloj(Duration.ofMinutes(10), Duration.ZERO, reloj);
        r.iniciar();
        reloj.avanzar(Duration.ofSeconds(15));
        r.registrarJugada(Color.BLANCO);
        Duration blancoTrasJugada = r.getRestante(Color.BLANCO);
        reloj.avanzar(Duration.ofSeconds(8));
        r.volverATurno(Color.BLANCO);
        assertEquals(blancoTrasJugada, r.getRestante(Color.BLANCO));
        reloj.avanzar(Duration.ofSeconds(3));
        assertEquals(blancoTrasJugada.minus(Duration.ofSeconds(3)), r.getRestante(Color.BLANCO));
    }
}
