package aralinks.model;

import aralinks.exception.InvalidRatingException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class ReviewTest {

    @Test
    void ratingMustBeOneToFive() throws Exception {
        assertTrue(Review.validateRating(1));
        assertTrue(Review.validateRating(5));
        assertFalse(Review.validateRating(0));
        assertFalse(Review.validateRating(6));

        TutoringSession s = new TutoringSession("S-1", Fixtures.ben(), Fixtures.java(), LocalDate.now(),
                LocalTime.of(10, 0), LocalTime.of(11, 0), null);
        Learner cara = Fixtures.learner("U-2", "Cara");
        assertThrows(InvalidRatingException.class, () -> new Review("R-1", s, cara, 6, "wow"));
        assertEquals(5, new Review("R-2", s, cara, 5, " great ").getRating());
    }
}
