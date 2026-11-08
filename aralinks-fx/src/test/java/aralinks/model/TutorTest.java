package aralinks.model;

import aralinks.exception.InvalidRateException;
import aralinks.exception.InvalidScheduleException;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class TutorTest {

    @Test
    void setRateRejectsNegative() throws Exception {
        Tutor ben = Fixtures.ben();
        assertThrows(InvalidRateException.class, () -> ben.setRate(-5));
        ben.setRate(250);
        assertEquals(250, ben.getHourlyRate());
    }

    @Test
    void availabilityIsCheckedAndOverlapsRejected() throws Exception {
        Tutor ben = Fixtures.ben();   // Monday 09:00–17:00
        assertTrue(ben.isAvailable(DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(11, 0)));
        assertFalse(ben.isAvailable(DayOfWeek.MONDAY, LocalTime.of(16, 30), LocalTime.of(18, 0)));
        assertFalse(ben.isAvailable(DayOfWeek.SUNDAY, LocalTime.of(10, 0), LocalTime.of(11, 0)));
        assertThrows(InvalidScheduleException.class,
                () -> ben.addAvailability(DayOfWeek.MONDAY, LocalTime.of(12, 0), LocalTime.of(13, 0)));
        assertThrows(InvalidScheduleException.class,
                () -> ben.addAvailability(DayOfWeek.TUESDAY, LocalTime.of(13, 0), LocalTime.of(12, 0)));
    }

    @Test
    void coursesCanOnlyChangeThroughMethods() throws Exception {
        Tutor ben = Fixtures.ben();
        // Encapsulation: the list returned by the getter is read-only.
        assertThrows(UnsupportedOperationException.class, () -> ben.getCourses().clear());
    }
}
