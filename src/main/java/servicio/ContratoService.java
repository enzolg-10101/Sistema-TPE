package servicio;

import dao.AsignaDAO;
import dao.CheckInOutDAO;
import dao.ClienteDAO;
import dao.ContratoDAO;
import dao.CorrespondeDAO;
import dao.FirmaDAO;
import dao.VehiculoDAO;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import modelo.Asigna;
import modelo.CheckInOut;
import modelo.Cliente;
import modelo.Contrato;
import modelo.Corresponde;
import modelo.Firma;
import modelo.Vehiculo;
import util.DatabaseConnection;

public class ContratoService {

    // Dependencias de los DAOs (repositorios) y otros servicios
    private ContratoDAO contratoDAO;
    private ClienteDAO clienteDAO;
    private VehiculoDAO vehiculoDAO;
    private CheckInOutDAO checkInOutDAO;
    private FirmaDAO firmaDAO;
    private AsignaDAO asignaDAO;
    private CorrespondeDAO correspondeDAO;

    private ClienteService clienteService;
    private CheckInOutService checkInOutService;

    public ContratoService(ContratoDAO contratoDAO, ClienteDAO clienteDAO, VehiculoDAO vehiculoDAO,
            CheckInOutDAO checkInOutDAO, FirmaDAO firmaDAO, AsignaDAO asignaDAO,
            CorrespondeDAO correspondeDAO, ClienteService clienteService,
            CheckInOutService checkInOutService) {
        this.contratoDAO = contratoDAO;
        this.clienteDAO = clienteDAO;
        this.vehiculoDAO = vehiculoDAO;
        this.checkInOutDAO = checkInOutDAO;
        this.firmaDAO = firmaDAO;
        this.asignaDAO = asignaDAO;
        this.correspondeDAO = correspondeDAO;
        this.clienteService = clienteService;
        this.checkInOutService = checkInOutService;
    }

    /**
     * Alta de Contrato delegando toda la transacción atómica al procedimiento almacenado 'insertar_contrato'.
     *
     * @param existe       true si el cliente ya existe en BD (1), false si es un nuevo cliente que el SP debe dar de alta (0).
     * @param contrato     datos del contrato a generar.
     * @param cliente      datos del cliente.
     * @param vehiculo     vehículo seleccionado.
     * @param checkInOut   datos de salida del vehículo.
     * @param nroPEnt      empleado de entrada (opcional/null en la apertura).
     */

    public void altaContrato(boolean existe, Contrato contrato, Cliente cliente, Vehiculo vehiculo,
            CheckInOut checkInOut, Integer nroPEnt) {
        if (vehiculo != null && vehiculo.getId() != null) {
            contrato.setIdAsigna(vehiculo.getId());
        }
        if (existe && cliente != null && cliente.getId() != null) {
            contrato.setIdFirma(cliente.getId());
        }

        contratoDAO.insertar(existe, contrato, cliente, checkInOut, nroPEnt);
    }


    public void altaContrato(boolean existe, Contrato contrato, Cliente cliente, Vehiculo vehiculo,
            CheckInOut checkInOut) {
        altaContrato(existe, contrato, cliente, vehiculo, checkInOut, null);
    }

    /**
     * Sobrecarga de conveniencia: deduce si el cliente existe según si cuenta con C_id asignado.
     */
    public void altaContrato(Contrato contrato, Cliente cliente, Vehiculo vehiculo, CheckInOut checkInOut) {
        boolean existe = (cliente != null && cliente.getId() != null);
        altaContrato(existe, contrato, cliente, vehiculo, checkInOut, null);
    }


    /**
     * Registro de alquiler para un cliente nuevo (existe = false).
     */
    public void registrarAlquilerNuevoCliente(Cliente cliente, Contrato contrato) {
        altaContrato(false, contrato, cliente, null, null, null);
    }


    public void registrarAlquilerNuevoCliente(Cliente cliente, Contrato contrato, CheckInOut checkInOut) {
        altaContrato(false, contrato, cliente, null, checkInOut, null);
    }

    public List<Contrato> listarTodos() {
        return contratoDAO.listarTodos();
    }

    public Contrato buscarPorId(int id) {
        return contratoDAO.buscarPorId(id);
    }
}
