package aralinks.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class TutoringSessionTest {

    @Test
    void privateSessionCostsHourlyRateTimesHours() throws Exception {
        Tutor ben = Fixtures.ben();   // PHP 200/hr
        TutoringSession s = new TutoringSession("S-1", ben, Fixtures.java(), LocalDate.now(),
                LocalTime.of(10, 0), LocalTime.of(11, 30), null);
        assertEquals(300.0, s.computeCost(), 0.001);
    }

    @Test
    void groupSessionCostsRateTimesLearners() throws Exception {
        Tutor ben = Fixtures.ben();
        TutoringClass c = new TutoringClass("K-1", ben, Fixtures.java(), "Mon", 5, 150, "Zoom");
        TutoringSession s = new TutoringSession("S-2", ben, Fixtures.java(), LocalDate.now(),
                LocalTime.of(15, 0), LocalTime.of(17, 0), c);
        Learner cara = Fixtures.learner("U-2", "Cara");
        s.addLearner(cara);
        s.addLearner(Fixtures.learner("U-3", "Dan"));
        assertEquals(300.0, s.computeCost(), 0.001);
        assertTrue(cara.getSessionHistory().contains(s));   // aggregation kept in sync
    }
}
