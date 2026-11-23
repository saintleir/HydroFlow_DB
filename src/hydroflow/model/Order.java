package hydroflow.model;

import java.time.LocalDateTime;

// one row of the ORDER LIST (the same object is shown in the SALES RECORD and the REPORTS

public class Order {

    // fixed text values (these constants are used instead of typing the words again and again)
	
    public static final String DELIVERY = "Delivery";
    public static final String WALK_IN = "Walk-in";
    public static final String PENDING = "Pending";
    public static final String DELIVERED = "Delivered";
    public static final String CANCELLED = "Cancelled";
    public static final String UNPAID = "Unpaid";
    public static final String PAID = "Paid";

    private int orderNo;
    private Customer customer;
    private String orderType;       // Delivery or Walk-in
    private String waterType;       // Alkaline or Mineral
    private String containerType;   // Round Cylinder or Slim Rectangular
    private int quantity;           // gallons
    private int emptiesIn;          // empty containers swapped in
    private double unitPrice;       // price per gallon when the order was made
    private double deliveryFee;
    private double discount;
    private double deposit;         // refundable container deposit (NOT revenue)
    private double amountDue;       // water revenue = price x qty + delivery fee - discount
    private String status = PENDING;
    private String paymentStatus = UNPAID;
    private double amountPaid = 0;  // cash handed over by the customer
    private double change = 0;
    private LocalDateTime orderDateTime = LocalDateTime.now();
    private String remarks;

    public Order(Customer customer, String orderType, String waterType, String containerType,
                 int quantity, int emptiesIn, double unitPrice, double deliveryFee,
                 double discount, double deposit, String remarks) {
        this.customer = customer;
        this.orderType = orderType;
        this.waterType = waterType;
        this.containerType = containerType;
        this.quantity = quantity;
        this.emptiesIn = emptiesIn;
        this.unitPrice = unitPrice;
        this.deliveryFee = deliveryFee;
        this.discount = discount;
        this.deposit = deposit;
        this.remarks = remarks;
        this.amountDue = Billing.computeAmountDue(unitPrice, quantity, deliveryFee, discount);
    }

    // what the customer must hand over: water amount + refundable deposit
    
    public double getTotalToPay() {
        return amountDue + deposit;
    }

    // containers borrowed and not yet returned for this order
    
    public int getOwed() {
        return Math.max(quantity - emptiesIn, 0);
    }

    public int getOrderNo() { return orderNo; }
    public Customer getCustomer() { return customer; }
    public String getOrderType() { return orderType; }
    public String getWaterType() { return waterType; }
    public String getContainerType() { return containerType; }
    public int getQuantity() { return quantity; }
    public int getEmptiesIn() { return emptiesIn; }
    public double getUnitPrice() { return unitPrice; }
    public double getDeliveryFee() { return deliveryFee; }
    public double getDiscount() { return discount; }
    public double getDeposit() { return deposit; }
    public double getAmountDue() { return amountDue; }
    public String getStatus() { return status; }
    public String getPaymentStatus() { return paymentStatus; }
    public double getAmountPaid() { return amountPaid; }
    public double getChange() { return change; }
    public LocalDateTime getOrderDateTime() { return orderDateTime; }
    public String getRemarks() { return remarks; }

    public void setOrderNo(int orderNo) { this.orderNo = orderNo; }
    public void setStatus(String status) { this.status = status; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public void setAmountPaid(double amountPaid) { this.amountPaid = amountPaid; }
    public void setChange(double change) { this.change = change; }
    public void setOrderDateTime(LocalDateTime orderDateTime) { this.orderDateTime = orderDateTime; }
}
