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

import org.obiba.onyx.core.domain.user.User;
import org.obiba.onyx.core.service.IPasswordHashingStrategy;
import org.obiba.onyx.core.service.IPasswordValidationStrategy;
import org.obiba.onyx.core.service.UserPasswordService;
import org.obiba.onyx.core.service.UserService;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.support.DefaultMessageSourceResolvable;

/**
 *
 */
public class DefaultUserPasswordServiceImpl implements UserPasswordService {

  private boolean preventPasswordReuse = true;

  private int passwordHistorySize = 10;

  /** Number of days a password remains valid. */
  private int passwordValidityPeriod = 90;

  private IPasswordValidationStrategy passwordValidationStrategy;

  private IPasswordHashingStrategy passwordHashingStrategy;

  private UserService userService;

  public void setPreventPasswordReuse(boolean preventPasswordReuse) {
    this.preventPasswordReuse = preventPasswordReuse;
  }

  public void setPasswordHistorySize(int passwordHistorySize) {
    this.passwordHistorySize = passwordHistorySize;
  }

  public void setPasswordValidityPeriod(int passwordValidityPeriod) {
    this.passwordValidityPeriod = passwordValidityPeriod;
  }

  public void setPasswordValidationStrategy(IPasswordValidationStrategy passwordValidationStrategy) {
    this.passwordValidationStrategy = passwordValidationStrategy;
  }

  public void setPasswordHashingStrategy(IPasswordHashingStrategy passwordHashingStrategy) {
    this.passwordHashingStrategy = passwordHashingStrategy;
  }

  public void setUserService(UserService userService) {
    this.userService = userService;
  }

  public List<MessageSourceResolvable> assignPassword(User user, String password) {
    List<MessageSourceResolvable> errors = passwordValidationStrategy.validatePassword(user, password);
    if(errors.size() > 0) return errors;
    // validate // problem
    // check for reuse // problem
    boolean newPassword = userService.isNewPassword(user, password); // TODO or userService.isPasswordUsed....
    if(!newPassword) {
      MessageSourceResolvable msg = new DefaultMessageSourceResolvable(new String[] { PasswordValidationErrors.OLD_PASSWORD_REUSE.name() }, "DefaultUserPasswordServiceImpl.cannotReusePassword");
      errors.add(msg);
      return errors;
    }
    // save
    userService.updatePassword(user, password); // TODO User new api to save
    return errors;
  }

  public User authenticate(String login, String password) throws AuthenticationFailedException {
    User user = userService.getUserWithLogin(login);
    if(user == null) throw new AuthenticationFailedException(); // User does not exist.
    String currentHashedPassword = user.getPassword();
    String suppliedHashedPassword = passwordHashingStrategy.hashPassword(password);
    if(suppliedHashedPassword.equals(currentHashedPassword)) {
      return user;
    } else {
      throw new AuthenticationFailedException();
    }
  }

  public String generatePassword(User user) {
    return passwordValidationStrategy.generatePassword(user);
  }

  public int getPasswordValidDaysLeft(User user) {
    // TODO Auto-generated method stub
    return 0;
  }

  public boolean isNewPasswordRequired(User user) {
    if(preventPasswordReuse) {
      // TODO Auto-generated method stub
      return false;
    } else {
      return false;
    }
  }

  public String resetPassword(User user) {
    String newPassword = generatePassword(user);
    assignPassword(user, newPassword);
    return newPassword;
  }

  public List<MessageSourceResolvable> validatePassword(User user, String password) {
    return passwordValidationStrategy.validatePassword(user, password);
  }

  /**
   * Validate passwordValidityPeriod value. Can be called after setting the passwordValidityPeriod value. For example
   * this method is called by Spring after this bean is initialised.
   * @throws UserPasswordServiceException Thrown if passwordValidityPeriod is less than 1.
   */
  public void validatePasswordValidityPeriod() throws UserPasswordServiceException {
    if(passwordValidityPeriod < 1) throw new UserPasswordServiceException("The passwordValidityPeriod [" + passwordValidityPeriod + "] must not be less than [1].");
  }

}
