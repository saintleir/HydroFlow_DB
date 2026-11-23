package hydroflow.model;

/* values that the admin can change in the SETTINGS popup:
 * prices, delivery fee, deposit, available containers and the daily sales quota */
public class Settings {

    // the two water types and the two container types of the station
	
    public static final String[] WATER_TYPES = {"Alkaline", "Mineral"};
    public static final String[] CONTAINER_TYPES = {"Round Cylinder", "Slim Rectangular"};

    private double deliveryFee = 20.00;         // baseline fee PER GALLON (delivery orders only)
    private double depositPerContainer = 100.00; // refundable fee for each borrowed container

    private double alkalineRound = 40.00;
    private double alkalineSlim = 40.00;
    private double mineralRound = 25.00;
    private double mineralSlim = 25.00;

    private int totalContainers = 100;          // ceiling of available containers
    private double dailySalesQuota = 1000.00;   // target sales per day

    // price per gallon for a water type + container type combination
    
    public double getPrice(String waterType, String containerType) {
        boolean round = containerType.equals("Round Cylinder");
        if (waterType.equals("Alkaline")) {
            return round ? alkalineRound : alkalineSlim;
        }
        return round ? mineralRound : mineralSlim;
    }

    public double getDeliveryFee() { return deliveryFee; }
    public double getDepositPerContainer() { return depositPerContainer; }
    public double getAlkalineRound() { return alkalineRound; }
    public double getAlkalineSlim() { return alkalineSlim; }
    public double getMineralRound() { return mineralRound; }
    public double getMineralSlim() { return mineralSlim; }
    public int getTotalContainers() { return totalContainers; }
    public double getDailySalesQuota() { return dailySalesQuota; }

    public void setDeliveryFee(double deliveryFee) { this.deliveryFee = deliveryFee; }
    public void setDepositPerContainer(double depositPerContainer) { this.depositPerContainer = depositPerContainer; }
    public void setAlkalineRound(double alkalineRound) { this.alkalineRound = alkalineRound; }
    public void setAlkalineSlim(double alkalineSlim) { this.alkalineSlim = alkalineSlim; }
    public void setMineralRound(double mineralRound) { this.mineralRound = mineralRound; }
    public void setMineralSlim(double mineralSlim) { this.mineralSlim = mineralSlim; }
    public void setTotalContainers(int totalContainers) { this.totalContainers = totalContainers; }
    public void setDailySalesQuota(double dailySalesQuota) { this.dailySalesQuota = dailySalesQuota; }
}
