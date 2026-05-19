package com.usc.lugarlangfinal.models;

public class Ticket {
    private String origin;
    private String destination;
    private String passenger_type;
    private double regular_fare;
    private double discount;
    private double total_fare;
    private String timestamp;
    private String route_code;
    private String payment_method;

    public Ticket() {
        // Required for Firebase
    }

    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public String getPassenger_type() { return passenger_type; }
    public void setPassenger_type(String passenger_type) { this.passenger_type = passenger_type; }

    public double getRegular_fare() { return regular_fare; }
    public void setRegular_fare(double regular_fare) { this.regular_fare = regular_fare; }

    public double getDiscount() { return discount; }
    public void setDiscount(double discount) { this.discount = discount; }

    public double getTotal_fare() { return total_fare; }
    public void setTotal_fare(double total_fare) { this.total_fare = total_fare; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public String getRoute_code() { return route_code; }
    public void setRoute_code(String route_code) { this.route_code = route_code; }

    public String getPayment_method() { return payment_method; }
    public void setPayment_method(String payment_method) { this.payment_method = payment_method; }
}