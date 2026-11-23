package hydroflow.data;

import hydroflow.dao.CustomerDAO;
import hydroflow.dao.LedgerDAO;
import hydroflow.dao.OrderDAO;
import hydroflow.dao.SettingsDAO;
import hydroflow.dao.UserDAO;
import hydroflow.model.Billing;
import hydroflow.model.Customer;
import hydroflow.model.LedgerEntry;
import hydroflow.model.Order;
import hydroflow.model.Settings;

import java.time.LocalDate;
import java.util.List;

// the windows talk ONLY to this class. every method here calls a DAO (MySQL), so the windows never touch SQL

public class DataStore {

    private static DataStore instance;

    // all windows share the same DataStore object
    
    public static DataStore getInstance() {
        if (instance == null) {
            instance = new DataStore();
        }
        return instance;
    }

    private final UserDAO userDao = new UserDAO();
    private final SettingsDAO settingsDao = new SettingsDAO();
    private final CustomerDAO customerDao = new CustomerDAO();
    private final OrderDAO orderDao = new OrderDAO();
    private final LedgerDAO ledgerDao = new LedgerDAO();

    private final Settings settings;   // loaded once from MySQL, saved again by saveSettings()

    private DataStore() {
        settings = settingsDao.load();
    }


    // LOGIN !!!

    public boolean login(String id, String password) {
        return userDao.authenticate(id, password);
    }


    // SETTINGS !!!


    public Settings getSettings() {
        return settings;
    }

    // call this after changing the values of the SETTINGS object
    
    public void saveSettings() {
        settingsDao.save(settings);
    }

    
    // CUSTOMERS !!!


    public List<Customer> getCustomers() {
        return customerDao.findAll();
    }

    public Customer addCustomer(String name, String address, String contactNo) {
        return customerDao.insert(name, address, contactNo);
    }

    public void updateCustomer(Customer c, String name, String address, String contactNo) {
        c.setName(name);
        c.setAddress(address);
        c.setContactNo(contactNo);
        customerDao.update(c);
    }

    // a customer that already has orders cannot be deleted (foreign key in MySQL)
    public boolean deleteCustomer(Customer c) {
        if (customerDao.hasOrders(c.getId())) {
            return false;
        }
        customerDao.delete(c.getId());
        return true;
    }


    // ORDERS !!!


    public List<Order> getOrders() {
        return orderDao.findAll();
    }

    // builds a new order and computes its delivery fee, deposit and amount due from the current SETTINGS
    // not saved yet. call addOrder for that
    
    public Order buildOrder(Customer customer, String orderType, String waterType, String containerType,
                            int qty, int emptiesIn, double discount, String remarks) {
        double price = settings.getPrice(waterType, containerType);
        double deliveryFee = Billing.computeDeliveryFee(orderType, qty, settings.getDeliveryFee());
        double deposit = Billing.computeDeposit(qty, emptiesIn, settings.getDepositPerContainer());
        return new Order(customer, orderType, waterType, containerType, qty, emptiesIn,
                price, deliveryFee, discount, deposit, remarks);
    }

    // saves a new order (walk-in orders are delivered right away)
    
    public void addOrder(Order order) {
        order.setOrderNo(orderDao.insert(order));
        if (order.getOrderType().equals(Order.WALK_IN)) {
            markDelivered(order);
        }
    }

    // updates the status AND adds the order to the EMPTIES LEDGER
    
    public void markDelivered(Order order) {
        LedgerEntry entry = new LedgerEntry(order.getOrderNo(), LocalDate.now(), order.getCustomer(),
                order.getWaterType(), order.getContainerType(),
                order.getQuantity(), order.getEmptiesIn(), order.getDeposit());
        ledgerDao.addDeliveredEntry(entry);
        order.setStatus(Order.DELIVERED);
    }

    public void cancelOrder(Order order) {
        orderDao.updateStatus(order.getOrderNo(), Order.CANCELLED);
        order.setStatus(Order.CANCELLED);
    }

    // Unpaid -> Paid. the amount paid and the change are shown later in the SALES RECORD
    
    public void recordPayment(Order order, double amountPaid) {
        double change = amountPaid - order.getTotalToPay();
        orderDao.updatePayment(order.getOrderNo(), amountPaid, change);
        order.setAmountPaid(amountPaid);
        order.setChange(change);
        order.setPaymentStatus(Order.PAID);
    }

    // used by SALES RECORD: orders on one date that are not cancelled
    
    public List<Order> getOrdersOnDate(LocalDate date) {
        return orderDao.findByDate(date);
    }

    //used by REPORTS: orders in one month that are not cancelled
    
    public List<Order> getActiveOrdersInMonth(int month, int year) {
        return orderDao.findByMonth(month, year);
    }

    // revenue of PAID orders for each month of a year (index 0 = January)
    
    public double[] getMonthlyRevenue(int year) {
        return orderDao.getMonthlyRevenue(year);
    }

    // revenue of PAID orders for each day of a month (index 0 = day 1)
    
    public double[] getDailyRevenue(int month, int year) {
        return orderDao.getDailyRevenue(month, year);
    }

    
    // EMPTIES LEDGER !!!


    public List<LedgerEntry> getLedger() {
        return ledgerDao.findAll();
    }

    // SUM(OWED) of all ledger rows for one container type
    
    public int getTotalOwed(String containerType) {
        int total = 0;
        for (LedgerEntry e : ledgerDao.findAll()) {
            if (e.getContainerType().equals(containerType)) {
                total += e.getOwed();
            }
        }
        return total;
    }

    // SUM(DEPOSIT) of all ledger rows where OWED > 0
    
    public double getTotalDepositsHeld() {
        double total = 0;
        for (LedgerEntry e : ledgerDao.findAll()) {
            if (e.getOwed() > 0) {
                total += e.getDeposit();
            }
        }
        return total;
    }

    // how many containers of one type a customer still owes
    
    public int getOwedByCustomer(Customer c, String containerType) {
        int total = 0;
        for (LedgerEntry e : ledgerDao.findAll()) {
            boolean match = e.getCustomer().getId() == c.getId()
                    && e.getContainerType().equals(containerType);
            if (match) {
                total += e.getOwed();
            }
        }
        return total;
    }

    // RETURN EMPTIES ONLY: the oldest rows of that customer that still owe containers are filled first
    
    public void returnEmpties(Customer c, String containerType, int qty) {
        int remaining = qty;
        for (LedgerEntry e : ledgerDao.findAll()) {
            if (remaining == 0) {
                break;
            }
            boolean match = e.getCustomer().getId() == c.getId()
                    && e.getContainerType().equals(containerType);
            if (match && e.getOwed() > 0) {
                int give = Math.min(e.getOwed(), remaining);
                e.addEmptiesIn(give);
                ledgerDao.updateEmptiesIn(e.getOrderNo(), e.getEmptiesIn());
                remaining -= give;
            }
        }
    }

    // REFUND DEPOSIT: the cash was handed back, so the deposit becomes 0
    public void refundDeposit(LedgerEntry entry) {
        ledgerDao.clearDeposit(entry.getOrderNo());
        entry.clearDeposit();
    }
}
