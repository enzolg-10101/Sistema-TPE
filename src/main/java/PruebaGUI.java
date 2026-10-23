import java.io.File;
import java.net.URL;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class PruebaGUI {

    public static void main(String[] args) {
        Application.launch(App.class, args);
    }

    public static class App extends Application {

        @Override
        public void start(Stage stage) throws Exception {
            Application.setUserAgentStylesheet(new atlantafx.base.theme.PrimerLight().getUserAgentStylesheet());

            URL fxmlUrl = getClass().getResource("/EmpleadoView.fxml");
            if (fxmlUrl == null) {
                fxmlUrl = getClass().getResource("/vista/EmpleadoView.fxml");
            }
            if (fxmlUrl == null) {
                fxmlUrl = new File("src/main/resources/EmpleadoView.fxml").toURI().toURL();
            }

            Parent root = FXMLLoader.load(fxmlUrl);
            Scene scene = new Scene(root);

            stage.setTitle("Prueba Visual - Alta Empleados");
            stage.setScene(scene);
            stage.show();
        }
    }
}
