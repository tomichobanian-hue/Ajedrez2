package ajedrez.ui;

import ajedrez.excepciones.MovimientoInvalidoException;
import ajedrez.modelo.Color;
import ajedrez.modelo.Movimiento;
import ajedrez.modelo.Pieza;
import ajedrez.modelo.Posicion;
import ajedrez.modelo.TableroConsulta;
import ajedrez.modelo.TipoPieza;
import ajedrez.partida.ControladorJuego;
import ajedrez.partida.Jaque;
import ajedrez.validacion.MotivoFin;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

/**
 * Pantalla de una partida. Solo usa ControladorJuego: pregunta (tablero, relojes, estado) y
 * pide acciones (mover, deshacer...). No contiene reglas de ajedrez.
 */
public class VistaAjedrez {
    private static final double CELDA = 72;
    private static final String CLARA = "#f0d9b5";
    private static final String OSCURA = "#b58863";
    private static final String ULTIMA = "#cdd26a";
    private static final String SELECCION = "#f6f669";
    private static final String JAQUE = "#e8605a";

    private final ControladorJuego controlador;
    private final ConfiguracionPartida config;
    private final Runnable alPedirNuevaPartida;

    private final BorderPane raiz = new BorderPane();
    private final StackPane[][] celdas = new StackPane[8][8];
    private final Label nombreArriba = new Label();
    private final Label relojArriba = new Label();
    private final Label nombreAbajo = new Label();
    private final Label relojAbajo = new Label();
    private final HBox panelArriba = new HBox(12);
    private final HBox panelAbajo = new HBox(12);
    private final Label estado = new Label();
    private final Label mensaje = new Label();
    private final Label capturadasPorBlancas = new Label();
    private final Label capturadasPorNegras = new Label();
    private final Button btnDeshacer = new Button("Deshacer");
    private final Button btnRetroceder = new Button("◀ Retroceder");
    private final Button btnAvanzar = new Button("Avanzar ▶");
    private final Button btnTablas = new Button("Acordar tablas");
    private final Button btnAbandonar = new Button("Abandonar");
    private final Button btnNueva = new Button("Nueva partida");

    private Posicion seleccion;
    private List<Posicion> destinos = List.of();
    private Color abajo = Color.BLANCO;
    private String ultimaClave = "";
    private boolean finAvisado;

    public VistaAjedrez(ControladorJuego controlador, ConfiguracionPartida config, Runnable alPedirNuevaPartida) {
        this.controlador = controlador;
        this.config = config;
        this.alPedirNuevaPartida = alPedirNuevaPartida;
        construir();
        refrescar();
    }

    public Parent getRaiz() {
        return raiz;
    }

    // ---------- construccion ----------

