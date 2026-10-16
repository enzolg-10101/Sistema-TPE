package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import modelo.Corresponde;
import util.DatabaseConnection;

public class CorrespondeDAO {

    public void insertar(Corresponde corresponde) {
        String sql = "INSERT INTO Corresponde (contrato_id, checkinout_id) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, corresponde.getContratoId());
            stmt.setInt(2, corresponde.getCheckInOutId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al insertar en Corresponde: " + e.getMessage(), e);
        }
    }

    public Corresponde buscarPorId(int id) {
        String sql = "SELECT contrato_id, checkinout_id FROM Corresponde WHERE contrato_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Corresponde(
                        rs.getInt("contrato_id"),
                        rs.getInt("checkinout_id")
                    );
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar en Corresponde por contrato_id: " + e.getMessage(), e);
        }
        return null;
    }

    public boolean existeVinculo(Integer idCheckInOut) {
        String sql = "SELECT COUNT(*) FROM Corresponde WHERE checkinout_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idCheckInOut);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al verificar vínculo en Corresponde: " + e.getMessage(), e);
        }
        return false;
    }
}
