package aralinks.exception;

/** Thrown when a classroom link is not a valid http(s) URL. */
public class InvalidLinkException extends AraLinksException {
    public InvalidLinkException(String link) {
        super("Classroom link must start with http:// or https:// (got \"" + link + "\").");
    }
}
