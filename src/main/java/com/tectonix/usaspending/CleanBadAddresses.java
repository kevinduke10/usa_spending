package com.tectonix.usaspending;

import com.opencsv.CSVWriter;
import com.tectonix.domain.MoneyLine;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CleanBadAddresses {

    static String originalFileName = "/Users/Kev/Downloads/2019_all_Contracts_Full_20190513/2019_all_Contracts_Full_20190515_2.csv";
    static String filteredAddressFile = "/Users/Kev/Downloads/2019_all_Contracts_Full_20190513/2019_all_Contracts_Full_20190515_2_filtered.csv";

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
}

