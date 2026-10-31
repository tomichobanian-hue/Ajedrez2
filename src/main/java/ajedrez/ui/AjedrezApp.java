package ajedrez.ui;

import ajedrez.ia.NivelIA;
import ajedrez.modelo.Color;
import ajedrez.partida.ControladorJuego;
import java.time.Duration;
import java.util.Optional;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

/** Ventana principal: pide la configuracion, crea la partida y refresca la vista cada 100 ms. */
public class AjedrezApp extends Application {
    private static final String OTRA_PERSONA = "Otra persona";
    private static final String CON_BLANCAS = "La computadora (yo juego con blancas)";
    private static final String CON_NEGRAS = "La computadora (yo juego con negras)";

    private final ControladorJuego controlador = new ControladorJuego();
    private Stage ventana;
    private VistaAjedrez vista;
    private Timeline reloj;

    @Override
    public void start(Stage stage) {
        this.ventana = stage;
        stage.setTitle("Ajedrez");
        Optional<ConfiguracionPartida> config = pedirConfiguracion();
        if (config.isEmpty()) {
            stage.close();
            return;
        }
        iniciar(config.get());
        stage.show();
    }

    private void iniciar(ConfiguracionPartida config) {
        controlador.nuevaPartida(Duration.ofMinutes(config.minutos()), Duration.ofSeconds(config.incrementoSegundos()));
        vista = new VistaAjedrez(controlador, config, this::nuevaPartida);
        if (ventana.getScene() == null) {
            ventana.setScene(new Scene(vista.getRaiz()));
        } else {
            ventana.getScene().setRoot(vista.getRaiz());
        }
        ventana.sizeToScene();
        if (reloj != null) reloj.stop();
        reloj = new Timeline(new KeyFrame(javafx.util.Duration.millis(100), e -> vista.refrescar()));
        reloj.setCycleCount(Timeline.INDEFINITE);
        reloj.play();
    }

    private void nuevaPartida() {
        pedirConfiguracion().ifPresent(this::iniciar);
    }

    private Optional<ConfiguracionPartida> pedirConfiguracion() {
        Dialog<ConfiguracionPartida> dialogo = new Dialog<>();
        dialogo.setTitle("Nueva partida");
        dialogo.setHeaderText("Elegi contra quien jugar y el tiempo");

        ComboBox<String> rival = new ComboBox<>();
        rival.getItems().addAll(OTRA_PERSONA, CON_BLANCAS, CON_NEGRAS);
        rival.setValue(OTRA_PERSONA);
        ComboBox<NivelIA> nivel = new ComboBox<>();
        nivel.getItems().addAll(NivelIA.values());
        nivel.setValue(NivelIA.MEDIO);
        Spinner<Integer> minutos = new Spinner<>(1, 180, 10);
        Spinner<Integer> incremento = new Spinner<>(0, 60, 0);
        minutos.setEditable(true);
        incremento.setEditable(true);
        CheckBox girar = new CheckBox("Girar el tablero en cada turno");
        girar.setSelected(true);
        // El nivel solo importa contra la computadora; girar el tablero, solo entre dos personas.
        nivel.disableProperty().bind(rival.valueProperty().isEqualTo(OTRA_PERSONA));
        girar.disableProperty().bind(rival.valueProperty().isNotEqualTo(OTRA_PERSONA));

        GridPane rejilla = new GridPane();
        rejilla.setHgap(10);
        rejilla.setVgap(10);
        rejilla.setPadding(new Insets(10));
        rejilla.addRow(0, new Label("Rival:"), rival);
        rejilla.addRow(1, new Label("Nivel de la computadora:"), nivel);
        rejilla.addRow(2, new Label("Minutos por jugador:"), minutos);
        rejilla.addRow(3, new Label("Incremento por jugada (segundos):"), incremento);
        rejilla.add(girar, 0, 4, 2, 1);
        dialogo.getDialogPane().setContent(rejilla);

        ButtonType jugar = new ButtonType("Jugar", ButtonBar.ButtonData.OK_DONE);
        dialogo.getDialogPane().getButtonTypes().addAll(jugar, ButtonType.CANCEL);
        dialogo.setResultConverter(b -> {
            if (b != jugar) return null;
            Color computadora = switch (rival.getValue()) {
                case CON_BLANCAS -> Color.NEGRO;
                case CON_NEGRAS -> Color.BLANCO;
                default -> null;
            };
            return new ConfiguracionPartida(valor(minutos, 10, 1, 180), valor(incremento, 0, 0, 60),
                    girar.isSelected(), computadora, nivel.getValue());
        });
        return dialogo.showAndWait();
    }

    /** Lee lo escrito a mano en el campo; si no es un numero valido usa el valor por defecto. */
    private static int valor(Spinner<Integer> spinner, int pordefecto, int min, int max) {
        try {
            int escrito = Integer.parseInt(spinner.getEditor().getText().trim());
            return Math.max(min, Math.min(max, escrito));
        } catch (NumberFormatException e) {
            return pordefecto;
        }
    }

    @Override
    public void stop() {
        if (reloj != null) reloj.stop();
    }
}
