package modelo;

public enum TipoMantenimiento {
    FLUIDOS("Fluidos"),
    FILTROS("Filtros"),
    NEUMATICOS("Neumáticos"),
    FRENOS("Frenos"),
    BATERIA("Batería"),
    LUCES("Luces");

    private final String descripcion;

    TipoMantenimiento(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public static TipoMantenimiento fromString(String texto) {
        if (texto == null) {
            return null;
        }
        for (TipoMantenimiento t : TipoMantenimiento.values()) {
            if (t.descripcion.equalsIgnoreCase(texto) || t.name().equalsIgnoreCase(texto)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Tipo de mantenimiento no válido: " + texto);
    }

    @Override
    public String toString() {
        return descripcion;
    }
}
