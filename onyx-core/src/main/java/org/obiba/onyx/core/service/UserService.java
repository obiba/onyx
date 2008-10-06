package org.obiba.onyx.core.service;

import java.io.Serializable;
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
   * @param isDeleted
   * @param paging
   * @param clauses
   * @return
   */
  public List<User> getUsers(User template, PagingClause paging, SortingClause... clauses);

  /**
   * Returns the count of users
   * @return
   */
  public int getUserCount(User template);

  /**
   * Gives the user with the specified login
   * @param login
   * @return
   */
  public User getUserWithLogin(String login);

  /**
   * Change the status of the specified user
   * @param user
   * @param status
   */
  public void updateStatus(Serializable userId, Status status);

  /**
   * User creation during the application configuration
   */
  public void createUser(User user);

  /**
   * Delete specified user by setting the "deleted" field to "true"
   * @param user
   */
  public void deleteUser(Serializable userId);

  /**
   * Update the language of the specified user
   * @param template
   * @param language
   */
  public void updateUserLanguage(Serializable userId, Locale language);

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
  public void updatePassword(Serializable userId, String password);

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
