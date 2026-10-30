package aralinks.service;

import aralinks.api.Dto;
import aralinks.model.Course;
import aralinks.model.TutoringClass;
import aralinks.model.TutoringSession;
import aralinks.repository.CourseRepository;
import aralinks.repository.TutoringClassRepository;
import aralinks.repository.TutoringSessionRepository;
import aralinks.repository.UserRepository;
import aralinks.users.User;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ClassService {
    private final TutoringClassRepository classes;
    private final TutoringSessionRepository sessions;
    private final UserRepository users;
    private final CourseRepository courses;
    private final BookingRules rules;
    private final SessionService sessionService;

    public ClassService(TutoringClassRepository classes, TutoringSessionRepository sessions,
                        UserRepository users, CourseRepository courses, BookingRules rules,
                        SessionService sessionService) {
        this.classes = classes;
        this.sessions = sessions;
        this.users = users;
        this.courses = courses;
        this.rules = rules;
        this.sessionService = sessionService;
    }

    public Dto.ClassView create(UUID tutorId, Dto.NewClass c) {
        User tutor = users.getReferenceById(tutorId);
        Course course = courses.findById(c.courseId())
                .orElseThrow(() -> new NotFoundException("Course not found."));
        TutoringClass created = new TutoringClass(tutor, course, c.maxCapacity(), c.platform(), c.classroomLink());
        return Dto.ClassView.of(classes.save(created));
    }

    @Transactional(readOnly = true)
    public List<Dto.ClassView> listOpen() {
        return classes.findByStatus(TutoringClass.Status.OPEN).stream().map(Dto.ClassView::of).toList();
    }

    @Transactional(readOnly = true)
    public Dto.ClassView get(UUID classId) {
        return Dto.ClassView.of(find(classId));
    }

    public Dto.ClassView enroll(UUID learnerId, UUID classId) {
        TutoringClass tutoringClass = classes.findByIdForUpdate(classId)
                .orElseThrow(() -> new NotFoundException("Class not found."));
        User learner = users.getReferenceById(learnerId);
        if (tutoringClass.getTutor().equals(learner)) {
            throw new IllegalArgumentException("You cannot enroll in your own class.");
        }
        tutoringClass.addLearner(learner);
        tutoringClass.getSessions().stream()
                .filter(s -> s.getStatus() == TutoringSession.Status.SCHEDULED)
                .forEach(s -> s.addAttendee(learner));
        return Dto.ClassView.of(tutoringClass);
    }

    /** A class can hold as many sessions as it needs, which is what makes weekly classes work. */
    public Dto.SessionView addSession(UUID tutorId, UUID classId, Dto.NewClassSession s) {
        TutoringClass tutoringClass = owned(tutorId, classId);
        if (tutoringClass.getStatus() == TutoringClass.Status.COMPLETED
                || tutoringClass.getStatus() == TutoringClass.Status.REMOVED) {
            throw new IllegalStateException("This class has ended.");
        }
        rules.check(tutoringClass.getTutor(), tutoringClass.getCourse(), s.date(), s.startTime(), s.endTime());
        TutoringSession session = TutoringSession.fromClass(tutoringClass, s.date(), s.startTime(), s.endTime());
        return sessionService.toView(sessions.save(session));
    }

    public Dto.ClassView start(UUID tutorId, UUID classId) {
        TutoringClass tutoringClass = owned(tutorId, classId);
        tutoringClass.startClass();
        return Dto.ClassView.of(tutoringClass);
    }

    public Dto.ClassView end(UUID tutorId, UUID classId) {
        TutoringClass tutoringClass = owned(tutorId, classId);
        tutoringClass.endClass();
        return Dto.ClassView.of(tutoringClass);
    }

    public Dto.ClassView remove(UUID classId) {
        TutoringClass tutoringClass = find(classId);
        tutoringClass.remove();
        return Dto.ClassView.of(tutoringClass);
    }

    private TutoringClass owned(UUID tutorId, UUID classId) {
        TutoringClass tutoringClass = find(classId);
        if (!tutoringClass.getTutor().getId().equals(tutorId)) {
            throw new AccessDeniedException("This class belongs to another tutor.");
        }
        return tutoringClass;
    }

    private TutoringClass find(UUID id) {
        return classes.findById(id).orElseThrow(() -> new NotFoundException("Class not found."));
    }
}
