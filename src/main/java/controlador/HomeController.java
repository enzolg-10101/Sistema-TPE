package controlador;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;
import modelo.Sucursal;

public class HomeController {

    private static HomeController instancia;

    // --- Estilos para pestañas en estado EXPANDIDO ---
    private static final String ESTILO_BOTON_INACTIVO_EXPANDIDO =
            "-fx-background-color: transparent; -fx-text-fill: #4B5563; -fx-font-size: 14px; "
            + "-fx-font-weight: 600; -fx-alignment: CENTER_LEFT; -fx-padding: 10 12 10 12; "
            + "-fx-background-radius: 10; -fx-cursor: hand;";

    private static final String ESTILO_BOTON_ACTIVO_EXPANDIDO =
            "-fx-background-color: #FEF3C7; -fx-text-fill: #D97706; -fx-font-size: 14px; "
            + "-fx-font-weight: bold; -fx-alignment: CENTER_LEFT; -fx-padding: 10 12 10 12; "
            + "-fx-background-radius: 10; -fx-cursor: hand; -fx-border-color: #FDE68A; -fx-border-radius: 10;";

    // --- Estilos para pestañas en estado COMPACTO / COLAPSADO ---
    private static final String ESTILO_BOTON_INACTIVO_COLAPSADO =
            "-fx-background-color: transparent; -fx-text-fill: #4B5563; -fx-font-size: 14px; "
            + "-fx-font-weight: 600; -fx-alignment: CENTER; -fx-padding: 10 6 10 6; "
            + "-fx-background-radius: 10; -fx-cursor: hand;";

    private static final String ESTILO_BOTON_ACTIVO_COLAPSADO =
            "-fx-background-color: #FEF3C7; -fx-text-fill: #D97706; -fx-font-size: 14px; "
            + "-fx-font-weight: bold; -fx-alignment: CENTER; -fx-padding: 10 6 10 6; "
            + "-fx-background-radius: 10; -fx-cursor: hand; -fx-border-color: #FDE68A; -fx-border-radius: 10;";

    // --- Estilos para botón NUEVO en estado EXPANDIDO ---
    private static final String ESTILO_NUEVO_INACTIVO_EXPANDIDO =
            "-fx-background-color: #111827; -fx-background-radius: 12; -fx-padding: 8 10 8 10; "
            + "-fx-cursor: hand; -fx-alignment: CENTER_LEFT;";

    private static final String ESTILO_NUEVO_ACTIVO_EXPANDIDO =
            "-fx-background-color: #1F2937; -fx-border-color: #F59E0B; -fx-border-width: 1.5; "
            + "-fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 8 10 8 10; "
            + "-fx-cursor: hand; -fx-alignment: CENTER_LEFT;";

    // --- Estilos para botón NUEVO en estado COMPACTO / COLAPSADO ---
    private static final String ESTILO_NUEVO_INACTIVO_COLAPSADO =
            "-fx-background-color: #111827; -fx-background-radius: 12; -fx-padding: 8 6 8 6; "
            + "-fx-cursor: hand; -fx-alignment: CENTER;";

    private static final String ESTILO_NUEVO_ACTIVO_COLAPSADO =
            "-fx-background-color: #1F2937; -fx-border-color: #F59E0B; -fx-border-width: 1.5; "
            + "-fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 8 6 8 6; "
            + "-fx-cursor: hand; -fx-alignment: CENTER;";

    // Colores temáticos para iconos vectoriales
    private static final Color COLOR_ICONO_INACTIVO = Color.web("#6B7280");
    private static final Color COLOR_ICONO_ACTIVO = Color.web("#D97706");

    @FXML private StackPane panelCentral;
    @FXML private VBox vistaBienvenida;
    @FXML private ImageView imgBienvenida;

    // Referencia al nodo de bienvenida para poder regresar a él
    private Node nodoBienvenida;

    // Componentes del Menú Lateral
    @FXML private VBox menuLateral;
    @FXML private HBox boxFilaSuperior;
    @FXML private Button btnToggleMenu;
    @FXML private SVGPath iconoToggleMenu;
    @FXML private Tooltip tooltipToggle;
    @FXML private HBox boxMarca;
    @FXML private Separator sepEncabezado;

