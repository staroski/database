package br.com.staroski.db;

import java.io.File;
import java.io.FileOutputStream;
import java.util.LinkedList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor.HSSFColorPredefined;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;

public final class DatabaseComparator {

    private static final String MISSING = "MISSING";

    private static void debug(String format, Object... args) {
        System.out.printf(format, args);
    }

    private final File excelFile;
    private CellStyle green;
    private CellStyle red;
    private CellStyle yellow;
    private CellStyle header;

    public DatabaseComparator(File excelFile) {
        this.excelFile = excelFile;
    }

    public DatabaseComparator(String excelFile) {
        this(new File(excelFile));
    }

    public void compare(Database leftDatabase, Database rightDatabase, String catalogName, String schemaName) throws Exception {
        try {
            debug("comparing database %s with %s%n", leftDatabase.getAlias(), rightDatabase.getAlias());
            Catalog leftCatalog = leftDatabase.getCatalog(catalogName);
            Schema leftSchema = leftCatalog.getSchema(schemaName);

            Catalog rightCatalog = rightDatabase.getCatalog(catalogName);
            Schema rightSchema = rightCatalog.getSchema(schemaName);
            debug("comparing schema %s%n", leftSchema.getName());

            SchemaDiff diff = leftSchema.compareWith(rightSchema);
            exportExcel(diff);
            debug("finished%n");
            debug("report saved at: \"%s\"%n", excelFile.getAbsolutePath());
        } finally {
            leftDatabase.disconnect();
        }
    }

    private boolean areColumnsEquals(String columnName, Table leftTable, Table rightTable) {
        Column leftColumn = leftTable.getColumn(columnName);
        Column rightColumn = rightTable.getColumn(columnName);
        if (leftColumn == rightColumn) {
            return true;
        }
        if (leftColumn == null) {
            return false;
        }
        if (rightColumn == null) {
            return false;
        }
        if (!leftColumn.getType().equals(rightColumn.getType())) {
            return false;
        }
        if (leftColumn.getSize() != rightColumn.getSize()) {
            return false;
        }
        if (leftColumn.getScale() != rightColumn.getScale()) {
            return false;
        }
        return true;
    }

    private CellStyle cellStyleGreen(Workbook workbook) {
        if (green == null) {
            green = workbook.createCellStyle();
            green.setFillForegroundColor(HSSFColorPredefined.LIGHT_GREEN.getIndex());
            green.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            green.setBorderBottom(BorderStyle.THIN);
            green.setBorderLeft(BorderStyle.THIN);
            green.setBorderRight(BorderStyle.THIN);
            green.setBorderTop(BorderStyle.THIN);
        }
        return green;
    }

    private CellStyle cellStyleHeader(Workbook workbook) {
        if (header == null) {
            header = workbook.createCellStyle();
            header.setFillForegroundColor(HSSFColorPredefined.GREY_25_PERCENT.getIndex());
            header.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            header.setAlignment(HorizontalAlignment.CENTER);

            header.setBorderBottom(BorderStyle.THIN);
            header.setBorderLeft(BorderStyle.THIN);
            header.setBorderRight(BorderStyle.THIN);
            header.setBorderTop(BorderStyle.THIN);

            Font font = workbook.createFont();
            font.setBold(true);
            header.setFont(font);
        }
        return header;
    }

    private CellStyle cellStyleRed(Workbook workbook) {
        if (red == null) {
            red = workbook.createCellStyle();
            red.setFillForegroundColor(HSSFColorPredefined.CORAL.getIndex());
            red.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            red.setAlignment(HorizontalAlignment.CENTER);

            red.setBorderBottom(BorderStyle.THIN);
            red.setBorderLeft(BorderStyle.THIN);
            red.setBorderRight(BorderStyle.THIN);
            red.setBorderTop(BorderStyle.THIN);
        }
        return red;
    }

    private CellStyle cellStyleYellow(Workbook workbook) {
        if (yellow == null) {
            yellow = workbook.createCellStyle();
            yellow.setFillForegroundColor(HSSFColorPredefined.LIGHT_YELLOW.getIndex());
            yellow.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            yellow.setBorderBottom(BorderStyle.THIN);
            yellow.setBorderLeft(BorderStyle.THIN);
            yellow.setBorderRight(BorderStyle.THIN);
            yellow.setBorderTop(BorderStyle.THIN);
        }
        return yellow;
    }

