package controlador;

import dao.SucursalDAO;
import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import modelo.Sucursal;
import modelo.Vehiculo;
import servicio.SucursalService;

public class SucursalController {

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

    private static final List<Sucursal> SUCURSALES_EN_MEMORIA = new ArrayList<>();

    static {
        SUCURSALES_EN_MEMORIA.add(new Sucursal(1, "San Luis - Centro", "Av. Illia 450, San Luis", 2664500123L, 5));
        SUCURSALES_EN_MEMORIA.add(new Sucursal(2, "Villa Mercedes - Terminal", "Av. 25 de Mayo 1200, Villa Mercedes", 2657421122L, 2));
    }

    @FXML private ScrollPane vistaTarjetas;
    @FXML private VBox vistaDetalle;
    @FXML private VBox vistaAlta;

    @FXML private TilePane contenedorSucursales;
    @FXML private Label lblSubtituloSucursales;
    @FXML private Button btnNuevaSucursal;

    // Labels de la vista de detalle de sucursal
    @FXML private Label lblDetalleCodigo;
    @FXML private Label lblDetalleNombre;
    @FXML private Label lblDetalleFlota;
    @FXML private Label lblDetalleDireccion;
    @FXML private Label lblDetalleTelefono;

    // Campos del formulario de Nueva Sucursal (S_cod es AUTO_INCREMENT)
    @FXML private ComboBox<String> comboCiudad;
    @FXML private TextField txtNom;
    @FXML private TextField txtDir;
    @FXML private TextField txtTel;

    private final SucursalDAO sucursalDAO;
    private final SucursalService sucursalService;
    private Sucursal sucursalSeleccionada;

    public SucursalController() {
        this.sucursalDAO = new SucursalDAO();
        this.sucursalService = new SucursalService(this.sucursalDAO);
    }

    @FXML
    public void initialize() {
        if (lblDetalleFlota != null) {
            String estiloNormal = "-fx-background-color: #FEF3C7; -fx-text-fill: #D97706; -fx-font-size: 14px; "
                    + "-fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 8 16 8 16; "
                    + "-fx-cursor: hand; -fx-border-color: #FDE68A; -fx-border-radius: 10;";
            String estiloHover = "-fx-background-color: #FDE68A; -fx-text-fill: #B45309; -fx-font-size: 14px; "
                    + "-fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 8 16 8 16; "
                    + "-fx-cursor: hand; -fx-border-color: #F59E0B; -fx-border-radius: 10;";
            lblDetalleFlota.setStyle(estiloNormal);
            lblDetalleFlota.setOnMouseEntered(e -> lblDetalleFlota.setStyle(estiloHover));
            lblDetalleFlota.setOnMouseExited(e -> lblDetalleFlota.setStyle(estiloNormal));
        }

        // Configuración del desplegable de Ciudades
        if (comboCiudad != null) {
            comboCiudad.getItems().setAll(
                    "San Luis",
                    "Villa Mercedes",
                    "Merlo",
                    "La Punta",
                    "Juana Koslay",
                    "Otra"
            );
            comboCiudad.getSelectionModel().select("San Luis");
            comboCiudad.getSelectionModel().selectedItemProperty().addListener((obs, viejo, nuevo) -> {
                actualizarSugerenciaTelefono(nuevo);
            });
        }
        actualizarSugerenciaTelefono("San Luis");

        // Formateador para txtTel: solo dígitos y máximo 10 caracteres
        if (txtTel != null) {
            txtTel.setTextFormatter(new TextFormatter<String>(change -> {
                String nuevoTexto = change.getControlNewText();
                if (nuevoTexto.matches("\\d{0,10}")) {
                    return change;
                }
                return null;
            }));
        }

        mostrarVistaTarjetas();
    }

