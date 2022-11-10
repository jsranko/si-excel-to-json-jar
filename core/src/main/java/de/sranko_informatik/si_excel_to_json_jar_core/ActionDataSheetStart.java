package de.sranko_informatik.si_excel_to_json_jar_core;

public class ActionDataSheetStart {

    private int row;
    private int column;

    public ActionDataSheetStart() {
    }

    public ActionDataSheetStart(int row, int column) {
        this.row = row;
        this.column = column;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    @Override
    public String toString() {
        return "ActionDataSheetStart{" +
                "row=" + row +
                ", column=" + column +
                '}';
    }
}
