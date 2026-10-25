package controlador;

import dao.SucursalDAO;
import dao.VehiculoDAO;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import modelo.Sucursal;
import modelo.Vehiculo;
import servicio.VehiculoService;

public class VehiculoController {

    // Contenedores para alternar entre la Galería y el formulario de Nuevo Vehículo
    @FXML private VBox vistaGaleria;
    @FXML private VBox vistaAlta;

    // Galería y buscador
    @FXML private TilePane contenedorVehiculos;
    @FXML private TextField txtBuscarVehiculo;
    @FXML private Label lblCantidadVehiculos;

    // Campos del formulario de Nuevo Vehículo
    @FXML private TextField txtId;
    @FXML private TextField txtPat;
    @FXML private TextField txtMarca;
    @FXML private TextField txtMod;
    @FXML private TextField txtAFabr;
    @FXML private DatePicker dpFAdq;
    @FXML private TextField txtKmj;
    @FXML private TextField txtEst;
    @FXML private TextField txtPreAdq;
    @FXML private DatePicker dpFBaja;
    @FXML private TextField txtTiComb;
    @FXML private TextField txtCarga;
    @FXML private TextField txtPax;
    @FXML private TextField txtTarBase;
    @FXML private ComboBox<String> cmbCat;
    @FXML private ComboBox<Sucursal> cmbSucursal;

    private VehiculoService vehiculoService;
    private final SucursalDAO sucursalDAO = new SucursalDAO();
    private List<Vehiculo> listaCompleta = new ArrayList<>();

    public VehiculoController() {
        this.vehiculoService = new VehiculoService(new VehiculoDAO(), sucursalDAO);
    }

    public VehiculoController(VehiculoService vehiculoService) {
        this.vehiculoService = vehiculoService;
    }

    @FXML
    public void initialize() {
        if (cmbCat != null) {
            cmbCat.getItems().setAll("Auto", "Furgoneta", "Combi");
            cmbCat.getSelectionModel().selectFirst();
        }

        if (dpFAdq != null) {
            dpFAdq.setEditable(false);
        }
        if (dpFBaja != null) {
            dpFBaja.setEditable(false);
        }

        if (cmbSucursal != null) {
            cmbSucursal.setConverter(new StringConverter<Sucursal>() {
                @Override
                public String toString(Sucursal sucursal) {
                    return sucursal == null ? "" : sucursal.getCod() + " - " + sucursal.getNom();
                }

                @Override
                public Sucursal fromString(String string) {
                    return null;
                }
            });
            cargarSucursales();
        }

        if (txtBuscarVehiculo != null) {
            txtBuscarVehiculo.textProperty().addListener((obs, oldVal, newVal) -> filtrarGaleria(newVal));
        }

        recargarVehiculos();
        mostrarVistaGaleria();
    }

    private void cargarSucursales() {
        if (cmbSucursal == null) {
            return;
        }
        try {
            List<Sucursal> sucursales = sucursalDAO.listarTodas();
            cmbSucursal.getItems().setAll(sucursales);
        } catch (Exception e) {
            // Si la base de datos aún no está conectada, mantiene la lista vacía sin bloquear la UI
        }
    }

    /**
     * Limpia el TilePane y construye dinámicamente una tarjeta (VBox) por cada vehículo de la lista.
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
            lblCantidadVehiculos.setText(total == 1 ? "1 vehículo en flota" : total + " vehículos en flota");
        }
    }

    /**
     * Construye dinámicamente la tarjeta visual (VBox) de un vehículo siguiendo la estética sobria.
     */
    private VBox crearTarjetaVehiculo(Vehiculo vehiculo) {
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

        // Lógica estricta para la carga de la imagen del vehículo
        String marcaFormateada = vehiculo.getMarca() != null
                ? vehiculo.getMarca().trim().replace(" ", "_")
                : "";
        String modeloFormateado = vehiculo.getMod() != null
                ? vehiculo.getMod().trim().replace(" ", "_")
                : "";

        String rutaEspecifica = "/autos/" + marcaFormateada + "_" + modeloFormateado + ".png";
        InputStream flujo = getClass().getResourceAsStream(rutaEspecifica);
        if (flujo == null) {
            flujo = getClass().getResourceAsStream("/autos/default.png");
        }

        // Contenedor de la imagen
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

        // Labels obligatorios debajo del ImageView: Marca, Modelo y Patente
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
                ? String.format("$%.2f/día", vehiculo.getTarBase())
                : "";
        Label lblTarifa = new Label(textoTarifa);
        lblTarifa.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #D97706;");

        filaInferior.getChildren().addAll(lblPatente, espaciador, lblTarifa);

        tarjeta.getChildren().addAll(contenedorImagen, lblMarca, lblModelo, filaInferior);
        return tarjeta;
    }