    private void actualizarSugerenciaTelefono(String ciudad) {
        if (txtTel == null) return;
        if (ciudad == null) {
            txtTel.setPromptText("Ej. 2664123456");
            return;
        }
        switch (ciudad) {
            case "San Luis":
            case "La Punta":
            case "Juana Koslay":
                txtTel.setPromptText("Ej. 2664123456 o 2665123456");
                break;
            case "Villa Mercedes":
                txtTel.setPromptText("Ej. 2657421234");
                break;
            case "Merlo":
                txtTel.setPromptText("Ej. 2656471234");
                break;
            default:
                txtTel.setPromptText("Ej. Código de área + número");
                break;
        }
    }

    /**
     * Limpia el TilePane (contenedorSucursales) y genera dinámicamente una tarjeta (VBox)
     * por cada sucursal de la lista con Nombre, Dirección y 'Flota: X vehículos'.
     */
    public void cargarTarjetasSucursales(List<Sucursal> lista) {
        if (contenedorSucursales == null) {
            return;
        }
        contenedorSucursales.getChildren().clear();

        if (lista == null) {
            return;
        }

        for (Sucursal sucursal : lista) {
            VBox tarjetaVBox = crearTarjetaSucursal(sucursal);
            contenedorSucursales.getChildren().add(tarjetaVBox);
        }

        if (lblSubtituloSucursales != null) {
            int total = lista.size();
            lblSubtituloSucursales.setText(
                    (total == 1 ? "1 sucursal registrada" : total + " sucursales registradas")
                    + " · Haga clic en una tarjeta para ver su detalle"
            );
        }
    }

    /**
     * Construye dinámicamente la tarjeta visual (VBox) de una sucursal.
     */
    private VBox crearTarjetaSucursal(Sucursal sucursal) {
        VBox tarjetaVBox = new VBox(12.0);
        tarjetaVBox.setPrefWidth(260.0);
        tarjetaVBox.setAlignment(Pos.TOP_LEFT);
        tarjetaVBox.setPadding(new Insets(20.0));
        tarjetaVBox.setStyle(ESTILO_TARJETA_NORMAL);

        // 1. Label: Nombre de la sucursal (en negrita y tamaño mayor)
        Label lblNombre = new Label(sucursal.getNom() != null ? sucursal.getNom() : "Sucursal sin nombre");
        lblNombre.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #111827;");
        lblNombre.setWrapText(true);

        // 2. Label: Dirección
        Label lblDireccion = new Label(sucursal.getDir() != null ? sucursal.getDir() : "Sin dirección");
        lblDireccion.setStyle("-fx-font-size: 13px; -fx-text-fill: #6B7280;");
        lblDireccion.setWrapText(true);

        // 3. Label: Texto destacado 'Flota: X vehículos' (informativo en la tarjeta; toda la tarjeta abre el detalle)
        Label lblFlota = new Label("Flota: " + sucursal.getCantidadVehiculos() + " vehículos");
        lblFlota.setStyle(
                "-fx-background-color: #FEF3C7; "
                + "-fx-text-fill: #D97706; "
                + "-fx-font-size: 13px; "
                + "-fx-font-weight: bold; "
                + "-fx-background-radius: 8; "
                + "-fx-padding: 6 12 6 12;"
        );

        tarjetaVBox.getChildren().addAll(lblNombre, lblDireccion, lblFlota);

        // Efecto visual al pasar el mouse y evento de clic para abrir el detalle de la sucursal
        tarjetaVBox.setOnMouseEntered(e -> tarjetaVBox.setStyle(ESTILO_TARJETA_HOVER));
        tarjetaVBox.setOnMouseExited(e -> tarjetaVBox.setStyle(ESTILO_TARJETA_NORMAL));
        tarjetaVBox.setOnMouseClicked(event -> abrirDetalleSucursal(sucursal));

        return tarjetaVBox;
    }

