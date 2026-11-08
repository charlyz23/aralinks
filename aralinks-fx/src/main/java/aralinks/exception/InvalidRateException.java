package aralinks.exception;

/** Thrown when a tutor sets a negative or unrealistic rate. */
public class InvalidRateException extends AraLinksException {
    public InvalidRateException(double rate) {
        super("Rate must be between PHP 0 and PHP 5,000, not PHP " + rate + ".");
    }
}
