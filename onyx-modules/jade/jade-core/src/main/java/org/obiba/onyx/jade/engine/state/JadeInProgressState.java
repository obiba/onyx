/**
 * 
 */
package org.obiba.onyx.jade.engine.state;

import org.apache.wicket.Component;
import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ActionDefinition;
import org.obiba.onyx.engine.ActionType;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.engine.state.AbstractStageState;
import org.obiba.onyx.engine.state.TransitionEvent;
import org.obiba.onyx.jade.core.wicket.panel.JadePanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JadeInProgressState extends AbstractStageState {

  private static final Logger log = LoggerFactory.getLogger(JadeInProgressState.class);

  private Stage stage;

  public JadeInProgressState(Stage stage) {
    this.stage = stage;
    addAction(new ActionDefinition(ActionType.STOP, "Cancel"));
    addSystemAction(new ActionDefinition(ActionType.COMPLETE, "Complete"));
    addAction(new ActionDefinition(ActionType.COMMENT, "Comment"));
  }

  public Component getWidget(String id) {
    return new JadePanel(id, stage);
  }

  @Override
  public void stop(Action action) {
    log.info("Jade Stage {} is stopping", super.getStage().getName());
    // Invalidate current instrument run
    castEvent(TransitionEvent.CANCEL);
  }

  @Override
  public void complete(Action action) {
    log.info("Jade Stage {} is completing", super.getStage().getName());
    // Finish current instrument run
    castEvent(TransitionEvent.COMPLETE);
  }

  @Override
  public boolean isInteractive() {
    return true;
  }
  
  @Override
  public String getMessage() {
    return "In Progress";
  }
}