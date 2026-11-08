package aralinks.service;

import aralinks.exception.InvalidCredentialsException;
import aralinks.model.User;

/**
 * Flow chart: Enter credentials → Valid login? → User role?
 * Returns the logged-in user; the caller then shows user.displayMenu() (polymorphism).
 * Owner: Kennard. Status: DONE (login); TODO register().
 */
public class AuthService {
    private final DataStore db;

    public AuthService(DataStore db) {
        this.db = db;
    }

    public User login(String srCode, String password) throws InvalidCredentialsException {
        User user = db.findBySrCode(srCode == null ? "" : srCode.trim());
        if (user == null || !user.login(password)) throw new InvalidCredentialsException();
        return user;
    }

    /**
     * TODO (Kennard): validate the inputs, refuse a duplicate SR-Code, create a Learner or Tutor
     *   with db.nextId("U"), db.add(...) it and return it.
     */
    public User register(String role, String srCode, String name, String email, String password) {
        throw new UnsupportedOperationException("TODO: AuthService.register");
    }
}
