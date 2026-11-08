package aralinks.model;

import aralinks.exception.InvalidRequestStateException;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * A learner's request for a private session, which the tutor accepts or declines.
 * UML: Learner 1 — * Request (submits); Request * → 1 Tutor (sent to); Request * → 1 Course (about).
 * Owner: Charlyz.
 *
 * Changes from the UML (also listed in the README):
 *  - preferredSchedule: String became day + startTime + endTime, so it can be checked against
 *    the tutor's availability. getPreferredSchedule() still returns it as text.
 *  - status: String became RequestStatus.
 *  - a learner field was added so the tutor can see who sent it.
 */
public class Request {
    private final String requestId;
    private final Learner learner;
    private final Tutor tutor;
    private final Course course;
    private final DayOfWeek day;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final String message;
    private RequestStatus status = RequestStatus.PENDING;

    public Request(String requestId, Learner learner, Tutor tutor, Course course,
                   DayOfWeek day, LocalTime startTime, LocalTime endTime, String message) {
        this.requestId = requestId;
        this.learner = learner;
        this.tutor = tutor;
        this.course = course;
        this.day = day;
        this.startTime = startTime;
        this.endTime = endTime;
        this.message = message == null ? "" : message;
    }

    /** DONE. */
    public boolean isPending() {
        return status == RequestStatus.PENDING;
    }

    /**
     * TODO (Charlyz): if not pending → throw new InvalidRequestStateException("..."),
     *   otherwise set status to ACCEPTED.
     */
    public void accept() throws InvalidRequestStateException {
        throw new UnsupportedOperationException("TODO: Request.accept");
    }

    /** TODO (Charlyz): same as accept(), but DECLINED. */
    public void decline() throws InvalidRequestStateException {
        throw new UnsupportedOperationException("TODO: Request.decline");
    }

    /** Learner withdraws it. Not in the UML, but Learner.cancelRequest() needs it. TODO (Charlyz): same pattern, CANCELLED. */
    public void cancel() throws InvalidRequestStateException {
        throw new UnsupportedOperationException("TODO: Request.cancel");
    }

    /** Hours requested, e.g. 10:00–11:30 → 1.5. DONE. */
    public double getHours() {
        return java.time.Duration.between(startTime, endTime).toMinutes() / 60.0;
    }

    /** E.g. "MONDAY 10:00–11:00". DONE. */
    public String getPreferredSchedule() {
        return day + " " + startTime + "–" + endTime;
    }

    /**
     * One-line summary for the Requests screen, e.g.
     * "Cara Lim → Ben Reyes · Java Programming · MONDAY 10:00–11:00 · PENDING".
     * TODO (Charlyz): build it from the fields.
     */
    public String getDetails() {
        throw new UnsupportedOperationException("TODO: Request.getDetails");
    }

    // ---------- getters ----------
    public String getRequestId()     { return requestId; }
    public Learner getLearner()      { return learner; }
    public Tutor getTutor()          { return tutor; }
    public Course getCourse()        { return course; }
    public DayOfWeek getDay()        { return day; }
    public LocalTime getStartTime()  { return startTime; }
    public LocalTime getEndTime()    { return endTime; }
    public String getMessage()       { return message; }
    public RequestStatus getStatus() { return status; }
}
