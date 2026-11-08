package aralinks.model;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * One scheduled meeting: private (from an accepted Request) or part of a TutoringClass.
 * UML: TutoringSession * → 1 Tutor (conducted by); * → 1 Course (covers);
 *      TutoringSession ◇— * Learner (attended by); TutoringClass 0..1 — * TutoringSession (generates);
 *      Review * → 1 TutoringSession (for).
 * Owner: Charlyz.
 */
public class TutoringSession {
    private final String sessionId;
    private final Tutor tutor;
    private final Course course;
    private final TutoringClass tutoringClass;   // null for a private session (the 0..1 in the UML)
    private final List<Learner> learners = new ArrayList<>();
    private final List<Learner> attended = new ArrayList<>();   // attendance, recorded on completion
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private double totalCost;
    private SessionStatus status = SessionStatus.SCHEDULED;

    public TutoringSession(String sessionId, Tutor tutor, Course course, LocalDate date,
                           LocalTime startTime, LocalTime endTime, TutoringClass tutoringClass) {
        if (!endTime.isAfter(startTime)) throw new IllegalArgumentException("End time must be after the start time");
        this.sessionId = sessionId;
        this.tutor = tutor;
        this.course = course;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.tutoringClass = tutoringClass;
    }

    /** Adds a learner and records the session in their history. DONE. */
    public void addLearner(Learner learner) {
        if (!learners.contains(learner)) {
            learners.add(learner);
            learner.addToHistory(this);
        }
    }

    /**
     * Moves a SCHEDULED session to a new date/time.
     * TODO (Charlyz): only SCHEDULED sessions; end after start; update the three fields.
     */
    public void schedule(LocalDate date, LocalTime start, LocalTime end) {
        throw new UnsupportedOperationException("TODO: TutoringSession.schedule");
    }

    /**
     * Ends the session and records who attended (flow chart: "Mark session completed").
     * TODO (Charlyz): only SCHEDULED sessions; store attendees (each must be in learners);
     *   status → COMPLETED; totalCost = computeCost().
     */
    public void markCompleted(List<Learner> attendees) {
        throw new UnsupportedOperationException("TODO: TutoringSession.markCompleted");
    }

    /** TODO (Charlyz): a COMPLETED session can't be cancelled; otherwise status → CANCELLED. */
    public void cancel() {
        throw new UnsupportedOperationException("TODO: TutoringSession.cancel");
    }

    /**
     * Cost of the session.
     *  - group (tutoringClass != null): class rate × number of learners
     *  - private: tutor's hourly rate × hours
     * DONE: worked example of a polymorphism-free but branchy calculation.
     */
    public double computeCost() {
        if (tutoringClass != null) {
            return tutoringClass.getRate() * learners.size();
        }
        double hours = Duration.between(startTime, endTime).toMinutes() / 60.0;
        return tutor.getHourlyRate() * hours;
    }

    /** E.g. "Java Programming with Ben Reyes · 2026-11-12 15:00–17:00 · 3 learners · SCHEDULED". TODO (Charlyz). */
    public String getDetails() {
        throw new UnsupportedOperationException("TODO: TutoringSession.getDetails");
    }

    public boolean includes(Learner learner)  { return learners.contains(learner); }
    public boolean attended(Learner learner)  { return attended.contains(learner); }

    // ---------- getters ----------
    public String getSessionId()               { return sessionId; }
    public Tutor getTutor()                    { return tutor; }
    public Course getCourse()                  { return course; }
    public TutoringClass getTutoringClass()    { return tutoringClass; }
    public List<Learner> getLearners()         { return Collections.unmodifiableList(learners); }
    public LocalDate getDate()                 { return date; }
    public LocalTime getStartTime()            { return startTime; }
    public LocalTime getEndTime()              { return endTime; }
    public double getTotalCost()               { return totalCost; }
    public SessionStatus getStatus()           { return status; }
}
