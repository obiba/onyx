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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

public class TimeStampAlgorithmTest {

  private List<Action> actions;

  private static final Date sevenAm = TimeStampAlgorithmTest.constructDate(7);

  private static final Date eightAm = TimeStampAlgorithmTest.constructDate(8);

  private static final Date nineAm = TimeStampAlgorithmTest.constructDate(9);

  private static final Date tenAm = TimeStampAlgorithmTest.constructDate(10);

  @Before
  public void setUp() throws Exception {
    actions = new ArrayList<Action>();
  }

  @Test
  public void executeCompleteTest() {
    actions.add(constructAction(ActionType.EXECUTE, eightAm));
    actions.add(constructAction(ActionType.COMPLETE, nineAm));

    TimeStampAlgorithm timeStampAlgorithm = new TimeStampAlgorithm(actions);

    Assert.assertEquals(eightAm, timeStampAlgorithm.getStartTimeStamp());
    Assert.assertEquals(nineAm, timeStampAlgorithm.getEndTimeStamp());
  }

  @Test
  public void executeInterruptTest() {
    actions.add(constructAction(ActionType.EXECUTE, eightAm));
    actions.add(constructAction(ActionType.INTERRUPT, nineAm));

    TimeStampAlgorithm timeStampAlgorithm = new TimeStampAlgorithm(actions);

    Assert.assertEquals(eightAm, timeStampAlgorithm.getStartTimeStamp());
    Assert.assertEquals(nineAm, timeStampAlgorithm.getEndTimeStamp());
  }

  @Test
  public void skipTest() {
    actions.add(constructAction(ActionType.SKIP, sevenAm));

    TimeStampAlgorithm timeStampAlgorithm = new TimeStampAlgorithm(actions);

    Assert.assertEquals(sevenAm, timeStampAlgorithm.getStartTimeStamp());
    Assert.assertEquals(sevenAm, timeStampAlgorithm.getEndTimeStamp());
  }

  @Test
  public void executeInterruptStopTest() {
    actions.add(constructAction(ActionType.EXECUTE, sevenAm));
    actions.add(constructAction(ActionType.INTERRUPT, eightAm));
    actions.add(constructAction(ActionType.STOP, nineAm));

    TimeStampAlgorithm timeStampAlgorithm = new TimeStampAlgorithm(actions);

    Assert.assertEquals(null, timeStampAlgorithm.getStartTimeStamp());
    Assert.assertEquals(null, timeStampAlgorithm.getEndTimeStamp());
  }

  @Test
  public void executeCompleteStopTest() {
    actions.add(constructAction(ActionType.EXECUTE, sevenAm));
    actions.add(constructAction(ActionType.COMPLETE, eightAm));
    actions.add(constructAction(ActionType.STOP, nineAm));

    TimeStampAlgorithm timeStampAlgorithm = new TimeStampAlgorithm(actions);

    Assert.assertEquals(null, timeStampAlgorithm.getStartTimeStamp());
    Assert.assertEquals(null, timeStampAlgorithm.getEndTimeStamp());
  }

  @Test
  public void executeInterruptExecuteInterruptTest() {
    actions.add(constructAction(ActionType.EXECUTE, sevenAm));
    actions.add(constructAction(ActionType.INTERRUPT, eightAm));
    actions.add(constructAction(ActionType.EXECUTE, nineAm));
    actions.add(constructAction(ActionType.INTERRUPT, tenAm));

    TimeStampAlgorithm timeStampAlgorithm = new TimeStampAlgorithm(actions);

    Assert.assertEquals(sevenAm, timeStampAlgorithm.getStartTimeStamp());
    Assert.assertEquals(tenAm, timeStampAlgorithm.getEndTimeStamp());
  }

  @Test
  public void executeInterruptExecuteCompleteTest() {
    actions.add(constructAction(ActionType.EXECUTE, sevenAm));
    actions.add(constructAction(ActionType.INTERRUPT, eightAm));
    actions.add(constructAction(ActionType.EXECUTE, nineAm));
    actions.add(constructAction(ActionType.COMPLETE, tenAm));

    TimeStampAlgorithm timeStampAlgorithm = new TimeStampAlgorithm(actions);

    Assert.assertEquals(sevenAm, timeStampAlgorithm.getStartTimeStamp());
    Assert.assertEquals(tenAm, timeStampAlgorithm.getEndTimeStamp());
  }

  @Test
  public void executeCompleteExecuteInterruptTest() {
    actions.add(constructAction(ActionType.EXECUTE, sevenAm));
    actions.add(constructAction(ActionType.COMPLETE, eightAm));
    actions.add(constructAction(ActionType.EXECUTE, nineAm));
    actions.add(constructAction(ActionType.INTERRUPT, tenAm));

    TimeStampAlgorithm timeStampAlgorithm = new TimeStampAlgorithm(actions);

    Assert.assertEquals(sevenAm, timeStampAlgorithm.getStartTimeStamp());
    Assert.assertEquals(eightAm, timeStampAlgorithm.getEndTimeStamp());
  }

  @Test
  public void executeCompleteExecuteCompleteTest() {
    actions.add(constructAction(ActionType.EXECUTE, sevenAm));
    actions.add(constructAction(ActionType.COMPLETE, eightAm));
    actions.add(constructAction(ActionType.EXECUTE, nineAm));
    actions.add(constructAction(ActionType.COMPLETE, tenAm));

    TimeStampAlgorithm timeStampAlgorithm = new TimeStampAlgorithm(actions);

    Assert.assertEquals(sevenAm, timeStampAlgorithm.getStartTimeStamp());
    Assert.assertEquals(eightAm, timeStampAlgorithm.getEndTimeStamp());
  }

  private static Date constructDate(int hour) {
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.HOUR, hour);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    calendar.set(Calendar.AM_PM, Calendar.AM);
    return calendar.getTime();
  }

  private Action constructAction(ActionType actionType, Date dateTime) {
    Action action = new Action(actionType);
    action.setDateTime(dateTime);
    return action;
  }

}
