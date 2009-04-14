/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.reusable;

import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

/**
 * 
 */
public class FeedbackWindow extends Dialog {

  private static final int DEFAULT_INITIAL_HEIGHT = 120;

  private static final int DEFAULT_INITIAL_WIDTH = 450;

  private static final long serialVersionUID = -9039412352683671244L;

  /**
   * @param id
   */
  public FeedbackWindow(String id) {
    super(id);
    setCssClassName("onyx");
    setInitialHeight(DEFAULT_INITIAL_HEIGHT);
    setInitialWidth(DEFAULT_INITIAL_WIDTH);
    setOptions(Dialog.Option.CLOSE_OPTION);

    get("content").add(new AttributeModifier("class", true, new Model("onyx-feedback")));
  }

  @Override
  public void setContent(Component component) {
    FeedbackPanel feedbackPanel = (FeedbackPanel) component;
    FeedbackMessage feedbackMessage = ((List<FeedbackMessage>) feedbackPanel.getFeedbackMessagesModel().getObject()).get(0);

    component.add(new AttributeModifier("class", true, new Model("feedback")));

    if(feedbackMessage != null) {
      String messageLevel = feedbackMessage.getLevelAsString();
      setType(Dialog.Type.valueOf(messageLevel));
      setTitle(new StringResourceModel("Dialog." + messageLevel.toLowerCase(), this, null));
    }
    super.setContent(component);
  }
}
