package com.tectonix.usaspending;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.File;

public class GeoencodeAddresses {

    static String geoEncodingURLBase = "https://geocoding.geo.census.gov/geocoder/locations/onelineaddress?address=";
    static String filteredAddressFile = "~/Tectonix/data/usa_spending/2019_all_Contracts_Full_20190515_2_filtered.csv";

    public static void main(String[] args) {
        CloseableHttpClient client = HttpClients.createDefault();
        File filteredFile = new File(filteredAddressFile);

    }

    public static String lookupAddress(CloseableHttpClient client, String queryAddress) {
        queryAddress = queryAddress +"&benchmark=9&format=json";

        try {
            HttpGet geoQuery = new HttpGet(geoEncodingURLBase + queryAddress);
            geoQuery.addHeader("accept-encoding", "gzip, deflate");
            CloseableHttpResponse response = client.execute(geoQuery);
            String responseString = EntityUtils.toString(response.getEntity());
            client.close();
            return "";
        } catch (Exception e) {
            System.out.println("Failed Address Lookup");
            return "";
        }
    }
}
