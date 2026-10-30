package aralinks.model;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

import java.util.UUID;

/** Entities are equal by database ID, so a reloaded row matches the original. */
@MappedSuperclass
public abstract class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    public UUID getId() { return id; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        return o instanceof BaseEntity other && id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return 31;
    }
}
