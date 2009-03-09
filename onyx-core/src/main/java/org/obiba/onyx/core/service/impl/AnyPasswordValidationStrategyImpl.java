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

import java.util.ArrayList;
import java.util.List;

import org.obiba.onyx.core.domain.user.User;
import org.obiba.onyx.core.service.IPasswordValidationStrategy;
import org.springframework.context.MessageSourceResolvable;

/**
 * A strategy to validate any password. The {@code generatePassword} method always returns the same value.
 */
public class AnyPasswordValidationStrategyImpl implements IPasswordValidationStrategy {

  /** Value of password returned by generatePassword. */
  static final String password = "password";

  public String generatePassword(User user) {
    return AnyPasswordValidationStrategyImpl.password;
  }

  public List<MessageSourceResolvable> validatePassword(User user, String password) {
    return new ArrayList<MessageSourceResolvable>(0);
  }
}
