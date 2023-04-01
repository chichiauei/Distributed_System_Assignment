package org.client2;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingDeque;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;

public class Main {

    public static CountDownLatch completed;


    private static void writeToExcel(String filePath, BlockingDeque<Double> throughputList) {
        try {
            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet("Throughput");

            int rowNum = 0;
            Row row = sheet.createRow(rowNum++);
            int colNum = 0;
            Cell cell = row.createCell(colNum++);
            cell.setCellValue("Second");
            cell = row.createCell(colNum++);
            cell.setCellValue("Throughput/second");

            for (int i = 0; i < throughputList.size(); i++) {
                row = sheet.createRow(rowNum++);
                colNum = 0;
                cell = row.createCell(colNum++);
                cell.setCellValue(i);
                cell = row.createCell(colNum++);
                cell.setCellValue(throughputList.poll());
            }

            FileOutputStream outputStream = new FileOutputStream(filePath);
            workbook.write(outputStream);
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void printPostLatencyStats(BlockingDeque<Long> postLatencies) {
        long min = Long.MAX_VALUE;
        long max = Long.MIN_VALUE;
        long sum = 0;

        for (long latency : postLatencies) {
            min = Math.min(min, latency);
            max = Math.max(max, latency);
            sum += latency;
        }

        double mean = (double) sum / postLatencies.size();

        System.out.println("Min latency for doGET: " + min + " milliseconds.");
        System.out.println("Mean latency for doGET: " + mean + " milliseconds.");
        System.out.println("Max latency for doGET: " + max + " milliseconds.");
    }


//    http://localhost:8080/
//    http://52.33.79.11:8080/Server_war

    public static void main(String[] args) throws InterruptedException {

        final int NUM_THREADS = 200;
        final String filePath = "C:\\Users\\chich\\OneDrive\\Desktop\\6650 A1\\Client2\\records.csv";
        final String filePathExcel = "C:\\Users\\chich\\OneDrive\\Desktop\\6650 A1\\Client2\\plot.xlsx";
        final String BASE_URL = "http://localhost:8080/Server_war_exploded";

        final RequestCounter counter = new RequestCounter();
        completed = new CountDownLatch(NUM_THREADS);


        BlockingDeque<Record> requestRecords = new LinkedBlockingDeque<>();
        BlockingDeque<Long> responseTimes = new LinkedBlockingDeque<>();
        BlockingDeque<Double> throughputList = new LinkedBlockingDeque<>();
        BlockingDeque<Long> postLatencies = new LinkedBlockingDeque<>();

        Instant start = Instant.now();
        for (int i = 0; i < NUM_THREADS; i++) {
            Client client = new Client(BASE_URL,requestRecords,postLatencies);
            Thread thread = new Thread(client);
            thread.start();
            counter.inc();
        }
        completed.await();
//        CsvExporter.exportToCsv(requestRecords, filePath);

        Instant end = Instant.now();
        Duration timeElapsed = Duration.between(start, end);
        double totalThroughput = Client.getSuccessfulThreadsCount()  / (double) timeElapsed.toSeconds() ;


        Timestamp lastTimestamp = requestRecords.getLast().getStartTime();
        Timestamp currentTimestamp = requestRecords.getFirst().getStartTime();
        long throughput = 0;

        while(!currentTimestamp.after(lastTimestamp)) {
            while(requestRecords.peek() != null && requestRecords.peek().getStartTime().before(currentTimestamp)) {
                Record record = requestRecords.poll();
                responseTimes.add(record.getLatency());
                throughput++;
            }
            throughputList.add((double) throughput);
            throughput = 0;
            currentTimestamp = new Timestamp(currentTimestamp.getTime() + 1000);
        }



//        writeToExcel(filePathExcel,throughputList);



        // Mean Response Time
        long sum = 0;
        for (long responseTime : responseTimes) {
            sum += responseTime;
        }
        double meanResponseTime = (double) sum / responseTimes.size();


        // Median Response Time
        ArrayList<Long> responseTimeList = new ArrayList<>(responseTimes);
        responseTimeList.sort(Long::compareTo);
        double medianResponseTime;
        if (responseTimeList.size() % 2 == 0) {
            medianResponseTime = ((double)responseTimeList.get(responseTimeList.size() / 2 - 1) + (double)responseTimeList.get(responseTimeList.size() / 2)) / 2;
        } else {
            medianResponseTime = (double)responseTimeList.get(responseTimeList.size() / 2);
        }

        // p99 Response Time
        double p99ResponseTime = responseTimeList.get((int) (responseTimeList.size() * 0.99));

        // Min and Max Response Time
        long minResponseTime = responseTimeList.get(0);
        long maxResponseTime = responseTimeList.get(responseTimeList.size() - 1);


        System.out.println("Value should be equal to " + NUM_THREADS + " It is: " + counter.getVal() + ".");
        System.out.println("There are " + Client.getSuccessfulThreadsCount() + " successful threads and " + Client.getFailedThreadsCount() + " failed threads.");
        System.out.println("Time taken: "+ timeElapsed.toMillis() +" milliseconds.");
        System.out.println("The total throughput in requests per second is " + totalThroughput + ".");
        System.out.println("The Mean Response Time is " + meanResponseTime + " milliseconds.");
        System.out.println("The Median Response Time is " + medianResponseTime + " milliseconds.");
        System.out.println("The p99 Response Time is " + p99ResponseTime + " milliseconds.");
        System.out.println("The Min Response Time is " + minResponseTime + " milliseconds.");
        System.out.println("The Max Response Time is " + maxResponseTime + " milliseconds.");
        printPostLatencyStats(postLatencies);
    }
}