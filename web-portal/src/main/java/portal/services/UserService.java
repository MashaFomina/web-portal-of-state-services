package portal.services;

import portal.model.user.*;

public interface UserService {
    void save(Citizen user);

    User findByUsername(String username);
}
