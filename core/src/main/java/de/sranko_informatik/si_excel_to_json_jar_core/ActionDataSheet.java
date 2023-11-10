package de.sranko_informatik.si_excel_to_json_jar_core;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ActionDataSheet {
    private String sheet;
    private ActionDataSheetStart start;
    private String[] fieldsToUpload;

    public ActionDataSheet() {
    }

    public ActionDataSheet(String sheet, ActionDataSheetStart start) {
        this.sheet = sheet;
        this.start = start;
    }

    public String getSheet() {
        return sheet;
    }

    public void setSheet(String sheet) {
        this.sheet = sheet;
    }

    public ActionDataSheetStart getStart() {
        return start;
    }

    public void setStart(ActionDataSheetStart start) {
        this.start = start;
    }

    public String[] getFieldsToUpload() {
        return fieldsToUpload;
    }

    public void setFieldsToUpload(String[] fieldsToUpload) {
        this.fieldsToUpload = fieldsToUpload;
    }

    @Override
    public String toString() {
        return "ActionDataSheet{" +
                "sheetName='" + sheet + '\'' +
                ", start=" + start +
                '}';
    }
}
