package ajedrez.partida;

import ajedrez.modelo.Color;
import java.time.Duration;
import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/** Lleva el tiempo de cada jugador. Solo informa: nunca termina la partida por si mismo. */
public class Reloj {
    private final Map<Color, Duration> restante = new EnumMap<>(Color.class);
    private final Duration incremento;
    private final Supplier<Instant> fuenteDeTiempo;
    private Color enJuego = Color.BLANCO;
    private Instant ultimoCambio;
    private boolean corriendo;

    public Reloj(Duration tiempo, Duration incremento, Supplier<Instant> fuenteDeTiempo) {
        restante.put(Color.BLANCO, tiempo);
        restante.put(Color.NEGRO, tiempo);
        this.incremento = incremento;
        this.fuenteDeTiempo = fuenteDeTiempo;
    }

    public synchronized void iniciar() {
        enJuego = Color.BLANCO;
        ultimoCambio = fuenteDeTiempo.get();
        corriendo = true;
    }

    /** El jugador termino su jugada: se le descuenta lo gastado, se le suma el incremento y corre el rival. */
    public synchronized void registrarJugada(Color color) {
        if (!corriendo) return;
        Instant ahora = fuenteDeTiempo.get();
        Duration nuevo = restante.get(color).minus(Duration.between(ultimoCambio, ahora)).plus(incremento);
        restante.put(color, nuevo.isNegative() ? Duration.ZERO : nuevo);
        enJuego = color.opuesto();
        ultimoCambio = ahora;
    }

    /** Al deshacer: vuelve a correr el tiempo del jugador anterior, sin devolver tiempo. */
    public synchronized void volverATurno(Color color) {
        if (!corriendo) return;
        enJuego = color;
        ultimoCambio = fuenteDeTiempo.get();
    }

    public synchronized void detener() {
        if (!corriendo) return;
        restante.put(enJuego, calcular(enJuego));
        corriendo = false;
    }

    public synchronized Duration getRestante(Color color) {
        return calcular(color);
    }

    private Duration calcular(Color color) {
        Duration base = restante.get(color);
        if (corriendo && color == enJuego) {
            base = base.minus(Duration.between(ultimoCambio, fuenteDeTiempo.get()));
        }
        return base.isNegative() ? Duration.ZERO : base;
    }

    /** Color que se quedo sin tiempo, o vacio si nadie. */
    public synchronized Optional<Color> tiempoAgotado() {
        if (!corriendo) return Optional.empty();
        return calcular(enJuego).isZero() ? Optional.of(enJuego) : Optional.empty();
    }
}
