package org.pgstyle.rst2.application;

public final class ApplicationException extends RuntimeException {

    public ApplicationException() {
        this(null);
    }

    public ApplicationException(String message) {
        this(message, null);
    }

    public ApplicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
