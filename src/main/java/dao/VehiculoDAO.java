package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import modelo.Vehiculo;
import util.DatabaseConnection;

public class VehiculoDAO {

    public void insertar(Vehiculo vehiculo) {
        String sql = "INSERT INTO Vehiculos (V_pat, V_marca, V_mod, V_aFabr, V_fAdq, V_kmj, V_est, "
                   + "V_preAdq, V_fBaja, V_tiComb, T_carga, T_pax, T_tarBase, T_cat, cod_Pertenece) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, vehiculo.getPat());
            stmt.setString(2, vehiculo.getMarca());
            stmt.setString(3, vehiculo.getMod());
            stmt.setInt(4, vehiculo.getaFabr());
            stmt.setString(5, vehiculo.getfAdq());
            stmt.setInt(6, vehiculo.getKmj());
            stmt.setString(7, vehiculo.getEst());
            stmt.setDouble(8, vehiculo.getPreAdq());
            if (vehiculo.getfBaja() != null) {
                stmt.setString(9, vehiculo.getfBaja());
            } else {
                stmt.setNull(9, Types.VARCHAR);
            }
            stmt.setString(10, vehiculo.getTiComb());
            stmt.setDouble(11, vehiculo.getCarga());
            stmt.setInt(12, vehiculo.getPax());
            stmt.setDouble(13, vehiculo.getTarBase());
            stmt.setString(14, vehiculo.getCat());
            stmt.setInt(15, vehiculo.getCodPertenece());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    vehiculo.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al insertar el vehículo: " + e.getMessage(), e);
        }
    }

    public Vehiculo buscarPorId(String pat) {
        String sql = "SELECT V_id, V_pat, V_marca, V_mod, V_aFabr, V_fAdq, V_kmj, V_est, "
                   + "V_preAdq, V_fBaja, V_tiComb, T_carga, T_pax, T_tarBase, T_cat, cod_Pertenece "
                   + "FROM Vehiculos WHERE V_pat = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, pat);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearVehiculo(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar el vehículo por V_pat: " + e.getMessage(), e);
        }
        return null;
    }

    public Vehiculo buscarPorId(int id) {
        String sql = "SELECT V_id, V_pat, V_marca, V_mod, V_aFabr, V_fAdq, V_kmj, V_est, "
                   + "V_preAdq, V_fBaja, V_tiComb, T_carga, T_pax, T_tarBase, T_cat, cod_Pertenece "
                   + "FROM Vehiculos WHERE V_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearVehiculo(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar el vehículo por V_id: " + e.getMessage(), e);
        }
        return null;
    }

    public List<Vehiculo> listarTodos() {
        List<Vehiculo> lista = new ArrayList<>();
        String sql = "SELECT V_id, V_pat, V_marca, V_mod, V_aFabr, V_fAdq, V_kmj, V_est, "
                   + "V_preAdq, V_fBaja, V_tiComb, T_carga, T_pax, T_tarBase, T_cat, cod_Pertenece "
                   + "FROM Vehiculos ORDER BY V_id ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lista.add(mapearVehiculo(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar vehículos: " + e.getMessage(), e);
        }
        return lista;
    }

    private Vehiculo mapearVehiculo(ResultSet rs) throws SQLException {
        return new Vehiculo(
            rs.getInt("V_id"),
            rs.getString("V_pat"),
            rs.getString("V_marca"),
            rs.getString("V_mod"),
            rs.getInt("V_aFabr"),
            rs.getString("V_fAdq"),
            rs.getInt("V_kmj"),
            rs.getString("V_est"),
            rs.getDouble("V_preAdq"),
            rs.getString("V_fBaja"),
            rs.getString("V_tiComb"),
            rs.getDouble("T_carga"),
            rs.getInt("T_pax"),
            rs.getDouble("T_tarBase"),
            rs.getString("T_cat"),
            rs.getInt("cod_Pertenece")
        );
    }
}
