/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.engine.state;

import org.junit.Assert;
import org.junit.Test;
import org.obiba.onyx.engine.Action;
import org.springframework.context.MessageSourceResolvable;

public class JadeSkippedStateTest {

  /**
   * Tests that the state's message includes the name of the state followed by the reason the state was reached, between
   * parentheses.
   */
  @Test
  public void testMessageIncludesReasonIfThereIsOne() {
    String reasonSkipped = "DEFECTIVE_INSTRUMENT";

    JadeSkippedState skippedState = new JadeSkippedState();

    Action reasonSkippedAction = new Action();
    reasonSkippedAction.setEventReason(reasonSkipped);
    skippedState.setReason(reasonSkippedAction);

    MessageSourceResolvable message = skippedState.getMessage();
    Assert.assertNotNull(message);
    MessageSourceResolvable reason = skippedState.getReasonMessage();
    Assert.assertNotNull(reason);

  }
}
