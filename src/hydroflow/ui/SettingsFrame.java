package hydroflow.ui;

import hydroflow.data.DataStore;
import hydroflow.model.Settings;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

@SuppressWarnings("serial")
public class SettingsFrame extends JFrame {

    private final Settings settings = DataStore.getInstance().getSettings();
    private final Runnable onSaved;

    private final JTextField deliveryFeeField = new JTextField();
    private final JTextField depositField = new JTextField();
    private final JTextField alkalineRoundField = new JTextField();
    private final JTextField alkalineSlimField = new JTextField();
    private final JTextField mineralRoundField = new JTextField();
    private final JTextField mineralSlimField = new JTextField();
    private final JTextField containersField = new JTextField();
    private final JTextField quotaField = new JTextField();

    public SettingsFrame(Runnable onSaved) {
        this.onSaved = onSaved;

        setTitle("Prices & Settings");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(500, 410);
        setResizable(false);
        setLocationRelativeTo(null);

        // show the current values
        
        deliveryFeeField.setText(String.format("%.2f", settings.getDeliveryFee()));
        depositField.setText(String.format("%.2f", settings.getDepositPerContainer()));
        alkalineRoundField.setText(String.format("%.2f", settings.getAlkalineRound()));
        alkalineSlimField.setText(String.format("%.2f", settings.getAlkalineSlim()));
        mineralRoundField.setText(String.format("%.2f", settings.getMineralRound()));
        mineralSlimField.setText(String.format("%.2f", settings.getMineralSlim()));
        containersField.setText(String.valueOf(settings.getTotalContainers()));
        quotaField.setText(String.format("%.2f", settings.getDailySalesQuota()));

        JPanel form = new JPanel(new GridLayout(8, 2, 10, 8));
        form.setBorder(new EmptyBorder(15, 15, 10, 15));
        addRow(form, "Default delivery fee per gallon (PHP):", deliveryFeeField);
        addRow(form, "Deposit per container (PHP):", depositField);
        addRow(form, "Price: Alkaline - Round Cylinder", alkalineRoundField);
        addRow(form, "Price: Alkaline - Slim Rectangular", alkalineSlimField);
        addRow(form, "Price: Mineral - Round Cylinder", mineralRoundField);
        addRow(form, "Price: Mineral - Slim Rectangular", mineralSlimField);
        addRow(form, "Total available containers:", containersField);
        addRow(form, "Daily sales quota (PHP):", quotaField);

        JButton okButton = new JButton("OK");
        okButton.addActionListener(e -> save());
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        buttons.add(okButton);
        buttons.add(cancelButton);

        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(okButton);
    }

    private void addRow(JPanel form, String labelText, JTextField field) {
        form.add(new JLabel(labelText));
        form.add(field);
    }

    private void save() {
        try {
            double deliveryFee = Double.parseDouble(deliveryFeeField.getText().trim());
            double deposit = Double.parseDouble(depositField.getText().trim());
            double alkalineRound = Double.parseDouble(alkalineRoundField.getText().trim());
            double alkalineSlim = Double.parseDouble(alkalineSlimField.getText().trim());
            double mineralRound = Double.parseDouble(mineralRoundField.getText().trim());
            double mineralSlim = Double.parseDouble(mineralSlimField.getText().trim());
            int containers = Integer.parseInt(containersField.getText().trim());
            double quota = Double.parseDouble(quotaField.getText().trim());

            if (deliveryFee < 0 || deposit < 0 || alkalineRound < 0 || alkalineSlim < 0
                    || mineralRound < 0 || mineralSlim < 0 || containers < 0 || quota < 0) {
                UIHelper.showWarning(this, "Values cannot be negative.");
                return;
            }

            settings.setDeliveryFee(deliveryFee);
            settings.setDepositPerContainer(deposit);
            settings.setAlkalineRound(alkalineRound);
            settings.setAlkalineSlim(alkalineSlim);
            settings.setMineralRound(mineralRound);
            settings.setMineralSlim(mineralSlim);
            settings.setTotalContainers(containers);
            settings.setDailySalesQuota(quota);

            DataStore.getInstance().saveSettings();   // save to MySQL
            onSaved.run();
            dispose();
        } catch (NumberFormatException ex) {
            UIHelper.showWarning(this, "Please enter valid numbers in every field\n"
                    + "(total available containers must be a whole number).");
        }
    }
}
