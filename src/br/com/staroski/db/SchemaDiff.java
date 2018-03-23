package br.com.staroski.db;

import java.util.Collections;
import java.util.List;

public final class SchemaDiff {

    private final Schema leftSchema;
    private final Schema rightSchema;
    private final List<Table> allTables;
    private final List<Table> leftMissingTables;
    private final List<Table> rightMissingTables;

    SchemaDiff(Schema schemaLeft, Schema schemaRight, List<Table> all, List<Table> missingLeft, List<Table> missingRight) {
        this.leftSchema = schemaLeft;
        this.rightSchema = schemaRight;
        this.allTables = Collections.unmodifiableList(all);
        this.leftMissingTables = Collections.unmodifiableList(missingLeft);
        this.rightMissingTables = Collections.unmodifiableList(missingRight);
    }

    public List<Table> getAllTables() {
        return allTables;
    }

    public List<Table> getLeftMissingTables() {
        return leftMissingTables;
    }

    public Schema getLeftSchema() {
        return leftSchema;
    }

    public List<Table> getRightMissingTables() {
        return rightMissingTables;
    }

    public Schema getRightSchema() {
        return rightSchema;
    }
}
