package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import modelo.Mantenimiento;
import modelo.TipoMantenimiento;
import util.DatabaseConnection;

public class MantenimientoDAO {

    private static final String COLUMNAS =
            "M_id, M_fec, M_tipo, M_kmj, M_costo, M_obs, id_Realiza, id_Realizado";

    /**
     * Graba la nupla en Mantenimientos (M_id es AUTO_INCREMENT).
     */
    public void insertar(Mantenimiento mantenimiento) {
        String tipoBd = mantenimiento.getTipo() != null ? mantenimiento.getTipo().getNombreBd() : null;
        String sql = "INSERT INTO Mantenimientos (M_fec, M_tipo, M_kmj, M_costo, M_obs, id_Realiza, id_Realizado) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, mantenimiento.getFec());
            stmt.setString(2, tipoBd);
            stmt.setInt(3, mantenimiento.getKmj());
            stmt.setDouble(4, mantenimiento.getCosto());
            if (mantenimiento.getObs() != null && !mantenimiento.getObs().trim().isEmpty()) {
                stmt.setString(5, mantenimiento.getObs().trim());
            } else {
                stmt.setNull(5, Types.VARCHAR); // M_obs es opcional
            }
            stmt.setInt(6, mantenimiento.getIdRealiza());
            stmt.setInt(7, mantenimiento.getIdRealizado());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    mantenimiento.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al insertar el mantenimiento: " + e.getMessage(), e);
        }
    }

    public Mantenimiento buscarPorId(int id) {
        String sql = "SELECT " + COLUMNAS + " FROM Mantenimientos WHERE M_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearMantenimiento(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar el mantenimiento por M_id: " + e.getMessage(), e);
        }
        return null;
    }

    public List<Mantenimiento> listarTodos() {
        String sql = "SELECT " + COLUMNAS + " FROM Mantenimientos ORDER BY M_fec DESC, M_id DESC";
        return ejecutarListado(sql, null, "Error al listar los mantenimientos");
    }

    /**
     * Historial de mantenimientos de un vehículo (relación Realizado: id_Realizado -> Vehiculos.V_id).
     */
    public List<Mantenimiento> listarPorVehiculo(int idVehiculo) {
        String sql = "SELECT " + COLUMNAS + " FROM Mantenimientos WHERE id_Realizado = ? "
                   + "ORDER BY M_fec DESC, M_id DESC";
        return ejecutarListado(sql, idVehiculo, "Error al listar los mantenimientos del vehículo");
    }

    /**
     * Mantenimientos realizados por un empleado (relación Realiza: id_Realiza -> Empleados.E_id).
     */
    public List<Mantenimiento> listarPorEmpleado(int idEmpleado) {
        String sql = "SELECT " + COLUMNAS + " FROM Mantenimientos WHERE id_Realiza = ? "
                   + "ORDER BY M_fec DESC, M_id DESC";
        return ejecutarListado(sql, idEmpleado, "Error al listar los mantenimientos del empleado");
    }

    private List<Mantenimiento> ejecutarListado(String sql, Integer parametro, String mensajeError) {
        List<Mantenimiento> lista = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (parametro != null) {
                stmt.setInt(1, parametro);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearMantenimiento(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(mensajeError + ": " + e.getMessage(), e);
        }
        return lista;
    }

    private Mantenimiento mapearMantenimiento(ResultSet rs) throws SQLException {
        return new Mantenimiento(
            rs.getInt("M_id"),
            rs.getString("M_fec"),
            TipoMantenimiento.fromString(rs.getString("M_tipo")),
            rs.getInt("M_kmj"),
            rs.getDouble("M_costo"),
            rs.getString("M_obs"),
            rs.getInt("id_Realiza"),
            rs.getInt("id_Realizado")
        );
    }
}
