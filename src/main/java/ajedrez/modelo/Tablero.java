package ajedrez.modelo;

import java.util.Map;
import java.util.Optional;

/**
 * Unico gestor de las piezas. Ejecuta y deshace jugadas, incluidas las especiales.
 * No valida nada: confia en que quien lo llama ya valido la jugada.
 */
public class Tablero implements TableroConsulta {
    private final AlmacenPiezas almacen;
    private Movimiento ultimoMovimiento;

    public Tablero(AlmacenPiezas almacen) {
        this.almacen = almacen;
    }

    public void colocarPosicionInicial() {
        for (Color color : Color.values()) {
            for (Posicion p : almacen.piezasDe(color).keySet()) almacen.quitar(p);
        }
        ultimoMovimiento = null;
        TipoPieza[] fondo = {TipoPieza.TORRE, TipoPieza.CABALLO, TipoPieza.ALFIL, TipoPieza.REINA,
            TipoPieza.REY, TipoPieza.ALFIL, TipoPieza.CABALLO, TipoPieza.TORRE};
        for (int c = 0; c < 8; c++) {
            almacen.colocar(new Posicion(0, c), crearPieza(fondo[c], Color.BLANCO));
            almacen.colocar(new Posicion(1, c), crearPieza(TipoPieza.PEON, Color.BLANCO));
            almacen.colocar(new Posicion(6, c), crearPieza(TipoPieza.PEON, Color.NEGRO));
            almacen.colocar(new Posicion(7, c), crearPieza(fondo[c], Color.NEGRO));
        }
    }

    /** Ejecuta una jugada ya validada y devuelve lo que ocurrio. */
    public Movimiento mover(Jugada jugada) {
        Posicion origen = jugada.origen();
        Posicion destino = jugada.destino();
        TipoMovimiento tipo = clasificar(jugada);
        Pieza pieza = almacen.obtener(origen);
        boolean piezaHabiaMovido = pieza.haMovido();
        Pieza capturada = null;
        Pieza torre = null;
        boolean torreHabiaMovido = false;
        TipoPieza promocion = null;

        switch (tipo) {
            case NORMAL -> {
                capturada = almacen.quitar(destino);
                almacen.quitar(origen);
                almacen.colocar(destino, pieza);
            }
            case PROMOCION -> {
                capturada = almacen.quitar(destino);
                almacen.quitar(origen);
                promocion = jugada.promocion() != null ? jugada.promocion() : TipoPieza.REINA;
                Pieza nueva = crearPieza(promocion, pieza.getColor());
                nueva.marcarMovida();
                almacen.colocar(destino, nueva);
            }
            case CAPTURA_AL_PASO -> {
                capturada = almacen.quitar(new Posicion(origen.fila(), destino.columna()));
                almacen.quitar(origen);
                almacen.colocar(destino, pieza);
            }
            case ENROQUE_CORTO, ENROQUE_LARGO -> {
                boolean corto = tipo == TipoMovimiento.ENROQUE_CORTO;
                Posicion desdeTorre = new Posicion(origen.fila(), corto ? 7 : 0);
                Posicion haciaTorre = new Posicion(origen.fila(), corto ? 5 : 3);
                torre = almacen.quitar(desdeTorre);
                torreHabiaMovido = torre.haMovido();
                almacen.quitar(origen);
                almacen.colocar(destino, pieza);
                almacen.colocar(haciaTorre, torre);
                torre.marcarMovida();
            }
        }
        pieza.marcarMovida();
        Movimiento movimiento = new Movimiento(origen, destino, pieza, capturada, tipo, promocion,
                torre, piezaHabiaMovido, torreHabiaMovido, ultimoMovimiento);
        ultimoMovimiento = movimiento;
        return movimiento;
    }