    // Botón NUEVO y su contenido
    @FXML private Button btnNuevo;
    @FXML private HBox boxContenidoNuevo;
    @FXML private VBox boxTextoNuevo;

    // Título de sección
    @FXML private Label lblMenuPrincipal;

    // Pestañas individuales (Botón, HBox contenedor, Label de texto e Icono SVG)
    @FXML private Button btnVehiculos;
    @FXML private HBox boxVehiculos;
    @FXML private Label lblVehiculos;
    @FXML private SVGPath iconVehiculos;

    @FXML private Button btnContratos;
    @FXML private HBox boxContratos;
    @FXML private Label lblContratos;
    @FXML private SVGPath iconContratos;

    @FXML private Button btnClientes;
    @FXML private HBox boxClientes;
    @FXML private Label lblClientes;
    @FXML private SVGPath iconClientes;

    @FXML private Button btnEmpleados;
    @FXML private HBox boxEmpleados;
    @FXML private Label lblEmpleados;
    @FXML private SVGPath iconEmpleados;

    @FXML private Button btnSucursales;
    @FXML private HBox boxSucursales;
    @FXML private Label lblSucursales;
    @FXML private SVGPath iconSucursales;

    // Pie de menú lateral (expandido y compacto)
    @FXML private VBox boxFooterCompleto;
    @FXML private VBox boxFooterCompacto;

    // Estado del menú lateral
    private boolean menuColapsado = false;
    private Button botonActivoActual;

    @FXML
    public void initialize() {
        instancia = this;
        configurarEfectosHover();
        aplicarEstadoMenuLateral(false, false);

        if (boxMarca != null) {
            Tooltip.install(boxMarca, new Tooltip("Ir al inicio"));
        }

        if (vistaBienvenida != null) {
            this.nodoBienvenida = vistaBienvenida;
        }

        configurarImagenBienvenida();

        // El sistema inicia con la pantalla de bienvenida (sin pestaña seleccionada)
        activarBoton(null);
    }

    private void configurarImagenBienvenida() {
        if (imgBienvenida == null) {
            return;
        }

        // Si la imagen no cargó por FXML, cargarla programáticamente como respaldo
        if (imgBienvenida.getImage() == null) {
            try {
                URL imgUrl = getClass().getResource("/bienvenida.jpg");
                if (imgUrl == null) {
                    imgUrl = getClass().getResource("/vista/bienvenida.jpg");
                }
                if (imgUrl != null) {
                    imgBienvenida.setImage(new Image(imgUrl.toExternalForm()));
                }
            } catch (Exception ignored) {
            }
        }

        imgBienvenida.setPreserveRatio(true);
        imgBienvenida.setSmooth(true);

        // Bordes redondeados elegantes que coinciden con la tarjeta
        Rectangle clip = new Rectangle();
        clip.setArcWidth(24.0);
        clip.setArcHeight(24.0);
        imgBienvenida.layoutBoundsProperty().addListener((obs, oldB, newB) -> {
            if (newB != null) {
                clip.setWidth(newB.getWidth());
                clip.setHeight(newB.getHeight());
            }
        });
        imgBienvenida.setClip(clip);

        if (panelCentral != null) {
            panelCentral.widthProperty().addListener((obs, oldW, newW) -> ajustarTamanioImagen());
            panelCentral.heightProperty().addListener((obs, oldH, newH) -> ajustarTamanioImagen());
        }

        ajustarTamanioImagen();
    }

    private void ajustarTamanioImagen() {
        if (imgBienvenida == null || panelCentral == null) return;
        double w = panelCentral.getWidth() - 64.0;
        double h = panelCentral.getHeight() - 64.0;
        if (w > 120.0) {
            imgBienvenida.setFitWidth(Math.min(w, 980.0));
        }
        if (h > 120.0) {
            imgBienvenida.setFitHeight(Math.min(h, 560.0));
        }
    }

    /**
     * Muestra la pantalla inicial de bienvenida cuando el usuario hace clic en el logo.
     */
    @FXML
    public void mostrarPantallaBienvenida() {
        activarBoton(null);
        if (nodoBienvenida != null && panelCentral != null) {
            panelCentral.getChildren().setAll(nodoBienvenida);
            ajustarTamanioImagen();
        }
    }

