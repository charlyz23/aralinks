package aralinks.model;

import aralinks.exception.ClassFullException;
import aralinks.exception.InvalidLinkException;
import aralinks.exception.InvalidRateException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TutoringClassTest {

    private TutoringClass smallClass() throws Exception {
        return new TutoringClass("K-1", Fixtures.ben(), Fixtures.java(), "Mon 15:00–17:00", 2, 150, "Google Meet");
    }

    @Test
    void closesEnrollmentWhenFull() throws Exception {
        TutoringClass c = smallClass();
        c.addLearner(Fixtures.learner("U-2", "Cara"));
        c.addLearner(Fixtures.learner("U-3", "Dan"));
        assertTrue(c.isFull());
        assertEquals(ClassStatus.FULL, c.getStatus());
        assertThrows(ClassFullException.class, () -> c.addLearner(Fixtures.learner("U-4", "Ella")));
    }

    @Test
    void leavingReopensEnrollment() throws Exception {
        TutoringClass c = smallClass();
        Learner cara = Fixtures.learner("U-2", "Cara");
        c.addLearner(cara);
        c.addLearner(Fixtures.learner("U-3", "Dan"));
        c.removeLearner(cara);
        assertEquals(1, c.getAvailableSlots());
        assertEquals(ClassStatus.OPEN, c.getStatus());
    }

    @Test
    void rejectsNegativeRate() {
        assertThrows(InvalidRateException.class,
                () -> new TutoringClass("K-2", Fixtures.ben(), Fixtures.java(), "Mon", 5, -1, "Zoom"));
    }

    @Disabled("TODO (Charlyz): enable after implementing setClassroomLink")
    @Test
    void classroomLinkMustBeAUrl() throws Exception {
        TutoringClass c = smallClass();
        assertThrows(InvalidLinkException.class, () -> c.setClassroomLink("meet.google.com/abc"));
        c.setClassroomLink("https://meet.google.com/abc-defg-hij");
        assertEquals("https://meet.google.com/abc-defg-hij", c.getClassroomLink());
    }
}
