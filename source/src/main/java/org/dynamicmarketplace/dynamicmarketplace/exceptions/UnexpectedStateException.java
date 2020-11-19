package org.dynamicmarketplace.dynamicmarketplace.exceptions;

/**
 * Thrown when the state of a internal value is unexpected or illegal.
 * @author Geolykt
 */
public class UnexpectedStateException extends RuntimeException {

    /**
     * The serial UID of this class. I got no idea what this means.
     */
    private static final long serialVersionUID = 2079343165959868822L;

    public UnexpectedStateException() {
        this("No further information");
    }

    public UnexpectedStateException(String message) {
        super(message);
    }
}
