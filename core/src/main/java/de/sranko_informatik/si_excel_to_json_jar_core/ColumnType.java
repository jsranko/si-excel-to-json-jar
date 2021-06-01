package de.sranko_informatik.si_excel_to_json_jar_core;

public enum ColumnType {

    VARCHAR("Varchar"),
    NUMBER("Decimal"),
    CHAR("Char");

    public final String value;

    private ColumnType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
