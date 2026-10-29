package ajedrez;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Supplier;

/** Reloj controlable para probar el tiempo sin esperar. */
final class FakeClock implements Supplier<Instant> {
    private volatile Instant ahora = Instant.parse("2026-01-01T00:00:00Z");

    @Override
    public Instant get() {
        return ahora;
    }

    void avanzar(Duration d) {
        ahora = ahora.plus(d);
    }
}
