package ajedrez.validacion;

import ajedrez.modelo.Color;
import ajedrez.modelo.Movimiento;
import ajedrez.modelo.Pieza;
import ajedrez.modelo.Posicion;
import ajedrez.modelo.TableroConsulta;
import ajedrez.modelo.TipoPieza;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/** La misma posicion (con el mismo turno y derechos de enroque) tres veces. */
public class ReglaRepeticionTriple implements ReglaTablas {
    private final Map<String, Integer> apariciones = new HashMap<>();
    private final Deque<String> claves = new ArrayDeque<>();

    @Override
    public void iniciar(TableroConsulta tablero, Color turno) {
        apariciones.clear();
        claves.clear();
        agregar(generarClave(tablero, turno));
    }

    @Override
    public void registrar(Movimiento movimiento, TableroConsulta tablero, Color turno) {
        agregar(generarClave(tablero, turno));
    }

    private void agregar(String clave) {
        apariciones.merge(clave, 1, Integer::sum);
        claves.push(clave);
    }

    @Override
    public void deshacer() {
        if (claves.isEmpty()) return;
        String clave = claves.pop();
        int restantes = apariciones.get(clave) - 1;
        if (restantes <= 0) apariciones.remove(clave);
        else apariciones.put(clave, restantes);
    }

    @Override
    public boolean estaCumplida(TableroConsulta tablero) {
        if (claves.isEmpty()) return false;
        return apariciones.getOrDefault(claves.peek(), 0) >= 3;
    }

    @Override
    public MotivoFin getMotivo() {
        return MotivoFin.REPETICION_TRIPLE;
    }

    private String generarClave(TableroConsulta tablero, Color turno) {
        StringBuilder sb = new StringBuilder();
        for (int f = 0; f < 8; f++) {
            for (int c = 0; c < 8; c++) {
                Pieza p = tablero.obtenerPieza(new Posicion(f, c));
                if (p == null) {
                    sb.append('.');
                } else {
                    char letra = switch (p.getTipo()) {
                        case PEON -> 'p';
                        case CABALLO -> 'n';
                        case ALFIL -> 'b';
                        case TORRE -> 'r';
                        case REINA -> 'q';
                        case REY -> 'k';
                    };
                    sb.append(p.getColor() == Color.BLANCO ? Character.toUpperCase(letra) : letra);
                }
            }
        }
        sb.append(turno == Color.BLANCO ? 'w' : 'b');
        for (Color color : Color.values()) {
            int fila = color == Color.BLANCO ? 0 : 7;
            Pieza rey = tablero.obtenerPieza(new Posicion(fila, 4));
            boolean reyIntacto = rey != null && rey.getTipo() == TipoPieza.REY && !rey.haMovido();
            for (int columna : new int[] {7, 0}) {
                Pieza torre = tablero.obtenerPieza(new Posicion(fila, columna));
                boolean derecho = reyIntacto && torre != null && torre.getTipo() == TipoPieza.TORRE
                        && torre.getColor() == color && !torre.haMovido();
                sb.append(derecho ? '1' : '0');
            }
        }
        tablero.getUltimoMovimiento().ifPresent(m -> {
            if (m.pieza().getTipo() == TipoPieza.PEON
                    && Math.abs(m.destino().fila() - m.origen().fila()) == 2) {
                sb.append("ep").append(m.destino().columna());
            }
        });
        return sb.toString();
    }
}
