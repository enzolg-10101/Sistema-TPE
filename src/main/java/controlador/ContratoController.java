package controlador;

import dao.ClienteDAO;
import dao.ContratoDAO;
import dao.SucursalDAO;
import dao.VehiculoDAO;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.StringConverter;
import modelo.Contrato;
import modelo.MedioPago;
import modelo.Pago;
import modelo.Sucursal;
import servicio.ContratoService;
import servicio.PagoService;
import util.FormatoFecha;

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
    @FXML private TextField txtDetalleCargos;
    @FXML private TextField txtDetalleCondUso;

    // Campos de Liquidación y Pagos
    @FXML private TextField txtDetalleTotal;
    @FXML private TextField txtDetalleTotalPagado;
    @FXML private TextField txtDetalleSaldoPendiente;
    @FXML private Label lblCantidadPagos;
    @FXML private TableView<Pago> tablaPagosContrato;
    @FXML private TableColumn<Pago, String> colPagoFecha;
    @FXML private TableColumn<Pago, MedioPago> colPagoMedio;
    @FXML private TableColumn<Pago, Double> colPagoImporte;

    // Botones de acción
    @FXML private Button btnRegistrarDevolucion;
    @FXML private Button btnRegistrarPago;

    private final ContratoService contratoService;
    private final PagoService pagoService;
    private final SucursalDAO sucursalDAO = new SucursalDAO();
    private final ObservableList<Contrato> listaContratos = FXCollections.observableArrayList();
    private FilteredList<Contrato> listaFiltrada;
    private Contrato contratoSeleccionado;

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
        this.pagoService = new PagoService(new dao.PagoDAO(), contratoDAO);
    }

    public ContratoController(ContratoService contratoService, PagoService pagoService) {
        this.contratoService = contratoService;
        this.pagoService = pagoService != null ? pagoService : new PagoService();
    }

    @FXML
    public void initialize() {
        configurarColumnas();
        configurarColumnasPagos();
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

        // Formato dd-mm-aaaa para las columnas de Fecha Entrega y Fecha Devolución
        colFechaEntrega.setCellFactory(tc -> new TableCell<Contrato, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(FormatoFecha.formatearFechaGui(item));
                }
            }
        });

        colFechaDevolucion.setCellFactory(tc -> new TableCell<Contrato, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(FormatoFecha.formatearFechaGui(item));
                }
            }
        });

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
                    if (item.equalsIgnoreCase("Activo") || item.equalsIgnoreCase("Alquilado")) {
                        setStyle("-fx-text-fill: #065F46; -fx-font-weight: bold; -fx-background-color: #D1FAE5; -fx-background-radius: 6; -fx-alignment: CENTER;");
                    } else if (item.equalsIgnoreCase("Devuelto")) {
                        setStyle("-fx-text-fill: #D97706; -fx-font-weight: bold; -fx-background-color: #FEF3C7; -fx-background-radius: 6; -fx-alignment: CENTER;");
                    } else if (item.equalsIgnoreCase("Pagado") || item.equalsIgnoreCase("Finalizado")) {
                        setStyle("-fx-text-fill: #1E40AF; -fx-font-weight: bold; -fx-background-color: #DBEAFE; -fx-background-radius: 6; -fx-alignment: CENTER;");
                    } else {
                        setStyle("-fx-text-fill: #374151; -fx-background-color: #E5E7EB; -fx-background-radius: 6; -fx-alignment: CENTER;");
                    }
                }
            }
        });
    }

    private void configurarColumnasPagos() {
        if (colPagoFecha != null) {
            colPagoFecha.setCellValueFactory(new PropertyValueFactory<>("fec"));
            colPagoFecha.setCellFactory(tc -> new TableCell<Pago, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(FormatoFecha.formatearFechaGui(item));
                    }
                }
            });
        }

        if (colPagoMedio != null) {
            colPagoMedio.setCellValueFactory(new PropertyValueFactory<>("medio"));
            colPagoMedio.setCellFactory(tc -> new TableCell<Pago, MedioPago>() {
                @Override
                protected void updateItem(MedioPago item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getDescripcion());
                    }
                }
            });
        }

        if (colPagoImporte != null) {
            colPagoImporte.setCellValueFactory(new PropertyValueFactory<>("imp"));
            colPagoImporte.setCellFactory(tc -> new TableCell<Pago, Double>() {
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
        }
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
                    String feEntStr = FormatoFecha.formatearFechaGui(contrato.getFeHrEnt()).toLowerCase();
                    String feDevStr = FormatoFecha.formatearFechaGui(contrato.getFeHrDev()).toLowerCase();

                    return nroStr.contains(filtro)
                            || clienteStr.contains(filtro)
                            || dniStr.contains(filtro)
                            || vehiculoStr.contains(filtro)
                            || patenteStr.contains(filtro)
                            || estadoStr.contains(filtro)
                            || feEntStr.contains(filtro)
                            || feDevStr.contains(filtro);
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
        this.contratoSeleccionado = c;
        if (panelDetalleContrato == null) {
            return;
        }

        if (lblTituloPanel != null) {
            lblTituloPanel.setText("Contrato #" + (c.getNro() != null ? c.getNro() : "(nuevo)"));
        }

        double total = (c.getImpAlq() != null ? c.getImpAlq() : 0.0) + (c.getCargos() != null ? c.getCargos() : 0.0);
        double pagado = (c.getNro() != null) ? pagoService.obtenerTotalPagado(c.getNro()) : 0.0;
        double saldo = Math.max(0.0, total - pagado);

        String estado = (c.getEst() != null) ? c.getEst().trim() : "Activo";
        boolean yaDevuelto = estado.equalsIgnoreCase("Devuelto") || estado.equalsIgnoreCase("Finalizado");
        boolean yaPagado = estado.equalsIgnoreCase("Pagado") || (yaDevuelto && saldo <= 0.001 && total > 0);

        if (yaPagado && !estado.equalsIgnoreCase("Pagado")) {
            c.setEst("Pagado");
            estado = "Pagado";
            NuevoContratoController.marcarContratoPagadoEnMemoria(c.getNro());
        }

        if (lblBadgeEstadoDetalle != null) {
            if (yaPagado) {
                lblBadgeEstadoDetalle.setText("PAGADO");
                lblBadgeEstadoDetalle.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #1E40AF; -fx-background-color: #DBEAFE; -fx-background-radius: 8; -fx-padding: 3 10 3 10;");
            } else if (yaDevuelto) {
                lblBadgeEstadoDetalle.setText("DEVUELTO (SALDO: " + String.format("$%.2f", saldo) + ")");
                lblBadgeEstadoDetalle.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #D97706; -fx-background-color: #FEF3C7; -fx-background-radius: 8; -fx-padding: 3 10 3 10;");
            } else {
                lblBadgeEstadoDetalle.setText("ACTIVO");
                lblBadgeEstadoDetalle.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #065F46; -fx-background-color: #D1FAE5; -fx-background-radius: 8; -fx-padding: 3 10 3 10;");
            }
        }

        if (btnRegistrarDevolucion != null) {
            btnRegistrarDevolucion.setDisable(yaDevuelto);
            btnRegistrarDevolucion.setMinHeight(38.0);
            btnRegistrarDevolucion.setPrefHeight(38.0);
            if (yaDevuelto) {
                btnRegistrarDevolucion.setText("Vehículo Ya Devuelto");
                btnRegistrarDevolucion.setStyle("-fx-background-color: #E5E7EB; -fx-text-fill: #9CA3AF; -fx-font-size: 12px; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 8 16 8 16; -fx-cursor: default;");
            } else {
                btnRegistrarDevolucion.setText("Registrar Devolución");
                btnRegistrarDevolucion.setStyle("-fx-background-color: #262626; -fx-text-fill: #FFFFFF; -fx-font-size: 12px; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 8 16 8 16; -fx-cursor: hand;");
            }
        }

        if (btnRegistrarPago != null) {
            btnRegistrarPago.setMinHeight(38.0);
            btnRegistrarPago.setPrefHeight(38.0);
            if (yaPagado) {
                btnRegistrarPago.setDisable(true);
                btnRegistrarPago.setText("Contrato Pagado");
                btnRegistrarPago.setStyle("-fx-background-color: #E5E7EB; -fx-text-fill: #9CA3AF; -fx-font-size: 12px; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 8 16 8 16; -fx-cursor: default;");
            } else if (yaDevuelto) {
                btnRegistrarPago.setDisable(false);
                btnRegistrarPago.setText("💳 Registrar Pago");
                btnRegistrarPago.setStyle("-fx-background-color: #059669; -fx-text-fill: #FFFFFF; -fx-font-size: 12px; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 8 16 8 16; -fx-cursor: hand;");
            } else {
                btnRegistrarPago.setDisable(true);
                btnRegistrarPago.setText("💳 Pago Habilitado tras Devolución");
                btnRegistrarPago.setStyle("-fx-background-color: #F3F4F6; -fx-text-fill: #9CA3AF; -fx-font-size: 11.5px; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 8 16 8 16; -fx-cursor: default;");
            }
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
            txtDetalleFeEnt.setText(c.getFeHrEnt() != null ? FormatoFecha.formatearFechaGui(c.getFeHrEnt()) : "-");
        }
        if (txtDetalleFeDev != null) {
            txtDetalleFeDev.setText(c.getFeHrDev() != null ? FormatoFecha.formatearFechaGui(c.getFeHrDev()) : "-");
        }
        if (txtDetalleImpAlq != null) {
            txtDetalleImpAlq.setText(c.getImpAlq() != null ? String.format("$%.2f", c.getImpAlq()) : "$0.00");
        }
        if (txtDetalleFianza != null) {
            txtDetalleFianza.setText(c.getImpFianza() != null ? String.format("$%.2f", c.getImpFianza()) : "$0.00");
        }
        if (txtDetalleCargos != null) {
            txtDetalleCargos.setText(c.getCargos() != null ? String.format("$%.2f", c.getCargos()) : "$0.00");
        }
        if (txtDetalleCondUso != null) {
            txtDetalleCondUso.setText(c.getCondUso() != null ? c.getCondUso() : "Estándar");
        }

        if (txtDetalleTotal != null) {
            txtDetalleTotal.setText(String.format("$%.2f", total));
        }
        if (txtDetalleTotalPagado != null) {
            txtDetalleTotalPagado.setText(String.format("$%.2f", pagado));
        }
        if (txtDetalleSaldoPendiente != null) {
            txtDetalleSaldoPendiente.setText(String.format("$%.2f", saldo));
        }

        // Cargar tabla de pagos registrados
        if (tablaPagosContrato != null && c.getNro() != null) {
            List<Pago> pagos = pagoService.listarPorContrato(c.getNro());
            tablaPagosContrato.setItems(FXCollections.observableArrayList(pagos));
            if (lblCantidadPagos != null) {
                lblCantidadPagos.setText(pagos.size() == 1 ? "1 pago" : pagos.size() + " pagos");
            }
        }

        panelDetalleContrato.setVisible(true);
        panelDetalleContrato.setManaged(true);
    }

    @FXML
    public void cerrarPanelDetalle() {
        this.contratoSeleccionado = null;
        if (panelDetalleContrato != null) {
            panelDetalleContrato.setVisible(false);
            panelDetalleContrato.setManaged(false);
        }
        if (tablaContratos != null) {
            tablaContratos.getSelectionModel().clearSelection();
        }
    }

    /**
     * Abre el modal para capturar los datos y ejecutar el procedimiento 'registrar_devolucion'.
     */
    @FXML
    public void abrirDialogoDevolucion() {
        if (contratoSeleccionado == null) {
            mostrarAlerta("Seleccione un contrato",
                    "Debe seleccionar un contrato de la lista para registrar su devolución.",
                    Alert.AlertType.WARNING);
            return;
        }

        if (contratoSeleccionado.getEst() != null && (contratoSeleccionado.getEst().equalsIgnoreCase("Devuelto") || contratoSeleccionado.getEst().equalsIgnoreCase("Pagado"))) {
            mostrarAlerta("Contrato ya devuelto",
                    "Este contrato ya se encuentra en estado '" + contratoSeleccionado.getEst() + "'.",
                    Alert.AlertType.INFORMATION);
            return;
        }

        Stage ventana = new Stage();
        ventana.initModality(Modality.APPLICATION_MODAL);
        Window owner = (tablaContratos != null && tablaContratos.getScene() != null)
                ? tablaContratos.getScene().getWindow()
                : (btnRegistrarDevolucion != null && btnRegistrarDevolucion.getScene() != null ? btnRegistrarDevolucion.getScene().getWindow() : null);
        if (owner != null) {
            ventana.initOwner(owner);
        }
        ventana.setTitle("Registrar Devolución · Contrato #" + contratoSeleccionado.getNro());

        VBox root = new VBox(16.0);
        root.setPrefWidth(520.0);
        root.setPadding(new Insets(24.0));
        root.setStyle("-fx-background-color: #F3F4F6; -fx-font-family: 'Segoe UI', 'Inter', sans-serif;");

        // Tarjeta Encabezado
        VBox tarjetaEncabezado = new VBox(6.0);
        tarjetaEncabezado.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 14; -fx-border-color: #E5E7EB; -fx-border-radius: 14; -fx-padding: 16;");

        HBox filaBadge = new HBox(8.0);
        filaBadge.setAlignment(Pos.CENTER_LEFT);
        Label badge = new Label("PROCEDIMIENTO ALMACENADO");
        badge.setStyle("-fx-background-color: #FEF3C7; -fx-text-fill: #D97706; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 3 8 3 8; -fx-background-radius: 6;");
        Label lblNro = new Label("Contrato N° " + contratoSeleccionado.getNro());
        lblNro.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #111827;");
        filaBadge.getChildren().addAll(lblNro, badge);

        Label lblSubtitulo = new Label("Vehículo: " + contratoSeleccionado.getVehiculoModelo()
                + " (Patente: " + contratoSeleccionado.getVehiculoPatente() + ")  ·  Cliente: " + contratoSeleccionado.getClienteNombre());
        lblSubtitulo.setStyle("-fx-font-size: 12px; -fx-text-fill: #4B5563;");

        tarjetaEncabezado.getChildren().addAll(filaBadge, lblSubtitulo);

        // Tarjeta Formulario de Devolución
        VBox tarjetaFormulario = new VBox(12.0);
        tarjetaFormulario.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 14; -fx-border-color: #E5E7EB; -fx-border-radius: 14; -fx-padding: 18;");

        Label lblFormTitulo = new Label("Datos de Check-In y Devolución");
        lblFormTitulo.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #111827;");

        GridPane grid = new GridPane();
        grid.setHgap(14.0);
        grid.setVgap(12.0);

        // 1. Kilometraje de entrada (kmj)
        Label lblKmj = new Label("Kilometraje de Devolución (kmj) *");
        lblKmj.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #374151;");
        TextField txtKmj = new TextField();
        txtKmj.setPromptText("Ej. 45000");
        txtKmj.setStyle("-fx-background-color: #F3F4F6; -fx-background-radius: 8; -fx-padding: 9 12 9 12;");

        // 2. Nivel Combustible (nivComb)
        Label lblComb = new Label("Nivel Combustible % (nivComb) *");
        lblComb.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #374151;");
        TextField txtComb = new TextField("100.0");
        txtComb.setPromptText("0.0 a 100.0");
        txtComb.setStyle("-fx-background-color: #F3F4F6; -fx-background-radius: 8; -fx-padding: 9 12 9 12;");

        // 3. Cargos Adicionales (cargos)
        Label lblCargos = new Label("Cargos Adicionales $ (cargos) *");
        lblCargos.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #374151;");
        TextField txtCargos = new TextField("0.00");
        txtCargos.setPromptText("0.00");
        txtCargos.setStyle("-fx-background-color: #F3F4F6; -fx-background-radius: 8; -fx-padding: 9 12 9 12;");

        // 4. Sucursal de Devolución (sucEnt)
        Label lblSuc = new Label("Sucursal de Devolución (sucEnt) *");
        lblSuc.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #374151;");
        ComboBox<Sucursal> cmbSucursal = new ComboBox<>();
        cmbSucursal.setMaxWidth(Double.MAX_VALUE);
        cmbSucursal.setStyle("-fx-background-color: #F3F4F6; -fx-background-radius: 8; -fx-padding: 2;");

        List<Sucursal> sucursales = new ArrayList<>();
        try {
            sucursales = sucursalDAO.listarTodas();
        } catch (Exception ignored) {
        }
        if (sucursales.isEmpty()) {
            sucursales.add(new Sucursal(1, "Sucursal Central", "Principal 123", 11223344L));
            sucursales.add(new Sucursal(2, "Sucursal Norte", "Norte 456", 11556677L));
        }
        cmbSucursal.setItems(FXCollections.observableArrayList(sucursales));
        cmbSucursal.setConverter(new StringConverter<Sucursal>() {
            @Override
            public String toString(Sucursal s) {
                return (s != null) ? (s.getCod() + " - " + s.getNom()) : "";
            }
            @Override
            public Sucursal fromString(String string) {
                return null;
            }
        });
        cmbSucursal.getSelectionModel().selectFirst();

        // 5. Daños Nuevos (daNuevos)
        Label lblDanios = new Label("Daños Nuevos Detectados (daNuevos)");
        lblDanios.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #374151;");
        TextField txtDanios = new TextField("Ninguno");
        txtDanios.setPromptText("Ej. Rayón en puerta, ninguno...");
        txtDanios.setStyle("-fx-background-color: #F3F4F6; -fx-background-radius: 8; -fx-padding: 9 12 9 12;");

        grid.add(new VBox(4.0, lblKmj, txtKmj), 0, 0);
        grid.add(new VBox(4.0, lblComb, txtComb), 1, 0);
        grid.add(new VBox(4.0, lblCargos, txtCargos), 0, 1);
        grid.add(new VBox(4.0, lblSuc, cmbSucursal), 1, 1);
        grid.add(new VBox(4.0, lblDanios, txtDanios), 0, 2, 2, 1);

        tarjetaFormulario.getChildren().addAll(lblFormTitulo, grid);

        // Barra de Botones
        HBox barraBotones = new HBox(12.0);
        barraBotones.setAlignment(Pos.CENTER_RIGHT);
        barraBotones.setMinHeight(44.0);
        barraBotones.setPrefHeight(44.0);

        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setMinHeight(40.0);
        btnCancelar.setPrefHeight(40.0);
        btnCancelar.setStyle("-fx-background-color: #E5E7EB; -fx-text-fill: #374151; -fx-font-size: 12px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 10 18 10 18; -fx-cursor: hand;");
        btnCancelar.setOnAction(e -> ventana.close());

        Button btnConfirmar = new Button("Confirmar Devolución");
        btnConfirmar.setMinHeight(40.0);
        btnConfirmar.setPrefHeight(40.0);
        btnConfirmar.setStyle("-fx-background-color: #262626; -fx-text-fill: #FFFFFF; -fx-font-size: 12px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 10 20 10 20; -fx-cursor: hand;");

        btnConfirmar.setOnAction(e -> {
            try {
                String kmjTexto = txtKmj.getText() != null ? txtKmj.getText().trim() : "";
                if (kmjTexto.isEmpty()) {
                    mostrarAlerta("Campo requerido", "Ingrese el kilometraje de devolución.", Alert.AlertType.WARNING);
                    txtKmj.requestFocus();
                    return;
                }
                int kmjVal = Integer.parseInt(kmjTexto);
                if (kmjVal < 0) {
                    mostrarAlerta("Valor inválido", "El kilometraje no puede ser negativo.", Alert.AlertType.WARNING);
                    return;
                }

                String combTexto = txtComb.getText() != null ? txtComb.getText().trim().replace(",", ".") : "";
                double combVal = Double.parseDouble(combTexto);
                if (combVal < 0.0 || combVal > 100.0) {
                    mostrarAlerta("Valor inválido", "El nivel de combustible debe estar entre 0 y 100.", Alert.AlertType.WARNING);
                    return;
                }

                String cargosTexto = txtCargos.getText() != null ? txtCargos.getText().trim().replace(",", ".") : "0";
                double cargosVal = Double.parseDouble(cargosTexto);
                if (cargosVal < 0.0) {
                    mostrarAlerta("Valor inválido", "Los cargos adicionales no pueden ser negativos.", Alert.AlertType.WARNING);
                    return;
                }

                Sucursal sucSel = cmbSucursal.getValue();
                int sucId = (sucSel != null && sucSel.getCod() != null) ? sucSel.getCod() : 1;

                String daniosVal = (txtDanios.getText() != null && !txtDanios.getText().trim().isEmpty())
                        ? txtDanios.getText().trim()
                        : "Ninguno";

                // Ejecución del procedimiento almacenado
                contratoService.registrarDevolucion(
                        contratoSeleccionado.getNro(),
                        kmjVal,
                        combVal,
                        daniosVal,
                        cargosVal,
                        sucId
                );

                contratoSeleccionado.setEst("Devuelto");
                contratoSeleccionado.setCargos(cargosVal);
                NuevoContratoController.marcarContratoDevueltoEnMemoria(contratoSeleccionado.getNro(), cargosVal);

                ventana.close();

                mostrarAlerta("Devolución Exitosa",
                        "Se registró la devolución del vehículo con éxito.\nContrato N° "
                        + contratoSeleccionado.getNro() + " actualizado a estado 'Devuelto'.\nAhora puede registrar los pagos correspondientes.",
                        Alert.AlertType.INFORMATION);

                cargarContratos();
                mostrarDetalleContrato(contratoSeleccionado);

            } catch (NumberFormatException ex) {
                mostrarAlerta("Error de Formato", "Verifique que los valores numéricos tengan formato válido.", Alert.AlertType.WARNING);
            } catch (Exception ex) {
                mostrarAlerta("Error al registrar devolución", ex.getMessage(), Alert.AlertType.ERROR);
            }
        });

        barraBotones.getChildren().addAll(btnCancelar, btnConfirmar);
        root.getChildren().addAll(tarjetaEncabezado, tarjetaFormulario, barraBotones);

        Scene scene = new Scene(root);
        ventana.setScene(scene);
        ventana.setResizable(false);
        ventana.sizeToScene();
        ventana.showAndWait();
    }

    /**
     * Abre el modal para capturar los datos y ejecutar la transacción 4.6 (Alta en la entidad Pagos).
     */
    @FXML
    public void abrirDialogoPago() {
        if (contratoSeleccionado == null) {
            mostrarAlerta("Seleccione un contrato",
                    "Debe seleccionar un contrato de la lista para registrar un pago.",
                    Alert.AlertType.WARNING);
            return;
        }

        String est = (contratoSeleccionado.getEst() != null) ? contratoSeleccionado.getEst().trim() : "Activo";
        if (est.equalsIgnoreCase("Activo") || est.equalsIgnoreCase("Alquilado")) {
            mostrarAlerta("Contrato en curso",
                    "Para registrar un pago y liquidar el contrato, primero debe registrar la devolución del vehículo (para establecer los cargos finales).",
                    Alert.AlertType.WARNING);
            return;
        }

        double saldoActual = pagoService.calcularSaldoPendiente(contratoSeleccionado);
        if (saldoActual <= 0.001 || est.equalsIgnoreCase("Pagado")) {
            mostrarAlerta("Contrato Cancelado",
                    "Este contrato ya se encuentra completamente pagado (saldo pendiente $0.00).",
                    Alert.AlertType.INFORMATION);
            return;
        }

        Stage ventana = new Stage();
        ventana.initModality(Modality.APPLICATION_MODAL);
        Window owner = (tablaContratos != null && tablaContratos.getScene() != null)
                ? tablaContratos.getScene().getWindow()
                : (btnRegistrarPago != null && btnRegistrarPago.getScene() != null ? btnRegistrarPago.getScene().getWindow() : null);
        if (owner != null) {
            ventana.initOwner(owner);
        }
        ventana.setTitle("Registrar Pago · Contrato #" + contratoSeleccionado.getNro());

        VBox root = new VBox(16.0);
        root.setPrefWidth(520.0);
        root.setPadding(new Insets(24.0));
        root.setStyle("-fx-background-color: #F3F4F6; -fx-font-family: 'Segoe UI', 'Inter', sans-serif;");

        // Tarjeta Encabezado
        VBox tarjetaEncabezado = new VBox(6.0);
        tarjetaEncabezado.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 14; -fx-border-color: #E5E7EB; -fx-border-radius: 14; -fx-padding: 16;");

        HBox filaBadge = new HBox(8.0);
        filaBadge.setAlignment(Pos.CENTER_LEFT);
        Label badge = new Label("TRANSACCIÓN 4.6 · ALTA DE PAGO");
        badge.setStyle("-fx-background-color: #DBEAFE; -fx-text-fill: #1E40AF; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 3 8 3 8; -fx-background-radius: 6;");
        Label lblNro = new Label("Pago de Contrato N° " + contratoSeleccionado.getNro());
        lblNro.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #111827;");
        filaBadge.getChildren().addAll(lblNro, badge);

        Label lblSubtitulo = new Label("Cliente: " + contratoSeleccionado.getClienteNombre()
                + " (DNI: " + (contratoSeleccionado.getClienteDni() != null ? contratoSeleccionado.getClienteDni() : "-") + ")  ·  "
                + contratoSeleccionado.getVehiculoModelo() + " (" + contratoSeleccionado.getVehiculoPatente() + ")");
        lblSubtitulo.setStyle("-fx-font-size: 12px; -fx-text-fill: #4B5563;");

        tarjetaEncabezado.getChildren().addAll(filaBadge, lblSubtitulo);

        // Tarjeta Resumen Financiero
        VBox tarjetaFinanciera = new VBox(10.0);
        tarjetaFinanciera.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 14; -fx-border-color: #E5E7EB; -fx-border-radius: 14; -fx-padding: 16;");

        Label lblFinancieroTitulo = new Label("Estado de Cuenta del Contrato");
        lblFinancieroTitulo.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #111827;");

        double totalLiq = pagoService.calcularTotalContrato(contratoSeleccionado);
        double yaPagado = pagoService.obtenerTotalPagado(contratoSeleccionado.getNro());

        GridPane gridFinanzas = new GridPane();
        gridFinanzas.setHgap(12.0);
        gridFinanzas.setVgap(6.0);

        Label lblTot = new Label("Total Liquidado: " + String.format("$%.2f", totalLiq));
        lblTot.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #374151;");

        Label lblAbonado = new Label("Abonado a la Fecha: " + String.format("$%.2f", yaPagado));
        lblAbonado.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #047857;");

        Label lblSaldo = new Label("Saldo Pendiente: " + String.format("$%.2f", saldoActual));
        lblSaldo.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #B91C1C;");

        gridFinanzas.add(lblTot, 0, 0);
        gridFinanzas.add(lblAbonado, 1, 0);
        gridFinanzas.add(lblSaldo, 0, 1, 2, 1);

        tarjetaFinanciera.getChildren().addAll(lblFinancieroTitulo, gridFinanzas);

        // Tarjeta Formulario de Pago
        VBox tarjetaFormulario = new VBox(12.0);
        tarjetaFormulario.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 14; -fx-border-color: #E5E7EB; -fx-border-radius: 14; -fx-padding: 18;");

        Label lblFormTitulo = new Label("Detalles del Comprobante de Pago");
        lblFormTitulo.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #111827;");

        GridPane grid = new GridPane();
        grid.setHgap(14.0);
        grid.setVgap(12.0);

        // 1. Fecha de Pago (P_fec)
        Label lblFec = new Label("Fecha de Pago (P_fec) *");
        lblFec.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #374151;");
        DatePicker dpFechaPago = new DatePicker(LocalDate.now());
        dpFechaPago.setMaxWidth(Double.MAX_VALUE);
        FormatoFecha.configurarDatePicker(dpFechaPago);
        dpFechaPago.setStyle("-fx-background-color: #F3F4F6; -fx-background-radius: 8;");

        // 2. Medio de Pago (P_medio)
        Label lblMedio = new Label("Medio de Pago (P_medio) *");
        lblMedio.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #374151;");
        ComboBox<MedioPago> cmbMedio = new ComboBox<>(FXCollections.observableArrayList(MedioPago.values()));
        cmbMedio.setMaxWidth(Double.MAX_VALUE);
        cmbMedio.getSelectionModel().select(MedioPago.EFECTIVO);
        cmbMedio.setStyle("-fx-background-color: #F3F4F6; -fx-background-radius: 8; -fx-padding: 2;");

        // 3. Importe del Pago (P_imp) prellenado con el saldo pendiente
        Label lblImp = new Label("Monto a Abonar (P_imp) *");
        lblImp.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #374151;");
        TextField txtImp = new TextField(String.format(java.util.Locale.US, "%.2f", saldoActual));
        txtImp.setPromptText("Ej. 15000.00");
        txtImp.setStyle("-fx-background-color: #FEF3C7; -fx-background-radius: 8; -fx-padding: 9 12 9 12; -fx-font-weight: bold; -fx-text-fill: #D97706;");

        grid.add(new VBox(4.0, lblFec, dpFechaPago), 0, 0);
        grid.add(new VBox(4.0, lblMedio, cmbMedio), 1, 0);
        grid.add(new VBox(4.0, lblImp, txtImp), 0, 1, 2, 1);

        tarjetaFormulario.getChildren().addAll(lblFormTitulo, grid);

        // Barra de Botones
        HBox barraBotones = new HBox(12.0);
        barraBotones.setAlignment(Pos.CENTER_RIGHT);
        barraBotones.setMinHeight(44.0);
        barraBotones.setPrefHeight(44.0);

        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setMinHeight(40.0);
        btnCancelar.setPrefHeight(40.0);
        btnCancelar.setStyle("-fx-background-color: #E5E7EB; -fx-text-fill: #374151; -fx-font-size: 12px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 10 18 10 18; -fx-cursor: hand;");
        btnCancelar.setOnAction(e -> ventana.close());

        Button btnConfirmar = new Button("Confirmar Pago");
        btnConfirmar.setMinHeight(40.0);
        btnConfirmar.setPrefHeight(40.0);
        btnConfirmar.setStyle("-fx-background-color: #059669; -fx-text-fill: #FFFFFF; -fx-font-size: 12px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 10 20 10 20; -fx-cursor: hand;");

        btnConfirmar.setOnAction(e -> {
            try {
                LocalDate fechaVal = dpFechaPago.getValue();
                if (fechaVal == null) {
                    mostrarAlerta("Campo requerido", "Seleccione la fecha de pago.", Alert.AlertType.WARNING);
                    return;
                }

                String impTexto = txtImp.getText() != null ? txtImp.getText().trim().replace(",", ".") : "";
                if (impTexto.isEmpty()) {
                    mostrarAlerta("Campo requerido", "Ingrese el monto a pagar.", Alert.AlertType.WARNING);
                    txtImp.requestFocus();
                    return;
                }

                double impVal = Double.parseDouble(impTexto);
                if (impVal <= 0.0) {
                    mostrarAlerta("Valor inválido", "El importe del pago debe ser mayor a cero.", Alert.AlertType.WARNING);
                    return;
                }

                if (impVal > saldoActual + 0.001) {
                    mostrarAlerta("Pago Excedido",
                            "El importe ingresado ($" + String.format("%.2f", impVal)
                            + ") no puede ser superior al saldo pendiente ($" + String.format("%.2f", saldoActual) + ").",
                            Alert.AlertType.WARNING);
                    return;
                }

                MedioPago medioVal = cmbMedio.getValue();
                if (medioVal == null) {
                    medioVal = MedioPago.EFECTIVO;
                }

                // Ejecución de la transacción 4.6 (Alta de Pago)
                Pago nuevoPago = new Pago(fechaVal.toString(), impVal, medioVal, contratoSeleccionado.getNro());
                pagoService.altaPago(nuevoPago);

                double nuevoSaldo = Math.max(0.0, saldoActual - impVal);
                if (nuevoSaldo <= 0.001) {
                    contratoSeleccionado.setEst("Pagado");
                    NuevoContratoController.marcarContratoPagadoEnMemoria(contratoSeleccionado.getNro());
                }

                ventana.close();

                String mensajeExito = "Se registró el pago de $" + String.format("%.2f", impVal)
                        + " (" + medioVal.getDescripcion() + ") para el Contrato N° " + contratoSeleccionado.getNro() + ".\n"
                        + "Saldo pendiente restante: $" + String.format("%.2f", nuevoSaldo);
                if (nuevoSaldo <= 0.001) {
                    mensajeExito += "\n\n¡El contrato ha quedado totalmente PAGADO!";
                }

                mostrarAlerta("Pago Registrado con Éxito", mensajeExito, Alert.AlertType.INFORMATION);

                cargarContratos();
                mostrarDetalleContrato(contratoSeleccionado);

            } catch (NumberFormatException ex) {
                mostrarAlerta("Error de Formato", "Verifique que el importe tenga formato numérico válido.", Alert.AlertType.WARNING);
            } catch (Exception ex) {
                mostrarAlerta("Error al registrar pago", ex.getMessage(), Alert.AlertType.ERROR);
            }
        });

        barraBotones.getChildren().addAll(btnCancelar, btnConfirmar);
        root.getChildren().addAll(tarjetaEncabezado, tarjetaFinanciera, tarjetaFormulario, barraBotones);

        Scene scene = new Scene(root);
        ventana.setScene(scene);
        ventana.setResizable(false);
        ventana.sizeToScene();
        ventana.showAndWait();
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
