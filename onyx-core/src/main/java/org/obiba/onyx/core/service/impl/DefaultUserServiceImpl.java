package org.obiba.onyx.core.service.impl;

import java.util.Locale;

import org.obiba.core.service.impl.PersistenceManagerAwareService;
import org.obiba.onyx.core.domain.user.Status;
import org.obiba.onyx.core.domain.user.User;
import org.obiba.onyx.core.service.UserService;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default implementation (non hibernate specific) of User Service
 * @see#UserServiceHibernateImpl.
 * @author acarey
 * 
 */
@Transactional
public abstract class DefaultUserServiceImpl extends PersistenceManagerAwareService implements UserService {

  public int getUserCount() {
    return getPersistenceManager().count(User.class);
  }

  public User getUserWithLogin(String login) {
    User template = new User();
    template.setLogin(login);
    return getPersistenceManager().matchOne(template);
  }

  public void changeStatus(User user, Status status) {
    User userToChange = getPersistenceManager().matchOne(user);
    userToChange.setStatus(status);
    getPersistenceManager().save(userToChange);
  }

  public void createUser(User user) {
    getPersistenceManager().save(user);
  }

  public void deleteUser(User user) {
    User userToDelete = getPersistenceManager().matchOne(user);
    userToDelete.setDeleted(true);
    getPersistenceManager().save(userToDelete);
  }

  public void setUserLanguage(User template, Locale language) {
    User user = getPersistenceManager().matchOne(template);
    user.setLanguage(language);
    getPersistenceManager().save(user);
  }

  public boolean isNewPassword(User user, String password) {
    User template = new User();
    template.setLogin(user.getLogin());
    template.setPassword(password);

    return (getPersistenceManager().matchOne(template) == null);
  }

  public void setPassword(User template, String password) {
    User user = getPersistenceManager().matchOne(template);
    user.setPassword(password);
    getPersistenceManager().save(user);
  }

  public void setUser(User user) {
    User template = new User();
    if(user.getId() != null) {
      template.setLogin(user.getLogin());
      template = getPersistenceManager().matchOne(template);

      if(!user.getLastName().equals(template.getLastName())) template.setLastName(user.getLastName());
      if(!user.getFirstName().equals(template.getFirstName())) template.setFirstName(user.getFirstName());
      if(!user.getPassword().equals(template.getPassword())) template.setPassword(user.getPassword());
      if(!user.getEmail().equals(template.getEmail())) template.setEmail(user.getEmail());
      if(template.getLanguage() == null || !user.getLanguage().equals(template.getLanguage())) template.setLanguage(user.getLanguage());

      if(!user.getRoles().equals(template.getRoles())) {
        template.setRoles(user.getRoles());
      }

    } else {
      template = user;
    }
    getPersistenceManager().save(template);
  }

}
