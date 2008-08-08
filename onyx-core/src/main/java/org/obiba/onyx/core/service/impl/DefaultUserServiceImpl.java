package org.obiba.onyx.core.service.impl;

import org.obiba.core.service.impl.PersistenceManagerAwareService;
import org.obiba.onyx.core.domain.user.User;
import org.obiba.onyx.core.service.UserService;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class DefaultUserServiceImpl extends PersistenceManagerAwareService implements UserService {

  public int getUserCount() {
    return getPersistenceManager().count(User.class);
  }

  public void createUser(User user) {
    getPersistenceManager().save(user);
  }

}
