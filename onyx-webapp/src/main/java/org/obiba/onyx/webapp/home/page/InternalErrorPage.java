/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.webapp.home.page;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Page;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.obiba.onyx.webapp.base.page.BasePage;

public class InternalErrorPage extends BasePage {

  public InternalErrorPage() {
    super();

    FeedbackPanel feedbackPanel = new FeedbackPanel("feedback");
    feedbackPanel.get("feedbackul").add(new AttributeModifier("class", true, new Model<String>("page-feedback ui-corner-all")));
    replace(feedbackPanel);

    add(newLink("link", new HomePage()));
  }

  protected Link<?> newLink(String id, final Page returnPage) {
    return new Link<Object>(id) {

      private static final long serialVersionUID = 1L;

      @Override
      public void onClick() {
        setResponsePage(returnPage);
      }

    };
  }
}
