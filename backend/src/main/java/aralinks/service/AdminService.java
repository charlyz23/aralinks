package aralinks.service;

import aralinks.api.Dto;
import aralinks.repository.UserRepository;
import aralinks.users.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class AdminService {
    private final UserRepository users;

    public AdminService(UserRepository users) {
        this.users = users;
    }

    @Transactional(readOnly = true)
    public List<Dto.UserView> listUsers() {
        return users.findAll().stream().map(Dto.UserView::of).toList();
    }

    public Dto.UserView setActive(UUID adminId, UUID userId, boolean active) {
        if (adminId.equals(userId)) {
            throw new IllegalArgumentException("You cannot change your own account status.");
        }
        User user = users.findById(userId).orElseThrow(() -> new NotFoundException("User not found."));
        user.setActive(active);
        return Dto.UserView.of(user);
    }
}
