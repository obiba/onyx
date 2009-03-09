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

/**
 * Password Validation Error codes used by {@code AbstractPasswordLengthValidationStrategy} and {@code
 * ConfigurablePasswordValidationStrategyImpl}.
 */
public enum PasswordValidationErrors {

  /** The password is too short. */
  TOO_SHORT("tooShort"),
  /** The password is too long. */
  TOO_LONG("tooLong"),
  /** The password contains the user's first name. */
  CONTAINS_FIRST_NAME("containsFirstName"),
  /** The password contains the user's last name. */
  CONTAINS_LAST_NAME("containsLastName"),
  /** The password contains the user's login name. */
  CONTAINS_LOGIN_NAME("containsLoginName"),
  /** The password contains a character that is now allowed. */
  CHARACTER_NOT_ALLOWED("characterNotAllowed"),
  /** The password does not contains characters from enough character groups. */
  MINIMUM_CHARACTER_GROUP_USAGE_NOT_MET("minimumCharacterGroupUsageNotMet"),
  /** Cannot reuse old passwords. */
  OLD_PASSWORD_REUSE("oldPassWordReuse");

  private String assetKey;

  private PasswordValidationErrors(String assetKey) {
    this.assetKey = assetKey;
  }

  public String assetKey() {
    return this.getClass().getSimpleName() + "." + assetKey;
  }

}
