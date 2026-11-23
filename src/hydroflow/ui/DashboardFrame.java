package hydroflow.ui;

import hydroflow.data.DataStore;
import hydroflow.model.Customer;
import hydroflow.model.Order;
import hydroflow.model.Settings;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;

/**
 * Admin Dashboard / Home:
 *  - left menu (HOME, ADD CUSTOMER, ORDER LIST, SALES RECORD, REPORTS, EMPTIES LEDGER, SETTINGS)
 *  - 3 summary KPI panels
 *  - Customer List table with edit and delete icon buttons
 */
@SuppressWarnings("serial")
public class DashboardFrame extends JFrame {

    private static final int ACTION_COLUMN = 3;   // 4th column of the customer table

    private final DataStore store = DataStore.getInstance();

    // KPI panel 1: container dispatched
    
    private final JLabel containerValue = new JLabel();
    private final JProgressBar containerBar = new JProgressBar(0, 100);
    private final JLabel containerPercent = new JLabel();

    // KPI panel 2: delivery transaction
    
    private final JLabel deliveryValue = new JLabel();
    private final JProgressBar deliveryBar = new JProgressBar(0, 100);
    private final JLabel deliveryPercent = new JLabel();

    // KPI panel 3: total sales
    
    private final JLabel salesValue = new JLabel();
    private final JProgressBar salesBar = new JProgressBar(0, 100);
    private final JLabel salesPercent = new JLabel();

    // CUSTOMER LIST !!!
    
    private final DefaultTableModel customerModel =
            UIHelper.createReadOnlyModel(new String[]{"Name", "Address", "Contact No.", "Action"});
    private final JTable customerTable = new JTable(customerModel);

    public DashboardFrame() {
        setTitle("HydroFlow Water Refilling Station \u2013 Admin Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1150, 700);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());
        add(buildSidebar(), BorderLayout.WEST);
        add(buildMainArea(), BorderLayout.CENTER);

        refresh();
    }


    // building the screen


    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(UIHelper.SIDEBAR_BLUE);
        sidebar.setPreferredSize(new Dimension(240, 0));

        JLabel logo = new JLabel("HYDROFLOW", SwingConstants.CENTER);
        logo.setFont(new Font(UIHelper.FONT, Font.BOLD, 20));
        logo.setForeground(Color.WHITE);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        logo.setBorder(new EmptyBorder(25, 0, 35, 0));
        sidebar.add(logo);

        sidebar.add(createMenuButton("HOME", e -> refresh()));
        sidebar.add(createMenuButton("ADD CUSTOMER", e -> openAddCustomer()));
        sidebar.add(createMenuButton("ORDER LIST", e -> openFrame(new OrderListFrame())));
        sidebar.add(createMenuButton("SALES RECORD", e -> openFrame(new SalesRecordFrame())));
        sidebar.add(createMenuButton("REPORTS", e -> openFrame(new ReportsFrame())));
        sidebar.add(createMenuButton("EMPTIES LEDGER", e -> openFrame(new EmptiesLedgerFrame())));
        sidebar.add(createMenuButton("SETTINGS", e -> new SettingsFrame(this::refresh).setVisible(true)));
        return sidebar;
    }

