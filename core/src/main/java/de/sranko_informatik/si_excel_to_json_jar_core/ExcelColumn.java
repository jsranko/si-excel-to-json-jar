package de.sranko_informatik.si_excel_to_json_jar_core;

public class ExcelColumn {
    private ColumnType dataType;
    private String name;
    private String value;

    public ExcelColumn(ColumnType dataType, String name, String value) {
        this.dataType = dataType;
        this.name = name;
        this.value = value;
    }

    public void setDataType(ColumnType dataType) {
        this.dataType = dataType;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ColumnType getDataType() {
        return dataType;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

}
