package hydroflow.dao;

import hydroflow.db.DBConnection;
import hydroflow.db.DatabaseException;
import hydroflow.model.Customer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

// TABLE: customer lists
public class CustomerDAO {

    public List<Customer> findAll() {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT customer_id, name, address, contact_no FROM customers ORDER BY customer_id";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                customers.add(new Customer(rs.getInt("customer_id"), rs.getString("name"),
                        rs.getString("address"), rs.getString("contact_no")));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not load the customers.", e);
        }
        return customers;
    }

    // inserts a customer and returns it with the id created by MySQL
    
    public Customer insert(String name, String address, String contactNo) {
        String sql = "INSERT INTO customers (name, address, contact_no) VALUES (?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, address);
            ps.setString(3, contactNo);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return new Customer(keys.getInt(1), name, address, contactNo);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not add the customer.", e);
        }
    }

    public void update(Customer c) {
        String sql = "UPDATE customers SET name = ?, address = ?, contact_no = ? WHERE customer_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, c.getName());
            ps.setString(2, c.getAddress());
            ps.setString(3, c.getContactNo());
            ps.setInt(4, c.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Could not update the customer.", e);
        }
    }

    // if the customer already has at least one order
    
    public boolean hasOrders(int customerId) {
        String sql = "SELECT 1 FROM orders WHERE customer_id = ? LIMIT 1";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not check the customer's orders.", e);
        }
    }

    public void delete(int customerId) {
        String sql = "DELETE FROM customers WHERE customer_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Could not delete the customer.", e);
        }
    }
}
