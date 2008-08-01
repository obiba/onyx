package org.obiba.onyx.core.service;

import org.obiba.onyx.core.domain.user.User;

public interface UserService {

  public int getUserCount();

  public void createUser(User user);

}
