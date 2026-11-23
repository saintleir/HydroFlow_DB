package hydroflow.ui;

import hydroflow.data.DataStore;
import hydroflow.model.Customer;
import hydroflow.model.Settings;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

@SuppressWarnings("serial")
public class ReturnEmptiesFrame extends JFrame {

    private final DataStore store = DataStore.getInstance();
    private final Runnable onSaved;   // refreshes the EMPTIES LEDGER

    private final JComboBox<Customer> customerBox = new JComboBox<>();
    private final JComboBox<String> containerBox = new JComboBox<>(Settings.CONTAINER_TYPES);
    private final JSpinner quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));

    public ReturnEmptiesFrame(Runnable onSaved) {
        this.onSaved = onSaved;

        setTitle("Return Empties Only");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(380, 300);
        setResizable(false);
        setLocationRelativeTo(null);

        for (Customer c : store.getCustomers()) {
            customerBox.addItem(c);
        }

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(15, 15, 5, 15));
        addField(form, "Customer:", customerBox);
        addField(form, "Container type:", containerBox);
        addField(form, "Number of empties returned:", quantitySpinner);

        JButton okButton = new JButton("OK");
        okButton.addActionListener(e -> save());
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        buttons.add(okButton);
        buttons.add(cancelButton);

        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(okButton);
    }

    private void addField(JPanel form, String labelText, JComponent field) {
        JLabel label = new JLabel(labelText);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        form.add(label);
        form.add(Box.createVerticalStrut(3));
        form.add(field);
        form.add(Box.createVerticalStrut(10));
    }

    private void save() {
        Customer customer = (Customer) customerBox.getSelectedItem();
        if (customer == null) {
            UIHelper.showWarning(this, "There is no customer yet.");
            return;
        }

        String containerType = (String) containerBox.getSelectedItem();
        int qty = (Integer) quantitySpinner.getValue();
        int owed = store.getOwedByCustomer(customer, containerType);

        if (owed == 0) {
            UIHelper.showWarning(this, customer.getName() + " does not owe any " + containerType + " container.");
            return;
        }
        if (qty > owed) {
            UIHelper.showWarning(this, customer.getName() + " only owes " + owed + " " + containerType
                    + " container(s).");
            return;
        }

        store.returnEmpties(customer, containerType, qty);
        onSaved.run();
        dispose();
    }
}
