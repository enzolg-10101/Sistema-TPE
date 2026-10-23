package modelo;

public enum CategoriaVehiculo {
    AUTO("Auto"),
    FURGONETA("Furgoneta"),
    COMBI("Combi");

    private final String descripcion;

    CategoriaVehiculo(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public static CategoriaVehiculo fromString(String texto) {
        if (texto == null) {
            return null;
        }
        for (CategoriaVehiculo c : CategoriaVehiculo.values()) {
            if (c.descripcion.equalsIgnoreCase(texto) || c.name().equalsIgnoreCase(texto)) {
                return c;
            }
        }
        throw new IllegalArgumentException("Categoría de vehículo no válida: " + texto);
    }

    @Override
    public String toString() {
        return descripcion;
    }
}
