package aralinks.exception;

/** Thrown when a requested time is outside the tutor's availability. */
public class UnavailableScheduleException extends AraLinksException {
    public UnavailableScheduleException(String message) {
        super(message);
    }
}
