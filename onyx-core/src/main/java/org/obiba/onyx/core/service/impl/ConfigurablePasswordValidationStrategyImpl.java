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

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.obiba.onyx.core.domain.user.User;
import org.obiba.onyx.core.service.IPasswordValidationStrategy;
import org.springframework.context.MessageSourceResolvable;

/**
 *
 */
public class ConfigurablePasswordValidationStrategyImpl extends AbstractPasswordLengthValidationStrategy implements IPasswordValidationStrategy {

  private boolean preventUserAttributeUsage = true;

  /** Use ranges such as "A-Z" or lists of characters such as "[abcd]". Square brackets indicate list. */
  private String[] allowedCharacterGroups = { "A-Z", "a-z", "0-9" };

  private Pattern characterListPattern = Pattern.compile("^\\[(.*)\\]$"); // Match [()&], [[^@#]], etc.

  private Pattern characterRangePattern = Pattern.compile("^(.)-(.)$"); // Match A-Z, 0-9, etc.

  private int minimumCharacterGroupsUsage = 3;

  public String generatePassword(User user) {
    Random generator = new Random();
    String password = "";
    char[] passwordCharacters = createValidPasswordCharArray();

    do {
      do {
        char c = passwordCharacters[generator.nextInt(passwordCharacters.length)];
        password += c;
      } while(password.length() < getMinimumSize());
    } while(!checkPassword(user, password));
    // Note: If minimum password size is set to 0, the generated password will be one character long.
    return password;
  }

  public List<MessageSourceResolvable> validatePassword(User user, String password) {
    List<MessageSourceResolvable> list = super.validatePassword(user, password);
    try {
      validateCharacterGroups();
    } catch(ValidationStrategyException e) {
      // Throw a runtime exception if the values have changed since wiring at startup.
      throw new IllegalArgumentException(e.getMessage());
    }
    if(preventUserAttributeUsage) validateUserAttributeUsage(list, user, password);
    validateAllowedCharacters(list, password);
    validateMinimumCharacterGroupUsageMet(list, password);
    return list;
  }

  /**
   * Checks to ensure that the user's first name, last name and login name are not part of their password.
   * @param list Error messages are added to this list.
   * @param user The user who is having their password checked.
   * @param password The password being checked.
   */
  private void validateUserAttributeUsage(List<MessageSourceResolvable> list, User user, String password) {
    assert list != null;
    assert user != null;
    assert password != null;
    if(user.getFirstName() != null && password.toLowerCase().contains(user.getFirstName().toLowerCase())) {
      list.add(createErrorMessage(PasswordValidationErrors.CONTAINS_FIRST_NAME));
    }
    if(user.getLastName() != null && password.toLowerCase().contains(user.getLastName().toLowerCase())) {
      list.add(createErrorMessage(PasswordValidationErrors.CONTAINS_LAST_NAME));
    }
    if(user.getLogin() != null && password.toLowerCase().contains(user.getLogin().toLowerCase())) {
      list.add(createErrorMessage(PasswordValidationErrors.CONTAINS_LOGIN_NAME));
    }
  }

  /**
   * Checks to ensure that the password only contains characters that appear on the allowedCharacterGroup List.
   * @param list Error messages are added to this list.
   * @param password The password being checked.
   */
  private void validateAllowedCharacters(List<MessageSourceResolvable> list, String password) {
    assert list != null;
    assert password != null;
    String regex = "";
    for(String group : getEscapedAllowedCharacterGroups()) {
      regex += group;
    }

    if(!Pattern.compile("^[" + regex + "]+$").matcher(password).matches()) {
      list.add(createErrorMessage(PasswordValidationErrors.CHARACTER_NOT_ALLOWED, new String[] { getAllowedCharacterGroupsAsPrintableString() }));
    }

  }

  /**
   * Checks to ensure that the password contains characters from different character groups. The {@code
   * minimumCharacterGroupsUsage} specifies the minimum number of groups which can be used for a password to be valid.
   * For example if there are 5 character groups and {@code minimumCharacterGroupsUsage} is set to 4, then only 4 of the
   * groups need be represented in the password. It does not matter which 4.
   * @param list Error messages are added to this list.
   * @param password The password being checked.
   */
  private void validateMinimumCharacterGroupUsageMet(List<MessageSourceResolvable> list, String password) {
    assert list != null;
    assert password != null;
    int characterGroupsUsed = 0;
    for(String group : getEscapedAllowedCharacterGroups()) {
      if(Pattern.compile(".*[" + group + "]+.*").matcher(password).matches()) {
        characterGroupsUsed++;
      }
    }
    if(characterGroupsUsed < minimumCharacterGroupsUsage) {
      list.add(createErrorMessage(PasswordValidationErrors.MINIMUM_CHARACTER_GROUP_USAGE_NOT_MET, new String[] { "" + minimumCharacterGroupsUsage, getAllowedCharacterGroupsAsPrintableString() }));
    }
  }

  public void setPreventUserAttributeUsage(boolean preventUserAttributeUsage) {
    this.preventUserAttributeUsage = preventUserAttributeUsage;
  }

