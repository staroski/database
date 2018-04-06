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

    private String[] schemaColumnNames = new String[] { "Type", "Name" };
    private int[] schemaColumnWidths = new int[] { 25, 55 };

    private String[] tableColumnNames = new String[] { "Column", "Size", "Type", "Scale" };
    private int[] tableColumnWidths = new int[] { 40, 20, 10, 10 };

    public DatabaseComparator(File excelFile) {
        this.excelFile = excelFile;
    }

    public DatabaseComparator(String excelFile) {
        this(new File(excelFile));
    }

    public void compareSchemas(Schema schema1, Schema schema2, Schema... schemaN) throws Exception {
        StringBuilder text = new StringBuilder();
        for (Schema schema : Utils.asList(schema1, schema2, schemaN)) {
            if (text.length() > 1) {
                text.append(", ");
            }
            text.append(schema.getCatalog().getDatabase().getAlias());
            text.append(".");
            text.append(schema.getName());
        }

        debug("comparing schemas %s...", text.toString());
        SchemaDiff diff = schema1.compareWith(schema2, schemaN);
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

    private void createSchemaCellForTable(SchemaDiff diff, Workbook workbook, Sheet sheet, int line, String tableName) {
        CellStyle greenStyle = cellStyleGreen(workbook);
        CellStyle yellowStyle = cellStyleYellow(workbook);
        CellStyle redStyle = cellStyleRed(workbook);

        Row row = sheet.createRow(line);

        int rangeStart = 0;
        int rangeEnd = 0;
        int columnOffset = -1;
        for (Schema schema : diff.schemas) {
            Cell typeCell = row.createCell(++columnOffset);
            Cell tableCell = row.createCell(++columnOffset);
            rangeEnd = columnOffset;
            Table table = schema.getTable(tableName);
            final CellStyle style;
            final String stringType;
            final String stringName;
            if (diff.allSchemasContains(tableName)) {
                style = greenStyle;
                stringType = table.getType();
                stringName = table.getName();
            } else if (schema.contains(tableName)) {
                style = yellowStyle;
                stringType = table.getType();
                stringName = table.getName();
            } else {
                style = redStyle;
                stringType = MISSING;
                stringName = MISSING;
                sheet.addMergedRegion(new CellRangeAddress(line, line, rangeStart, rangeEnd));
            }
            rangeStart = rangeEnd + 1;
            typeCell.setCellStyle(style);
            tableCell.setCellStyle(style);
            typeCell.setCellValue(stringType);
            tableCell.setCellValue(stringName);
        }
    }

    private void createSchemaHeader(Workbook workbook, Sheet sheet, int line, SchemaDiff diff) {
        CellStyle headerStyle = cellStyleHeader(workbook);
        Row row = sheet.createRow(line);
        int columnOffset = -1;
        for (Schema schema : diff.schemas) {
            Cell typeCell = row.createCell(++columnOffset);
            Cell tableCell = row.createCell(++columnOffset);
            typeCell.setCellStyle(headerStyle);
            tableCell.setCellStyle(headerStyle);
            typeCell.setCellValue("Schema " + schema.getName());
        }
        sheet.addMergedRegion(new CellRangeAddress(line, line, 0, columnOffset));
    }

    private void createSchemaHeaderForDatabase(Workbook workbook, Sheet sheet, int line, SchemaDiff diff) {
        CellStyle headerStyle = cellStyleHeader(workbook);
        Row row = sheet.createRow(line);

        int rangeStart = 0;
        int rangeEnd = 0;
        int columnOffset = -1;
        for (Schema schema : diff.schemas) {
            Cell typeCell = row.createCell(++columnOffset);
            Cell tableCell = row.createCell(++columnOffset);
            typeCell.setCellStyle(headerStyle);
            tableCell.setCellStyle(headerStyle);
            typeCell.setCellValue(schema.getCatalog().getDatabase().getAlias());
            rangeEnd = columnOffset;
            sheet.addMergedRegion(new CellRangeAddress(line, line, rangeStart, rangeEnd));
            rangeStart = rangeEnd + 1;
        }
    }

    private void createSchemaHeaderForTables(Workbook workbook, Sheet sheet, int line, SchemaDiff diff) {
        CellStyle headerStyle = cellStyleHeader(workbook);
        Row row = sheet.createRow(line);
        int columnOffset = 0;
        for (Schema schema : diff.schemas) {
            Cell typeCell = row.createCell(columnOffset++);
            Cell tableCell = row.createCell(columnOffset++);
            typeCell.setCellStyle(headerStyle);
            tableCell.setCellStyle(headerStyle);
            typeCell.setCellValue(schemaColumnNames[0]);
            tableCell.setCellValue(schemaColumnNames[1]);
        }
    }

    private Sheet createSchemaSheet(SchemaDiff diff, Workbook workbook) {
        Sheet sheet = workbook.createSheet(diff.schemas.get(0).getName());
        int columnOffset = -1;
        for (Schema schema : diff.schemas) {
            for (int i = 0; i < schemaColumnWidths.length; i++) {
                sheet.setColumnWidth(++columnOffset, schemaColumnWidths[i] * 256);
            }
        }
        return sheet;
    }

    private void createTableCellForColumn(TableDiff diff, Workbook workbook, Sheet sheet, int line, String columnName) {
        CellStyle greenStyle = cellStyleGreen(workbook);
        CellStyle yellowStyle = cellStyleYellow(workbook);
        CellStyle redStyle = cellStyleRed(workbook);

        Row row = sheet.createRow(line);

        int rangeStart = 0;
        int rangeEnd = 0;
        int columnOffset = -1;
        for (Table table : diff.tables) {
            Cell columnCell = row.createCell(++columnOffset);
            Cell typeCell = row.createCell(++columnOffset);
            Cell lengthCell = row.createCell(++columnOffset);
            Cell scaleCell = row.createCell(++columnOffset);
            rangeEnd = columnOffset;
            CellStyle style;
            String stringColumn;
            String stringType;
            int length;
            int scale;

            if (Table.areColumnsEquals(columnName, diff.tables)) {
                style = greenStyle;
                Column column = table.getColumn(columnName);
                stringColumn = column.getName();
                stringType = column.getType();
                length = column.getSize();
                scale = column.getScale();
            } else if (table.contains(columnName)) {
                style = yellowStyle;
                Column column = table.getColumn(columnName);
                stringColumn = column.getName();
                stringType = column.getType();
                length = column.getSize();
                scale = column.getScale();
            } else {
                style = redStyle;
                stringColumn = MISSING;
                stringType = MISSING;
                length = 0;
                scale = 0;
                sheet.addMergedRegion(new CellRangeAddress(line, line, rangeStart, rangeEnd));
            }
            rangeStart = rangeEnd + 1;

            columnCell.setCellStyle(style);
            typeCell.setCellStyle(style);
            lengthCell.setCellStyle(style);
            scaleCell.setCellStyle(style);

            columnCell.setCellValue(stringColumn);
            typeCell.setCellValue(stringType);
            if (length > 0) {
                lengthCell.setCellValue(length);
            }
            if (scale > 0) {
                scaleCell.setCellValue(scale);
            }
        }
    }

    private void createTableHeader(Workbook workbook, TableDiff diff, Sheet sheet, int line) {
        CellStyle headerStyle = cellStyleHeader(workbook);
        Row row = sheet.createRow(line);

        int columnOffset = -1;
        for (Table table : diff.tables) {
            Cell columnCell = row.createCell(++columnOffset);
            Cell typeCell = row.createCell(++columnOffset);
            Cell lengthCell = row.createCell(++columnOffset);
            Cell scaleCell = row.createCell(++columnOffset);

            columnCell.setCellStyle(headerStyle);
            typeCell.setCellStyle(headerStyle);
            lengthCell.setCellStyle(headerStyle);
            scaleCell.setCellStyle(headerStyle);

            columnCell.setCellValue("Table " + table.getName());
        }
        sheet.addMergedRegion(new CellRangeAddress(line, line, 0, columnOffset));
    }

    private void createTableHeaderForColumns(Workbook workbook, TableDiff diff, Sheet sheet, int line) {
        CellStyle headerStyle = cellStyleHeader(workbook);
        Row row = sheet.createRow(line);

        int columnOffset = -1;
        for (Table table : diff.tables) {
            Cell columnCell = row.createCell(++columnOffset);
            Cell typeCell = row.createCell(++columnOffset);
            Cell lengthCell = row.createCell(++columnOffset);
            Cell scaleCell = row.createCell(++columnOffset);

            columnCell.setCellStyle(headerStyle);
            typeCell.setCellStyle(headerStyle);
            lengthCell.setCellStyle(headerStyle);
            scaleCell.setCellStyle(headerStyle);

            columnCell.setCellValue(tableColumnNames[0]);
            typeCell.setCellValue(tableColumnNames[1]);
            lengthCell.setCellValue(tableColumnNames[2]);
            scaleCell.setCellValue(tableColumnNames[3]);
        }
    }

    private void createTableHeaderForDatabase(Workbook workbook, TableDiff diff, Sheet sheet, int line) {
        CellStyle headerStyle = cellStyleHeader(workbook);
        Row row = sheet.createRow(line);

        int rangeStart = 0;
        int rangeEnd = 0;
        int columnOffset = -1;
        for (Table table : diff.tables) {
            Cell columnCell = row.createCell(++columnOffset);
            Cell typeCell = row.createCell(++columnOffset);
            Cell lengthCell = row.createCell(++columnOffset);
            Cell scaleCell = row.createCell(++columnOffset);

            columnCell.setCellStyle(headerStyle);
            typeCell.setCellStyle(headerStyle);
            lengthCell.setCellStyle(headerStyle);
            scaleCell.setCellStyle(headerStyle);

            columnCell.setCellValue(table.getSchema().getCatalog().getDatabase().getAlias());

            rangeEnd = columnOffset;
            sheet.addMergedRegion(new CellRangeAddress(line, line, rangeStart, rangeEnd));
            rangeStart = rangeEnd + 1;
        }
    }

    private Sheet createTableSheet(TableDiff tableDiff, Workbook workbook) {
        Sheet sheet = workbook.createSheet(tableDiff.tables.get(0).getName());
        int columnOffset = -1;
        for (Table table : tableDiff.tables) {
            for (int i = 0; i < tableColumnWidths.length; i++) {
                sheet.setColumnWidth(++columnOffset, tableColumnWidths[i] * 256);
            }
        }
        return sheet;
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

        Sheet sheet = createSchemaSheet(diff, workbook);

        int line = -1;
        createSchemaHeader(workbook, sheet, ++line, diff);
        createSchemaHeaderForDatabase(workbook, sheet, ++line, diff);
        createSchemaHeaderForTables(workbook, sheet, ++line, diff);

        for (String tableName : diff.tableNames) {
            ++line;
            createSchemaCellForTable(diff, workbook, sheet, line, tableName);

            List<Schema> schemasWithTable = diff.getSchemasWithTable(tableName);
            if (schemasWithTable.size() > 1) {
                Table table = schemasWithTable.get(0).getTable(tableName);
                List<Table> otherTables = new LinkedList<Table>();
                for (int i = 1; i < schemasWithTable.size(); i++) {
                    otherTables.add(schemasWithTable.get(i).getTable(tableName));
                }
                debug("comparing table %s...", tableName);
                TableDiff tableDiff = table.compareWith(otherTables);
                debug(" done!%n", tableName);
                if (tableDiff.hasDifferences) {
                    tableDiffs.add(tableDiff);
                }
            }
        }
        return tableDiffs;
    }

    private void exportTableDiff(TableDiff diff, Workbook workbook) {
        Sheet sheet = createTableSheet(diff, workbook);

        int line = -1;
        createTableHeader(workbook, diff, sheet, ++line);
        createTableHeaderForDatabase(workbook, diff, sheet, ++line);
        createTableHeaderForColumns(workbook, diff, sheet, ++line);

        for (String columnName : diff.columnNames) {
            createTableCellForColumn(diff, workbook, sheet, ++line, columnName);
        }
    }
}
