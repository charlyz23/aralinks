package aralinks.exception;

/** Thrown when an SR-Code and password don't match an account. */
public class InvalidCredentialsException extends AraLinksException {
    public InvalidCredentialsException() {
        super("Wrong SR-Code or password.");
    }
}
