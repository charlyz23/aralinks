package aralinks.exception;

/**
 * Parent of every rule the system enforces. It is a checked exception, so the compiler
 * makes the caller (usually a screen) either handle it or pass it on.
 * The UI shows getMessage() in the red "Oops!" banner.
 */
public class AraLinksException extends Exception {
    public AraLinksException(String message) {
        super(message);
    }
}
