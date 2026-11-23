package hydroflow.ui;

import hydroflow.data.DataStore;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

@SuppressWarnings("serial")
public class LoginFrame extends JFrame {

    private final JTextField idField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final JButton showButton = new JButton("Show");
    private final char hiddenChar = passwordField.getEchoChar();   // the dot that hides the characters
    private boolean passwordShown = false;

    public LoginFrame() {
        setTitle("HydroFlow Water Refilling Station \u2013 Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        JPanel background = new JPanel(new GridBagLayout());
        background.setBackground(UIHelper.LIGHT_BLUE_BG);
        background.add(buildContent());
        setContentPane(background);
    }

    // title texts + the white login card, stacked from top to bottom
    
    private JPanel buildContent() {
        JLabel logo = new JLabel("HYDROFLOW", SwingConstants.CENTER);
        logo.setFont(new Font(UIHelper.FONT, Font.BOLD, 36));
        logo.setForeground(new Color(46, 117, 182));
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel(
                "<html><div style='text-align:center'>WATER REFILLING STATION SALES<br>AND BILLING SYSTEM</div></html>",
                SwingConstants.CENTER);
        subtitle.setFont(new Font(UIHelper.FONT, Font.BOLD, 20));
        subtitle.setForeground(new Color(46, 117, 182));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitle.setBorder(new EmptyBorder(15, 0, 30, 0));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.add(logo);
        content.add(subtitle);
        content.add(buildLoginCard());
        return content;
    }

    private JPanel buildLoginCard() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(46, 117, 182)), new EmptyBorder(25, 30, 25, 30)));
        card.setPreferredSize(new Dimension(420, 220));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // row 0: ID
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.weightx = 0;
        card.add(boldLabel("ID"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.gridwidth = 2;
        card.add(idField, gbc);

        // row 1: PW + show button
        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.gridwidth = 1;
        card.add(boldLabel("PW"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        card.add(passwordField, gbc);
        gbc.gridx = 2;
        gbc.weightx = 0;
        showButton.setFocusPainted(false);
        showButton.addActionListener(e -> togglePassword());
        card.add(showButton, gbc);

        // row 2: LOGIN button
        JButton loginButton = UIHelper.createButton("LOGIN", UIHelper.BLUE);
        loginButton.addActionListener(e -> doLogin());
        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        gbc.insets = new Insets(20, 5, 5, 5);
        card.add(loginButton, gbc);

        getRootPane().setDefaultButton(loginButton);   // pressing Enter = click LOGIN
        return card;
    }

    private JLabel boldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font(UIHelper.FONT, Font.BOLD, 14));
        return label;
    }

    // switches between hidden characters and visible characters
    private void togglePassword() {
        passwordShown = !passwordShown;
        if (passwordShown) {
            passwordField.setEchoChar((char) 0);   // 0 = show the real characters
            showButton.setText("Hide");
        } else {
            passwordField.setEchoChar(hiddenChar);
            showButton.setText("Show");
        }
    }

    private void doLogin() {
        String id = idField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (id.isEmpty() || password.isEmpty()) {
            UIHelper.showWarning(this, "Please enter both ID and PW.");
            return;
        }

        if (DataStore.getInstance().login(id, password)) {
            dispose();
            new DashboardFrame().setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Invalid ID or PW.", "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}
