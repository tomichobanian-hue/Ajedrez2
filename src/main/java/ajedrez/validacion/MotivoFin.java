package ajedrez.validacion;

public enum MotivoFin {
    JAQUE_MATE("Jaque mate"),
    AHOGADO("Tablas por ahogado"),
    ABANDONO("Abandono"),
    ACUERDO_DE_TABLAS("Tablas por acuerdo"),
    TIEMPO_AGOTADO("Tiempo agotado"),
    CINCUENTA_MOVIMIENTOS("Tablas por 50 movimientos"),
    REPETICION_TRIPLE("Tablas por repeticion triple"),
    MATERIAL_INSUFICIENTE("Tablas por material insuficiente");

    private final String descripcion;

    MotivoFin(String descripcion) {
        this.descripcion = descripcion;
    }

    public String descripcion() {
        return descripcion;
    }
}
