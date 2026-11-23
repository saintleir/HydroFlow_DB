package hydroflow.model;

import java.time.LocalDate;

// one row of the EMPTIES LEDGER (a row is created automatically when an order is marked as Delivered)
public class LedgerEntry {

    private int orderNo;
    private LocalDate date;
    private Customer customer;
    private String waterType;
    private String containerType;
    private int qtyOut;       // containers handed out (the order QTY)
    private int emptiesIn;    // empties received back
    private double deposit;   // refundable deposit held by the station

    public LedgerEntry(int orderNo, LocalDate date, Customer customer, String waterType,
                       String containerType, int qtyOut, int emptiesIn, double deposit) {
        this.orderNo = orderNo;
        this.date = date;
        this.customer = customer;
        this.waterType = waterType;
        this.containerType = containerType;
        this.qtyOut = qtyOut;
        this.emptiesIn = emptiesIn;
        this.deposit = deposit;
    }

    // OWED = QTY - EMPTIES IN
    
    public int getOwed() {
        return qtyOut - emptiesIn;
    }

    // called by RETURN EMPTIES ONLY
    
    public void addEmptiesIn(int amount) {
        emptiesIn += amount;
    }

    // called by REFUND DEPOSIT after the cash is handed back
    
    public void clearDeposit() {
        deposit = 0;
    }

    public int getOrderNo() { return orderNo; }
    public LocalDate getDate() { return date; }
    public Customer getCustomer() { return customer; }
    public String getWaterType() { return waterType; }
    public String getContainerType() { return containerType; }
    public int getQtyOut() { return qtyOut; }
    public int getEmptiesIn() { return emptiesIn; }
    public double getDeposit() { return deposit; }
}
