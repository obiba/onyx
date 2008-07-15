package org.obiba.onyx.jade.webapp.pages.home;

import org.apache.wicket.ajax.IAjaxIndicatorAware;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.obiba.onyx.jade.core.wicket.JadePanel;

public class HomePage extends WebPage implements IAjaxIndicatorAware {

  private FeedbackPanel feedbackPanel;
  
  public HomePage() {
    super();
    
    //Create feedback panel and add to page
    feedbackPanel = new FeedbackPanel("feedback");
    feedbackPanel.setOutputMarkupId(true);
    add(feedbackPanel);
    
    add(new Label("baseAjaxIndicator", "Processing..."));
    
    add(new JadePanel("jade"));
  }

  /**
   * @see org.apache.wicket.ajax.IAjaxIndicatorAware#getAjaxIndicatorMarkupId()
   */
  public String getAjaxIndicatorMarkupId()
  {
    return "base-ajax-indicator";
  }

}
