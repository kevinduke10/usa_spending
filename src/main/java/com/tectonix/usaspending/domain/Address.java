package com.tectonix.usaspending.domain;

import org.json.JSONArray;
import org.json.JSONObject;
import org.locationtech.jts.geom.Coordinate;

public class Address {
    String inputAddress;
    Coordinate coordinate;
    String resultAddress;

    public Address(String inputAddress, Coordinate coordinate, String resultAddress){
        this.inputAddress = inputAddress;
        this.coordinate = coordinate;
        this.resultAddress = resultAddress;
    }

    public static Address fromResponse(String inputAddress, String response){
        JSONObject lookup = new JSONObject(response);
        try {
            if (lookup.has("result")) {
                JSONObject result = lookup.getJSONObject("result");
                if (result.has("addressMatches")) {
                    JSONArray matches = result.getJSONArray("addressMatches");
                    JSONObject address = matches.getJSONObject(0);
                    if (address.has("coordinates") && address.has("matchedAddress")) {
                        JSONObject coord = address.getJSONObject("coordinates");
                        Coordinate matchedCoord = new Coordinate(Double.valueOf(coord.get("x").toString()), Double.valueOf(coord.get("y").toString()));
                        return new Address(inputAddress, matchedCoord, address.get("matchedAddress").toString());
                    }
                }
            }
        }catch (Exception e){
            return null;
        }
        return null;
    }
}
