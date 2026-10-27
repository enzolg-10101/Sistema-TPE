package controlador;

import dao.AsignaDAO;
import dao.CheckInOutDAO;
import dao.ClienteDAO;
import dao.ContratoDAO;
import dao.CorrespondeDAO;
import dao.FirmaDAO;
import dao.SucursalDAO;
import dao.VehiculoDAO;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;
import modelo.CheckInOut;
import modelo.Cliente;
import modelo.Contrato;
import modelo.Vehiculo;
import servicio.CheckInOutService;
import servicio.ClienteService;
import servicio.ContratoService;
import servicio.VehiculoService;

public class NuevoContratoController {

    private static final String ESTILO_BOTON_CATEGORIA_NORMAL =
            "-fx-background-color: #FFFFFF; -fx-background-radius: 18; "
            + "-fx-border-color: #E5E7EB; -fx-border-radius: 18; -fx-border-width: 1.5; "
            + "-fx-padding: 22; -fx-cursor: hand;";

    private static final String ESTILO_BOTON_CATEGORIA_HOVER =
            "-fx-background-color: #FFFFFF; -fx-background-radius: 18; "
            + "-fx-border-color: #F59E0B; -fx-border-radius: 18; -fx-border-width: 2.0; "
            + "-fx-padding: 22; -fx-cursor: hand;";

    private static final String ESTILO_CONTENEDOR_IMG_NORMAL =
            "-fx-background-color: #F9FAFB; -fx-background-radius: 14;";

    private static final String ESTILO_CONTENEDOR_IMG_HOVER =
            "-fx-background-color: #FEF3C7; -fx-background-radius: 14;";

    private static final String ESTILO_CAMPO_CLIENTE_BLOQUEADO =
            "-fx-background-color: #F3F4F6; -fx-background-radius: 8; -fx-border-color: transparent; -fx-text-fill: #374151;";

    private static final String ESTILO_CAMPO_CLIENTE_EDITABLE =
            "-fx-background-color: #FFFFFF; -fx-background-radius: 8; -fx-border-color: #F59E0B; -fx-border-radius: 8; -fx-border-width: 1.4; -fx-text-fill: #111827;";

    // Vistas del flujo de Nuevo Contrato
    @FXML private VBox vistaCategorias;
    @FXML private VBox vistaSeleccionVehiculo;
    @FXML private VBox vistaFormularioContrato;

    // Paso 1: Los 3 botones grandes de categoría y sus imágenes representativas
    @FXML private Button btnCategoriaAuto;
    @FXML private Button btnCategoriaCombi;
    @FXML private Button btnCategoriaFurgoneta;

    @FXML private StackPane contenedorImgAuto;
    @FXML private StackPane contenedorImgCombi;
    @FXML private StackPane contenedorImgFurgoneta;

    @FXML private ImageView imgAuto;
    @FXML private ImageView imgCombi;
    @FXML private ImageView imgFurgoneta;

    // Paso 2: Galería de vehículos y barra superior de disponibilidad entre dos fechas
    @FXML private Label lblTituloCategoria;
    @FXML private Label lblCantidadCategoria;
    @FXML private TextField txtBuscarVehiculo;
    @FXML private DatePicker dpFiltroInicio;
    @FXML private DatePicker dpFiltroDevolucion;
    @FXML private Label lblDiasAlquiler;
    @FXML private TilePane contenedorVehiculos;

    // Paso 3: Resumen del vehículo seleccionado y formulario del contrato
    @FXML private Label lblResumenVehiculoElegido;
    @FXML private ImageView imgVehiculoSeleccionado;
    @FXML private Label lblCardMarcaModelo;
    @FXML private Label lblCardPatente;
    @FXML private Label lblCardCategoria;
    @FXML private Label lblCardTarifa;

    // Campos del formulario de Contrato (sin IDs manuales: Co_nro, C_id y Ck_id son AUTO_INCREMENT)
    @FXML private DatePicker dpFechaEntrega;
    @FXML private DatePicker dpFechaDevolucion;
    @FXML private TextField txtImpAlq;

    // Búsqueda de cliente existente (con pantalla emergente) o alta mediante la ventana flotante auxiliar
    @FXML private TextField txtBuscarClienteContrato;
    @FXML private Button btnBuscarCliente;
    @FXML private Label lblEstadoClienteContrato;
    @FXML private HBox tarjetaClienteSeleccionado;
    @FXML private Label lblClienteResumenNombre;
    @FXML private Label lblClienteResumenDatos;

    @FXML private TextField txtKmSalida;
    @FXML private TextField txtCombSalida;
    @FXML private TextField txtDaniosSalida;

    private final VehiculoService vehiculoService;
    private final ClienteDAO clienteDAO;
    private final ContratoDAO contratoDAO;
    private final ContratoService contratoService;

    private List<Vehiculo> flotaCompleta = new ArrayList<>();
    private List<Vehiculo> vehiculosCategoriaActual = new ArrayList<>();
    private final List<Contrato> contratosEnMemoria = new ArrayList<>();
    private String categoriaSeleccionada = "Auto";
    private Vehiculo vehiculoSeleccionado;

    private Cliente clienteElegidoExistente = null;
    private boolean clienteNuevoPendienteDeTransaccion = false;

    public NuevoContratoController() {
        VehiculoDAO vehiculoDAO = new VehiculoDAO();
        SucursalDAO sucursalDAO = new SucursalDAO();
        this.clienteDAO = new ClienteDAO();
        CheckInOutDAO checkInOutDAO = new CheckInOutDAO();
        this.contratoDAO = new ContratoDAO();

        this.vehiculoService = new VehiculoService(vehiculoDAO, sucursalDAO);
        this.contratoService = new ContratoService(
                this.contratoDAO,
                this.clienteDAO,
                vehiculoDAO,
                checkInOutDAO,
                new FirmaDAO(),
                new AsignaDAO(),
                new CorrespondeDAO(),
                new ClienteService(this.clienteDAO),
                new CheckInOutService(checkInOutDAO)
        );
    }