    private JButton createMenuButton(String text, java.awt.event.ActionListener action) {
        JButton button = new JButton(text);
        button.setFont(new Font(UIHelper.FONT, Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setBackground(UIHelper.SIDEBAR_BLUE);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBorder(new EmptyBorder(0, 25, 0, 5));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.addActionListener(action);
        return button;
    }

    private JPanel buildMainArea() {
    	
        // top bar with the LOG OUT button
    	
        JButton logoutButton = UIHelper.createButton("LOG OUT", new Color(238, 0, 0));
        logoutButton.setPreferredSize(new Dimension(130, 38));
        logoutButton.addActionListener(e -> UIHelper.logout(this));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 12));
        topBar.setBackground(Color.WHITE);
        topBar.add(logoutButton);

        // content: breadcrumb, KPI panels, customer list
        
        JLabel breadcrumb = new JLabel("DASHBOARD / HOME");
        breadcrumb.setFont(new Font(UIHelper.FONT, Font.BOLD, 13));

        JPanel kpiRow = new JPanel(new GridLayout(1, 3, 40, 0));
        kpiRow.setOpaque(false);
        kpiRow.add(createKpiPanel("CONTAINER DISPATCHED", containerValue, containerBar, containerPercent,
                new Color(109, 166, 217)));
        kpiRow.add(createKpiPanel("DELIVERY TRANSACTION", deliveryValue, deliveryBar, deliveryPercent,
                new Color(46, 117, 182)));
        kpiRow.add(createKpiPanel("TOTAL SALES", salesValue, salesBar, salesPercent,
                new Color(31, 78, 121)));

        JPanel top = new JPanel(new BorderLayout(0, 15));
        top.setOpaque(false);
        top.add(breadcrumb, BorderLayout.NORTH);
        top.add(kpiRow, BorderLayout.CENTER);

        JPanel content = new JPanel(new BorderLayout(0, 20));
        content.setBackground(new Color(242, 242, 242));
        content.setBorder(new EmptyBorder(15, 25, 25, 25));
        content.add(top, BorderLayout.NORTH);
        content.add(buildCustomerListPanel(), BorderLayout.CENTER);

        JPanel main = new JPanel(new BorderLayout());
        main.add(topBar, BorderLayout.NORTH);
        main.add(content, BorderLayout.CENTER);
        return main;
    }

    // one KPI panel: title, big value, progress bar and a percent text
    
    private JPanel createKpiPanel(String title, JLabel valueLabel, JProgressBar bar,
                                  JLabel percentLabel, Color background) {
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font(UIHelper.FONT, Font.BOLD, 14));
        titleLabel.setForeground(Color.WHITE);

        valueLabel.setFont(new Font(UIHelper.FONT, Font.BOLD, 28));
        valueLabel.setForeground(Color.WHITE);

        bar.setForeground(Color.WHITE);
        bar.setBackground(new Color(200, 200, 200));
        bar.setBorderPainted(false);
        bar.setPreferredSize(new Dimension(100, 8));

        percentLabel.setFont(new Font(UIHelper.FONT, Font.BOLD, 11));
        percentLabel.setForeground(Color.WHITE);

        JPanel barRow = new JPanel(new BorderLayout(8, 0));
        barRow.setOpaque(false);
        barRow.add(bar, BorderLayout.CENTER);
        barRow.add(percentLabel, BorderLayout.EAST);

