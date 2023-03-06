package org.client2;


import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.BlockingDeque;

public class CsvExporter {

    public static void exportToCsv(BlockingDeque<Record> records, String fileName) {
        try (FileWriter writer = new FileWriter(fileName)) {
            // Write the header row
            writer.append("startTime, requestType, latency, responseCode");
            writer.append('\n');

            // Write the data rows
            for (Record record : records) {
                writer.append(record.getStartTime().toString());
                writer.append(',');
                writer.append(record.getRequestType());
                writer.append(',');
                writer.append(String.valueOf(record.getLatency()));
                writer.append(',');
                writer.append(String.valueOf(record.getResponseCode()));
                writer.append('\n');
            }

            System.out.println("CSV file was created successfully.");
        } catch (IOException e) {
            System.out.println("An error occurred while writing to the CSV file: " + e.getMessage());
        }
    }

}