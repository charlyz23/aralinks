package aralinks.model;

import aralinks.exception.InvalidRateException;
import aralinks.exception.InvalidRequestStateException;
import aralinks.exception.InvalidScheduleException;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A student who offers tutoring.
 * UML: Tutor extends User; Tutor ◇— Course (teaches, aggregation); Tutor ◆— Availability (has, composition);
 *      Tutor 1 — * TutoringClass (creates); Request → Tutor (sent to); TutoringSession → Tutor (conducted by).
 * Owner: Kennard.
 */
public class Tutor extends User {
    private final List<Course> courses = new ArrayList<>();                  // aggregation: Courses live in DataStore
    private final List<Availability> availableSchedules = new ArrayList<>(); // composition: created only here
    private double hourlyRate;
    private String bio;
    private double averageRating;   // cached result of computeAverageRating()

    public Tutor(String userId, String srCode, String name, String email, String password,
                 String university, double hourlyRate, String bio) {
        super(userId, srCode, name, email, password, university);
        this.hourlyRate = hourlyRate;
        this.bio = bio;
    }

    @Override public String getRole() { return "Tutor"; }

    @Override
    public List<String> displayMenu() {
        return List.of("My Classes", "Requests", "History", "Profile", "Logout");
    }

    // ---------- courses (DONE: worked example of aggregation) ----------

    public void addCourse(Course course) {
        if (!courses.contains(course)) courses.add(course);
    }

    public void removeCourse(Course course) {
        courses.remove(course);
    }

    public boolean teaches(Course course) {
        return courses.contains(course);
    }

    // ---------- rate (DONE: worked example of validation + custom exception) ----------

    public void setRate(double hourlyRate) throws InvalidRateException {
        if (hourlyRate < 0 || hourlyRate > 5000) throw new InvalidRateException(hourlyRate);
        this.hourlyRate = hourlyRate;
    }

    // ---------- availability (DONE: worked example of composition) ----------

    /** Creates and stores a new Availability. Nobody else can create one (composition). */
    public void addAvailability(DayOfWeek day, LocalTime start, LocalTime end) throws InvalidScheduleException {
        if (start == null || end == null || !end.isAfter(start)) {
            throw new InvalidScheduleException("End time must be after the start time.");
        }
        Availability block = new Availability(day, start, end);
        for (Availability existing : availableSchedules) {
            if (existing.overlaps(block)) {
                throw new InvalidScheduleException("That overlaps hours you already set on " + day + ".");
            }
        }
        availableSchedules.add(block);
    }

    /** True if any availability block covers the requested time. */
    public boolean isAvailable(DayOfWeek day, LocalTime start, LocalTime end) {
        for (Availability a : availableSchedules) {
            if (a.isAvailable(day, start, end)) return true;
        }
        return false;
    }

    // ---------- requests and sessions ----------

    /**
     * Accepts a learner's private-session request and schedules the session.
     * TODO (Kennard):
     *   1. make sure request.getTutor() == this (else IllegalArgumentException)
     *   2. request.accept()   (it already throws InvalidRequestStateException if not pending)
     *   3. create and return new TutoringSession(sessionId, this, request.getCourse(), date,
     *      request.getStartTime(), request.getEndTime(), null) and add request.getLearner() to it
     */
    public TutoringSession acceptRequest(Request request, String sessionId, java.time.LocalDate date)
            throws InvalidRequestStateException {
        throw new UnsupportedOperationException("TODO: Tutor.acceptRequest");
    }

    /** TODO (Kennard): check the request was sent to this tutor, then call request.decline(). */
    public void declineRequest(Request request) throws InvalidRequestStateException {
        throw new UnsupportedOperationException("TODO: Tutor.declineRequest");
    }

    /** TODO (Kennard): check the session is this tutor's, then call session.markCompleted(). */
    public void completeSession(TutoringSession session) {
        throw new UnsupportedOperationException("TODO: Tutor.completeSession");
    }

    /**
     * Average of the ratings this tutor received, rounded to one decimal; 0 if there are none.
     * Reviews aren't stored on Tutor (no arrow in the UML), so the caller passes them in.
     * TODO (Kennard): keep only reviews whose session.getTutor() == this, average getRating(),
     *   store the result in averageRating and return it.
     */
    public double computeAverageRating(List<Review> allReviews) {
        throw new UnsupportedOperationException("TODO: Tutor.computeAverageRating");
    }

    // ---------- getters ----------
    public List<Course> getCourses()                    { return Collections.unmodifiableList(courses); }
    public List<Availability> getAvailableSchedules()   { return Collections.unmodifiableList(availableSchedules); }
    public double getHourlyRate()                       { return hourlyRate; }
    public String getBio()                              { return bio; }
    public void setBio(String bio)                      { this.bio = bio; }
    public double getAverageRating()                    { return averageRating; }
}
