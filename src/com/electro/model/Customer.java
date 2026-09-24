package com.electro.model;

/**
 * Customer profile for billing and warranty records.
 */
public class Customer {
    private String id;
    private String name;
    private String phone;
    private String email;
    private String address;
    private String gstin; // Tax identification number
    private String customerType = "RETAIL"; // RETAIL or WHOLESALE

    public Customer() {}

    public Customer(String id, String name, String phone, String email, String address, String gstin) {
        this(id, name, phone, email, address, gstin, "RETAIL");
    }

    public Customer(String id, String name, String phone, String email, String address, String gstin, String customerType) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.gstin = gstin;
        this.customerType = (customerType != null && !customerType.trim().isEmpty()) ? customerType.trim().toUpperCase() : "RETAIL";
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getGstin() { return gstin; }
    public void setGstin(String gstin) { this.gstin = gstin; }

    public String getCustomerType() { return customerType != null ? customerType : "RETAIL"; }
    public void setCustomerType(String customerType) {
        this.customerType = (customerType != null && !customerType.trim().isEmpty()) ? customerType.trim().toUpperCase() : "RETAIL";
    }

    public boolean isWholesale() {
        return "WHOLESALE".equalsIgnoreCase(customerType);
    }

    @Override
    public String toString() {
        return name + " (" + phone + ")";
    }
}
