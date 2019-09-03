package com.tectonix.usaspending.domain;

public class GoogleGeoEncodeResult{
    public String formattedAddress;
    public Double lat;
    public Double lon;

    public GoogleGeoEncodeResult(String formattedAddress, Double lat,Double lon){
        this.formattedAddress = formattedAddress;
        this.lat = lat;
        this.lon = lon;
    }
}
