package aralinks.service;

import aralinks.exception.InvalidCredentialsException;
import aralinks.model.Learner;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {

    private final DataStore db = DataStore.withDemoData();
    private final AuthService auth = new AuthService(db);

    @Test
    void demoLearnerCanLogIn() throws Exception {
        assertInstanceOf(Learner.class, auth.login("25-11111", "demo123"));
    }

    @Test
    void wrongPasswordThrows() {
        assertThrows(InvalidCredentialsException.class, () -> auth.login("25-11111", "nope"));
        assertThrows(InvalidCredentialsException.class, () -> auth.login("99-99999", "demo123"));
    }

    @Test
    void idsAreSequentialPerPrefix() {
        DataStore fresh = new DataStore();
        assertEquals("X-0001", fresh.nextId("X"));
        assertEquals("X-0002", fresh.nextId("X"));
        assertEquals("Y-0001", fresh.nextId("Y"));
    }
}
