package ajedrez.ui;

import javafx.application.Application;

/** Punto de entrada (separado de Application para poder empaquetar en un jar). */
public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        Application.launch(AjedrezApp.class, args);
    }
}
