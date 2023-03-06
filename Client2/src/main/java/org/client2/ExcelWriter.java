package org.client2;

import java.io.FileOutputStream;
import java.io.IOException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelWriter {

    public static void ExcelWriter(double[] time, double[] throughput) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Throughput Data");

        for (int i = 0; i < time.length; i++) {
            Row row = sheet.createRow(i);
            Cell timeCell = row.createCell(0);
            timeCell.setCellValue(time[i]);

            Cell throughputCell = row.createCell(1);
            throughputCell.setCellValue(throughput[i]);
        }

        FileOutputStream fileOut = new FileOutputStream("throughput.xlsx");
        workbook.write(fileOut);
        fileOut.close();
        workbook.close();
    }
}