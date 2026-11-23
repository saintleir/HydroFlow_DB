package hydroflow.model;

// all billing formulas from the system document kept in one place so every window uses the same math

public class Billing {

    // DELIVERY FEE = Baseline Fee x QTY (if orderType = Delivery, otherwise 0 for Walk-in)
	
    public static double computeDeliveryFee(String orderType, int qty, double baselineFee) {
        if (orderType.equals(Order.DELIVERY)) {
            return baselineFee * qty;
        }
        return 0;
    }

    // DEPOSIT = (QTY - EMPTIES IN) x Container Deposit Rate (if QTY > EMPTIES IN, otherwise 0)
    
    public static double computeDeposit(int qty, int emptiesIn, double depositRate) {
        if (qty > emptiesIn) {
            return (qty - emptiesIn) * depositRate;
        }
        return 0;
    }

    // AMT DUE = (Water Price x QTY) + Delivery Fee - Discount
    
    public static double computeAmountDue(double waterPrice, int qty, double deliveryFee, double discount) {
        return (waterPrice * qty) + deliveryFee - discount;
    }
}
