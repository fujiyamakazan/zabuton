package com.github.fujiyamakazan.zabuton.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PushbackInputStream;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.FileMagic;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class PoiUtils {

    public static class PoiBook {

        private FileInputStream fis;
        private PushbackInputStream pis = null;

        /**
         * Workbookを返します。
         */
        public Workbook getBook(File file) {

            try {
                final int length = 44;
                fis = new FileInputStream(file);
                pis = new PushbackInputStream(fis, length);
                byte[] data = new byte[length];
                pis.read(data, 0, length);
                pis.unread(data);
                FileMagic fm = FileMagic.valueOf(data);

                Workbook book;
                if (FileMagic.OOXML == fm) {
                    book = new XSSFWorkbook(pis);
                } else if (FileMagic.OLE2 == fm) {
                    book = new HSSFWorkbook(pis);
                } else {
                    book = WorkbookFactory.create(pis);
                }
                return book;

            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            // Jar化したときに失敗したことがある。
            //            Workbook book;
            //            this.fis = null;
            //            try {
            //                this.fis = new FileInputStream(file);
            //                book = WorkbookFactory.create(this.fis);
            //
            //            } catch (Exception e) {
            //                throw new RuntimeException(e);
            //            }
            //            return book;

        }

        /**
         * ストリームを閉じます。
         */
        public void close() {
            try {
                this.fis.close();
                this.fis.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    /**
     * 動作確認をします。
     */
    public static void main(String[] args) {

        File file = new File(EnvUtils.getUserDesktop(), "xxx.xls");

        try {
            try (FileInputStream in = new FileInputStream(file);
                Workbook wkbk1 = WorkbookFactory.create(in);) {

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
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static String getString(Cell cell) {
        return getString(cell, false);
    }

    /**
     * 文字列を取得します。
     */
    public static String getString(Cell cell, boolean deep) {
        //long start = System.currentTimeMillis();
        String cellValue = null;
        if (cell != null) {
            if (cell.getCellType().equals(CellType.STRING)) {
                cellValue = cell.getStringCellValue();
            } else if (cell.getCellType().equals(CellType.NUMERIC)) {
                cellValue = trimZero(cell.getNumericCellValue());
            } else if (cell.getCellType().equals(CellType.BLANK)) {
                cellValue = "";
            } else if (cell.getCellType().equals(CellType.FORMULA)) {
                if (deep) {
                    Workbook book = null;
                    try {
                        /* 計算結果のキャッシュを取得 */
                        book = cell.getSheet().getWorkbook();
                        CreationHelper helper = book.getCreationHelper();
                        FormulaEvaluator evaluator = helper.createFormulaEvaluator();
                        CellValue value = evaluator.evaluate(cell);
                        if (value.getCellType().equals(CellType.NUMERIC)) {
                            //cellValue = String.valueOf(value.getNumberValue());
                            cellValue = trimZero(value.getNumberValue());
                        } else if (value.getCellType().equals(CellType.STRING)) {
                            cellValue = value.getStringValue();
                        } else {
                            throw new RuntimeException("未知の形式：" + value.getCellType());
                        }
                    } finally {
                        try {
                            book.close();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                } else {
                    cellValue = cell.getCellFormula();
                }
            } else {
                throw new RuntimeException("未知の形式：" + cell.getCellType());
            }

        }

        //log.debug((System.currentTimeMillis()-start) + "[ms]" + cellValue);

        return cellValue;
    }

    /**
     * 「.0」を除去します。
     */
    public static String trimZero(double numericCellValue) {
        String cellValue;
        //double numericCellValue = cell.getNumericCellValue();
        cellValue = BigDecimal.valueOf(numericCellValue).toPlainString();
        if (cellValue.endsWith(".0")) {
            cellValue = cellValue.substring(0, cellValue.length() - 2);
        }
        return cellValue;
    }

    /**
     * 比較処理をします。
     */
    public static boolean equalsStringValue(Cell cell, String startKey) {
        return cell != null
            && cell.getCellType().equals(CellType.STRING)
            && cell.getStringCellValue().equals(startKey);
    }

    /**
     * 日付を返します。
     */
    public static Date toDateByExcelSerial(String cellValue) {

        /*
         * 1900/1/1のシリアルが1
         * 1900/2/29を考慮
         */
        int serial = Integer.parseInt(cellValue) - 1;
        serial -= (serial > 60 ? 1 : 0);
        Calendar cal = Calendar.getInstance();
        cal.set(1900, 0, 1, 0, 0, 0);
        cal.add(Calendar.DATE, serial);

        Date date = cal.getTime();

        date = DateUtils.truncate(date, Calendar.DAY_OF_MONTH);

        return date;
    }
}
