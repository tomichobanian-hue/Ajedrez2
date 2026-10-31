package ajedrez.ia;

/**
 * Que tan fuerte juega la computadora. profundidad: cuantas jugadas mira hacia adelante.
 * margen (centipeones): puede elegir cualquier jugada que no sea peor que la mejor en mas de eso.
 */
public enum NivelIA {
    FACIL("Facil", 1, 150), MEDIO("Medio", 2, 0), DIFICIL("Dificil", 3, 0);

    private final String nombre;
    private final int profundidad;
    private final int margen;

    NivelIA(String nombre, int profundidad, int margen) {
        this.nombre = nombre;
        this.profundidad = profundidad;
        this.margen = margen;
    }

    public int getProfundidad() {
        return profundidad;
    }

    public int getMargen() {
        return margen;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
