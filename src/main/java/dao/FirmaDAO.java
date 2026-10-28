package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import modelo.Firma;
import util.DatabaseConnection;

public class FirmaDAO {

    public void insertar(Firma firma) {
        String sql = "INSERT INTO Firma (contrato_id, cliente_id) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, firma.getContratoId());
            stmt.setInt(2, firma.getClienteId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al insertar en Firma: " + e.getMessage(), e);
        }
    }

    public Firma buscarPorId(int id) {
        String sql = "SELECT contrato_id, cliente_id FROM Firma WHERE contrato_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Firma(
                        rs.getInt("contrato_id"),
                        rs.getInt("cliente_id")
                    );
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar en Firma por contrato_id: " + e.getMessage(), e);
        }
        return null;
    }
}
