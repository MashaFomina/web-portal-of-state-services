package portal.services;

import portal.model.user.User;

public interface UserService {
    void save(User user);

    User findByUsername(String username);
}
