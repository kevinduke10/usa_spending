package com.tectonix.usaspending.services;

import com.opencsv.CSVWriter;
import com.tectonix.usaspending.domain.MoneyLine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CleanBadAddresses {

    @Value("${dataDir}")
    String dataDir;

    public void clean(){
        List<File> csvsToProcess = getAllCSVs(dataDir);
        File filteredFileDirectory = new File(dataDir + "filtered");
        filteredFileDirectory.mkdir();

        csvsToProcess.forEach(csvFile -> {
            try {
                File filteredAddressFile = new File(dataDir + "filtered/ " + csvFile.getName());
                filteredAddressFile.createNewFile();

                CSVWriter writer = new CSVWriter(new FileWriter(filteredAddressFile), CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER);

                int indexNum = 0;

                try (BufferedReader br = new BufferedReader(new FileReader(csvFile.getAbsoluteFile()))) {
                    String line;
                    int badVals = 0;
                    while ((line = br.readLine()) != null) {

                        if (indexNum != 0) {
                            String lineWithoutExtraCommas = removeExtraCommas(line);
                            String[] split = lineWithoutExtraCommas.split(",");

                            if (split.length == 261) {
                                MoneyLine ml = new MoneyLine(split);
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
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
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

    private static List<File> getAllCSVs(String dataLocation) {

        List<File> csvFiles = new ArrayList<>();

        File dataDirectory = new File(dataLocation);

        if (dataDirectory.isDirectory()) {
            File[] children = dataDirectory.listFiles();
            for (int i = 0; i < children.length; i++) {
                if (children[i].getAbsolutePath().endsWith(".csv")) {
                    csvFiles.add(children[i]);
                }
            }
        } else if (dataDirectory.getAbsolutePath().endsWith(".csv")) {
            csvFiles.add(dataDirectory);
        }

        return csvFiles;

    }
}

