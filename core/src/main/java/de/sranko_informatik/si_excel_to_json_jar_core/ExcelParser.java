package de.sranko_informatik.si_excel_to_json_jar_core;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.NumberToTextConverter;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ExcelParser {

    public static JSONObject getJSONObject(MultipartFile file) throws IOException{

        //Create an object of FileInputStream class to read excel file
        InputStream inputStream = file.getInputStream();

        Workbook workbook = null;

        //Find the file extension by splitting file name in substring  and getting only extension name

        String fileExtensionName = file.getOriginalFilename().substring(file.getOriginalFilename().indexOf("."));

        //Check condition if the file is xlsx file
        if(fileExtensionName.equals(".xlsx")){

            //If it is xlsx file then create object of XSSFWorkbook class
            workbook = new XSSFWorkbook(inputStream);

        }

        //Check condition if the file is xls file
        else if(fileExtensionName.equals(".xls")){

            //If it is xls file then create object of HSSFWorkbook class
            workbook = new HSSFWorkbook(inputStream);

        }

        JSONObject workbookJSON = new JSONObject();

        //Create a root json object
        for (int s = 0; s < workbook.getNumberOfSheets(); s++){

            //List<List<String>> sheetList = new ArrayList<List<String>>();

            //Read sheet inside the workbook by index
            Sheet sheet = workbook.getSheetAt(s);

            List<ExcelColumn> rowList;
            JSONArray sheetList = new JSONArray();

            //Find number of rows in excel file
            int rowCount = sheet.getLastRowNum()-sheet.getFirstRowNum();

            List<String> headerList = null;

            //Create a loop over all the rows of excel file to read it
            for (int i = 0; i < rowCount+1; i++) {

                Row row = sheet.getRow(i);

                if (isTableHead(workbook, row.getCell(0))) {
                    headerList = getTableColumns(row);
                } else {
                    List<ExcelColumn> rowColumnsList = getRowColumns(row);
                    JSONObject jsonRow = new JSONObject();
                    int index = 0;
                    for (ExcelColumn column: rowColumnsList) {
                        jsonRow.put(headerList.get(index) ,column.getValue());
//                        switch (column.getDataType()){
//                            case CHAR, VARCHAR, NUMBER -> jsonRow.put(headerList.get(index) ,column.getValue());
//                        }
                        index += 1;
                    }
                    sheetList.put(jsonRow);
                }
            }

            JSONObject sheetJSON = new JSONObject();
            sheetJSON.put("name", sheet.getSheetName());
            sheetJSON.put("data", sheetList);
            workbookJSON.append("sheets", sheetJSON);
            workbookJSON.put("name", file.getOriginalFilename());

        }

        return workbookJSON;
    }

    public static boolean isTableHead (Workbook book, Cell cell) {
        CellStyle style = cell.getCellStyle();
        Font font = book.getFontAt(style.getFontIndex());
        if (font.getBold()) {
            return true;
        }
        return false;
    }

    public static List<String> getTableColumns (Row row){

        List<String> output = new ArrayList<String>();

        //Create a loop to print cell values in a row
        for (int c = 0; c < row.getLastCellNum(); c++) {

            //Print Excel data in console
            Cell cell = row.getCell(c);
            CellType cellType = cell.getCellType();
            if (cellType == CellType.FORMULA) {
                cellType = cell.getCachedFormulaResultType();
            }
            switch (cellType) {
                case BOOLEAN:
                    output.add(String.valueOf(cell.getBooleanCellValue()));
                case NUMERIC:
                    output.add(String.valueOf(cell.getNumericCellValue()));
                case STRING:
                    output.add(cell.getRichStringCellValue().getString());
            }
        }

        return output;
    }

    public static List<ExcelColumn> getRowColumns(Row row) {

        List<ExcelColumn> output = new ArrayList<>();

        //Create a loop to print cell values in a row
        for (int c = 0; c < row.getLastCellNum(); c++) {

            //Print Excel data in console
            Cell cell = row.getCell(c);
            CellType cellType = cell.getCellType();
            if (cellType == CellType.FORMULA) {
                cellType = cell.getCachedFormulaResultType();
            }
            switch (cellType) {
                case BOOLEAN:
                    output.add(new ExcelColumn(ColumnType.CHAR, null, String.valueOf(cell.getBooleanCellValue())));
                    break;
                case NUMERIC:
                    output.add(new ExcelColumn(ColumnType.NUMBER, null, NumberToTextConverter.toText(cell.getNumericCellValue())));
                    break;
                case STRING:
                    output.add(new ExcelColumn(ColumnType.VARCHAR, null, cell.getRichStringCellValue().getString()));
                    break;
            }
        }

        return output;

    }
}