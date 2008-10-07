package org.obiba.onyx.quartz.core.wicket;

import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.wicket.IEngineComponentAware;
import org.obiba.onyx.wicket.action.ActionWindow;

public class QuartzPanel extends Panel implements IEngineComponentAware {

  private static final long serialVersionUID = 0L;

  @SuppressWarnings("serial")
  public QuartzPanel(String id, Stage stage) {
    super(id);
  }
  
  public void setActionWindwon(ActionWindow window) {
    // TODO Auto-generated method stub

  }

  public void setFeedbackPanel(FeedbackPanel feedbackPanel) {
    // TODO Auto-generated method stub

  }

}
