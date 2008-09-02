package org.obiba.onyx.core.service.impl;

import java.util.Locale;

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
  
  public void setUserLanguage(User template, Locale language) {
    User user = getPersistenceManager().matchOne(template);
    user.setLanguage(language);
    getPersistenceManager().save(user);
  }
  
  public boolean isNewPassword(User user, String password) {
    User template = user;
    template.setPassword(password);
    return (getPersistenceManager().matchOne(template) == null);
  }
  
  public void setPassword(User template, String password) {
    User user = getPersistenceManager().matchOne(template);
    user.setPassword(password);
    getPersistenceManager().save(user);
  }

}
