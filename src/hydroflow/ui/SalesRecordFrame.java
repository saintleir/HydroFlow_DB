package hydroflow.ui;

import hydroflow.data.DataStore;
import hydroflow.model.Order;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@SuppressWarnings("serial")
public class SalesRecordFrame extends JFrame {

    private final DataStore store = DataStore.getInstance();

    private final JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
    private final JLabel totalSalesLabel = new JLabel();
    private final JLabel gallonsLabel = new JLabel();
    private final JLabel unpaidLabel = new JLabel();

    private final DefaultTableModel tableModel = UIHelper.createReadOnlyModel(new String[]{
            "ORDER NO", "TIME", "CUSTOMER", "TYPE", "WATER / CONTAINER", "GALLONS",
            "SALES AMOUNT", "DEPOSIT", "PAID", "CHANGE"});
    private final JTable table = new JTable(tableModel);

    public SalesRecordFrame() {
        setTitle("HydroFlow Water Refilling Station \u2013 Sales Record");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 650);
        setMinimumSize(new Dimension(850, 500));
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());
        add(buildTopArea(), BorderLayout.NORTH);
        add(buildTable(), BorderLayout.CENTER);
        add(buildBackButton(), BorderLayout.SOUTH);

        loadRecords();
    }


    // building the screen !!


    private JPanel buildTopArea() {
    	
        // date selection: spinner + Today button
    	
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));
        dateSpinner.setPreferredSize(new Dimension(130, 30));
        dateSpinner.addChangeListener(e -> loadRecords());

        JButton todayButton = UIHelper.createButton("Today", new Color(46, 117, 182));
        todayButton.setPreferredSize(new Dimension(90, 30));
        todayButton.addActionListener(e -> dateSpinner.setValue(new Date()));

        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        datePanel.setOpaque(false);
        datePanel.add(new JLabel("Select Date:"));
        datePanel.add(dateSpinner);
        datePanel.add(todayButton);

        // KPI panels
        
        JPanel kpiPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 50, 5));
        kpiPanel.setOpaque(false);
        kpiPanel.add(sized(UIHelper.createKpiBox("TOTAL SALES", totalSalesLabel)));
        kpiPanel.add(sized(UIHelper.createKpiBox("GALLONS SOLD", gallonsLabel)));
        kpiPanel.add(sized(UIHelper.createKpiBox("UNPAID", unpaidLabel)));

        JPanel filterArea = new JPanel(new BorderLayout());
        filterArea.setBackground(Color.WHITE);
        filterArea.setBorder(new EmptyBorder(0, 0, 15, 0));
        filterArea.add(datePanel, BorderLayout.NORTH);
        filterArea.add(kpiPanel, BorderLayout.CENTER);

        JPanel top = new JPanel(new BorderLayout());
        top.add(UIHelper.createHeader("SALES RECORD", new Color(147, 190, 229)), BorderLayout.NORTH);
        top.add(filterArea, BorderLayout.CENTER);
        return top;
    }

    private JPanel sized(JPanel box) {
        box.setPreferredSize(new Dimension(200, 90));
        return box;
    }

    private JPanel buildTable() {
        UIHelper.styleTable(table);
        UIHelper.setColumnWidths(table, new int[]{80, 80, 130, 80, 200, 80, 120, 90, 90, 90});

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UIHelper.GRAY_BG);
        panel.setBorder(new EmptyBorder(15, 30, 10, 30));
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildBackButton() {
        JButton backButton = UIHelper.createButton("BACK", UIHelper.BLUE);
        backButton.addActionListener(e -> UIHelper.goToDashboard(this));

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 30, 12));
        bar.setBackground(UIHelper.GRAY_BG);
        bar.add(backButton);
        return bar;
    }


    // loading the records of the selected date !!


    private LocalDate getSelectedDate() {
        Date date = (Date) dateSpinner.getValue();
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private void loadRecords() {
        DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm:ss");

        double totalSales = 0;   // SUM(SALES AMOUNT) of Paid orders
        int gallonsSold = 0;     // SUM(GALLONS) of all active orders
        double unpaid = 0;       // SUM(SALES AMOUNT + DEPOSIT - PAID) of Unpaid orders

        tableModel.setRowCount(0);

        // getOrdersOnDate() already leaves out the Cancelled orders
        
        for (Order o : store.getOrdersOnDate(getSelectedDate())) {
            gallonsSold += o.getQuantity();
            if (o.getPaymentStatus().equals(Order.PAID)) {
                totalSales += o.getAmountDue();
            } else {
                unpaid += o.getAmountDue() + o.getDeposit() - o.getAmountPaid();
            }

            tableModel.addRow(new Object[]{
                    o.getOrderNo(),
                    o.getOrderDateTime().format(timeFormat),
                    o.getCustomer().getName(),
                    o.getOrderType(),
                    o.getWaterType() + " / " + o.getContainerType(),
                    o.getQuantity(),
                    String.format("%.2f", o.getAmountDue()),
                    String.format("%.2f", o.getDeposit()),
                    String.format("%.2f", o.getAmountPaid()),
                    String.format("%.2f", o.getChange())});
        }

        totalSalesLabel.setText(UIHelper.money(totalSales));
        gallonsLabel.setText(String.valueOf(gallonsSold));
        unpaidLabel.setText(UIHelper.money(unpaid));
    }
}
