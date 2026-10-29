package ajedrez.validacion;

import ajedrez.modelo.Color;
import ajedrez.modelo.Movimiento;
import ajedrez.modelo.TableroConsulta;
import ajedrez.modelo.TipoPieza;
import java.util.ArrayDeque;
import java.util.Deque;

/** 50 movimientos (100 semijugadas) sin captura ni movimiento de peon. */
public class ReglaCincuentaMovimientos implements ReglaTablas {
    private int semijugadas;
    private final Deque<Integer> anteriores = new ArrayDeque<>();

    @Override
    public void iniciar(TableroConsulta tablero, Color turno) {
        semijugadas = 0;
        anteriores.clear();
    }

    @Override
    public void registrar(Movimiento movimiento, TableroConsulta tablero, Color turno) {
        anteriores.push(semijugadas);
        if (movimiento.pieza().getTipo() == TipoPieza.PEON || movimiento.capturada() != null) {
            semijugadas = 0;
        } else {
            semijugadas++;
        }
    }

    @Override
    public void deshacer() {
        if (!anteriores.isEmpty()) semijugadas = anteriores.pop();
    }

    @Override
    public boolean estaCumplida(TableroConsulta tablero) {
        return semijugadas >= 100;
    }

    @Override
    public MotivoFin getMotivo() {
        return MotivoFin.CINCUENTA_MOVIMIENTOS;
    }
}
