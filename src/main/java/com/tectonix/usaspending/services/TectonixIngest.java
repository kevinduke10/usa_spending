package com.tectonix.usaspending.services;

import com.tectonix.usaspending.domain.EncodedAgency;
import com.tectonix.usaspending.domain.EncodedLine;
import com.tectonix.usaspending.domain.MoneyLine;
import com.tectonix.usaspending.utils.SpendingUtils;
import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.template.put.PutIndexTemplateRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.IndexTemplatesExistRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.mapper.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

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

    @Value("classpath:resources/recipient.json")
    File recipientJson;

    @Value("classpath:resources/sender.json")
    File senderJson;

    private final RestHighLevelClient client  = new RestHighLevelClient(RestClient.builder(new HttpHost("localhost", 9200)));

    List<EncodedAgency> encodedAgencies = new ArrayList<>();

    Map<Integer,EncodedLine> encodedMap = new TreeMap<>();

    private BulkRequest bulkRequest = new BulkRequest();
    private final int bulkSize = 1000;

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
        try {
            tryToCreateTemplate("sender");
            //tryToCreateTemplate("recipient");
        }catch(Exception e){
            e.printStackTrace();
            return;
        }

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

                        IndexRequest request = new IndexRequest("sender")
                                .source(getRelevantSenderFields(ml,encodedLine), XContentType.JSON);
                        bulkRequest.add(request);
                    }else{
                        System.out.println("failed");
                    }
                }
                if(bulkRequest.numberOfActions() == bulkSize) {
                    submitBulk();
                }
                idx++;
            }
            if(bulkRequest.numberOfActions() > 0){
                submitBulk();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    void tryToCreateTemplate(String name) throws IOException {
        if(true){
            System.out.println("Template doesn't exist, adding");
            File mappingFile = new File(name + ".json");
            boolean ack = this.client.indices().putTemplate(
                    new PutIndexTemplateRequest(name).source(readLineByLine(name + ".json"), XContentType.JSON), RequestOptions.DEFAULT).isAcknowledged();

            if(ack){
                System.out.println("Template pushed successfully");
            }else {
                System.out.println("Template push failed");
            }
        }else{
            System.out.println("Template already exists for " + name);
        }
    }

    private void submitBulk() {
        while(true) {
            try {
                BulkResponse response = client.bulk(bulkRequest, RequestOptions.DEFAULT);
                bulkRequest = new BulkRequest();
                return;
            } catch (IOException e) {
                try {
                    e.printStackTrace();
                    Thread.sleep(2000);
                } catch (InterruptedException intEx){
                    intEx.printStackTrace();
                }
            }
        }
    }

    public Map<String, Object> getRelevantSenderFields(MoneyLine ml, EncodedLine el){
        Map<String, Object> out = new HashMap<>();

        out.put("parentAwardAgencyName", ml.getParentAwardAgencyName());
        out.put("awardingAgencyName", ml.getAwardingAgencyName());
        out.put("awardingOfficeName", ml.getAwardingOfficeName());
        out.put("fundingAgencyName", ml.getFundingAgencyName());
        out.put("recipientName", ml.getRecipientName());
        out.put("recipientParentName", ml.getRecipientParentName());
        out.put("recipientStateName", ml.getRecipientStateName());
        out.put("totalDollarsObligated", ml.getTotalDollarsObligated());
        out.put("actionDate", ml.getActionDate());

        out.put("location", el.getLat() + "," + el.getLon());

        return out;
    }

    private static String readLineByLine(String filePath) {
        Resource resource = new ClassPathResource(filePath);
        StringBuilder contentBuilder = new StringBuilder();
        try (Stream<String> stream = Files.lines( Paths.get(resource.getFile().getAbsolutePath()), StandardCharsets.UTF_8)) {
            stream.forEach(s -> contentBuilder.append(s).append("\n"));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return contentBuilder.toString();
    }
}

