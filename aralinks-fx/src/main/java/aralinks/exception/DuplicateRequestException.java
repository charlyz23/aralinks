package aralinks.exception;

/** Thrown when a learner already has a pending request for the same thing. */
public class DuplicateRequestException extends AraLinksException {
    public DuplicateRequestException(String message) {
        super(message);
    }
}
