package aralinks.model;

import java.util.List;

/**
 * Manages accounts and monitors listings.
 * UML: Administrator extends User.
 * Owner: Aifa.
 */
public class Administrator extends User {
    private final String adminId;

    public Administrator(String userId, String adminId, String srCode, String name, String email,
                         String password, String university) {
        super(userId, srCode, name, email, password, university);
        this.adminId = adminId;
    }

    @Override public String getRole() { return "Administrator"; }

    @Override
    public List<String> displayMenu() {
        return List.of("Users", "Listings", "Records", "Logout");
    }

    /**
     * Activates or deactivates an account. A deactivated user can't log in.
     * TODO (Aifa): refuse to deactivate yourself (throw IllegalArgumentException),
     *   then call user.setActive(active).
     */
    public void manageUser(User user, boolean active) {
        throw new UnsupportedOperationException("TODO: Administrator.manageUser");
    }

    /**
     * Takes a class listing down so learners no longer see it.
     * TODO (Aifa): set the class status to CANCELLED via a TutoringClass method.
     */
    public void removeListing(TutoringClass listing) {
        throw new UnsupportedOperationException("TODO: Administrator.removeListing");
    }

    /**
     * A text summary for the Records screen, e.g. "10 users · 3 tutors · 6 learners · 8 open classes".
     * UML says void; returning the text lets the UI display it.
     * TODO (Aifa): count users by getRole() and classes by status.
     */
    public String viewRecords(List<User> users, List<TutoringClass> classes) {
        throw new UnsupportedOperationException("TODO: Administrator.viewRecords");
    }

    public String getAdminId() { return adminId; }
}
