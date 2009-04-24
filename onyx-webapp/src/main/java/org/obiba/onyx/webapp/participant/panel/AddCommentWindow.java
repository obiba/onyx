/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.webapp.participant.panel;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.obiba.onyx.core.reusable.Dialog;

public class AddCommentWindow extends Dialog {

  private static final int DEFAULT_INITIAL_HEIGHT = 420;

  private static final int DEFAULT_INITIAL_WIDTH = 375;

  private static final long serialVersionUID = 283560854902508942L;

  public AddCommentWindow(String id) {
    super(id);

    setInitialHeight(DEFAULT_INITIAL_HEIGHT);
    setInitialWidth(DEFAULT_INITIAL_WIDTH);
    setOptions(Dialog.Option.OK_CANCEL_OPTION, "Save");
    setTitle(new ResourceModel("AddComment"));
  }

  @Override
  public void setContent(Component component) {
    component.add(new AttributeModifier("class", true, new Model("obiba-content add-comment-panel-content")));
    super.setContent(component);
  }

}
