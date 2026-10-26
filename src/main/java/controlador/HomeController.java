package controlador;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

public class HomeController {

    private static final String ESTILO_BOTON_INACTIVO =
            "-fx-background-color: transparent; -fx-text-fill: #4B5563; -fx-font-size: 14px; "
            + "-fx-font-weight: 600; -fx-alignment: CENTER_LEFT; -fx-padding: 12 16 12 16; "
            + "-fx-background-radius: 10; -fx-cursor: hand;";

    private static final String ESTILO_BOTON_ACTIVO =
            "-fx-background-color: #FEF3C7; -fx-text-fill: #D97706; -fx-font-size: 14px; "
            + "-fx-font-weight: bold; -fx-alignment: CENTER_LEFT; -fx-padding: 12 16 12 16; "
            + "-fx-background-radius: 10; -fx-cursor: hand;";

    private static final String ESTILO_NUEVO_INACTIVO =
            "-fx-background-color: #111827; -fx-background-radius: 12; -fx-padding: 10 14 10 12; "
            + "-fx-cursor: hand; -fx-alignment: CENTER_LEFT;";

    private static final String ESTILO_NUEVO_ACTIVO =
            "-fx-background-color: #1F2937; -fx-border-color: #F59E0B; -fx-border-width: 1.5; "
            + "-fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 10 14 10 12; "
            + "-fx-cursor: hand; -fx-alignment: CENTER_LEFT;";

    @FXML private StackPane panelCentral;

    @FXML private Button btnNuevo;
    @FXML private Button btnEmpleados;
    @FXML private Button btnVehiculos;
    @FXML private Button btnSucursales;
    @FXML private Button btnClientes;
    @FXML private Button btnContratos;

    @FXML
    public void initialize() {
        // El sistema inicia con el HomeScreen vacío (sin obligar a estar en una pantalla)
        activarBoton(null);
    }

    @FXML
    public void abrirPantallaNuevoContrato(ActionEvent event) {
        activarBoton(btnNuevo);
        cargarVista("/NuevoContratoView.fxml");
    }

    @FXML
    public void abrirPantallaEmpleados(ActionEvent event) {
        activarBoton(btnEmpleados);
        cargarVista("/EmpleadoView.fxml");
    }

    @FXML
    public void abrirPantallaVehiculos(ActionEvent event) {
        activarBoton(btnVehiculos);
        cargarVista("/VehiculoView.fxml");
    }

    @FXML
    public void abrirPantallaSucursales(ActionEvent event) {
        activarBoton(btnSucursales);
        cargarVista("/SucursalView.fxml");
    }

    @FXML
    public void abrirPantallaClientes(ActionEvent event) {
        activarBoton(btnClientes);
        cargarVista("/ClienteView.fxml");
    }

    @FXML
    public void abrirPantallaContratos(ActionEvent event) {
        activarBoton(btnContratos);
        cargarVista("/ContratoView.fxml");
    }

    /**
     * Carga el archivo FXML indicado y lo establece como único contenido de panelCentral.
     */
    private void cargarVista(String rutaFxml) {
        try {
            URL url = obtenerRecursoFxml(rutaFxml);
            if (url == null) {
                mostrarAlerta("Vista no disponible",
                        "Aún no se ha creado el archivo de vista: " + rutaFxml,
                        Alert.AlertType.INFORMATION);
                return;
            }

            Parent vista = FXMLLoader.load(url);
            panelCentral.getChildren().setAll(vista);

        } catch (IOException e) {
            mostrarAlerta("Error al cargar pantalla",
                    "No se pudo cargar la vista " + rutaFxml + ": " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    /**
     * Busca el recurso FXML en la raíz del classpath, en /vista/ o en src/main/resources.
     */
    private URL obtenerRecursoFxml(String rutaFxml) throws IOException {
        URL url = getClass().getResource(rutaFxml);
        if (url == null) {
            String nombreArchivo = rutaFxml.startsWith("/") ? rutaFxml.substring(1) : rutaFxml;
            url = getClass().getResource("/vista/" + nombreArchivo);
            if (url == null) {
                File archivoRaiz = new File("src/main/resources/" + nombreArchivo);
                if (archivoRaiz.exists()) {
                    url = archivoRaiz.toURI().toURL();
                } else {
                    File archivoVista = new File("src/main/resources/vista/" + nombreArchivo);
                    if (archivoVista.exists()) {
                        url = archivoVista.toURI().toURL();
                    }
                }
            }
        }
        return url;
    }

    /**
     * Resalta visualmente el botón de la sección activa en el menú lateral.
     */
    private void activarBoton(Button botonActivo) {
        if (btnNuevo != null) {
            btnNuevo.setStyle(botonActivo == btnNuevo ? ESTILO_NUEVO_ACTIVO : ESTILO_NUEVO_INACTIVO);
        }
        Button[] botones = {btnEmpleados, btnVehiculos, btnSucursales, btnClientes, btnContratos};
        for (Button btn : botones) {
            if (btn != null) {
                btn.setStyle(btn == botonActivo ? ESTILO_BOTON_ACTIVO : ESTILO_BOTON_INACTIVO);
            }
        }
    }

    private void mostrarAlerta(String titulo, String contenido, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(contenido);
        alerta.showAndWait();
    }
}
