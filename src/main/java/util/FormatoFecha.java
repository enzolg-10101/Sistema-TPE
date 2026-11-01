package util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import javafx.scene.control.DatePicker;
import javafx.util.StringConverter;

/**
 * Utilidad centralizada para unificar todas las fechas de la interfaz gráfica
 * al formato dd-MM-yyyy (dd-mm-aaaa), manteniendo compatibilidad con el formato
 * SQL (yyyy-MM-dd) de la base de datos.
 */
public final class FormatoFecha {

    public static final String PATRON_GUI = "dd-MM-yyyy";
    public static final DateTimeFormatter FORMATO_GUI = DateTimeFormatter.ofPattern(PATRON_GUI);
    public static final DateTimeFormatter FORMATO_SQL = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter FORMATO_BARRAS = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private FormatoFecha() {
    }

    /**
     * Configura un DatePicker de JavaFX para que muestre siempre la fecha en formato dd-MM-yyyy (dd-mm-aaaa).
     */
    public static void configurarDatePicker(DatePicker datePicker) {
        if (datePicker == null) {
            return;
        }
        datePicker.setEditable(false);
        if (datePicker.getPromptText() == null || datePicker.getPromptText().trim().isEmpty()
                || datePicker.getPromptText().equalsIgnoreCase("Calendario")
                || datePicker.getPromptText().equalsIgnoreCase("Seleccione en calendario")
                || datePicker.getPromptText().equalsIgnoreCase("Seleccione en el calendario")) {
            datePicker.setPromptText("dd-mm-aaaa");
        }

        datePicker.setConverter(new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate fecha) {
                return (fecha != null) ? FORMATO_GUI.format(fecha) : "";
            }

            @Override
            public LocalDate fromString(String texto) {
                return parsearFecha(texto);
            }
        });
    }

    /**
     * Convierte un LocalDate al texto con formato dd-MM-yyyy para mostrar en la interfaz gráfica.
     */
    public static String formatearFechaGui(LocalDate fecha) {
        return (fecha != null) ? FORMATO_GUI.format(fecha) : "";
    }

    /**
     * Convierte una cadena de fecha (provenga en yyyy-MM-dd, yyyy-MM-dd HH:mm:ss, dd/MM/yyyy o dd-MM-yyyy)
     * al formato visual dd-MM-yyyy (dd-mm-aaaa) para mostrar en tablas, etiquetas o campos de texto.
     */
    public static String formatearFechaGui(String textoFecha) {
        if (textoFecha == null || textoFecha.trim().isEmpty() || textoFecha.trim().equals("-")) {
            return textoFecha != null ? textoFecha : "-";
        }
        LocalDate fecha = parsearFecha(textoFecha);
        if (fecha != null) {
            return FORMATO_GUI.format(fecha);
        }
        return textoFecha.trim();
    }

    /**
     * Parsea una cadena de fecha en cualquiera de los formatos habituales (dd-MM-yyyy, yyyy-MM-dd, dd/MM/yyyy),
     * ignorando la porción de hora si existiera.
     */
    public static LocalDate parsearFecha(String textoFecha) {
        if (textoFecha == null) {
            return null;
        }
        String limpio = textoFecha.trim();
        if (limpio.isEmpty() || limpio.equals("-")) {
            return null;
        }

        // Si incluye hora (ej. "2026-10-27 14:30:00" o "2026-10-27T14:30:00"), tomar solo la parte de fecha
        int idxEspacio = limpio.indexOf(' ');
        if (idxEspacio > 0) {
            limpio = limpio.substring(0, idxEspacio).trim();
        }
        int idxT = limpio.indexOf('T');
        if (idxT > 0) {
            limpio = limpio.substring(0, idxT).trim();
        }

        DateTimeFormatter[] formatos = {FORMATO_GUI, FORMATO_SQL, FORMATO_BARRAS};
        for (DateTimeFormatter fmt : formatos) {
            try {
                return LocalDate.parse(limpio, fmt);
            } catch (DateTimeParseException ignored) {
            }
        }
        return null;
    }
}
