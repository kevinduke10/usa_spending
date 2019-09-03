package com.tectonix.usaspending.domain;

public class MoneyRecipient {

    MoneyLine line;
    Double lat;
    Double lon;

    MoneyRecipient(String[] line, Double lat, Double lon){
        this.line = MoneyLine.fromFilteredCSV(line);
        this.lat = lat;
        this.lon = lon;
    }

}
