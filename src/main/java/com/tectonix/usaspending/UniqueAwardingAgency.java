package com.tectonix.usaspending;

import com.opencsv.CSVWriter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class UniqueAwardingAgency {

    static String filteredDataLocation = "/Users/Kev/Tectonix/data/spend/filtered/";

    public static void main(String[] args) {

        List<String> uniqueAwardingAgencies = new ArrayList<>();
        File filteredDataDir = new File(filteredDataLocation);
        File[] filteredFiles = filteredDataDir.listFiles();
        for(int i = 0; i < filteredFiles.length; i++){
            File filteredFile = filteredFiles[i];
            int idx = 0;
            try (BufferedReader br = new BufferedReader(new FileReader(filteredFile))) {
                String line;

                while ((line = br.readLine()) != null) {

                    if (idx != 0) {
                        String[] split = line.split(",");
                        if(split.length > 19){
                            String awardingAgencyName = split[14];
                            String awardingSubAgencyName = split[16];
                            String awardingOfficeName = split[18];
                            awardingSubAgencyName = awardingSubAgencyName.replaceAll("\"","")
                                    .replaceAll(",", "").trim();
                            if(!uniqueAwardingAgencies.contains(awardingSubAgencyName)){
                                uniqueAwardingAgencies.add(awardingSubAgencyName);
                            }
                        }else if(split.length == 1){
                            String awardingAgencyName = split[0];
                            awardingAgencyName = awardingAgencyName.replaceAll("\"","")
                                    .replaceAll(",", "").trim();
                            if(!uniqueAwardingAgencies.contains(awardingAgencyName)){
                                uniqueAwardingAgencies.add(awardingAgencyName);
                            }
                        }
                    }
                    idx++;
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        try {
            File uniqueSubAgencyFile = new File(filteredDataLocation + "uniqueagency.csv");
            FileWriter outWriter = new FileWriter(uniqueSubAgencyFile, false);
            CSVWriter csvWriter = new CSVWriter(outWriter, CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER);

            uniqueAwardingAgencies.sort(String::compareToIgnoreCase);
//            String[] headers = {"AWARDING_AGENCY", "AWARDING_SUB_AGENCY", "SUB_AGENCY_CODE"};
//            csvWriter.writeNext(headers);

            uniqueAwardingAgencies.forEach(agency -> {
                String[] writeMe = {agency};
                csvWriter.writeNext(writeMe);
            });

            csvWriter.flush();
            csvWriter.close();

        }catch(Exception e){
            e.printStackTrace();
        }
    }
}