package com.tectonix.usaspending;

import com.opencsv.CSVWriter;
import com.tectonix.usaspending.domain.Address;
import com.tectonix.usaspending.domain.MoneyLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.locationtech.jts.util.Stopwatch;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.util.stream.Collectors.toList;

public class GeoencodeAddresses {

    static String whichCSV = "1";

    //This geocoder fails for many addresses outside (even inside) the US.
    // We'll spin up our own and make available.
    // Cost to use google geoencode API on 2 MIL requests == $2000
    static String geoCoder = "PELIAS";
    static String censusGeoEncodingURLBase = "https://geocoding.geo.census.gov/geocoder/locations/onelineaddress?address=";
    static String peliasGeoEncodingURLBase = "http://localhost:4000/v1/search?text=";

    // If we run all the GET requests for 2 MIL addresses synchronously it'll take forever.
    // When set to true the requests to the geoencoder will go out in batches and wait for the geoencoder to respond
    // to all requests before resuming.
    static boolean runAsync = true;
    static int asyncBatchSize = 10;

    static String filteredAddressFile = "/Users/Kev/Tectonix/data/usa_spending/2019_all_Contracts_Full_20190613_" + whichCSV + "_filtered.csv";
    static String outFileString = "/Users/Kev/Tectonix/data/usa_spending/index_address_file" + whichCSV + ".csv";
    static CloseableHttpClient client = HttpClients.createDefault();
    static File outFile = new File(outFileString);

    public static void main(String[] args){

        int resumeFromMax = 0;
        int badVals = 0;
        Stopwatch sw = new Stopwatch();

        try {
            if(outFile.exists()){
                FileReader indexReader = new FileReader(outFile);
                try (BufferedReader br = new BufferedReader(indexReader)) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        if(line.split(",").length > 0 && !line.split(",")[0].equals("indexNum")){
                            if(Integer.parseInt(line.split(",")[0]) > resumeFromMax){
                                resumeFromMax = Integer.parseInt(line.split(",")[0]);
                            }
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    indexReader.close();
                }
                indexReader.close();
            }

            FileWriter outWriter = new FileWriter(outFile,true);
            CSVWriter csvWriter = new CSVWriter(outWriter, CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER);

            String[] headerLine = {"indexNum", "inputAddress", "coordinate", "resultAddress"};
            int indexNum = 0;

            sw.start();

            try (BufferedReader br = new BufferedReader(new FileReader(filteredAddressFile))) {

                List<MoneyLine> moneyLineBatch = new ArrayList<>();

                String line;
                while ((line = br.readLine()) != null) {
                    if (indexNum < resumeFromMax) {
                        indexNum++;
                        continue;
                    }

                    if(runAsync && moneyLineBatch.size() > asyncBatchSize){

                        List<CompletableFuture<Address>> futureList = moneyLineBatch.stream()
                                .map(ml -> CompletableFuture.supplyAsync(() -> addressLookup(client, ml, csvWriter)))
                                .collect(toList());

                        CompletableFuture.allOf(CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0]))).join();

                        moneyLineBatch.clear();
                    }

                    if (indexNum != 0) {
                        MoneyLine ml = MoneyLine.fromFilteredCSV(line.split(","));
                        ml.setIndexNum(indexNum);
                        moneyLineBatch.add(ml);
                    } else {
                        csvWriter.writeNext(headerLine);
                        csvWriter.flush();
                    }

                    indexNum++;
                    if(indexNum % 100 == 0){
                        System.out.println("Processed row = " + indexNum);
                        System.out.println(sw.getTimeString());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                String[] emptyLine = {String.valueOf(indexNum)};
                csvWriter.writeNext(emptyLine);
                csvWriter.flush();
                indexNum++;
                badVals++;
            }
            csvWriter.close();
            outWriter.close();
            client.close();
            System.out.println("DONE");

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static Address addressLookup(CloseableHttpClient client, MoneyLine ml, CSVWriter writer){
        try {
            Address address = censusAddressLookup(client,ml);
            if(address == null){
                address = peliasAddressLookup(client,ml);
            }

            if(address != null){
                writeResult(address,writer);
                return address;
            }else{
                writeEmpty(writer,ml.getIndexNum());
            }
        }catch (Exception e){
            writeEmpty(writer,ml.getIndexNum());
        }
        return null;
    }

    public static Address censusAddressLookup(CloseableHttpClient client, MoneyLine ml){
        String queryAddress = ml.getAddress() +"&benchmark=9&format=json";
        //Stopwatch sw = new Stopwatch();
        //sw.start();
        try {
            HttpGet geoQuery = new HttpGet(censusGeoEncodingURLBase +
                    queryAddress.replace(" ","+")
                                .replace("\"", "")
                                .replace("#", "")
            );
            geoQuery.addHeader("accept-encoding", "gzip, deflate");
            CloseableHttpResponse response = client.execute(geoQuery);
            String responseString = EntityUtils.toString(response.getEntity());
            Address resultAddress = Address.fromCensusResponse(ml.getAddress(), responseString, ml.getIndexNum());
            response.close();

            //System.out.println(sw.getTimeString());
            return resultAddress;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static Address peliasAddressLookup(CloseableHttpClient client, MoneyLine ml){
//        Stopwatch sw = new Stopwatch();
//        sw.start();
        try {
            HttpGet geoQuery = new HttpGet(peliasGeoEncodingURLBase +
                    ml.getAddress().replace(" ","+")
                            .replace("\"", "")
            );

            CloseableHttpResponse response = client.execute(geoQuery);
            String responseString = EntityUtils.toString(response.getEntity());
            Address resultAddress = Address.fromPeliasResponse(ml.getAddress(), responseString,ml.getIndexNum());
            response.close();
            //System.out.println(sw.getTimeString());
            return resultAddress;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static void writeResult(Address address, CSVWriter csvWriter){
        try {
            String[] writeMe = {String.valueOf(address.getIndexNum()),
                    address.getInputAddress(),
                    address.getCoordinate().x + "\\" + address.getCoordinate().y,
                    address.getResultAddress()
            };
            csvWriter.writeNext(writeMe);
            csvWriter.flush();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void writeEmpty(CSVWriter csvWriter,Integer indexNum){
        try {
            String[] emptyLine = {String.valueOf(indexNum)};
            csvWriter.writeNext(emptyLine);
            csvWriter.flush();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
