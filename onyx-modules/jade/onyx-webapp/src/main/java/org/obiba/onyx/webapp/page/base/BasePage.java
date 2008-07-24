package org.obiba.onyx.webapp.page.base;

import java.util.Locale;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxIndicatorAware;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.StringResourceModel;
import org.obiba.onyx.webapp.panel.base.MenuBar;

public abstract class BasePage extends WebPage implements IAjaxIndicatorAware {

  private FeedbackPanel feedbackPanel;

  public BasePage() {
    super();

    // Create feedback panel and add to page
    feedbackPanel = new FeedbackPanel("feedback");
    feedbackPanel.setOutputMarkupId(true);
    add(feedbackPanel);

    add(new MenuBar("menuBar"));

    add(new Label("baseAjaxIndicator", new StringResourceModel("Processing", this, null)));
  }

  protected FeedbackPanel getFeedbackPanel() {
    return feedbackPanel;
  }

  /**
   * @see org.apache.wicket.ajax.IAjaxIndicatorAware#getAjaxIndicatorMarkupId()
   */
  public String getAjaxIndicatorMarkupId() {
    return "base-ajax-indicator";
  }

  public void onLanguageUpdate(Locale language, AjaxRequestTarget target) {
    setResponsePage(getPage());
  }

}
