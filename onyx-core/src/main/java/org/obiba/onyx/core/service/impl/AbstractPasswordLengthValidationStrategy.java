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
import org.springframework.context.support.DefaultMessageSourceResolvable;

/**
 * Provides validation to ensure that a password meets minimum and maximum length requirements. Validation classes that
 * require minimum and maximum password lengths may extend this class. Subclass implementations must remember to call
 * {@code super.validatePassword} at the top of their {@code validatePassword} method and any items in the returned list
 * to the total results.
 */
public abstract class AbstractPasswordLengthValidationStrategy implements IPasswordValidationStrategy {

  /** Minimum password length. Zero [0] indicates no minimum. */
  private int minimumSize = 8;

  /** Maximum password length. Zero [0] indicates no maximum. */
  private int maximumSize = 14;

  private static final int NO_MAX_SIZE = 0;

  /**
   * Sets the minimum password length. Zero [0] indicates no minimum.
   * @param minimumSize minimum password length.
   */
  public void setMinimumSize(int minimumSize) {
    this.minimumSize = minimumSize;
  }

  /**
   * Sets the maximum password length. Zero [0] indicates no maximum.
   * @param maximumSize maximum password length.
   */
  public void setMaximumSize(int maximumSize) {
    this.maximumSize = maximumSize;
  }

  public List<MessageSourceResolvable> validatePassword(User user, String password) {
    if(user == null) throw new IllegalArgumentException("User cannot be null.");
    if(password == null) throw new IllegalArgumentException("Password cannot be null.");
    try {
      validateSizes();
    } catch(ValidationStrategyException e) {
      // Throw a runtime exception if the values have changed since wiring at startup.
      throw new IllegalArgumentException(e.getMessage());
    }
    List<MessageSourceResolvable> list = new ArrayList<MessageSourceResolvable>();

    if(password.length() < minimumSize) {
      list.add(createErrorMessage(PasswordValidationErrors.TOO_SHORT, new String[] { "" + getMinimumSize() }));
    }

    if(maximumSize != NO_MAX_SIZE && password.length() > maximumSize) {
      list.add(createErrorMessage(PasswordValidationErrors.TOO_LONG, new String[] { "" + getMaximumSize() }));
    }

    return list;
  }

  /**
   * Validate size attributes. Can be called after setting the size values. For example this method is called by Spring
   * after this bean is initialised.
   * @throws ValidationStrategyException thrown if sizes have invalid values.
   */
  public void validateSizes() throws ValidationStrategyException {
    if(minimumSize < 0) throw new ValidationStrategyException("The minumumSize [" + minimumSize + "] cannot be less than zero [0]. ");
    if(maximumSize < 0) throw new ValidationStrategyException("The maximumSize [" + maximumSize + "] cannot be less than zero [0]. ");
    if(maximumSize != NO_MAX_SIZE && minimumSize > maximumSize) throw new ValidationStrategyException("The minimumSize [" + minimumSize + "] can not be greater than the maximumSize [" + maximumSize + "].");
  }

  protected MessageSourceResolvable createErrorMessage(PasswordValidationErrors error, String[] assetArguments) {
    MessageSourceResolvable msg = new DefaultMessageSourceResolvable(new String[] { error.name() }, assetArguments, error.assetKey());
    return msg;
  }

  protected MessageSourceResolvable createErrorMessage(PasswordValidationErrors error) {
    return createErrorMessage(error, null);
  }

  protected int getMinimumSize() {
    return minimumSize;
  }

  protected int getMaximumSize() {
    return maximumSize;
  }
}
