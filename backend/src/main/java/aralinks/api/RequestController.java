package aralinks.api;

import aralinks.service.RequestService;
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
@RequestMapping("/api/requests")
public class RequestController {
    private final RequestService requests;

    public RequestController(RequestService requests) {
        this.requests = requests;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('LEARNER')")
    Dto.RequestView submit(@AuthenticationPrincipal UUID me, @Valid @RequestBody Dto.NewRequest request) {
        return requests.submit(me, request);
    }

    @GetMapping
    List<Dto.RequestView> mine(@AuthenticationPrincipal UUID me) {
        return requests.mine(me);
    }

    @PostMapping("/{id}/accept")
    @PreAuthorize("hasRole('TUTOR')")
    Dto.RequestView accept(@AuthenticationPrincipal UUID me, @PathVariable UUID id) {
        return requests.accept(me, id);
    }

    @PostMapping("/{id}/decline")
    @PreAuthorize("hasRole('TUTOR')")
    Dto.RequestView decline(@AuthenticationPrincipal UUID me, @PathVariable UUID id) {
        return requests.decline(me, id);
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('LEARNER')")
    Dto.RequestView cancel(@AuthenticationPrincipal UUID me, @PathVariable UUID id) {
        return requests.cancel(me, id);
    }
}
