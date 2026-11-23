package hydroflow.model;

// one customer in the CUSTOMER LIST (dashboard)
public class Customer {

    private int id;
    private String name;
    private String address;
    private String contactNo;

    public Customer(int id, String name, String address, String contactNo) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.contactNo = contactNo;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public String getContactNo() { return contactNo; }

    public void setName(String name) { this.name = name; }
    public void setAddress(String address) { this.address = address; }
    public void setContactNo(String contactNo) { this.contactNo = contactNo; }

    // text shown inside combo boxes (New Order, Return Empties)
    
    @Override
    public String toString() {
        return name + "  -  " + address;
    }
}
