package ch.fhnw.group10.Case2_ExtRestService;

public class NewConsignment {

    private String deliveryAddress;
    private String customerReference;
    private String recepientPhone;
    private Integer weight;

    private Country countryCode;
    private ShippingMethod shippingMethod;

    public enum Country{
        CH,
        DE,
        AR,
        RU,
        JP,
        OTHER
    }

    public enum ShippingMethod{
        STANDARD,
        SPECIAL,
        AIR,
        MANUAL_CHECK
    }


    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String destination) {
        this.deliveryAddress = deliveryAddress;
    }

    public String getCustomerReference() {
        return customerReference;
    }

    public void setCustomerReference(String customerReference) {
        this.customerReference = customerReference;
    }

    public String getRecepientPhone() {
        return recepientPhone;
    }

    public void setRecepientPhone(String recepientPhone) {
        this.recepientPhone = recepientPhone;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public Country getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(Country countryCode) {
        this.countryCode = countryCode;
    }


    public void setShippingMethod(ShippingMethod method) {
        shippingMethod = method;
    }

    public ShippingMethod getShippingMethod() {
        return shippingMethod;
    }
}