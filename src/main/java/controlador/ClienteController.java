package controlador;

import dao.ClienteDAO;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import modelo.Cliente;
import servicio.ClienteService;

public class ClienteController {

    private static final String ESTILO_CAMPO_SOLO_LECTURA =
            "-fx-background-color: #F3F4F6; -fx-background-radius: 8; "
            + "-fx-border-color: transparent; -fx-padding: 8 10 8 10; -fx-text-fill: #374151;";

    private static final String ESTILO_CAMPO_EDITABLE =
            "-fx-background-color: #FFFFFF; -fx-background-radius: 8; "
            + "-fx-border-color: #F59E0B; -fx-border-radius: 8; -fx-border-width: 1.4; "
            + "-fx-padding: 8 10 8 10; -fx-text-fill: #111827;";

    // Lista en memoria de respaldo para pruebas visuales cuando la BD no esté activa
    private static final List<Cliente> CLIENTES_EN_MEMORIA = new ArrayList<>();

    static {
        Cliente c1 = new Cliente(1, 38450123, "Martín", "Gómez", "Av. Libertador 2450", 1145678901L, "martin.gomez@email.com", "2024-03-15");
        c1.setEstado("Activo");
        Cliente c2 = new Cliente(2, 41230987, "Lucía", "Fernández", "Calle Belgrano 840", 1158901234L, "lucia.fernandez@email.com", "2024-06-10");
        c2.setEstado("Activo");
        Cliente c3 = new Cliente(3, 35980456, "Santiago", "Rodríguez", "Bv. Illia 420", 2664509876L, "santiago.rodriguez@email.com", "2023-11-22");
        c3.setEstado("Inactivo");

        CLIENTES_EN_MEMORIA.add(c1);
        CLIENTES_EN_MEMORIA.add(c2);
        CLIENTES_EN_MEMORIA.add(c3);
    }

    // Región TOP
    @FXML private TextField campoBusqueda;
    @FXML private Button btnNuevoCliente;
    @FXML private Label lblContadorClientes;

    // Región CENTER: TableView y sus 4 columnas obligatorias
    @FXML private TableView<Cliente> tablaClientes;
    @FXML private TableColumn<Cliente, Integer> colDni;
    @FXML private TableColumn<Cliente, String> colNombre;
    @FXML private TableColumn<Cliente, Long> colTelefono;
    @FXML private TableColumn<Cliente, String> colEstado;

    // Región RIGHT: Panel de información completa y edición en la misma pantalla
    @FXML private VBox panelDetalleCliente;
    @FXML private Label lblBadgeModo;
    @FXML private Label lblTituloPanel;
    @FXML private Button btnEditarCliente;

    @FXML private TextField txtDni;
    @FXML private ComboBox<String> cmbEstado;
    @FXML private TextField txtNom;
    @FXML private TextField txtApl;
    @FXML private TextField txtTel;
    @FXML private DatePicker dpFechaAlta;
    @FXML private TextField txtCorreo;
    @FXML private TextField txtDir;

    @FXML private HBox contenedorAccionesEdicion;
    @FXML private Button btnCancelarEdicion;
    @FXML private Button btnGuardarCambios;

    private final ClienteService clienteService;
    private final ObservableList<Cliente> listaCompleta = FXCollections.observableArrayList();

    private Cliente clienteSeleccionado;
    private boolean modoNuevoCliente = false;
    private boolean modoEdicionActivo = false;

    public ClienteController() {
        this.clienteService = new ClienteService(new ClienteDAO());
    }

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    public static List<Cliente> getClientesEnMemoria() {
        return CLIENTES_EN_MEMORIA;
    }

    public static void registrarClienteEnMemoria(Cliente cliente) {
        if (cliente == null) {
            return;
        }
        for (Cliente c : CLIENTES_EN_MEMORIA) {
            if (c.getDni() != null && c.getDni().equals(cliente.getDni())) {
                return;
            }
        }
        CLIENTES_EN_MEMORIA.add(cliente);
    }

