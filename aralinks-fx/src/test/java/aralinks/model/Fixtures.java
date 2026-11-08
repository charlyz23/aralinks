package aralinks.model;

import java.time.DayOfWeek;
import java.time.LocalTime;

/** Small factory for test objects so each test stays short. */
final class Fixtures {
    static final String UNI = "BatStateU";
    private Fixtures() {}

    static Course java() { return new Course("C-1", "Java Programming", "Computer Science/IT", "Course"); }

    static Tutor ben() throws Exception {
        Tutor t = new Tutor("U-1", "23-01234", "Ben Reyes", "ben@x.edu", "demo123", UNI, 200, "Java tutor");
        t.addCourse(java());
        t.addAvailability(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(17, 0));
        return t;
    }

    static Learner learner(String id, String name) {
        return new Learner(id, "25-1111" + id.charAt(id.length() - 1), name, name + "@x.edu", "pw1234", UNI, 1, "BSCS");
    }
}
