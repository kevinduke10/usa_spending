package com.tectonix.usaspending.domain;

public class GoogleFormattedAddress{
    public String street;
    public String city;
    public String state;
    public String zip;
    public String country;

    public GoogleFormattedAddress(String street,String city,String state, String zip, String country){
        this.street = street;
        this.city = city;
        this.state = state;
        this.zip = zip;
        this.country = country;
    }
}
