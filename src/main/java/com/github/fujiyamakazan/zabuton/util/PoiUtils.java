package com.github.fujiyamakazan.zabuton.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

public class PoiUtils {

    public static void main(String[] args) {

        File file = new File(EnvUtils.getUserDesktop(), "xxx.xls");
        Workbook wkbk1;

        try {
            FileInputStream in = new FileInputStream(file);
            wkbk1 = WorkbookFactory.create(in);
            Sheet sheet1 = wkbk1.getSheet("Sheet1");

            for (int i = 1; i < sheet1.getPhysicalNumberOfRows(); i++) {
                Row row1 = sheet1.getRow(i);
                Cell cell1 = row1.getCell(1);

                if (cell1.getCellType().equals(CellType.NUMERIC)) {
                    System.out.println(cell1.getNumericCellValue());
                } else {
                    System.out.println(cell1);
                }


            }



        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }



    }

}
