package hydroflow.dao;

import hydroflow.db.DBConnection;
import hydroflow.db.DatabaseException;
import hydroflow.model.Customer;
import hydroflow.model.LedgerEntry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

// TABLE: empties ledger. one row per delivered order
public class LedgerDAO {

    public List<LedgerEntry> findAll() {
        List<LedgerEntry> entries = new ArrayList<>();
        String sql = "SELECT l.order_no, l.ledger_date, l.water_type, l.container_type, l.qty_out, "
                + "l.empties_in, l.deposit, c.customer_id, c.name, c.address, c.contact_no "
                + "FROM empties_ledger l JOIN customers c ON c.customer_id = l.customer_id "
                + "ORDER BY l.order_no";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Customer customer = new Customer(rs.getInt("customer_id"), rs.getString("name"),
                        rs.getString("address"), rs.getString("contact_no"));
                        entries.add(new LedgerEntry(rs.getInt("order_no"), rs.getDate("ledger_date").toLocalDate(),
                        customer, rs.getString("water_type"), rs.getString("container_type"),
                        rs.getInt("qty_out"), rs.getInt("empties_in"), rs.getDouble("deposit")));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not load the empties ledger.", e);
        }
        return entries;
    }

    // MARK AS DELIVERED: sets the order status to Delivered and adds the ledger row

    public void addDeliveredEntry(LedgerEntry entry) {
        String updateOrder = "UPDATE orders SET order_status = 'Delivered' WHERE order_no = ?";
        String insertLedger = "INSERT INTO empties_ledger (order_no, ledger_date, customer_id, water_type, "
                + "container_type, qty_out, empties_in, deposit) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps1 = con.prepareStatement(updateOrder);
                 PreparedStatement ps2 = con.prepareStatement(insertLedger)) {
                ps1.setInt(1, entry.getOrderNo());
                ps1.executeUpdate();

                ps2.setInt(1, entry.getOrderNo());
                ps2.setDate(2, java.sql.Date.valueOf(entry.getDate()));
                ps2.setInt(3, entry.getCustomer().getId());
                ps2.setString(4, entry.getWaterType());
                ps2.setString(5, entry.getContainerType());
                ps2.setInt(6, entry.getQtyOut());
                ps2.setInt(7, entry.getEmptiesIn());
                ps2.setDouble(8, entry.getDeposit());
                ps2.executeUpdate();

                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not mark the order as delivered.", e);
        }
    }

    // RETURN EMPTIES ONLY: saves the new EMPTIES IN value of one ledger row
    
    public void updateEmptiesIn(int orderNo, int emptiesIn) {
        String sql = "UPDATE empties_ledger SET empties_in = ? WHERE order_no = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, emptiesIn);
            ps.setInt(2, orderNo);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Could not save the returned empties.", e);
        }
    }

    // REFUND DEPOSIT: the cash was handed back, so the deposit becomes 0
    
    public void clearDeposit(int orderNo) {
        String sql = "UPDATE empties_ledger SET deposit = 0 WHERE order_no = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, orderNo);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Could not refund the deposit.", e);
        }
    }
}
