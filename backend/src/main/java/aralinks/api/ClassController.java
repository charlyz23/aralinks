package aralinks.api;

import aralinks.service.ClassService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/classes")
public class ClassController {
    private final ClassService classes;

    public ClassController(ClassService classes) {
        this.classes = classes;
    }

    @GetMapping
    List<Dto.ClassView> listOpen() {
        return classes.listOpen();
    }

    @GetMapping("/{id}")
    Dto.ClassView get(@PathVariable UUID id) {
        return classes.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('TUTOR')")
    Dto.ClassView create(@AuthenticationPrincipal UUID me, @Valid @RequestBody Dto.NewClass newClass) {
        return classes.create(me, newClass);
    }

    @PostMapping("/{id}/enroll")
    @PreAuthorize("hasRole('LEARNER')")
    Dto.ClassView enroll(@AuthenticationPrincipal UUID me, @PathVariable UUID id) {
        return classes.enroll(me, id);
    }

    @PostMapping("/{id}/sessions")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('TUTOR')")
    Dto.SessionView addSession(@AuthenticationPrincipal UUID me, @PathVariable UUID id,
                               @Valid @RequestBody Dto.NewClassSession session) {
        return classes.addSession(me, id, session);
    }

    @PostMapping("/{id}/start")
    @PreAuthorize("hasRole('TUTOR')")
    Dto.ClassView start(@AuthenticationPrincipal UUID me, @PathVariable UUID id) {
        return classes.start(me, id);
    }

    @PostMapping("/{id}/end")
    @PreAuthorize("hasRole('TUTOR')")
    Dto.ClassView end(@AuthenticationPrincipal UUID me, @PathVariable UUID id) {
        return classes.end(me, id);
    }
}
