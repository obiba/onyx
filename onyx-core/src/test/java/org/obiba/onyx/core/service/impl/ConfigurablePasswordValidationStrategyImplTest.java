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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.obiba.onyx.core.domain.user.User;
import org.obiba.onyx.core.service.IPasswordValidationStrategy;
import org.springframework.context.MessageSourceResolvable;

/**
 *
 */
public class ConfigurablePasswordValidationStrategyImplTest {

  private IPasswordValidationStrategy passwordStrategy;

  private User user;

  @Before
  public void setUp() {
    passwordStrategy = new ConfigurablePasswordValidationStrategyImpl();
    user = new User();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testValidateSizesWithNullUser() {
    passwordStrategy.validatePassword(null, "password");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testValidateSizesWithNullPassword() {
    passwordStrategy.validatePassword(user, null);
  }

  @Test
  public void testValidatePasswordReturnsList() {
    ((ConfigurablePasswordValidationStrategyImpl) passwordStrategy).setMinimumCharacterGroupsUsage(1);
    List<MessageSourceResolvable> messageList = passwordStrategy.validatePassword(user, "password");
    Assert.assertNotNull(messageList);
    Assert.assertTrue(messageList.size() == 0); // No error messages. Passed.
  }

  @Test(expected = ValidationStrategyException.class)
  public void testValidateSizesWithNegativeMaximumLength() throws ValidationStrategyException {
    ((AbstractPasswordLengthValidationStrategy) passwordStrategy).setMaximumSize(-1);
    ((AbstractPasswordLengthValidationStrategy) passwordStrategy).validateSizes();
  }

  @Test(expected = ValidationStrategyException.class)
  public void testValidateSizesWithNegativeMinimumLength() throws ValidationStrategyException {
    ((AbstractPasswordLengthValidationStrategy) passwordStrategy).setMinimumSize(-1);
    ((AbstractPasswordLengthValidationStrategy) passwordStrategy).validateSizes();
  }

  @Test(expected = ValidationStrategyException.class)
  public void testValidateSizesWithMinimumValueGreaterThanMaximumValue() throws ValidationStrategyException {
    ((AbstractPasswordLengthValidationStrategy) passwordStrategy).setMinimumSize(20);
    ((AbstractPasswordLengthValidationStrategy) passwordStrategy).setMaximumSize(10);
    ((AbstractPasswordLengthValidationStrategy) passwordStrategy).validateSizes();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testValidatePasswordWithNegativeMinimumValue() {
    ((AbstractPasswordLengthValidationStrategy) passwordStrategy).setMinimumSize(-1);
    passwordStrategy.validatePassword(user, "password");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testValidatePasswordWithNegativeMaximumValue() {
    ((AbstractPasswordLengthValidationStrategy) passwordStrategy).setMaximumSize(-1);
    passwordStrategy.validatePassword(user, "password");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testValidatePasswordWithMinimumValueGreaterThanMaximumValue() {
    ((AbstractPasswordLengthValidationStrategy) passwordStrategy).setMinimumSize(20);
    ((AbstractPasswordLengthValidationStrategy) passwordStrategy).setMaximumSize(10);
    passwordStrategy.validatePassword(user, "password");
  }

  @Test
  public void testValidatePasswordTooShort() {
    ((AbstractPasswordLengthValidationStrategy) passwordStrategy).setMinimumSize(5);
    List<MessageSourceResolvable> messageList = passwordStrategy.validatePassword(user, "pass");
    Assert.assertTrue(containsErrorCode(messageList, PasswordValidationErrors.TOO_SHORT));
  }

  @Test
  public void testValidatePasswordTooLong() {
    ((AbstractPasswordLengthValidationStrategy) passwordStrategy).setMinimumSize(3);
    ((AbstractPasswordLengthValidationStrategy) passwordStrategy).setMaximumSize(5);
    List<MessageSourceResolvable> messageList = passwordStrategy.validatePassword(user, "passwd");
    Assert.assertTrue(containsErrorCode(messageList, PasswordValidationErrors.TOO_LONG));
  }

  @Test
  public void testValidatePasswordNoMinimumLength() {
    ((AbstractPasswordLengthValidationStrategy) passwordStrategy).setMinimumSize(0);
    ((ConfigurablePasswordValidationStrategyImpl) passwordStrategy).setMinimumCharacterGroupsUsage(1);
    List<MessageSourceResolvable> messageList = passwordStrategy.validatePassword(user, "1");
    Assert.assertTrue(messageList.size() == 0); // No error messages. Passed.
  }

  @Test
  public void testValidatePasswordNoMaximumLength() {
    ((AbstractPasswordLengthValidationStrategy) passwordStrategy).setMaximumSize(0);
    ((ConfigurablePasswordValidationStrategyImpl) passwordStrategy).setMinimumCharacterGroupsUsage(1);
    List<MessageSourceResolvable> messageList = passwordStrategy.validatePassword(user, "Thisismylongandsecretpassword");
    Assert.assertTrue(messageList.size() == 0); // No error messages. Passed.
  }

  @Test
  public void testValidatePasswordContainsFirstName() {
    user.setFirstName("Molly");
    List<MessageSourceResolvable> messageList = passwordStrategy.validatePassword(user, "Molly4");
    Assert.assertTrue(containsErrorCode(messageList, PasswordValidationErrors.CONTAINS_FIRST_NAME));
  }

  @Test
  public void testValidatePasswordContainsLastName() {
    user.setLastName("Stephenson");
    List<MessageSourceResolvable> messageList = passwordStrategy.validatePassword(user, "Stephenson22");
    Assert.assertTrue(containsErrorCode(messageList, PasswordValidationErrors.CONTAINS_LAST_NAME));
  }

  @Test
  public void testValidatePasswordContainsLoginName() {
    user.setLogin("fherbert");
    List<MessageSourceResolvable> messageList = passwordStrategy.validatePassword(user, "00fherbert");
    Assert.assertTrue(containsErrorCode(messageList, PasswordValidationErrors.CONTAINS_LOGIN_NAME));
  }

  @Test
  public void testTurningOffUserAttributePasswordValidation() {
    user.setFirstName("Molly");
    user.setLastName("Stephenson");
    user.setLogin("fherbert");
    ((AbstractPasswordLengthValidationStrategy) passwordStrategy).setMaximumSize(0);
    ((ConfigurablePasswordValidationStrategyImpl) passwordStrategy).setPreventUserAttributeUsage(false);
    List<MessageSourceResolvable> messageList = passwordStrategy.validatePassword(user, "MollyStephensonfherbert");
    Assert.assertFalse(containsErrorCode(messageList, PasswordValidationErrors.CONTAINS_FIRST_NAME));
    Assert.assertFalse(containsErrorCode(messageList, PasswordValidationErrors.CONTAINS_LAST_NAME));
    Assert.assertFalse(containsErrorCode(messageList, PasswordValidationErrors.CONTAINS_LOGIN_NAME));
  }

  @Test(expected = ValidationStrategyException.class)
  public void testCharacterGroupsNull() throws ValidationStrategyException {
    ((ConfigurablePasswordValidationStrategyImpl) passwordStrategy).setAllowedCharacterGroups(null);
    ((ConfigurablePasswordValidationStrategyImpl) passwordStrategy).validateCharacterGroups();
  }

  @Test(expected = ValidationStrategyException.class)
  public void testCharacterGroupsEmpty() throws ValidationStrategyException {
    ((ConfigurablePasswordValidationStrategyImpl) passwordStrategy).setAllowedCharacterGroups(new String[] {});
    ((ConfigurablePasswordValidationStrategyImpl) passwordStrategy).validateCharacterGroups();
  }

  @Test(expected = ValidationStrategyException.class)
  public void testNotEnoughCharacterGroupsToValidate() throws ValidationStrategyException {
    // Configure one [1] character group
    ((ConfigurablePasswordValidationStrategyImpl) passwordStrategy).setAllowedCharacterGroups(new String[] { "A-Z" });
    // Require two [2] character groups to form a valid password.
    ((ConfigurablePasswordValidationStrategyImpl) passwordStrategy).setMinimumCharacterGroupsUsage(2);
    ((ConfigurablePasswordValidationStrategyImpl) passwordStrategy).validateCharacterGroups();
  }

  @Test
  public void testCharacterNotInCharacterGroupSingle() {
    ((ConfigurablePasswordValidationStrategyImpl) passwordStrategy).setMinimumCharacterGroupsUsage(1);
    ((ConfigurablePasswordValidationStrategyImpl) passwordStrategy).setAllowedCharacterGroups(new String[] { "A-C" });
    List<MessageSourceResolvable> messageList = passwordStrategy.validatePassword(user, "DDDDDDDDDD");
    Assert.assertTrue(containsErrorCode(messageList, PasswordValidationErrors.CHARACTER_NOT_ALLOWED));
  }

  @Test
  public void testCharacterNotInCharacterGroupMultiple() {
    ((ConfigurablePasswordValidationStrategyImpl) passwordStrategy).setMinimumCharacterGroupsUsage(1);
    ((ConfigurablePasswordValidationStrategyImpl) passwordStrategy).setAllowedCharacterGroups(new String[] { "A-C", "F-H", "J-L" });
    List<MessageSourceResolvable> messageList = passwordStrategy.validatePassword(user, "DEIMN");
    Assert.assertTrue(containsErrorCode(messageList, PasswordValidationErrors.CHARACTER_NOT_ALLOWED));
  }

  @Test
  public void testPasswordWithNotEnoughCharactersFromEachGroup() {
    ((ConfigurablePasswordValidationStrategyImpl) passwordStrategy).setMinimumCharacterGroupsUsage(3);
    ((ConfigurablePasswordValidationStrategyImpl) passwordStrategy).setAllowedCharacterGroups(new String[] { "A-C", "F-H", "J-L" });
    List<MessageSourceResolvable> messageList = passwordStrategy.validatePassword(user, "AF");
    Assert.assertTrue(containsErrorCode(messageList, PasswordValidationErrors.MINIMUM_CHARACTER_GROUP_USAGE_NOT_MET));
  }

  @Test
  public void testPasswordWithEnoughCharactersFromEachGroup() {
    ((ConfigurablePasswordValidationStrategyImpl) passwordStrategy).setMinimumSize(0);
    ((ConfigurablePasswordValidationStrategyImpl) passwordStrategy).setMinimumCharacterGroupsUsage(5);
    ((ConfigurablePasswordValidationStrategyImpl) passwordStrategy).setAllowedCharacterGroups(new String[] { "A-C", "F-H", "J-L", "0-9", "[[]" });
    List<MessageSourceResolvable> messageList = passwordStrategy.validatePassword(user, "[8LHC");
    Assert.assertTrue(messageList.size() == 0); // No error messages. Passed.
  }

  @Test
  public void testCreateValidPasswordCharArray() {
    ((ConfigurablePasswordValidationStrategyImpl) passwordStrategy).setAllowedCharacterGroups(new String[] { "A-C", "A-B", "F-H", "J-L", "0-2", "[[@!()]" });
    char[] allowedPasswordChars = ((ConfigurablePasswordValidationStrategyImpl) passwordStrategy).createValidPasswordCharArray();
    char[] correct = { 'A', 'B', 'C', 'F', 'G', 'H', 'J', 'K', 'L', '0', '1', '2', '[', '@', '!', '(', ')' };
    Set<Character> correctSet = new HashSet<Character>(correct.length);
    for(char c : correct) {
      correctSet.add(c); // Populate Hash with correct chars.
    }
    for(char c : allowedPasswordChars) {
      correctSet.remove(c); // Remove chars from Hash contained in allowedPasswordChars.
    }
    Assert.assertTrue(correctSet.size() == 0); // The size of the Hash should be zero [0]. All chars accounted for.
  }

  @Test
  public void testCreatePasswordWithMinimumSizeSetToZero() {
    ((ConfigurablePasswordValidationStrategyImpl) passwordStrategy).setMinimumSize(0);
    ((ConfigurablePasswordValidationStrategyImpl) passwordStrategy).setMinimumCharacterGroupsUsage(1);
    ((ConfigurablePasswordValidationStrategyImpl) passwordStrategy).setAllowedCharacterGroups(new String[] { "A-C" });
    String password = ((ConfigurablePasswordValidationStrategyImpl) passwordStrategy).generatePassword(user);
    Assert.assertTrue(password.length() == 1); // Minimum password length is 1.
  }

  @Test
  public void testRandomDistributionForCreatePassword() {
    ((ConfigurablePasswordValidationStrategyImpl) passwordStrategy).setMinimumSize(4);
    ((ConfigurablePasswordValidationStrategyImpl) passwordStrategy).setMinimumCharacterGroupsUsage(1);
    ((ConfigurablePasswordValidationStrategyImpl) passwordStrategy).setAllowedCharacterGroups(new String[] { "A-D" });
    Map<Character, Integer> distributionMap = new HashMap<Character, Integer>();
    for(int i = 0; i < 10000; i++) { // Generate 10000 passwords of length 4.
      String password = ((ConfigurablePasswordValidationStrategyImpl) passwordStrategy).generatePassword(user);
      // Count occurrences of each of the 4 possible characters in each password.
      for(int j = 0; j < password.length(); j++) {
        char c = password.charAt(j);
        if(distributionMap.containsKey(c)) {
          int count = distributionMap.get(c);
          distributionMap.put(c, ++count);
        } else {
          distributionMap.put(c, 1);
        }
      }
    }
    Set<Character> keys = distributionMap.keySet();
    for(Character c : keys) {
      // All characters should be used roughly the same number of times. An even distribution.
      // This is true in this case, but adding character groups and minimum group usage will
      // have an effect on the distribution.
      Assert.assertTrue("The distribution of [" + c + "] has the value [" + distributionMap.get(c) + "].", distributionMap.get(c) > 9700 && distributionMap.get(c) < 10300);
    }
  }

  @Test
  public void testGetAllowedCharacterGroupsAsPrintableString() {
    ((ConfigurablePasswordValidationStrategyImpl) passwordStrategy).setAllowedCharacterGroups(new String[] { "A-Z", "a-z", "0-9" });
    String actual = ((ConfigurablePasswordValidationStrategyImpl) passwordStrategy).getAllowedCharacterGroupsAsPrintableString();
    System.out.println(actual);
    Assert.assertEquals("[A-Z],[a-z],[0-9]", actual);
  }

  @Test
  public void testGetAllowedCharacterGroupsAsPrintableStringWithIndividualCharacters() {
    ((ConfigurablePasswordValidationStrategyImpl) passwordStrategy).setAllowedCharacterGroups(new String[] { "[()&]", "A-Z", "a-z", "0-9", "[[^@#]" });
    String actual = ((ConfigurablePasswordValidationStrategyImpl) passwordStrategy).getAllowedCharacterGroupsAsPrintableString();
    System.out.println(actual);
    Assert.assertEquals("[()&],[[^@#],[A-Z],[a-z],[0-9]", actual);
  }

  @Test
  public void testGetAllowedCharacterGroupsAsPrintableStringWithIndividualCharactersOnly() {
    ((ConfigurablePasswordValidationStrategyImpl) passwordStrategy).setAllowedCharacterGroups(new String[] { "[()&]", "[[^@#]" });
    String actual = ((ConfigurablePasswordValidationStrategyImpl) passwordStrategy).getAllowedCharacterGroupsAsPrintableString();
    System.out.println(actual);
    Assert.assertEquals("[()&],[[^@#]", actual);
  }

  /**
   * Convenience method to verify that a particular error code is present in a list of messages.
   * @param messageList List of messages returned by a validator.
   * @param passwordValidationError Error code we're looking for.
   * @return True if the error code is present in the list.
   */
  private boolean containsErrorCode(List<MessageSourceResolvable> messageList, PasswordValidationErrors passwordValidationError) {
    assert messageList != null;
    assert passwordValidationError != null;
    for(MessageSourceResolvable msg : messageList) {
      String[] strings = msg.getCodes();
      if(strings != null) {
        for(String code : strings) {
          if(code.equals(passwordValidationError.name())) {
            return true;
          }
        }
      }
    }
    return false; // Error code not found.
  }

}
