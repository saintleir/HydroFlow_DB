package hydroflow.dao;

import hydroflow.db.DBConnection;
import hydroflow.db.DatabaseException;
import hydroflow.model.Customer;
import hydroflow.model.Order;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// TABLE: order list
public class OrderDAO {

    // every order is loaded together with its customer
	
    private static final String SELECT_ORDERS =
            "SELECT o.*, c.name, c.address, c.contact_no "
            + "FROM orders o JOIN customers c ON c.customer_id = o.customer_id ";

    // READING: all orders
    
    public List<Order> findAll() {
        return query(SELECT_ORDERS + "ORDER BY o.order_no");
    }

    // orders of one day that are not cancelled
    
    public List<Order> findByDate(LocalDate date) {
        return query(SELECT_ORDERS
                + "WHERE DATE(o.order_datetime) = ? AND o.order_status <> 'Cancelled' ORDER BY o.order_no",
                java.sql.Date.valueOf(date));
    }

    // orders of one month that are not cancelled
    
    public List<Order> findByMonth(int month, int year) {
        return query(SELECT_ORDERS
                + "WHERE MONTH(o.order_datetime) = ? AND YEAR(o.order_datetime) = ? "
                + "AND o.order_status <> 'Cancelled' ORDER BY o.order_no",
                month, year);
    }

    // runs a SELECT and converts each row to an Order
    
    private List<Order> query(String sql, Object... parameters) {
        List<Order> orders = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < parameters.length; i++) {
                ps.setObject(i + 1, parameters[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    orders.add(toOrder(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not load the orders.", e);
        }
        return orders;
    }

    private Order toOrder(ResultSet rs) throws SQLException {
        Customer customer = new Customer(rs.getInt("customer_id"), rs.getString("name"),
                rs.getString("address"), rs.getString("contact_no"));

        Order order = new Order(customer, rs.getString("order_type"), rs.getString("water_type"),
                rs.getString("container_type"), rs.getInt("quantity"), rs.getInt("empties_in"),
                rs.getDouble("unit_price"), rs.getDouble("delivery_fee"), rs.getDouble("discount"),
                rs.getDouble("deposit"), rs.getString("remarks"));

        order.setOrderNo(rs.getInt("order_no"));
        order.setStatus(rs.getString("order_status"));
        order.setPaymentStatus(rs.getString("payment_status"));
        order.setAmountPaid(rs.getDouble("amount_paid"));
        order.setChange(rs.getDouble("change_amount"));
        order.setOrderDateTime(rs.getTimestamp("order_datetime").toLocalDateTime());
        return order;
    }

    // WRITING

    // inserts a new order and returns the order number created by MySQL 
    
    public int insert(Order o) {
        String sql = "INSERT INTO orders (customer_id, order_type, water_type, container_type, quantity, "
                + "empties_in, unit_price, delivery_fee, discount, deposit, amount_due, order_status, "
                + "payment_status, amount_paid, change_amount, order_datetime, remarks) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, o.getCustomer().getId());
            ps.setString(2, o.getOrderType());
            ps.setString(3, o.getWaterType());
            ps.setString(4, o.getContainerType());
            ps.setInt(5, o.getQuantity());
            ps.setInt(6, o.getEmptiesIn());
            ps.setDouble(7, o.getUnitPrice());
            ps.setDouble(8, o.getDeliveryFee());
            ps.setDouble(9, o.getDiscount());
            ps.setDouble(10, o.getDeposit());
            ps.setDouble(11, o.getAmountDue());
            ps.setString(12, o.getStatus());
            ps.setString(13, o.getPaymentStatus());
            ps.setDouble(14, o.getAmountPaid());
            ps.setDouble(15, o.getChange());
            ps.setTimestamp(16, Timestamp.valueOf(o.getOrderDateTime()));
            ps.setString(17, o.getRemarks() == null ? "" : o.getRemarks());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not save the order.", e);
        }
    }

    // used by CANCEL ORDER
    public void updateStatus(int orderNo, String status) {
        String sql = "UPDATE orders SET order_status = ? WHERE order_no = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, orderNo);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Could not update the order status.", e);
        }
    }

    // used by RECORD PAYMENT: Unpaid -> Paid, with the amount paid and the change
    
    public void updatePayment(int orderNo, double amountPaid, double change) {
        String sql = "UPDATE orders SET payment_status = 'Paid', amount_paid = ?, change_amount = ? "
                + "WHERE order_no = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDouble(1, amountPaid);
            ps.setDouble(2, change);
            ps.setInt(3, orderNo);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Could not record the payment.", e);
        }
    }


    // numbers for the charts

    // revenue of PAID orders for each month of a year (index 0 = January)
    
    public double[] getMonthlyRevenue(int year) {
        double[] totals = new double[12];
        String sql = "SELECT MONTH(order_datetime) AS part, SUM(amount_due) AS total FROM orders "
                + "WHERE YEAR(order_datetime) = ? AND payment_status = 'Paid' AND order_status <> 'Cancelled' "
                + "GROUP BY MONTH(order_datetime)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, year);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    totals[rs.getInt("part") - 1] = rs.getDouble("total");
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not load the monthly revenue.", e);
        }
        return totals;
    }

    // revenue of PAID orders for each day of a month (index 0 = day 1)
    
    public double[] getDailyRevenue(int month, int year) {
        double[] totals = new double[LocalDate.of(year, month, 1).lengthOfMonth()];
        String sql = "SELECT DAY(order_datetime) AS part, SUM(amount_due) AS total FROM orders "
                + "WHERE MONTH(order_datetime) = ? AND YEAR(order_datetime) = ? "
                + "AND payment_status = 'Paid' AND order_status <> 'Cancelled' "
                + "GROUP BY DAY(order_datetime)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, month);
            ps.setInt(2, year);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    totals[rs.getInt("part") - 1] = rs.getDouble("total");
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not load the daily revenue.", e);
        }
        return totals;
    }
}
