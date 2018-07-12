package br.com.staroski;

/**
 * Used to wrap checked exceptions into unchecked exceptions
 * 
 * @author Ricardo Artur Staroski
 */
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
