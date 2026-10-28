package servicio;

import dao.SucursalDAO;
import java.util.List;
import modelo.Sucursal;

public class SucursalService {

    private SucursalDAO sucursalDAO;

    public SucursalService() {
        this.sucursalDAO = new SucursalDAO();
    }

    public SucursalService(SucursalDAO sucursalDAO) {
        this.sucursalDAO = sucursalDAO;
    }

    /**
     * 4.2 Alta en la entidad Sucursales
     */
    public Sucursal altaSucursal(Sucursal sucursal) {
        // Inicio-ts
        try {
            if (sucursal == null) {
                throw new IllegalArgumentException("La sucursal no puede ser nula.");
            }
            // Grabar nupla en Sucursales
            sucursalDAO.insertar(sucursal);
            return sucursal;
        } catch (Exception e) {
            throw new RuntimeException("Error en la transacción de Alta de Sucursal: " + e.getMessage(), e);
        }
        // Fin-ts
    }

    public List<Sucursal> listarTodas() {
        return sucursalDAO.listarTodas();
    }

    public List<Sucursal> obtenerResumenSucursales() {
        return sucursalDAO.obtenerResumenSucursales();
    }
}