  public void setAllowedCharacterGroups(String[] allowedCharacterGroups) {
    this.allowedCharacterGroups = allowedCharacterGroups;
  }

  public void setMinimumCharacterGroupsUsage(int minimumCharacterGroupsUsage) {
    this.minimumCharacterGroupsUsage = minimumCharacterGroupsUsage;
  }

  /**
   * Validate size attributes. Can be called after setting the size values. For example this method is called by Spring
   * after this bean is initialised.
   * @throws ValidationStrategyException thrown if sizes have invalid values.
   */
  public void validateCharacterGroups() throws ValidationStrategyException {
    if(allowedCharacterGroups == null) throw new ValidationStrategyException("The allowedCharacterGroups cannot be null.");
    if(allowedCharacterGroups.length == 0) throw new ValidationStrategyException("The allowedCharacterGroups must contain at least one group.");
    if(minimumCharacterGroupsUsage > allowedCharacterGroups.length) throw new ValidationStrategyException("The minimumCharacterGroupsUsage [" + minimumCharacterGroupsUsage + "] must not exceed the number of allowedCharacterGroups [" + allowedCharacterGroups + "].");
  }

  /**
   * Returns a char[] of all the allowed characters. The allowed characters are obtained from {@code
   * allowedCharacterGroups}.
   * @return A char[] of all allowed password characters.
   */
  char[] createValidPasswordCharArray() {
    Set<Character> passwordCharacterSet = new HashSet<Character>();
    char[] passwordCharArray = new char[0];
    for(String group : allowedCharacterGroups) {
      Matcher rangeMatcher = characterRangePattern.matcher(group); // Match A-Z, 0-9, etc.
      Matcher characterListMatcher = characterListPattern.matcher(group); // Match [()&], [[^@#]], etc.
      if(rangeMatcher.matches()) {
        char start = rangeMatcher.group(1).charAt(0);
        char end = rangeMatcher.group(2).charAt(0);
        for(char c = start; c <= end; c++) {
          passwordCharacterSet.add(c);
        }
      } else if(characterListMatcher.matches()) {
        String characterList = characterListMatcher.group(1);
        for(int i = 0; i < characterList.length(); i++) {
          passwordCharacterSet.add(characterList.charAt(i));
        }
      }
    }
    if(!passwordCharacterSet.isEmpty()) {
      passwordCharArray = new char[passwordCharacterSet.size()];
      int i = 0;
      for(Character character : passwordCharacterSet) {
        passwordCharArray[i++] = character;
      }
    }
    return passwordCharArray;
  }

  /**
   * Convenience method returns true of the supplied password is valid.
   * @param user The user the password is associated with.
   * @param password The plain text password.
   * @return True if password is valid, false otherwise.
   */
  private boolean checkPassword(User user, String password) {
    List<MessageSourceResolvable> messageList = validatePassword(user, password);
    return messageList.size() == 0 ? true : false;
  }

  String getAllowedCharacterGroupsAsPrintableString() {
    StringBuilder sb = new StringBuilder();
    sb = getAllowedCharacterListsAsPrintableString(sb);
    if(sb.length() != 0) sb.append(",");
    sb = getAllowedCharacterRangesAsPrintableString(sb);
    return sb.toString();
  }

  private StringBuilder getAllowedCharacterRangesAsPrintableString(StringBuilder sb) {
    for(String group : allowedCharacterGroups) {
      Matcher rangeMatcher = characterRangePattern.matcher(group); // Match A-Z, 0-9, etc.
      if(rangeMatcher.matches()) {
        sb.append("[").append(group).append("]").append(",");
      }
    }
    sb = removeLastCommaFromList(sb);
    return sb;
  }

  private StringBuilder getAllowedCharacterListsAsPrintableString(StringBuilder sb) {
    for(String group : allowedCharacterGroups) {
      Matcher characterListMatcher = characterListPattern.matcher(group); // Match [()&], [[^@#]], etc.
      if(characterListMatcher.matches()) {
        sb.append(group).append(",");
      }
    }
    sb = removeLastCommaFromList(sb);
    return sb;
  }

  private StringBuilder removeLastCommaFromList(StringBuilder sb) {
    if(sb.length() == 0) return sb;
    if(sb.charAt(sb.length() - 1) == ',') {
      sb.setLength(sb.length() - 1);
    }
    return sb;
  }

  private String[] getEscapedAllowedCharacterGroups() {
    String[] escapedAllowedCharacterGroups = new String[allowedCharacterGroups.length];
    for(int i = 0; i < allowedCharacterGroups.length; i++) {
      Matcher rangeMatcher = characterRangePattern.matcher(allowedCharacterGroups[i]); // Match A-Z, 0-9, etc.
      Matcher characterListMatcher = characterListPattern.matcher(allowedCharacterGroups[i]); // Match [()&], [[^@#]]
      if(rangeMatcher.matches()) {
        escapedAllowedCharacterGroups[i] = allowedCharacterGroups[i];
      } else if(characterListMatcher.matches()) {
        String characterList = characterListMatcher.group(1);
        escapedAllowedCharacterGroups[i] = Pattern.quote(characterList);
      }
    }
    return escapedAllowedCharacterGroups;
  }
}
