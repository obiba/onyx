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

import java.util.List;
import java.util.Locale;

import org.obiba.core.service.SortingClause;
import org.obiba.core.service.impl.PersistenceManagerAwareService;
import org.obiba.onyx.core.domain.user.Role;
import org.obiba.onyx.core.domain.user.Status;
import org.obiba.onyx.core.domain.user.User;
import org.obiba.onyx.core.service.UserService;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default implementation of User Service
 */
@Transactional
public abstract class DefaultUserServiceImpl extends PersistenceManagerAwareService implements UserService {

  public User getUserWithLogin(String login) {
    User template = new User();
    template.setLogin(login);
    return getPersistenceManager().matchOne(template);
  }

  public void updateStatus(User user, Status status) {
    user.setStatus(status);
    getPersistenceManager().save(user);
  }

  public void deleteUser(User user) {
    user.setDeleted(true);
    getPersistenceManager().save(user);
  }

  public void updateUserLanguage(User user, Locale language) {
    user.setLanguage(language);
    getPersistenceManager().save(user);
  }

  public boolean isNewPassword(User user, String password) {
    User template = new User();
    template.setLogin(user.getLogin());
    template.setPassword(password);

    return (getPersistenceManager().matchOne(template) == null);
  }

  public void updatePassword(User user, String password) {
    user.setPassword(password);
    getPersistenceManager().save(user);
  }

  public void createOrUpdateUser(User user) {
    getPersistenceManager().save(user);
  }

  public Role createRole(Role role) {
    return getPersistenceManager().save(role);
  }

  public List<Role> getRoles(SortingClause... clauses) {
    return getPersistenceManager().list(Role.class, clauses);
  }

}