    private void exportExcel(SchemaDiff schemaDiff) throws Exception {
        Workbook workbook = new HSSFWorkbook();
        List<TableDiff> tableDiffs = exportSchemaDiff(schemaDiff, workbook);
        for (TableDiff tableDiff : tableDiffs) {
            exportTableDiff(tableDiff, workbook);
        }
        workbook.write(new FileOutputStream(excelFile));
        workbook.close();
    }

    private List<TableDiff> exportSchemaDiff(SchemaDiff diff, Workbook workbook) {
        List<TableDiff> tableDiffs = new LinkedList<TableDiff>();
        String schemaName = diff.getLeftSchema().getName();

        Sheet sheet = workbook.createSheet(schemaName);
        sheet.setColumnWidth(0, 20 * 256);
        sheet.setColumnWidth(1, 50 * 256);
        sheet.setColumnWidth(2, 20 * 256);
        sheet.setColumnWidth(3, 50 * 256);

        Schema leftSchema = diff.getLeftSchema();
        Schema rightSchema = diff.getRightSchema();

        int line = 0;
        Row row = sheet.createRow(line);
        Cell leftTypeCell = row.createCell(0);
        Cell leftTableCell = row.createCell(1);
        Cell rightTypeCell = row.createCell(2);
        Cell rightTableCell = row.createCell(3);

        leftTypeCell.setCellStyle(cellStyleHeader(workbook));
        leftTableCell.setCellStyle(cellStyleHeader(workbook));
        rightTypeCell.setCellStyle(cellStyleHeader(workbook));
        rightTableCell.setCellStyle(cellStyleHeader(workbook));

        leftTypeCell.setCellValue("Schema " + schemaName);
        sheet.addMergedRegion(new CellRangeAddress(line, line, 0, 3));

        row = sheet.createRow(++line);
        leftTypeCell = row.createCell(0);
        leftTableCell = row.createCell(1);
        rightTypeCell = row.createCell(2);
        rightTableCell = row.createCell(3);

        leftTypeCell.setCellStyle(cellStyleHeader(workbook));
        leftTableCell.setCellStyle(cellStyleHeader(workbook));
        rightTypeCell.setCellStyle(cellStyleHeader(workbook));
        rightTableCell.setCellStyle(cellStyleHeader(workbook));

        leftTypeCell.setCellValue(leftSchema.getCatalog().getDatabase().getAlias());
        sheet.addMergedRegion(new CellRangeAddress(line, line, 0, 1));

        rightTypeCell.setCellValue(rightSchema.getCatalog().getDatabase().getAlias());
        sheet.addMergedRegion(new CellRangeAddress(line, line, 2, 3));

        row = sheet.createRow(++line);
        leftTypeCell = row.createCell(0);
        leftTableCell = row.createCell(1);
        rightTypeCell = row.createCell(2);
        rightTableCell = row.createCell(3);

        leftTypeCell.setCellStyle(cellStyleHeader(workbook));
        leftTableCell.setCellStyle(cellStyleHeader(workbook));
        rightTypeCell.setCellStyle(cellStyleHeader(workbook));
        rightTableCell.setCellStyle(cellStyleHeader(workbook));

        leftTypeCell.setCellValue("Type");
        leftTableCell.setCellValue("Name");
        rightTypeCell.setCellValue("Type");
        rightTableCell.setCellValue("Name");

        List<Table> all = diff.getAllTables();
        List<Table> leftMissing = diff.getLeftMissingTables();
        List<Table> rightMissing = diff.getRightMissingTables();

        for (Table table : all) {
            row = sheet.createRow(++line);
            leftTypeCell = row.createCell(0);
            leftTableCell = row.createCell(1);
            rightTypeCell = row.createCell(2);
            rightTableCell = row.createCell(3);

            boolean missingOnLeft = leftMissing.contains(table);
            boolean missingOnRight = rightMissing.contains(table);

            if (missingOnLeft) {
                leftTypeCell.setCellStyle(cellStyleRed(workbook));
                leftTypeCell.setCellValue(MISSING);
                leftTableCell.setCellStyle(cellStyleRed(workbook));
                sheet.addMergedRegion(new CellRangeAddress(line, line, 0, 1));
            } else {
                CellStyle cellStyle = missingOnRight ? cellStyleYellow(workbook) : cellStyleGreen(workbook);
                leftTypeCell.setCellStyle(cellStyle);
                leftTypeCell.setCellValue(table.getType());
                leftTableCell.setCellStyle(cellStyle);
                leftTableCell.setCellValue(table.getName());
            }

            if (missingOnRight) {
                rightTypeCell.setCellStyle(cellStyleRed(workbook));
                rightTypeCell.setCellValue(MISSING);
                rightTableCell.setCellStyle(cellStyleRed(workbook));
                sheet.addMergedRegion(new CellRangeAddress(line, line, 2, 3));
            } else {
                CellStyle cellStyle = missingOnLeft ? cellStyleYellow(workbook) : cellStyleGreen(workbook);
                rightTypeCell.setCellStyle(cellStyle);
                rightTypeCell.setCellValue(table.getType());
                rightTableCell.setCellStyle(cellStyle);
                rightTableCell.setCellValue(table.getName());
            }

            if (!missingOnLeft && !missingOnRight) {
                String name = table.getName();
                Table leftTable = leftSchema.getTable(name);
                Table rightTable = rightSchema.getTable(name);
                debug("comparing table %s%n", leftTable.getName());
                TableDiff tableDiff = leftTable.compareWith(rightTable);
                if (tableDiff.hasDifferences()) {
                    tableDiffs.add(tableDiff);
                }
            }
        }
        return tableDiffs;
    }

