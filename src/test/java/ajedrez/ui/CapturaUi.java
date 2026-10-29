package ajedrez.ui;

import ajedrez.modelo.Posicion;
import ajedrez.partida.ControladorJuego;
import java.io.File;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javax.imageio.ImageIO;

/** Prueba de humo de la interfaz: arma la vista, juega unas jugadas y guarda capturas de pantalla. */
public class CapturaUi {
    static Posicion p(String c) {
        return new Posicion(c.charAt(1) - '1', c.charAt(0) - 'a');
    }

    static void mover(ControladorJuego c, String j) {
        c.mover(p(j.substring(0, 2)), p(j.substring(2, 4)), null);
    }

    static void guardar(Scene escena, String ruta) throws Exception {
        WritableImage img = escena.getRoot().snapshot(new SnapshotParameters(), null);
        ImageIO.write(SwingFXUtils.fromFXImage(img, null), "png", new File(ruta));
    }

    public static void main(String[] args) throws Exception {
        String carpeta = args[0];
        CountDownLatch listo = new CountDownLatch(1);
        Platform.startup(() -> {
            try {
                ControladorJuego c = new ControladorJuego();
                c.nuevaPartida(Duration.ofMinutes(10), Duration.ofSeconds(2));
                VistaAjedrez vista = new VistaAjedrez(c, new ConfiguracionPartida(10, 2, true), () -> { });
                Scene escena = new Scene(vista.getRaiz());
                mover(c, "e2e4"); mover(c, "d7d5"); mover(c, "e4d5"); mover(c, "d8d5");
                vista.refrescar();
                escena.getRoot().applyCss();
                escena.getRoot().layout();
                guardar(escena, carpeta + "/ui_1_giro_blancas.png");

                mover(c, "b1c3");
                vista.refrescar();
                escena.getRoot().layout();
                guardar(escena, carpeta + "/ui_2_giro_negras.png");

                c.retroceder(); c.retroceder();
                vista.refrescar();
                escena.getRoot().layout();
                guardar(escena, carpeta + "/ui_3_revision.png");
                c.avanzar(); c.avanzar();

                mover(c, "d5a5");
                vista.refrescar();
                escena.getRoot().layout();
                guardar(escena, carpeta + "/ui_4_jaque.png");
                System.out.println("OK capturas");
            } catch (Throwable t) {
                t.printStackTrace();
            } finally {
                listo.countDown();
            }
        });
        listo.await();
        Platform.exit();
        System.exit(0);
    }
}
