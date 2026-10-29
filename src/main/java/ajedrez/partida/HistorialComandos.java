package ajedrez.partida;

import ajedrez.excepciones.MovimientoInvalidoException;
import ajedrez.modelo.Tablero;
import ajedrez.modelo.TableroConsulta;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Unico dueno del historial de jugadas. hechos: cada jugada valida se apila al ejecutarse.
 * rehacer: retroceder pasa la ultima de hechos a rehacer y avanzar la devuelve.
 * Para navegar crea una copia del tablero real (tableroRevision) y le deshace o rehace
 * los movimientos. El tablero real, el turno, el reloj y el estado no se tocan.
 */
public class HistorialComandos {
    private final Deque<Comando> hechos = new ArrayDeque<>();
    private final Deque<Comando> rehacer = new ArrayDeque<>();
    private final Tablero tableroReal;
    private Tablero tableroRevision;

    public HistorialComandos(Tablero tableroReal) {
        this.tableroReal = tableroReal;
    }

    public void ejecutar(Comando comando) {
        verificarSinRevision();
        comando.ejecutar();
        hechos.push(comando);
    }

    /** Deshace de verdad la ultima jugada de la partida real. */
    public void deshacer() {
        verificarSinRevision();
        if (hechos.isEmpty()) throw new MovimientoInvalidoException("No hay jugadas para deshacer");
        hechos.pop().deshacer();
    }

    public void retroceder() {
        if (hechos.isEmpty()) throw new MovimientoInvalidoException("No hay jugadas anteriores");
        if (tableroRevision == null) tableroRevision = tableroReal.copiar();
        Comando comando = hechos.pop();
        tableroRevision.deshacer(comando.getMovimiento());
        rehacer.push(comando);
    }

    public void avanzar() {
        if (rehacer.isEmpty()) throw new MovimientoInvalidoException("No hay jugadas siguientes");
        Comando comando = rehacer.pop();
        tableroRevision.mover(comando.getMovimiento().comoJugada());
        hechos.push(comando);
        if (rehacer.isEmpty()) tableroRevision = null;
    }

    public boolean enRevision() {
        return tableroRevision != null;
    }

    /** Copia que se esta revisando, o null si no hay revision. */
    public TableroConsulta getTableroRevision() {
        return tableroRevision;
    }

    public int cantidadHechas() {
        return hechos.size();
    }

    private void verificarSinRevision() {
        if (enRevision()) {
            throw new MovimientoInvalidoException("Hay una revision en curso: vuelve a la ultima jugada");
        }
    }
}
