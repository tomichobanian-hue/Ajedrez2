package ajedrez.modelo;

public enum Color {
    BLANCO, NEGRO;

    public Color opuesto() {
        return this == BLANCO ? NEGRO : BLANCO;
    }
}
