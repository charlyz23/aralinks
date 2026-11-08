package aralinks.model;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void eachRoleShowsItsOwnMenu() throws Exception {
        // Polymorphism: same method call, different behaviour per subclass.
        List<User> users = List.of(
                Fixtures.ben(),
                Fixtures.learner("U-2", "Cara"),
                new Administrator("U-3", "A-1", "00-00001", "Admin", "a@x.edu", "pw", Fixtures.UNI));
        assertEquals("Tutor", users.get(0).getRole());
        assertTrue(users.get(0).displayMenu().contains("Requests"));
        assertTrue(users.get(1).displayMenu().contains("Browse Classes"));
        assertTrue(users.get(2).displayMenu().contains("Listings"));
    }

    @Test
    void loginChecksPassword() throws Exception {
        Tutor ben = Fixtures.ben();
        assertTrue(ben.login("demo123"));
        assertFalse(ben.login("wrong"));
    }

    @Test
    void rejectsBadSrCode() {
        assertThrows(IllegalArgumentException.class,
                () -> new Learner("U-9", "12345", "X", "x@x.edu", "pw", Fixtures.UNI, 1, "BSCS"));
    }

    @Disabled("TODO (Kennard): enable after implementing User.updateProfile")
    @Test
    void updateProfileRejectsBlankName() {
        Learner cara = Fixtures.learner("U-2", "Cara");
        assertThrows(IllegalArgumentException.class, () -> cara.updateProfile(" ", "c@x.edu", "BatStateU"));
    }
}
