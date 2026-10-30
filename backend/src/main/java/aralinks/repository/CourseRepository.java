package aralinks.repository;

import aralinks.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CourseRepository extends JpaRepository<Course, UUID> {
    boolean existsByCode(String code);
}