    @FXML
    public void initialize() {
        // 1. Configurar las columnas del TableView usando PropertyValueFactory sobre la clase Cliente
        colDni.setCellValueFactory(new PropertyValueFactory<>("dni"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("tel"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        if (cmbEstado != null) {
            cmbEstado.getItems().setAll("Activo", "Inactivo", "Suspendido");
            cmbEstado.getSelectionModel().selectFirst();
        }

        if (dpFechaAlta != null) {
            dpFechaAlta.setEditable(false);
        }

        // 2. Filtrar la tabla en tiempo real cuando el usuario escriba en campoBusqueda
        if (campoBusqueda != null) {
            campoBusqueda.textProperty().addListener((obs, valorAnterior, nuevoTexto) -> {
                filtrarTablaEnTiempoReal(nuevoTexto);
            });
        }

        // 3. Al hacer clic sobre un cliente en la tabla, desplegar su ficha completa lateral
        if (tablaClientes != null) {
            tablaClientes.setOnMouseClicked(event -> {
                Cliente seleccionado = tablaClientes.getSelectionModel().getSelectedItem();
                if (seleccionado != null) {
                    mostrarInformacionCompletaCliente(seleccionado);
                }
            });
        }

        cargarClientes();
        cerrarPanelDetalle();
    }

    /**
     * Carga todos los clientes desde la base de datos (o desde la lista en memoria si la BD no está conectada).
     */
    public void cargarClientes() {
        List<Cliente> obtenidos;
        try {
            obtenidos = clienteService.listarTodos();
        } catch (Exception ignored) {
            obtenidos = new ArrayList<>(CLIENTES_EN_MEMORIA);
        }

        if (obtenidos == null || obtenidos.isEmpty()) {
            obtenidos = new ArrayList<>(CLIENTES_EN_MEMORIA);
        }

        listaCompleta.setAll(obtenidos);
        filtrarTablaEnTiempoReal(campoBusqueda != null ? campoBusqueda.getText() : "");
    }

    /**
     * Filtra la tabla en tiempo real por DNI, Nombre o Apellido cuando el usuario escribe en campoBusqueda.
     */
    public void filtrarTablaEnTiempoReal(String textoBusqueda) {
        if (tablaClientes == null) {
            return;
        }

        String filtro = textoBusqueda == null ? "" : textoBusqueda.trim().toLowerCase();
        if (filtro.isEmpty()) {
            tablaClientes.setItems(FXCollections.observableArrayList(listaCompleta));
            actualizarContador(listaCompleta.size());
            return;
        }

        ObservableList<Cliente> filtrados = FXCollections.observableArrayList();
        for (Cliente c : listaCompleta) {
            String dniStr = c.getDni() != null ? String.valueOf(c.getDni()) : "";
            String nombre = c.getNom() != null ? c.getNom().toLowerCase() : "";
            String apellido = c.getApl() != null ? c.getApl().toLowerCase() : "";
            String nombreCompleto = (nombre + " " + apellido).trim();

            if (dniStr.contains(filtro)
                    || nombre.contains(filtro)
                    || apellido.contains(filtro)
                    || nombreCompleto.contains(filtro)) {
                filtrados.add(c);
            }
        }

        tablaClientes.setItems(filtrados);
        actualizarContador(filtrados.size());
    }

    private void actualizarContador(int cantidad) {
        if (lblContadorClientes != null) {
            lblContadorClientes.setText(
                    (cantidad == 1 ? "1 cliente listado" : cantidad + " clientes listados")
                    + " · Haga clic sobre un cliente para ver o editar sus datos"
            );
        }
    }

    /**
     * Muestra toda la información completa del cliente seleccionado en el panel lateral en modo lectura.
     */
    public void mostrarInformacionCompletaCliente(Cliente cliente) {
        if (cliente == null) {
            return;
        }

        this.clienteSeleccionado = cliente;
        this.modoNuevoCliente = false;
        this.modoEdicionActivo = false;

        if (lblBadgeModo != null) {
            lblBadgeModo.setText("INFORMACIÓN COMPLETA");
        }
        if (lblTituloPanel != null) {
            String nombreCompleto = (cliente.getNom() != null ? cliente.getNom() : "")
                    + " " + (cliente.getApl() != null ? cliente.getApl() : "");
            lblTituloPanel.setText(nombreCompleto.trim().isEmpty() ? "Ficha del Cliente" : nombreCompleto.trim());
        }
        if (btnEditarCliente != null) {
            btnEditarCliente.setDisable(false);
            btnEditarCliente.setVisible(true);
            btnEditarCliente.setManaged(true);
        }

        txtDni.setText(cliente.getDni() != null ? String.valueOf(cliente.getDni()) : "");
        txtNom.setText(cliente.getNom() != null ? cliente.getNom() : "");
        txtApl.setText(cliente.getApl() != null ? cliente.getApl() : "");
        txtTel.setText(cliente.getTel() != null ? String.valueOf(cliente.getTel()) : "");
        txtCorreo.setText(cliente.getCorreo() != null ? cliente.getCorreo() : "");
        txtDir.setText(cliente.getDir() != null ? cliente.getDir() : "");

        if (cmbEstado != null) {
            String estadoActual = (cliente.getEstado() != null && !cliente.getEstado().isEmpty())
                    ? cliente.getEstado()
                    : "Activo";
            cmbEstado.setValue(estadoActual);
        }

        if (dpFechaAlta != null) {
            if (cliente.getfAlta() != null && !cliente.getfAlta().trim().isEmpty()) {
                try {
                    dpFechaAlta.setValue(LocalDate.parse(cliente.getfAlta().trim()));
                } catch (Exception e) {
                    dpFechaAlta.setValue(null);
                }
            } else {
                dpFechaAlta.setValue(null);
            }
        }

        if (panelDetalleCliente != null) {
            panelDetalleCliente.setVisible(true);
            panelDetalleCliente.setManaged(true);
        }
        establecerCamposEditables(false);
    }

    /**
     * Oculta el panel lateral de detalle para mantener la interfaz limpia cuando no se inspecciona un cliente.
     */
    @FXML
    public void cerrarPanelDetalle() {
        this.clienteSeleccionado = null;
        this.modoEdicionActivo = false;
        if (tablaClientes != null) {
            tablaClientes.getSelectionModel().clearSelection();
        }
        if (panelDetalleCliente != null) {
            panelDetalleCliente.setVisible(false);
            panelDetalleCliente.setManaged(false);
        }
    }

    /**
     * Habilita la edición de los campos del cliente seleccionado directamente en la misma pantalla.
     */
    @FXML
    public void habilitarEdicionCliente() {
        if (clienteSeleccionado == null) {
            return;
        }

        this.modoNuevoCliente = false;
        this.modoEdicionActivo = true;

        if (lblBadgeModo != null) {
            lblBadgeModo.setText("MODO EDICIÓN");
        }
        if (btnGuardarCambios != null) {
            btnGuardarCambios.setText("Guardar Cambios");
        }

        establecerCamposEditables(true);
        txtNom.requestFocus();
    }

    /**
     * Abre la ventana flotante auxiliar reutilizable para cargar un Nuevo Cliente.
     */
    @FXML
    public void abrirVentanaNuevoCliente() {
        javafx.stage.Window owner = (btnNuevoCliente != null && btnNuevoCliente.getScene() != null)
                ? btnNuevoCliente.getScene().getWindow()
                : null;
        String textoActual = (campoBusqueda != null) ? campoBusqueda.getText() : "";

        Cliente creado = NuevoClienteDialogController.mostrarVentanaNuevoCliente(owner, textoActual);
        if (creado != null) {
            cargarClientes();
            tablaClientes.getSelectionModel().select(creado);
            mostrarInformacionCompletaCliente(creado);
        }
    }

    /**
     * Cancela la edición en curso y restaura la vista de solo lectura en la misma pantalla.
     */
    @FXML
    public void cancelarEdicion() {
        if (clienteSeleccionado != null) {
            mostrarInformacionCompletaCliente(clienteSeleccionado);
        } else {
            cerrarPanelDetalle();
        }
    }

    /**
     * Guarda los cambios del cliente editado o registra un nuevo cliente (usando constructor sin ID).
     */
    @FXML
    public void guardarCliente() {
        if (!modoEdicionActivo) {
            return;
        }

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
                mostrarMensaje("Campos incompletos",
                        "Por favor complete DNI, Nombre, Apellido, Teléfono, Correo, Dirección y Fecha de Alta.",
                        Alert.AlertType.WARNING);
                return;
            }

            int dni = Integer.parseInt(dniStr);
            long tel = Long.parseLong(telStr);

            if (modoNuevoCliente) {
                // Alta de Nuevo Cliente: usa el constructor sin ID (C_id es AUTO_INCREMENT en MySQL)
                Cliente nuevo = new Cliente(dni, nom, apl, dir, tel, correo, fAlta);
                nuevo.setEstado(estado);

                try {
                    clienteService.altaCliente(nuevo);
                } catch (Exception ignored) {
                    if (nuevo.getId() == null) {
                        nuevo.setId(CLIENTES_EN_MEMORIA.size() + 10);
                    }
                }

                CLIENTES_EN_MEMORIA.add(nuevo);
                listaCompleta.add(nuevo);
                filtrarTablaEnTiempoReal(campoBusqueda != null ? campoBusqueda.getText() : "");
                tablaClientes.getSelectionModel().select(nuevo);
                mostrarInformacionCompletaCliente(nuevo);

                mostrarMensaje("Cliente Registrado",
                        "El cliente " + nom + " " + apl + " (DNI " + dni + ") fue registrado exitosamente.",
                        Alert.AlertType.INFORMATION);

            } else if (clienteSeleccionado != null) {
                // Edición in-situ del cliente seleccionado sin cambiar de pantalla
                clienteSeleccionado.setDni(dni);
                clienteSeleccionado.setNom(nom);
                clienteSeleccionado.setApl(apl);
                clienteSeleccionado.setTel(tel);
                clienteSeleccionado.setCorreo(correo);
                clienteSeleccionado.setDir(dir);
                clienteSeleccionado.setfAlta(fAlta);
                clienteSeleccionado.setEstado(estado);

                try {
                    clienteService.actualizarCliente(clienteSeleccionado);
                } catch (Exception ignored) {
                    // Si la base de datos no está activa en la prueba visual, se actualiza en sesión
                }

                tablaClientes.refresh();
                filtrarTablaEnTiempoReal(campoBusqueda != null ? campoBusqueda.getText() : "");
                mostrarInformacionCompletaCliente(clienteSeleccionado);

                mostrarMensaje("Cambios Guardados",
                        "Los datos del cliente " + nom + " " + apl + " se actualizaron correctamente.",
                        Alert.AlertType.INFORMATION);
            }

        } catch (NumberFormatException e) {
            mostrarMensaje("Error de Formato",
                    "Verifique que el DNI y el Teléfono contengan únicamente números válidos.",
                    Alert.AlertType.WARNING);
        } catch (Exception e) {
            mostrarMensaje("Error",
                    "No se pudo guardar la información del cliente: " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    private void establecerCamposEditables(boolean editable) {
        TextField[] campos = {txtDni, txtNom, txtApl, txtTel, txtCorreo, txtDir};
        String estilo = editable ? ESTILO_CAMPO_EDITABLE : ESTILO_CAMPO_SOLO_LECTURA;
        for (TextField campo : campos) {
            if (campo != null) {
                campo.setEditable(editable);
                campo.setStyle(estilo);
            }
        }
        if (cmbEstado != null) {
            cmbEstado.setDisable(!editable);
        }
        if (dpFechaAlta != null) {
            dpFechaAlta.setDisable(!editable);
            dpFechaAlta.setEditable(false);
        }
        if (contenedorAccionesEdicion != null) {
            contenedorAccionesEdicion.setVisible(editable);
            contenedorAccionesEdicion.setManaged(editable);
        }
    }

    private void mostrarMensaje(String titulo, String contenido, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(contenido);
        alerta.showAndWait();
    }
}
