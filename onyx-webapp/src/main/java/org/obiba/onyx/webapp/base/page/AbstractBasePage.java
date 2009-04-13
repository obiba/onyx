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

import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.resources.JavascriptResourceReference;
import org.obiba.onyx.core.reusable.FeedbackWindow;
import org.obiba.onyx.webapp.OnyxApplication;

/**
 * 
 */
public class AbstractBasePage extends WebPage implements IHeaderContributor {

  private FeedbackWindow feedbackWindow;

  protected AbstractBasePage() {

    feedbackWindow = new FeedbackWindow("feedback");
    feedbackWindow.setOutputMarkupId(true);
    add(feedbackWindow);

    // Tests the application type for unit testing
    if(getApplication() instanceof OnyxApplication) {
      add(new Label("version", ((OnyxApplication) getApplication()).getVersion().toString()));
    } else {
      add(new EmptyPanel("version"));
    }
  }

  public void renderHead(IHeaderResponse response) {
    response.renderJavascriptReference(new JavascriptResourceReference(BasePage.class, "jquery.js"));
    response.renderJavascriptReference(new JavascriptResourceReference(BasePage.class, "jquery.layout.js"));
    response.renderJavascriptReference(new JavascriptResourceReference(BasePage.class, "ui.js"));
    response.renderJavascriptReference(new JavascriptResourceReference(BasePage.class, "onyx.js"));
  }

  protected FeedbackWindow getFeedbackWindow() {
    return feedbackWindow;
  }

}
