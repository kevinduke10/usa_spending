package com.tectonix.usaspending.domain;

import org.json.JSONArray;
import org.json.JSONObject;
import org.locationtech.jts.geom.Coordinate;

public class Address {
    Integer indexNum;
    String inputAddress;
    Coordinate coordinate;
    String resultAddress;
    String source;

    public Address(String inputAddress, Coordinate coordinate, String resultAddress,int indexNum, String source){
        this.indexNum = indexNum;
        this.inputAddress = inputAddress;
        this.coordinate = coordinate;
        this.resultAddress = resultAddress;
        this.source = source;
    }

    public static Address fromCensusResponse(String inputAddress, String response, int indexNum){
        if(!response.startsWith("{")){
            //System.out.println("Failed with res = " + response);
            return new Address(inputAddress, new Coordinate(0D,0D), "FAILED",indexNum, "CENSUS");
        }
        JSONObject lookup = new JSONObject(response);
        try {
            if (lookup.has("result")) {
                JSONObject result = lookup.getJSONObject("result");
                if (result.has("addressMatches")) {
                    JSONArray matches = result.getJSONArray("addressMatches");
                    if(matches.length() > 0) {
                        JSONObject address = matches.getJSONObject(0);
                        if (address.has("coordinates") && address.has("matchedAddress")) {
                            JSONObject coord = address.getJSONObject("coordinates");
                            Coordinate matchedCoord = new Coordinate(Double.valueOf(coord.get("x").toString()), Double.valueOf(coord.get("y").toString()));
                            return new Address(inputAddress, matchedCoord, address.get("matchedAddress").toString(),indexNum,"CENSUS");
                        }
                    }else{
                        return new Address(inputAddress, new Coordinate(0D,0D), "FAILED",indexNum, "CENSUS");
                    }
                }
            }
        }catch (Exception e){
            return new Address(inputAddress, new Coordinate(0D,0D), "FAILED",indexNum, "CENSUS");
        }
        return new Address(inputAddress, new Coordinate(0D,0D), "FAILED",indexNum, "CENSUS");
    }

    public static Address fromPeliasResponse(String inputAddress, String response, int indexNum){
        JSONObject lookup = new JSONObject(response);
        try {
            if (lookup.has("features")) {
                JSONArray result = lookup.getJSONArray("features");
                if(result != null && result.length() > 0){
                    JSONObject firstResult = result.getJSONObject(0);
                    JSONArray coords = firstResult.getJSONObject("geometry").getJSONArray("coordinates");

                    String resultAddress = firstResult.getJSONObject("properties").getString("label");
                    if(resultAddress == null) resultAddress = "";

                    return new Address(inputAddress, new Coordinate(coords.getDouble(1),coords.getDouble(0)), resultAddress.replace(","," "),indexNum, "PELIAS");
                }else{
                    return new Address(inputAddress, new Coordinate(0D,0D), "FAILED",indexNum, "PELIAS");
                }

            }
        }catch (Exception e){
            return new Address(inputAddress, new Coordinate(0D,0D), "FAILED",indexNum, "PELIAS");
        }
        return new Address(inputAddress, new Coordinate(0D,0D), "FAILED",indexNum, "PELIAS");

    }

    public String getInputAddress() {
        return inputAddress;
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public String getResultAddress() {
        return resultAddress;
    }

    public Integer getIndexNum(){
        return indexNum;
    }
}
