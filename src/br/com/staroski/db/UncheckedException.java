package br.com.staroski.db;

public final class UncheckedException extends RuntimeException {

    private static final long serialVersionUID = 1;

    public static UncheckedException wrap(Throwable t) {
        if (t instanceof UncheckedException) {
            return (UncheckedException) t;
        }
        return new UncheckedException(t);
    }

    private UncheckedException(Throwable t) {
        super(t.getMessage(), t);
    }
}
