package br.com.staroski;

public final class UncheckedException extends RuntimeException {

    private static final long serialVersionUID = 1;

    public static RuntimeException wrap(Throwable t) {
        if (t instanceof RuntimeException) {
            return (RuntimeException) t;
        }
        return new UncheckedException(t);
    }

    private UncheckedException(Throwable t) {
        super(t.getMessage(), t);
    }
}
