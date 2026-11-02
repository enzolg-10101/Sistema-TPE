package modelo;

/**
 * Dominio del atributo M_tipo de la entidad Mantenimientos.
 * Debe coincidir con ENUM('Fluidos','Filtros','Neumaticos','Frenos','Bateria','Luces','Varios') de la BD.
 */
public enum TipoMantenimiento {
    FLUIDOS("Fluidos"),
    FILTROS("Filtros"),
    NEUMATICOS("Neumáticos"),
    FRENOS("Frenos"),
    BATERIA("Batería"),
    LUCES("Luces"),
    VARIOS("Varios");

    private final String descripcion;

    TipoMantenimiento(String descripcion) {
        this.descripcion = descripcion;
    }

    /**
     * Texto para mostrar en la interfaz (con tildes).
     */
    public String getDescripcion() {
        return descripcion;
    }

    /**
     * Valor exacto del ENUM M_tipo en la base de datos (sin tildes).
     */
    public String getNombreBd() {
        switch (this) {
            case FLUIDOS:
                return "Fluidos";
            case FILTROS:
                return "Filtros";
            case NEUMATICOS:
                return "Neumaticos";
            case FRENOS:
                return "Frenos";
            case BATERIA:
                return "Bateria";
            case LUCES:
                return "Luces";
            case VARIOS:
                return "Varios";
            default:
                return name();
        }
    }

    public static TipoMantenimiento fromString(String texto) {
        if (texto == null) {
            return null;
        }
        String limpio = texto.trim();
        for (TipoMantenimiento t : TipoMantenimiento.values()) {
            if (t.descripcion.equalsIgnoreCase(limpio)
                    || t.name().equalsIgnoreCase(limpio)
                    || t.getNombreBd().equalsIgnoreCase(limpio)) {
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
