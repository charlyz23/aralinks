package aralinks.model;

import aralinks.exception.DuplicateRequestException;
import aralinks.exception.InvalidRatingException;
import aralinks.exception.InvalidRequestStateException;
import aralinks.exception.InvalidScheduleException;
import aralinks.exception.UnavailableScheduleException;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A student who searches for and requests tutoring.
 * UML: Learner extends User; Learner 1 — * Request (submits); TutoringClass ◇— Learner (enrolls);
 *      TutoringSession ◇— Learner (attended by); Review * — 1 Learner (written by).
 * Owner: Kennard.
 */
public class Learner extends User {
    private final List<Request> requestsMade = new ArrayList<>();
    private final List<TutoringSession> sessionHistory = new ArrayList<>();
    private int yearLevel;
    private String program;

    public Learner(String userId, String srCode, String name, String email, String password,
                   String university, int yearLevel, String program) {
        super(userId, srCode, name, email, password, university);
        this.yearLevel = yearLevel;
        this.program = program;
    }

    @Override public String getRole() { return "Learner"; }

    @Override
    public List<String> displayMenu() {
        return List.of("Browse Classes", "My Classes", "Sessions", "Profile", "Logout");
    }

    /**
     * Tutors who are active, teach the course (null = any course) and charge at most maxRate.
     * TODO (Kennard): loop over allTutors and keep the matches (feature: Tutor Search and Filtering).
     */
    public List<Tutor> searchTutors(List<Tutor> allTutors, Course course, double maxRate) {
        throw new UnsupportedOperationException("TODO: Learner.searchTutors");
    }

    /**
     * A readable profile: name, courses, rate, availability, average rating.
     * UML says void; returning text lets a screen (or a test) show it.
     * TODO (Kennard): build the string from the tutor's getters.
     */
    public String viewTutorProfile(Tutor tutor) {
        throw new UnsupportedOperationException("TODO: Learner.viewTutorProfile");
    }

    /**
     * Sends a private-session request to a tutor and records it in requestsMade.
     * TODO (Kennard), in this order:
     *   1. end must be after start                         → InvalidScheduleException
     *   2. tutor.isAvailable(day, start, end) must be true → UnavailableScheduleException
     *   3. no PENDING request to the same tutor already    → DuplicateRequestException
     *   4. new Request(requestId, this, tutor, course, day, start, end, message), add it, return it
     */
    public Request submitRequest(String requestId, Tutor tutor, Course course, DayOfWeek day,
                                 LocalTime start, LocalTime end, String message)
            throws InvalidScheduleException, UnavailableScheduleException, DuplicateRequestException {
        throw new UnsupportedOperationException("TODO: Learner.submitRequest");
    }

    /** TODO (Kennard): the request must be one of mine; then call request.cancel(). */
    public void cancelRequest(Request request) throws InvalidRequestStateException {
        throw new UnsupportedOperationException("TODO: Learner.cancelRequest");
    }

    /**
     * Rates a completed session.
     * TODO (Kennard):
     *   - the session must be COMPLETED and include this learner (else IllegalStateException)
     *   - return new Review(reviewId, session, this, rating, comment)
     *     (the Review constructor already throws InvalidRatingException for bad ratings)
     */
    public Review rateSession(String reviewId, TutoringSession session, int rating, String comment)
            throws InvalidRatingException {
        throw new UnsupportedOperationException("TODO: Learner.rateSession");
    }

    /** Called by TutoringSession when this learner is added to it. Package-private on purpose. */
    void addToHistory(TutoringSession session) {
        if (!sessionHistory.contains(session)) sessionHistory.add(session);
    }

    // ---------- getters ----------
    public List<Request> getRequestsMade()             { return Collections.unmodifiableList(requestsMade); }
    public List<TutoringSession> getSessionHistory()   { return Collections.unmodifiableList(sessionHistory); }
    public int getYearLevel()                          { return yearLevel; }
    public String getProgram()                         { return program; }
}
