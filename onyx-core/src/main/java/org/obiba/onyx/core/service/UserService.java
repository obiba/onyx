package org.obiba.onyx.core.service;

import java.util.Locale;

import org.obiba.onyx.core.domain.user.User;

public interface UserService {

  public int getUserCount();

  public void createUser(User user);
  
  public void setUserLanguage(User template, Locale language);
  
  /**
   * Check if password was not previously given to the given user.
   * @param user
   * @param password
   * @return
   */
  public boolean isNewPassword(User user, String password);

  public void setPassword(User template, String password);
}
