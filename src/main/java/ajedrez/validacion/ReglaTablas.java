package ajedrez.validacion;

import ajedrez.modelo.Color;
import ajedrez.modelo.Movimiento;
import ajedrez.modelo.TableroConsulta;

public interface ReglaTablas {
    void iniciar(TableroConsulta tablero, Color turno);

    void registrar(Movimiento movimiento, TableroConsulta tablero, Color turno);

    void deshacer();

    boolean estaCumplida(TableroConsulta tablero);

    MotivoFin getMotivo();
}
