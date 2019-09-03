package com.tectonix.usaspending.domain;

public class EncodedAgency extends UniqueAgency {

    Double lat;
    Double lon;

    public EncodedAgency(String awardingAgencyName,
                  String awardingSubAgencyName,
                  String awardingOfficeName,
                  String lat,
                  String lon){
        super(awardingAgencyName,awardingSubAgencyName,awardingOfficeName);
        this.lat = Double.parseDouble(lat);
        this.lon = Double.parseDouble(lon);
    }

    public static EncodedAgency fromCSV(String[] line){
        if(line.length >8){
            return new EncodedAgency(line[0],line[1],line[2], line[8],line[9]);
        }
        return null;
    }
}
