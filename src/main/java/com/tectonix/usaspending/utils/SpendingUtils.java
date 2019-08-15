package com.tectonix.usaspending.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SpendingUtils {

    public static List<File> getAllCSVs(String dataLocation) {
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
