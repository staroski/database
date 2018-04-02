package br.com.staroski.db;

import java.util.Collections;
import java.util.List;

public final class SchemaDiff {

    private final Schema leftSchema;
    private final Schema rightSchema;
    private final List<Table> allTables;
    private final List<Table> leftMissingTables;
    private final List<Table> rightMissingTables;

    SchemaDiff(Schema leftSchema,
               Schema rightSchema,
               List<Table> allTables,
               List<Table> leftMissingTables,
               List<Table> rightMissingTables) {
        this.leftSchema = leftSchema;
        this.rightSchema = rightSchema;
        Collections.sort(allTables);
        Collections.sort(leftMissingTables);
        Collections.sort(rightMissingTables);
        this.allTables = Collections.unmodifiableList(allTables);
        this.leftMissingTables = Collections.unmodifiableList(leftMissingTables);
        this.rightMissingTables = Collections.unmodifiableList(rightMissingTables);
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

    public boolean hasDifferences() {
        return !(leftMissingTables.isEmpty() && rightMissingTables.isEmpty());
    }
}
