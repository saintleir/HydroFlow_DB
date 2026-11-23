package hydroflow.ui;

import hydroflow.data.DataStore;
import hydroflow.model.LedgerEntry;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class EmptiesLedgerFrame extends JFrame {

    private final DataStore store = DataStore.getInstance();

    private final JComboBox<String> containerBox =
            new JComboBox<>(new String[]{"All", "Round Cylinder", "Slim Rectangular"});
    private final JTextField searchField = new JTextField(18);

    private final JLabel slimOwedLabel = new JLabel();
    private final JLabel roundOwedLabel = new JLabel();
    private final JLabel depositsLabel = new JLabel();

    private final DefaultTableModel tableModel = UIHelper.createReadOnlyModel(new String[]{
            "ORDER NO", "DATE", "CUSTOMER", "WATER / CONTAINER", "OUT (QTY)", "EMPTIES IN", "OWED", "DEPOSIT"});
    private final JTable table = new JTable(tableModel);

    // the ledger rows that are currently shown in the same order as the table rows
    
    private final List<LedgerEntry> shownEntries = new ArrayList<>();

    public EmptiesLedgerFrame() {
        setTitle("HydroFlow Water Refilling Station \u2013 Empties Ledger");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1050, 650);
        setMinimumSize(new Dimension(900, 500));
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());
        add(buildTopArea(), BorderLayout.NORTH);
        add(buildTable(), BorderLayout.CENTER);
        add(buildButtonBar(), BorderLayout.SOUTH);

        refresh();
    }


    // building the screen


    private JPanel buildTopArea() {
    	
        // container dropdown + search
    	
        JButton searchButton = UIHelper.createButton("Search", UIHelper.LIGHT_BLUE);
        searchButton.setPreferredSize(new Dimension(100, 30));
        searchButton.addActionListener(e -> loadTable());
        searchField.addActionListener(e -> loadTable());
        containerBox.addActionListener(e -> loadTable());   // dropdown filter of the container type

        JLabel containerLabel = new JLabel("Container");
        containerLabel.setForeground(Color.WHITE);
        containerLabel.setFont(new Font(UIHelper.FONT, Font.BOLD, 13));

        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        filterBar.setBackground(new Color(31, 78, 121));
        filterBar.add(containerLabel);
        filterBar.add(containerBox);
        filterBar.add(searchField);
        filterBar.add(searchButton);

        // KPI panels
        
        JPanel kpiPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 15));
        kpiPanel.setBackground(Color.WHITE);
        kpiPanel.add(sized(UIHelper.createKpiBox("SLIM RECTANGULAR OWED:", slimOwedLabel)));
        kpiPanel.add(sized(UIHelper.createKpiBox("ROUND CYLINDER OWED:", roundOwedLabel)));
        kpiPanel.add(sized(UIHelper.createKpiBox("TOTAL DEPOSITS HELD:", depositsLabel)));

        JPanel top = new JPanel(new BorderLayout());
        top.add(UIHelper.createHeader("EMPTIES LEDGER", new Color(31, 78, 121)), BorderLayout.NORTH);
        JPanel below = new JPanel(new BorderLayout());
        below.add(filterBar, BorderLayout.NORTH);
        below.add(kpiPanel, BorderLayout.CENTER);
        top.add(below, BorderLayout.CENTER);
        return top;
    }

    private JPanel sized(JPanel box) {
        box.setPreferredSize(new Dimension(260, 90));
        return box;
    }

    private JPanel buildTable() {
        UIHelper.styleTable(table);
        UIHelper.setColumnWidths(table, new int[]{80, 100, 140, 200, 90, 100, 70, 100});

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UIHelper.GRAY_BG);
        panel.setBorder(new EmptyBorder(15, 30, 5, 30));
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildButtonBar() {
        JButton returnButton = UIHelper.createButton("RETURN EMPTIES ONLY", UIHelper.YELLOW);
        returnButton.setPreferredSize(new Dimension(210, 38));
        returnButton.addActionListener(e -> new ReturnEmptiesFrame(this::refresh).setVisible(true));

        JButton refundButton = UIHelper.createButton("REFUND DEPOSIT", UIHelper.PURPLE);
        refundButton.setPreferredSize(new Dimension(190, 38));
        refundButton.addActionListener(e -> refundDeposit());

        JButton backButton = UIHelper.createButton("BACK", UIHelper.BLUE);
        backButton.addActionListener(e -> UIHelper.goToDashboard(this));

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 15));
        bar.setBackground(UIHelper.GRAY_BG);
        bar.add(returnButton);
        bar.add(refundButton);
        bar.add(backButton);
        return bar;
    }


    // DATA !!

    // updates the KPI panels and the table
    
    private void refresh() {
    	
        // these 3 values always use ALL ledger rows (the filter only affects the table)
    	
        slimOwedLabel.setText(String.valueOf(store.getTotalOwed("Slim Rectangular")));
        roundOwedLabel.setText(String.valueOf(store.getTotalOwed("Round Cylinder")));
        depositsLabel.setText(UIHelper.money(store.getTotalDepositsHeld()));
        loadTable();
    }

    private void loadTable() {
        String containerFilter = (String) containerBox.getSelectedItem();
        String keyword = searchField.getText().trim().toLowerCase();
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        tableModel.setRowCount(0);
        shownEntries.clear();

        for (LedgerEntry e : store.getLedger()) {
            boolean containerMatches = containerFilter.equals("All")
                    || e.getContainerType().equals(containerFilter);
            boolean searchMatches = keyword.isEmpty()
                    || e.getCustomer().getName().toLowerCase().contains(keyword)
                    || String.valueOf(e.getOrderNo()).equals(keyword);
            if (!containerMatches || !searchMatches) {
                continue;
            }

            shownEntries.add(e);
            tableModel.addRow(new Object[]{
                    e.getOrderNo(),
                    e.getDate().format(dateFormat),
                    e.getCustomer().getName(),
                    e.getWaterType() + " / " + e.getContainerType(),
                    e.getQtyOut(),
                    e.getEmptiesIn(),
                    e.getOwed(),
                    String.format("%.2f", e.getDeposit())});
        }
    }

 
    // REFUND DEPOSIT !!!


    private void refundDeposit() {
        int row = table.getSelectedRow();
        if (row < 0) {
            UIHelper.showWarning(this, "Please select a ledger row first.");
            return;
        }
        LedgerEntry entry = shownEntries.get(row);

        if (entry.getOwed() > 0) {
            UIHelper.showWarning(this, "The customer must return all borrowed containers first.\n"
                    + "Still owed: " + entry.getOwed());
            return;
        }
        if (entry.getDeposit() <= 0) {
            UIHelper.showWarning(this, "There is no deposit to refund for this row.");
            return;
        }

        int choice = JOptionPane.showConfirmDialog(this,
                "Hand back " + UIHelper.money(entry.getDeposit()) + " to " + entry.getCustomer().getName() + "?",
                "Refund Deposit", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            store.refundDeposit(entry);
            refresh();
        }
    }
}
