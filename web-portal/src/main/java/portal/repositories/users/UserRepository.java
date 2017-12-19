package portal.repositories.users;

import portal.model.user.User;

import javax.transaction.Transactional;

@Transactional
public interface UserRepository extends UserBaseRepository<User> { }