    /**
     * Abre el detalle de la sucursal seleccionada al hacer clic sobre su tarjeta.
     */
    public void abrirDetalleSucursal(Sucursal sucursal) {
        if (sucursal == null) {
            return;
        }
        this.sucursalSeleccionada = sucursal;

        if (lblDetalleCodigo != null) {
            lblDetalleCodigo.setText("CÓDIGO DE SUCURSAL #" + (sucursal.getCod() != null ? sucursal.getCod() : "-"));
        }
        if (lblDetalleNombre != null) {
            lblDetalleNombre.setText(sucursal.getNom() != null ? sucursal.getNom() : "-");
        }
        if (lblDetalleFlota != null) {
            lblDetalleFlota.setText("Flota: " + sucursal.getCantidadVehiculos() + " vehículos");
        }
        if (lblDetalleDireccion != null) {
            lblDetalleDireccion.setText(sucursal.getDir() != null ? sucursal.getDir() : "-");
        }
        if (lblDetalleTelefono != null) {
            lblDetalleTelefono.setText(sucursal.getTel() != null ? String.valueOf(sucursal.getTel()) : "-");
        }

        if (vistaTarjetas != null && vistaDetalle != null && vistaAlta != null) {
            vistaTarjetas.setVisible(false);
            vistaTarjetas.setManaged(false);
            vistaAlta.setVisible(false);
            vistaAlta.setManaged(false);
            vistaDetalle.setVisible(true);
            vistaDetalle.setManaged(true);
        }
    }

    @FXML
    public void verVehiculosSucursalSeleccionada() {
        if (sucursalSeleccionada != null) {
            HomeController.mostrarVehiculosDeSucursal(sucursalSeleccionada);
        }
    }

    public void recargarSucursales() {
        List<Sucursal> lista;
        try {
            lista = sucursalDAO.obtenerResumenSucursales();
        } catch (Exception ignored) {
            // Si la base de datos no está activa aún, utiliza las sucursales en memoria actualizando su flota
            lista = new ArrayList<>();
            for (Sucursal s : SUCURSALES_EN_MEMORIA) {
                int extraEnSesion = 0;
                for (Vehiculo v : VehiculoController.getVehiculosRegistradosEnSesion()) {
                    if (v.getCodPertenece() != null && v.getCodPertenece().equals(s.getCod())) {
                        extraEnSesion++;
                    }
                }
                Sucursal copia = new Sucursal(
                        s.getCod(), s.getNom(), s.getDir(), s.getTel(),
                        s.getCantidadVehiculos() + extraEnSesion
                );
                lista.add(copia);
            }
        }
        cargarTarjetasSucursales(lista);
    }

    @FXML
    public void mostrarVistaTarjetas() {
        if (vistaTarjetas != null && vistaDetalle != null && vistaAlta != null) {
            vistaDetalle.setVisible(false);
            vistaDetalle.setManaged(false);
            vistaAlta.setVisible(false);
            vistaAlta.setManaged(false);
            vistaTarjetas.setVisible(true);
            vistaTarjetas.setManaged(true);
        }
        if (btnNuevaSucursal != null) {
            btnNuevaSucursal.setVisible(true);
            btnNuevaSucursal.setManaged(true);
        }
        recargarSucursales();
    }

    @FXML
    public void mostrarVistaAlta() {
        if (vistaTarjetas != null && vistaDetalle != null && vistaAlta != null) {
            vistaTarjetas.setVisible(false);
            vistaTarjetas.setManaged(false);
            vistaDetalle.setVisible(false);
            vistaDetalle.setManaged(false);
            vistaAlta.setVisible(true);
            vistaAlta.setManaged(true);
        }
        if (btnNuevaSucursal != null) {
            btnNuevaSucursal.setVisible(false);
            btnNuevaSucursal.setManaged(false);
        }
    }

