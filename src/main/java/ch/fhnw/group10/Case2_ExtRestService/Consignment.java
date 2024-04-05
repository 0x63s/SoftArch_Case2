package ch.fhnw.group10.Case2_ExtRestService;

public class Consignment {

    private String orderId;
    private int weight;
    private String pickupdate;
    private String deliverydate;
    private String customerRefernce;
    private String recepientPhone;
    private Country destination;

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


    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public String getPickupdate() {
        return pickupdate;
    }

    public void setPickupdate(String pickupdate) {
        this.pickupdate = pickupdate;
    }


    public String getDeliverydate() {
        return deliverydate;
    }

    public void setDeliverydate(String deliverydate) {
        this.deliverydate = deliverydate;
    }

    public String getCustomerRefernce() {
        return customerRefernce;
    }

    public void setCustomerRefernce(String customerRefernce) {
        this.customerRefernce = customerRefernce;
    }

    public String getRecepientPhone() {
        return recepientPhone;
    }

    public void setRecepientPhone(String recepientPhone) {
        this.recepientPhone = recepientPhone;
    }



    public Country getDestination() {
        return destination;
    }

    public void setDestination(Country destination) {
        this.destination = destination;
    }


    public void setShippingMethod(ShippingMethod method) {
        shippingMethod = method;
    }

    public ShippingMethod getShippingMethod() {
        return shippingMethod;
    }

}