        JPanel panel = new JPanel(new GridLayout(3, 1, 0, 8));
        panel.setBackground(background);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        panel.add(titleLabel);
        panel.add(valueLabel);
        panel.add(barRow);
        return panel;
    }

    private JPanel buildCustomerListPanel() {
        JLabel title = new JLabel("Customer List");
        title.setFont(new Font(UIHelper.FONT, Font.BOLD, 15));

        UIHelper.styleTable(customerTable);
        customerTable.setRowHeight(32);
        UIHelper.setColumnWidths(customerTable, new int[]{200, 280, 180, 100});
        customerTable.getColumnModel().getColumn(ACTION_COLUMN).setCellRenderer(new ActionCellRenderer());

        // the ACTION column only DRAWS the two icons, so we check where the user clicked
        
        customerTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = customerTable.rowAtPoint(e.getPoint());
                int column = customerTable.columnAtPoint(e.getPoint());
                if (row < 0 || column != ACTION_COLUMN) {
                    return;
                }
                Rectangle cell = customerTable.getCellRect(row, column, false);
                boolean clickedLeftHalf = e.getX() < cell.x + cell.width / 2;
                if (clickedLeftHalf) {
                    editCustomer(row);
                } else {
                    deleteCustomer(row);
                }
            }
        });

        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)), new EmptyBorder(12, 15, 12, 15)));
        panel.add(title, BorderLayout.NORTH);
        panel.add(new JScrollPane(customerTable), BorderLayout.CENTER);
        return panel;
    }

   
    // ACTIONS !!!


    private void openFrame(JFrame next) {
        dispose();
        next.setVisible(true);
    }

    private void openAddCustomer() {
        new AddCustomerFrame(null, this::refresh).setVisible(true);
    }

    private void editCustomer(int row) {
        Customer customer = store.getCustomers().get(row);
        new AddCustomerFrame(customer, this::refresh).setVisible(true);
    }

    private void deleteCustomer(int row) {
        Customer customer = store.getCustomers().get(row);
        int choice = JOptionPane.showConfirmDialog(this,
                "Delete customer \"" + customer.getName() + "\"?", "Confirm Delete",
                JOptionPane.YES_NO_OPTION);
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }
        if (!store.deleteCustomer(customer)) {
            UIHelper.showWarning(this, "This customer already has orders, so it cannot be deleted.");
        }
        refresh();
    }

    // re-reads the data and updates the KPI panels and the customer table
    
    private void refresh() {
        refreshKpis();
        refreshCustomerTable();
    }

    private void refreshKpis() {
        Settings settings = store.getSettings();

        // count the orders that are not cancelled
        
        int totalOrders = 0;
        int deliveredOrders = 0;
        int containersOrdered = 0;
        for (Order o : store.getOrders()) {
            if (o.getStatus().equals(Order.CANCELLED)) {
                continue;
            }
            totalOrders++;
            containersOrdered += o.getQuantity();
            if (o.getStatus().equals(Order.DELIVERED)) {
                deliveredOrders++;
            }
        }

        // sales of today = paid orders only
        
        double salesToday = 0;
        for (Order o : store.getOrdersOnDate(LocalDate.now())) {
            if (o.getPaymentStatus().equals(Order.PAID)) {
                salesToday += o.getAmountDue();
            }
        }

        // total quantity of containers ordered / ceiling of available containers
        
        showKpi(containerValue, containerBar, containerPercent,
                containersOrdered + " / " + settings.getTotalContainers(),
                containersOrdered, settings.getTotalContainers());

        // delivered orders / orders in the ORDER LIST
        
        showKpi(deliveryValue, deliveryBar, deliveryPercent,
                deliveredOrders + " / " + totalOrders,
                deliveredOrders, totalOrders);

        // sales of today / daily sales quota
        
        showKpi(salesValue, salesBar, salesPercent,
                String.format("\u20B1%,.0f / \u20B1%,.0f", salesToday, settings.getDailySalesQuota()),
                salesToday, settings.getDailySalesQuota());
    }

    private void showKpi(JLabel valueLabel, JProgressBar bar, JLabel percentLabel,
                         String text, double current, double target) {
        int percent = 0;
        if (target > 0) {
            percent = (int) Math.round(current / target * 100);
        }
        if (percent > 100) {
            percent = 100;
        }
        valueLabel.setText(text);
        bar.setValue(percent);
        percentLabel.setText(percent + " %");
    }

    private void refreshCustomerTable() {
        customerModel.setRowCount(0);
        for (Customer c : store.getCustomers()) {
            customerModel.addRow(new Object[]{c.getName(), c.getAddress(), c.getContactNo(), ""});
        }
    }

 
    // DRAWS the pencil (edit) and X (delete) icons inside the ACTION column
   

    private static class ActionCellRenderer implements TableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            JPanel panel = new JPanel(new GridLayout(1, 2));
            panel.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            panel.add(new JLabel(UIHelper.createPencilIcon(), SwingConstants.CENTER));
            panel.add(new JLabel(UIHelper.createCrossIcon(), SwingConstants.CENTER));
            return panel;
        }
    }
}