    @FXML
    public void guardarSucursal() {
        try {
            String ciudad = comboCiudad != null && comboCiudad.getValue() != null
                    ? comboCiudad.getValue().trim()
                    : "";
            String nom = txtNom.getText() != null ? txtNom.getText().trim() : "";
            String dir = txtDir.getText() != null ? txtDir.getText().trim() : "";
            String telStr = txtTel.getText() != null ? txtTel.getText().trim() : "";

            if (ciudad.isEmpty() || nom.isEmpty() || dir.isEmpty() || telStr.isEmpty()) {
                mostrarAlerta("Campos obligatorios",
                        "Por favor seleccione la Ciudad y complete Nombre, Dirección y Teléfono de la sucursal.",
                        Alert.AlertType.WARNING);
                return;
            }

            // Exige exactamente 10 dígitos (código de área + número local)
            if (telStr.length() != 10) {
                mostrarAlerta("Teléfono Inválido",
                        "El teléfono debe contener exactamente 10 dígitos (código de área sin 0 + número sin 15, ej. 2664123456).",
                        Alert.AlertType.WARNING);
                return;
            }

            // Validación de característica según la ciudad seleccionada
            if ("San Luis".equals(ciudad) || "La Punta".equals(ciudad) || "Juana Koslay".equals(ciudad)) {
                if (!telStr.startsWith("266")) {
                    mostrarAlerta("Característica Telefónica",
                            "Para " + ciudad + ", el teléfono debe comenzar con la característica 266 (ej. 2664XXXXXX o 2665XXXXXX).",
                            Alert.AlertType.WARNING);
                    return;
                }
            } else if ("Villa Mercedes".equals(ciudad)) {
                if (!telStr.startsWith("2657")) {
                    mostrarAlerta("Característica Telefónica",
                            "Para Villa Mercedes, el teléfono debe comenzar con la característica 2657 (ej. 2657XXXXXX).",
                            Alert.AlertType.WARNING);
                    return;
                }
            } else if ("Merlo".equals(ciudad)) {
                if (!telStr.startsWith("2656")) {
                    mostrarAlerta("Característica Telefónica",
                            "Para Merlo, el teléfono debe comenzar con la característica 2656 (ej. 2656XXXXXX).",
                            Alert.AlertType.WARNING);
                    return;
                }
            }

            long tel = Long.parseLong(telStr);

            // Formateo del Nombre para respetar VARCHAR(30)
            String nombreFinal;
            if ("Otra".equalsIgnoreCase(ciudad) || nom.toLowerCase().contains(ciudad.toLowerCase())) {
                nombreFinal = nom;
            } else {
                nombreFinal = ciudad + " - " + nom;
            }
            if (nombreFinal.length() > 30) {
                nombreFinal = nombreFinal.substring(0, 30);
            }

            // Formateo de la Dirección para respetar VARCHAR(50)
            String direccionFinal;
            if ("Otra".equalsIgnoreCase(ciudad) || dir.toLowerCase().contains(ciudad.toLowerCase())) {
                direccionFinal = dir;
            } else {
                direccionFinal = dir + ", " + ciudad;
            }
            if (direccionFinal.length() > 50) {
                direccionFinal = direccionFinal.substring(0, 50);
            }

            // Usa el constructor sin ID: MySQL genera S_cod con AUTO_INCREMENT y el DAO lo asigna con setCod()
            Sucursal nueva = new Sucursal(nombreFinal, direccionFinal, tel);
            nueva.setCantidadVehiculos(0);

            try {
                sucursalService.altaSucursal(nueva);
                SUCURSALES_EN_MEMORIA.add(nueva);

                if (txtNom != null) txtNom.clear();
                if (txtDir != null) txtDir.clear();
                if (txtTel != null) txtTel.clear();
                if (comboCiudad != null) comboCiudad.getSelectionModel().select("San Luis");

                mostrarAlerta("Sucursal Registrada",
                        "La sucursal '" + nombreFinal + "' se guardó correctamente.",
                        Alert.AlertType.INFORMATION);
                mostrarVistaTarjetas();

            } catch (Exception e) {
                mostrarAlerta("Error al Registrar",
                        "No se pudo guardar la sucursal en la base de datos: " + e.getMessage(),
                        Alert.AlertType.ERROR);
            }

        } catch (NumberFormatException e) {
            mostrarAlerta("Error de Formato",
                    "Verifique que el Teléfono sea un valor numérico válido.",
                    Alert.AlertType.WARNING);
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
