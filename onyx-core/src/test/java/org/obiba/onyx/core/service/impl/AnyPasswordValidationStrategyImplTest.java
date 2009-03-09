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

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.obiba.onyx.core.domain.user.User;
import org.obiba.onyx.core.service.IPasswordValidationStrategy;
import org.springframework.context.MessageSourceResolvable;

/**
 * Tests AnyPasswordValidationStrategyImpl.
 */
public class AnyPasswordValidationStrategyImplTest {

  private IPasswordValidationStrategy anyPasswordStrategy;

  private User user;

  @Before
  public void setup() {
    anyPasswordStrategy = new AnyPasswordValidationStrategyImpl();
    user = new User();
  }

  @Test
  public void testValidatePasswordReturnsList() {
    List<MessageSourceResolvable> messageList = anyPasswordStrategy.validatePassword(user, "password");
    Assert.assertNotNull(messageList);
    Assert.assertEquals(0, messageList.size()); // Zero messages. Always valid.
  }

  /**
   * Ensure that even null passwords validate. This validator performs no validation.
   */
  @Test
  public void testValidateNullPassword() {
    List<MessageSourceResolvable> messageList = anyPasswordStrategy.validatePassword(user, null);
    Assert.assertTrue(messageList.size() == 0);
  }

  @Test
  public void testGeneratePasswordReturnsValue() {
    String password = anyPasswordStrategy.generatePassword(user);
    Assert.assertNotNull(password);
  }

  @Test
  public void testGeneratePasswordReturnsConstant() {
    String password = anyPasswordStrategy.generatePassword(user);
    Assert.assertEquals(AnyPasswordValidationStrategyImpl.password, password);
  }

}
