package ajedrez.validacion;

import ajedrez.modelo.Color;
import ajedrez.modelo.Jugada;
import ajedrez.modelo.Movimiento;
import ajedrez.modelo.Pieza;
import ajedrez.modelo.TableroConsulta;
import ajedrez.modelo.TipoPieza;
import java.util.List;
import java.util.Optional;

/**
 * Lleva la cuenta de las tablas. Se llama DESPUES de que el tablero ejecuto la jugada:
 * validar toma el ultimo movimiento del tablero y actualiza las reglas.
 */
public class ValidadorTablas implements ValidadorMovimiento {
    private final List<ReglaTablas> reglas;

    public ValidadorTablas(List<ReglaTablas> reglas) {
        this.reglas = List.copyOf(reglas);
    }

    public void iniciar(TableroConsulta tablero, Color turno) {
        for (ReglaTablas regla : reglas) regla.iniciar(tablero, turno);
    }

    @Override
    public void validar(TableroConsulta tablero, Jugada jugada) {
        Optional<Movimiento> ultimo = tablero.getUltimoMovimiento();
        if (ultimo.isEmpty()) return;
        Movimiento movimiento = ultimo.get();
        Color turno = movimiento.pieza().getColor().opuesto();
        for (ReglaTablas regla : reglas) regla.registrar(movimiento, tablero, turno);
    }

    /** Motivo de tablas si alguna regla se cumple o si ningun bando puede ganar. */
    public Optional<MotivoFin> motivoDeTablas(TableroConsulta tablero) {
        for (ReglaTablas regla : reglas) {
            if (regla.estaCumplida(tablero)) return Optional.of(regla.getMotivo());
        }
        if (!puedeGanar(tablero, Color.BLANCO) && !puedeGanar(tablero, Color.NEGRO)) {
            return Optional.of(MotivoFin.MATERIAL_INSUFICIENTE);
        }
        return Optional.empty();
    }

    public void deshacer() {
        for (ReglaTablas regla : reglas) regla.deshacer();
    }

    /**
     * Falso si el bando no tiene peones, torres ni reinas y sus piezas suman 3 o menos
     * (rey solo, o con un alfil o un caballo).
     */
    public boolean puedeGanar(TableroConsulta tablero, Color color) {
        int valor = 0;
        for (Pieza pieza : tablero.piezasDe(color).values()) {
            TipoPieza tipo = pieza.getTipo();
            if (tipo == TipoPieza.PEON || tipo == TipoPieza.TORRE || tipo == TipoPieza.REINA) return true;
            valor += pieza.getValor();
        }
        return valor > 3;
    }
}
