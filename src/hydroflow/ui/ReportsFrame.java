package hydroflow.ui;

import hydroflow.data.DataStore;
import hydroflow.model.Order;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;


@SuppressWarnings("serial")
public class ReportsFrame extends JFrame {

    private static final int TAB_WATER = 0;     // positions of the nested chart tabs
    private static final int TAB_MONTHLY = 1;
    private static final int TAB_DAILY = 2;

    private final DataStore store = DataStore.getInstance();

    private final JComboBox<String> monthBox = new JComboBox<>();
    private final JSpinner yearSpinner =
            new JSpinner(new SpinnerNumberModel(LocalDate.now().getYear(), 2000, 2100, 1));

    private final DefaultTableModel detailModel = UIHelper.createReadOnlyModel(new String[]{
            "ORDER NO.", "DATE & TIME", "CUSTOMER", "TYPE", "WATER", "CONTAINER", "QTY",
            "DELIVERY FEE", "DISCOUNT", "SALES AMOUNT", "PAYMENT"});
    private final DefaultTableModel summaryModel = UIHelper.createReadOnlyModel(new String[]{"SUMMARY", "VALUE"});

    private final JTabbedPane mainTabs = new JTabbedPane();
    private final JTabbedPane chartTabs = new JTabbedPane();

    public ReportsFrame() {
        setTitle("HydroFlow Water Refilling Station \u2013 Reports");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setMinimumSize(new Dimension(900, 550));
        setLocationRelativeTo(null);

        // month dropdown + year spinner
        
        for (Month m : Month.values()) {
            monthBox.addItem(m.getDisplayName(TextStyle.FULL, Locale.ENGLISH));
        }
        monthBox.setSelectedIndex(LocalDate.now().getMonthValue() - 1);
        yearSpinner.setEditor(new JSpinner.NumberEditor(yearSpinner, "#"));   // 2026 instead of 2,026
        monthBox.addActionListener(e -> refresh());
        yearSpinner.addChangeListener(e -> refresh());

        buildTabs();

        setLayout(new BorderLayout());
        add(buildTopArea(), BorderLayout.NORTH);
        add(mainTabs, BorderLayout.CENTER);
        add(buildBackButton(), BorderLayout.SOUTH);

        refresh();
    }


    // building the screen !!


    private JPanel buildTopArea() {
        JPanel filter = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        filter.setBackground(Color.WHITE);
        filter.add(new JLabel("Month:"));
        filter.add(monthBox);
        filter.add(new JLabel("Year:"));
        filter.add(yearSpinner);

        JPanel top = new JPanel(new BorderLayout());
        top.add(UIHelper.createHeader("SALES REPORTS", UIHelper.DARK_BLUE), BorderLayout.NORTH);
        top.add(filter, BorderLayout.CENTER);
        return top;
    }

    private void buildTabs() {
    	
        // tab 1: detailed report
    	
        JTable detailTable = new JTable(detailModel);
        UIHelper.styleTable(detailTable);
        UIHelper.setColumnWidths(detailTable, new int[]{90, 150, 130, 80, 80, 130, 50, 110, 90, 120, 80});
        detailTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // tab 2: summary report
        
        JTable summaryTable = new JTable(summaryModel);
        UIHelper.styleTable(summaryTable);
        UIHelper.setColumnWidths(summaryTable, new int[]{500, 300});

        // tab 3: charts (3 nested tabs, the chart panels are put in by refreshCharts())
        
        chartTabs.addTab("By Water Type", new JPanel());
        chartTabs.addTab("Monthly Revenue", new JPanel());
        chartTabs.addTab("Daily Revenue", new JPanel());

        mainTabs.addTab("1. Detailed Report", wrap(new JScrollPane(detailTable)));
        mainTabs.addTab("2. Summary Report", wrap(new JScrollPane(summaryTable)));
        mainTabs.addTab("3. Statistics (Chart)", wrap(chartTabs));
    }

