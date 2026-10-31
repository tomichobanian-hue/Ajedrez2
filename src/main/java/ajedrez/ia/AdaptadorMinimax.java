package ajedrez.ia;

import ajedrez.modelo.Color;
import ajedrez.modelo.Jugada;
import ajedrez.modelo.Tablero;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * Adapter: hace que MotorMinimax sirva como JugadorComputadora. Traduce el nivel a la profundidad
 * de busqueda y, si el nivel tiene margen, elige al azar entre las jugadas que el motor puntua
 * cerca de la mejor.
 */
public class AdaptadorMinimax implements JugadorComputadora {
    private final MotorMinimax motor;
    private final NivelIA nivel;
    private final Random azar;

    public AdaptadorMinimax(MotorMinimax motor, NivelIA nivel) {
        this(motor, nivel, new Random());
    }

    public AdaptadorMinimax(MotorMinimax motor, NivelIA nivel, Random azar) {
        this.motor = motor;
        this.nivel = nivel;
        this.azar = azar;
    }

    @Override
    public Optional<Jugada> elegirJugada(Tablero copia, Color turno) {
        if (nivel.getMargen() == 0) return motor.mejorJugada(copia, turno, nivel.getProfundidad());
        List<MotorMinimax.JugadaEvaluada> evaluadas = motor.evaluarTodas(copia, turno, nivel.getProfundidad());
        if (evaluadas.isEmpty()) return Optional.empty();
        int minimo = evaluadas.get(0).puntaje() - nivel.getMargen();
        List<Jugada> aceptables = new ArrayList<>();
        for (MotorMinimax.JugadaEvaluada evaluada : evaluadas) {
            if (evaluada.puntaje() >= minimo) aceptables.add(evaluada.jugada());
        }
        return Optional.of(aceptables.get(azar.nextInt(aceptables.size())));
    }
}
