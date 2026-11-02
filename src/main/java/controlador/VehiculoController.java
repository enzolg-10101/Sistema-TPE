package controlador;

import dao.ContratoDAO;
import dao.EmpleadoDAO;
import dao.MantenimientoDAO;
import dao.SucursalDAO;
import dao.VehiculoDAO;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import modelo.Contrato;
import modelo.Empleado;
import modelo.Mantenimiento;
import modelo.Sucursal;
import modelo.TipoMantenimiento;
import modelo.Vehiculo;
import servicio.MantenimientoService;
import servicio.VehiculoService;
import util.FormatoFecha;

public class VehiculoController {

    private static final String ESTILO_TARJETA_NORMAL =
            "-fx-background-color: #FFFFFF; "
            + "-fx-background-radius: 14; "
            + "-fx-border-color: #E5E7EB; "
            + "-fx-border-radius: 14; "
            + "-fx-border-width: 1; "
            + "-fx-cursor: hand;";

    private static final String ESTILO_TARJETA_HOVER =
            "-fx-background-color: #FFFFFF; "
            + "-fx-background-radius: 14; "
            + "-fx-border-color: #F59E0B; "
            + "-fx-border-radius: 14; "
            + "-fx-border-width: 1.5; "
            + "-fx-cursor: hand;";

    // Lista compartida en memoria para conservar los vehículos registrados durante la sesión
    private static final List<Vehiculo> VEHICULOS_REGISTRADOS = new ArrayList<>();

    // Diccionario en memoria para marcas y modelos del mercado argentino
    private static final Map<String, List<String>> CATALOGO_VEHICULOS = new HashMap<>();

    static {
        CATALOGO_VEHICULOS.put("Toyota", Arrays.asList("Etios", "Yaris", "Corolla", "Corolla Cross", "Hilux", "Hiace"));
        CATALOGO_VEHICULOS.put("Volkswagen", Arrays.asList("Gol", "Polo", "Virtus", "Taos", "Amarok", "Saveiro"));
        CATALOGO_VEHICULOS.put("Fiat", Arrays.asList("Cronos", "Mobi", "Argo", "Siena", "Toro", "Fiorino", "Ducato"));
        CATALOGO_VEHICULOS.put("Renault", Arrays.asList("Logan", "Sandero", "Stepway", "Duster", "Kangoo", "Master"));
        CATALOGO_VEHICULOS.put("Chevrolet", Arrays.asList("Joy", "Onix", "Tracker", "Cruze", "S10", "Spin"));
        CATALOGO_VEHICULOS.put("Ford", Arrays.asList("Ka", "Fiesta", "Focus", "Ranger", "Maverick", "Transit"));
        CATALOGO_VEHICULOS.put("Peugeot", Arrays.asList("208", "2008", "Partner", "Expert", "Boxer"));
        CATALOGO_VEHICULOS.put("Nissan", Arrays.asList("Versa", "Sentra", "Kicks", "Frontier"));
        CATALOGO_VEHICULOS.put("Mercedes-Benz", Arrays.asList("Sprinter", "Vito"));
    }

    // Contenedores para alternar entre Galería, Ficha Detallada, Alta y Mantenimiento
    @FXML private VBox vistaGaleria;
    @FXML private VBox vistaDetalle;
    @FXML private VBox vistaAlta;
    @FXML private VBox vistaHistorialMantenimiento;
    @FXML private VBox vistaNuevoMantenimiento;

    // Galería, filtro desplegable por tipo y buscador
    @FXML private TilePane contenedorVehiculos;
    @FXML private ComboBox<String> cmbFiltroTipo;
    @FXML private TextField txtBuscarVehiculo;
    @FXML private Label lblTituloGaleria;
    @FXML private Button btnLimpiarFiltroSucursal;
    @FXML private Label lblCantidadVehiculos;

    // Componentes de la pantalla de Ficha Completa del Vehículo (vistaDetalle)
    @FXML private Label lblFichaTitulo;
    @FXML private Label lblFichaSubtitulo;
    @FXML private Label lblDetalleBadgeDisponibilidad;
    @FXML private Label lblDetalleCatBadge;
    @FXML private ImageView imgDetalleVehiculo;
    @FXML private Label lblDetalleMarcaModelo;
    @FXML private Label lblDetallePatenteBadge;
    @FXML private Label lblDetalleTarifaDestacada;
    @FXML private Label lblDetalleSucursalDestacada;
    @FXML private Label lblTituloDatosDetalle;
    @FXML private Button btnEditarDetalle;
    @FXML private Button btnLimpiarFBajaDetalle;
    @FXML private Separator sepAccionesEdicionDetalle;
    @FXML private HBox boxAccionesEdicionDetalle;

    @FXML private TextField txtDetId;
    @FXML private TextField txtDetPat;
    @FXML private TextField txtDetMarca;
    @FXML private TextField txtDetMod;
    @FXML private ComboBox<Integer> cmbDetAFabr;
    @FXML private ComboBox<String> cmbDetCat;
    @FXML private ComboBox<String> cmbDetTiComb;
    @FXML private ComboBox<String> cmbDetTransmision;
    @FXML private TextField txtDetKmj;
    @FXML private TextField txtDetCarga;
    @FXML private TextField txtDetPax;
    @FXML private TextField txtDetTarBase;
    @FXML private TextField txtDetEst;
    @FXML private DatePicker dpDetFAdq;
    @FXML private TextField txtDetPreAdq;
    @FXML private DatePicker dpDetFBaja;
    @FXML private ComboBox<Sucursal> cmbDetSucursal;

    // Botones de acciones de mantenimiento en la ficha técnica (vistaDetalle)
    @FXML private Button btnVerMantenimientos;
    @FXML private Button btnNuevoMantenimiento;

    // Componentes de la pantalla de Historial de Mantenimientos (vistaHistorialMantenimiento)
    @FXML private Label lblHistorialTitulo;
    @FXML private Label lblHistorialSubtitulo;
    @FXML private Label lblHistorialTotalServicios;
    @FXML private Label lblHistorialCostoTotal;
    @FXML private Label lblHistorialEstadoTabla;
    @FXML private TableView<Mantenimiento> tablaMantenimientos;
    @FXML private TableColumn<Mantenimiento, Integer> colMantId;
    @FXML private TableColumn<Mantenimiento, String> colMantFecha;
    @FXML private TableColumn<Mantenimiento, String> colMantTipo;
    @FXML private TableColumn<Mantenimiento, String> colMantKmj;
    @FXML private TableColumn<Mantenimiento, String> colMantCosto;
    @FXML private TableColumn<Mantenimiento, String> colMantEmpleado;
    @FXML private TableColumn<Mantenimiento, String> colMantObs;

    // Componentes de la pantalla de Registro de Mantenimiento (vistaNuevoMantenimiento)
    @FXML private Label lblNuevoMantTitulo;
    @FXML private Label lblNuevoMantSubtitulo;
    @FXML private Label lblNuevoMantVehiculoInfo;
    @FXML private Label lblNuevoMantPatente;
    @FXML private Label lblNuevoMantKmjActual;
    @FXML private Label lblNuevoMantSucursal;
    @FXML private DatePicker dpNuevoMantFecha;
    @FXML private ComboBox<TipoMantenimiento> cmbNuevoMantTipo;
    @FXML private TextField txtNuevoMantKmj;
    @FXML private TextField txtNuevoMantCosto;
    @FXML private ComboBox<Empleado> cmbNuevoMantEmpleado;
    @FXML private TextField txtNuevoMantObs;
    @FXML private Label lblNuevoMantContadorObs;

    // Servicios y DAOs para Mantenimiento y Empleados
    private final MantenimientoService mantenimientoService = new MantenimientoService();
    private final EmpleadoDAO empleadoDAO = new EmpleadoDAO();
    private final Map<Integer, Empleado> cacheEmpleados = new HashMap<>();
    private boolean vieneDesdeHistorial = false;

    private Vehiculo vehiculoActualDetalle;

    // Campos del formulario de Nuevo Vehículo (sin ID manual, V_id es AUTO_INCREMENT y V_pat es UNIQUE)
    @FXML private TextField txtPat;
    @FXML private ComboBox<String> comboMarca;
    @FXML private ComboBox<String> comboModelo;
    @FXML private ComboBox<Integer> cmbAFabr;
    @FXML private DatePicker dpFAdq;
    @FXML private TextField txtKmj;
    @FXML private TextField txtEst;
    @FXML private TextField txtPreAdq;
    @FXML private ComboBox<String> cmbTiComb;
    @FXML private ComboBox<String> cmbTransmision;
    @FXML private TextField txtCarga;
    @FXML private TextField txtPax;
    @FXML private TextField txtTarBase;
    @FXML private ComboBox<String> cmbCat;
    @FXML private ComboBox<Sucursal> cmbSucursal;

    private static Sucursal sucursalFiltroPendiente;
    private Sucursal sucursalFiltroActiva;

    private VehiculoService vehiculoService;
    private final SucursalDAO sucursalDAO = new SucursalDAO();
    private final ContratoDAO contratoDAO = new ContratoDAO();
    private List<Vehiculo> listaCompleta = new ArrayList<>();
    private List<Sucursal> cacheSucursales = new ArrayList<>();
    private final Set<Integer> idsVehiculosAlquilados = new HashSet<>();
    private final Set<String> patentesVehiculosAlquilados = new HashSet<>();

    public VehiculoController() {
        this.vehiculoService = new VehiculoService(new VehiculoDAO(), sucursalDAO);
    }

    public VehiculoController(VehiculoService vehiculoService) {
        this.vehiculoService = vehiculoService;
    }

    public static void setSucursalFiltroPendiente(Sucursal sucursal) {
        sucursalFiltroPendiente = sucursal;
    }

    public static List<Vehiculo> getVehiculosRegistradosEnSesion() {
        return VEHICULOS_REGISTRADOS;
    }

