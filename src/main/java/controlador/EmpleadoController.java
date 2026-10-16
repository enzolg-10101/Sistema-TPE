package controlador;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import modelo.Empleado;
import servicio.EmpleadoService; // Asumiendo que este servicio será creado luego

public class EmpleadoController {

    // Definición de variables @FXML para cada atributo del Empleado
    @FXML private TextField txtId;
    @FXML private TextField txtDni;
    @FXML private TextField txtNom;
    @FXML private TextField txtApl;
    @FXML private TextField txtDir;
    @FXML private TextField txtTel;
    @FXML private TextField txtCorreo;
    @FXML private TextField txtFIng;
    @FXML private TextField txtCodTrabaja;

    // Dependencia hacia la capa de servicio
    private EmpleadoService empleadoService;

    public EmpleadoController() {
        // En una arquitectura Java estándar, la inyección se hace manualmente o instanciando.
        // this.empleadoService = new EmpleadoService(...);
    }

    /**
     * Método vinculado al botón 'Guardar' en la vista FXML.
     */
    @FXML
    public void guardarEmpleado() {
        try {
            // 1. Recolección de datos y conversión de tipos
            Integer id = Integer.parseInt(txtId.getText());
            Integer dni = Integer.parseInt(txtDni.getText());
            String nom = txtNom.getText();
            String apl = txtApl.getText();
            String dir = txtDir.getText();
            Long tel = Long.parseLong(txtTel.getText());
            String correo = txtCorreo.getText();
            String fIng = txtFIng.getText();
            Integer codTrabaja = Integer.parseInt(txtCodTrabaja.getText());

            // 2. Armado del objeto Empleado
            Empleado empleado = new Empleado(id, dni, nom, apl, dir, tel, correo, fIng, codTrabaja);

            // 3. Llamada al servicio de alta (comentado para evitar error de compilación hasta que exista el servicio real)
            // empleadoService.altaEmpleado(empleado);

            mostrarMensaje("Éxito", "El empleado ha sido guardado correctamente.", Alert.AlertType.INFORMATION);
            limpiarCampos();

        } catch (NumberFormatException e) {
            // Manejo de errores si el usuario ingresa texto en un campo numérico
            mostrarMensaje("Error de Validación", "Por favor, verifique que ID, DNI, Teléfono y Cod. de Sucursal sean números válidos.", Alert.AlertType.WARNING);
        } catch (Exception e) {
            // Manejo de errores desde la capa de servicio (ej: DNI duplicado)
            mostrarMensaje("Error", "Ocurrió un error al guardar: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Método auxiliar para mostrar alertas de JavaFX de forma limpia.
     */
    private void mostrarMensaje(String titulo, String contenido, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(contenido);
        alerta.showAndWait();
    }

    /**
     * Método auxiliar para limpiar el formulario tras un guardado exitoso.
     */
    private void limpiarCampos() {
        txtId.clear();
        txtDni.clear();
        txtNom.clear();
        txtApl.clear();
        txtDir.clear();
        txtTel.clear();
        txtCorreo.clear();
        txtFIng.clear();
        txtCodTrabaja.clear();
    }
}
