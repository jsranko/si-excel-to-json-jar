package de.sranko_informatik.si_excel_to_json_jar_core;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.NumberToTextConverter;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

public class ExcelParser {

    public static JSONObject getJSONObject(MultipartFile file, ActionDataSheet actionData) throws IOException, IllegalStateException {

        Logger logger = LoggerFactory.getLogger(ExcelParser.class);

        //Create an object of FileInputStream class to read excel file
        InputStream inputStream = file.getInputStream();
        logger.debug(String.format("Datei %s (%s bytes) wird bearbeitet., ", file.getOriginalFilename(), file.getSize()));

        Workbook workbook = null;

        //Find the file extension by splitting file name in substring  and getting only extension name

        String fileExtensionName = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));

        //Check condition if the file is xlsx file
        if(fileExtensionName.equals(".xlsx")){

            //If it is xlsx file then create object of XSSFWorkbook class
            workbook = new XSSFWorkbook(inputStream);
            logger.debug(String.format("XSSFWorkbook erstellt, weil %s Extension ermittelt.", fileExtensionName));

        }

        //Check condition if the file is xls file
        else if(fileExtensionName.equals(".xls")){

            //If it is xls file then create object of HSSFWorkbook class
            workbook = new HSSFWorkbook(inputStream);
            logger.debug(String.format("HSSFWorkbook erstellt, weil %s Extension ermittelt.", fileExtensionName));

        }

        logger.debug(String.format("%s Sheets werden bearbeitet.", workbook.getNumberOfSheets()));

        JSONObject workbookJSON = new JSONObject();
        workbookJSON.put("name", file.getOriginalFilename());
        JSONObject sheetJSON = null;
        Sheet sheet = null;

        if (!Objects.isNull(actionData)) {

            sheet = workbook.getSheet(actionData.getSheet());

            workbookJSON = getSheetAsJSON(workbookJSON, sheet, actionData.getStart().getRow(), actionData.getStart().getColumn(), actionData.getFieldsToUpload());

        } else {

            //Create a root json object
            for (int s = 0; s < workbook.getNumberOfSheets(); s++) {

                //Read sheet inside the workbook by index
                sheet = workbook.getSheetAt(s);

                workbookJSON = getSheetAsJSON(workbookJSON, sheet, 0, 0, null);

            }

        }

        logger.debug(String.format("Workbook %s ist fertig", file.getOriginalFilename()));

        return workbookJSON;
    }

    private static JSONObject getSheetAsJSON(JSONObject workbookJSON, Sheet sheet, int startRow, int startColumn, String[] fieldsToUpload) throws IllegalStateException{

        ArrayList<Integer> fieldsNrToUpload = null;
        Logger logger = LoggerFactory.getLogger(ExcelParser.class);

        logger.debug(String.format("Sheet: %s wird von Zeile: %s und Splate: %s bearbeitet.", sheet.getSheetName(), startRow, startColumn));

        JSONArray sheetList = new JSONArray();

        //Find number of rows in excel file
        int rowCount = 0;
        int tableDataRow = 0;
        if (startRow == 0) {
            rowCount = sheet.getLastRowNum() - sheet.getFirstRowNum();
        } else {
            rowCount = sheet.getLastRowNum() - startRow;
        }

        logger.debug(String.format("%s Zeilen werden bearbeitet", rowCount));

        List<String> headerList = null;

        //First row in Excel ist always table head
        int headerRow = 0;
        if (startRow != 0) {
            headerRow = startRow - 1;
        }
        headerList = getTableColumns(sheet.getRow(headerRow), startColumn, fieldsToUpload, fieldsNrToUpload);
        logger.debug(String.format("Tablenkopf gefunden: %s.", headerList.stream().toString()));

        //Create a loop over all the rows of excel file to read it
        tableDataRow = headerRow + 1;
        for (int i = 0; i < rowCount; i++) {

            Row row = sheet.getRow(tableDataRow + i);

            if (isRowEmpty(row)) {
                continue;
            }
            List<ExcelColumn> rowColumnsList = null;
            try {
                rowColumnsList = getRowColumns(row, headerList.size(), fieldsNrToUpload);
            } catch (Exception e) {
                logger.debug(String.format("Error found: %s", e.toString()));
            }
            JSONObject jsonRow = new JSONObject();
            int index = 0;
            for (ExcelColumn column : rowColumnsList) {

                jsonRow.put(headerList.get(index), column.getValue());
                index += 1;
            }
            logger.debug(String.format("Zeile %s erstellt: %s", i, jsonRow.toString()));
            sheetList.put(jsonRow);
        }

        JSONObject sheetJSON = new JSONObject();
        sheetJSON.put("name", sheet.getSheetName());
        sheetJSON.put("data", sheetList);
        workbookJSON.append("sheets", sheetJSON);

        logger.debug(String.format("Sheet %s ist fertig. %s", sheet.getSheetName(), sheetList));

        return  workbookJSON;
    }

    public static boolean isTableHead (Workbook book, Cell cell) {
        if (cell == null) {
            return false;
        }
        CellStyle style = cell.getCellStyle();
        Font font = book.getFontAt(style.getFontIndex());
        if (font.getBold()) {
            return true;
        }
        return false;
    }

    public static List<String> getTableColumns (Row row, int column, String[] fieldsToUpload, ArrayList<Integer> fieldsNrToUpload) throws IllegalStateException{

        List<String> output = new ArrayList<String>();
        String spaltenName = new String();

        //Create a loop to print cell values in a row
        int startCol = column;
        for (int c = startCol; c < row.getLastCellNum(); c++) {

            //Print Excel data in console
            Cell cell = row.getCell(c);
            CellType cellType = cell.getCellType();
            if (cellType == CellType.FORMULA) {
                cellType = cell.getCachedFormulaResultType();
            }
            switch (cellType) {
                case BOOLEAN:
                    spaltenName = String.valueOf(cell.getBooleanCellValue());
                case NUMERIC:
                    spaltenName = String.valueOf(cell.getNumericCellValue());
                case STRING:
                    spaltenName = cell.getRichStringCellValue().getString();
            }

            if (!istStringImArray(fieldsToUpload, spaltenName)) {
               continue;
            };

            fieldsNrToUpload.add(c);
            output.add(spaltenName);
        }

        return output;
    }

    public static List<ExcelColumn> getRowColumns(Row row, int size, ArrayList<Integer> fieldsNrToUpload) {

        SimpleDateFormat sdf;
        sdf = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss.SSSXXX");
        sdf.setTimeZone(TimeZone.getTimeZone("CET"));

        List<ExcelColumn> output = new ArrayList<>();

        //Create a loop to print cell values in a row
        for (int c = 0; c < size; c++) {

            // machen nur, wenn man Felder zur Upload ausgewahlt hat
            if (fieldsNrToUpload.size() > 0 && fieldsNrToUpload != null) {
                if (!fieldsNrToUpload.contains(Integer.valueOf(c))) {
                    continue;
                }
            }

            //Print Excel data in console
            Cell cell = row.getCell(c);
            if (cell == null) {
                output.add(new ExcelColumn(ColumnType.CHAR, null, ""));
                continue;
            }
            CellType cellType = cell.getCellType();
            if (cellType == CellType.FORMULA) {
                cellType = cell.getCachedFormulaResultType();
            }
            switch (cellType) {
                case BOOLEAN:
                    output.add(new ExcelColumn(ColumnType.CHAR, null, String.valueOf(cell.getBooleanCellValue())));
                    break;
                case NUMERIC:
                    if (DateUtil.isCellDateFormatted(cell)) {
                        output.add(new ExcelColumn(ColumnType.NUMBER, null, sdf.format(cell.getDateCellValue())));
                    } else {
                        output.add(new ExcelColumn(ColumnType.NUMBER, null, NumberToTextConverter.toText(cell.getNumericCellValue())));
                    }

                    break;
                case STRING:
                    output.add(new ExcelColumn(ColumnType.VARCHAR, null, cell.getRichStringCellValue().getString()));
                    break;
                case BLANK:
                case _NONE:
                case ERROR:
                    output.add(new ExcelColumn(ColumnType.VARCHAR, null, ""));
                    break;
            }
        }

        return output;

    }

    private static boolean isRowEmpty(Row row) {
        boolean isEmpty = true;
        DataFormatter dataFormatter = new DataFormatter();

        if (row != null) {
            for (Cell cell : row) {
                if (dataFormatter.formatCellValue(cell).trim().length() > 0) {
                    isEmpty = false;
                    break;
                }
            }
        }

        return isEmpty;
    }

    // Methode zur Überprüfung, ob der String im Array ist
    public static boolean istStringImArray(String[] array, String zuSuchenderString) {
        if (array.length == 0 || array == null) {
            return true;
        }
        for (String element : array) {
            if (element.equals(zuSuchenderString)) {
                return true; // Der String wurde im Array gefunden
            }
        }
        return false; // Der String wurde im Array nicht gefunden
    }
}