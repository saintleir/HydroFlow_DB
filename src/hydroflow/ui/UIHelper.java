package hydroflow.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

// shared colors and small helper methods used by every window so the code of each window stays short
public class UIHelper {

    // colors
    public static final Color DARK_BLUE = new Color(47, 85, 151);
    public static final Color SIDEBAR_BLUE = new Color(0, 112, 192);
    public static final Color LIGHT_BLUE_BG = new Color(205, 235, 243);
    public static final Color GRAY_BG = new Color(229, 229, 229);
    public static final Color BLUE = new Color(68, 114, 196);
    public static final Color LIGHT_BLUE = new Color(91, 155, 213);
    public static final Color GREEN = new Color(84, 130, 53);
    public static final Color LIGHT_GREEN = new Color(112, 173, 71);
    public static final Color YELLOW = new Color(255, 192, 0);
    public static final Color RED = new Color(187, 27, 27);
    public static final Color PURPLE = new Color(112, 48, 160);
    public static final Color KPI_BOX_BG = new Color(222, 235, 247);
    public static final Color KPI_TEXT = new Color(47, 85, 151);

    public static final String FONT = "Segoe UI";

    // buttons

    public static JButton createButton(String text, Color background) {
        JButton button = new JButton(text);
        button.setFont(new Font(FONT, Font.BOLD, 13));
        button.setBackground(background);
        button.setForeground(Color.WHITE);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(170, 38));
        return button;
    }

    // headers

    // colored title bar ("ORDERS", "SALES RECORD", ...) shown at the top of a window
    public static JPanel createHeader(String text, Color background) {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(background);
        header.setPreferredSize(new Dimension(0, 55));
        JLabel title = new JLabel(text, SwingConstants.CENTER);
        title.setFont(new Font(FONT, Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.CENTER);
        return header;
    }

    // tables

    // a table model where the user cannot type inside the cells
    
    public static DefaultTableModel createReadOnlyModel(String[] columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    public static void styleTable(JTable table) {
        table.setFont(new Font(FONT, Font.PLAIN, 13));
        table.setRowHeight(28);
        table.getTableHeader().setFont(new Font(FONT, Font.BOLD, 13));
        table.getTableHeader().setReorderingAllowed(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    // sets the width of each column in order
    
    public static void setColumnWidths(JTable table, int[] widths) {
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
    }

    // KPI box (used by SALES RECORD and EMPTIES LEDGER)

    public static JPanel createKpiBox(String title, JLabel valueLabel) {
        JPanel box = new JPanel(new GridLayout(2, 1, 0, 5));
        box.setBackground(KPI_BOX_BG);
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BLUE, 2), new EmptyBorder(12, 20, 12, 20)));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font(FONT, Font.BOLD, 14));

        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        valueLabel.setFont(new Font(FONT, Font.BOLD, 18));
        valueLabel.setForeground(KPI_TEXT);

        box.add(titleLabel);
        box.add(valueLabel);
        return box;
    }

    // icons for the edit / delete buttons of the CUSTOMER LIST

    public static Icon createPencilIcon() {
        return new Icon() {
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.BLACK);
                g2.setStroke(new BasicStroke(4f));
                g2.drawLine(x + 5, y + 11, x + 13, y + 3);          // body of the pencil
                g2.fillPolygon(new int[]{x + 1, x + 4, x + 7},       // pencil tip
                               new int[]{y + 15, y + 9, y + 12}, 3);
                g2.dispose();
            }
            public int getIconWidth() { return 16; }
            public int getIconHeight() { return 16; }
        };
    }

    public static Icon createCrossIcon() {
        return new Icon() {
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.BLACK);
                g2.setStroke(new BasicStroke(3f));
                g2.drawLine(x + 2, y + 2, x + 13, y + 13);
                g2.drawLine(x + 13, y + 2, x + 2, y + 13);
                g2.dispose();
            }
            public int getIconWidth() { return 16; }
            public int getIconHeight() { return 16; }
        };
    }

    // formatting

    public static String money(double value) {
        return String.format("PHP %,.2f", value);
    }

    // navigation

    // closes the current window and opens the DASHBOARD (used by every BACK button)
    
    public static void goToDashboard(JFrame currentFrame) {
        currentFrame.dispose();
        new DashboardFrame().setVisible(true);
    }

    public static void logout(JFrame currentFrame) {
        int choice = JOptionPane.showConfirmDialog(currentFrame, "Are you sure you want to log out?",
                "Confirm Logout", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            currentFrame.dispose();
            new LoginFrame().setVisible(true);
        }
    }

    public static void showWarning(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Please check", JOptionPane.WARNING_MESSAGE);
    }
}
