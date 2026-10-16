package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import modelo.Contrato;
import util.DatabaseConnection;

public class ContratoDAO {

    public void insertar(Contrato contrato) {
        String sql = "INSERT INTO Contratos (Co_nro, Co_feHrEnt, Co_feHrDev, Co_condUso, Co_est, "
                   + "Co_impFianza, L_impAlq, L_cargos, id_Asigna, id_Firma) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, contrato.getNro());
            stmt.setString(2, contrato.getFeHrEnt());
            stmt.setString(3, contrato.getFeHrDev());
            stmt.setString(4, contrato.getCondUso());
            stmt.setString(5, contrato.getEst());
            if (contrato.getImpFianza() != null) {
                stmt.setDouble(6, contrato.getImpFianza());
            } else {
                stmt.setNull(6, Types.DOUBLE);
            }
            stmt.setDouble(7, contrato.getImpAlq());
            if (contrato.getCargos() != null) {
                stmt.setDouble(8, contrato.getCargos());
            } else {
                stmt.setNull(8, Types.DOUBLE);
            }
            stmt.setInt(9, contrato.getIdAsigna());
            stmt.setInt(10, contrato.getIdFirma());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al insertar el contrato: " + e.getMessage(), e);
        }
    }

    public Contrato buscarPorId(int id) {
        String sql = "SELECT Co_nro, Co_feHrEnt, Co_feHrDev, Co_condUso, Co_est, "
                   + "Co_impFianza, L_impAlq, L_cargos, id_Asigna, id_Firma "
                   + "FROM Contratos WHERE Co_nro = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Double fianza = rs.getObject("Co_impFianza") != null ? rs.getDouble("Co_impFianza") : null;
                    Double cargos = rs.getObject("L_cargos") != null ? rs.getDouble("L_cargos") : null;
                    return new Contrato(
                        rs.getInt("Co_nro"),
                        rs.getString("Co_feHrEnt"),
                        rs.getString("Co_feHrDev"),
                        rs.getString("Co_condUso"),
                        rs.getString("Co_est"),
                        fianza,
                        rs.getDouble("L_impAlq"),
                        cargos,
                        rs.getInt("id_Asigna"),
                        rs.getInt("id_Firma")
                    );
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar el contrato por Co_nro: " + e.getMessage(), e);
        }
        return null;
    }
}
