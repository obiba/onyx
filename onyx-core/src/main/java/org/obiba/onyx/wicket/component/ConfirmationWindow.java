/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.wicket.component;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.IBehavior;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

public class ConfirmationWindow extends ModalWindow {
  //
  // Constants
  //

  private static final long serialVersionUID = 1L;

  private static final int DEFAULT_INITIAL_HEIGHT = 120;

  private static final int DEFAULT_INITIAL_WIDTH = 400;

  //
  // Instance Variables
  //

  private ConfirmationFragment confirmationFragment;

  //
  // Constructors
  //

  public ConfirmationWindow(String id, IModel messageModel, int height, int width) {
    super(id);

    setTitle((new StringResourceModel("Confirm", this, null)));
    setContent(confirmationFragment = new ConfirmationFragment(getContentId(), messageModel));
    setInitialHeight(height);
    setInitialWidth(width);
  }

  public ConfirmationWindow(String id, IModel messageModel) {
    this(id, messageModel, DEFAULT_INITIAL_HEIGHT, DEFAULT_INITIAL_WIDTH);
  }

  //
  // Methods
  //

  /**
   * Adds a behavior to the OK button.
   * 
   * @param behavior
   */
  public void addOkBehavior(IBehavior behavior) {
    confirmationFragment.okLink.add(behavior);
  }

  //
  // Inner Classes
  //

  class ConfirmationFragment extends Fragment {
    //
    // Constants
    //

    private static final long serialVersionUID = 1L;

    //
    // Instance Variables
    //

    private Image icon;

    private MultiLineLabel messageLabel;

    private AjaxLink okLink;

    private AjaxLink cancelLink;

    //
    // Constructors
    //

    public ConfirmationFragment(String id, IModel messageModel) {
      super(id, "confirmationFragment", ConfirmationWindow.this);

      icon = new Image("confirmIcon");
      add(icon);

      messageLabel = new MultiLineLabel("confirmMessage", messageModel);
      add(messageLabel);

      okLink = createOkLink();
      add(okLink);

      cancelLink = createCancelLink();
      add(cancelLink);
    }

    //
    // Methods
    //

    private AjaxLink createOkLink() {
      return new AjaxLink("ok") {
        private static final long serialVersionUID = 1L;

        public void onClick(AjaxRequestTarget target) {
          closeWindow(target);
        }
      };
    }

    private AjaxLink createCancelLink() {
      return new AjaxLink("cancel") {
        private static final long serialVersionUID = 1L;

        public void onClick(AjaxRequestTarget target) {
          closeWindow(target);
        }
      };
    }

    private void closeWindow(AjaxRequestTarget target) {
      MarkupContainer parent = ConfirmationFragment.this.getParent();
      ((ModalWindow) parent).close(target);
    }
  }
}
