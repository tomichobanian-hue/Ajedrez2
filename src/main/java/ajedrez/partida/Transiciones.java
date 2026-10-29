package ajedrez.partida;

import ajedrez.modelo.Color;
import ajedrez.validacion.MotivoFin;
import ajedrez.validacion.ResultadoPosicion;
import java.util.Optional;

/** Reglas de transicion compartidas por EnCurso y Jaque. Solo la usan los estados. */
final class Transiciones {
    private Transiciones() {
    }

    static EstadoJuego tras(ResultadoJugada resultado) {
        Optional<MotivoFin> fin = resultado.motivoFinal();
        if (fin.isPresent()) {
            Color ganador = fin.get() == MotivoFin.JAQUE_MATE ? resultado.quienMovio() : null;
            return new Terminada(fin.get(), ganador);
        }
        if (resultado.posicion() == ResultadoPosicion.JAQUE) return new Jaque();
        return new EnCurso();
    }

    static EstadoJuego porTiempo(Color perdedor, boolean rivalPuedeGanar) {
        if (rivalPuedeGanar) return new Terminada(MotivoFin.TIEMPO_AGOTADO, perdedor.opuesto());
        return new Terminada(MotivoFin.MATERIAL_INSUFICIENTE, null);
    }
}