    private void construir() {
        GridPane rejilla = new GridPane();
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                StackPane celda = new StackPane();
                celda.setPrefSize(CELDA, CELDA);
                celda.setMinSize(CELDA, CELDA);
                final int fr = r;
                final int fc = c;
                celda.setOnMouseClicked(e -> alHacerClic(fr, fc));
                celdas[r][c] = celda;
                rejilla.add(celda, c, r);
            }
        }
        rejilla.setStyle("-fx-border-color: #3b2f2f; -fx-border-width: 3;");

        estilizarReloj(relojArriba);
        estilizarReloj(relojAbajo);
        nombreArriba.setFont(Font.font(16));
        nombreAbajo.setFont(Font.font(16));
        panelArriba.getChildren().addAll(nombreArriba, espaciador(), relojArriba);
        panelAbajo.getChildren().addAll(nombreAbajo, espaciador(), relojAbajo);
        for (HBox p : List.of(panelArriba, panelAbajo)) {
            p.setAlignment(Pos.CENTER_LEFT);
            p.setPadding(new Insets(6, 10, 6, 10));
            p.setPrefWidth(CELDA * 8 + 6);
        }

        VBox centro = new VBox(8, panelArriba, rejilla, panelAbajo);
        centro.setPadding(new Insets(14));
        centro.setAlignment(Pos.CENTER);

        estado.setFont(Font.font("System", FontWeight.BOLD, 17));
        estado.setWrapText(true);
        mensaje.setStyle("-fx-text-fill: #b00020;");
        mensaje.setWrapText(true);
        capturadasPorBlancas.setFont(Font.font(24));
        capturadasPorNegras.setFont(Font.font(24));
        capturadasPorBlancas.setWrapText(true);
        capturadasPorNegras.setWrapText(true);

        for (Button b : List.of(btnDeshacer, btnRetroceder, btnAvanzar, btnTablas, btnAbandonar, btnNueva)) {
            b.setMaxWidth(Double.MAX_VALUE);
        }
        btnDeshacer.setOnAction(e -> ejecutar(controlador::deshacer));
        btnRetroceder.setOnAction(e -> ejecutar(controlador::retroceder));
        btnAvanzar.setOnAction(e -> ejecutar(controlador::avanzar));
        btnTablas.setOnAction(e -> confirmar("Acordar tablas",
                "Los dos jugadores aceptan las tablas?", controlador::acordarTablas));
        btnAbandonar.setOnAction(e -> {
            Optional<Color> turno = controlador.getTurnoActual();
            if (turno.isEmpty()) return;
            confirmar("Abandonar", "Abandonan las " + nombre(turno.get()) + "?",
                    () -> controlador.abandonar(turno.get()));
        });
        btnNueva.setOnAction(e -> alPedirNuevaPartida.run());

        VBox lateral = new VBox(10,
                titulo("Estado"), estado, mensaje,
                titulo("Capturadas por las blancas"), capturadasPorBlancas,
                titulo("Capturadas por las negras"), capturadasPorNegras,
                titulo("Historial"), btnRetroceder, btnAvanzar, btnDeshacer,
                titulo("Partida"), btnTablas, btnAbandonar, btnNueva);
        lateral.setPadding(new Insets(14));
        lateral.setPrefWidth(250);
        lateral.setStyle("-fx-background-color: #f4f1ec;");

        raiz.setCenter(centro);
        raiz.setRight(lateral);
    }

    private static Region espaciador() {
        Region r = new Region();
        HBox.setHgrow(r, Priority.ALWAYS);
        return r;
    }

    private static Label titulo(String texto) {
        Label l = new Label(texto);
        l.setFont(Font.font("System", FontWeight.BOLD, 12));
        l.setStyle("-fx-text-fill: #6b6258;");
        return l;
    }

    private static void estilizarReloj(Label l) {
        l.setFont(Font.font("Monospaced", FontWeight.BOLD, 26));
        l.setPadding(new Insets(2, 12, 2, 12));
    }

    // ---------- acciones ----------

    private void ejecutar(Runnable accion) {
        try {
            accion.run();
            mensaje.setText("");
        } catch (MovimientoInvalidoException e) {
            mensaje.setText(e.getMessage());
        }
        seleccion = null;
        destinos = List.of();
        refrescar();
    }

    private void confirmar(String titulo, String texto, Runnable accion) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION, texto, ButtonType.YES, ButtonType.NO);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.showAndWait().filter(b -> b == ButtonType.YES).ifPresent(b -> ejecutar(accion));
    }

    private void alHacerClic(int filaPantalla, int columnaPantalla) {
        if (controlador.getEstado().estaTerminada() || controlador.enRevision()) return;
        Posicion clic = aPosicion(filaPantalla, columnaPantalla);
        if (seleccion != null && destinos.contains(clic)) {
            intentarMover(seleccion, clic);
            return;
        }
        List<Posicion> legales = controlador.movimientosLegales(clic);
        if (legales.isEmpty()) {
            seleccion = null;
            destinos = List.of();
        } else {
            seleccion = clic;
            destinos = legales;
            mensaje.setText("");
        }
        refrescar();
    }

    private void intentarMover(Posicion origen, Posicion destino) {
        TipoPieza promocion = null;
        Pieza pieza = controlador.getTablero().obtenerPieza(origen);
        if (pieza != null && pieza.getTipo() == TipoPieza.PEON
                && destino.fila() == (pieza.getColor() == Color.BLANCO ? 7 : 0)) {
            promocion = pedirPromocion();
            if (promocion == null) return;   // cancelado
        }
        final TipoPieza elegida = promocion;
        ejecutar(() -> controlador.mover(origen, destino, elegida));
    }

    private TipoPieza pedirPromocion() {
        ChoiceDialog<String> dialogo = new ChoiceDialog<>("Reina", "Reina", "Torre", "Alfil", "Caballo");
        dialogo.setTitle("Promocion");
        dialogo.setHeaderText("El peon llego al final. Elegi la pieza:");
        dialogo.setContentText("Pieza:");
        Optional<String> resultado = dialogo.showAndWait();
        if (resultado.isEmpty()) return null;
        return switch (resultado.get()) {
            case "Torre" -> TipoPieza.TORRE;
            case "Alfil" -> TipoPieza.ALFIL;
            case "Caballo" -> TipoPieza.CABALLO;
            default -> TipoPieza.REINA;
        };
    }

    // ---------- orientacion ----------

    private Posicion aPosicion(int filaPantalla, int columnaPantalla) {
        return abajo == Color.BLANCO
                ? new Posicion(7 - filaPantalla, columnaPantalla)
                : new Posicion(filaPantalla, 7 - columnaPantalla);
    }

    private int[] aPantalla(Posicion p) {
        return abajo == Color.BLANCO
                ? new int[] {7 - p.fila(), p.columna()}
                : new int[] {p.fila(), 7 - p.columna()};
    }

    // ---------- dibujo ----------

    /** Se llama periodicamente: actualiza relojes y estado, y redibuja el tablero si algo cambio. */
    public void refrescar() {
        Optional<Color> turno = controlador.getTurnoActual();
        boolean revision = controlador.enRevision();
        if (config.girarTablero() && turno.isPresent() && !revision) abajo = turno.get();

        actualizarRelojes(turno);
        actualizarEstado(turno, revision);
        actualizarCapturadas();
        btnRetroceder.setDisable(false);
        btnAvanzar.setDisable(!revision);
        btnDeshacer.setDisable(revision || controlador.getEstado().estaTerminada());
        boolean terminada = controlador.getEstado().estaTerminada();
        btnTablas.setDisable(terminada || revision);
        btnAbandonar.setDisable(terminada || revision);

        TableroConsulta tablero = controlador.getTableroRevision();
        String clave = claveDe(tablero, revision);
        if (!clave.equals(ultimaClave)) {
            ultimaClave = clave;
            dibujarTablero(tablero);
        }
        if (terminada && !finAvisado) {
            finAvisado = true;
            Platform.runLater(this::avisarFin);
        }
    }

    private String claveDe(TableroConsulta tablero, boolean revision) {
        StringBuilder sb = new StringBuilder();
        for (int f = 0; f < 8; f++) {
            for (int c = 0; c < 8; c++) {
                Pieza p = tablero.obtenerPieza(new Posicion(f, c));
                sb.append(p == null ? '.' : glifo(p));
            }
        }
        sb.append(abajo).append(seleccion).append(destinos).append(revision)
                .append(controlador.getEstado().getClass().getSimpleName());
        tablero.getUltimoMovimiento().ifPresent(m -> sb.append(m.origen()).append(m.destino()));
        return sb.toString();
    }

    private void dibujarTablero(TableroConsulta tablero) {
        Optional<Movimiento> ultimo = tablero.getUltimoMovimiento();
        Posicion reyEnJaque = null;
        if (controlador.getEstado() instanceof Jaque && !controlador.enRevision()) {
            Optional<Color> turno = controlador.getTurnoActual();
            if (turno.isPresent()) reyEnJaque = tablero.buscarRey(turno.get());
        }
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Posicion pos = aPosicion(r, c);
                StackPane celda = celdas[r][c];
                celda.getChildren().clear();

                boolean clara = (pos.fila() + pos.columna()) % 2 == 1;
                String color = clara ? CLARA : OSCURA;
                if (ultimo.isPresent() && (ultimo.get().origen().equals(pos) || ultimo.get().destino().equals(pos))) {
                    color = ULTIMA;
                }
                if (pos.equals(seleccion)) color = SELECCION;
                if (pos.equals(reyEnJaque)) color = JAQUE;
                Rectangle fondo = new Rectangle(CELDA, CELDA, Paint.valueOf(color));
                celda.getChildren().add(fondo);

                Pieza pieza = tablero.obtenerPieza(pos);
                if (pieza != null) {
                    Text t = new Text(String.valueOf(glifo(pieza)));
                    t.setFont(Font.font(CELDA * 0.68));
                    if (pieza.getColor() == Color.BLANCO) {
                        t.setFill(Paint.valueOf("#ffffff"));
                        t.setStroke(Paint.valueOf("#222222"));
                        t.setStrokeWidth(1.1);
                    } else {
                        t.setFill(Paint.valueOf("#1a1a1a"));
                    }
                    celda.getChildren().add(t);
                }
                if (destinos.contains(pos)) {
                    if (pieza == null) {
                        Circle punto = new Circle(CELDA * 0.16, Paint.valueOf("#4b7a3a"));
                        punto.setOpacity(0.7);
                        celda.getChildren().add(punto);
                    } else {
                        Circle anillo = new Circle(CELDA * 0.44);
                        anillo.setFill(null);
                        anillo.setStroke(Paint.valueOf("#4b7a3a"));
                        anillo.setStrokeWidth(5);
                        anillo.setOpacity(0.8);
                        celda.getChildren().add(anillo);
                    }
                }
                if (c == 0) celda.getChildren().add(etiqueta(String.valueOf(pos.fila() + 1), Pos.TOP_LEFT, clara));
                if (r == 7) {
                    celda.getChildren().add(etiqueta(String.valueOf((char) ('a' + pos.columna())), Pos.BOTTOM_RIGHT, clara));
                }
            }
        }
    }

    private static Label etiqueta(String texto, Pos posicion, boolean sobreClara) {
        Label l = new Label(texto);
        l.setFont(Font.font("System", FontWeight.BOLD, 11));
        l.setStyle("-fx-text-fill: " + (sobreClara ? OSCURA : CLARA) + ";");
        l.setPadding(new Insets(2, 4, 2, 4));
        StackPane.setAlignment(l, posicion);
        return l;
    }

    private static char glifo(Pieza p) {
        // Se usan los glifos "rellenos" para ambos colores; el color se da con el relleno del texto.
        return switch (p.getTipo()) {
            case REY -> '♚';
            case REINA -> '♛';
            case TORRE -> '♜';
            case ALFIL -> '♝';
            case CABALLO -> '♞';
            case PEON -> '♟';
        };
    }

    // ---------- textos ----------

    private void actualizarRelojes(Optional<Color> turno) {
        Color arriba = abajo.opuesto();
        nombreArriba.setText(nombre(arriba));
        nombreAbajo.setText(nombre(abajo));
        relojArriba.setText(formato(controlador.getTiempoRestante(arriba)));
        relojAbajo.setText(formato(controlador.getTiempoRestante(abajo)));
        marcar(panelArriba, relojArriba, turno.isPresent() && turno.get() == arriba, arriba);
        marcar(panelAbajo, relojAbajo, turno.isPresent() && turno.get() == abajo, abajo);
    }

    private void marcar(HBox panel, Label reloj, boolean activo, Color color) {
        String fondo = activo ? "#fff4c2" : "#e9e6e1";
        String borde = activo ? "#c9a100" : "#c9c4bb";
        panel.setStyle("-fx-background-color: " + fondo + "; -fx-border-color: " + borde
                + "; -fx-border-width: 2; -fx-background-radius: 6; -fx-border-radius: 6;");
        boolean corto = controlador.getTiempoRestante(color).compareTo(Duration.ofSeconds(20)) < 0;
        reloj.setStyle("-fx-text-fill: " + (corto ? "#b00020" : "#1b1b1b") + ";");
    }

    private void actualizarEstado(Optional<Color> turno, boolean revision) {
        if (controlador.getEstado().estaTerminada()) {
            Optional<MotivoFin> motivo = controlador.getMotivoFin();
            String texto = motivo.map(MotivoFin::descripcion).orElse("Fin");
            Optional<Color> ganador = controlador.getGanador();
            estado.setText(ganador.isPresent()
                    ? texto + ". Ganan las " + nombre(ganador.get()) + "."
                    : texto + ". La partida termino en tablas.");
        } else if (revision) {
            estado.setText("Revisando la partida. El tablero real no cambia; avanza hasta la ultima jugada para seguir.");
        } else if (controlador.getEstado() instanceof Jaque) {
            estado.setText("Jaque! Mueven las " + nombre(turno.orElse(Color.BLANCO)) + ".");
        } else {
            estado.setText("Mueven las " + nombre(turno.orElse(Color.BLANCO)) + ".");
        }
    }

    private void actualizarCapturadas() {
        List<Pieza> capturadas = new ArrayList<>(controlador.getPiezasCapturadas());
        capturadas.sort(Comparator.comparingInt(Pieza::getValor).reversed());
        StringBuilder porBlancas = new StringBuilder();
        StringBuilder porNegras = new StringBuilder();
        for (Pieza p : capturadas) {
            (p.getColor() == Color.NEGRO ? porBlancas : porNegras).append(glifo(p));
        }
        capturadasPorBlancas.setText(porBlancas.length() == 0 ? "-" : porBlancas.toString());
        capturadasPorNegras.setText(porNegras.length() == 0 ? "-" : porNegras.toString());
    }

    private void avisarFin() {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION, estado.getText(), ButtonType.OK);
        alerta.setTitle("Fin de la partida");
        alerta.setHeaderText(null);
        alerta.show();
    }

    private static String nombre(Color color) {
        return color == Color.BLANCO ? "blancas" : "negras";
    }

    static String formato(Duration d) {
        long total = d.getSeconds();
        if (d.toMillis() > 0 && total == 0) total = 0;
        long horas = total / 3600;
        long minutos = (total % 3600) / 60;
        long segundos = total % 60;
        if (horas > 0) return String.format("%d:%02d:%02d", horas, minutos, segundos);
        if (total < 20 && d.toMillis() > 0) {
            return String.format("%02d:%02d.%d", minutos, segundos, (d.toMillis() % 1000) / 100);
        }
        return String.format("%02d:%02d", minutos, segundos);
    }
}
