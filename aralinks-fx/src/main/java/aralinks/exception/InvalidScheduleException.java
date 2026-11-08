package aralinks.exception;

/** Thrown when a schedule is malformed, e.g. the end time is not after the start time. */
public class InvalidScheduleException extends AraLinksException {
    public InvalidScheduleException(String message) {
        super(message);
    }
}
