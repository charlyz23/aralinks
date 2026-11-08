package aralinks.model;

import aralinks.exception.ClassFullException;
import aralinks.exception.InvalidLinkException;
import aralinks.exception.InvalidRateException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A group class: one tutor, many learners, an external classroom link (Meet, Zoom, Discord…).
 * UML: Tutor 1 — * TutoringClass (creates); TutoringClass * → 1 Course (covers);
 *      TutoringClass ◇— * Learner (enrolls); TutoringClass 0..1 — * TutoringSession (generates).
 * Owner: Charlyz.
 */
public class TutoringClass {
    private final String classId;
    private final Tutor tutor;
    private final Course course;
    private final List<Learner> learners = new ArrayList<>();   // aggregation: learners live in DataStore
    private final String schedule;      // e.g. "Mon, Wed 15:00–17:00"
    private final int maxCapacity;
    private final double rate;          // per tutee
    private String classroomLink = "";
    private String platform;            // "Google Meet", "Zoom", "Discord", …
    private ClassStatus status = ClassStatus.OPEN;

    public TutoringClass(String classId, Tutor tutor, Course course, String schedule,
                         int maxCapacity, double rate, String platform) throws InvalidRateException {
        if (maxCapacity < 2 || maxCapacity > 50) throw new IllegalArgumentException("Max tutees must be 2 to 50");
        if (rate < 0 || rate > 5000) throw new InvalidRateException(rate);
        this.classId = classId;
        this.tutor = tutor;
        this.course = course;
        this.schedule = schedule;
        this.maxCapacity = maxCapacity;
        this.rate = rate;
        this.platform = platform;
    }

    // ---------- enrollment (DONE: the Lesson 2 example) ----------

    /** Flow chart: "Capacity left?" → yes: add learner; no: ClassFullException ("Choose another"). */
    public void addLearner(Learner learner) throws ClassFullException {
        if (status != ClassStatus.OPEN && status != ClassStatus.FULL) {
            throw new IllegalStateException("This class is no longer accepting learners.");
        }
        if (learners.contains(learner)) throw new IllegalStateException(learner.getName() + " is already enrolled.");
        if (isFull()) throw new ClassFullException(learners.size(), maxCapacity);
        learners.add(learner);
        if (isFull()) status = ClassStatus.FULL;   // flow chart: "Class full? → Close enrollment"
    }

    /** DONE. Reopens enrollment if a seat frees up. */
    public void removeLearner(Learner learner) {
        if (learners.remove(learner) && status == ClassStatus.FULL) status = ClassStatus.OPEN;
    }

    public boolean isFull()            { return learners.size() >= maxCapacity; }
    public int getAvailableSlots()     { return maxCapacity - learners.size(); }
    public List<Learner> getParticipants() { return Collections.unmodifiableList(learners); }

    // ---------- classroom link ----------

    /**
     * AraLinks only stores the link; the call happens on the external platform.
     * TODO (Charlyz): an empty link is allowed (tutor adds it later); otherwise it must start with
     *   "http://" or "https://" → else throw new InvalidLinkException(link). Then save it.
     */
    public void setClassroomLink(String link) throws InvalidLinkException {
        throw new UnsupportedOperationException("TODO: TutoringClass.setClassroomLink");
    }

    public String getClassroomLink() { return classroomLink; }

    // ---------- running the class ----------

    /**
     * Starts one meeting of this class and returns the TutoringSession it generates (UML "generates").
     * TODO (Charlyz):
     *   - needs at least one learner (else IllegalStateException)
     *   - status → ONGOING
     *   - create new TutoringSession(sessionId, tutor, course, date, start, end, this),
     *     add every participant to it, and return it
     */
    public TutoringSession startClass(String sessionId, LocalDate date, LocalTime start, LocalTime end) {
        throw new UnsupportedOperationException("TODO: TutoringClass.startClass");
    }

    /** TODO (Charlyz): only an ONGOING class can end; status → COMPLETED. */
    public void endClass() {
        throw new UnsupportedOperationException("TODO: TutoringClass.endClass");
    }

    /** Used by Administrator.removeListing() and the tutor's "Cancel class". DONE. */
    public void cancel() {
        status = ClassStatus.CANCELLED;
    }

    // ---------- getters ----------
    public String getClassId()      { return classId; }
    public Tutor getTutor()         { return tutor; }
    public Course getCourse()       { return course; }
    public String getSchedule()     { return schedule; }
    public int getMaxCapacity()     { return maxCapacity; }
    public double getRate()         { return rate; }
    public String getPlatform()     { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }
    public ClassStatus getStatus()  { return status; }
}
