package aralinks.exception;

/** Thrown when a review rating is outside 1 to 5 stars. */
public class InvalidRatingException extends AraLinksException {
    public InvalidRatingException(int rating) {
        super("Rating must be 1 to 5 stars, not " + rating + ".");
    }
}
