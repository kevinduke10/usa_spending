package com.tectonix.usaspending;

import com.opencsv.CSVWriter;
import com.tectonix.domain.MoneyLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CleanBadAddresses {

    static String geoEncodingURLBase = "https://geocoding.geo.census.gov/geocoder/locations/onelineaddress?address=";
    static String originalFileName = "/PATH_TO/2019_all_Contracts_Full_20190513/2019_all_Contracts_Full_20190515_1.csv";
    static String filteredAddressFile = "/PATH_TO/2019_all_Contracts_Full_20190513/2019_all_Contracts_Full_20190515_1_filtered.csv";

    public static void main(String[] args) throws IOException {
        CSVWriter writer = new CSVWriter(new FileWriter(filteredAddressFile), CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER);

        int indexNum = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(originalFileName))) {
            String line;
            int badVals = 0;
            while ((line = br.readLine()) != null) {

                if (indexNum != 0) {
                    String lineWithoutExtraCommas = removeExtraCommas(line);
                    String[] split = lineWithoutExtraCommas.split(",");

                    if (split.length == 261) {
                        String queryAddress = "";

                        String line1 = split[42];
                        String line2 = split[43];
                        String cityName = split[44];
                        String stateName = split[46];
                        if (line1.length() > 0) {
                            queryAddress = queryAddress + line1.replace(" ", "+");
                        }
                        if (line2.length() > 0) {
                            queryAddress = queryAddress + line2.replace(" ", "+");
                        }

                        if (cityName != null) {
                            queryAddress = queryAddress + "+" + cityName.replace(" ", "+") + "+";
                        } else {
                            return;
                        }

                        if (stateName != null) {
                            queryAddress = queryAddress + stateName;
                        }

                        MoneyLine ml = new MoneyLine(split, queryAddress);

                        writer.writeNext(ml.toCSV());
                    } else {
                        badVals++;
                    }
                } else {
                    writer.writeNext(MoneyLine.getRelevantHeaderFields);
                }

                indexNum++;
            }
            System.out.println("Quality Addresses = " + indexNum);
            System.out.println("Bad Addresses = " + badVals);
        } catch (Exception e) {
            e.printStackTrace();
        }
        writer.close();
    }

    private static String removeExtraCommas(String line) {
        try {
            Pattern quotePattern = Pattern.compile(",\"(.*?)\",");
            Matcher m = quotePattern.matcher(line);

            while (m.find()) {
                line = line.replaceAll(m.group(1), m.group(1).replace(",", " "));
            }

            return line;
        } catch (Exception e) {
            return "";
        }
    }

    public static String lookupAddress(String queryAddress) {
        //lookupAddress(queryAddress +"&benchmark=9&format=json");

        queryAddress = queryAddress +"&benchmark=9&format=json";

        CloseableHttpClient client = HttpClients.createDefault();
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

