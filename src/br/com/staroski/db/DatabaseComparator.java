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

    public void compareSchemas(Schema leftSchema, Schema rightSchema) throws Exception {
        debug("comparing schema %s.%s with %s.%s...",
              leftSchema.getCatalog().getDatabase().getAlias(),
              leftSchema.getName(),
              rightSchema.getCatalog().getDatabase().getAlias(),
              rightSchema.getName());
        SchemaDiff diff = leftSchema.compareWith(rightSchema);
        debug("    done!%n");

        debug("exporting excel report%n");
        exportExcel(diff);
        debug("report saved at: \"%s\"%n", excelFile.getAbsolutePath());
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

    private boolean createSchemaCellForTable(SchemaDiff diff, Workbook workbook, Sheet sheet, int line, String tableName) {
        Row row = sheet.createRow(line);
        Cell leftTypeCell = row.createCell(0);
        Cell leftTableCell = row.createCell(1);
        Cell rightTypeCell = row.createCell(2);
        Cell rightTableCell = row.createCell(3);

        boolean missingOnLeft = diff.isMissingOnLeft(tableName);
        boolean missingOnRight = diff.isMissingOnRight(tableName);
        if (missingOnLeft) {
            leftTypeCell.setCellStyle(cellStyleRed(workbook));
            leftTypeCell.setCellValue(MISSING);
            leftTableCell.setCellStyle(cellStyleRed(workbook));
            sheet.addMergedRegion(new CellRangeAddress(line, line, 0, 1));
        } else {
            Table table = diff.getLeftSchema().getTable(tableName);
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
            Table table = diff.getRightSchema().getTable(tableName);
            CellStyle cellStyle = missingOnLeft ? cellStyleYellow(workbook) : cellStyleGreen(workbook);
            rightTypeCell.setCellStyle(cellStyle);
            rightTypeCell.setCellValue(table.getType());
            rightTableCell.setCellStyle(cellStyle);
            rightTableCell.setCellValue(table.getName());
        }
        return !missingOnLeft && !missingOnRight;
    }

    private void createSchemaHeader(Workbook workbook, Sheet sheet, int line, SchemaDiff diff) {
        Row row = sheet.createRow(line);
        Cell leftTypeCell = row.createCell(0);
        Cell leftTableCell = row.createCell(1);
        Cell rightTypeCell = row.createCell(2);
        Cell rightTableCell = row.createCell(3);

        leftTypeCell.setCellStyle(cellStyleHeader(workbook));
        leftTableCell.setCellStyle(cellStyleHeader(workbook));
        rightTypeCell.setCellStyle(cellStyleHeader(workbook));
        rightTableCell.setCellStyle(cellStyleHeader(workbook));

        leftTypeCell.setCellValue("Schema " + diff.getLeftSchema().getName());
        sheet.addMergedRegion(new CellRangeAddress(line, line, 0, 3));
    }

    private void createSchemaHeaderForDatabase(Workbook workbook, Sheet sheet, int line, SchemaDiff diff) {
        Schema leftSchema = diff.getLeftSchema();
        Schema rightSchema = diff.getRightSchema();

        Row row = sheet.createRow(line);
        Cell leftTypeCell = row.createCell(0);
        Cell leftTableCell = row.createCell(1);
        Cell rightTypeCell = row.createCell(2);
        Cell rightTableCell = row.createCell(3);

        leftTypeCell.setCellStyle(cellStyleHeader(workbook));
        leftTableCell.setCellStyle(cellStyleHeader(workbook));
        rightTypeCell.setCellStyle(cellStyleHeader(workbook));
        rightTableCell.setCellStyle(cellStyleHeader(workbook));

        leftTypeCell.setCellValue(leftSchema.getCatalog().getDatabase().getAlias());
        sheet.addMergedRegion(new CellRangeAddress(line, line, 0, 1));

        rightTypeCell.setCellValue(rightSchema.getCatalog().getDatabase().getAlias());
        sheet.addMergedRegion(new CellRangeAddress(line, line, 2, 3));
    }

    private void createSchemaHeaderForTables(Workbook workbook, Sheet sheet, int line) {
        Row row = sheet.createRow(line);
        Cell leftTypeCell = row.createCell(0);
        Cell leftTableCell = row.createCell(1);
        Cell rightTypeCell = row.createCell(2);
        Cell rightTableCell = row.createCell(3);

        leftTypeCell.setCellStyle(cellStyleHeader(workbook));
        leftTableCell.setCellStyle(cellStyleHeader(workbook));
        rightTypeCell.setCellStyle(cellStyleHeader(workbook));
        rightTableCell.setCellStyle(cellStyleHeader(workbook));

        leftTypeCell.setCellValue("Type");
        leftTableCell.setCellValue("Name");
        rightTypeCell.setCellValue("Type");
        rightTableCell.setCellValue("Name");
    }

    private void createTableCellForColumn(TableDiff diff, Workbook workbook, Sheet sheet, int line, String columnName) {
        Row row = sheet.createRow(line);
        Cell leftColumnCell = row.createCell(0);
        Cell leftTypeCell = row.createCell(1);
        Cell leftLengthCell = row.createCell(2);
        Cell leftScaleCell = row.createCell(3);
        Cell rightColumnCell = row.createCell(4);
        Cell rightTypeCell = row.createCell(5);
        Cell rightLengthCell = row.createCell(6);
        Cell rightScaleCell = row.createCell(7);

        boolean different = !Table.areColumnsEquals(columnName, diff.getLeftTable(), diff.getRightTable());
        boolean missingOnLeft = diff.isMissingOnLeft(columnName);
        boolean missingOnRight = diff.isMissingOnRight(columnName);

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

    private void createTableHeader(Workbook workbook, Table leftTable, Sheet sheet, int line) {
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

        leftColumnCell.setCellValue("Table " + leftTable.getName());
        sheet.addMergedRegion(new CellRangeAddress(line, line, 0, 7));
    }

    private void createTableHeaderForColumns(Workbook workbook, Sheet sheet, int line) {
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

        leftColumnCell.setCellValue("Column");
        leftTypeCell.setCellValue("Type");
        leftLengthCell.setCellValue("Size");
        leftScaleCell.setCellValue("Scale");
        rightColumnCell.setCellValue("Column");
        rightTypeCell.setCellValue("Type");
        rightLengthCell.setCellValue("Size");
        rightScaleCell.setCellValue("Scale");
    }

    private void createTableHeaderForDatabase(Workbook workbook, Table leftTable, Table rightTable, Sheet sheet, int line) {
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

        leftColumnCell.setCellValue(leftTable.getSchema().getCatalog().getDatabase().getAlias());
        sheet.addMergedRegion(new CellRangeAddress(line, line, 0, 3));

        rightColumnCell.setCellValue(rightTable.getSchema().getCatalog().getDatabase().getAlias());
        sheet.addMergedRegion(new CellRangeAddress(line, line, 4, 7));
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

        Sheet sheet = workbook.createSheet(diff.getLeftSchema().getName());
        sheet.setColumnWidth(0, 25 * 256);
        sheet.setColumnWidth(1, 55 * 256);
        sheet.setColumnWidth(2, 25 * 256);
        sheet.setColumnWidth(3, 55 * 256);

        int line = -1;
        createSchemaHeader(workbook, sheet, ++line, diff);
        createSchemaHeaderForDatabase(workbook, sheet, ++line, diff);
        createSchemaHeaderForTables(workbook, sheet, ++line);

        for (String tableName : diff.getTableNames()) {
            ++line;
            boolean existsOnBothSides = createSchemaCellForTable(diff, workbook, sheet, line, tableName);
            if (existsOnBothSides) {
                Table leftTable = diff.getLeftSchema().getTable(tableName);
                Table rightTable = diff.getRightSchema().getTable(tableName);
                debug("comparing table %s...", tableName);
                TableDiff tableDiff = leftTable.compareWith(rightTable);
                debug("    done!%n", tableName);
                if (tableDiff.hasDifferences()) {
                    tableDiffs.add(tableDiff);
                }
            }
        }
        return tableDiffs;
    }

    private void exportTableDiff(TableDiff diff, Workbook workbook) {
        Table leftTable = diff.getLeftTable();
        Table rightTable = diff.getRightTable();

        Sheet sheet = workbook.createSheet(leftTable.getName());
        sheet.setColumnWidth(0, 40 * 256);
        sheet.setColumnWidth(1, 20 * 256);
        sheet.setColumnWidth(2, 10 * 256);
        sheet.setColumnWidth(3, 10 * 256);
        sheet.setColumnWidth(4, 40 * 256);
        sheet.setColumnWidth(5, 20 * 256);
        sheet.setColumnWidth(6, 10 * 256);
        sheet.setColumnWidth(7, 10 * 256);

        int line = -1;
        createTableHeader(workbook, leftTable, sheet, ++line);
        createTableHeaderForDatabase(workbook, leftTable, rightTable, sheet, ++line);
        createTableHeaderForColumns(workbook, sheet, ++line);

        List<String> columnNames = diff.getColumnNames();
        for (String columnName : columnNames) {
            createTableCellForColumn(diff, workbook, sheet, ++line, columnName);
        }
    }
}
