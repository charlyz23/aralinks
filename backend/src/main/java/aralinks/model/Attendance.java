package aralinks.model;

import aralinks.users.User;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "session_attendees")
public class Attendance extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    private TutoringSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "learner_id")
    private User learner;

    private boolean attended;

    protected Attendance() {
    }

    Attendance(TutoringSession session, User learner) {
        this.session = session;
        this.learner = learner;
    }

    void setAttended(boolean attended) { this.attended = attended; }

    public TutoringSession getSession() { return session; }
    public User getLearner() { return learner; }
    public boolean isAttended() { return attended; }
}