    private void filtrarGaleria(String texto) {
        String filtro = texto == null ? "" : texto.trim().toLowerCase();
        if (filtro.isEmpty()) {
            cargarGrillaVehiculos(listaCompleta);
            return;
        }
        List<Vehiculo> filtrados = new ArrayList<>();
        for (Vehiculo v : listaCompleta) {
            boolean coincide = (v.getMarca() != null && v.getMarca().toLowerCase().contains(filtro))
                    || (v.getMod() != null && v.getMod().toLowerCase().contains(filtro))
                    || (v.getPat() != null && v.getPat().toLowerCase().contains(filtro))
                    || (v.getCat() != null && v.getCat().toLowerCase().contains(filtro));
            if (coincide) {
                filtrados.add(v);
            }
        }
        cargarGrillaVehiculos(filtrados);
    }

    public void recargarVehiculos() {
        try {
            listaCompleta = vehiculoService.listarTodos();
        } catch (Exception e) {
            // Si la base de datos no está disponible aún, mantiene la lista en memoria
        }
        cargarGrillaVehiculos(listaCompleta);
    }

    @FXML
    public void mostrarVistaAlta() {
        if (vistaGaleria != null && vistaAlta != null) {
            cargarSucursales();
            vistaGaleria.setVisible(false);
            vistaGaleria.setManaged(false);
            vistaAlta.setVisible(true);
            vistaAlta.setManaged(true);
        }
    }

    @FXML
    public void mostrarVistaGaleria() {
        if (vistaGaleria != null && vistaAlta != null) {
            vistaAlta.setVisible(false);
            vistaAlta.setManaged(false);
            vistaGaleria.setVisible(true);
            vistaGaleria.setManaged(true);
            recargarVehiculos();
        }
    }

    @FXML
    public void guardarVehiculo() {
        try {
            Integer id = (txtId.getText() == null || txtId.getText().trim().isEmpty())
                    ? null
                    : Integer.parseInt(txtId.getText().trim());
            String pat = txtPat.getText().trim();
            String marca = txtMarca.getText().trim();
            String mod = txtMod.getText().trim();
            Integer aFabr = Integer.parseInt(txtAFabr.getText().trim());
            String fAdq = dpFAdq.getValue() != null ? dpFAdq.getValue().toString() : "";
            Integer kmj = Integer.parseInt(txtKmj.getText().trim());
            String est = txtEst.getText().trim();
            Double preAdq = Double.parseDouble(txtPreAdq.getText().trim());
            String fBaja = dpFBaja.getValue() != null ? dpFBaja.getValue().toString() : null;
            String tiComb = txtTiComb.getText().trim();
            Double carga = Double.parseDouble(txtCarga.getText().trim());
            Integer pax = Integer.parseInt(txtPax.getText().trim());
            Double tarBase = Double.parseDouble(txtTarBase.getText().trim());
            String cat = cmbCat.getValue();
            Sucursal sucursalSeleccionada = cmbSucursal.getValue();

            if (pat.isEmpty() || marca.isEmpty() || mod.isEmpty() || fAdq.isEmpty()
                    || est.isEmpty() || tiComb.isEmpty() || cat == null || sucursalSeleccionada == null) {
                mostrarMensaje("Error de Validación",
                        "Por favor complete todos los campos obligatorios (incluyendo Fecha de Adquisición y Sucursal).",
                        Alert.AlertType.WARNING);
                return;
            }

            Integer codPertenece = sucursalSeleccionada.getCod();

            Vehiculo nuevo = new Vehiculo(id, pat, marca, mod, aFabr, fAdq, kmj, est,
                    preAdq, fBaja, tiComb, carga, pax, tarBase, cat, codPertenece);

            vehiculoService.altaVehiculo(nuevo);
            listaCompleta.add(nuevo);

            mostrarMensaje("Éxito", "El vehículo se guardó correctamente.", Alert.AlertType.INFORMATION);
            limpiarCampos();
            mostrarVistaGaleria();

        } catch (NumberFormatException e) {
            mostrarMensaje("Error de Validación",
                    "Verifique que Año, Kilometraje, Precio, Carga, Pasajeros y Tarifa sean valores numéricos válidos.",
                    Alert.AlertType.WARNING);
        } catch (Exception e) {
            mostrarMensaje("Error", "No se pudo guardar el vehículo: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void limpiarCampos() {
        if (txtId != null) txtId.clear();
        if (txtPat != null) txtPat.clear();
        if (txtMarca != null) txtMarca.clear();
        if (txtMod != null) txtMod.clear();
        if (txtAFabr != null) txtAFabr.clear();
        if (dpFAdq != null) dpFAdq.setValue(null);
        if (txtKmj != null) txtKmj.clear();
        if (txtEst != null) txtEst.clear();
        if (txtPreAdq != null) txtPreAdq.clear();
        if (dpFBaja != null) dpFBaja.setValue(null);
        if (txtTiComb != null) txtTiComb.clear();
        if (txtCarga != null) txtCarga.clear();
        if (txtPax != null) txtPax.clear();
        if (txtTarBase != null) txtTarBase.clear();
        if (cmbSucursal != null) cmbSucursal.setValue(null);
    }

    private void mostrarMensaje(String titulo, String contenido, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(contenido);
        alerta.showAndWait();
    }
}
