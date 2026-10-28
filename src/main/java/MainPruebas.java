import dao.SucursalDAO;
import modelo.Sucursal;

public class MainPruebas {
    public static void main(String[] args) {
        try {
            // Creamos un objeto de prueba
            Sucursal sucursalPrueba = new Sucursal();
            sucursalPrueba.setS_nom("Sede Central");
            sucursalPrueba.setS_dir("Av. Illia 123");
            sucursalPrueba.setS_tel(422334);

            // Instanciamos el DAO y ejecutamos el alta
            SucursalDAO dao = new SucursalDAO();
            dao.insertar(sucursalPrueba);

            System.out.println("¡Conexión exitosa! La sucursal se guardó en la base de datos.");
        } catch (Exception e) {
            System.out.println("Error de conexión o inserción: ");
            e.printStackTrace();
        }
    }
}