    /**
     * Invierte un movimiento. Usa las piezas que hay en este tablero y copias de las que guarda
     * el Movimiento, para no compartir objetos con otro tablero (por ejemplo la copia de revision).
     */
    public void deshacer(Movimiento m) {
        Posicion origen = m.origen();
        Posicion destino = m.destino();
        Color color = m.pieza().getColor();
        switch (m.tipo()) {
            case NORMAL -> {
                Pieza pieza = almacen.quitar(destino);
                pieza.restaurarMovida(m.piezaHabiaMovido());
                almacen.colocar(origen, pieza);
                if (m.capturada() != null) almacen.colocar(destino, m.capturada().copiar());
            }
            case PROMOCION -> {
                almacen.quitar(destino);
                Pieza peon = crearPieza(TipoPieza.PEON, color);
                peon.restaurarMovida(m.piezaHabiaMovido());
                almacen.colocar(origen, peon);
                if (m.capturada() != null) almacen.colocar(destino, m.capturada().copiar());
            }
            case CAPTURA_AL_PASO -> {
                Pieza pieza = almacen.quitar(destino);
                pieza.restaurarMovida(m.piezaHabiaMovido());
                almacen.colocar(origen, pieza);
                almacen.colocar(new Posicion(origen.fila(), destino.columna()), m.capturada().copiar());
            }
            case ENROQUE_CORTO, ENROQUE_LARGO -> {
                boolean corto = m.tipo() == TipoMovimiento.ENROQUE_CORTO;
                Pieza rey = almacen.quitar(destino);
                rey.restaurarMovida(m.piezaHabiaMovido());
                almacen.colocar(origen, rey);
                Pieza torre = almacen.quitar(new Posicion(origen.fila(), corto ? 5 : 3));
                torre.restaurarMovida(m.torreHabiaMovido());
                almacen.colocar(new Posicion(origen.fila(), corto ? 7 : 0), torre);
            }
        }
        ultimoMovimiento = m.ultimoAnterior();
    }

    public void colocar(Posicion posicion, Pieza pieza) {
        almacen.colocar(posicion, pieza);
    }

    public Pieza quitar(Posicion posicion) {
        return almacen.quitar(posicion);
    }

    /** Copia independiente del tablero: piezas propias y el mismo ultimo movimiento. */
    public Tablero copiar() {
        Tablero copia = new Tablero(almacen.copiar());
        copia.ultimoMovimiento = ultimoMovimiento;
        return copia;
    }

    public Pieza crearPieza(TipoPieza tipo, Color color) {
        EstrategiaMovimiento estrategia = switch (tipo) {
            case TORRE -> new MovimientoHorizontalVertical();
            case ALFIL -> new MovimientoDiagonal();
            case REINA -> new MovimientoReina();
            case CABALLO -> new MovimientoCaballo();
            case REY -> new MovimientoRey();
            case PEON -> new MovimientoPeon();
        };
        return new Pieza(color, tipo, estrategia);
    }

    // ---- TableroConsulta ----

    @Override
    public Pieza obtenerPieza(Posicion posicion) {
        return estaDentro(posicion) ? almacen.obtener(posicion) : null;
    }

    @Override
    public boolean estaOcupada(Posicion posicion) {
        return obtenerPieza(posicion) != null;
    }

    @Override
    public boolean estaDentro(Posicion p) {
        return p.fila() >= 0 && p.fila() < 8 && p.columna() >= 0 && p.columna() < 8;
    }

    @Override
    public Map<Posicion, Pieza> piezasDe(Color color) {
        return almacen.piezasDe(color);
    }

    @Override
    public Posicion buscarRey(Color color) {
        for (Map.Entry<Posicion, Pieza> e : almacen.piezasDe(color).entrySet()) {
            if (e.getValue().getTipo() == TipoPieza.REY) return e.getKey();
        }
        return null;
    }

    @Override
    public Optional<Movimiento> getUltimoMovimiento() {
        return Optional.ofNullable(ultimoMovimiento);
    }

    @Override
    public TipoMovimiento clasificar(Jugada jugada) {
        Pieza pieza = obtenerPieza(jugada.origen());
        if (pieza == null) return TipoMovimiento.NORMAL;
        Posicion o = jugada.origen();
        Posicion d = jugada.destino();
        if (pieza.getTipo() == TipoPieza.REY && o.fila() == d.fila()
                && Math.abs(d.columna() - o.columna()) == 2) {
            return d.columna() > o.columna() ? TipoMovimiento.ENROQUE_CORTO : TipoMovimiento.ENROQUE_LARGO;
        }
        if (pieza.getTipo() == TipoPieza.PEON) {
            if (o.columna() != d.columna() && !estaOcupada(d)) return TipoMovimiento.CAPTURA_AL_PASO;
            int ultimaFila = pieza.getColor() == Color.BLANCO ? 7 : 0;
            if (d.fila() == ultimaFila) return TipoMovimiento.PROMOCION;
        }
        return TipoMovimiento.NORMAL;
    }

    @Override
    public TableroConsulta simular(Jugada jugada) {
        Tablero copia = copiar();
        copia.mover(jugada);
        return copia;
    }
}
