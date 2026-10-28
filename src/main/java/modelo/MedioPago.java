package modelo;

public enum MedioPago {
    CREDITO("Crédito"),
    DEBITO("Débito"),
    TRANSFERENCIA("Transferencia"),
    EFECTIVO("Efectivo");

    private final String descripcion;

    MedioPago(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public static MedioPago fromString(String texto) {
        if (texto == null) {
            return null;
        }
        for (MedioPago m : MedioPago.values()) {
            if (m.descripcion.equalsIgnoreCase(texto) || m.name().equalsIgnoreCase(texto)) {
                return m;
            }
        }
        throw new IllegalArgumentException("Medio de pago no válido: " + texto);
    }

    @Override
    public String toString() {
        return descripcion;
    }
}
