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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
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
public class DefaultUserPasswordServiceImplTest {

  private IPasswordValidationStrategy iPasswordValidationStrategyMock;

  private UserService userServiceMock;

  private IPasswordHashingStrategy iPasswordHashingStrategyMock;

  private UserPasswordService userPasswordService;

  private User user;

  @Before
  public void setUp() {
    userPasswordService = new DefaultUserPasswordServiceImpl();

    iPasswordValidationStrategyMock = createMock(IPasswordValidationStrategy.class);
    ((DefaultUserPasswordServiceImpl) userPasswordService).setPasswordValidationStrategy(iPasswordValidationStrategyMock);

    userServiceMock = createMock(UserService.class);
    ((DefaultUserPasswordServiceImpl) userPasswordService).setUserService(userServiceMock);

    iPasswordHashingStrategyMock = createMock(IPasswordHashingStrategy.class);
    ((DefaultUserPasswordServiceImpl) userPasswordService).setPasswordHashingStrategy(iPasswordHashingStrategyMock);

    user = new User();
  }

  @Test
  public void testGeneratePassword() {
    expect(iPasswordValidationStrategyMock.generatePassword(user)).andReturn("password");
    replay(iPasswordValidationStrategyMock);
    String password = userPasswordService.generatePassword(user);
    verify(iPasswordValidationStrategyMock);
    Assert.assertTrue(password.equals("password"));
  }

  @Test
  public void testValidatePassword() {
    expect(iPasswordValidationStrategyMock.validatePassword(user, "GoodPassword")).andReturn(new ArrayList<MessageSourceResolvable>());
    List<MessageSourceResolvable> errorList = new ArrayList<MessageSourceResolvable>(1);
    errorList.add(new DefaultMessageSourceResolvable("BadPasswordCode"));
    expect(iPasswordValidationStrategyMock.validatePassword(user, "BadPassword")).andReturn(errorList);
    replay(iPasswordValidationStrategyMock);
    userPasswordService.validatePassword(user, "GoodPassword");
    userPasswordService.validatePassword(user, "BadPassword");
    verify(iPasswordValidationStrategyMock);
  }

  @Test
  public void testAssignPasswordSuccessful() {
    expect(iPasswordValidationStrategyMock.validatePassword(user, "GoodPassword")).andReturn(new ArrayList<MessageSourceResolvable>());
    expect(userServiceMock.isNewPassword(user, "GoodPassword")).andReturn(true);
    userServiceMock.updatePassword(user, "GoodPassword");
    replay(iPasswordValidationStrategyMock);
    replay(userServiceMock);
    userPasswordService.assignPassword(user, "GoodPassword");
    verify(iPasswordValidationStrategyMock);
    verify(userServiceMock);
  }

  @Test
  public void testAssignPasswordValidationFails() {
    List<MessageSourceResolvable> errorList = new ArrayList<MessageSourceResolvable>(1);
    errorList.add(new DefaultMessageSourceResolvable("BadPasswordCode"));
    expect(iPasswordValidationStrategyMock.validatePassword(user, "BadPassword")).andReturn(errorList);
    replay(iPasswordValidationStrategyMock);
    userPasswordService.assignPassword(user, "BadPassword");
    verify(iPasswordValidationStrategyMock);
  }

  @Test
  public void testAssignPasswordFailsBecauseOfPasswordReuse() {
    expect(iPasswordValidationStrategyMock.validatePassword(user, "ReusedPassword")).andReturn(new ArrayList<MessageSourceResolvable>());
    expect(userServiceMock.isNewPassword(user, "ReusedPassword")).andReturn(false);
    replay(iPasswordValidationStrategyMock);
    replay(userServiceMock);
    userPasswordService.assignPassword(user, "ReusedPassword");
    verify(iPasswordValidationStrategyMock);
    verify(userServiceMock);
  }

  @Test
  public void testAuthenticateSuccess() throws AuthenticationFailedException {
    user.setPassword("storedHashedPassword");
    expect(userServiceMock.getUserWithLogin("GoodLoginName")).andReturn(user);
    expect(iPasswordHashingStrategyMock.hashPassword("GoodPassword")).andReturn("storedHashedPassword");
    replay(iPasswordHashingStrategyMock);
    replay(userServiceMock);
    userPasswordService.authenticate("GoodLoginName", "GoodPassword");
    verify(iPasswordHashingStrategyMock);
    verify(userServiceMock);
  }

  @Test(expected = AuthenticationFailedException.class)
  public void testAuthenticateFailsUserDoesNotExist() throws AuthenticationFailedException {
    user.setPassword("storedHashedPassword");
    expect(userServiceMock.getUserWithLogin("BadLoginName")).andReturn(null);
    replay(userServiceMock);
    userPasswordService.authenticate("BadLoginName", "BadPassword");
    verify(userServiceMock);
  }

  @Test(expected = AuthenticationFailedException.class)
  public void testAuthenticateFailedBadPassword() throws AuthenticationFailedException {
    user.setPassword("storedHashedPassword");
    expect(userServiceMock.getUserWithLogin("GoodLoginName")).andReturn(user);
    expect(iPasswordHashingStrategyMock.hashPassword("BadPassword")).andReturn("hashedBadPassword");
    replay(iPasswordHashingStrategyMock);
    replay(userServiceMock);
    userPasswordService.authenticate("GoodLoginName", "BadPassword");
    verify(iPasswordHashingStrategyMock);
    verify(userServiceMock);
  }

  // TODO
  @Ignore
  @Test
  public void testIsNewPasswordRequired() {
    // List passwords = user.getUserPasswords(); //Not implemented yet.

    // Need user.getPasswordAge() actually.
  }

  // TODO
  @Ignore
  @Test
  public void testGetPasswordValidDaysLeft() {
    // Need user.getPasswordAge()
  }

  @Test
  public void testValidatePasswordValidityPeriodSuccess() throws UserPasswordServiceException {
    ((DefaultUserPasswordServiceImpl) userPasswordService).setPasswordValidityPeriod(180);
    ((DefaultUserPasswordServiceImpl) userPasswordService).validatePasswordValidityPeriod();
    Assert.assertTrue(true); // Validation succeeded.
  }

  @Test(expected = UserPasswordServiceException.class)
  public void testValidatePasswordValidityPeriodFailure() throws UserPasswordServiceException {
    ((DefaultUserPasswordServiceImpl) userPasswordService).setPasswordValidityPeriod(-2);
    ((DefaultUserPasswordServiceImpl) userPasswordService).validatePasswordValidityPeriod();
  }
}
