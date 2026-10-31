package controlador;

import dao.ClienteDAO;
import dao.ContratoDAO;
import dao.SucursalDAO;
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
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.StringConverter;
import modelo.Contrato;
import modelo.Sucursal;
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
    @FXML private TextField txtDetalleCargos;
    @FXML private TextField txtDetalleCondUso;
    @FXML private Button btnRegistrarDevolucion;

    private final ContratoService contratoService;
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
                    if (item.equalsIgnoreCase("Activo") || item.equalsIgnoreCase("Alquilado")) {
                        setStyle("-fx-text-fill: #065F46; -fx-font-weight: bold; -fx-background-color: #D1FAE5; -fx-background-radius: 6; -fx-alignment: CENTER;");
                    } else if (item.equalsIgnoreCase("Devuelto") || item.equalsIgnoreCase("Finalizado")) {
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
        this.contratoSeleccionado = c;
        if (panelDetalleContrato == null) {
            return;
        }

        if (lblTituloPanel != null) {
            lblTituloPanel.setText("Contrato #" + (c.getNro() != null ? c.getNro() : "(nuevo)"));
        }

        String estado = (c.getEst() != null) ? c.getEst().trim() : "ALQUILADO";
        boolean yaDevuelto = estado.equalsIgnoreCase("Devuelto") || estado.equalsIgnoreCase("Finalizado");

        if (lblBadgeEstadoDetalle != null) {
            lblBadgeEstadoDetalle.setText(estado.toUpperCase());
            if (yaDevuelto) {
                lblBadgeEstadoDetalle.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #374151; -fx-background-color: #E5E7EB; -fx-background-radius: 8; -fx-padding: 3 10 3 10;");
            } else {
                lblBadgeEstadoDetalle.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #065F46; -fx-background-color: #D1FAE5; -fx-background-radius: 8; -fx-padding: 3 10 3 10;");
            }
        }

        if (btnRegistrarDevolucion != null) {
            btnRegistrarDevolucion.setDisable(yaDevuelto);
            if (yaDevuelto) {
                btnRegistrarDevolucion.setText("Vehículo Ya Devuelto");
                btnRegistrarDevolucion.setStyle("-fx-background-color: #E5E7EB; -fx-text-fill: #9CA3AF; -fx-font-size: 13px; -fx-font-weight: bold; -fx-background-radius: 10; -fx-cursor: default;");
            } else {
                btnRegistrarDevolucion.setText("Registrar Devolución");
                btnRegistrarDevolucion.setStyle("-fx-background-color: #262626; -fx-text-fill: #FFFFFF; -fx-font-size: 13px; -fx-font-weight: bold; -fx-background-radius: 10; -fx-cursor: hand;");
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
        if (txtDetalleCargos != null) {
            txtDetalleCargos.setText(c.getCargos() != null ? String.format("$%.2f", c.getCargos()) : "$0.00");
        }
        if (txtDetalleCondUso != null) {
            txtDetalleCondUso.setText(c.getCondUso() != null ? c.getCondUso() : "Estándar");
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

        if (contratoSeleccionado.getEst() != null && contratoSeleccionado.getEst().equalsIgnoreCase("Devuelto")) {
            mostrarAlerta("Contrato Devuelto",
                    "Este contrato ya se encuentra en estado 'Devuelto'.",
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

        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setStyle("-fx-background-color: #E5E7EB; -fx-text-fill: #374151; -fx-font-size: 12px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 10 18 10 18; -fx-cursor: hand;");
        btnCancelar.setOnAction(e -> ventana.close());

        Button btnConfirmar = new Button("Confirmar Devolución");
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

                ventana.close();

                mostrarAlerta("Devolución Exitosa",
                        "Se registró la devolución del vehículo con éxito.\nContrato N° "
                        + contratoSeleccionado.getNro() + " actualizado a estado 'Devuelto'.",
                        Alert.AlertType.INFORMATION);

                cargarContratos();
                mostrarDetalleContrato(contratoSeleccionado);

            } catch (NumberFormatException ex) {
                mostrarAlerta("Error de Formato", "Verifique que los valores numéricos (kilometraje, combustible, cargos) tengan formato válido.", Alert.AlertType.WARNING);
            } catch (Exception ex) {
                mostrarAlerta("Error al registrar devolución", ex.getMessage(), Alert.AlertType.ERROR);
            }
        });

        barraBotones.getChildren().addAll(btnCancelar, btnConfirmar);
        root.getChildren().addAll(tarjetaEncabezado, tarjetaFormulario, barraBotones);

        Scene scene = new Scene(root, 520.0, 420.0);
        ventana.setScene(scene);
        ventana.setResizable(false);
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

