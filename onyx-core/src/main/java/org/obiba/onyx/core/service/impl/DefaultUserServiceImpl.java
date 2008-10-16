/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.service.impl;

import java.io.Serializable;
import java.util.Locale;

import org.obiba.core.service.impl.PersistenceManagerAwareService;
import org.obiba.onyx.core.domain.user.Role;
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

  public User getUserWithLogin(String login) {
    User template = new User();
    template.setLogin(login);
    return getPersistenceManager().matchOne(template);
  }

  public void updateStatus(Serializable userId, Status status) {
    User user = loadUser(userId);
    user.setStatus(status);
    getPersistenceManager().save(user);
  }

  public void createUser(User user) {
    getPersistenceManager().save(user);
  }

  public void deleteUser(Serializable userId) {
    User user = loadUser(userId);
    user.setDeleted(true);
    getPersistenceManager().save(user);
  }

  public void updateUserLanguage(Serializable userId, Locale language) {
    User user = loadUser(userId);
    user.setLanguage(language);
    getPersistenceManager().save(user);
  }

  public boolean isNewPassword(User user, String password) {
    User template = new User();
    template.setLogin(user.getLogin());
    template.setPassword(password);

    return (getPersistenceManager().matchOne(template) == null);
  }

  public void updatePassword(Serializable userId, String password) {
    User user = loadUser(userId);
    user.setPassword(password);
    getPersistenceManager().save(user);
  }

  public void createOrUpdateUser(User user) {
    User template = new User();
    if(user.getId() != null) {
      template = loadUser(user.getId());

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

  public Role createRole(Role role) {
    return getPersistenceManager().save(role);
  }

  private User loadUser(Serializable userId) {
    User user = getPersistenceManager().get(User.class, userId);
    if(user == null) throw new IllegalArgumentException("Invalid user id");
    return user;
  }
}
