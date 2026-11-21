package ajedrez.soporte;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Supplier;

/** Fuente de tiempo controlable para tests (equivalente publico a FakeClock). */
public final class RelojControlable implements Supplier<Instant> {
    private volatile Instant ahora = Instant.parse("2026-01-01T00:00:00Z");

    @Override
    public Instant get() {
        return ahora;
    }

    public void avanzar(Duration d) {
        ahora = ahora.plus(d);
    }
}
