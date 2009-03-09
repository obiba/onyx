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

import org.obiba.onyx.core.domain.user.User;
import org.springframework.context.MessageSourceResolvable;

/**
 * Provides the ability to validate passwords against a set of rule. Also provides the ability to generate a password
 * that obeys those same rules.
 */
public interface IPasswordValidationStrategy {

  /**
   * Validates that a plain text password respects the password construction rules. Null handling of {@code user} and
   * {@code password} is an implementation issue.
   * @param user Password is being validated for this user.
   * @param password The plain text password.
   * @return A List of error messages. An empty List for success.
   */
  List<MessageSourceResolvable> validatePassword(User user, String password);

  /**
   * Generates a password for the supplied user. Null handling of {@code user} is an implementation issue.
   * @param user The user the password is being generated for.
   * @return A plain text password.
   */
  String generatePassword(User user);

}
