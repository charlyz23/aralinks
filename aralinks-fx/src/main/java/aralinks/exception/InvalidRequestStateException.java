package aralinks.exception;

/** Thrown when accepting/declining/cancelling a request that is no longer pending. */
public class InvalidRequestStateException extends AraLinksException {
    public InvalidRequestStateException(String message) {
        super(message);
    }
}
