package org.obiba.onyx.core.service;

import java.util.List;
import java.util.Locale;

import org.obiba.core.service.PagingClause;
import org.obiba.core.service.SortingClause;
import org.obiba.onyx.core.domain.user.Status;
import org.obiba.onyx.core.domain.user.User;

public interface UserService {

  public List<User> getUsers(boolean isDeleted, PagingClause paging, SortingClause... clauses);
  
  public int getUserCount();

  public User getUserWithLogin(String login);
  
  public void changeStatus(User user, Status status);
  
  public void createUser(User user);
  
  public void deleteUser(User user);
  
  public void setUserLanguage(User template, Locale language);
  
  /**
   * Check if password was not previously given to the given user.
   * @param user
   * @param password
   * @return
   */
  public boolean isNewPassword(User user, String password);

  public void setPassword(User template, String password);
  
  public void setUser(User user);
}
