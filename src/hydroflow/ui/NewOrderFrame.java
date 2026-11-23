package hydroflow.ui;

import hydroflow.data.DataStore;
import hydroflow.model.Billing;
import hydroflow.model.Customer;
import hydroflow.model.Order;
import hydroflow.model.Settings;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

@SuppressWarnings("serial")
public class NewOrderFrame extends JFrame {

    private final DataStore store = DataStore.getInstance();
    private final Settings settings = store.getSettings();
    private final Runnable onSaved;   // refreshes the ORDER LIST

    private final JComboBox<String> typeBox = new JComboBox<>(new String[]{Order.DELIVERY, Order.WALK_IN});
    private final JComboBox<Customer> customerBox = new JComboBox<>();
    private final JComboBox<String> productBox = new JComboBox<>();
    private final JSpinner quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
    private final JSpinner emptiesSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 999, 1));
    private final JTextField deliveryFeeField = new JTextField();
    private final JTextField discountField = new JTextField("0");
    private final JTextField remarksField = new JTextField();

    private final JLabel salesLabel = new JLabel();     // water + delivery - discount
    private final JLabel dueLabel = new JLabel();       // amount due
    private final JLabel depositLabel = new JLabel();   // deposit + total to pay

    public NewOrderFrame(Runnable onSaved) {
        this.onSaved = onSaved;

        setTitle("New Order");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(460, 720);
        setResizable(false);
        setLocationRelativeTo(null);

        fillCustomerBox();
        fillProductBox();
        deliveryFeeField.setEditable(false);   // computed automatically

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(12, 15, 5, 15));
        addField(form, "Order type:", typeBox);
        addField(form, "Customer:", customerBox);
        addField(form, "Water type / Container type:", productBox);
        addField(form, "Quantity (gallons):", quantitySpinner);
        addField(form, "Empty containers returned (swap):", emptiesSpinner);
        addField(form, "Delivery fee (PHP, waived for walk-in):", deliveryFeeField);
        addField(form, "Discount (PHP):", discountField);
        addField(form, "Remarks (optional):", remarksField);

        for (JLabel label : new JLabel[]{salesLabel, dueLabel, depositLabel}) {
            label.setFont(new Font(UIHelper.FONT, Font.BOLD, 13));
            label.setForeground(UIHelper.DARK_BLUE);
            label.setAlignmentX(Component.LEFT_ALIGNMENT);
            form.add(label);
            form.add(Box.createVerticalStrut(4));
        }

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

        // recompute the preview whenever the user changes something
        
        typeBox.addActionListener(e -> updatePreview());
        productBox.addActionListener(e -> updatePreview());
        quantitySpinner.addChangeListener(e -> updatePreview());
        emptiesSpinner.addChangeListener(e -> updatePreview());
        discountField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updatePreview(); }
            public void removeUpdate(DocumentEvent e) { updatePreview(); }
            public void changedUpdate(DocumentEvent e) { updatePreview(); }
        });
        updatePreview();
    }


    // setting up the form


    private void fillCustomerBox() {
        for (Customer c : store.getCustomers()) {
            customerBox.addItem(c);
        }
    }

    // 4 choices: Alkaline/Mineral x Round Cylinder/Slim Rectangular, each with its corresponding price
    private void fillProductBox() {
        for (String water : Settings.WATER_TYPES) {
            for (String container : Settings.CONTAINER_TYPES) {
                double price = settings.getPrice(water, container);
                productBox.addItem(water + " - " + container + "  (PHP " + String.format("%.2f", price) + ")");
            }
        }
    }

    private void addField(JPanel form, String labelText, JComponent field) {
        JLabel label = new JLabel(labelText);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        field.setPreferredSize(new Dimension(300, 30));
        form.add(label);
        form.add(Box.createVerticalStrut(3));
        form.add(field);
        form.add(Box.createVerticalStrut(8));
    }


    // reading the form

    
    // WATER type of the selected choice
    private String getSelectedWater() {
        return Settings.WATER_TYPES[productBox.getSelectedIndex() / 2];
    }

    // CONTAINER type of the selected choice (even index = Round Cylinder, odd = Slim Rectangular)
    
    private String getSelectedContainer() {
        return Settings.CONTAINER_TYPES[productBox.getSelectedIndex() % 2];
    }

    private double readDiscount() {
        try {
            return Double.parseDouble(discountField.getText().trim());
        } catch (NumberFormatException ex) {
            return 0;
        }
    }


    // live computation


    private void updatePreview() {
        String orderType = (String) typeBox.getSelectedItem();
        int qty = (Integer) quantitySpinner.getValue();
        int empties = (Integer) emptiesSpinner.getValue();

        double price = settings.getPrice(getSelectedWater(), getSelectedContainer());
        double deliveryFee = Billing.computeDeliveryFee(orderType, qty, settings.getDeliveryFee());
        double deposit = Billing.computeDeposit(qty, empties, settings.getDepositPerContainer());
        double discount = readDiscount();
        double amountDue = Billing.computeAmountDue(price, qty, deliveryFee, discount);

        deliveryFeeField.setText(String.format("%.2f", deliveryFee));
        salesLabel.setText(String.format("Water %.2f + delivery %.2f - discount %.2f", price * qty, deliveryFee, discount));
        dueLabel.setText(String.format("AMT DUE: %.2f", amountDue));
        depositLabel.setText(String.format("Deposit (%d container(s)): %.2f   |   Total to pay: %.2f",
                Math.max(qty - empties, 0), deposit, amountDue + deposit));
    }


    // OK button


    private void save() {
        Customer customer = (Customer) customerBox.getSelectedItem();
        if (customer == null) {
            UIHelper.showWarning(this, "There is no customer yet. Add a customer on the Dashboard first.");
            return;
        }

        int qty = (Integer) quantitySpinner.getValue();
        int empties = (Integer) emptiesSpinner.getValue();
        if (empties > qty) {
            UIHelper.showWarning(this, "Empty containers returned cannot be more than the quantity.");
            return;
        }

        double discount;
        try {
            discount = Double.parseDouble(discountField.getText().trim());
        } catch (NumberFormatException ex) {
            UIHelper.showWarning(this, "Please enter a valid discount (example: 0 or 5.00).");
            return;
        }

        String orderType = (String) typeBox.getSelectedItem();
        double price = settings.getPrice(getSelectedWater(), getSelectedContainer());
        double deliveryFee = Billing.computeDeliveryFee(orderType, qty, settings.getDeliveryFee());
        if (discount < 0 || discount > price * qty + deliveryFee) {
            UIHelper.showWarning(this, "The discount cannot be negative or higher than the amount due.");
            return;
        }

        Order order = store.buildOrder(customer, orderType, getSelectedWater(), getSelectedContainer(),
                qty, empties, discount, remarksField.getText().trim());
        store.addOrder(order);   // walk-in orders become Delivered right away

        onSaved.run();
        dispose();
    }
}
