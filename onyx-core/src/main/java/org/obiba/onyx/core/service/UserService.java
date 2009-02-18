/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.service;

import java.util.List;
import java.util.Locale;

import org.obiba.core.service.PagingClause;
import org.obiba.core.service.SortingClause;
import org.obiba.onyx.core.domain.user.Role;
import org.obiba.onyx.core.domain.user.Status;
import org.obiba.onyx.core.domain.user.User;

public interface UserService {

  /**
   * Returns the list of users
   * @param template a template instance for matching users
   * @param paging paging clause, can be null
   * @param clauses sorting clause(s), can be null
   * @return a list of user instances that match the template
   */
  public List<User> getUsers(User template, PagingClause paging, SortingClause... clauses);

  /**
   * Returns the count of users that match the specified template
   * @return the number of users that match the template
   */
  public int getUserCount(User template);

  /**
   * Returns the user with the specified login
   * @param login the unique login to match
   * @return the user with the specified login or null if none exist
   */
  public User getUserWithLogin(String login);

  /**
   * Change the status of the specified user
   * @param user
   * @param status
   */
  public void updateStatus(User user, Status status);

  /**
   * User creation during the application configuration
   */
  public void createUser(User user);

  /**
   * Delete specified user
   * @param user
   */
  public void deleteUser(User user);

  /**
   * Update the language of the specified user
   * @param template
   * @param language
   */
  public void updateUserLanguage(User user, Locale language);

  /**
   * Check if password was not previously given to the given user.
   * @param user
   * @param password
   * @return
   */
  public boolean isNewPassword(User user, String password);

  /**
   * Update the password of the specified user
   * @param template
   * @param password
   */
  public void updatePassword(User user, String password);

  /**
   * Create a user when id is not provided, otherwise, updates the changed fields
   * @param user
   */
  public void createOrUpdateUser(User user);

  /**
   * Create the given role.
   * @param role
   * @return
   */
  public Role createRole(Role role);

  /**
   * Returns the list of roles
   * @param clauses
   * @return
   */
  public List<Role> getRoles(SortingClause... clauses);
}
