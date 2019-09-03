package com.tectonix.usaspending.domain;

import org.json.JSONArray;
import org.json.JSONObject;
import org.locationtech.jts.geom.Coordinate;

public class Address {
    String tectonixUUID;
    String inputAddress;
    Coordinate coordinate;
    String resultAddress;
    String source;
    Integer deleteMeIndex;


    public Address(String inputAddress, Coordinate coordinate, String resultAddress, String tectonixUUID, String source,Integer deleteMe){
        this.tectonixUUID = tectonixUUID;
        this.inputAddress = inputAddress;
        this.coordinate = coordinate;
        this.resultAddress = resultAddress;
        this.source = source;
        this.deleteMeIndex = deleteMe;
    }

    public static Address fromCensusResponse(String inputAddress, String response, String tectonixUUID, Integer index){

        try {
            if(!response.startsWith("{")){
                return null;
                //return new Address(inputAddress, new Coordinate(0D,0D), "FAILED",indexNum, "CENSUS");
            }
            JSONObject lookup = new JSONObject(response);

            if (lookup.has("result")) {
                JSONObject result = lookup.getJSONObject("result");
                if (result.has("addressMatches")) {
                    JSONArray matches = result.getJSONArray("addressMatches");
                    if(matches.length() > 0) {
                        JSONObject address = matches.getJSONObject(0);
                        if (address.has("coordinates") && address.has("matchedAddress")) {
                            JSONObject coord = address.getJSONObject("coordinates");
                            Coordinate matchedCoord = new Coordinate(Double.valueOf(coord.get("x").toString()), Double.valueOf(coord.get("y").toString()));
                            return new Address(inputAddress, matchedCoord, address.get("matchedAddress").toString(),tectonixUUID,"CENSUS",index);
                        }
                    }else{
                        return null;
                    }
                }
            }
            return null;
        }catch (Exception e){
            return null;
        }
    }

    public static Address fromPeliasResponse(String inputAddress, String response, String tectonixUUID,Integer deleteMeIndex){
        try {
            JSONObject lookup = new JSONObject(response);

            if (lookup.has("features")) {
                JSONArray result = lookup.getJSONArray("features");
                if(result != null && result.length() > 0){
                    JSONObject firstResult = result.getJSONObject(0);
                    JSONArray coords = firstResult.getJSONObject("geometry").getJSONArray("coordinates");

                    String resultAddress = firstResult.getJSONObject("properties").getString("label");
                    if(resultAddress == null) resultAddress = "";

                    return new Address(inputAddress, new Coordinate(coords.getDouble(1),coords.getDouble(0)), resultAddress.replace(","," "),tectonixUUID, "PELIAS",deleteMeIndex);
                }else{
                    return null;
                }

            }
            return null;
        }catch (Exception e){
            return null;
        }
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

    public String getTectonixUUID(){
        return tectonixUUID;
    }

    public Integer getIndex(){return deleteMeIndex;}
}
