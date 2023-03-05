/*
 * Michael Morrison: March 4, 2023
 * 
 * Application written for EastBay Publishing to allow for automated extraction of PDF data using AWS tools
 * and infrastructure.
 * 
 */

package com.broyden;

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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;


public class Handler {
    private final TextractClient textractClient;

    public Handler() {
        textractClient = DependencyFactory.textractClient();
    }

    public void sendRequest() {
        // TODO: invoking the api calls using textractClient.
        System.out.println("!!!BROYDEN TEXTRACT EXAMPLE@@@");
        //detectDocText(textractClient, "/broyden/data_files/page_9_out.pdf");
        analyzeDoc(textractClient, "/broyden/data_files/page_19_out.pdf");
    }

    public static void analyzeDoc(TextractClient textractClient, String sourceDoc) {

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
            Query query1 = Query.builder()
                    .text("who is the check TO THE ORDER OF?")
                    .build();

            Query query2 = Query.builder()
                    .text("who is the check made out to?")
                    .build();

            Query query3 = Query.builder()
                    .text("what is the R/T Number?")
                    .build();

            queries.add(query1);
            queries.add(query2);
            queries.add(query3);

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
                if (type == "QUERY_RESULT") {
                    System.out.println("The block type is " +block.blockType().toString());
                    System.out.println(block.query().text());
                    System.out.println(block.text());
                    System.out.println("  ");
                }
            }

        } catch (TextractException | FileNotFoundException e) {

            System.err.println(e.getMessage());
            System.exit(1);
        }
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
