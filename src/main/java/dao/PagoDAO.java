package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import modelo.MedioPago;
import modelo.Pago;
import util.DatabaseConnection;

public class PagoDAO {

    public void insertar(Pago pago) {
        String medioValor = pago.getMedio() != null ? pago.getMedio().getDescripcion() : null;
        if (pago.getId() != null) {
            String sql = "INSERT INTO Pagos (P_id, P_fec, P_imp, P_medio, nro_Registra) VALUES (?, ?, ?, ?, ?)";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, pago.getId());
                stmt.setString(2, pago.getFec());
                stmt.setDouble(3, pago.getImp());
                stmt.setString(4, medioValor);
                stmt.setInt(5, pago.getNroRegistra());
                stmt.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Error al insertar el pago: " + e.getMessage(), e);
            }
        } else {
            String sql = "INSERT INTO Pagos (P_fec, P_imp, P_medio, nro_Registra) VALUES (?, ?, ?, ?)";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, pago.getFec());
                stmt.setDouble(2, pago.getImp());
                stmt.setString(3, medioValor);
                stmt.setInt(4, pago.getNroRegistra());
                stmt.executeUpdate();
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        pago.setId(keys.getInt(1));
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException("Error al insertar el pago: " + e.getMessage(), e);
            }
        }
    }

    public Pago buscarPorId(int id) {
        String sql = "SELECT P_id, P_fec, P_imp, P_medio, nro_Registra FROM Pagos WHERE P_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Pago(
                        rs.getInt("P_id"),
                        rs.getString("P_fec"),
                        rs.getDouble("P_imp"),
                        MedioPago.fromString(rs.getString("P_medio")),
                        rs.getInt("nro_Registra")
                    );
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar el pago por P_id: " + e.getMessage(), e);
        }
        return null;
    }
}
