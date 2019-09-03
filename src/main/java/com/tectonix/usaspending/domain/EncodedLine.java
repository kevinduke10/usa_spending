package com.tectonix.usaspending.domain;

public class EncodedLine {

    Integer index;
    String resultAddress;
    Double lat;
    Double lon;

    EncodedLine(Integer index, String result, Double lon, Double lat){
        this.index = index;
        this.resultAddress = result;
        this.lat = lat;
        this.lon = lon;
    }

    public static EncodedLine fromCSV(String[] line){
        try {
            if (line[3] != null && line[3].length() > 0 && !line[3].equals("resultAddress")) {
                if (line[2] != null && line[2].length() > 0) {
                    String[] coordSplit = line[2].replace("\\", ";").split(";");
                    Double lon = Double.parseDouble(coordSplit[0]);
                    Double lat = Double.parseDouble(coordSplit[1]);

                    return new EncodedLine(Integer.parseInt(line[0]), line[3], lon, lat);

                }
            }
        }catch(Exception e){

        }

        return null;
    }

    public Integer getIndex() {
        return index;
    }

    public String getResultAddress() {
        return resultAddress;
    }

    public Double getLat() {
        return lat;
    }

    public Double getLon() {
        return lon;
    }
}
