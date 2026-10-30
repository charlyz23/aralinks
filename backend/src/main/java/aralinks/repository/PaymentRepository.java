package aralinks.repository;

import aralinks.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    boolean existsBySessionIdAndLearnerId(UUID sessionId, UUID learnerId);
}
