package ajedrez.partida;

/**
 * Vigila el reloj: cada periodoMs pregunta si alguien se quedo sin tiempo y, si es asi,
 * llama a Juego.verificarTiempo. No hay avisos ni oyentes.
 */
public class HiloReloj implements Runnable {
    private final Reloj reloj;
    private final Juego juego;
    private final long periodoMs;
    private volatile boolean activo = true;
    private Thread hilo;

    public HiloReloj(Reloj reloj, Juego juego, long periodoMs) {
        this.reloj = reloj;
        this.juego = juego;
        this.periodoMs = periodoMs;
    }

    public void iniciar() {
        hilo = new Thread(this, "hilo-reloj");
        hilo.setDaemon(true);
        hilo.start();
    }

    @Override
    public void run() {
        while (activo) {
            try {
                Thread.sleep(periodoMs);
            } catch (InterruptedException e) {
                return;
            }
            if (activo && reloj.tiempoAgotado().isPresent()) {
                juego.verificarTiempo();
            }
        }
    }

    public void detener() {
        activo = false;
        if (hilo != null) hilo.interrupt();
    }
}
