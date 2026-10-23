package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import modelo.Mantenimiento;
import modelo.TipoMantenimiento;
import util.DatabaseConnection;

public class MantenimientoDAO {

    public void insertar(Mantenimiento mantenimiento) {
        String tipoValor = mantenimiento.getTipo() != null ? mantenimiento.getTipo().getDescripcion() : null;
        if (mantenimiento.getId() != null) {
            String sql = "INSERT INTO Mantenimientos (M_id, M_fec, M_tipo, M_kmj, M_costo, M_obs, id_Realiza, id_Realizado) "
                       + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, mantenimiento.getId());
                stmt.setString(2, mantenimiento.getFec());
                stmt.setString(3, tipoValor);
                stmt.setInt(4, mantenimiento.getKmj());
                stmt.setDouble(5, mantenimiento.getCosto());
                stmt.setString(6, mantenimiento.getObs());
                stmt.setInt(7, mantenimiento.getIdRealiza());
                stmt.setInt(8, mantenimiento.getIdRealizado());
                stmt.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Error al insertar el mantenimiento: " + e.getMessage(), e);
            }
        } else {
            String sql = "INSERT INTO Mantenimientos (M_fec, M_tipo, M_kmj, M_costo, M_obs, id_Realiza, id_Realizado) "
                       + "VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, mantenimiento.getFec());
                stmt.setString(2, tipoValor);
                stmt.setInt(3, mantenimiento.getKmj());
                stmt.setDouble(4, mantenimiento.getCosto());
                stmt.setString(5, mantenimiento.getObs());
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
    }

    public Mantenimiento buscarPorId(int id) {
        String sql = "SELECT M_id, M_fec, M_tipo, M_kmj, M_costo, M_obs, id_Realiza, id_Realizado "
                   + "FROM Mantenimientos WHERE M_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
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
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar el mantenimiento por M_id: " + e.getMessage(), e);
        }
        return null;
    }
}
