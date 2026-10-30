package aralinks.service;

import aralinks.api.Dto;
import aralinks.model.Course;
import aralinks.model.Request;
import aralinks.model.TutoringSession;
import aralinks.repository.CourseRepository;
import aralinks.repository.RequestRepository;
import aralinks.repository.TutoringSessionRepository;
import aralinks.repository.UserRepository;
import aralinks.users.Role;
import aralinks.users.User;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class RequestService {
    private final RequestRepository requests;
    private final UserRepository users;
    private final CourseRepository courses;
    private final TutoringSessionRepository sessions;
    private final BookingRules rules;

    public RequestService(RequestRepository requests, UserRepository users, CourseRepository courses,
                          TutoringSessionRepository sessions, BookingRules rules) {
        this.requests = requests;
        this.users = users;
        this.courses = courses;
        this.sessions = sessions;
        this.rules = rules;
    }

    public Dto.RequestView submit(UUID learnerId, Dto.NewRequest r) {
        User learner = users.getReferenceById(learnerId);
        User tutor = users.findById(r.tutorId()).filter(u -> u.hasRole(Role.TUTOR))
                .orElseThrow(() -> new NotFoundException("Tutor not found."));
        Course course = courses.findById(r.courseId())
                .orElseThrow(() -> new NotFoundException("Course not found."));
        if (tutor.equals(learner)) {
            throw new IllegalArgumentException("You cannot book yourself.");
        }
        rules.check(tutor, course, r.date(), r.startTime(), r.endTime());
        Request request = new Request(learner, tutor, course, r.date(), r.startTime(), r.endTime(), r.message());
        return Dto.RequestView.of(requests.save(request));
    }

    @Transactional(readOnly = true)
    public List<Dto.RequestView> mine(UUID userId) {
        return requests.findForUser(userId).stream().map(Dto.RequestView::of).toList();
    }

    public Dto.RequestView accept(UUID tutorId, UUID requestId) {
        Request request = forTutor(tutorId, requestId);
        rules.check(request.getTutor(), request.getCourse(), request.getDate(),
                request.getStartTime(), request.getEndTime());
        TutoringSession session = sessions.save(TutoringSession.fromRequest(request));
        request.accept(session);
        return Dto.RequestView.of(request);
    }

    public Dto.RequestView decline(UUID tutorId, UUID requestId) {
        Request request = forTutor(tutorId, requestId);
        request.decline();
        return Dto.RequestView.of(request);
    }

    public Dto.RequestView cancel(UUID learnerId, UUID requestId) {
        Request request = find(requestId);
        if (!request.getLearner().getId().equals(learnerId)) {
            throw new AccessDeniedException("This is not your request.");
        }
        request.cancel();
        return Dto.RequestView.of(request);
    }

    private Request forTutor(UUID tutorId, UUID requestId) {
        Request request = find(requestId);
        if (!request.getTutor().getId().equals(tutorId)) {
            throw new AccessDeniedException("This request was sent to another tutor.");
        }
        return request;
    }

    private Request find(UUID id) {
        return requests.findById(id).orElseThrow(() -> new NotFoundException("Request not found."));
    }
}
