/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.webapp.base.page;

import java.util.Locale;

import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxIndicatorAware;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.resources.JavascriptResourceReference;
import org.apache.wicket.model.StringResourceModel;
import org.obiba.onyx.webapp.OnyxApplication;
import org.obiba.onyx.webapp.OnyxAuthenticatedSession;
import org.obiba.onyx.webapp.base.panel.HeaderPanel;
import org.obiba.onyx.webapp.base.panel.MenuBar;

public abstract class BasePage extends WebPage implements IAjaxIndicatorAware, IHeaderContributor {

  private FeedbackPanel feedbackPanel;

  public BasePage() {
    super();

    // Create feedback panel and add to page
    feedbackPanel = new FeedbackPanel("feedback");
    feedbackPanel.setOutputMarkupId(true);
    add(feedbackPanel);

    Panel headerPanel = new EmptyPanel("header");
    Panel menuBar = new EmptyPanel("menuBar");
    Session session = getSession();
    // Tests the session type for unit testing
    if(session instanceof OnyxAuthenticatedSession) {
      if(((OnyxAuthenticatedSession) getSession()).isSignedIn()) {
        headerPanel = new HeaderPanel("header");
        menuBar = new MenuBar("menuBar");
      }
    }
    add(headerPanel);
    add(menuBar);

    // Tests the application type for unit testing
    if(getApplication() instanceof OnyxApplication) {
      add(new Label("version", ((OnyxApplication) getApplication()).getVersion().toString()));
    } else {
      add(new EmptyPanel("version"));
    }

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

  public void renderHead(IHeaderResponse response) {
    response.renderJavascriptReference(new JavascriptResourceReference(BasePage.class, "jquery.js"));
    response.renderJavascriptReference(new JavascriptResourceReference(BasePage.class, "jquery.layout.js"));
    response.renderJavascriptReference(new JavascriptResourceReference(BasePage.class, "ui.js"));
    response.renderJavascriptReference(new JavascriptResourceReference(BasePage.class, "onyx.js"));
  }

}
