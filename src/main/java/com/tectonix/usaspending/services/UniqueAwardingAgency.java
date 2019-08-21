package com.tectonix.usaspending.services;

import com.opencsv.CSVWriter;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

@Service
public class UniqueAwardingAgency {

    @Value("${dataDir}")
    String dataDir;

    @Value("${googleEncodingAPIKey}")
    String apiKey;

    static CloseableHttpClient client = HttpClients.createDefault();

    private class UniqueAgency{
        String awardingAgencyName;
        String awardingSubAgencyName;
        String awardingOfficeName;

        UniqueAgency(String awardingAgencyName, String awardingSubAgencyName, String awardingOfficeName){
            this.awardingAgencyName = awardingAgencyName;
            this.awardingSubAgencyName = awardingSubAgencyName;
            this.awardingOfficeName = awardingOfficeName;
        }
    }

    private class GoogleGeoEncodeResult{
        String formattedAddress;
        Double lat;
        Double lon;

        GoogleGeoEncodeResult(String formattedAddress, Double lat,Double lon){
            this.formattedAddress = formattedAddress;
            this.lat = lat;
            this.lon = lon;
        }
    }

    private class GoogleFormattedAddress{
        String street;
        String city;
        String state;
        String zip;
        String country;

        GoogleFormattedAddress(String street,String city,String state, String zip, String country){
            this.street = street;
            this.city = city;
            this.state = state;
            this.zip = zip;
            this.country = country;
        }
    }

