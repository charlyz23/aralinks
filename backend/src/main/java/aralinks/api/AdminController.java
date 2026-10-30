package aralinks.api;

import aralinks.service.AdminService;
import aralinks.service.ClassService;
import aralinks.service.SessionService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/** Everything under /api/admin requires the ADMIN role; see SecurityConfig. */
@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final AdminService admin;
    private final ClassService classes;
    private final SessionService sessions;

    public AdminController(AdminService admin, ClassService classes, SessionService sessions) {
        this.admin = admin;
        this.classes = classes;
        this.sessions = sessions;
    }

    @GetMapping("/users")
    List<Dto.UserView> users() {
        return admin.listUsers();
    }

    @PatchMapping("/users/{id}/active")
    Dto.UserView setActive(@AuthenticationPrincipal UUID me, @PathVariable UUID id,
                           @RequestBody Dto.SetActive body) {
        return admin.setActive(me, id, body.active());
    }

    @DeleteMapping("/classes/{id}")
    Dto.ClassView removeListing(@PathVariable UUID id) {
        return classes.remove(id);
    }

    @GetMapping("/sessions")
    List<Dto.SessionView> records() {
        return sessions.all();
    }
}
