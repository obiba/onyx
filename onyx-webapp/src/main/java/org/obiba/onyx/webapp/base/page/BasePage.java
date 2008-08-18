package org.obiba.onyx.webapp.base.page;

import java.util.Locale;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxIndicatorAware;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.StringResourceModel;
import org.obiba.onyx.webapp.OnyxAuthenticatedSession;
import org.obiba.onyx.webapp.base.panel.HeaderPanel;
import org.obiba.onyx.webapp.base.panel.MenuBar;

public abstract class BasePage extends WebPage implements IAjaxIndicatorAware {

  private FeedbackPanel feedbackPanel;

  public BasePage() {
    super();

    // Create feedback panel and add to page
    feedbackPanel = new FeedbackPanel("feedback");
    feedbackPanel.setOutputMarkupId(true);
    add(feedbackPanel);

    if(((OnyxAuthenticatedSession) getSession()).isSignedIn()) {
      add(new HeaderPanel("header"));
    } else {
      add(new EmptyPanel("header"));
    }

    add(new MenuBar("menuBar"));

    add(new Label("baseAjaxIndicator", new StringResourceModel("Processing", this, null)));
  }

  public void setMenuBarVisible(boolean visible) {
    get("menuBar").setVisible(visible);
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
