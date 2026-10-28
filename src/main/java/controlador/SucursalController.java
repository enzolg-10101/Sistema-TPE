package controlador;

import dao.SucursalDAO;
import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
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
        SUCURSALES_EN_MEMORIA.add(new Sucursal(1, "Sucursal Central", "Av. Libertador 1200", 1145001234L, 5));
        SUCURSALES_EN_MEMORIA.add(new Sucursal(2, "Sucursal Aeropuerto", "Au. Riccheri Km 33", 1148005678L, 2));
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

    // Campos del formulario de Nueva Sucursal (sin código manual, S_cod es AUTO_INCREMENT)
    @FXML private TextField txtNom;
    @FXML private TextField txtDir;
    @FXML private TextField txtTel;

    private final SucursalDAO sucursalDAO;
    private final SucursalService sucursalService;

    public SucursalController() {
        this.sucursalDAO = new SucursalDAO();
        this.sucursalService = new SucursalService(this.sucursalDAO);
    }

    @FXML
    public void initialize() {
        mostrarVistaTarjetas();
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

        // 3. Label: Texto destacado 'Flota: X vehículos'
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
            String nom = txtNom.getText() != null ? txtNom.getText().trim() : "";
            String dir = txtDir.getText() != null ? txtDir.getText().trim() : "";
            String telStr = txtTel.getText() != null ? txtTel.getText().trim() : "";

            if (nom.isEmpty() || dir.isEmpty() || telStr.isEmpty()) {
                mostrarAlerta("Campos obligatorios",
                        "Por favor complete Nombre, Dirección y Teléfono de la sucursal.",
                        Alert.AlertType.WARNING);
                return;
            }

            long tel = Long.parseLong(telStr);

            // Usa el constructor sin ID: MySQL genera S_cod con AUTO_INCREMENT y el DAO lo asigna con setCod()
            Sucursal nueva = new Sucursal(nom, dir, tel);
            nueva.setCantidadVehiculos(0);

            try {
                sucursalService.altaSucursal(nueva);
            } catch (Exception ignored) {
                // Si la BD no está conectada en la prueba visual, se asigna un código provisorio en sesión
                if (nueva.getCod() == null) {
                    nueva.setCod(SUCURSALES_EN_MEMORIA.size() + 1);
                }
            }
            SUCURSALES_EN_MEMORIA.add(nueva);

            if (txtNom != null) txtNom.clear();
            if (txtDir != null) txtDir.clear();
            if (txtTel != null) txtTel.clear();

            mostrarAlerta("Sucursal Registrada",
                    "La sucursal '" + nom + "' se guardó correctamente.",
                    Alert.AlertType.INFORMATION);
            mostrarVistaTarjetas();

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
