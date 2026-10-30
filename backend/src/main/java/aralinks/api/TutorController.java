package aralinks.api;

import aralinks.service.TutorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class TutorController {
    private final TutorService tutors;

    public TutorController(TutorService tutors) {
        this.tutors = tutors;
    }

    @GetMapping("/courses")
    List<Dto.CourseView> courses() {
        return tutors.listCourses();
    }

    @PostMapping("/admin/courses")
    @ResponseStatus(HttpStatus.CREATED)
    Dto.CourseView addCourse(@Valid @RequestBody Dto.NewCourse course) {
        return tutors.addCourse(course);
    }

    @GetMapping("/tutors")
    List<Dto.TutorView> search(@RequestParam(required = false) String q) {
        return tutors.search(q);
    }

    @GetMapping("/tutors/{id}")
    Dto.TutorView get(@PathVariable UUID id) {
        return tutors.get(id);
    }

    @PutMapping("/me/tutor")
    @PreAuthorize("hasRole('TUTOR')")
    Dto.TutorView update(@AuthenticationPrincipal UUID me, @Valid @RequestBody Dto.TutorUpdate update) {
        return tutors.update(me, update);
    }

    @PostMapping("/me/tutor/courses/{courseId}")
    @PreAuthorize("hasRole('TUTOR')")
    Dto.TutorView addCourse(@AuthenticationPrincipal UUID me, @PathVariable UUID courseId) {
        return tutors.addCourse(me, courseId);
    }

    @DeleteMapping("/me/tutor/courses/{courseId}")
    @PreAuthorize("hasRole('TUTOR')")
    Dto.TutorView removeCourse(@AuthenticationPrincipal UUID me, @PathVariable UUID courseId) {
        return tutors.removeCourse(me, courseId);
    }

    @PostMapping("/me/tutor/availability")
    @PreAuthorize("hasRole('TUTOR')")
    Dto.TutorView addAvailability(@AuthenticationPrincipal UUID me, @Valid @RequestBody Dto.NewAvailability a) {
        return tutors.addAvailability(me, a);
    }

    @DeleteMapping("/me/tutor/availability/{availabilityId}")
    @PreAuthorize("hasRole('TUTOR')")
    Dto.TutorView removeAvailability(@AuthenticationPrincipal UUID me, @PathVariable UUID availabilityId) {
        return tutors.removeAvailability(me, availabilityId);
    }
}
