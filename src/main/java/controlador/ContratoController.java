package controlador;

import dao.ClienteDAO;
import dao.ContratoDAO;
import dao.VehiculoDAO;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import modelo.Contrato;
import servicio.ContratoService;

public class ContratoController {

    // Región TOP: Buscador, botón '+ Nuevo Contrato' y contador
    @FXML private TextField campoBusqueda;
    @FXML private Button btnNuevoContrato;
    @FXML private Label lblContadorContratos;

    // Región CENTER: TableView con atributos principales del contrato
    @FXML private TableView<Contrato> tablaContratos;
    @FXML private TableColumn<Contrato, Integer> colNro;
    @FXML private TableColumn<Contrato, String> colCliente;
    @FXML private TableColumn<Contrato, String> colVehiculo;
    @FXML private TableColumn<Contrato, String> colPatente;
    @FXML private TableColumn<Contrato, String> colFechaEntrega;
    @FXML private TableColumn<Contrato, String> colFechaDevolucion;
    @FXML private TableColumn<Contrato, Double> colImporte;
    @FXML private TableColumn<Contrato, String> colEstado;

    // Región RIGHT: Panel lateral con la información detallada del contrato seleccionado
    @FXML private VBox panelDetalleContrato;
    @FXML private Label lblTituloPanel;
    @FXML private Label lblBadgeEstadoDetalle;
    @FXML private TextField txtDetalleNro;
    @FXML private TextField txtDetalleCliente;
    @FXML private TextField txtDetalleDni;
    @FXML private TextField txtDetalleVehiculo;
    @FXML private TextField txtDetallePatente;
    @FXML private TextField txtDetalleFeEnt;
    @FXML private TextField txtDetalleFeDev;
    @FXML private TextField txtDetalleImpAlq;
    @FXML private TextField txtDetalleFianza;
    @FXML private TextField txtDetalleCondUso;

    private final ContratoService contratoService;
    private final ObservableList<Contrato> listaContratos = FXCollections.observableArrayList();
    private FilteredList<Contrato> listaFiltrada;

