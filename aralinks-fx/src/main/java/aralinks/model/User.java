package aralinks.model;

import java.util.List;

/**
 * Parent class for every account. Abstract: you can't create a plain "User", only a
 * Tutor, Learner or Administrator.
 * UML: <<abstract>> User, generalized by Administrator, Tutor, Learner.
 * Owner: Kennard.
 *
 * OOP shown here:
 *  - Encapsulation: every field is private; changes go through methods.
 *  - Abstraction: getRole() and displayMenu() are declared here but each subclass decides what they do.
 *  - Polymorphism: the UI calls user.displayMenu() without knowing which kind of user it has.
 */
public abstract class User {
    private final String userId;
    private final String srCode;      // format 00-00000
    private String name;
    private String email;
    private String password;          // plain text: fine for this prototype (proposal limitation #3)
    private String university;
    private boolean active = true;    // not in UML; Administrator.manageUser() switches it

    protected User(String userId, String srCode, String name, String email, String password, String university) {
        if (srCode == null || !srCode.matches("\\d{2}-\\d{5}")) {
            throw new IllegalArgumentException("SR-Code must look like 00-00000");
        }
        this.userId = userId;
        this.srCode = srCode;
        this.name = name;
        this.email = email;
        this.password = password;
        this.university = university;
    }

    /** True if the password matches and the account is active. DONE. */
    public boolean login(String password) {
        return active && this.password.equals(password);
    }

    /**
     * Changes the editable profile fields.
     * TODO (Kennard): reject a blank name or an email without "@" by throwing
     *   IllegalArgumentException (or a custom exception), then assign the fields.
     */
    public void updateProfile(String name, String email, String university) {
        throw new UnsupportedOperationException("TODO: User.updateProfile");
    }

    /** "Tutor", "Learner" or "Administrator". Each subclass overrides this. */
    public abstract String getRole();

    /**
     * The menu this role sees after login (flow chart: "User role?" → Tutor/Learner/Admin menu).
     * Returns menu labels instead of printing, so the JavaFX sidebar can build buttons from it.
     * UML says void; see the README for why this returns a list.
     */
    public abstract List<String> displayMenu();

    // ---- getters (no setters for userId/srCode: they never change) ----
    public String getUserId()     { return userId; }
    public String getSrCode()     { return srCode; }
    public String getName()       { return name; }
    public String getEmail()      { return email; }
    public String getUniversity() { return university; }
    public boolean isActive()     { return active; }

    /** Package-private: only classes in aralinks.model (i.e. Administrator) can deactivate accounts. */
    void setActive(boolean active) { this.active = active; }

    @Override
    public String toString() {
        return name + " (" + getRole() + ", " + srCode + ")";
    }
}