    public void createUniqueCSV(){

        List<String> uniqueSubAgencies = new ArrayList<>();
        List<UniqueAgency> uniqueAwardingAgencies = new ArrayList<>();
        File filteredDataDir = new File(dataDir + "filtered");
        File[] filteredFiles = filteredDataDir.listFiles();
        for(int i = 0; i < filteredFiles.length; i++){
            File filteredFile = filteredFiles[i];
            if(!filteredFile.getAbsolutePath().contains("DS_Store")) {
                int idx = 0;
                try (BufferedReader br = new BufferedReader(new FileReader(filteredFile))) {
                    String line;

                    while ((line = br.readLine()) != null) {

                        if (idx != 0) {
                            String[] split = line.split(",");
                            if (split.length > 19) {
                                String awardingAgencyName = split[14];
                                String awardingSubAgencyName = split[16];
                                String awardingOfficeName = split[18];
                                awardingSubAgencyName = awardingSubAgencyName.replaceAll("\"", "")
                                        .replaceAll(",", "").trim();
                                awardingOfficeName = awardingOfficeName.replaceAll("\"", "")
                                        .replaceAll(",", "").trim();
                                if (!uniqueSubAgencies.contains(awardingSubAgencyName)) {
                                    uniqueAwardingAgencies.add(new UniqueAgency(awardingAgencyName,awardingSubAgencyName, awardingOfficeName));
                                    uniqueSubAgencies.add(awardingSubAgencyName);
                                }
                            } else if (split.length == 1) {
                                String awardingAgencyName = split[0];
                                awardingAgencyName = awardingAgencyName.replaceAll("\"", "")
                                        .replaceAll(",", "").trim();
                                if (!uniqueSubAgencies.contains(awardingAgencyName)) {
                                    uniqueAwardingAgencies.add(new UniqueAgency(awardingAgencyName,"", ""));
                                    uniqueSubAgencies.add(awardingAgencyName);
                                }
                            }
                        }
                        idx++;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        try {
            File uniqueDir = new File(dataDir + "/unique/");
            uniqueDir.mkdir();
            File uniqueSubAgencyFile = new File(uniqueDir.getAbsolutePath() + "/uniqueagency.csv");
            FileWriter outWriter = new FileWriter(uniqueSubAgencyFile, false);
            CSVWriter csvWriter = new CSVWriter(outWriter, CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER);

            String[] headers = {"AWARDING_AGENCY", "AWARDING_SUB_AGENCY", "AWARDING_OFFICE_NAME"};
            csvWriter.writeNext(headers);

            uniqueAwardingAgencies.forEach(agency -> {
                String[] writeMe = {agency.awardingAgencyName,agency.awardingSubAgencyName,agency.awardingOfficeName};
                csvWriter.writeNext(writeMe);
            });

            csvWriter.flush();
            csvWriter.close();

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void lookupSenders(){
        File uniqueDir = new File(dataDir + "/unique/");
        uniqueDir.mkdir();
        File uniqueSubAgencyFile = new File(uniqueDir.getAbsolutePath() + "/uniqueagency.csv");
        File encodedUnique = new File(uniqueDir.getAbsolutePath() + "/encoded.csv");

        int idx = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(uniqueSubAgencyFile))) {
            String line;

            FileWriter outWriter = new FileWriter(encodedUnique, false);
            CSVWriter csvWriter = new CSVWriter(outWriter, CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER);

            String[] headers = {
                                "AWARDING_AGENCY",
                                "AWARDING_SUB_AGENCY",
                                "AWARDING_OFFICE_NAME",
                                "STREET",
                                "CITY",
                                "STATE",
                                "ZIP",
                                "COUNTRY",
                                "LAT",
                                "LON"
                            };
            csvWriter.writeNext(headers);

            while ((line = br.readLine()) != null) {
                if(idx > 0) {
                    String[] split = line.split(",");
                    UniqueAgency ua = new UniqueAgency(split[0], split[1], split[2]);
                    GoogleGeoEncodeResult result = sendGoogleRequest(ua);
                    if (result != null) {
                        GoogleFormattedAddress formattedAddress = breakoutFormattedAddress(result.formattedAddress);
                        String[] writeMe = {ua.awardingAgencyName,
                                ua.awardingSubAgencyName,
                                ua.awardingOfficeName,
                                formattedAddress.street,
                                formattedAddress.city,
                                formattedAddress.state,
                                formattedAddress.zip,
                                formattedAddress.country,
                                result.lat.toString(),
                                result.lon.toString()
                        };
                        csvWriter.writeNext(writeMe);
                    } else {
                        String[] writeMe = {ua.awardingAgencyName,
                                ua.awardingSubAgencyName,
                                ua.awardingOfficeName
                        };
                        csvWriter.writeNext(writeMe);
                    }
                }
                idx++;
            }
            csvWriter.flush();
            csvWriter.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private GoogleFormattedAddress breakoutFormattedAddress(String addresss){
        try {
            String[] split = addresss.split(",");
            if (split.length == 4) {
                String street = split[0];
                String city = split[1];

                String state = "";
                String zip = "";
                if (split[2] != null && split[2].split(" ").length == 3) {
                    String[] stateZip = split[2].split(" ");
                    state = stateZip[1];
                    zip = stateZip[2];
                }else if(split[2] != null && split[2].split(" ").length == 2){
                    state = split[2].split(" ")[2];
                }

                String country = split[3];
                return new GoogleFormattedAddress(street, city, state, zip, country);
            } else if (split.length == 3) {
                String city = split[0];

                String state = "";
                String zip = "";
                if (split[1] != null && split[1].split(" ").length == 3) {
                    String[] stateZip = split[1].split(" ");
                    state = stateZip[1];
                    zip = stateZip[2];
                }else if(split[1] != null && split[1].split(" ").length == 2){
                    state = split[1].split(" ")[1];
                }
                String country = split[2];
                return new GoogleFormattedAddress("", city, state, zip, country);
            } else {
                return new GoogleFormattedAddress("", "", "", "", "");
            }
        }catch(Exception e){
            return new GoogleFormattedAddress("", "", "", "", "");
        }
    }

    private GoogleGeoEncodeResult sendGoogleRequest(UniqueAgency ua){
        String address = "https://maps.googleapis.com/maps/api/geocode/json?address=";
        if(ua.awardingOfficeName.length() > 0){
            address = address + ua.awardingSubAgencyName + " " + ua.awardingOfficeName;
        }else if(ua.awardingAgencyName.length() > 0){
            address = address + ua.awardingAgencyName;
        }else{
            return null;
        }

        address = address + "&key=" + apiKey;


        try {
            HttpGet geoQuery = new HttpGet(address.replace(" ","+")
                            .replace("\"", "")
                            .replace("#", "")
            );
            CloseableHttpResponse response = client.execute(geoQuery);
            String responseString = EntityUtils.toString(response.getEntity());
            GoogleGeoEncodeResult result = fromCSV(responseString);
            response.close();

            return result;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    private GoogleGeoEncodeResult fromCSV(String result){
        JSONObject resultObject = new JSONObject(result);
        JSONArray results = resultObject.getJSONArray("results");
        if(results.length() == 0){
            return null;
        }

        JSONObject firstResult = results.getJSONObject(0);
        String formattedResult = firstResult.get("formatted_address").toString();
        JSONObject geometry = firstResult.getJSONObject("geometry");
        JSONObject location = geometry.getJSONObject("location");
        Double lat = location.getDouble("lat");
        Double lon = location.getDouble("lng");

        return new GoogleGeoEncodeResult(formattedResult, lat, lon);
    }
}