    @FXML
    public void initialize() {
        // 1. Cargar las imágenes representativas de cada categoría
        cargarImagenRepresentativa(imgAuto, "/autos/cat_auto.png");
        cargarImagenRepresentativa(imgCombi, "/autos/cat_combi.png");
        cargarImagenRepresentativa(imgFurgoneta, "/autos/cat_furgoneta.png");

        // 2. Configurar la animación que resalta la imagen genérica del vehículo al pasar el mouse
        configurarAnimacionHoverCategoria(btnCategoriaAuto, contenedorImgAuto, imgAuto);
        configurarAnimacionHoverCategoria(btnCategoriaCombi, contenedorImgCombi, imgCombi);
        configurarAnimacionHoverCategoria(btnCategoriaFurgoneta, contenedorImgFurgoneta, imgFurgoneta);

        // 3. Configurar la barra de disponibilidad entre dos fechas (Paso 2):
        //    Al elegir una fecha de inicio, el segundo calendario solo permite elegir
        //    a partir del día siguiente (fechaInicio.plusDays(1)).
        if (dpFiltroInicio != null && dpFiltroDevolucion != null) {
            dpFiltroInicio.setEditable(false);
            dpFiltroDevolucion.setEditable(false);

            LocalDate hoy = LocalDate.now();
            dpFiltroInicio.setValue(hoy);
            dpFiltroDevolucion.setValue(hoy.plusDays(1));

            configurarRestriccionCalendarioDevolucion(dpFiltroInicio, dpFiltroDevolucion, () -> {
                actualizarEtiquetaDiasFiltro();
                aplicarFiltrosDisponibilidadYTexto();
            });
            actualizarEtiquetaDiasFiltro();
        }

        // 4. Configurar la misma restricción lógica de fechas en el Paso 3 (Formulario del Contrato)
        if (dpFechaEntrega != null && dpFechaDevolucion != null) {
            dpFechaEntrega.setEditable(false);
            dpFechaDevolucion.setEditable(false);

            LocalDate hoy = LocalDate.now();
            dpFechaEntrega.setValue(hoy);
            dpFechaDevolucion.setValue(hoy.plusDays(1));

            configurarRestriccionCalendarioDevolucion(dpFechaEntrega, dpFechaDevolucion, this::recalcularImporteEstimado);
        }

        // 5. Buscador dinámico en la galería de vehículos de la categoría
        if (txtBuscarVehiculo != null) {
            txtBuscarVehiculo.textProperty().addListener((obs, oldVal, newVal) -> aplicarFiltrosDisponibilidadYTexto());
        }

        cargarFlotaDisponible();
        volverACategorias();
    }

    /**
     * Vincula dos DatePicker (inicio y devolución) de modo que:
     * - Al seleccionar una fecha de inicio, en el segundo calendario (devolución) quedan
     *   deshabilitadas todas las fechas anteriores o iguales a la fecha de inicio.
     * - Solo se puede elegir a partir del día siguiente (fechaInicio.plusDays(1)).
     * - Si la fecha de devolución era anterior o igual a la nueva fecha de inicio,
     *   avanza automáticamente al día siguiente de la fecha de inicio.
     */
    private void configurarRestriccionCalendarioDevolucion(
            DatePicker dpInicio,
            DatePicker dpDevolucion,
            Runnable alCambiarFechas) {

        Callback<DatePicker, DateCell> fabricaCeldasDevolucion = picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    return;
                }
                LocalDate fechaInicio = dpInicio.getValue();
                LocalDate minimaDevolucion = (fechaInicio != null)
                        ? fechaInicio.plusDays(1)
                        : LocalDate.now().plusDays(1);

