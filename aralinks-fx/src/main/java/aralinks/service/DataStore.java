package aralinks.service;

import aralinks.exception.ClassFullException;
import aralinks.exception.InvalidRateException;
import aralinks.exception.InvalidScheduleException;
import aralinks.model.*;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Holds every object in memory with ArrayLists (proposal: "Arrays/Data Handling"),
 * hands out IDs, and answers "reverse" questions the UML arrows don't store,
 * e.g. "which requests were sent to this tutor?".
 * Owner: Kennard.
 */
public class DataStore {
    private final List<User> users = new ArrayList<>();
    private final List<Course> courses = new ArrayList<>();
    private final List<TutoringClass> classes = new ArrayList<>();
    private final List<TutoringSession> sessions = new ArrayList<>();
    private final List<Request> requests = new ArrayList<>();
    private final List<Review> reviews = new ArrayList<>();
    private final Map<String, Integer> counters = new HashMap<>();

    /** Next ID for a prefix: nextId("U") → "U-0001", "U-0002", … */
    public String nextId(String prefix) {
        int n = counters.merge(prefix, 1, Integer::sum);
        return String.format("%s-%04d", prefix, n);
    }

    // ---------- add ----------
    public void add(User u)            { users.add(u); }
    public void add(Course c)          { courses.add(c); }
    public void add(TutoringClass c)   { classes.add(c); }
    public void add(TutoringSession s) { sessions.add(s); }
    public void add(Request r)         { requests.add(r); }
    public void add(Review r)          { reviews.add(r); }

    // ---------- read (read-only views) ----------
    public List<User> getUsers()                 { return Collections.unmodifiableList(users); }
    public List<Course> getCourses()             { return Collections.unmodifiableList(courses); }
    public List<TutoringClass> getClasses()      { return Collections.unmodifiableList(classes); }
    public List<TutoringSession> getSessions()   { return Collections.unmodifiableList(sessions); }
    public List<Request> getRequests()           { return Collections.unmodifiableList(requests); }
    public List<Review> getReviews()             { return Collections.unmodifiableList(reviews); }

    public List<Tutor> getTutors() {
        List<Tutor> tutors = new ArrayList<>();
        for (User u : users) if (u instanceof Tutor t) tutors.add(t);
        return tutors;
    }

    /** Reverse lookup for the tutor's Requests screen. */
    public List<Request> requestsFor(Tutor tutor) {
        List<Request> out = new ArrayList<>();
        for (Request r : requests) if (r.getTutor() == tutor) out.add(r);
        return out;
    }

    /** Open classes a learner can still join (Browse screen). */
    public List<TutoringClass> openClasses() {
        List<TutoringClass> out = new ArrayList<>();
        for (TutoringClass c : classes) if (c.getStatus() == ClassStatus.OPEN) out.add(c);
        return out;
    }

    public User findBySrCode(String srCode) {
        for (User u : users) if (u.getSrCode().equals(srCode)) return u;
        return null;
    }

    /**
     * Demo data, matching the web demo's accounts (password "demo123" for all of them).
     * TODO (Kennard): add the rest of assets/demo-kit/data/seed-data.json as you need it.
     */
    public static DataStore withDemoData() {
        DataStore db = new DataStore();
        String uni = "BatStateU – Alangilan";
        try {
            Course java = new Course(db.nextId("C"), "Java Programming", "Computer Science/IT", "Course");
            Course dsa  = new Course(db.nextId("C"), "Data Structures", "Computer Science/IT", "Course");
            Course calc = new Course(db.nextId("C"), "Calculus I", "Engineering", "Course");
            Course draft = new Course(db.nextId("C"), "Architectural Drafting", "Architecture", "Course");
            for (Course c : List.of(java, dsa, calc, draft)) db.add(c);

            db.add(new Administrator(db.nextId("U"), "A-01", "00-00001", "Admin Office", "admin@aralinks.edu", "demo123", uni));

            Tutor ben = new Tutor(db.nextId("U"), "23-01234", "Ben Reyes", "ben@aralinks.edu", "demo123", uni, 200,
                    "3rd-year CS student. I make Java click with live coding.");
            ben.addCourse(java);
            ben.addCourse(dsa);
            for (DayOfWeek d : List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                                       DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY)) {
                ben.addAvailability(d, LocalTime.of(9, 0), LocalTime.of(17, 0));
            }
            db.add(ben);

            Tutor gia = new Tutor(db.nextId("U"), "22-04567", "Gia Santos", "gia@aralinks.edu", "demo123", uni, 180,
                    "Engineering dean's lister. Calculus is just patterns.");
            gia.addCourse(calc);
            gia.addAvailability(DayOfWeek.TUESDAY, LocalTime.of(10, 0), LocalTime.of(18, 0));
            gia.addAvailability(DayOfWeek.THURSDAY, LocalTime.of(10, 0), LocalTime.of(18, 0));
            db.add(gia);

            Learner cara = new Learner(db.nextId("U"), "25-11111", "Cara Lim", "cara@aralinks.edu", "demo123", uni, 1, "BS Computer Science");
            Learner dan  = new Learner(db.nextId("U"), "24-22222", "Dan Cruz", "dan@aralinks.edu", "demo123", uni, 2, "BS Electronics Engineering");
            db.add(cara);
            db.add(dan);

            TutoringClass bootcamp = new TutoringClass(db.nextId("K"), ben, java, "Mon, Wed 15:00–17:00", 5, 150, "Google Meet");
            bootcamp.addLearner(cara);
            db.add(bootcamp);
            db.add(new TutoringClass(db.nextId("K"), gia, calc, "Tue, Thu 13:00–15:00", 5, 150, "Zoom"));
        } catch (InvalidRateException | InvalidScheduleException | ClassFullException e) {
            // Seed data is fixed, so this only happens if someone breaks it while editing.
            throw new IllegalStateException("Demo data is invalid: " + e.getMessage(), e);
        }
        return db;
    }
}
