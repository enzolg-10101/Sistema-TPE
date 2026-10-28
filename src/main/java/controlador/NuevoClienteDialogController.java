package controlador;

import dao.ClienteDAO;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import modelo.Cliente;
import servicio.ClienteService;

public class NuevoClienteDialogController {

    @FXML private TextField txtDni;
    @FXML private ComboBox<String> cmbEstado;
    @FXML private TextField txtNom;
    @FXML private TextField txtApl;
    @FXML private TextField txtTel;
    @FXML private DatePicker dpFechaAlta;
    @FXML private TextField txtCorreo;
    @FXML private TextField txtDir;

    private final ClienteService clienteService = new ClienteService(new ClienteDAO());
    private Cliente clienteCreado = null;
    private boolean persistirInmediatamente = true;

    @FXML
    public void initialize() {
        if (cmbEstado != null) {
            cmbEstado.getItems().setAll("Activo", "Inactivo", "Suspendido");
            cmbEstado.getSelectionModel().selectFirst();
        }
        if (dpFechaAlta != null) {
            dpFechaAlta.setEditable(false);
            dpFechaAlta.setValue(LocalDate.now());
        }
    }

    /**
     * Pre-rellena el DNI o el Nombre si el usuario venía escribiendo una búsqueda antes de abrir la ventana.
     */
    public void prellenarDatoBusqueda(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return;
        }
        String limpio = texto.trim();
        if (limpio.matches("\\d+") && txtDni != null) {
            txtDni.setText(limpio);
        } else if (txtNom != null) {
            txtNom.setText(limpio);
        }
    }

    public Cliente getClienteCreado() {
        return clienteCreado;
    }

    public void enfocarCampoInicial() {
        if (txtDni != null) {
            if (txtDni.getText() == null || txtDni.getText().isEmpty()) {
                txtDni.requestFocus();
            } else if (txtNom != null) {
                txtNom.requestFocus();
            }
        }
    }

    @FXML
    public void guardarNuevoCliente() {
        try {
            String dniStr = txtDni.getText() != null ? txtDni.getText().trim() : "";
            String nom = txtNom.getText() != null ? txtNom.getText().trim() : "";
            String apl = txtApl.getText() != null ? txtApl.getText().trim() : "";
            String telStr = txtTel.getText() != null ? txtTel.getText().trim() : "";
            String correo = txtCorreo.getText() != null ? txtCorreo.getText().trim() : "";
            String dir = txtDir.getText() != null ? txtDir.getText().trim() : "";
            String fAlta = dpFechaAlta.getValue() != null ? dpFechaAlta.getValue().toString() : "";
            String estado = cmbEstado.getValue() != null ? cmbEstado.getValue() : "Activo";

            if (dniStr.isEmpty() || nom.isEmpty() || apl.isEmpty()
                    || telStr.isEmpty() || correo.isEmpty() || dir.isEmpty() || fAlta.isEmpty()) {
                mostrarAlerta("Campos incompletos",
                        "Por favor complete DNI, Nombre, Apellido, Teléfono, Fecha de Alta, Correo y Dirección.",
                        Alert.AlertType.WARNING);
                return;
            }

            int dni = Integer.parseInt(dniStr);
            long tel = Long.parseLong(telStr);

            // Usa el constructor sin ID: MySQL genera el C_id con AUTO_INCREMENT y ClienteDAO lo asigna con setId()
            Cliente nuevo = new Cliente(dni, nom, apl, dir, tel, correo, fAlta);
            nuevo.setEstado(estado);

            if (persistirInmediatamente) {
                try {
                    clienteService.altaCliente(nuevo);
                } catch (Exception ignored) {
                    if (nuevo.getId() == null) {
                        nuevo.setId(ClienteController.getClientesEnMemoria().size() + 10);
                    }
                }
                ClienteController.registrarClienteEnMemoria(nuevo);
            }

            this.clienteCreado = nuevo;
            cerrarVentana();

        } catch (NumberFormatException e) {
            mostrarAlerta("Error de Formato",
                    "Verifique que el DNI y el Teléfono sean valores numéricos válidos.",
                    Alert.AlertType.WARNING);
        }
    }

    @FXML
    public void cancelar() {
        this.clienteCreado = null;
        cerrarVentana();
    }

    private void cerrarVentana() {
        if (txtDni != null && txtDni.getScene() != null) {
            Stage stage = (Stage) txtDni.getScene().getWindow();
            stage.close();
        }
    }

    /**
     * Abre la ventana flotante auxiliar desde la pestaña 'Clientes' y persiste el cliente inmediatamente.
     */
    public static Cliente mostrarVentanaNuevoCliente(Window owner, String datoBusquedaInicial) {
        return mostrarVentanaNuevoCliente(owner, datoBusquedaInicial, true);
    }

    /**
     * Abre la ventana flotante auxiliar desde 'Nuevo Contrato' sin guardar aún en la base de datos,
     * para que el alta del cliente se ejecute dentro de la transacción estricta junto con el contrato.
     */
    public static Cliente mostrarVentanaNuevoClienteDiferidoParaContrato(Window owner, String datoBusquedaInicial) {
        return mostrarVentanaNuevoCliente(owner, datoBusquedaInicial, false);
    }

    private static Cliente mostrarVentanaNuevoCliente(Window owner, String datoBusquedaInicial, boolean persistirAhora) {
        try {
            URL url = obtenerUrlFxml("/NuevoClienteDialogView.fxml");
            FXMLLoader loader = new FXMLLoader(url);
            Parent raiz = loader.load();

            NuevoClienteDialogController controller = loader.getController();
            controller.persistirInmediatamente = persistirAhora;
            controller.prellenarDatoBusqueda(datoBusquedaInicial);

            Stage stage = new Stage();
            if (owner != null && owner.isShowing()) {
                stage.initOwner(owner);
                stage.initModality(Modality.WINDOW_MODAL);
            } else {
                stage.initModality(Modality.APPLICATION_MODAL);
            }
            stage.setTitle("Nuevo Cliente");
            stage.setResizable(false);

            Scene scene = new Scene(raiz);
            stage.setScene(scene);
            stage.sizeToScene();
            stage.setOnShown(e -> Platform.runLater(() -> {
                stage.sizeToScene();
                stage.requestFocus();
                controller.enfocarCampoInicial();
            }));
            stage.showAndWait();

            return controller.getClienteCreado();
        } catch (IOException e) {
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("Error al abrir ventana");
            alerta.setHeaderText(null);
            alerta.setContentText("No se pudo abrir la ventana de Nuevo Cliente: " + e.getMessage());
            alerta.showAndWait();
            return null;
        }
    }

    private static URL obtenerUrlFxml(String rutaFxml) throws IOException {
        URL url = NuevoClienteDialogController.class.getResource(rutaFxml);
        if (url == null) {
            String nombre = rutaFxml.startsWith("/") ? rutaFxml.substring(1) : rutaFxml;
            url = NuevoClienteDialogController.class.getResource("/vista/" + nombre);
            if (url == null) {
                File f1 = new File("src/main/resources/" + nombre);
                if (f1.exists()) {
                    url = f1.toURI().toURL();
                } else {
                    File f2 = new File("src/main/resources/vista/" + nombre);
                    if (f2.exists()) {
                        url = f2.toURI().toURL();
                    }
                }
            }
        }
        return url;
    }

    private void mostrarAlerta(String titulo, String contenido, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(contenido);
        alerta.showAndWait();
    }
}