                if (item.isBefore(minimaDevolucion)) {
                    setDisable(true);
                    setStyle("-fx-background-color: #F3F4F6; -fx-text-fill: #D1D5DB; -fx-opacity: 0.65;");
                }
            }
        };

        dpDevolucion.setDayCellFactory(fabricaCeldasDevolucion);

        dpInicio.valueProperty().addListener((obs, fechaAnterior, nuevaFechaInicio) -> {
            if (nuevaFechaInicio != null) {
                LocalDate diaSiguiente = nuevaFechaInicio.plusDays(1);
                if (dpDevolucion.getValue() == null || !dpDevolucion.getValue().isAfter(nuevaFechaInicio)) {
                    dpDevolucion.setValue(diaSiguiente);
                }
            }
            // Reasignar el dayCellFactory fuerza a JavaFX a refrescar las celdas del segundo calendario
            dpDevolucion.setDayCellFactory(null);
            dpDevolucion.setDayCellFactory(fabricaCeldasDevolucion);

            if (alCambiarFechas != null) {
                alCambiarFechas.run();
            }
        });

        dpDevolucion.valueProperty().addListener((obs, fechaAnterior, nuevaFechaDevolucion) -> {
            if (nuevaFechaDevolucion != null && dpInicio.getValue() != null) {
                LocalDate diaSiguiente = dpInicio.getValue().plusDays(1);
                if (nuevaFechaDevolucion.isBefore(diaSiguiente)) {
                    dpDevolucion.setValue(diaSiguiente);
                    return;
                }
            }
            if (alCambiarFechas != null) {
                alCambiarFechas.run();
            }
        });
    }

    private void actualizarEtiquetaDiasFiltro() {
        if (lblDiasAlquiler == null || dpFiltroInicio == null || dpFiltroDevolucion == null) {
            return;
        }
        LocalDate inicio = dpFiltroInicio.getValue();
        LocalDate devolucion = dpFiltroDevolucion.getValue();
        if (inicio != null && devolucion != null && devolucion.isAfter(inicio)) {
            long dias = ChronoUnit.DAYS.between(inicio, devolucion);
            lblDiasAlquiler.setText(dias == 1 ? "1 día" : dias + " días");
        } else {
            lblDiasAlquiler.setText("1 día");
        }
    }

    /**
     * Carga la imagen representativa de la categoría con fallback a /autos/default.png.
     */
    private void cargarImagenRepresentativa(ImageView imageView, String rutaCategoria) {
        if (imageView == null) {
            return;
        }
        InputStream flujo = getClass().getResourceAsStream(rutaCategoria);
        if (flujo == null) {
            flujo = getClass().getResourceAsStream("/autos/default.png");
        }
        if (flujo != null) {
            imageView.setImage(new Image(flujo));
        }
    }

    /**
     * Configura la animación al pasar el mouse sobre cada botón grande de categoría,
     * resaltando y escalando suavemente la imagen genérica del vehículo.
     */
    private void configurarAnimacionHoverCategoria(Button boton, StackPane contenedorImg, ImageView imageView) {
        if (boton == null || imageView == null) {
            return;
        }

        DropShadow sombraImagen = new DropShadow(20.0, Color.web("#F59E0B", 0.45));
        sombraImagen.setOffsetY(6.0);

        DropShadow sombraBoton = new DropShadow(18.0, Color.web("#111827", 0.12));
        sombraBoton.setOffsetY(6.0);

        boton.setOnMouseEntered(event -> {
            boton.setStyle(ESTILO_BOTON_CATEGORIA_HOVER);
            boton.setEffect(sombraBoton);
            if (contenedorImg != null) {
                contenedorImg.setStyle(ESTILO_CONTENEDOR_IMG_HOVER);
            }
            imageView.setEffect(sombraImagen);

            ScaleTransition escala = new ScaleTransition(Duration.millis(220), imageView);
            escala.setToX(1.16);
            escala.setToY(1.16);
            escala.setInterpolator(Interpolator.EASE_OUT);

            TranslateTransition elevacion = new TranslateTransition(Duration.millis(220), imageView);
            elevacion.setToY(-8.0);
            elevacion.setInterpolator(Interpolator.EASE_OUT);

            ParallelTransition animacion = new ParallelTransition(escala, elevacion);
            animacion.play();
        });

        boton.setOnMouseExited(event -> {
            boton.setStyle(ESTILO_BOTON_CATEGORIA_NORMAL);
            boton.setEffect(null);
            if (contenedorImg != null) {
                contenedorImg.setStyle(ESTILO_CONTENEDOR_IMG_NORMAL);
            }
            imageView.setEffect(null);

            ScaleTransition escala = new ScaleTransition(Duration.millis(200), imageView);
            escala.setToX(1.0);
            escala.setToY(1.0);
            escala.setInterpolator(Interpolator.EASE_BOTH);

            TranslateTransition elevacion = new TranslateTransition(Duration.millis(200), imageView);
            elevacion.setToY(0.0);
            elevacion.setInterpolator(Interpolator.EASE_BOTH);

            ParallelTransition animacion = new ParallelTransition(escala, elevacion);
            animacion.play();
        });
    }

    private void cargarFlotaDisponible() {
        List<Vehiculo> base = new ArrayList<>();
        try {
            List<Vehiculo> desdeBd = vehiculoService.listarTodos();
            if (desdeBd != null && !desdeBd.isEmpty()) {
                base.addAll(desdeBd);
            }
        } catch (Exception ignored) {
            // Si la base de datos no está activa durante la prueba visual, se usan unidades de muestra
        }

        if (base.isEmpty()) {
            base.add(new Vehiculo(1, "AB123CD", "Toyota", "Corolla", 2023, "2023-03-10", 14200, "Disponible", 24000.0, null, "Nafta", 420.0, 5, 45000.0, "Auto", 1));
            base.add(new Vehiculo(2, "AF489KL", "Volkswagen", "Vento", 2024, "2024-01-15", 6800, "Disponible", 27500.0, null, "Nafta", 460.0, 5, 52000.0, "Auto", 1));
            base.add(new Vehiculo(3, "AE902MN", "Peugeot", "208", 2022, "2022-07-20", 29500, "Disponible", 18500.0, null, "Nafta", 310.0, 5, 38000.0, "Auto", 1));

            base.add(new Vehiculo(4, "AD551OP", "Mercedes Benz", "Sprinter", 2023, "2023-05-12", 22400, "Disponible", 48000.0, null, "Diesel", 950.0, 15, 89000.0, "Combi", 1));
            base.add(new Vehiculo(5, "AF774QR", "Ford", "Transit Minibus", 2024, "2024-02-01", 9100, "Disponible", 51000.0, null, "Diesel", 1000.0, 17, 94000.0, "Combi", 1));

            base.add(new Vehiculo(6, "AC310ST", "Renault", "Kangoo Express", 2022, "2022-09-18", 34000, "Disponible", 22000.0, null, "Nafta", 750.0, 2, 56000.0, "Furgoneta", 1));
            base.add(new Vehiculo(7, "AF118UV", "Fiat", "Ducato Cargo", 2023, "2023-11-05", 16300, "Disponible", 41000.0, null, "Diesel", 1450.0, 3, 78000.0, "Furgoneta", 1));
        }

        // Incluir también cualquier vehículo dado de alta en la pestaña 'Vehículos' durante la sesión
        for (Vehiculo registrado : VehiculoController.getVehiculosRegistradosEnSesion()) {
            boolean existe = false;
            for (Vehiculo v : base) {
                if (v.getPat() != null && registrado.getPat() != null
                        && v.getPat().equalsIgnoreCase(registrado.getPat())) {
                    existe = true;
                    break;
                }
            }
            if (!existe) {
                base.add(registrado);
            }
        }

        flotaCompleta = base;
    }

    @FXML
    public void seleccionarCategoriaAuto() {
        abrirGaleriaPorCategoria("Auto");
    }

    @FXML
    public void seleccionarCategoriaCombi() {
        abrirGaleriaPorCategoria("Combi");
    }

    @FXML
    public void seleccionarCategoriaFurgoneta() {
        abrirGaleriaPorCategoria("Furgoneta");
    }

    private void abrirGaleriaPorCategoria(String categoria) {
        this.categoriaSeleccionada = categoria;
        cargarFlotaDisponible();

        vehiculosCategoriaActual = new ArrayList<>();
        for (Vehiculo v : flotaCompleta) {
            if (v.getCat() != null && v.getCat().trim().equalsIgnoreCase(categoria)) {
                vehiculosCategoriaActual.add(v);
            }
        }

        if (lblTituloCategoria != null) {
            lblTituloCategoria.setText("Vehículos Disponibles · " + categoria);
        }
        if (txtBuscarVehiculo != null) {
            txtBuscarVehiculo.clear();
        }

        aplicarFiltrosDisponibilidadYTexto();

        vistaCategorias.setVisible(false);
        vistaCategorias.setManaged(false);
        vistaFormularioContrato.setVisible(false);
        vistaFormularioContrato.setManaged(false);
        vistaSeleccionVehiculo.setVisible(true);
        vistaSeleccionVehiculo.setManaged(true);
    }

    /**
     * Acción del botón 'Comprobar Disponibilidad' en la barra superior de fechas.
     */
    @FXML
    public void comprobarDisponibilidad() {
        if (dpFiltroInicio == null || dpFiltroDevolucion == null
                || dpFiltroInicio.getValue() == null || dpFiltroDevolucion.getValue() == null) {
            mostrarMensaje("Fechas requeridas",
                    "Seleccione la fecha de inicio y la fecha de devolución para comprobar disponibilidad.",
                    Alert.AlertType.WARNING);
            return;
        }
        aplicarFiltrosDisponibilidadYTexto();
    }

    /**
     * Filtra los vehículos de la categoría actual verificando su disponibilidad entre
     * dpFiltroInicio y dpFiltroDevolucion y aplicando el filtro de búsqueda por texto.
     */
    private void aplicarFiltrosDisponibilidadYTexto() {
        LocalDate fechaInicio = dpFiltroInicio != null ? dpFiltroInicio.getValue() : null;
        LocalDate fechaFin = dpFiltroDevolucion != null ? dpFiltroDevolucion.getValue() : null;

        Set<Integer> idsOcupados = obtenerVehiculosOcupadosEnRango(fechaInicio, fechaFin);

        String texto = (txtBuscarVehiculo != null && txtBuscarVehiculo.getText() != null)
                ? txtBuscarVehiculo.getText().trim().toLowerCase()
                : "";

        List<Vehiculo> disponibles = new ArrayList<>();
        for (Vehiculo v : vehiculosCategoriaActual) {
            if (v.getId() != null && idsOcupados.contains(v.getId())) {
                continue;
            }
            if (v.getEst() != null && v.getEst().trim().equalsIgnoreCase("En Mantenimiento")) {
                continue;
            }
            boolean coincideTexto = texto.isEmpty()
                    || (v.getMarca() != null && v.getMarca().toLowerCase().contains(texto))
                    || (v.getMod() != null && v.getMod().toLowerCase().contains(texto))
                    || (v.getPat() != null && v.getPat().toLowerCase().contains(texto));
            if (coincideTexto) {
                disponibles.add(v);
            }
        }

        cargarGrillaVehiculos(disponibles);

        if (lblCantidadCategoria != null) {
            int total = disponibles.size();
            String rangoTexto = (fechaInicio != null && fechaFin != null)
                    ? " (" + fechaInicio + " al " + fechaFin + ")"
                    : "";
            lblCantidadCategoria.setText(
                    (total == 1 ? "1 unidad disponible" : total + " unidades disponibles") + rangoTexto
            );
        }
    }

    private Set<Integer> obtenerVehiculosOcupadosEnRango(LocalDate fechaInicio, LocalDate fechaFin) {
        Set<Integer> ocupados = new HashSet<>();
        if (fechaInicio == null || fechaFin == null) {
            return ocupados;
        }
        String inicioStr = fechaInicio.toString();
        String finStr = fechaFin.toString();

        try {
            ocupados.addAll(contratoDAO.obtenerIdsVehiculosOcupadosEntreFechas(inicioStr, finStr));
        } catch (Exception ignored) {
            // Si la base de datos no está conectada, se verifica contra los contratos en memoria
        }

        for (Contrato c : contratosEnMemoria) {
            if (c.getIdAsigna() != null && c.getFeHrEnt() != null && c.getFeHrDev() != null) {
                if (c.getFeHrEnt().compareTo(finStr) <= 0 && c.getFeHrDev().compareTo(inicioStr) >= 0) {
                    ocupados.add(c.getIdAsigna());
                }
            }
        }
        return ocupados;
    }

    /**
     * Reutiliza la lógica de galería (TilePane + tarjetas VBox con imagen genérica)
     * para mostrar los vehículos de la categoría elegida.
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
            VBox tarjeta = crearTarjetaVehiculoContrato(vehiculo);
            contenedorVehiculos.getChildren().add(tarjeta);
        }
    }

    /**
     * Construye dinámicamente la tarjeta del vehículo reutilizando la estructura de VehiculoController
     * y agregando la acción de selección para el contrato.
     */
    private VBox crearTarjetaVehiculoContrato(Vehiculo vehiculo) {
        VBox tarjeta = new VBox(10.0);
        tarjeta.setPrefWidth(250.0);
        tarjeta.setAlignment(Pos.TOP_LEFT);
        tarjeta.setPadding(new Insets(16.0));
        tarjeta.setStyle(
                "-fx-background-color: #FFFFFF; "
                + "-fx-background-radius: 14; "
                + "-fx-border-color: #E5E7EB; "
                + "-fx-border-radius: 14; "
                + "-fx-border-width: 1;"
        );

        InputStream flujo = obtenerFlujoImagenVehiculo(vehiculo);

        VBox contenedorImagen = new VBox();
        contenedorImagen.setAlignment(Pos.CENTER);
        contenedorImagen.setPrefHeight(135.0);
        contenedorImagen.setStyle("-fx-background-color: #F9FAFB; -fx-background-radius: 10; -fx-padding: 8;");

        ImageView imageView = new ImageView();
        imageView.setFitWidth(210.0);
        imageView.setFitHeight(120.0);
        imageView.setPreserveRatio(true);
        if (flujo != null) {
            imageView.setImage(new Image(flujo));
        }
        contenedorImagen.getChildren().add(imageView);

        Label lblMarca = new Label(vehiculo.getMarca() != null ? vehiculo.getMarca() : "Sin marca");
        lblMarca.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #111827;");

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
                ? String.format("$%.0f/día", vehiculo.getTarBase())
                : "";
        Label lblTarifa = new Label(textoTarifa);
        lblTarifa.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #D97706;");

        filaInferior.getChildren().addAll(lblPatente, espaciador, lblTarifa);

        Button btnElegir = new Button("Seleccionar Vehículo");
        btnElegir.setMaxWidth(Double.MAX_VALUE);
        btnElegir.setStyle(
                "-fx-background-color: #111827; -fx-text-fill: #FFFFFF; -fx-font-size: 12px; "
                + "-fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 8 12 8 12; -fx-cursor: hand;"
        );
        btnElegir.setOnAction(e -> pasarAFormularioContrato(vehiculo));

        tarjeta.getChildren().addAll(contenedorImagen, lblMarca, lblModelo, filaInferior, btnElegir);
        return tarjeta;
    }

    /**
     * Aplica la misma lógica de imagen genérica (/autos/<marca>_<modelo>.png) con respaldo a la imagen
     * de su categoría o /autos/default.png.
     */
    private InputStream obtenerFlujoImagenVehiculo(Vehiculo vehiculo) {
        return VehiculoController.obtenerFlujoImagenPorTipo(vehiculo);
    }

    private void pasarAFormularioContrato(Vehiculo vehiculo) {
        this.vehiculoSeleccionado = vehiculo;

        // Transferir las fechas elegidas en la barra de disponibilidad al formulario del contrato
        if (dpFiltroInicio != null && dpFiltroInicio.getValue() != null && dpFechaEntrega != null) {
            dpFechaEntrega.setValue(dpFiltroInicio.getValue());
        }
        if (dpFiltroDevolucion != null && dpFiltroDevolucion.getValue() != null && dpFechaDevolucion != null) {
            dpFechaDevolucion.setValue(dpFiltroDevolucion.getValue());
        }

        InputStream flujo = obtenerFlujoImagenVehiculo(vehiculo);
        if (flujo != null && imgVehiculoSeleccionado != null) {
            imgVehiculoSeleccionado.setImage(new Image(flujo));
        }

        if (lblCardMarcaModelo != null) {
            lblCardMarcaModelo.setText(vehiculo.getMarca() + " " + vehiculo.getMod());
        }
        if (lblCardPatente != null) {
            lblCardPatente.setText("Patente: " + vehiculo.getPat());
        }
        if (lblCardCategoria != null) {
            lblCardCategoria.setText("Categoría: " + vehiculo.getCat() + " · " + vehiculo.getPax() + " pax");
        }
        if (lblCardTarifa != null && vehiculo.getTarBase() != null) {
            lblCardTarifa.setText(String.format("$%.2f / día", vehiculo.getTarBase()));
        }
        if (lblResumenVehiculoElegido != null) {
            lblResumenVehiculoElegido.setText("Unidad seleccionada: " + vehiculo.getMarca() + " " + vehiculo.getMod() + " (" + vehiculo.getPat() + ")");
        }
        if (txtKmSalida != null && vehiculo.getKmj() != null) {
            txtKmSalida.setText(String.valueOf(vehiculo.getKmj()));
        }

        recalcularImporteEstimado();

        vistaCategorias.setVisible(false);
        vistaCategorias.setManaged(false);
        vistaSeleccionVehiculo.setVisible(false);
        vistaSeleccionVehiculo.setManaged(false);
        vistaFormularioContrato.setVisible(true);
        vistaFormularioContrato.setManaged(true);
    }

    /**
     * Obtiene el padrón completo de clientes combinando la base de datos y los registrados en sesión.
     */
    private List<Cliente> obtenerTodosLosClientesDisponibles() {
        List<Cliente> resultado = new ArrayList<>();
        try {
            List<Cliente> desdeBd = clienteDAO.listarTodos();
            if (desdeBd != null) {
                resultado.addAll(desdeBd);
            }
        } catch (Exception ignored) {
            // Si la base de datos no está conectada, se utiliza el padrón en memoria
        }

        for (Cliente enMemoria : ClienteController.getClientesEnMemoria()) {
            boolean yaExiste = false;
            for (Cliente c : resultado) {
                if (c.getDni() != null && c.getDni().equals(enMemoria.getDni())) {
                    yaExiste = true;
                    break;
                }
            }
            if (!yaExiste) {
                resultado.add(enMemoria);
            }
        }
        return resultado;
    }

    /**
     * Filtra clientes buscando coincidencias por cualquier atributo (DNI, Nombre, Apellido, Teléfono, Correo o Dirección).
     */
    private List<Cliente> filtrarClientesPorCualquierAtributo(List<Cliente> base, String criterioTexto) {
        String filtro = criterioTexto == null ? "" : criterioTexto.trim().toLowerCase();
        if (filtro.isEmpty()) {
            return new ArrayList<>(base);
        }

        List<Cliente> coincidencias = new ArrayList<>();
        for (Cliente c : base) {
            String dniStr = c.getDni() != null ? String.valueOf(c.getDni()) : "";
            String nom = c.getNom() != null ? c.getNom().toLowerCase() : "";
            String apl = c.getApl() != null ? c.getApl().toLowerCase() : "";
            String nombreCompleto = (nom + " " + apl).trim();
            String telStr = c.getTel() != null ? String.valueOf(c.getTel()) : "";
            String correo = c.getCorreo() != null ? c.getCorreo().toLowerCase() : "";
            String dir = c.getDir() != null ? c.getDir().toLowerCase() : "";

            if (dniStr.contains(filtro)
                    || nom.contains(filtro)
                    || apl.contains(filtro)
                    || nombreCompleto.contains(filtro)
                    || telStr.contains(filtro)
                    || correo.contains(filtro)
                    || dir.contains(filtro)) {
                coincidencias.add(c);
            }
        }
        return coincidencias;
    }

    /**
     * Abre una pantalla emergente (Stage modal) con los resultados de búsqueda del cliente
     * mostrando las columnas: Nombre, Apellido, DNI y Teléfono.
     * Si no se encuentra por ningún atributo, permite habilitar directamente la carga de un Nuevo Cliente.
     */
    @FXML
    public void abrirVentanaBusquedaCliente() {
        String textoInicial = (txtBuscarClienteContrato != null && txtBuscarClienteContrato.getText() != null)
                ? txtBuscarClienteContrato.getText().trim()
                : "";

        List<Cliente> todosLosClientes = obtenerTodosLosClientesDisponibles();

        Stage ventanaEmergente = new Stage();
        ventanaEmergente.initModality(Modality.APPLICATION_MODAL);
        if (txtBuscarClienteContrato != null && txtBuscarClienteContrato.getScene() != null) {
            ventanaEmergente.initOwner(txtBuscarClienteContrato.getScene().getWindow());
        }
        ventanaEmergente.setTitle("Seleccionar Cliente · Resultados de Búsqueda");

        VBox contenedorPrincipal = new VBox(14.0);
        contenedorPrincipal.setPadding(new Insets(20.0));
        contenedorPrincipal.setStyle("-fx-background-color: #F3F4F6; -fx-font-family: 'Segoe UI', 'Inter', sans-serif;");

        // Encabezado y buscador interactivo dentro de la ventana emergente
        VBox tarjetaSuperior = new VBox(10.0);
        tarjetaSuperior.setStyle(
                "-fx-background-color: #FFFFFF; -fx-background-radius: 14; "
                + "-fx-border-color: #E5E7EB; -fx-border-radius: 14; -fx-padding: 16;"
        );

        Label lblTituloPopup = new Label("Resultados de Búsqueda de Clientes");
        lblTituloPopup.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #111827;");

        Label lblSubtituloPopup = new Label("Haga doble clic sobre un cliente o presione 'Seleccionar Cliente'. Si no existe, pulse '+ Cargar Nuevo Cliente'.");
        lblSubtituloPopup.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7280;");

        TextField txtFiltroPopup = new TextField(textoInicial);
        txtFiltroPopup.setPromptText("Filtrar por Nombre, Apellido, DNI, Teléfono, Correo o Dirección...");
        txtFiltroPopup.setStyle(
                "-fx-background-color: #F3F4F6; -fx-background-radius: 8; "
                + "-fx-border-color: transparent; -fx-padding: 9 12 9 12; -fx-font-size: 13px;"
        );

        tarjetaSuperior.getChildren().addAll(lblTituloPopup, lblSubtituloPopup, txtFiltroPopup);

        // Tabla con las 4 columnas solicitadas: Nombre, Apellido, DNI y Teléfono
        TableView<Cliente> tablaResultados = new TableView<>();
        tablaResultados.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tablaResultados.setPrefHeight(260.0);
        tablaResultados.setStyle("-fx-background-radius: 10; -fx-border-color: #E5E7EB; -fx-border-radius: 10;");

        TableColumn<Cliente, String> colNom = new TableColumn<>("Nombre");
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));

        TableColumn<Cliente, String> colApl = new TableColumn<>("Apellido");
        colApl.setCellValueFactory(new PropertyValueFactory<>("apl"));

        TableColumn<Cliente, Integer> colDni = new TableColumn<>("DNI");
        colDni.setCellValueFactory(new PropertyValueFactory<>("dni"));

        TableColumn<Cliente, Long> colTel = new TableColumn<>("Teléfono");
        colTel.setCellValueFactory(new PropertyValueFactory<>("tel"));

        tablaResultados.getColumns().add(colNom);
        tablaResultados.getColumns().add(colApl);
        tablaResultados.getColumns().add(colDni);
        tablaResultados.getColumns().add(colTel);

        // Placeholder limpio solo con el mensaje informativo (sin botón adentro de la tabla)
        Label lblSinCoincidencias = new Label("No se encontró ningún cliente con ese criterio.");
        lblSinCoincidencias.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 13px; -fx-font-weight: bold;");
        tablaResultados.setPlaceholder(lblSinCoincidencias);

        ObservableList<Cliente> itemsPopup = FXCollections.observableArrayList(
                filtrarClientesPorCualquierAtributo(todosLosClientes, textoInicial)
        );
        tablaResultados.setItems(itemsPopup);
        if (!itemsPopup.isEmpty()) {
            tablaResultados.getSelectionModel().selectFirst();
        }

        // Bandera para abrir la ventana flotante de Nuevo Cliente DESPUÉS de que cierre completamente esta ventana modal,
        // evitando el bloqueo de eventos por doble showAndWait() anidado en JavaFX.
        final boolean[] abrirNuevoClienteAlCerrar = {false};

        // Barra inferior de botones de la ventana emergente
        HBox barraBotones = new HBox(10.0);
        barraBotones.setAlignment(Pos.CENTER_RIGHT);

        // Único botón '+ Cargar nuevo cliente' siempre visible en la esquina inferior izquierda
        Button btnCargarNuevo = new Button("+ Cargar nuevo cliente");
        btnCargarNuevo.setStyle(
                "-fx-background-color: #FEF3C7; -fx-text-fill: #D97706; -fx-border-color: #F59E0B; "
                + "-fx-border-radius: 8; -fx-background-radius: 8; -fx-font-size: 12px; -fx-font-weight: bold; "
                + "-fx-padding: 9 14 9 14; -fx-cursor: hand;"
        );
        btnCargarNuevo.setOnAction(e -> {
            if (txtBuscarClienteContrato != null) {
                txtBuscarClienteContrato.setText(txtFiltroPopup.getText());
            }
            abrirNuevoClienteAlCerrar[0] = true;
            ventanaEmergente.close();
        });

        Region espaciador = new Region();
        HBox.setHgrow(espaciador, Priority.ALWAYS);

        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setStyle(
                "-fx-background-color: #E5E7EB; -fx-text-fill: #374151; -fx-font-size: 12px; "
                + "-fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 9 16 9 16; -fx-cursor: hand;"
        );
        btnCancelar.setOnAction(e -> ventanaEmergente.close());

        Button btnSeleccionar = new Button("Seleccionar Cliente");
        btnSeleccionar.setStyle(
                "-fx-background-color: #262626; -fx-text-fill: #FFFFFF; -fx-font-size: 12px; "
                + "-fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 9 18 9 18; -fx-cursor: hand;"
        );
        btnSeleccionar.setOnAction(e -> {
            Cliente elegido = tablaResultados.getSelectionModel().getSelectedItem();
            if (elegido == null) {
                mostrarMensaje("Seleccione un cliente",
                        "Haga clic sobre un cliente de la lista o pulse '+ Cargar nuevo cliente'.",
                        Alert.AlertType.WARNING);
                return;
            }
            asignarClienteSeleccionado(elegido);
            ventanaEmergente.close();
        });

        btnSeleccionar.setDisable(itemsPopup.isEmpty());

        txtFiltroPopup.textProperty().addListener((obs, ant, nuevoTexto) -> {
            List<Cliente> filtrados = filtrarClientesPorCualquierAtributo(todosLosClientes, nuevoTexto);
            tablaResultados.setItems(FXCollections.observableArrayList(filtrados));
            boolean sinResultados = filtrados.isEmpty();

            btnSeleccionar.setDisable(sinResultados);
            if (!sinResultados) {
                tablaResultados.getSelectionModel().selectFirst();
            }
        });

        // Doble clic sobre una fila selecciona automáticamente al cliente y cierra la ventana emergente
        tablaResultados.setRowFactory(tv -> {
            TableRow<Cliente> fila = new TableRow<>();
            fila.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !fila.isEmpty()) {
                    asignarClienteSeleccionado(fila.getItem());
                    ventanaEmergente.close();
                }
            });
            return fila;
        });

        barraBotones.getChildren().addAll(btnCargarNuevo, espaciador, btnCancelar, btnSeleccionar);
        contenedorPrincipal.getChildren().addAll(tarjetaSuperior, tablaResultados, barraBotones);

        Scene escenaPopup = new Scene(contenedorPrincipal, 620.0, 440.0);
        ventanaEmergente.setScene(escenaPopup);
        ventanaEmergente.showAndWait();

        // Una vez que la ventana de búsqueda terminó su ciclo modal por completo, se abre la ventana de Nuevo Cliente
        if (abrirNuevoClienteAlCerrar[0]) {
            Platform.runLater(this::abrirVentanaNuevoClienteContrato);
        }
    }

    /**
     * Abre la ventana flotante auxiliar reutilizable (NuevoClienteDialogView.fxml) en modo diferido:
     * el cliente NO se guarda en la base de datos hasta que se confirme la transacción del contrato.
     */
    @FXML
    public void abrirVentanaNuevoClienteContrato() {
        javafx.stage.Window owner = (txtBuscarClienteContrato != null && txtBuscarClienteContrato.getScene() != null)
                ? txtBuscarClienteContrato.getScene().getWindow()
                : null;
        String textoBuscado = (txtBuscarClienteContrato != null && txtBuscarClienteContrato.getText() != null)
                ? txtBuscarClienteContrato.getText().trim()
                : "";

        Cliente nuevoCliente = NuevoClienteDialogController.mostrarVentanaNuevoClienteDiferidoParaContrato(owner, textoBuscado);
        if (nuevoCliente != null) {
            asignarClienteAlContrato(nuevoCliente, true);
        }
    }

    /**
     * Asigna un cliente existente seleccionado desde la ventana emergente de búsqueda.
     */
    private void asignarClienteSeleccionado(Cliente cliente) {
        asignarClienteAlContrato(cliente, false);
    }

    /**
     * Asigna el cliente al contrato indicando si es un cliente nuevo pendiente de persistir
     * junto con el contrato en la transacción JDBC o si ya existía en la base de datos.
     */
    private void asignarClienteAlContrato(Cliente cliente, boolean esNuevoPendienteDeTransaccion) {
        if (cliente == null) {
            return;
        }
        this.clienteElegidoExistente = cliente;
        this.clienteNuevoPendienteDeTransaccion = esNuevoPendienteDeTransaccion;

        if (lblClienteResumenNombre != null) {
            lblClienteResumenNombre.setText(cliente.getNom() + " " + cliente.getApl() + "  ·  DNI " + cliente.getDni());
            lblClienteResumenNombre.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #111827;");
        }
        if (lblClienteResumenDatos != null) {
            lblClienteResumenDatos.setText(
                    "Tel: " + (cliente.getTel() != null ? cliente.getTel() : "-")
                    + "   |   Correo: " + (cliente.getCorreo() != null ? cliente.getCorreo() : "-")
                    + "   |   Dirección: " + (cliente.getDir() != null ? cliente.getDir() : "-")
            );
        }
        if (tarjetaClienteSeleccionado != null) {
            tarjetaClienteSeleccionado.setStyle(
                    "-fx-background-color: #ECFDF5; -fx-background-radius: 12; "
                    + "-fx-border-color: #10B981; -fx-border-radius: 12; -fx-padding: 12 16 12 16;"
            );
        }
        if (lblEstadoClienteContrato != null) {
            String etiqueta = esNuevoPendienteDeTransaccion
                    ? "Nuevo cliente listo (se guardará al confirmar contrato)"
                    : ("Cliente asignado: " + cliente.getNom() + " " + cliente.getApl());
            lblEstadoClienteContrato.setText(etiqueta);
            lblEstadoClienteContrato.setStyle(
                    "-fx-background-color: #D1FAE5; -fx-text-fill: #065F46; "
                    + "-fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 3 10 3 10; -fx-background-radius: 10;"
            );
        }
    }

    private void recalcularImporteEstimado() {
        if (vehiculoSeleccionado == null || vehiculoSeleccionado.getTarBase() == null || txtImpAlq == null) {
            return;
        }
        LocalDate entrega = dpFechaEntrega != null ? dpFechaEntrega.getValue() : null;
        LocalDate devolucion = dpFechaDevolucion != null ? dpFechaDevolucion.getValue() : null;
        if (entrega != null && devolucion != null) {
            long dias = Math.max(1, ChronoUnit.DAYS.between(entrega, devolucion));
            double total = dias * vehiculoSeleccionado.getTarBase();
            txtImpAlq.setText(String.format("%.2f", total).replace(",", "."));
        }
    }

    private void limpiarSeleccionCliente() {
        this.clienteElegidoExistente = null;
        this.clienteNuevoPendienteDeTransaccion = false;
        if (txtBuscarClienteContrato != null) {
            txtBuscarClienteContrato.clear();
        }
        if (lblClienteResumenNombre != null) {
            lblClienteResumenNombre.setText("Ningún cliente seleccionado");
            lblClienteResumenNombre.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #374151;");
        }
        if (lblClienteResumenDatos != null) {
            lblClienteResumenDatos.setText("Busque un cliente por DNI, nombre, apellido o teléfono para asignarlo al contrato");
        }
        if (tarjetaClienteSeleccionado != null) {
            tarjetaClienteSeleccionado.setStyle(
                    "-fx-background-color: #F9FAFB; -fx-background-radius: 12; "
                    + "-fx-border-color: #E5E7EB; -fx-border-radius: 12; -fx-padding: 12 16 12 16;"
            );
        }
        if (lblEstadoClienteContrato != null) {
            lblEstadoClienteContrato.setText("Pendiente de selección");
            lblEstadoClienteContrato.setStyle(
                    "-fx-background-color: #FEF3C7; -fx-text-fill: #D97706; "
                    + "-fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 3 10 3 10; -fx-background-radius: 10;"
            );
        }
    }

    @FXML
    public void volverACategorias() {
        limpiarSeleccionCliente();
        if (vistaCategorias != null) {
            vistaSeleccionVehiculo.setVisible(false);
            vistaSeleccionVehiculo.setManaged(false);
            vistaFormularioContrato.setVisible(false);
            vistaFormularioContrato.setManaged(false);
            vistaCategorias.setVisible(true);
            vistaCategorias.setManaged(true);
        }
    }

    @FXML
    public void volverAGaleriaCategoria() {
        limpiarSeleccionCliente();
        if (vistaSeleccionVehiculo != null) {
            vistaCategorias.setVisible(false);
            vistaCategorias.setManaged(false);
            vistaFormularioContrato.setVisible(false);
            vistaFormularioContrato.setManaged(false);
            vistaSeleccionVehiculo.setVisible(true);
            vistaSeleccionVehiculo.setManaged(true);
            aplicarFiltrosDisponibilidadYTexto();
        }
    }

    @FXML
    public void confirmarNuevoContrato() {
        if (vehiculoSeleccionado == null) {
            mostrarMensaje("Vehículo requerido", "Seleccione primero un vehículo de la galería.", Alert.AlertType.WARNING);
            return;
        }

        if (clienteElegidoExistente == null) {
            mostrarMensaje("Cliente requerido",
                    "Busque y seleccione un cliente existente con 'Buscar Cliente', o pulse '+ Nuevo Cliente' para cargarlo en la ventana flotante.",
                    Alert.AlertType.WARNING);
            return;
        }

        try {
            String feEnt = dpFechaEntrega.getValue() != null ? dpFechaEntrega.getValue().toString() : "";
            String feDev = dpFechaDevolucion.getValue() != null ? dpFechaDevolucion.getValue().toString() : "";
            double impAlq = Double.parseDouble(txtImpAlq.getText().trim());

            int kmSalida = Integer.parseInt(txtKmSalida.getText().trim());
            double combSalida = Double.parseDouble(txtCombSalida.getText().trim());
            String danios = txtDaniosSalida.getText().trim();

            if (feEnt.isEmpty() || feDev.isEmpty()) {
                mostrarMensaje("Fechas incompletas", "Seleccione las fechas de entrega y devolución.", Alert.AlertType.WARNING);
                return;
            }

            // Constructores sin ID: MySQL genera los IDs con AUTO_INCREMENT y los DAOs los asignan mediante setters
            Contrato contrato = new Contrato(
                    feEnt, feDev, "Estándar", "Activo",
                    0.0, impAlq, 0.0, vehiculoSeleccionado.getId(), clienteElegidoExistente.getId()
            );
            CheckInOut checkInOut = new CheckInOut(
                    feEnt, kmSalida, combSalida, danios,
                    null, null, null, null, 1, null
            );

            if (clienteNuevoPendienteDeTransaccion) {
                // Transacción estricta JDBC: inserta Cliente y Contrato en la misma conexión con commit/rollback
                contratoService.registrarAlquilerNuevoCliente(clienteElegidoExistente, contrato);
                // Solo si la transacción del contrato tuvo éxito, se agrega el nuevo cliente al padrón en memoria
                ClienteController.registrarClienteEnMemoria(clienteElegidoExistente);
                this.clienteNuevoPendienteDeTransaccion = false;
            } else {
                contratoService.altaContrato(contrato, clienteElegidoExistente, vehiculoSeleccionado, checkInOut);
            }

            contratosEnMemoria.add(contrato);

            String nroGenerado = contrato.getNro() != null ? String.valueOf(contrato.getNro()) : "(generado)";
            mostrarMensaje("Contrato Generado",
                    "El contrato N° " + nroGenerado + " a nombre de "
                    + clienteElegidoExistente.getNom() + " " + clienteElegidoExistente.getApl()
                    + " para el vehículo " + vehiculoSeleccionado.getMarca() + " " + vehiculoSeleccionado.getMod()
                    + " se registró exitosamente.",
                    Alert.AlertType.INFORMATION);
            volverACategorias();

        } catch (NumberFormatException e) {
            mostrarMensaje("Error de Formato",
                    "Verifique que el Importe y el Kilometraje de salida sean valores numéricos válidos.",
                    Alert.AlertType.WARNING);
        } catch (Exception e) {
            mostrarMensaje("Aviso al registrar en BD",
                    "No se pudo persistir el contrato en la base de datos: " + e.getMessage(),
                    Alert.AlertType.ERROR);
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