    public ContratoController() {
        ContratoDAO contratoDAO = new ContratoDAO();
        this.contratoService = new ContratoService(
                contratoDAO,
                new ClienteDAO(),
                new VehiculoDAO(),
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    public ContratoController(ContratoService contratoService) {
        this.contratoService = contratoService;
    }

    @FXML
    public void initialize() {
        configurarColumnas();
        configurarFiltroBusqueda();
        configurarSeleccionFila();
        cargarContratos();
    }

    private void configurarColumnas() {
        colNro.setCellValueFactory(new PropertyValueFactory<>("nro"));
        colCliente.setCellValueFactory(new PropertyValueFactory<>("clienteNombre"));
        colVehiculo.setCellValueFactory(new PropertyValueFactory<>("vehiculoModelo"));
        colPatente.setCellValueFactory(new PropertyValueFactory<>("vehiculoPatente"));
        colFechaEntrega.setCellValueFactory(new PropertyValueFactory<>("feHrEnt"));
        colFechaDevolucion.setCellValueFactory(new PropertyValueFactory<>("feHrDev"));
        colImporte.setCellValueFactory(new PropertyValueFactory<>("impAlq"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("est"));

        // Formato de moneda para el importe
        colImporte.setCellFactory(tc -> new TableCell<Contrato, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", item));
                }
            }
        });

        // Formato visual sobrio con badge para el estado
        colEstado.setCellFactory(tc -> new TableCell<Contrato, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.equalsIgnoreCase("Activo")) {
                        setStyle("-fx-text-fill: #065F46; -fx-font-weight: bold; -fx-background-color: #D1FAE5; -fx-background-radius: 6; -fx-alignment: CENTER;");
                    } else if (item.equalsIgnoreCase("Finalizado")) {
                        setStyle("-fx-text-fill: #374151; -fx-background-color: #E5E7EB; -fx-background-radius: 6; -fx-alignment: CENTER;");
                    } else {
                        setStyle("-fx-text-fill: #D97706; -fx-background-color: #FEF3C7; -fx-background-radius: 6; -fx-alignment: CENTER;");
                    }
                }
            }
        });
    }

    private void configurarFiltroBusqueda() {
        listaFiltrada = new FilteredList<>(listaContratos, p -> true);

        if (campoBusqueda != null) {
            campoBusqueda.textProperty().addListener((obs, anterior, nuevoTexto) -> {
                listaFiltrada.setPredicate(contrato -> {
                    if (nuevoTexto == null || nuevoTexto.trim().isEmpty()) {
                        return true;
                    }
                    String filtro = nuevoTexto.trim().toLowerCase();

                    String nroStr = contrato.getNro() != null ? String.valueOf(contrato.getNro()) : "";
                    String clienteStr = contrato.getClienteNombre() != null ? contrato.getClienteNombre().toLowerCase() : "";
                    String dniStr = contrato.getClienteDni() != null ? String.valueOf(contrato.getClienteDni()) : "";
                    String vehiculoStr = contrato.getVehiculoModelo() != null ? contrato.getVehiculoModelo().toLowerCase() : "";
                    String patenteStr = contrato.getVehiculoPatente() != null ? contrato.getVehiculoPatente().toLowerCase() : "";
                    String estadoStr = contrato.getEst() != null ? contrato.getEst().toLowerCase() : "";

                    return nroStr.contains(filtro)
                            || clienteStr.contains(filtro)
                            || dniStr.contains(filtro)
                            || vehiculoStr.contains(filtro)
                            || patenteStr.contains(filtro)
                            || estadoStr.contains(filtro);
                });
                actualizarContador();
            });
        }

        SortedList<Contrato> listaOrdenada = new SortedList<>(listaFiltrada);
        listaOrdenada.comparatorProperty().bind(tablaContratos.comparatorProperty());
        tablaContratos.setItems(listaOrdenada);
    }

    private void configurarSeleccionFila() {
        tablaContratos.getSelectionModel().selectedItemProperty().addListener((obs, ant, nuevo) -> {
            if (nuevo != null) {
                mostrarDetalleContrato(nuevo);
            }
        });

        tablaContratos.setRowFactory(tv -> {
            TableRow<Contrato> fila = new TableRow<>();
            fila.setOnMouseClicked(event -> {
                if (!fila.isEmpty() && event.getClickCount() == 1) {
                    mostrarDetalleContrato(fila.getItem());
                }
            });
            return fila;
        });
    }

    /**
     * Carga los contratos desde la base de datos (con fallback/unión a contratos creados en memoria).
     */
    public void cargarContratos() {
        listaContratos.clear();
        List<Contrato> desdeBd = new ArrayList<>();

        try {
            desdeBd = contratoService.listarTodos();
        } catch (Exception e) {
            System.err.println("Aviso al consultar contratos de la BD: " + e.getMessage());
        }

        listaContratos.addAll(desdeBd);

        // Añadir contratos de la sesión actual que aún no se reflejen en la lista
        for (Contrato enMemoria : NuevoContratoController.getContratosEnMemoria()) {
            boolean yaExiste = false;
            for (Contrato c : listaContratos) {
                if (c.getNro() != null && c.getNro().equals(enMemoria.getNro())) {
                    yaExiste = true;
                    break;
                }
            }
            if (!yaExiste) {
                listaContratos.add(0, enMemoria);
            }
        }

        actualizarContador();
    }

    private void actualizarContador() {
        if (lblContadorContratos != null) {
            int total = (listaFiltrada != null) ? listaFiltrada.size() : listaContratos.size();
            lblContadorContratos.setText(total == 1 ? "1 contrato registrado" : total + " contratos registrados");
        }
    }

    private void mostrarDetalleContrato(Contrato c) {
        if (panelDetalleContrato == null) {
            return;
        }

        if (lblTituloPanel != null) {
            lblTituloPanel.setText("Contrato #" + (c.getNro() != null ? c.getNro() : "(nuevo)"));
        }
        if (lblBadgeEstadoDetalle != null) {
            lblBadgeEstadoDetalle.setText(c.getEst() != null ? c.getEst().toUpperCase() : "ACTIVO");
        }
        if (txtDetalleNro != null) {
            txtDetalleNro.setText(c.getNro() != null ? String.valueOf(c.getNro()) : "-");
        }
        if (txtDetalleCliente != null) {
            txtDetalleCliente.setText(c.getClienteNombre());
        }
        if (txtDetalleDni != null) {
            txtDetalleDni.setText(c.getClienteDni() != null ? String.valueOf(c.getClienteDni()) : "-");
        }
        if (txtDetalleVehiculo != null) {
            txtDetalleVehiculo.setText(c.getVehiculoModelo());
        }
        if (txtDetallePatente != null) {
            txtDetallePatente.setText(c.getVehiculoPatente());
        }
        if (txtDetalleFeEnt != null) {
            txtDetalleFeEnt.setText(c.getFeHrEnt() != null ? c.getFeHrEnt() : "-");
        }
        if (txtDetalleFeDev != null) {
            txtDetalleFeDev.setText(c.getFeHrDev() != null ? c.getFeHrDev() : "-");
        }
        if (txtDetalleImpAlq != null) {
            txtDetalleImpAlq.setText(c.getImpAlq() != null ? String.format("$%.2f", c.getImpAlq()) : "$0.00");
        }
        if (txtDetalleFianza != null) {
            txtDetalleFianza.setText(c.getImpFianza() != null ? String.format("$%.2f", c.getImpFianza()) : "$0.00");
        }
        if (txtDetalleCondUso != null) {
            txtDetalleCondUso.setText(c.getCondUso() != null ? c.getCondUso() : "Estándar");
        }

        panelDetalleContrato.setVisible(true);
        panelDetalleContrato.setManaged(true);
    }

    @FXML
    public void cerrarPanelDetalle() {
        if (panelDetalleContrato != null) {
            panelDetalleContrato.setVisible(false);
            panelDetalleContrato.setManaged(false);
        }
        if (tablaContratos != null) {
            tablaContratos.getSelectionModel().clearSelection();
        }
    }

    /**
     * Acción del botón '+ Nuevo Contrato': navega a NuevoContratoView.fxml.
     */
    @FXML
    public void abrirNuevoContrato() {
        try {
            URL url = obtenerRecursoFxml("/NuevoContratoView.fxml");
            if (url == null) {
                mostrarAlerta("Error", "No se encontró el archivo NuevoContratoView.fxml", Alert.AlertType.ERROR);
                return;
            }

            Parent vista = FXMLLoader.load(url);

            Scene scene = (btnNuevoContrato != null && btnNuevoContrato.getScene() != null)
                    ? btnNuevoContrato.getScene()
                    : (tablaContratos != null ? tablaContratos.getScene() : null);

            if (scene != null) {
                StackPane panelCentral = (StackPane) scene.lookup("#panelCentral");
                if (panelCentral != null) {
                    panelCentral.getChildren().setAll(vista);

                    // Sincronizar el botón activo del menú lateral
                    Button btnNuevo = (Button) scene.lookup("#btnNuevo");
                    if (btnNuevo != null) {
                        btnNuevo.setStyle("-fx-background-color: #1F2937; -fx-border-color: #F59E0B; -fx-border-width: 1.5; -fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 10 14 10 12; -fx-cursor: hand; -fx-alignment: CENTER_LEFT;");
                    }
                    Button btnContratos = (Button) scene.lookup("#btnContratos");
                    if (btnContratos != null) {
                        btnContratos.setStyle("-fx-background-color: transparent; -fx-text-fill: #4B5563; -fx-font-size: 14px; -fx-font-weight: 600; -fx-alignment: CENTER_LEFT; -fx-padding: 12 16 12 16; -fx-background-radius: 10; -fx-cursor: hand;");
                    }
                }
            }
        } catch (IOException e) {
            mostrarAlerta("Error de Navegación", "No se pudo abrir Nuevo Contrato: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

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

    private void mostrarAlerta(String titulo, String contenido, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(contenido);
        alerta.showAndWait();
    }
}

