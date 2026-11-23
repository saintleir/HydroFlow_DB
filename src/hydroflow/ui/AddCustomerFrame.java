package hydroflow.ui;

import hydroflow.data.DataStore;
import hydroflow.model.Customer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

// popup form for a new customer (it is also used to edit a customer)

@SuppressWarnings("serial")
public class AddCustomerFrame extends JFrame {

    private final DataStore store = DataStore.getInstance();
    private final Customer customerToEdit;   // null = adding a new customer
    private final Runnable onSaved;          // refreshes the table of the DASHBOARD

    private final JTextField nameField = new JTextField();
    private final JTextField addressField = new JTextField();
    private final JTextField contactField = new JTextField();

    public AddCustomerFrame(Customer customerToEdit, Runnable onSaved) {
        this.customerToEdit = customerToEdit;
        this.onSaved = onSaved;

        setTitle(customerToEdit == null ? "Add Customer" : "Edit Customer");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(350, 300);
        setResizable(false);
        setLocationRelativeTo(null);

        // when editing, show the old values
        
        if (customerToEdit != null) {
            nameField.setText(customerToEdit.getName());
            addressField.setText(customerToEdit.getAddress());
            contactField.setText(customerToEdit.getContactNo());
        }

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(15, 15, 10, 15));
        addField(form, "Customer Name:", nameField);
        addField(form, "Address:", addressField);
        addField(form, "Contact Number:", contactField);

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

    // adds a label and a text field under each other
    
    private void addField(JPanel form, String labelText, JTextField field) {
        JLabel label = new JLabel(labelText);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        form.add(label);
        form.add(Box.createVerticalStrut(3));
        form.add(field);
        form.add(Box.createVerticalStrut(10));
    }

    private void save() {
        String name = nameField.getText().trim();
        String address = addressField.getText().trim();
        String contact = contactField.getText().trim();

        if (name.isEmpty() || address.isEmpty()) {
            UIHelper.showWarning(this, "Customer name and address are required.");
            return;
        }
        if (!contact.isEmpty() && !contact.matches("[0-9]+")) {
            UIHelper.showWarning(this, "Contact number must contain digits only.");
            return;
        }

        if (customerToEdit == null) {
            store.addCustomer(name, address, contact);
        } else {
            store.updateCustomer(customerToEdit, name, address, contact);
        }
        onSaved.run();
        dispose();
    }
}
