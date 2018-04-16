package br.com.staroski.db.export;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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

import br.com.staroski.UncheckedException;
import br.com.staroski.Utils;
import br.com.staroski.db.Column;
import br.com.staroski.db.Schema;
import br.com.staroski.db.SchemaDiff;
import br.com.staroski.db.Table;
import br.com.staroski.db.TableDiff;

public final class DiffExporter {

    private static final String MISSING = "MISSING";

    private static boolean areColumnsEquals(String columnName, List<Table> tables) {
        Column column1 = tables.get(0).getColumn(columnName);
        for (int i = 1; i < tables.size(); i++) {
            Column columnN = tables.get(i).getColumn(columnName);
            if (column1 == columnN) {
                continue;
            }
            if (column1 == null) {
                return false;
            }
            if (columnN == null) {
                return false;
            }
            if (!column1.getType().equals(columnN.getType())) {
                return false;
            }
            if (column1.getSize() != columnN.getSize()) {
                return false;
            }
            if (column1.getScale() != columnN.getScale()) {
                return false;
            }
        }
        return true;
    }

    private static void debug(String format, Object... args) {
        System.out.printf(format, args);
    }

    private CellStyle header;
    private CellStyle green;
    private CellStyle yellow;
    private CellStyle red;

    private String[] schemaColumnNames = new String[] { "Type", "Name" };
    private int[] schemaColumnWidths = new int[] { 25, 55 };

    private String[] tableColumnNames = new String[] { "Column", "Size", "Type", "Scale" };
    private int[] tableColumnWidths = new int[] { 40, 20, 10, 10 };

    public void exportExcel(File excel, SchemaDiff schemaDiff) {
        try {
            exportExcel(new FileOutputStream(excel), schemaDiff);
        } catch (FileNotFoundException fnfe) {
            throw UncheckedException.wrap(fnfe);
        }
    }

    public void exportExcel(OutputStream excel, SchemaDiff schemaDiff) {
        try {
            long start = System.currentTimeMillis();
            debug("exporting excel report...%n");
            Workbook workbook = new HSSFWorkbook();

            debug("creating sheet %s...", schemaDiff.schemas.get(0).getName());
            exportSchemaDiff(schemaDiff, workbook);
            debug("    done!%n");

            List<TableDiff> tableDiffs = new LinkedList<TableDiff>();
            for (String tableName : schemaDiff.tableNames) {
                debug("comparing table %s...", tableName);
                TableDiff tableDiff = schemaDiff.getTableDiffBetweenAllSchemas(tableName);
                debug("    done!%n");
                if (tableDiff != null) {
                    tableDiffs.add(tableDiff);
                }
            }
            for (TableDiff tableDiff : tableDiffs) {
                if (tableDiff.hasDifferences) {
                    debug("creating sheet %s...", tableDiff.tables.get(0).getName());
                    exportTableDiff(tableDiff, workbook);
                    debug("    done!%n");
                }
            }
            workbook.write(excel);
            workbook.close();
            debug("excel report exported!%n");

            long elapsed = System.currentTimeMillis() - start;
            debug("elapsed time: %s%n", Utils.formatInterval(elapsed));
        } catch (IOException ioe) {
            throw UncheckedException.wrap(ioe);
        }
    }

    public void exportExcel(String excel, SchemaDiff schemaDiff) {
        exportExcel(new File(excel), schemaDiff);
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
        for (int i = 0, count = diff.schemas.size(); i < count; i++) {
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
        for (int i = 0, count = diff.schemas.size(); i < count; i++) {
            for (int col = 0; col < schemaColumnWidths.length; col++) {
                sheet.setColumnWidth(++columnOffset, schemaColumnWidths[col] * 256);
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

            if (areColumnsEquals(columnName, diff.tables)) {
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
        for (int i = 0, count = diff.tables.size(); i < count; i++) {
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

    private Sheet createTableSheet(TableDiff diff, Workbook workbook) {
        Sheet sheet = workbook.createSheet(diff.tables.get(0).getName());
        int columnOffset = -1;
        for (int i = 0, count = diff.tables.size(); i < count; i++) {
            for (int col = 0; col < tableColumnWidths.length; col++) {
                sheet.setColumnWidth(++columnOffset, tableColumnWidths[col] * 256);
            }
        }
        return sheet;
    }

    private void exportSchemaDiff(SchemaDiff diff, Workbook workbook) {
        Sheet sheet = createSchemaSheet(diff, workbook);

        int line = -1;
        createSchemaHeader(workbook, sheet, ++line, diff);
        createSchemaHeaderForDatabase(workbook, sheet, ++line, diff);
        createSchemaHeaderForTables(workbook, sheet, ++line, diff);

        for (String tableName : diff.tableNames) {
            createSchemaCellForTable(diff, workbook, sheet, ++line, tableName);
        }
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