    /**
     * Alterna entre el menú lateral expandido (225px) y compactado en iconos (68px).
     * Al esconderlo, la ventana central (panelCentral) se expande automáticamente
     * para visualizar mejor sus elementos y tablas.
     */
    @FXML
    public void toggleMenuLateral() {
        menuColapsado = !menuColapsado;
        aplicarEstadoMenuLateral(menuColapsado, true);
    }

    private void aplicarEstadoMenuLateral(boolean colapsado, boolean animar) {
        double anchoDestino = colapsado ? 68.0 : 225.0;

        // Ocultar o mostrar los textos y elementos que solo se ven en modo expandido
        if (boxMarca != null) {
            boxMarca.setVisible(!colapsado);
            boxMarca.setManaged(!colapsado);
        }
        if (lblMenuPrincipal != null) {
            lblMenuPrincipal.setVisible(!colapsado);
            lblMenuPrincipal.setManaged(!colapsado);
        }
        if (boxTextoNuevo != null) {
            boxTextoNuevo.setVisible(!colapsado);
            boxTextoNuevo.setManaged(!colapsado);
        }
        if (lblVehiculos != null) {
            lblVehiculos.setVisible(!colapsado);
            lblVehiculos.setManaged(!colapsado);
        }
        if (lblContratos != null) {
            lblContratos.setVisible(!colapsado);
            lblContratos.setManaged(!colapsado);
        }
        if (lblClientes != null) {
            lblClientes.setVisible(!colapsado);
            lblClientes.setManaged(!colapsado);
        }
        if (lblEmpleados != null) {
            lblEmpleados.setVisible(!colapsado);
            lblEmpleados.setManaged(!colapsado);
        }
        if (lblSucursales != null) {
            lblSucursales.setVisible(!colapsado);
            lblSucursales.setManaged(!colapsado);
        }
        if (boxFooterCompleto != null) {
            boxFooterCompleto.setVisible(!colapsado);
            boxFooterCompleto.setManaged(!colapsado);
        }
        if (boxFooterCompacto != null) {
            boxFooterCompacto.setVisible(colapsado);
            boxFooterCompacto.setManaged(colapsado);
        }

        // Ajustar alineación de las filas contenedoras de cada botón (centradas al colapsar, a la izquierda al expandir)
        Pos alineacion = colapsado ? Pos.CENTER : Pos.CENTER_LEFT;
        if (boxFilaSuperior != null) {
            boxFilaSuperior.setAlignment(colapsado ? Pos.CENTER : Pos.CENTER_LEFT);
        }
        if (boxContenidoNuevo != null) {
            boxContenidoNuevo.setAlignment(alineacion);
        }
        if (boxVehiculos != null) {
            boxVehiculos.setAlignment(alineacion);
        }
        if (boxContratos != null) {
            boxContratos.setAlignment(alineacion);
        }
        if (boxClientes != null) {
            boxClientes.setAlignment(alineacion);
        }
        if (boxEmpleados != null) {
            boxEmpleados.setAlignment(alineacion);
        }
        if (boxSucursales != null) {
            boxSucursales.setAlignment(alineacion);
        }

        // Ajustar padding y ancho del contenedor del menú lateral
        if (menuLateral != null) {
            menuLateral.setAlignment(colapsado ? Pos.TOP_CENTER : Pos.TOP_LEFT);
            menuLateral.setPadding(colapsado
                    ? new Insets(20.0, 8.0, 20.0, 8.0)
                    : new Insets(20.0, 16.0, 20.0, 16.0));

            if (animar) {
                Timeline timeline = new Timeline(
                        new KeyFrame(Duration.millis(160),
                                new KeyValue(menuLateral.prefWidthProperty(), anchoDestino, Interpolator.EASE_BOTH),
                                new KeyValue(menuLateral.minWidthProperty(), anchoDestino, Interpolator.EASE_BOTH),
                                new KeyValue(menuLateral.maxWidthProperty(), anchoDestino, Interpolator.EASE_BOTH)
                        )
                );
                timeline.play();
            } else {
                menuLateral.setPrefWidth(anchoDestino);
                menuLateral.setMinWidth(anchoDestino);
                menuLateral.setMaxWidth(anchoDestino);
            }
        }

        // Tooltip del botón de esconder / mostrar
        if (tooltipToggle != null) {
            tooltipToggle.setText(colapsado ? "Mostrar menú lateral" : "Ocultar menú lateral");
        }

        actualizarEstilosBotones();
    }

