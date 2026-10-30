package aralinks.model;

import aralinks.users.User;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

/** Simulated for the demo: this records a payment, but no money moves. */
@Entity
@Table(name = "payments")
public class Payment extends BaseEntity {
    public enum Method { GCASH, CARD, CASH }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    private TutoringSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "learner_id")
    private User learner;

    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private Method method;

    private String reference;
    private Instant paidAt;

    protected Payment() {
    }

    public Payment(TutoringSession session, User learner, Method method, String reference, Instant paidAt) {
        this.session = session;
        this.learner = learner;
        this.amount = session.costPerLearner();
        this.method = method;
        this.reference = reference;
        this.paidAt = paidAt;
    }

    public BigDecimal getAmount() { return amount; }
    public Method getMethod() { return method; }
    public String getReference() { return reference; }
    public Instant getPaidAt() { return paidAt; }
}
