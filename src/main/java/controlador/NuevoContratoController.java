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
import java.util.List;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
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

    // Paso 2: Galería de vehículos filtrada por categoría (reutiliza estructura de VehiculoView)
    @FXML private Label lblTituloCategoria;
    @FXML private Label lblCantidadCategoria;
    @FXML private TextField txtBuscarVehiculo;
    @FXML private TilePane contenedorVehiculos;

    // Paso 3: Resumen del vehículo seleccionado y formulario del contrato
    @FXML private Label lblResumenVehiculoElegido;
    @FXML private ImageView imgVehiculoSeleccionado;
    @FXML private Label lblCardMarcaModelo;
    @FXML private Label lblCardPatente;
    @FXML private Label lblCardCategoria;
    @FXML private Label lblCardTarifa;

    @FXML private TextField txtNroContrato;
    @FXML private DatePicker dpFechaEntrega;
    @FXML private DatePicker dpFechaDevolucion;
    @FXML private TextField txtImpAlq;

    @FXML private TextField txtIdCliente;
    @FXML private TextField txtDniCliente;
    @FXML private TextField txtNomCliente;
    @FXML private TextField txtAplCliente;

    @FXML private TextField txtIdCheck;
    @FXML private TextField txtKmSalida;
    @FXML private TextField txtCombSalida;
    @FXML private TextField txtDaniosSalida;

    private final VehiculoService vehiculoService;
    private final ContratoService contratoService;

    private List<Vehiculo> flotaCompleta = new ArrayList<>();
    private List<Vehiculo> vehiculosCategoriaActual = new ArrayList<>();
    private String categoriaSeleccionada = "Auto";
    private Vehiculo vehiculoSeleccionado;

    public NuevoContratoController() {
        VehiculoDAO vehiculoDAO = new VehiculoDAO();
        SucursalDAO sucursalDAO = new SucursalDAO();
        ClienteDAO clienteDAO = new ClienteDAO();
        CheckInOutDAO checkInOutDAO = new CheckInOutDAO();

        this.vehiculoService = new VehiculoService(vehiculoDAO, sucursalDAO);
        this.contratoService = new ContratoService(
                new ContratoDAO(),
                clienteDAO,
                vehiculoDAO,
                checkInOutDAO,
                new FirmaDAO(),
                new AsignaDAO(),
                new CorrespondeDAO(),
                new ClienteService(clienteDAO),
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

        // 3. Configurar DatePickers solo por calendario (sin ingreso manual)
        if (dpFechaEntrega != null) {
            dpFechaEntrega.setEditable(false);
            dpFechaEntrega.setValue(LocalDate.now());
            dpFechaEntrega.valueProperty().addListener((obs, oldV, newV) -> recalcularImporteEstimado());
        }
        if (dpFechaDevolucion != null) {
            dpFechaDevolucion.setEditable(false);
            dpFechaDevolucion.setValue(LocalDate.now().plusDays(3));
            dpFechaDevolucion.valueProperty().addListener((obs, oldV, newV) -> recalcularImporteEstimado());
        }

        // 4. Buscador dinámico en la galería de vehículos de la categoría
        if (txtBuscarVehiculo != null) {
            txtBuscarVehiculo.textProperty().addListener((obs, oldVal, newVal) -> filtrarVehiculosCategoria(newVal));
        }

        cargarFlotaDisponible();
        volverACategorias();
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
        try {
            List<Vehiculo> desdeBd = vehiculoService.listarTodos();
            if (desdeBd != null && !desdeBd.isEmpty()) {
                flotaCompleta = desdeBd;
                return;
            }
        } catch (Exception ignored) {
            // Si la base de datos no está activa durante la prueba visual, se usan unidades de muestra
        }

        flotaCompleta = new ArrayList<>();
        flotaCompleta.add(new Vehiculo(1, "AB123CD", "Toyota", "Corolla", 2023, "2023-03-10", 14200, "Disponible", 24000.0, null, "Nafta", 420.0, 5, 45000.0, "Auto", 1));
        flotaCompleta.add(new Vehiculo(2, "AF489KL", "Volkswagen", "Vento", 2024, "2024-01-15", 6800, "Disponible", 27500.0, null, "Nafta", 460.0, 5, 52000.0, "Auto", 1));
        flotaCompleta.add(new Vehiculo(3, "AE902MN", "Peugeot", "208", 2022, "2022-07-20", 29500, "Disponible", 18500.0, null, "Nafta", 310.0, 5, 38000.0, "Auto", 1));

        flotaCompleta.add(new Vehiculo(4, "AD551OP", "Mercedes Benz", "Sprinter", 2023, "2023-05-12", 22400, "Disponible", 48000.0, null, "Diesel", 950.0, 15, 89000.0, "Combi", 1));
        flotaCompleta.add(new Vehiculo(5, "AF774QR", "Ford", "Transit Minibus", 2024, "2024-02-01", 9100, "Disponible", 51000.0, null, "Diesel", 1000.0, 17, 94000.0, "Combi", 1));

        flotaCompleta.add(new Vehiculo(6, "AC310ST", "Renault", "Kangoo Express", 2022, "2022-09-18", 34000, "Disponible", 22000.0, null, "Nafta", 750.0, 2, 56000.0, "Furgoneta", 1));
        flotaCompleta.add(new Vehiculo(7, "AF118UV", "Fiat", "Ducato Cargo", 2023, "2023-11-05", 16300, "Disponible", 41000.0, null, "Diesel", 1450.0, 3, 78000.0, "Furgoneta", 1));
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

        cargarGrillaVehiculos(vehiculosCategoriaActual);

        vistaCategorias.setVisible(false);
        vistaCategorias.setManaged(false);
        vistaFormularioContrato.setVisible(false);
        vistaFormularioContrato.setManaged(false);
        vistaSeleccionVehiculo.setVisible(true);
        vistaSeleccionVehiculo.setManaged(true);
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

        if (lblCantidadCategoria != null) {
            int total = lista.size();
            lblCantidadCategoria.setText(total == 1
                    ? "1 unidad disponible para alquilar"
                    : total + " unidades disponibles para alquilar");
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
        String marcaFormateada = vehiculo.getMarca() != null
                ? vehiculo.getMarca().trim().replace(" ", "_")
                : "";
        String modeloFormateado = vehiculo.getMod() != null
                ? vehiculo.getMod().trim().replace(" ", "_")
                : "";

        String rutaEspecifica = "/autos/" + marcaFormateada + "_" + modeloFormateado + ".png";
        InputStream flujo = getClass().getResourceAsStream(rutaEspecifica);
        if (flujo == null && vehiculo.getCat() != null) {
            String catLower = vehiculo.getCat().trim().toLowerCase();
            if (catLower.equals("auto")) {
                flujo = getClass().getResourceAsStream("/autos/cat_auto.png");
            } else if (catLower.equals("combi")) {
                flujo = getClass().getResourceAsStream("/autos/cat_combi.png");
            } else if (catLower.equals("furgoneta")) {
                flujo = getClass().getResourceAsStream("/autos/cat_furgoneta.png");
            }
        }
        if (flujo == null) {
            flujo = getClass().getResourceAsStream("/autos/default.png");
        }
        return flujo;
    }

    private void filtrarVehiculosCategoria(String texto) {
        String filtro = texto == null ? "" : texto.trim().toLowerCase();
        if (filtro.isEmpty()) {
            cargarGrillaVehiculos(vehiculosCategoriaActual);
            return;
        }
        List<Vehiculo> filtrados = new ArrayList<>();
        for (Vehiculo v : vehiculosCategoriaActual) {
            boolean coincide = (v.getMarca() != null && v.getMarca().toLowerCase().contains(filtro))
                    || (v.getMod() != null && v.getMod().toLowerCase().contains(filtro))
                    || (v.getPat() != null && v.getPat().toLowerCase().contains(filtro));
            if (coincide) {
                filtrados.add(v);
            }
        }
        cargarGrillaVehiculos(filtrados);
    }

    private void pasarAFormularioContrato(Vehiculo vehiculo) {
        this.vehiculoSeleccionado = vehiculo;

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

    @FXML
    public void volverACategorias() {
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
        if (vistaSeleccionVehiculo != null) {
            vistaCategorias.setVisible(false);
            vistaCategorias.setManaged(false);
            vistaFormularioContrato.setVisible(false);
            vistaFormularioContrato.setManaged(false);
            vistaSeleccionVehiculo.setVisible(true);
            vistaSeleccionVehiculo.setManaged(true);
        }
    }

    @FXML
    public void confirmarNuevoContrato() {
        if (vehiculoSeleccionado == null) {
            mostrarMensaje("Vehículo requerido", "Seleccione primero un vehículo de la galería.", Alert.AlertType.WARNING);
            return;
        }

        try {
            int nroContrato = Integer.parseInt(txtNroContrato.getText().trim());
            String feEnt = dpFechaEntrega.getValue() != null ? dpFechaEntrega.getValue().toString() : "";
            String feDev = dpFechaDevolucion.getValue() != null ? dpFechaDevolucion.getValue().toString() : "";
            double impAlq = Double.parseDouble(txtImpAlq.getText().trim());

            int idCliente = Integer.parseInt(txtIdCliente.getText().trim());
            int dniCliente = Integer.parseInt(txtDniCliente.getText().trim());
            String nomCliente = txtNomCliente.getText().trim();
            String aplCliente = txtAplCliente.getText().trim();

            int idCheck = Integer.parseInt(txtIdCheck.getText().trim());
            int kmSalida = Integer.parseInt(txtKmSalida.getText().trim());
            double combSalida = Double.parseDouble(txtCombSalida.getText().trim());
            String danios = txtDaniosSalida.getText().trim();

            if (feEnt.isEmpty() || feDev.isEmpty() || nomCliente.isEmpty() || aplCliente.isEmpty()) {
                mostrarMensaje("Datos incompletos", "Complete las fechas y los datos del cliente.", Alert.AlertType.WARNING);
                return;
            }

            Contrato contrato = new Contrato(
                    nroContrato, feEnt, feDev, "Estándar", "Activo",
                    0.0, impAlq, 0.0, vehiculoSeleccionado.getId(), idCliente
            );
            Cliente cliente = new Cliente(
                    idCliente, dniCliente, nomCliente, aplCliente,
                    "Sin especificar", 0L, "cliente@email.com", LocalDate.now().toString()
            );
            CheckInOut checkInOut = new CheckInOut(
                    idCheck, feEnt, kmSalida, combSalida, danios,
                    null, null, null, null, 1, nroContrato
            );

            contratoService.altaContrato(contrato, cliente, vehiculoSeleccionado, checkInOut);
            mostrarMensaje("Contrato Generado",
                    "El contrato N° " + nroContrato + " para el vehículo "
                    + vehiculoSeleccionado.getMarca() + " " + vehiculoSeleccionado.getMod()
                    + " se registró exitosamente.",
                    Alert.AlertType.INFORMATION);
            volverACategorias();

        } catch (NumberFormatException e) {
            mostrarMensaje("Error de Formato",
                    "Verifique que Nro. Contrato, Importe, ID/DNI del Cliente, ID CheckInOut y Kilometraje sean numéricos.",
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
