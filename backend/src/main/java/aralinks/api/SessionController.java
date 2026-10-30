package aralinks.api;

import aralinks.service.SessionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {
    private final SessionService sessions;

    public SessionController(SessionService sessions) {
        this.sessions = sessions;
    }

    @GetMapping
    List<Dto.SessionView> mine(@AuthenticationPrincipal UUID me) {
        return sessions.mine(me);
    }

    @GetMapping("/{id}")
    Dto.SessionView get(@AuthenticationPrincipal UUID me, @PathVariable UUID id) {
        return sessions.get(me, id);
    }

    @PutMapping("/{id}/meeting-link")
    @PreAuthorize("hasRole('TUTOR')")
    Dto.SessionView setMeetingLink(@AuthenticationPrincipal UUID me, @PathVariable UUID id,
                                   @Valid @RequestBody Dto.MeetingLink link) {
        return sessions.setMeetingLink(me, id, link.meetingLink());
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasRole('TUTOR')")
    Dto.SessionView complete(@AuthenticationPrincipal UUID me, @PathVariable UUID id,
                             @Valid @RequestBody Dto.CompleteSession body) {
        return sessions.complete(me, id, body);
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('TUTOR')")
    Dto.SessionView cancel(@AuthenticationPrincipal UUID me, @PathVariable UUID id) {
        return sessions.cancel(me, id);
    }

    @PostMapping("/{id}/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('LEARNER')")
    Dto.ReviewView review(@AuthenticationPrincipal UUID me, @PathVariable UUID id,
                          @Valid @RequestBody Dto.NewReview review) {
        return sessions.review(me, id, review);
    }

    /** Simulated payment: see {@link aralinks.model.Payment}. */
    @PostMapping("/{id}/payments")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('LEARNER')")
    Dto.PaymentView pay(@AuthenticationPrincipal UUID me, @PathVariable UUID id,
                        @Valid @RequestBody Dto.NewPayment payment) {
        return sessions.pay(me, id, payment.method());
    }
}