    private void configurarEfectosHover() {
        Button[] botones = {btnVehiculos, btnContratos, btnClientes, btnEmpleados, btnSucursales};
        for (Button btn : botones) {
            if (btn != null) {
                btn.setOnMouseEntered(e -> {
                    if (btn != botonActivoActual) {
                        btn.setStyle("-fx-background-color: #F3F4F6; -fx-text-fill: #111827; "
                                + "-fx-font-size: 14px; -fx-font-weight: 600; "
                                + "-fx-alignment: " + (menuColapsado ? "CENTER" : "CENTER_LEFT") + "; "
                                + "-fx-padding: " + (menuColapsado ? "10 6 10 6" : "10 12 10 12") + "; "
                                + "-fx-background-radius: 10; -fx-cursor: hand;");
                    }
                });
                btn.setOnMouseExited(e -> {
                    if (btn != botonActivoActual) {
                        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #4B5563; "
                                + "-fx-font-size: 14px; -fx-font-weight: 600; "
                                + "-fx-alignment: " + (menuColapsado ? "CENTER" : "CENTER_LEFT") + "; "
                                + "-fx-padding: " + (menuColapsado ? "10 6 10 6" : "10 12 10 12") + "; "
                                + "-fx-background-radius: 10; -fx-cursor: hand;");
                    }
                });
            }
        }
    }

    public static void mostrarVehiculosDeSucursal(Sucursal sucursal) {
        VehiculoController.setSucursalFiltroPendiente(sucursal);
        if (instancia != null) {
            instancia.activarBoton(instancia.btnVehiculos);
            instancia.cargarVista("/VehiculoView.fxml");
        }
    }

    public static void volverASucursales() {
        VehiculoController.setSucursalFiltroPendiente(null);
        if (instancia != null) {
            instancia.activarBoton(instancia.btnSucursales);
            instancia.cargarVista("/SucursalView.fxml");
        }
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
        VehiculoController.setSucursalFiltroPendiente(null);
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
     * Resalta visualmente el botón de la sección activa en el menú lateral,
     * adaptando su estilo y el color del icono según el estado expandido o colapsado.
     */
    private void activarBoton(Button botonActivo) {
        this.botonActivoActual = botonActivo;
        actualizarEstilosBotones();
    }

    private void actualizarEstilosBotones() {
        if (btnNuevo != null) {
            boolean esActivo = (botonActivoActual == btnNuevo);
            if (menuColapsado) {
                btnNuevo.setStyle(esActivo ? ESTILO_NUEVO_ACTIVO_COLAPSADO : ESTILO_NUEVO_INACTIVO_COLAPSADO);
            } else {
                btnNuevo.setStyle(esActivo ? ESTILO_NUEVO_ACTIVO_EXPANDIDO : ESTILO_NUEVO_INACTIVO_EXPANDIDO);
            }
        }

        Button[] botones = {btnVehiculos, btnContratos, btnClientes, btnEmpleados, btnSucursales};
        SVGPath[] iconos = {iconVehiculos, iconContratos, iconClientes, iconEmpleados, iconSucursales};

        for (int i = 0; i < botones.length; i++) {
            Button btn = botones[i];
            SVGPath icon = (i < iconos.length) ? iconos[i] : null;

            if (btn != null) {
                boolean esActivo = (btn == botonActivoActual);
                if (menuColapsado) {
                    btn.setStyle(esActivo ? ESTILO_BOTON_ACTIVO_COLAPSADO : ESTILO_BOTON_INACTIVO_COLAPSADO);
                } else {
                    btn.setStyle(esActivo ? ESTILO_BOTON_ACTIVO_EXPANDIDO : ESTILO_BOTON_INACTIVO_EXPANDIDO);
                }

                if (icon != null) {
                    icon.setFill(esActivo ? COLOR_ICONO_ACTIVO : COLOR_ICONO_INACTIVO);
                }
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
