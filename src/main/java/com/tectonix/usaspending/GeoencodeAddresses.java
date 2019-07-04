package com.tectonix.usaspending;

import com.tectonix.domain.Address;
import com.tectonix.domain.MoneyLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class GeoencodeAddresses {

    static String geoEncodingURLBase = "https://geocoding.geo.census.gov/geocoder/locations/onelineaddress?address=";
    static String filteredAddressFile = "/Users/Kev/Tectonix/data/usa_spending/2019_all_Contracts_Full_20190515_2_filtered.csv";

    public static void main(String[] args) throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();

        int indexNum = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(filteredAddressFile))) {
            String line;
            int badVals = 0;
            while ((line = br.readLine()) != null) {
                if(indexNum != 0) {
                    MoneyLine ml = MoneyLine.fromFilteredCSV(line.split(","));
                    lookupAddress(client,ml.getAddress());
                    Thread.sleep(1000);
                }
                indexNum ++;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        client.close();

    }

    public static Address lookupAddress(CloseableHttpClient client, String queryAddress) {
        queryAddress = queryAddress +"&benchmark=9&format=json";

        try {
            HttpGet geoQuery = new HttpGet(geoEncodingURLBase + queryAddress);
            geoQuery.addHeader("accept-encoding", "gzip, deflate");
            CloseableHttpResponse response = client.execute(geoQuery);
            String responseString = EntityUtils.toString(response.getEntity());
            System.out.println(responseString);
            Address resultAddress = Address.fromResponse(queryAddress,responseString);
            response.close();
            return resultAddress;
        } catch (Exception e) {
            System.out.println("Failed Address Lookup");
            return null;
        }
    }


}
