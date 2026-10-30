package aralinks.service;

import aralinks.api.Dto;
import aralinks.model.Availability;
import aralinks.model.Course;
import aralinks.repository.CourseRepository;
import aralinks.repository.ReviewRepository;
import aralinks.repository.UserRepository;
import aralinks.users.Role;
import aralinks.users.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class TutorService {
    private final UserRepository users;
    private final CourseRepository courses;
    private final ReviewRepository reviews;

    public TutorService(UserRepository users, CourseRepository courses, ReviewRepository reviews) {
        this.users = users;
        this.courses = courses;
        this.reviews = reviews;
    }

    @Transactional(readOnly = true)
    public List<Dto.CourseView> listCourses() {
        return courses.findAll().stream()
                .sorted(Comparator.comparing(Course::getCode))
                .map(Dto.CourseView::of).toList();
    }

    public Dto.CourseView addCourse(Dto.NewCourse c) {
        if (courses.existsByCode(c.code())) {
            throw new IllegalStateException("That course code already exists.");
        }
        return Dto.CourseView.of(courses.save(new Course(c.code(), c.name(), c.category(), c.type())));
    }

    @Transactional(readOnly = true)
    public List<Dto.TutorView> search(String keyword) {
        String pattern = "%" + (keyword == null ? "" : keyword.trim().toLowerCase()) + "%";
        return users.searchTutors(pattern).stream().map(this::toView).toList();
    }

    @Transactional(readOnly = true)
    public Dto.TutorView get(UUID tutorId) {
        return toView(requireTutor(tutorId));
    }

    public Dto.TutorView update(UUID tutorId, Dto.TutorUpdate update) {
        User tutor = requireTutor(tutorId);
        tutor.setTutorDetails(update.hourlyRate(), update.bio());
        return toView(tutor);
    }

    public Dto.TutorView addCourse(UUID tutorId, UUID courseId) {
        User tutor = requireTutor(tutorId);
        tutor.addCourse(requireCourse(courseId));
        return toView(tutor);
    }

    public Dto.TutorView removeCourse(UUID tutorId, UUID courseId) {
        User tutor = requireTutor(tutorId);
        tutor.removeCourse(requireCourse(courseId));
        return toView(tutor);
    }

    public Dto.TutorView addAvailability(UUID tutorId, Dto.NewAvailability a) {
        User tutor = requireTutor(tutorId);
        tutor.addAvailability(new Availability(tutor, a.day(), a.startTime(), a.endTime()));
        users.flush();
        return toView(tutor);
    }

    public Dto.TutorView removeAvailability(UUID tutorId, UUID availabilityId) {
        User tutor = requireTutor(tutorId);
        Availability availability = tutor.getAvailableSchedules().stream()
                .filter(a -> a.getId().equals(availabilityId)).findFirst()
                .orElseThrow(() -> new NotFoundException("Availability not found."));
        tutor.removeAvailability(availability);
        return toView(tutor);
    }

    private Dto.TutorView toView(User tutor) {
        var tutorReviews = reviews.findForTutor(tutor.getId());
        Double average = reviews.averageRatingForTutor(tutor.getId());
        return new Dto.TutorView(tutor.getId(), tutor.getName(), tutor.getUniversity(), tutor.getHourlyRate(),
                tutor.getBio(), average == null ? 0.0 : average, tutorReviews.size(),
                tutor.getCourses().stream().sorted(Comparator.comparing(Course::getCode))
                        .map(Dto.CourseView::of).toList(),
                tutor.getAvailableSchedules().stream().map(Dto.AvailabilityView::of).toList(),
                tutorReviews.stream().map(Dto.ReviewView::of).toList());
    }

    private User requireTutor(UUID id) {
        return users.findById(id).filter(u -> u.hasRole(Role.TUTOR))
                .orElseThrow(() -> new NotFoundException("Tutor not found."));
    }

    private Course requireCourse(UUID id) {
        return courses.findById(id).orElseThrow(() -> new NotFoundException("Course not found."));
    }
}
