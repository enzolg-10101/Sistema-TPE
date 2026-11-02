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

    /**
     * Retorna el nombre exacto para la base de datos (ENUM('Credito','Debito','Transferencia','Efectivo')).
     */
    public String getNombreBd() {
        switch (this) {
            case CREDITO:
                return "Credito";
            case DEBITO:
                return "Debito";
            case TRANSFERENCIA:
                return "Transferencia";
            case EFECTIVO:
                return "Efectivo";
            default:
                return name();
        }
    }

    public static MedioPago fromString(String texto) {
        if (texto == null) {
            return null;
        }
        String limpio = texto.trim();
        for (MedioPago m : MedioPago.values()) {
            if (m.descripcion.equalsIgnoreCase(limpio)
                    || m.name().equalsIgnoreCase(limpio)
                    || m.getNombreBd().equalsIgnoreCase(limpio)) {
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
