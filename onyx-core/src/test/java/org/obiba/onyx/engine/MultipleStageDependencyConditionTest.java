/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.engine;

import java.util.Arrays;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.engine.MultipleStageDependencyCondition.Operator;

public class MultipleStageDependencyConditionTest {

  ActiveInterviewService activeInterviewServiceMock;

  StageDependencyCondition first = EasyMock.createMock(StageDependencyCondition.class);

  StageDependencyCondition second = EasyMock.createMock(StageDependencyCondition.class);

  @Before
  public void setupMocks() {
    activeInterviewServiceMock = EasyMock.createMock(ActiveInterviewService.class);
    first = EasyMock.createMock(StageDependencyCondition.class);
    second = EasyMock.createMock(StageDependencyCondition.class);
  }

  private MultipleStageDependencyCondition setupCondition(Operator op) {
    MultipleStageDependencyCondition mult = new MultipleStageDependencyCondition();
    mult.setOperator(op);
    mult.setConditions(Arrays.asList(first, second));
    return mult;
  }

  @Test
  public void testAndWhenBothReturnTrue() {
    EasyMock.expect(first.isDependencySatisfied(null, activeInterviewServiceMock)).andReturn(true);
    EasyMock.expect(second.isDependencySatisfied(null, activeInterviewServiceMock)).andReturn(true);

    EasyMock.replay(first, second);
    MultipleStageDependencyCondition condition = setupCondition(Operator.AND);
    Boolean result = condition.isDependencySatisfied(null, activeInterviewServiceMock);
    EasyMock.verify(first, second);
    Assert.assertNotNull(result);
    Assert.assertTrue(result);
  }

  /**
   * Test that for the AND operator, when one condition is null, {@link MultipleStageDependencyCondition} returns null
   * when the second's return value is either true or null.
   */
  @Test
  public void testAndWhenOneIsNullAndOtherIsNotFalse() {
    testChangingCondition(Operator.AND, new StableConditionSetupCallback(first, null), second, null, true, null);
    testChangingCondition(Operator.AND, new StableConditionSetupCallback(second, null), first, null, true, null);
  }

  /**
   * Test that for the AND operator, when one condition is false, {@link MultipleStageDependencyCondition} always
   * returns false, regardless of the second's return value.
   */
  @Test
  public void testAndWhenEitherReturnFalse() {
    testChangingCondition(Operator.AND, new StableConditionSetupCallback(first, false), second, false, true, false, null);
    testChangingCondition(Operator.AND, new StableConditionSetupCallback(second, false), first, false, true, false, null);
  }

  @Test
  public void testOrWhenBothReturnFalse() {
    EasyMock.expect(first.isDependencySatisfied(null, activeInterviewServiceMock)).andReturn(false);
    EasyMock.expect(second.isDependencySatisfied(null, activeInterviewServiceMock)).andReturn(false);

    EasyMock.replay(first, second);
    MultipleStageDependencyCondition condition = setupCondition(Operator.OR);
    Boolean result = condition.isDependencySatisfied(null, activeInterviewServiceMock);
    EasyMock.verify(first, second);
    Assert.assertNotNull(result);
    Assert.assertFalse(result);
  }

  /**
   * Test that for the OR operator, when one condition is null, {@link MultipleStageDependencyCondition} returns null
   * when the second's return value is either false or null.
   */
  @Test
  public void testOrWhenOneIsNullAndOtherIsNotTrue() {
    testChangingCondition(Operator.OR, new StableConditionSetupCallback(first, null), second, null, false, null);
    testChangingCondition(Operator.OR, new StableConditionSetupCallback(second, null), first, null, false, null);
  }

  /**
   * Test that for the OR operator, when one condition is true, {@link MultipleStageDependencyCondition} always returns
   * true, regardless of the second's return value.
   */
  @Test
  public void testOrWhenEitherReturnTrue() {
    testChangingCondition(Operator.OR, new StableConditionSetupCallback(first, true), second, true, true, false, null);
    testChangingCondition(Operator.OR, new StableConditionSetupCallback(second, true), first, true, true, false, null);
  }

  /**
   * Utility method for testing cases where the same return value is expected when one condition always has the same
   * value and the other may have multiple different ones. For example, for the OR operator, true is returned whenever
   * one of the two conditions is true, regardless of the second's value.
   * 
   * @param op the Operator used to create the test instance
   * @param callback a callback method that creates the expectations for the stable condition
   * @param changingCondition the condition for which the return value varies.
   * @param assertValue the expected (constant) return value of the test instance
   * @param testValues the values that the changing condition can return
   */
  private void testChangingCondition(Operator op, StableConditionSetupCallback callback, StageDependencyCondition changingCondition, Boolean assertValue, Boolean... testValues) {
    MultipleStageDependencyCondition condition = setupCondition(op);
    for(int i = 0; i < testValues.length; i++) {
      callback.setupStableCondition();
      EasyMock.expect(changingCondition.isDependencySatisfied(null, activeInterviewServiceMock)).andReturn(testValues[i]);
      EasyMock.replay(first, second);
      Boolean result = condition.isDependencySatisfied(null, activeInterviewServiceMock);
      EasyMock.verify(first, second);
      Assert.assertEquals(assertValue, result);

      EasyMock.reset(first, second);
    }
  }

  private class StableConditionSetupCallback {
    private StageDependencyCondition condition;

    private Boolean returnValue;

    public StableConditionSetupCallback(StageDependencyCondition condition, Boolean returnValue) {
      this.condition = condition;
      this.returnValue = returnValue;
    }

    void setupStableCondition() {
      EasyMock.expect(condition.isDependencySatisfied(null, activeInterviewServiceMock)).andReturn(returnValue);
    }
  }
}
