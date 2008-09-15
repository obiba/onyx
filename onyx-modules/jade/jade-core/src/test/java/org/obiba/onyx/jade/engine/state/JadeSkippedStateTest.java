package org.obiba.onyx.jade.engine.state;

import org.junit.Assert;
import org.junit.Test;
import org.obiba.onyx.engine.Action;

public class JadeSkippedStateTest {
  
  /**
   * Tests that the state's message includes the reason the state was entered
   * into, if a reason was provided.
   * 
   * Note: The reason should be at the end of the message, between parentheses.
   */
  @Test
  public void testMessageIncludesReasonIfThereIsOne() {
    JadeSkippedState skippedState = new JadeSkippedState();
    String reasonSkipped = "What caused the skipped state";
    
    Action reasonSkippedAction = new Action();
    reasonSkippedAction.setEventReason(reasonSkipped);
    
    skippedState.setReason(reasonSkippedAction);
    
    Assert.assertTrue(skippedState.getMessage().endsWith("("+reasonSkipped+")"));
  }
}
