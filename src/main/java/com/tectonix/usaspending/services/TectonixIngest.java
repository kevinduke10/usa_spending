package com.tectonix.usaspending.services;

import com.tectonix.usaspending.domain.EncodedAgency;
import com.tectonix.usaspending.domain.EncodedLine;
import com.tectonix.usaspending.domain.MoneyLine;
import com.tectonix.usaspending.utils.SpendingUtils;
import org.locationtech.jts.util.Stopwatch;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/*
    "Filtered" contains csv with address rows with the correct length of array after a split on ","
    "Encoded" contains csv with index number and the result from an attempt to geoencode
    "Unique" contains a list of unique agency senders
 */

@Service
public class TectonixIngest {

    @Value("${dataDir}")
    String dataDir;

    @Value("${fromComputeDir}")
    String encodedDir;

    List<EncodedAgency> encodedAgencies = new ArrayList<>();

    Map<Integer,EncodedLine> encodedMap = new TreeMap<>();

    public void ingest() {
        File uniqueSenderFile = new File(dataDir + "/unique/encoded.csv");

        try (BufferedReader br = new BufferedReader(new FileReader(uniqueSenderFile))) {
            int idx = 0;
            String line;
            while ((line = br.readLine()) != null) {
                String[] split = line.split(",");
                if(idx != 0){
                    EncodedAgency ea = EncodedAgency.fromCSV(split);
                    if(ea != null){
                        encodedAgencies.add(EncodedAgency.fromCSV(split));
                    }
                }
                idx++;
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        List<File> encodedFiles = SpendingUtils.getAllCSVs(encodedDir);

        List<File> filteredCsvs = SpendingUtils.getAllCSVs(dataDir + "/filtered/");

        filteredCsvs.forEach(filteredFile -> {
            if(filteredFile.getName().contains("3_1")){
                File encoded = encodedFiles.get(0);
                setEncodedLines(encoded);
                populateTectonixIndex(filteredFile);
            }
        });
    }

    private void setEncodedLines(File encodedFile){
        try (BufferedReader br = new BufferedReader(new FileReader(encodedFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                EncodedLine encoded = EncodedLine.fromCSV(line.split(","));
                if(encoded != null){
                    encodedMap.put(encoded.getIndex(), encoded);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void populateTectonixIndex(File filteredFileToIngest){
        try (BufferedReader br = new BufferedReader(new FileReader(filteredFileToIngest))) {
            int idx = 0;
            String line;
            while ((line = br.readLine()) != null) {
                if(idx > 0){
                    MoneyLine ml = MoneyLine.fromFilteredCSV(line.split(","));
                    EncodedLine encodedLine = encodedMap.getOrDefault(idx,null);
                    if(encodedLine != null){
                        System.out.println("Original Address: " + ml.getAddress());
                        System.out.println("ENCODED: " + encodedLine.getResultAddress());
                        System.out.println("-------------");
                    }else{
                        System.out.println("failed");
                    }
                }
                idx++;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //TODO: Up next is to create the mappings for Tectonix.
    private void createMappings(){

    }
}