    private void exportTableDiff(TableDiff diff, Workbook workbook) {
        Table leftSchema = diff.getLeftTable();
        Table rightSchema = diff.getRightTable();

        Sheet sheet = workbook.createSheet(leftSchema.getName());
        sheet.setColumnWidth(0, 50 * 256);
        sheet.setColumnWidth(1, 20 * 256);
        sheet.setColumnWidth(2, 10 * 256);
        sheet.setColumnWidth(3, 10 * 256);
        sheet.setColumnWidth(4, 50 * 256);
        sheet.setColumnWidth(5, 20 * 256);
        sheet.setColumnWidth(6, 10 * 256);
        sheet.setColumnWidth(7, 10 * 256);

        int line = 0;
        Row row = sheet.createRow(line);
        Cell leftColumnCell = row.createCell(0);
        Cell leftTypeCell = row.createCell(1);
        Cell leftLengthCell = row.createCell(2);
        Cell leftScaleCell = row.createCell(3);
        Cell rightColumnCell = row.createCell(4);
        Cell rightTypeCell = row.createCell(5);
        Cell rightLengthCell = row.createCell(6);
        Cell rightScaleCell = row.createCell(7);

        leftColumnCell.setCellStyle(cellStyleHeader(workbook));
        leftTypeCell.setCellStyle(cellStyleHeader(workbook));
        leftLengthCell.setCellStyle(cellStyleHeader(workbook));
        leftScaleCell.setCellStyle(cellStyleHeader(workbook));
        rightColumnCell.setCellStyle(cellStyleHeader(workbook));
        rightTypeCell.setCellStyle(cellStyleHeader(workbook));
        rightLengthCell.setCellStyle(cellStyleHeader(workbook));
        rightScaleCell.setCellStyle(cellStyleHeader(workbook));

        leftColumnCell.setCellValue("Table " + leftSchema.getName());
        sheet.addMergedRegion(new CellRangeAddress(line, line, 0, 7));

        row = sheet.createRow(++line);
        leftColumnCell = row.createCell(0);
        leftTypeCell = row.createCell(1);
        leftLengthCell = row.createCell(2);
        leftScaleCell = row.createCell(3);
        rightColumnCell = row.createCell(4);
        rightTypeCell = row.createCell(5);
        rightLengthCell = row.createCell(6);
        rightScaleCell = row.createCell(7);

        leftColumnCell.setCellStyle(cellStyleHeader(workbook));
        leftTypeCell.setCellStyle(cellStyleHeader(workbook));
        leftLengthCell.setCellStyle(cellStyleHeader(workbook));
        leftScaleCell.setCellStyle(cellStyleHeader(workbook));
        rightColumnCell.setCellStyle(cellStyleHeader(workbook));
        rightTypeCell.setCellStyle(cellStyleHeader(workbook));
        rightLengthCell.setCellStyle(cellStyleHeader(workbook));
        rightScaleCell.setCellStyle(cellStyleHeader(workbook));

        leftColumnCell.setCellValue(leftSchema.getSchema().getCatalog().getDatabase().getAlias());
        sheet.addMergedRegion(new CellRangeAddress(line, line, 0, 3));

        rightColumnCell.setCellValue(rightSchema.getSchema().getCatalog().getDatabase().getAlias());
        sheet.addMergedRegion(new CellRangeAddress(line, line, 4, 7));

        row = sheet.createRow(++line);
        leftColumnCell = row.createCell(0);
        leftTypeCell = row.createCell(1);
        leftLengthCell = row.createCell(2);
        leftScaleCell = row.createCell(3);
        rightColumnCell = row.createCell(4);
        rightTypeCell = row.createCell(5);
        rightLengthCell = row.createCell(6);
        rightScaleCell = row.createCell(7);

        leftColumnCell.setCellStyle(cellStyleHeader(workbook));
        leftTypeCell.setCellStyle(cellStyleHeader(workbook));
        leftLengthCell.setCellStyle(cellStyleHeader(workbook));
        leftScaleCell.setCellStyle(cellStyleHeader(workbook));
        rightColumnCell.setCellStyle(cellStyleHeader(workbook));
        rightTypeCell.setCellStyle(cellStyleHeader(workbook));
        rightLengthCell.setCellStyle(cellStyleHeader(workbook));
        rightScaleCell.setCellStyle(cellStyleHeader(workbook));

        leftColumnCell.setCellValue("Column");
        leftTypeCell.setCellValue("Type");
        leftLengthCell.setCellValue("Size");
        leftScaleCell.setCellValue("Scale");
        rightColumnCell.setCellValue("Column");
        rightTypeCell.setCellValue("Type");
        rightLengthCell.setCellValue("Size");
        rightScaleCell.setCellValue("Scale");

        List<Column> all = diff.getAllColumns();
        List<Column> leftMissing = diff.getLeftMissingColumns();
        List<Column> rightMissing = diff.getRightMissingColumns();

        for (Column column : all) {
            String columnName = column.getName();
            row = sheet.createRow(++line);
            leftColumnCell = row.createCell(0);
            leftTypeCell = row.createCell(1);
            leftLengthCell = row.createCell(2);
            leftScaleCell = row.createCell(3);
            rightColumnCell = row.createCell(4);
            rightTypeCell = row.createCell(5);
            rightLengthCell = row.createCell(6);
            rightScaleCell = row.createCell(7);

            boolean different = !areColumnsEquals(columnName, diff.getLeftTable(), diff.getRightTable());
            boolean missingOnLeft = leftMissing.contains(column);
            boolean missingOnRight = rightMissing.contains(column);

            if (missingOnLeft) {
                CellStyle cellStyle = cellStyleRed(workbook);
                leftColumnCell.setCellStyle(cellStyle);
                leftTypeCell.setCellStyle(cellStyle);
                leftLengthCell.setCellStyle(cellStyle);
                leftScaleCell.setCellStyle(cellStyle);
                leftColumnCell.setCellValue(MISSING);
                sheet.addMergedRegion(new CellRangeAddress(line, line, 0, 3));
            } else {
                CellStyle cellStyle = different || missingOnRight ? cellStyleYellow(workbook) : cellStyleGreen(workbook);
                leftColumnCell.setCellStyle(cellStyle);
                leftTypeCell.setCellStyle(cellStyle);
                leftLengthCell.setCellStyle(cellStyle);
                leftScaleCell.setCellStyle(cellStyle);

                Column leftColumn = diff.getLeftTable().getColumn(columnName);
                leftColumnCell.setCellValue(leftColumn.getName());
                leftTypeCell.setCellValue(leftColumn.getType());
                leftLengthCell.setCellValue(leftColumn.getSize());
                int scale = leftColumn.getScale();
                if (scale > 0) {
                    leftScaleCell.setCellValue(scale);
                }
            }

            if (missingOnRight) {
                CellStyle cellStyle = cellStyleRed(workbook);
                rightColumnCell.setCellStyle(cellStyle);
                rightTypeCell.setCellStyle(cellStyle);
                rightLengthCell.setCellStyle(cellStyle);
                rightScaleCell.setCellStyle(cellStyle);
                rightColumnCell.setCellValue(MISSING);
                sheet.addMergedRegion(new CellRangeAddress(line, line, 4, 7));
            } else {
                CellStyle cellStyle = different || missingOnLeft ? cellStyleYellow(workbook) : cellStyleGreen(workbook);
                rightColumnCell.setCellStyle(cellStyle);
                rightTypeCell.setCellStyle(cellStyle);
                rightLengthCell.setCellStyle(cellStyle);
                rightScaleCell.setCellStyle(cellStyle);

                Column rightColumn = diff.getRightTable().getColumn(columnName);
                rightColumnCell.setCellValue(rightColumn.getName());
                rightTypeCell.setCellValue(rightColumn.getType());
                rightLengthCell.setCellValue(rightColumn.getSize());
                int scale = rightColumn.getScale();
                if (scale > 0) {
                    rightScaleCell.setCellValue(scale);
                }
            }
        }
    }
}