    @FXML
    public void initialize() {
        this.sucursalFiltroActiva = sucursalFiltroPendiente;
        sucursalFiltroPendiente = null;
        actualizarEncabezadoSucursalFiltro();

        if (comboMarca != null) {
            List<String> marcasOrdenadas = new ArrayList<>(CATALOGO_VEHICULOS.keySet());
            Collections.sort(marcasOrdenadas);
            comboMarca.getItems().setAll(marcasOrdenadas);

            comboMarca.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, marcaSeleccionada) -> {
                if (comboModelo != null) {
                    comboModelo.getItems().clear();
                    if (marcaSeleccionada != null && CATALOGO_VEHICULOS.containsKey(marcaSeleccionada)) {
                        comboModelo.getItems().addAll(CATALOGO_VEHICULOS.get(marcaSeleccionada));
                    }
                }
            });
        }

        if (cmbCat != null) {
            cmbCat.getItems().setAll("Auto", "Combi", "Furgoneta");
            cmbCat.getSelectionModel().selectFirst();
        }
        if (cmbDetCat != null) {
            cmbDetCat.getItems().setAll("Auto", "Combi", "Furgoneta");
        }

        if (cmbTiComb != null) {
            cmbTiComb.getItems().setAll("Nafta", "Diesel");
            cmbTiComb.getSelectionModel().selectFirst();
        }
        if (cmbDetTiComb != null) {
            cmbDetTiComb.getItems().setAll("Nafta", "Diesel");
        }

        if (cmbTransmision != null) {
            cmbTransmision.getItems().setAll("Manual", "Automática");
            cmbTransmision.getSelectionModel().selectFirst();
        }
        if (cmbDetTransmision != null) {
            cmbDetTransmision.getItems().setAll("Manual", "Automática");
        }

        if (dpFAdq != null) {
            FormatoFecha.configurarDatePicker(dpFAdq);
        }
        if (dpDetFAdq != null) {
            FormatoFecha.configurarDatePicker(dpDetFAdq);
        }
        if (dpDetFBaja != null) {
            FormatoFecha.configurarDatePicker(dpDetFBaja);
        }

        configurarAniosFabricacionYFechaAdquisicion();

        StringConverter<Sucursal> convertidorSucursal = new StringConverter<Sucursal>() {
            @Override
            public String toString(Sucursal sucursal) {
                return sucursal == null ? "" : sucursal.getCod() + " - " + sucursal.getNom();
            }

            @Override
            public Sucursal fromString(String string) {
                return null;
            }
        };

        if (cmbSucursal != null) {
            cmbSucursal.setConverter(convertidorSucursal);
        }
        if (cmbDetSucursal != null) {
            cmbDetSucursal.setConverter(convertidorSucursal);
        }
        cargarSucursales();

        if (cmbFiltroTipo != null) {
            cmbFiltroTipo.getItems().setAll("Todos", "Auto", "Combi", "Furgoneta");
            cmbFiltroTipo.setValue("Todos");
            cmbFiltroTipo.valueProperty().addListener((obs, oldVal, newVal) -> aplicarFiltrosGaleria());
        }

        if (txtBuscarVehiculo != null) {
            txtBuscarVehiculo.textProperty().addListener((obs, oldVal, newVal) -> aplicarFiltrosGaleria());
        }

        configurarTablaMantenimientos();
        configurarFormularioMantenimiento();

        recargarVehiculos();
        mostrarVistaGaleria();
    }

    private void configurarAniosFabricacionYFechaAdquisicion() {
        int anioActual = LocalDate.now().getYear();
        int anioMinimo = anioActual - 10;

        List<Integer> anios = new ArrayList<>();
        for (int anio = anioActual; anio >= anioMinimo; anio--) {
            anios.add(anio);
        }

        if (cmbAFabr != null) {
            cmbAFabr.getItems().setAll(anios);
            cmbAFabr.getSelectionModel().selectFirst();

            cmbAFabr.valueProperty().addListener((obs, oldVal, nuevoAnio) -> {
                actualizarRestriccionFechaAdquisicion(dpFAdq, cmbAFabr);
                if (nuevoAnio != null && dpFAdq != null && dpFAdq.getValue() != null) {
                    if (dpFAdq.getValue().getYear() < nuevoAnio) {
                        dpFAdq.setValue(LocalDate.of(nuevoAnio, 1, 1));
                    }
                }
            });
        }

        if (dpFAdq != null) {
            actualizarRestriccionFechaAdquisicion(dpFAdq, cmbAFabr);
            dpFAdq.valueProperty().addListener((obs, oldVal, nuevaFecha) -> {
                if (nuevaFecha != null && cmbAFabr != null && cmbAFabr.getValue() != null) {
                    int anioFabr = cmbAFabr.getValue();
                    if (nuevaFecha.getYear() < anioFabr) {
                        dpFAdq.setValue(LocalDate.of(anioFabr, 1, 1));
                    }
                }
            });
        }

        if (cmbDetAFabr != null) {
            cmbDetAFabr.getItems().setAll(anios);
            cmbDetAFabr.valueProperty().addListener((obs, oldVal, nuevoAnio) -> {
                actualizarRestriccionFechaAdquisicion(dpDetFAdq, cmbDetAFabr);
                if (nuevoAnio != null && dpDetFAdq != null && dpDetFAdq.getValue() != null) {
                    if (dpDetFAdq.getValue().getYear() < nuevoAnio) {
                        dpDetFAdq.setValue(LocalDate.of(nuevoAnio, 1, 1));
                    }
                }
            });
        }

        if (dpDetFAdq != null) {
            actualizarRestriccionFechaAdquisicion(dpDetFAdq, cmbDetAFabr);
            dpDetFAdq.valueProperty().addListener((obs, oldVal, nuevaFecha) -> {
                if (nuevaFecha != null && cmbDetAFabr != null && cmbDetAFabr.getValue() != null) {
                    int anioFabr = cmbDetAFabr.getValue();
                    if (nuevaFecha.getYear() < anioFabr) {
                        dpDetFAdq.setValue(LocalDate.of(anioFabr, 1, 1));
                    }
                }
            });
        }
    }

    private void actualizarRestriccionFechaAdquisicion(DatePicker datePicker, ComboBox<Integer> comboAnio) {
        if (datePicker == null) {
            return;
        }
        int anioActual = LocalDate.now().getYear();
        int anioFabrSeleccionado = (comboAnio != null && comboAnio.getValue() != null)
                ? comboAnio.getValue()
                : (anioActual - 10);
        LocalDate fechaMinimaPermitida = LocalDate.of(anioFabrSeleccionado, 1, 1);

        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setDisable(false);
                    setStyle("");
                } else if (item.isBefore(fechaMinimaPermitida)) {
                    setDisable(true);
                    setStyle("-fx-background-color: #FEE2E2; -fx-text-fill: #9CA3AF;");
                } else {
                    setDisable(false);
                    setStyle("");
                }
            }
        });
    }

    private void cargarSucursales() {
        List<Sucursal> sucursales = new ArrayList<>();
        try {
            sucursales = sucursalDAO.listarTodas();
        } catch (Exception ignored) {
            // Si la base de datos aún no está conectada, se ofrece una sucursal por defecto para permitir pruebas
        }
        if (sucursales == null || sucursales.isEmpty()) {
            sucursales = new ArrayList<>();
            sucursales.add(new Sucursal(1, "Sucursal Central", "Av. Libertador 1200", 1145001234L));
            sucursales.add(new Sucursal(2, "Sucursal Aeropuerto", "Au. Riccheri Km 33", 1148005678L));
        }
        this.cacheSucursales = sucursales;

        if (cmbSucursal != null) {
            cmbSucursal.getItems().setAll(sucursales);
            if (cmbSucursal.getValue() == null && !sucursales.isEmpty()) {
                cmbSucursal.getSelectionModel().selectFirst();
            }
        }
        if (cmbDetSucursal != null) {
            Sucursal seleccionActual = cmbDetSucursal.getValue();
            cmbDetSucursal.getItems().setAll(sucursales);
            if (seleccionActual != null) {
                for (Sucursal s : sucursales) {
                    if (s.getCod() != null && s.getCod().equals(seleccionActual.getCod())) {
                        cmbDetSucursal.setValue(s);
                        break;
                    }
                }
            }
        }
    }

    /**
     * Limpia el TilePane y construye dinámicamente una tarjeta (VBox) clickeable por cada vehículo de la lista.
     */
    public void cargarGrillaVehiculos(List<Vehiculo> lista) {
        if (contenedorVehiculos == null) {
            return;
        }
        contenedorVehiculos.getChildren().clear();

        if (lista == null) {
            return;
        }

        for (Vehiculo vehiculo : lista) {
            VBox tarjeta = crearTarjetaVehiculo(vehiculo);
            contenedorVehiculos.getChildren().add(tarjeta);
        }

        if (lblCantidadVehiculos != null) {
            int total = lista.size();
            lblCantidadVehiculos.setText(
                    (total == 1 ? "1 vehículo en flota" : total + " vehículos en flota")
                    + " · Haga clic en un vehículo para ver su ficha completa"
            );
        }
    }

    /**
     * Construye dinámicamente la tarjeta visual (VBox) clickeable de un vehículo.
     */
    private VBox crearTarjetaVehiculo(Vehiculo vehiculo) {
        VBox tarjeta = new VBox(10.0);
        tarjeta.setPrefWidth(250.0);
        tarjeta.setAlignment(Pos.TOP_LEFT);
        tarjeta.setPadding(new Insets(16.0));
        tarjeta.setStyle(ESTILO_TARJETA_NORMAL);

        // Obtiene la imagen del vehículo identificando su tipo (Auto, Combi o Furgoneta)
        // o usando la imagen genérica predeterminada
        InputStream flujo = obtenerFlujoImagenPorTipo(vehiculo);

        // Contenedor de la imagen con etiqueta superior de estado (Disponible / Alquilado)
        VBox contenedorImagen = new VBox(4.0);
        contenedorImagen.setAlignment(Pos.CENTER);
        contenedorImagen.setPrefHeight(150.0);
        contenedorImagen.setStyle("-fx-background-color: #F9FAFB; -fx-background-radius: 10; -fx-padding: 8;");

        boolean alquilado = estaVehiculoAlquilado(vehiculo);
        Label lblEstadoDisponibilidad = new Label(alquilado ? "● Alquilado" : "● Disponible");
        lblEstadoDisponibilidad.setStyle(alquilado
                ? "-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #B91C1C; "
                  + "-fx-background-color: #FEE2E2; -fx-border-color: #FCA5A5; -fx-border-radius: 6; "
                  + "-fx-background-radius: 6; -fx-padding: 2 8 2 8;"
                : "-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #15803D; "
                  + "-fx-background-color: #DCFCE7; -fx-border-color: #86EFAC; -fx-border-radius: 6; "
                  + "-fx-background-radius: 6; -fx-padding: 2 8 2 8;"
        );

        HBox barraEstadoSuperior = new HBox(lblEstadoDisponibilidad);
        barraEstadoSuperior.setAlignment(Pos.CENTER_RIGHT);

        ImageView imageView = new ImageView();
        imageView.setFitWidth(210.0);
        imageView.setFitHeight(110.0);
        imageView.setPreserveRatio(true);
        if (flujo != null) {
            imageView.setImage(new Image(flujo));
        }
        contenedorImagen.getChildren().addAll(barraEstadoSuperior, imageView);

        // Encabezado de tarjeta con Marca y etiqueta del Tipo de Vehículo
        HBox filaTitulo = new HBox(8.0);
        filaTitulo.setAlignment(Pos.CENTER_LEFT);

        Label lblMarca = new Label(vehiculo.getMarca() != null ? vehiculo.getMarca() : "Sin marca");
        lblMarca.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #111827;");

        Region espTitulo = new Region();
        HBox.setHgrow(espTitulo, Priority.ALWAYS);

        String categoriaTexto = (vehiculo.getCat() != null && !vehiculo.getCat().trim().isEmpty())
                ? vehiculo.getCat().trim()
                : "Auto";
        Label lblTipo = new Label(categoriaTexto);
        lblTipo.setStyle(
                "-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #D97706; "
                + "-fx-background-color: #FEF3C7; -fx-background-radius: 6; -fx-padding: 2 8 2 8;"
        );

        filaTitulo.getChildren().addAll(lblMarca, espTitulo, lblTipo);

        // Labels obligatorios debajo del ImageView: Marca, Modelo y Patente
        Label lblModelo = new Label(vehiculo.getMod() != null ? vehiculo.getMod() : "Sin modelo");
        lblModelo.setStyle("-fx-font-size: 13px; -fx-text-fill: #6B7280;");

        HBox filaInferior = new HBox(8.0);
        filaInferior.setAlignment(Pos.CENTER_LEFT);

        Label lblPatente = new Label("Patente: " + (vehiculo.getPat() != null ? vehiculo.getPat() : "-"));
        lblPatente.setStyle(
                "-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #374151; "
                + "-fx-background-color: #F3F4F6; -fx-background-radius: 8; -fx-padding: 4 10 4 10;"
        );

        Region espaciador = new Region();
        HBox.setHgrow(espaciador, Priority.ALWAYS);

        String textoTarifa = vehiculo.getTarBase() != null
                ? String.format("$%.2f/día", vehiculo.getTarBase())
                : "";
        Label lblTarifa = new Label(textoTarifa);
        lblTarifa.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #D97706;");

        filaInferior.getChildren().addAll(lblPatente, espaciador, lblTarifa);

        tarjeta.getChildren().addAll(contenedorImagen, filaTitulo, lblModelo, filaInferior);

        // Efecto hover y acción de clic para abrir la ficha completa en una nueva pantalla
        tarjeta.setOnMouseEntered(e -> tarjeta.setStyle(ESTILO_TARJETA_HOVER));
        tarjeta.setOnMouseExited(e -> tarjeta.setStyle(ESTILO_TARJETA_NORMAL));
        tarjeta.setOnMouseClicked(e -> abrirFichaVehiculo(vehiculo));

        return tarjeta;
    }

    /**
     * Abre la pantalla de Ficha Completa del Vehículo (vistaDetalle) mostrando todos sus atributos para ver o editar.
     */
    public void abrirFichaVehiculo(Vehiculo v) {
        if (v == null || vistaDetalle == null) {
            return;
        }
        this.vehiculoActualDetalle = v;
        cargarSucursales();

        boolean alquilado = estaVehiculoAlquilado(v);
        String marcaMod = ((v.getMarca() != null ? v.getMarca() : "") + " " + (v.getMod() != null ? v.getMod() : "")).trim();
        if (marcaMod.isEmpty()) {
            marcaMod = "Vehículo sin nombre";
        }

        if (lblFichaTitulo != null) {
            lblFichaTitulo.setText("Ficha del Vehículo · " + marcaMod);
        }
        if (lblFichaSubtitulo != null) {
            lblFichaSubtitulo.setText("Patente: " + (v.getPat() != null ? v.getPat() : "-")
                    + "  ·  ID de Unidad: #" + (v.getId() != null ? v.getId() : "-"));
        }

        if (lblDetalleBadgeDisponibilidad != null) {
            lblDetalleBadgeDisponibilidad.setText(alquilado ? "● Alquilado" : "● Disponible");
            lblDetalleBadgeDisponibilidad.setStyle(alquilado
                    ? "-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #B91C1C; "
                      + "-fx-background-color: #FEE2E2; -fx-border-color: #FCA5A5; -fx-border-radius: 8; "
                      + "-fx-background-radius: 8; -fx-padding: 6 14 6 14;"
                    : "-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #15803D; "
                      + "-fx-background-color: #DCFCE7; -fx-border-color: #86EFAC; -fx-border-radius: 8; "
                      + "-fx-background-radius: 8; -fx-padding: 6 14 6 14;"
            );
        }

        if (lblDetalleCatBadge != null) {
            lblDetalleCatBadge.setText(v.getCat() != null && !v.getCat().trim().isEmpty() ? v.getCat().trim() : "Auto");
        }
        if (imgDetalleVehiculo != null) {
            InputStream flujo = obtenerFlujoImagenPorTipo(v);
            if (flujo != null) {
                imgDetalleVehiculo.setImage(new Image(flujo));
            }
        }
        if (lblDetalleMarcaModelo != null) {
            lblDetalleMarcaModelo.setText(marcaMod);
        }
        if (lblDetallePatenteBadge != null) {
            lblDetallePatenteBadge.setText("Patente: " + (v.getPat() != null ? v.getPat() : "-"));
        }
        if (lblDetalleTarifaDestacada != null) {
            lblDetalleTarifaDestacada.setText(
                    v.getTarBase() != null ? String.format("$%.2f / día", v.getTarBase()) : "$0.00 / día"
            );
        }

        String sucursalTexto = formatearSucursal(v.getCodPertenece());
        if (lblDetalleSucursalDestacada != null) {
            lblDetalleSucursalDestacada.setText(sucursalTexto);
        }

        // Poblar los 16 campos de la grilla de atributos (editables salvo ID)
        if (txtDetId != null) {
            txtDetId.setText(v.getId() != null ? String.valueOf(v.getId()) : "-");
        }
        if (txtDetPat != null) {
            txtDetPat.setText(v.getPat() != null ? v.getPat() : "");
        }
        if (txtDetMarca != null) {
            txtDetMarca.setText(v.getMarca() != null ? v.getMarca() : "");
        }
        if (txtDetMod != null) {
            txtDetMod.setText(v.getMod() != null ? v.getMod() : "");
        }
        if (cmbDetAFabr != null) {
            int anioActual = LocalDate.now().getYear();
            int anioMinimo = anioActual - 10;
            List<Integer> anios = new ArrayList<>();
            for (int anio = anioActual; anio >= anioMinimo; anio--) {
                anios.add(anio);
            }
            if (v.getaFabr() != null && !anios.contains(v.getaFabr())) {
                anios.add(v.getaFabr());
            }
            cmbDetAFabr.getItems().setAll(anios);
            if (v.getaFabr() != null) {
                cmbDetAFabr.setValue(v.getaFabr());
            } else if (!anios.isEmpty()) {
                cmbDetAFabr.getSelectionModel().selectFirst();
            }
        }
        if (cmbDetCat != null) {
            String catActual = v.getCat() != null && !v.getCat().trim().isEmpty() ? v.getCat().trim() : "Auto";
            if (!cmbDetCat.getItems().contains(catActual)) {
                cmbDetCat.getItems().add(catActual);
            }
            cmbDetCat.setValue(catActual);
        }
        if (cmbDetTiComb != null) {
            String combActual = (v.getTiComb() != null && v.getTiComb().trim().equalsIgnoreCase("diesel"))
                    ? "Diesel"
                    : "Nafta";
            cmbDetTiComb.setValue(combActual);
        }
        if (cmbDetTransmision != null) {
            String tranActual = (v.getTran() != null && v.getTran().trim().toLowerCase().startsWith("auto"))
                    ? "Automática"
                    : "Manual";
            cmbDetTransmision.setValue(tranActual);
        }
        if (txtDetKmj != null) {
            txtDetKmj.setText(v.getKmj() != null ? String.valueOf(v.getKmj()) : "0");
        }
        if (txtDetCarga != null) {
            txtDetCarga.setText(v.getCarga() != null ? String.valueOf(v.getCarga()) : "0.0");
        }
        if (txtDetPax != null) {
            txtDetPax.setText(v.getPax() != null ? String.valueOf(v.getPax()) : "0");
        }
        if (txtDetTarBase != null) {
            txtDetTarBase.setText(v.getTarBase() != null ? String.valueOf(v.getTarBase()) : "0.0");
        }
        if (txtDetEst != null) {
            txtDetEst.setText(v.getEst() != null ? v.getEst() : "Disponible");
        }
        if (dpDetFAdq != null) {
            LocalDate fechaAdq = FormatoFecha.parsearFecha(v.getfAdq());
            dpDetFAdq.setValue(fechaAdq);
            actualizarRestriccionFechaAdquisicion(dpDetFAdq, cmbDetAFabr);
        }
        if (txtDetPreAdq != null) {
            txtDetPreAdq.setText(v.getPreAdq() != null ? String.valueOf(v.getPreAdq()) : "0.0");
        }
        if (dpDetFBaja != null) {
            LocalDate fechaBaja = FormatoFecha.parsearFecha(v.getfBaja());
            dpDetFBaja.setValue(fechaBaja);
        }
        if (cmbDetSucursal != null) {
            Sucursal sucursalEncontrada = null;
            if (v.getCodPertenece() != null && cacheSucursales != null) {
                for (Sucursal s : cacheSucursales) {
                    if (v.getCodPertenece().equals(s.getCod())) {
                        sucursalEncontrada = s;
                        break;
                    }
                }
            }
            if (sucursalEncontrada != null) {
                cmbDetSucursal.setValue(sucursalEncontrada);
            } else if (!cmbDetSucursal.getItems().isEmpty()) {
                cmbDetSucursal.getSelectionModel().selectFirst();
            }
        }

        // Iniciar siempre en modo solo lectura hasta que el usuario presione "Editar Vehículo"
        setModoEdicionDetalle(false);

        // Cambiar a la pantalla de Ficha Completa
        if (vistaGaleria != null) {
            vistaGaleria.setVisible(false);
            vistaGaleria.setManaged(false);
        }
        if (vistaAlta != null) {
            vistaAlta.setVisible(false);
            vistaAlta.setManaged(false);
        }
        if (vistaHistorialMantenimiento != null) {
            vistaHistorialMantenimiento.setVisible(false);
            vistaHistorialMantenimiento.setManaged(false);
        }
        if (vistaNuevoMantenimiento != null) {
            vistaNuevoMantenimiento.setVisible(false);
            vistaNuevoMantenimiento.setManaged(false);
        }
        vistaDetalle.setVisible(true);
        vistaDetalle.setManaged(true);
    }

    /**
     * Habilita o deshabilita la edición de los campos en la pantalla de Ficha del Vehículo.
     */
    private void setModoEdicionDetalle(boolean editable) {
        String estiloTextoLectura = "-fx-background-color: #F3F4F6; -fx-background-radius: 8; -fx-padding: 8 10 8 10;";
        String estiloTextoEdicion = "-fx-background-color: #FFFFFF; -fx-border-color: #F59E0B; -fx-border-radius: 8; "
                + "-fx-background-radius: 8; -fx-padding: 7 9 7 9;";

        String estiloPatenteLectura = "-fx-background-color: #F3F4F6; -fx-background-radius: 8; -fx-padding: 8 10 8 10; -fx-font-weight: bold;";
        String estiloPatenteEdicion = "-fx-background-color: #FFFFFF; -fx-border-color: #F59E0B; -fx-border-radius: 8; "
                + "-fx-background-radius: 8; -fx-padding: 7 9 7 9; -fx-font-weight: bold;";

        String estiloTarifaLectura = "-fx-background-color: #FEF3C7; -fx-background-radius: 8; -fx-padding: 8 10 8 10; "
                + "-fx-font-weight: bold; -fx-text-fill: #D97706;";
        String estiloTarifaEdicion = "-fx-background-color: #FFFBEB; -fx-border-color: #F59E0B; -fx-border-radius: 8; "
                + "-fx-background-radius: 8; -fx-padding: 7 9 7 9; -fx-font-weight: bold; -fx-text-fill: #D97706;";

        String estiloComboLectura = "-fx-background-color: #F3F4F6; -fx-background-radius: 8;";
        String estiloComboEdicion = "-fx-background-color: #FFFFFF; -fx-border-color: #F59E0B; -fx-border-radius: 8; -fx-background-radius: 8;";

        if (txtDetPat != null) {
            txtDetPat.setEditable(editable);
            txtDetPat.setStyle(editable ? estiloPatenteEdicion : estiloPatenteLectura);
        }
        TextField[] camposNormales = {
            txtDetMarca, txtDetMod, txtDetKmj,
            txtDetCarga, txtDetPax, txtDetEst, txtDetPreAdq
        };
        for (TextField tf : camposNormales) {
            if (tf != null) {
                tf.setEditable(editable);
                tf.setStyle(editable ? estiloTextoEdicion : estiloTextoLectura);
            }
        }
        if (txtDetTarBase != null) {
            txtDetTarBase.setEditable(editable);
            txtDetTarBase.setStyle(editable ? estiloTarifaEdicion : estiloTarifaLectura);
        }

        ComboBox<?>[] combos = {cmbDetAFabr, cmbDetCat, cmbDetTiComb, cmbDetTransmision, cmbDetSucursal};
        for (ComboBox<?> cb : combos) {
            if (cb != null) {
                cb.setMouseTransparent(!editable);
                cb.setFocusTraversable(editable);
                cb.setStyle(editable ? estiloComboEdicion : estiloComboLectura);
            }
        }

        DatePicker[] pickers = {dpDetFAdq, dpDetFBaja};
        for (DatePicker dp : pickers) {
            if (dp != null) {
                dp.setMouseTransparent(!editable);
                dp.setFocusTraversable(editable);
                dp.setStyle(editable ? estiloComboEdicion : estiloComboLectura);
            }
        }

        if (btnLimpiarFBajaDetalle != null) {
            btnLimpiarFBajaDetalle.setVisible(editable);
            btnLimpiarFBajaDetalle.setManaged(editable);
        }
        if (sepAccionesEdicionDetalle != null) {
            sepAccionesEdicionDetalle.setVisible(editable);
            sepAccionesEdicionDetalle.setManaged(editable);
        }
        if (boxAccionesEdicionDetalle != null) {
            boxAccionesEdicionDetalle.setVisible(editable);
            boxAccionesEdicionDetalle.setManaged(editable);
        }
        if (btnEditarDetalle != null) {
            btnEditarDetalle.setVisible(!editable);
            btnEditarDetalle.setManaged(!editable);
        }
        if (lblTituloDatosDetalle != null) {
            lblTituloDatosDetalle.setText(editable
                    ? "Editando Datos del Vehículo"
                    : "Datos Registrados del Vehículo");
        }
    }

    @FXML
    public void habilitarEdicionDetalle() {
        setModoEdicionDetalle(true);
        if (txtDetPat != null) {
            txtDetPat.requestFocus();
        }
    }

    @FXML
    public void cancelarEdicionDetalle() {
        if (vehiculoActualDetalle != null) {
            abrirFichaVehiculo(vehiculoActualDetalle);
        } else {
            setModoEdicionDetalle(false);
        }
    }

    @FXML
    public void limpiarFechaBajaDetalle() {
        if (dpDetFBaja != null) {
            dpDetFBaja.setValue(null);
        }
    }

    @FXML
    public void restablecerDetalleVehiculo() {
        if (vehiculoActualDetalle != null) {
            abrirFichaVehiculo(vehiculoActualDetalle);
        }
    }

    @FXML
    public void guardarCambiosVehiculo() {
        if (vehiculoActualDetalle == null) {
            return;
        }
        try {
            String pat = txtDetPat != null ? txtDetPat.getText().trim() : "";
            String marca = txtDetMarca != null ? txtDetMarca.getText().trim() : "";
            String mod = txtDetMod != null ? txtDetMod.getText().trim() : "";
            Integer aFabr = cmbDetAFabr != null ? cmbDetAFabr.getValue() : null;
            LocalDate fechaAdq = dpDetFAdq != null ? dpDetFAdq.getValue() : null;
            String fAdq = fechaAdq != null ? fechaAdq.toString() : "";
            Integer kmj = Integer.parseInt(txtDetKmj.getText().trim());
            String est = txtDetEst != null ? txtDetEst.getText().trim() : "";
            Double preAdq = Double.parseDouble(txtDetPreAdq.getText().trim());
            String fBaja = (dpDetFBaja != null && dpDetFBaja.getValue() != null)
                    ? dpDetFBaja.getValue().toString()
                    : null;
            String tiComb = (cmbDetTiComb != null && cmbDetTiComb.getValue() != null) ? cmbDetTiComb.getValue().trim() : "";
            String tran = (cmbDetTransmision != null && cmbDetTransmision.getValue() != null) ? cmbDetTransmision.getValue().trim() : "";
            Double carga = Double.parseDouble(txtDetCarga.getText().trim());
            Integer pax = Integer.parseInt(txtDetPax.getText().trim());
            Double tarBase = Double.parseDouble(txtDetTarBase.getText().trim());
            String cat = cmbDetCat != null ? cmbDetCat.getValue() : null;
            Sucursal sucursalSeleccionada = cmbDetSucursal != null ? cmbDetSucursal.getValue() : null;

            if (pat.isEmpty() || marca.isEmpty() || mod.isEmpty() || aFabr == null || fechaAdq == null
                    || est.isEmpty() || tiComb.isEmpty() || tran.isEmpty() || cat == null || sucursalSeleccionada == null) {
                mostrarMensaje("Error de Validación",
                        "Por favor complete todos los campos obligatorios (Patente, Marca, Modelo, Año, Fecha de Adquisición, Estado, Combustible, Transmisión, Categoría y Sucursal).",
                        Alert.AlertType.WARNING);
                return;
            }

            if (fechaAdq.getYear() < aFabr) {
                mostrarMensaje("Error de Validación",
                        "La fecha de adquisición (" + FormatoFecha.formatearFechaGui(fechaAdq)
                        + ") no puede ser menor al año de fabricación (" + aFabr + ").",
                        Alert.AlertType.WARNING);
                return;
            }

            Vehiculo datosActualizados = new Vehiculo(
                    vehiculoActualDetalle.getId(),
                    pat, marca, mod, tran, aFabr, fAdq, kmj, est,
                    preAdq, fBaja, tiComb, carga, pax, tarBase, cat,
                    sucursalSeleccionada.getCod()
            );

            vehiculoService.modificarVehiculo(datosActualizados);

            vehiculoActualDetalle.setPat(pat);
            vehiculoActualDetalle.setMarca(marca);
            vehiculoActualDetalle.setMod(mod);
            vehiculoActualDetalle.setTran(tran);
            vehiculoActualDetalle.setaFabr(aFabr);
            vehiculoActualDetalle.setfAdq(fAdq);
            vehiculoActualDetalle.setKmj(kmj);
            vehiculoActualDetalle.setEst(est);
            vehiculoActualDetalle.setPreAdq(preAdq);
            vehiculoActualDetalle.setfBaja(fBaja);
            vehiculoActualDetalle.setTiComb(tiComb);
            vehiculoActualDetalle.setCarga(carga);
            vehiculoActualDetalle.setPax(pax);
            vehiculoActualDetalle.setTarBase(tarBase);
            vehiculoActualDetalle.setCat(cat);
            vehiculoActualDetalle.setCodPertenece(sucursalSeleccionada.getCod());

            // Actualizar también en la lista de sesión en memoria si corresponde
            for (int i = 0; i < VEHICULOS_REGISTRADOS.size(); i++) {
                Vehiculo reg = VEHICULOS_REGISTRADOS.get(i);
                if (vehiculoActualDetalle.getId() != null && vehiculoActualDetalle.getId().equals(reg.getId())) {
                    VEHICULOS_REGISTRADOS.set(i, vehiculoActualDetalle);
                    break;
                }
            }

            abrirFichaVehiculo(vehiculoActualDetalle);
            mostrarMensaje("Vehículo Actualizado",
                    "Los datos del vehículo (" + cat + ", patente " + pat + ") se actualizaron correctamente.",
                    Alert.AlertType.INFORMATION);

        } catch (NumberFormatException e) {
            mostrarMensaje("Error de Validación",
                    "Verifique que Kilometraje, Precio, Carga, Pasajeros y Tarifa sean valores numéricos válidos.",
                    Alert.AlertType.WARNING);
        } catch (Exception e) {
            mostrarMensaje("Error", "No se pudieron guardar los cambios del vehículo: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private String formatearSucursal(Integer codSucursal) {
        if (codSucursal == null) {
            return "-";
        }
        if (cacheSucursales != null) {
            for (Sucursal s : cacheSucursales) {
                if (codSucursal.equals(s.getCod())) {
                    return s.getCod() + " - " + s.getNom();
                }
            }
        }
        return "Sucursal #" + codSucursal;
    }

    @FXML
    public void volverAGaleriaDesdeDetalle() {
        if (vistaDetalle != null) {
            vistaDetalle.setVisible(false);
            vistaDetalle.setManaged(false);
        }
        if (vistaAlta != null) {
            vistaAlta.setVisible(false);
            vistaAlta.setManaged(false);
        }
        if (vistaHistorialMantenimiento != null) {
            vistaHistorialMantenimiento.setVisible(false);
            vistaHistorialMantenimiento.setManaged(false);
        }
        if (vistaNuevoMantenimiento != null) {
            vistaNuevoMantenimiento.setVisible(false);
            vistaNuevoMantenimiento.setManaged(false);
        }
        if (vistaGaleria != null) {
            vistaGaleria.setVisible(true);
            vistaGaleria.setManaged(true);
        }
        recargarVehiculos();
    }

    /**
     * Identifica por código el tipo/categoría del vehículo registrado (Auto, Combi o Furgoneta)
     * para asignarle su imagen genérica representativa correspondiente, o /autos/default.png por defecto.
     */
    public static InputStream obtenerFlujoImagenPorTipo(Vehiculo vehiculo) {
        if (vehiculo == null) {
            return VehiculoController.class.getResourceAsStream("/autos/default.png");
        }

        // 1. Intentar primero imagen específica por Marca_Modelo si existiera en /autos/
        String marcaFormateada = vehiculo.getMarca() != null
                ? vehiculo.getMarca().trim().replace(" ", "_")
                : "";
        String modeloFormateado = vehiculo.getMod() != null
                ? vehiculo.getMod().trim().replace(" ", "_")
                : "";

        InputStream flujo = null;
        if (!marcaFormateada.isEmpty() && !modeloFormateado.isEmpty()) {
            String rutaEspecifica = "/autos/" + marcaFormateada + "_" + modeloFormateado + ".png";
            flujo = VehiculoController.class.getResourceAsStream(rutaEspecifica);
        }

        // 2. Si no tiene imagen específica, identificar por código de qué tipo es (Auto, Combi, Furgoneta)
        if (flujo == null && vehiculo.getCat() != null) {
            String tipo = vehiculo.getCat().trim().toLowerCase();
            if (tipo.contains("combi") || tipo.contains("minibus")) {
                flujo = VehiculoController.class.getResourceAsStream("/autos/cat_combi.png");
            } else if (tipo.contains("furgoneta") || tipo.contains("utilitario") || tipo.contains("cargo")) {
                flujo = VehiculoController.class.getResourceAsStream("/autos/cat_furgoneta.png");
            } else if (tipo.contains("auto")) {
                flujo = VehiculoController.class.getResourceAsStream("/autos/cat_auto.png");
            }
        }

        // 3. Respaldo final a la imagen genérica predeterminada
        if (flujo == null) {
            flujo = VehiculoController.class.getResourceAsStream("/autos/default.png");
        }
        return flujo;
    }

    /**
     * Determina si un vehículo se encuentra actualmente 'Alquilado' o 'Disponible'
     * verificando tanto su atributo V_est como los contratos activos en la BD y en memoria.
     */
    private boolean estaVehiculoAlquilado(Vehiculo vehiculo) {
        if (vehiculo == null) {
            return false;
        }
        if (vehiculo.getEst() != null) {
            String estado = vehiculo.getEst().trim().toLowerCase();
            if (estado.contains("alquilado") || estado.contains("ocupado") || estado.contains("rentado")) {
                return true;
            }
        }
        if (vehiculo.getId() != null && idsVehiculosAlquilados.contains(vehiculo.getId())) {
            return true;
        }
        if (vehiculo.getPat() != null
                && patentesVehiculosAlquilados.contains(vehiculo.getPat().trim().toUpperCase())) {
            return true;
        }
        return false;
    }

    private void actualizarVehiculosAlquilados() {
        idsVehiculosAlquilados.clear();
        patentesVehiculosAlquilados.clear();

        List<Contrato> todosLosContratos = new ArrayList<>();
        Set<Integer> nrosEnBd = new HashSet<>();
        try {
            List<Contrato> desdeBd = contratoDAO.listarTodos();
            if (desdeBd != null) {
                for (Contrato c : desdeBd) {
                    todosLosContratos.add(c);
                    if (c.getNro() != null) {
                        nrosEnBd.add(c.getNro());
                    }
                }
            }
        } catch (Exception ignored) {
        }
        for (Contrato enMem : NuevoContratoController.getContratosEnMemoria()) {
            if (enMem.getNro() == null || !nrosEnBd.contains(enMem.getNro())) {
                todosLosContratos.add(enMem);
            }
        }

        for (Contrato c : todosLosContratos) {
            String est = c.getEst() != null ? c.getEst().trim().toLowerCase() : "activo";
            boolean contratoActivo = !est.equals("devuelto")
                    && !est.equals("pagado")
                    && !est.equals("finalizado")
                    && !est.equals("cancelado");
            if (contratoActivo) {
                if (c.getIdAsigna() != null) {
                    idsVehiculosAlquilados.add(c.getIdAsigna());
                }
                if (c.getVehiculoPatente() != null && !c.getVehiculoPatente().trim().isEmpty()) {
                    patentesVehiculosAlquilados.add(c.getVehiculoPatente().trim().toUpperCase());
                }
            }
        }
    }

    private void actualizarEncabezadoSucursalFiltro() {
        if (lblTituloGaleria != null) {
            if (sucursalFiltroActiva != null) {
                String nomSuc = sucursalFiltroActiva.getNom() != null
                        ? sucursalFiltroActiva.getNom()
                        : ("Sucursal #" + sucursalFiltroActiva.getCod());
                lblTituloGaleria.setText("Flota · " + nomSuc);
            } else {
                lblTituloGaleria.setText("Flota de Vehículos");
            }
        }
        if (btnLimpiarFiltroSucursal != null) {
            boolean activo = sucursalFiltroActiva != null;
            btnLimpiarFiltroSucursal.setVisible(activo);
            btnLimpiarFiltroSucursal.setManaged(activo);
        }
    }

    @FXML
    public void quitarFiltroSucursal() {
        this.sucursalFiltroActiva = null;
        actualizarEncabezadoSucursalFiltro();
        aplicarFiltrosGaleria();
    }

    private void aplicarFiltrosGaleria() {
        String filtroTexto = (txtBuscarVehiculo != null && txtBuscarVehiculo.getText() != null)
                ? txtBuscarVehiculo.getText().trim().toLowerCase()
                : "";
        String tipoSeleccionado = (cmbFiltroTipo != null && cmbFiltroTipo.getValue() != null)
                ? cmbFiltroTipo.getValue().trim()
                : "Todos";

        List<Vehiculo> filtrados = new ArrayList<>();
        for (Vehiculo v : listaCompleta) {
            if (sucursalFiltroActiva != null && sucursalFiltroActiva.getCod() != null) {
                if (v.getCodPertenece() == null || !v.getCodPertenece().equals(sucursalFiltroActiva.getCod())) {
                    continue;
                }
            }
            if (!coincideTipoVehiculo(v, tipoSeleccionado)) {
                continue;
            }
            if (!filtroTexto.isEmpty()) {
                String etiquetaEstado = estaVehiculoAlquilado(v) ? "alquilado" : "disponible";
                boolean coincideTexto = (v.getMarca() != null && v.getMarca().toLowerCase().contains(filtroTexto))
                        || (v.getMod() != null && v.getMod().toLowerCase().contains(filtroTexto))
                        || (v.getPat() != null && v.getPat().toLowerCase().contains(filtroTexto))
                        || (v.getCat() != null && v.getCat().toLowerCase().contains(filtroTexto))
                        || etiquetaEstado.contains(filtroTexto);
                if (!coincideTexto) {
                    continue;
                }
            }
            filtrados.add(v);
        }
        cargarGrillaVehiculos(filtrados);
    }

    private boolean coincideTipoVehiculo(Vehiculo v, String tipoSeleccionado) {
        if (v == null) {
            return false;
        }
        if (tipoSeleccionado == null || tipoSeleccionado.isEmpty() || tipoSeleccionado.equalsIgnoreCase("Todos")) {
            return true;
        }
        String catVehiculo = (v.getCat() != null) ? v.getCat().trim().toLowerCase() : "";
        String buscada = tipoSeleccionado.trim().toLowerCase();

        if (catVehiculo.equals(buscada)) {
            return true;
        }
        boolean esCombi = catVehiculo.contains("combi") || catVehiculo.contains("minibus") || catVehiculo.contains("minibús");
        boolean esFurgoneta = catVehiculo.contains("furgon") || catVehiculo.contains("furgón")
                || catVehiculo.contains("utilitario") || catVehiculo.contains("cargo");

        if (buscada.equals("combi")) {
            return esCombi;
        }
        if (buscada.equals("furgoneta")) {
            return esFurgoneta;
        }
        if (buscada.equals("auto")) {
            return catVehiculo.contains("auto") || (!esCombi && !esFurgoneta);
        }
        return false;
    }

    public void recargarVehiculos() {
        actualizarVehiculosAlquilados();

        List<Vehiculo> desdeBd = new ArrayList<>();
        try {
            desdeBd = vehiculoService.listarTodos();
        } catch (Exception ignored) {
            // Si la base de datos no está disponible aún, se conserva la lista registrada en sesión
        }

        listaCompleta = new ArrayList<>(desdeBd);
        for (Vehiculo enSesion : VEHICULOS_REGISTRADOS) {
            boolean yaExiste = false;
            for (Vehiculo v : listaCompleta) {
                if (v.getPat() != null && enSesion.getPat() != null
                        && v.getPat().equalsIgnoreCase(enSesion.getPat())) {
                    yaExiste = true;
                    break;
                }
            }
            if (!yaExiste) {
                listaCompleta.add(enSesion);
            }
        }

        aplicarFiltrosGaleria();
    }

    @FXML
    public void mostrarVistaAlta() {
        if (vistaGaleria != null && vistaAlta != null) {
            cargarSucursales();
            vistaGaleria.setVisible(false);
            vistaGaleria.setManaged(false);
            if (vistaDetalle != null) {
                vistaDetalle.setVisible(false);
                vistaDetalle.setManaged(false);
            }
            if (vistaHistorialMantenimiento != null) {
                vistaHistorialMantenimiento.setVisible(false);
                vistaHistorialMantenimiento.setManaged(false);
            }
            if (vistaNuevoMantenimiento != null) {
                vistaNuevoMantenimiento.setVisible(false);
                vistaNuevoMantenimiento.setManaged(false);
            }
            vistaAlta.setVisible(true);
            vistaAlta.setManaged(true);
        }
    }

    @FXML
    public void mostrarVistaGaleria() {
        if (vistaGaleria != null && vistaAlta != null) {
            vistaAlta.setVisible(false);
            vistaAlta.setManaged(false);
            if (vistaDetalle != null) {
                vistaDetalle.setVisible(false);
                vistaDetalle.setManaged(false);
            }
            if (vistaHistorialMantenimiento != null) {
                vistaHistorialMantenimiento.setVisible(false);
                vistaHistorialMantenimiento.setManaged(false);
            }
            if (vistaNuevoMantenimiento != null) {
                vistaNuevoMantenimiento.setVisible(false);
                vistaNuevoMantenimiento.setManaged(false);
            }
            vistaGaleria.setVisible(true);
            vistaGaleria.setManaged(true);
            if (cmbFiltroTipo != null) {
                cmbFiltroTipo.setValue("Todos");
            }
            recargarVehiculos();
        }
    }

    @FXML
    public void guardarVehiculo() {
        try {
            String pat = txtPat.getText().trim();
            String marca = (comboMarca != null && comboMarca.getValue() != null) ? comboMarca.getValue().trim() : "";
            String mod = (comboModelo != null && comboModelo.getValue() != null) ? comboModelo.getValue().trim() : "";
            Integer aFabr = cmbAFabr != null ? cmbAFabr.getValue() : null;
            LocalDate fechaAdq = dpFAdq != null ? dpFAdq.getValue() : null;
            String fAdq = fechaAdq != null ? fechaAdq.toString() : "";
            Integer kmj = Integer.parseInt(txtKmj.getText().trim());
            String est = txtEst.getText().trim();
            Double preAdq = Double.parseDouble(txtPreAdq.getText().trim());
            String tiComb = (cmbTiComb != null && cmbTiComb.getValue() != null) ? cmbTiComb.getValue().trim() : "";
            String tran = (cmbTransmision != null && cmbTransmision.getValue() != null) ? cmbTransmision.getValue().trim() : "";
            Double carga = Double.parseDouble(txtCarga.getText().trim());
            Integer pax = Integer.parseInt(txtPax.getText().trim());
            Double tarBase = Double.parseDouble(txtTarBase.getText().trim());
            String cat = cmbCat.getValue();
            Sucursal sucursalSeleccionada = cmbSucursal.getValue();

            if (pat.isEmpty() || marca.isEmpty() || mod.isEmpty() || aFabr == null || fechaAdq == null
                    || est.isEmpty() || tiComb.isEmpty() || tran.isEmpty() || cat == null || sucursalSeleccionada == null) {
                mostrarMensaje("Error de Validación",
                        "Por favor complete todos los campos obligatorios (incluyendo Patente, Marca, Modelo, Combustible, Transmisión, Año de Fabricación, Fecha de Adquisición y Sucursal).",
                        Alert.AlertType.WARNING);
                return;
            }

            int anioActual = LocalDate.now().getYear();
            int anioMinimo = anioActual - 10;
            if (aFabr < anioMinimo || aFabr > anioActual) {
                mostrarMensaje("Error de Validación",
                        "El año de fabricación debe estar entre " + anioMinimo + " y " + anioActual + ".",
                        Alert.AlertType.WARNING);
                return;
            }

            if (fechaAdq.getYear() < aFabr) {
                mostrarMensaje("Error de Validación",
                        "La fecha de adquisición (" + FormatoFecha.formatearFechaGui(fechaAdq)
                        + ") no puede ser menor al año de fabricación (" + aFabr + ").",
                        Alert.AlertType.WARNING);
                return;
            }

            Integer codPertenece = sucursalSeleccionada.getCod();

            // Usa el constructor sin ID (fBaja = null en el alta; solo se edita en la ficha de detalle)
            Vehiculo nuevo = new Vehiculo(pat, marca, mod, tran, aFabr, fAdq, kmj, est,
                    preAdq, null, tiComb, carga, pax, tarBase, cat, codPertenece);

            vehiculoService.altaVehiculo(nuevo);

            VEHICULOS_REGISTRADOS.add(nuevo);

            mostrarMensaje("Éxito",
                    "El vehículo (" + cat + ", patente " + pat + ") se registró correctamente.",
                    Alert.AlertType.INFORMATION);
            limpiarCampos();
            mostrarVistaGaleria();

        } catch (NumberFormatException e) {
            mostrarMensaje("Error de Validación",
                    "Verifique que Kilometraje, Precio, Carga, Pasajeros y Tarifa sean valores numéricos válidos.",
                    Alert.AlertType.WARNING);
        } catch (Exception e) {
            mostrarMensaje("Error", "No se pudo guardar el vehículo: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void limpiarCampos() {
        if (txtPat != null) txtPat.clear();
        if (comboMarca != null) comboMarca.getSelectionModel().clearSelection();
        if (comboModelo != null) {
            comboModelo.getSelectionModel().clearSelection();
            comboModelo.getItems().clear();
        }
        if (cmbAFabr != null && !cmbAFabr.getItems().isEmpty()) {
            cmbAFabr.getSelectionModel().selectFirst();
        }
        if (dpFAdq != null) dpFAdq.setValue(null);
        if (txtKmj != null) txtKmj.clear();
        if (txtEst != null) txtEst.clear();
        if (txtPreAdq != null) txtPreAdq.clear();
        if (cmbTiComb != null && !cmbTiComb.getItems().isEmpty()) {
            cmbTiComb.getSelectionModel().selectFirst();
        }
        if (cmbTransmision != null && !cmbTransmision.getItems().isEmpty()) {
            cmbTransmision.getSelectionModel().selectFirst();
        }
        if (txtCarga != null) txtCarga.clear();
        if (txtPax != null) txtPax.clear();
        if (txtTarBase != null) txtTarBase.clear();
        if (cmbSucursal != null && !cmbSucursal.getItems().isEmpty()) {
            cmbSucursal.getSelectionModel().selectFirst();
        }
    }

    private void mostrarMensaje(String titulo, String contenido, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(contenido);
        alerta.showAndWait();
    }

    // =========================================================================
    // MÓDULO DE MANTENIMIENTOS DEL VEHÍCULO (HISTORIAL Y ALTA - TRANSACCIÓN 4.4)
    // =========================================================================

    private void configurarTablaMantenimientos() {
        if (tablaMantenimientos == null) {
            return;
        }

        if (colMantId != null) {
            colMantId.setCellValueFactory(cellData ->
                    new SimpleObjectProperty<>(cellData.getValue() != null ? cellData.getValue().getId() : null));
        }
        if (colMantFecha != null) {
            colMantFecha.setCellValueFactory(cellData -> {
                if (cellData.getValue() == null || cellData.getValue().getFec() == null) {
                    return new SimpleStringProperty("-");
                }
                return new SimpleStringProperty(FormatoFecha.formatearFechaGui(cellData.getValue().getFec()));
            });
        }
        if (colMantTipo != null) {
            colMantTipo.setCellValueFactory(cellData -> {
                if (cellData.getValue() == null || cellData.getValue().getTipo() == null) {
                    return new SimpleStringProperty("-");
                }
                return new SimpleStringProperty(cellData.getValue().getTipo().getDescripcion());
            });
        }
        if (colMantKmj != null) {
            colMantKmj.setCellValueFactory(cellData -> {
                if (cellData.getValue() == null || cellData.getValue().getKmj() == null) {
                    return new SimpleStringProperty("-");
                }
                return new SimpleStringProperty(String.format("%,d km", cellData.getValue().getKmj()));
            });
        }
        if (colMantCosto != null) {
            colMantCosto.setCellValueFactory(cellData -> {
                if (cellData.getValue() == null || cellData.getValue().getCosto() == null) {
                    return new SimpleStringProperty("$0.00");
                }
                return new SimpleStringProperty(String.format("$%.2f", cellData.getValue().getCosto()));
            });
        }
        if (colMantEmpleado != null) {
            colMantEmpleado.setCellValueFactory(cellData -> {
                if (cellData.getValue() == null || cellData.getValue().getIdRealiza() == null) {
                    return new SimpleStringProperty("-");
                }
                return new SimpleStringProperty(obtenerNombreEmpleado(cellData.getValue().getIdRealiza()));
            });
        }
        if (colMantObs != null) {
            colMantObs.setCellValueFactory(cellData -> {
                if (cellData.getValue() == null || cellData.getValue().getObs() == null) {
                    return new SimpleStringProperty("");
                }
                return new SimpleStringProperty(cellData.getValue().getObs());
            });
        }

        Label placeholder = new Label("No se registran mantenimientos previos para esta unidad.");
        placeholder.setStyle("-fx-font-size: 13px; -fx-text-fill: #6B7280; -fx-padding: 20;");
        tablaMantenimientos.setPlaceholder(placeholder);
    }

    private void configurarFormularioMantenimiento() {
        if (dpNuevoMantFecha != null) {
            FormatoFecha.configurarDatePicker(dpNuevoMantFecha);
            dpNuevoMantFecha.setValue(LocalDate.now());
            dpNuevoMantFecha.setDayCellFactory(picker -> new DateCell() {
                @Override
                public void updateItem(LocalDate date, boolean empty) {
                    super.updateItem(date, empty);
                    setDisable(empty || date.isAfter(LocalDate.now()));
                }
            });
        }

        if (cmbNuevoMantTipo != null) {
            cmbNuevoMantTipo.getItems().setAll(TipoMantenimiento.values());
            cmbNuevoMantTipo.setConverter(new StringConverter<TipoMantenimiento>() {
                @Override
                public String toString(TipoMantenimiento tipo) {
                    return tipo != null ? tipo.getDescripcion() : "";
                }

                @Override
                public TipoMantenimiento fromString(String string) {
                    return TipoMantenimiento.fromString(string);
                }
            });
            if (!cmbNuevoMantTipo.getItems().isEmpty()) {
                cmbNuevoMantTipo.getSelectionModel().selectFirst();
            }
        }

        if (cmbNuevoMantEmpleado != null) {
            cmbNuevoMantEmpleado.setConverter(new StringConverter<Empleado>() {
                @Override
                public String toString(Empleado emp) {
                    if (emp == null) return "";
                    String apl = emp.getApl() != null ? emp.getApl().trim() : "";
                    String nom = emp.getNom() != null ? emp.getNom().trim() : "";
                    String dni = emp.getDni() != null ? String.valueOf(emp.getDni()) : "";
                    return (apl + ", " + nom + " (DNI: " + dni + ")").trim();
                }

                @Override
                public Empleado fromString(String string) {
                    return null;
                }
            });
            cargarEmpleados();
        }

        if (txtNuevoMantObs != null && lblNuevoMantContadorObs != null) {
            txtNuevoMantObs.textProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal == null) {
                    lblNuevoMantContadorObs.setText("0 / 100 caracteres");
                    return;
                }
                if (newVal.length() > 100) {
                    txtNuevoMantObs.setText(newVal.substring(0, 100));
                    newVal = txtNuevoMantObs.getText();
                }
                lblNuevoMantContadorObs.setText(newVal.length() + " / 100 caracteres");
            });
        }
    }

    private void cargarEmpleados() {
        try {
            List<Empleado> empleados = empleadoDAO.listarTodos();
            cacheEmpleados.clear();
            for (Empleado e : empleados) {
                if (e.getId() != null) {
                    cacheEmpleados.put(e.getId(), e);
                }
            }
            if (cmbNuevoMantEmpleado != null) {
                cmbNuevoMantEmpleado.getItems().setAll(empleados);
            }
        } catch (Exception e) {
            System.err.println("No se pudieron cargar los empleados para mantenimiento: " + e.getMessage());
        }
    }

    private String obtenerNombreEmpleado(Integer idEmpleado) {
        if (idEmpleado == null) return "-";
        Empleado e = cacheEmpleados.get(idEmpleado);
        if (e != null) {
            String apl = e.getApl() != null ? e.getApl().trim() : "";
            String nom = e.getNom() != null ? e.getNom().trim() : "";
            return (apl + ", " + nom).trim();
        }
        return "Empleado #" + idEmpleado;
    }

    /**
     * Abre la pantalla con el historial de mantenimientos previos del vehículo actual.
     */
    @FXML
    public void abrirHistorialMantenimiento() {
        if (vehiculoActualDetalle == null) {
            mostrarMensaje("Atención", "No se ha seleccionado ningún vehículo para ver mantenimientos.", Alert.AlertType.WARNING);
            return;
        }

        if (vistaDetalle != null) {
            vistaDetalle.setVisible(false);
            vistaDetalle.setManaged(false);
        }
        if (vistaGaleria != null) {
            vistaGaleria.setVisible(false);
            vistaGaleria.setManaged(false);
        }
        if (vistaAlta != null) {
            vistaAlta.setVisible(false);
            vistaAlta.setManaged(false);
        }
        if (vistaNuevoMantenimiento != null) {
            vistaNuevoMantenimiento.setVisible(false);
            vistaNuevoMantenimiento.setManaged(false);
        }

        cargarHistorialMantenimiento(vehiculoActualDetalle);

        if (vistaHistorialMantenimiento != null) {
            vistaHistorialMantenimiento.setVisible(true);
            vistaHistorialMantenimiento.setManaged(true);
        }
    }

    private void cargarHistorialMantenimiento(Vehiculo v) {
        if (v == null) return;
        cargarEmpleados();

        String marcaMod = ((v.getMarca() != null ? v.getMarca() : "") + " " + (v.getMod() != null ? v.getMod() : "")).trim();
        String pat = v.getPat() != null ? v.getPat() : "-";
        Integer id = v.getId();

        if (lblHistorialTitulo != null) {
            lblHistorialTitulo.setText("Mantenimientos Previos · " + marcaMod);
        }
        if (lblHistorialSubtitulo != null) {
            lblHistorialSubtitulo.setText("Unidad: " + marcaMod + " · Patente: " + pat + " · ID de Unidad: #" + (id != null ? id : "-"));
        }

        List<Mantenimiento> mantenimientos = Collections.emptyList();
        if (id != null) {
            try {
                mantenimientos = mantenimientoService.listarPorVehiculo(id);
            } catch (Exception e) {
                System.err.println("Error al obtener mantenimientos del vehículo: " + e.getMessage());
            }
        }

        if (tablaMantenimientos != null) {
            tablaMantenimientos.getItems().setAll(mantenimientos);
        }

        int cantidad = mantenimientos.size();
        double costoTotal = 0.0;
        for (Mantenimiento m : mantenimientos) {
            if (m.getCosto() != null) {
                costoTotal += m.getCosto();
            }
        }

        if (lblHistorialTotalServicios != null) {
            lblHistorialTotalServicios.setText(String.valueOf(cantidad));
        }
        if (lblHistorialCostoTotal != null) {
            lblHistorialCostoTotal.setText(String.format("$%.2f", costoTotal));
        }
        if (lblHistorialEstadoTabla != null) {
            lblHistorialEstadoTabla.setText(cantidad == 0
                    ? "Esta unidad no registra mantenimientos previos en la base de datos"
                    : "Mostrando " + cantidad + " servicio(s) registrado(s)");
        }
    }

    /**
     * Abre la pantalla para registrar un nuevo mantenimiento desde la ficha del vehículo.
     */
    @FXML
    public void abrirNuevoMantenimiento() {
        if (vehiculoActualDetalle == null) {
            mostrarMensaje("Atención", "No se ha seleccionado ningún vehículo para registrar mantenimiento.", Alert.AlertType.WARNING);
            return;
        }
        vieneDesdeHistorial = false;
        prepararYMostrarNuevoMantenimiento(vehiculoActualDetalle);
    }

    /**
     * Abre la pantalla para registrar un nuevo mantenimiento directamente desde el historial.
     */
    @FXML
    public void mostrarVistaNuevoMantenimientoDesdeHistorial() {
        if (vehiculoActualDetalle == null) {
            return;
        }
        vieneDesdeHistorial = true;
        prepararYMostrarNuevoMantenimiento(vehiculoActualDetalle);
    }

    private void prepararYMostrarNuevoMantenimiento(Vehiculo v) {
        if (v == null) return;
        cargarEmpleados();

        if (vistaDetalle != null) {
            vistaDetalle.setVisible(false);
            vistaDetalle.setManaged(false);
        }
        if (vistaGaleria != null) {
            vistaGaleria.setVisible(false);
            vistaGaleria.setManaged(false);
        }
        if (vistaAlta != null) {
            vistaAlta.setVisible(false);
            vistaAlta.setManaged(false);
        }
        if (vistaHistorialMantenimiento != null) {
            vistaHistorialMantenimiento.setVisible(false);
            vistaHistorialMantenimiento.setManaged(false);
        }

        String marcaMod = ((v.getMarca() != null ? v.getMarca() : "") + " " + (v.getMod() != null ? v.getMod() : "")).trim();
        String pat = v.getPat() != null ? v.getPat() : "-";
        Integer kmj = v.getKmj() != null ? v.getKmj() : 0;
        String sucursalTexto = formatearSucursal(v.getCodPertenece());

        if (lblNuevoMantTitulo != null) {
            lblNuevoMantTitulo.setText("Registrar Mantenimiento · " + marcaMod);
        }
        if (lblNuevoMantSubtitulo != null) {
            lblNuevoMantSubtitulo.setText("Carga de servicio en taller oficial para patente " + pat + " (Transacción 4.4)");
        }
        if (lblNuevoMantVehiculoInfo != null) {
            lblNuevoMantVehiculoInfo.setText(marcaMod + " (" + (v.getCat() != null ? v.getCat() : "Auto") + ")");
        }
        if (lblNuevoMantPatente != null) {
            lblNuevoMantPatente.setText(pat);
        }
        if (lblNuevoMantKmjActual != null) {
            lblNuevoMantKmjActual.setText(String.format("%,d km", kmj));
        }
        if (lblNuevoMantSucursal != null) {
            lblNuevoMantSucursal.setText(sucursalTexto);
        }

        // Limpiar / resetear campos del formulario
        if (dpNuevoMantFecha != null) {
            dpNuevoMantFecha.setValue(LocalDate.now());
        }
        if (cmbNuevoMantTipo != null) {
            if (!cmbNuevoMantTipo.getItems().isEmpty()) {
                cmbNuevoMantTipo.getSelectionModel().selectFirst();
            }
        }
        if (txtNuevoMantKmj != null) {
            txtNuevoMantKmj.setText(String.valueOf(kmj));
        }
        if (txtNuevoMantCosto != null) {
            txtNuevoMantCosto.clear();
        }
        if (cmbNuevoMantEmpleado != null) {
            if (!cmbNuevoMantEmpleado.getItems().isEmpty()) {
                cmbNuevoMantEmpleado.getSelectionModel().selectFirst();
            }
        }
        if (txtNuevoMantObs != null) {
            txtNuevoMantObs.clear();
        }
        if (lblNuevoMantContadorObs != null) {
            lblNuevoMantContadorObs.setText("0 / 100 caracteres");
        }

        if (vistaNuevoMantenimiento != null) {
            vistaNuevoMantenimiento.setVisible(true);
            vistaNuevoMantenimiento.setManaged(true);
        }
    }

    /**
     * Vuelve desde el historial de mantenimientos a la ficha del vehículo.
     */
    @FXML
    public void volverADetalleDesdeMantenimiento() {
        if (vistaHistorialMantenimiento != null) {
            vistaHistorialMantenimiento.setVisible(false);
            vistaHistorialMantenimiento.setManaged(false);
        }
        if (vistaDetalle != null) {
            vistaDetalle.setVisible(true);
            vistaDetalle.setManaged(true);
        }
    }

    /**
     * Vuelve desde el formulario de nuevo mantenimiento a la pantalla previa (historial o ficha).
     */
    @FXML
    public void volverADetalleDesdeNuevoMantenimiento() {
        if (vistaNuevoMantenimiento != null) {
            vistaNuevoMantenimiento.setVisible(false);
            vistaNuevoMantenimiento.setManaged(false);
        }
        if (vieneDesdeHistorial) {
            if (vistaHistorialMantenimiento != null) {
                vistaHistorialMantenimiento.setVisible(true);
                vistaHistorialMantenimiento.setManaged(true);
            }
        } else {
            if (vistaDetalle != null) {
                vistaDetalle.setVisible(true);
                vistaDetalle.setManaged(true);
            }
        }
    }

    /**
     * Limpia los campos del formulario de registro de mantenimiento.
     */
    @FXML
    public void limpiarFormularioNuevoMantenimiento() {
        if (dpNuevoMantFecha != null) {
            dpNuevoMantFecha.setValue(LocalDate.now());
        }
        if (cmbNuevoMantTipo != null) {
            cmbNuevoMantTipo.getSelectionModel().clearSelection();
        }
        if (txtNuevoMantKmj != null) {
            Integer kmjActual = vehiculoActualDetalle != null && vehiculoActualDetalle.getKmj() != null
                    ? vehiculoActualDetalle.getKmj()
                    : 0;
            txtNuevoMantKmj.setText(String.valueOf(kmjActual));
        }
        if (txtNuevoMantCosto != null) {
            txtNuevoMantCosto.clear();
        }
        if (cmbNuevoMantEmpleado != null) {
            cmbNuevoMantEmpleado.getSelectionModel().clearSelection();
        }
        if (txtNuevoMantObs != null) {
            txtNuevoMantObs.clear();
        }
        if (lblNuevoMantContadorObs != null) {
            lblNuevoMantContadorObs.setText("0 / 100 caracteres");
        }
    }

    /**
     * Valida y persiste un nuevo mantenimiento para el vehículo actual según Transacción 4.4.
     */
    @FXML
    public void guardarNuevoMantenimiento() {
        if (vehiculoActualDetalle == null || vehiculoActualDetalle.getId() == null) {
            mostrarMensaje("Error", "No se pudo identificar la unidad vehicular a la cual registrar el mantenimiento.", Alert.AlertType.ERROR);
            return;
        }

        LocalDate fecha = dpNuevoMantFecha != null ? dpNuevoMantFecha.getValue() : null;
        if (fecha == null) {
            mostrarMensaje("Campo Requerido", "Debe seleccionar la fecha en la que se realizó el mantenimiento.", Alert.AlertType.WARNING);
            if (dpNuevoMantFecha != null) dpNuevoMantFecha.requestFocus();
            return;
        }
        if (fecha.isAfter(LocalDate.now())) {
            mostrarMensaje("Fecha Inválida", "La fecha del mantenimiento no puede ser posterior a la fecha actual.", Alert.AlertType.WARNING);
            if (dpNuevoMantFecha != null) dpNuevoMantFecha.requestFocus();
            return;
        }

        TipoMantenimiento tipo = cmbNuevoMantTipo != null ? cmbNuevoMantTipo.getValue() : null;
        if (tipo == null) {
            mostrarMensaje("Campo Requerido", "Debe seleccionar el tipo de mantenimiento realizado (Fluidos, Filtros, Neumáticos, etc.).", Alert.AlertType.WARNING);
            if (cmbNuevoMantTipo != null) cmbNuevoMantTipo.requestFocus();
            return;
        }

        String kmjStr = txtNuevoMantKmj != null ? txtNuevoMantKmj.getText().trim() : "";
        if (kmjStr.isEmpty()) {
            mostrarMensaje("Campo Requerido", "Debe ingresar el kilometraje del vehículo al momento del servicio.", Alert.AlertType.WARNING);
            if (txtNuevoMantKmj != null) txtNuevoMantKmj.requestFocus();
            return;
        }
        int kmj;
        try {
            kmj = Integer.parseInt(kmjStr);
            if (kmj < 0) {
                mostrarMensaje("Valor Inválido", "El kilometraje no puede ser negativo.", Alert.AlertType.WARNING);
                return;
            }
        } catch (NumberFormatException e) {
            mostrarMensaje("Valor Inválido", "El kilometraje debe ser un número entero válido.", Alert.AlertType.WARNING);
            return;
        }

        String costoStr = txtNuevoMantCosto != null ? txtNuevoMantCosto.getText().trim().replace(",", ".") : "";
        if (costoStr.isEmpty()) {
            mostrarMensaje("Campo Requerido", "Debe ingresar el costo del mantenimiento.", Alert.AlertType.WARNING);
            if (txtNuevoMantCosto != null) txtNuevoMantCosto.requestFocus();
            return;
        }
        double costo;
        try {
            costo = Double.parseDouble(costoStr);
            if (costo < 0) {
                mostrarMensaje("Valor Inválido", "El costo del mantenimiento no puede ser negativo.", Alert.AlertType.WARNING);
                return;
            }
            if (costo > 999999.99) {
                mostrarMensaje("Valor Inválido", "El costo no puede superar $999.999,99.", Alert.AlertType.WARNING);
                return;
            }
        } catch (NumberFormatException e) {
            mostrarMensaje("Valor Inválido", "El costo debe ser un valor numérico válido (ej. 15000.00).", Alert.AlertType.WARNING);
            return;
        }

        Empleado empleado = cmbNuevoMantEmpleado != null ? cmbNuevoMantEmpleado.getValue() : null;
        if (empleado == null || empleado.getId() == null) {
            mostrarMensaje("Campo Requerido", "Debe seleccionar el empleado / mecánico que realizó el mantenimiento.", Alert.AlertType.WARNING);
            if (cmbNuevoMantEmpleado != null) cmbNuevoMantEmpleado.requestFocus();
            return;
        }

        String obs = txtNuevoMantObs != null ? txtNuevoMantObs.getText().trim() : "";
        if (obs.length() > 100) {
            mostrarMensaje("Texto demasiado largo", "Las observaciones no pueden superar los 100 caracteres.", Alert.AlertType.WARNING);
            return;
        }

        Mantenimiento m = new Mantenimiento();
        m.setFec(fecha.toString()); // yyyy-MM-dd
        m.setTipo(tipo);
        m.setKmj(kmj);
        m.setCosto(costo);
        m.setObs(obs.isEmpty() ? null : obs);
        m.setIdRealiza(empleado.getId());
        m.setIdRealizado(vehiculoActualDetalle.getId());

        try {
            mantenimientoService.altaMantenimiento(m);

            // Si el kilometraje del mantenimiento es mayor al actual del vehículo, actualizarlo
            if (vehiculoActualDetalle.getKmj() == null || kmj > vehiculoActualDetalle.getKmj()) {
                vehiculoActualDetalle.setKmj(kmj);
                try {
                    VehiculoDAO vDao = new VehiculoDAO();
                    vDao.actualizar(vehiculoActualDetalle);
                } catch (Exception exActualizar) {
                    System.err.println("Aviso: No se pudo actualizar el km en el vehículo: " + exActualizar.getMessage());
                }
                if (txtDetKmj != null) {
                    txtDetKmj.setText(String.valueOf(kmj));
                }
            }

            mostrarMensaje("Mantenimiento Registrado con Éxito",
                    "El mantenimiento #" + (m.getId() != null ? m.getId() : "") + " (" + tipo.getDescripcion() + ") por $"
                            + String.format("%.2f", costo) + " se registró correctamente para la unidad "
                            + vehiculoActualDetalle.getPat() + ".",
                    Alert.AlertType.INFORMATION);

            // Redirigir a la pantalla de Historial de Mantenimientos para que el usuario visualice el nuevo registro
            if (vistaNuevoMantenimiento != null) {
                vistaNuevoMantenimiento.setVisible(false);
                vistaNuevoMantenimiento.setManaged(false);
            }
            cargarHistorialMantenimiento(vehiculoActualDetalle);
            if (vistaHistorialMantenimiento != null) {
                vistaHistorialMantenimiento.setVisible(true);
                vistaHistorialMantenimiento.setManaged(true);
            }

        } catch (Exception e) {
            mostrarMensaje("Error al Guardar Mantenimiento", e.getMessage(), Alert.AlertType.ERROR);
        }
    }
}
