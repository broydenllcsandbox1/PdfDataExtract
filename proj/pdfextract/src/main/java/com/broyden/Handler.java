/*
 * Michael Morrison: March 4, 2023
 * 
 * Application written for EastBay Publishing to allow for automated extraction of PDF data using AWS tools
 * and infrastructure.
 * 
 */

package com.broyden;

import com.broyden.ValueConfidence;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.textract.TextractClient;
import software.amazon.awssdk.services.textract.model.Document;
import software.amazon.awssdk.services.textract.model.DetectDocumentTextRequest;
import software.amazon.awssdk.services.textract.model.DetectDocumentTextResponse;
import software.amazon.awssdk.services.textract.model.Block;
import software.amazon.awssdk.services.textract.model.DocumentMetadata;
import software.amazon.awssdk.services.textract.model.TextractException;
import software.amazon.awssdk.services.textract.model.FeatureType;
import software.amazon.awssdk.services.textract.model.AnalyzeDocumentRequest;
import software.amazon.awssdk.services.textract.model.AnalyzeDocumentResponse;
import software.amazon.awssdk.services.textract.model.Query;
import software.amazon.awssdk.services.textract.model.QueriesConfig;
import software.amazon.awssdk.services.textract.model.Relationship;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Collection;
import java.util.ArrayList;
import java.util.HashMap;

public class Handler {
    private final TextractClient textractClient;

    public Handler() {
        textractClient = DependencyFactory.textractClient();
    }

    public void sendRequest() {
        HashMap fields = new HashMap<String, String>();
        fields.put("Name", "who is the check made out to?");
        fields.put("CheckNumber", "what is the Serial Number?");
        fields.put("Amount", "what is the Amount?");
        fields.put("Date", "what is the Processing Date?");

        String outputFile = "/broyden/CheckRegister.csv";
        List<String> queries = new ArrayList<String>(fields.values());

        File directoryPath = new File("/broyden/data_files/");
        File filesList[] = directoryPath.listFiles();
        //filesList = new File[] {new File("/broyden/data_files/page_32_out.pdf")};
        int totalFiles = filesList.length;
        int counter = 1;
        for(File file : filesList) {
            System.out.println("File name: "+file.getName());
            System.out.println(counter++ + " of " + totalFiles);
            HashMap<String, ValueConfidence> results = analyzeDoc(textractClient, file.getAbsolutePath(), queries);
            
            ValueConfidence checkNumber = results.get(fields.get("CheckNumber"));
            ValueConfidence name = results.get(fields.get("Name"));
            ValueConfidence amount = results.get(fields.get("Amount"));
            ValueConfidence date = results.get(fields.get("Date"));

            WriteValuesToFile(outputFile, checkNumber, name, amount, date, file.getName());
         }
    }

    private static String val(ValueConfidence vc) {
        if (vc == null) {
            return "null";
        }

        return vc.getValue();
    }

    private static String conf(ValueConfidence vc) {
        if (vc == null) {
            return "null";
        }

        return vc.getConfidence();
    }

    private static void WriteValuesToFile(String file, ValueConfidence checkNumber, ValueConfidence name, ValueConfidence amount, ValueConfidence date, String fileName) {
        try {
            FileWriter fw = new FileWriter(file, true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(val(checkNumber) + "," + val(name) + ",\"" + val(amount) + "\"," + val(date)+ "," + fileName + "," + conf(checkNumber) + "," + conf(name) + "," + conf(amount) + "," + conf(date));
            bw.newLine();
            bw.close();
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    public static HashMap<String, ValueConfidence> analyzeDoc(TextractClient textractClient, String sourceDoc, List<String> queryList) {
        HashMap<String, ValueConfidence> map = new HashMap<String, ValueConfidence>();

        try {
            InputStream sourceStream = new FileInputStream(new File(sourceDoc));
            SdkBytes sourceBytes = SdkBytes.fromInputStream(sourceStream);

            // Get the input Document object as bytes
            Document myDoc = Document.builder()
                    .bytes(sourceBytes)
                    .build();

            List<FeatureType> featureTypes = new ArrayList<FeatureType>();
            featureTypes.add(FeatureType.QUERIES);
            featureTypes.add(FeatureType.FORMS);
            featureTypes.add(FeatureType.TABLES);

            List<Query> queries = new ArrayList<Query>();

            for (String query : queryList) {
                queries.add(Query.builder()
                    .text(query)
                    .build());
            }

            QueriesConfig queriesConfig = QueriesConfig.builder()
                    .queries(queries)
                    .build();

            AnalyzeDocumentRequest analyzeDocumentRequest = AnalyzeDocumentRequest.builder()
                    .featureTypes(featureTypes)
                    .document(myDoc)
                    .queriesConfig(queriesConfig)
                    .build();

            AnalyzeDocumentResponse analyzeDocument = textractClient.analyzeDocument(analyzeDocumentRequest);
            List<Block> docInfo = analyzeDocument.blocks();
            Iterator<Block> blockIterator = docInfo.iterator();

            while(blockIterator.hasNext()) {
                Block block = blockIterator.next();
                String type = block.blockType().toString();
                if (type == "QUERY") {
                    List<Relationship> relationships = block.relationships();
                    for (Relationship relationship : relationships) {
                        List<String> relatedBlockIds = relationship.ids();
                        String relatedBlockId = relatedBlockIds.get(0);
                        Block relatedBlock = docInfo.stream()
                                                .filter(item -> relatedBlockId.equals(item.id()))
                                                .findAny()
                                                .orElse(null);
                        map.put(block.query().text(), new ValueConfidence(relatedBlock.text(), relatedBlock.confidence()));
                    }
                }
            }

        } catch (TextractException | FileNotFoundException e) {

            System.err.println(e.getMessage());
            System.exit(1);
        }

        return map;
    }

    public static void detectDocText(TextractClient textractClient,String sourceDoc) {

        try {
            InputStream sourceStream = new FileInputStream(new File(sourceDoc));
            SdkBytes sourceBytes = SdkBytes.fromInputStream(sourceStream);

            // Get the input Document object as bytes
            Document myDoc = Document.builder()
                .bytes(sourceBytes)
                .build();

            DetectDocumentTextRequest detectDocumentTextRequest = DetectDocumentTextRequest.builder()
                .document(myDoc)
                .build();

            // Invoke the Detect operation
            DetectDocumentTextResponse textResponse = textractClient.detectDocumentText(detectDocumentTextRequest);
            List<Block> docInfo = textResponse.blocks();
            for (Block block : docInfo) {
                System.out.println("The block type is " + block.blockType().toString());
                String type = block.blockType().toString();
                if (type == "WORD") {
                    System.out.println("The block text " + block.text());
                    System.out.println("The block text type " + block.textTypeAsString());
                    System.out.println("The block bounding box " + block.geometry().boundingBox().toString());
                }
            }

            DocumentMetadata documentMetadata = textResponse.documentMetadata();
            System.out.println("The number of pages in the document is " +documentMetadata.pages());

        } catch (TextractException | FileNotFoundException e) {

            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
}
