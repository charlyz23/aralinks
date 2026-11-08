package aralinks.model;

import aralinks.exception.InvalidRequestStateException;
import aralinks.exception.UnavailableScheduleException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class RequestTest {

    private Request pending() throws Exception {
        return new Request("Q-1", Fixtures.learner("U-2", "Cara"), Fixtures.ben(), Fixtures.java(),
                DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(11, 0), "Help with loops");
    }

    @Test
    void startsPending() throws Exception {
        Request r = pending();
        assertTrue(r.isPending());
        assertEquals(1.0, r.getHours(), 0.001);
    }

    @Disabled("TODO (Charlyz): enable after implementing Request.accept/decline")
    @Test
    void canOnlyBeHandledOnce() throws Exception {
        Request r = pending();
        r.accept();
        assertEquals(RequestStatus.ACCEPTED, r.getStatus());
        assertThrows(InvalidRequestStateException.class, r::decline);
    }

    @Disabled("TODO (Kennard): enable after implementing Learner.submitRequest")
    @Test
    void cannotRequestOutsideTutorHours() throws Exception {
        Learner cara = Fixtures.learner("U-2", "Cara");
        Tutor ben = Fixtures.ben();   // Monday 09:00–17:00 only
        assertThrows(UnavailableScheduleException.class, () -> cara.submitRequest("Q-2", ben, Fixtures.java(),
                DayOfWeek.SUNDAY, LocalTime.of(10, 0), LocalTime.of(11, 0), ""));
    }
}
