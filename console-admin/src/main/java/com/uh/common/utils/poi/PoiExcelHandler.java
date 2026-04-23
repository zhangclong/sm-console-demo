package com.uh.common.utils.poi;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class PoiExcelHandler {
    protected Workbook workbook = null;

    protected File excelFile;

    private String excelPath;

    public PoiExcelHandler(String excelPath) {
        this.excelPath = excelPath;
        this.excelFile = new File(this.excelPath);

        if (!this.excelFile.exists()) {
            throw new RuntimeException("file not found! Please check the file path.");
        }

        try {
            getWorkBook(new FileInputStream(this.excelFile));
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }

    }

    public PoiExcelHandler(InputStream is) {
        try {
            getWorkBook(is);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public PoiExcelHandler(File file) {
        this.excelFile = file;
        try {
            getWorkBook(new FileInputStream(file));
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void handleExcel(PoiHandleExcel handleExcel) {
        try {
            handleExcel.handleExcel(this.workbook);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        } finally {

            if (this.workbook != null) {
                try {
                    this.workbook.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    private void getWorkBook(InputStream is) throws IOException {
        try {
            this.workbook = new XSSFWorkbook(is);
        } catch (Exception e) {
            this.workbook = new HSSFWorkbook(is);
        }
    }

    public boolean hasSheet(String sheetName) {
        return this.workbook.getSheet(sheetName) != null;
    }

    public Workbook getWorkbook(){
        return this.workbook;
    }

}