    private JPanel wrap(JComponent content) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 15, 10, 15));
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildBackButton() {
        JButton backButton = UIHelper.createButton("BACK", UIHelper.BLUE);
        backButton.addActionListener(e -> UIHelper.goToDashboard(this));

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 30, 10));
        bar.setBackground(Color.WHITE);
        bar.add(backButton);
        return bar;
    }

  
    // filling the tabs for the selected month and year !!


    private void refresh() {
        int month = monthBox.getSelectedIndex() + 1;
        int year = (Integer) yearSpinner.getValue();
        String monthYear = monthBox.getSelectedItem() + " " + year;

        // the same list (non-cancelled orders of the month) feeds all three tabs
        List<Order> orders = store.getActiveOrdersInMonth(month, year);

        fillDetailedReport(orders);
        fillSummaryReport(orders, monthYear);
        refreshCharts(orders, month, year, monthYear);
    }

    // TAB 1: one row for each order
    private void fillDetailedReport(List<Order> orders) {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        detailModel.setRowCount(0);
        for (Order o : orders) {
            detailModel.addRow(new Object[]{
                    o.getOrderNo(),
                    o.getOrderDateTime().format(format),
                    o.getCustomer().getName(),
                    o.getOrderType(),
                    o.getWaterType(),
                    o.getContainerType(),
                    o.getQuantity(),
                    String.format("%.2f", o.getDeliveryFee()),
                    String.format("%.2f", o.getDiscount()),
                    String.format("%.2f", o.getAmountDue()),
                    o.getPaymentStatus()});
        }
    }

    // TAB 2: totals calculated from the same orders
    private void fillSummaryReport(List<Order> orders, String monthYear) {
        double totalRevenue = 0;        // SUM(SALES AMOUNT) where Payment = Paid
        int totalGallons = 0;           // SUM(QTY) of all active orders
        int alkalineGallons = 0;
        double alkalineRevenue = 0;
        int mineralGallons = 0;
        double mineralRevenue = 0;
        int deliveryCount = 0;
        int walkInCount = 0;
        int unpaidCount = 0;            // delivered orders that are still unpaid
        double unpaidAmount = 0;

        for (Order o : orders) {
            totalGallons += o.getQuantity();

            if (o.getPaymentStatus().equals(Order.PAID)) {
                totalRevenue += o.getAmountDue();
            }

            if (o.getWaterType().equals("Alkaline")) {
                alkalineGallons += o.getQuantity();
                alkalineRevenue += o.getAmountDue();
            } else {
                mineralGallons += o.getQuantity();
                mineralRevenue += o.getAmountDue();
            }

            if (o.getOrderType().equals(Order.DELIVERY)) {
                deliveryCount++;
            } else {
                walkInCount++;
            }

            boolean deliveredAndUnpaid = o.getStatus().equals(Order.DELIVERED)
                    && o.getPaymentStatus().equals(Order.UNPAID);
            if (deliveredAndUnpaid) {
                unpaidCount++;
                unpaidAmount += o.getAmountDue() - o.getAmountPaid();
            }
        }

        summaryModel.setRowCount(0);
        summaryModel.addRow(new Object[]{"Total revenue for " + monthYear, UIHelper.money(totalRevenue)});
        summaryModel.addRow(new Object[]{"Total gallons sold", totalGallons});
        summaryModel.addRow(new Object[]{"Alkaline water \u2013 gallons sold / revenue",
                alkalineGallons + " / " + UIHelper.money(alkalineRevenue)});
        summaryModel.addRow(new Object[]{"Mineral water \u2013 gallons sold / revenue",
                mineralGallons + " / " + UIHelper.money(mineralRevenue)});
        summaryModel.addRow(new Object[]{"Delivery orders \u2013 count", deliveryCount});
        summaryModel.addRow(new Object[]{"Walk-in orders \u2013 count", walkInCount});
        summaryModel.addRow(new Object[]{"Unpaid delivered orders \u2013 count / amount due",
                unpaidCount + " / " + UIHelper.money(unpaidAmount)});
    }

    // TAB 3: builds the 3 JFreeChart panels
    
    private void refreshCharts(List<Order> orders, int month, int year, String monthYear) {
        double alkalineSales = 0;
        double mineralSales = 0;
        for (Order o : orders) {
            if (o.getWaterType().equals("Alkaline")) {
                alkalineSales += o.getAmountDue();
            } else {
                mineralSales += o.getAmountDue();
            }
        }

        try {
            chartTabs.setComponentAt(TAB_WATER, ReportCharts.createWaterTypePie(alkalineSales, mineralSales, monthYear));
            chartTabs.setComponentAt(TAB_MONTHLY, ReportCharts.createMonthlyBar(store.getMonthlyRevenue(year), year));
            chartTabs.setComponentAt(TAB_DAILY, ReportCharts.createDailyBar(store.getDailyRevenue(month, year), monthYear));
        } catch (Throwable ex) {
            JLabel warning = new JLabel("JFreeChart jar not found. Add it to the build path (see lib/README.txt).",
                    SwingConstants.CENTER);
            chartTabs.setComponentAt(TAB_WATER, warning);
        }
    }
}
