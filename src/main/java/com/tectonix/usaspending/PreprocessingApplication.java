package com.tectonix.usaspending;

import com.tectonix.usaspending.services.CleanBadAddresses;
import com.tectonix.usaspending.services.GeoencodeAddresses;
import com.tectonix.usaspending.services.UniqueAwardingAgency;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PreprocessingApplication implements CommandLineRunner {

    @Autowired
    CleanBadAddresses cleanService;

    @Autowired
    GeoencodeAddresses addressService;

    @Autowired
    UniqueAwardingAgency uniqueAwardingService;

    public static void main(String[] args) {
        SpringApplication.run(PreprocessingApplication.class, args);
    }

    @Override
    public void run(String... args){
        if(args.length == 0){
            System.out.println("Please provide a service to run (CLEAN/GEOENCODE/AWARDS)");
            System.exit(1);
        }
        switch(args[0].toUpperCase()){
            case "CLEAN":
                cleanService.clean();
                break;
            case "GEOENCODE":
                addressService.encode();
                break;
            case "AWARDS":
                uniqueAwardingService.createUniqueCSV();
                break;
            case "SENDERS":
                uniqueAwardingService.lookupSenders();
                break;
            default:
                System.out.println("Invalid arg passed for processing. CLEAN/GEOENCODE/AWARDS");
                System.exit(1);
        }
    }
}
