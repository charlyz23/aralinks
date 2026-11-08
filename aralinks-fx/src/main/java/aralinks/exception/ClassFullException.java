package aralinks.exception;

/** Thrown when a learner tries to join a class with no slots left. */
public class ClassFullException extends AraLinksException {
    public ClassFullException(int taken, int max) {
        super("No slots left (" + taken + "/" + max + "). Choose another class.");
    }
}
