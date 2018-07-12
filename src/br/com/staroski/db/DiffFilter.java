package br.com.staroski.db;

/**
 * This class represents a filter to be used when comparing databases<br>
 * It is possible to append filters using the {@link #and(DiffFilter) and} method
 * 
 * @author Ricardo Artur Staroski
 */
public class DiffFilter {

    public static DiffFilter ignoreColumnName(final String nameToIgnore) {
        return new DiffFilter() {

            @Override
            public boolean acceptColumn(String name) {
                return !name.equalsIgnoreCase(nameToIgnore);
            }
        };
    }

    private static class CompositeFilter extends DiffFilter {

        private final DiffFilter a;
        private final DiffFilter b;

        CompositeFilter(DiffFilter a, DiffFilter b) {
            this.a = a;
            this.b = b;
        }

        @Override
        protected boolean acceptTable(String name) {
            return a.acceptTable(name) && b.acceptTable(name);
        }

        @Override
        protected boolean acceptColumn(String name) {
            return a.acceptColumn(name) && b.acceptColumn(name);
        }
    }

    protected boolean acceptTable(String name) {
        return true;
    }

    protected boolean acceptColumn(String name) {
        return true;
    }

    public DiffFilter and(DiffFilter otherFilter) {
        return new CompositeFilter(this, otherFilter);
    }
}