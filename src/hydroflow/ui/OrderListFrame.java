package hydroflow.ui;

import hydroflow.data.DataStore;
import hydroflow.model.Order;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class OrderListFrame extends JFrame {

    private final DataStore store = DataStore.getInstance();

    private final JComboBox<String> statusBox =
            new JComboBox<>(new String[]{"All", "Delivered", "Pending", "Cancelled"});
    private final JTextField searchField = new JTextField(18);

    private final DefaultTableModel tableModel = UIHelper.createReadOnlyModel(new String[]{
            "ORDER NO", "CUSTOMER", "ADDRESS", "TYPE", "WATER", "CONTAINER", "QTY", "EMPTIES IN",
            "DELIVERY FEE", "DISC", "DEPOSIT", "AMT DUE", "STATUS", "PAYMENT", "ORDER DATE"});
    private final JTable table = new JTable(tableModel);

    // the orders that are currently shown in the same order as the table rows
    
    private final List<Order> shownOrders = new ArrayList<>();

    public OrderListFrame() {
        setTitle("HydroFlow Water Refilling Station \u2013 Order List");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1250, 650);
        setMinimumSize(new Dimension(900, 500));
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());
        add(buildTopArea(), BorderLayout.NORTH);
        add(buildTable(), BorderLayout.CENTER);
        add(buildButtonBar(), BorderLayout.SOUTH);

        loadTable();
    }


    // building the screen !!


    // blue "ORDERS" header + status dropdown, search box, and NEW ORDER button
    
    private JPanel buildTopArea() {
        JButton searchButton = UIHelper.createButton("Search", UIHelper.LIGHT_BLUE);
        searchButton.setPreferredSize(new Dimension(100, 30));
        searchButton.addActionListener(e -> loadTable());
        searchField.addActionListener(e -> loadTable());       // Enter key also searches
        statusBox.addActionListener(e -> loadTable());         // sorter for delivery status

        JButton newOrderButton = UIHelper.createButton("NEW ORDER", UIHelper.LIGHT_GREEN);
        newOrderButton.setPreferredSize(new Dimension(150, 32));
        newOrderButton.addActionListener(e -> new NewOrderFrame(this::loadTable).setVisible(true));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        left.add(new JLabel("Status"));
        left.add(statusBox);
        left.add(searchField);
        left.add(searchButton);

        JPanel filterBar = new JPanel(new BorderLayout());
        filterBar.setBackground(UIHelper.GRAY_BG);
        filterBar.setBorder(new EmptyBorder(12, 20, 12, 20));
        filterBar.add(left, BorderLayout.WEST);
        filterBar.add(newOrderButton, BorderLayout.EAST);

        JPanel top = new JPanel(new BorderLayout());
        top.add(UIHelper.createHeader("ORDERS", UIHelper.BLUE), BorderLayout.NORTH);
        top.add(filterBar, BorderLayout.CENTER);
        return top;
    }

    // the table inside a JScrollPane so every column can be reached
    
    private JPanel buildTable() {
        UIHelper.styleTable(table);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);   // keeps the widths, shows a horizontal scrollbar
        UIHelper.setColumnWidths(table, new int[]{80, 120, 150, 80, 80, 130, 50, 90, 100, 60, 80, 90, 90, 80, 100});

        JScrollPane scrollPane = new JScrollPane(table);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UIHelper.GRAY_BG);
        panel.setBorder(new EmptyBorder(5, 20, 5, 20));
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildButtonBar() {
        JButton deliveredButton = UIHelper.createButton("MARK AS DELIVERED", UIHelper.GREEN);
        deliveredButton.setPreferredSize(new Dimension(210, 38));
        deliveredButton.addActionListener(e -> markAsDelivered());

        JButton paymentButton = UIHelper.createButton("RECORD PAYMENT", UIHelper.YELLOW);
        paymentButton.addActionListener(e -> recordPayment());

        JButton cancelButton = UIHelper.createButton("CANCEL ORDER", UIHelper.RED);
        cancelButton.addActionListener(e -> cancelOrder());

        JButton backButton = UIHelper.createButton("BACK", UIHelper.BLUE);
        backButton.addActionListener(e -> UIHelper.goToDashboard(this));

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 15));
        bar.setBackground(UIHelper.GRAY_BG);
        bar.add(deliveredButton);
        bar.add(paymentButton);
        bar.add(cancelButton);
        bar.add(backButton);
        return bar;
    }

  
    // TABLE !!!


    // fills the table using the selected status and the search text
    
    private void loadTable() {
        String statusFilter = (String) statusBox.getSelectedItem();
        String keyword = searchField.getText().trim().toLowerCase();
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        tableModel.setRowCount(0);
        shownOrders.clear();

        for (Order o : store.getOrders()) {
            boolean statusMatches = statusFilter.equals("All") || o.getStatus().equals(statusFilter);
            boolean searchMatches = keyword.isEmpty()
                    || o.getCustomer().getName().toLowerCase().contains(keyword)
                    || o.getCustomer().getAddress().toLowerCase().contains(keyword)
                    || String.valueOf(o.getOrderNo()).equals(keyword);
            if (!statusMatches || !searchMatches) {
                continue;
            }

            shownOrders.add(o);
            tableModel.addRow(new Object[]{
                    o.getOrderNo(),
                    o.getCustomer().getName(),
                    o.getCustomer().getAddress(),
                    o.getOrderType(),
                    o.getWaterType(),
                    o.getContainerType(),
                    o.getQuantity(),
                    o.getEmptiesIn(),
                    String.format("%.2f", o.getDeliveryFee()),
                    String.format("%.2f", o.getDiscount()),
                    String.format("%.2f", o.getDeposit()),
                    String.format("%.2f", o.getAmountDue()),
                    o.getStatus(),
                    o.getPaymentStatus(),
                    o.getOrderDateTime().format(dateFormat)});
        }
    }

    // returns the selected order, or null (with a message) when no row is selected
    private Order getSelectedOrder() {
        int row = table.getSelectedRow();
        if (row < 0) {
            UIHelper.showWarning(this, "Please select an order in the table first.");
            return null;
        }
        return shownOrders.get(row);
    }


    // BUTTONS !!!


    // updates the status AND the Empties Ledger (done inside DataStore.markDelivered)
    private void markAsDelivered() {
        Order order = getSelectedOrder();
        if (order == null) {
            return;
        }
        if (!order.getStatus().equals(Order.PENDING)) {
            UIHelper.showWarning(this, "Only Pending orders can be marked as delivered.");
            return;
        }
        store.markDelivered(order);
        loadTable();
    }

    // asks for the amount paid, computes the change, then changes Unpaid to Paid
    
    private void recordPayment() {
        Order order = getSelectedOrder();
        if (order == null) {
            return;
        }
        if (order.getStatus().equals(Order.CANCELLED)) {
            UIHelper.showWarning(this, "A cancelled order cannot be paid.");
            return;
        }
        if (order.getPaymentStatus().equals(Order.PAID)) {
            UIHelper.showWarning(this, "This order is already paid.");
            return;
        }

        double totalToPay = order.getTotalToPay();
        String message = "Amount due: " + UIHelper.money(order.getAmountDue())
                + "\nContainer deposit: " + UIHelper.money(order.getDeposit())
                + "\nTOTAL TO PAY: " + UIHelper.money(totalToPay)
                + "\n\nEnter the amount of payment:";
        String input = JOptionPane.showInputDialog(this, message, "Record Payment", JOptionPane.QUESTION_MESSAGE);
        if (input == null) {
            return;   // the user pressed Cancel
        }

        try {
            double amountPaid = Double.parseDouble(input.trim());
            if (amountPaid < totalToPay) {
                UIHelper.showWarning(this, "The payment is less than the total to pay.");
                return;
            }
            store.recordPayment(order, amountPaid);
            loadTable();
            JOptionPane.showMessageDialog(this, "Payment recorded.\nChange: " + UIHelper.money(amountPaid - totalToPay));
        } catch (NumberFormatException ex) {
            UIHelper.showWarning(this, "Please enter a valid amount.");
        }
    }

    private void cancelOrder() {
        Order order = getSelectedOrder();
        if (order == null) {
            return;
        }
        if (!order.getStatus().equals(Order.PENDING)) {
            UIHelper.showWarning(this, "Only Pending orders can be cancelled.");
            return;
        }
        int choice = JOptionPane.showConfirmDialog(this, "Cancel order no. " + order.getOrderNo() + "?",
                "Cancel Order", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            store.cancelOrder(order);
            loadTable();
        }
    }
}
