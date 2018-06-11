/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout.impl.simplified;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.StringResourceModel;
import org.obiba.onyx.wicket.link.AjaxImageLink;

/**
 * This is an alternative feedback panel intended to facilitate error reporting in the context of a touch-screen
 * environment. It is meant to be used in a ModalWindow which is displayed when errors are encountered.
 * 
 */
public class ModalFeedbackPanel extends FeedbackPanel {

  private static final long serialVersionUID = 1L;

  /**
   * @param id Component id.
   * @param feedbackWindow ModalWindow in which the panel will be displayed.
   */
  @SuppressWarnings("serial")
  public ModalFeedbackPanel(final ModalWindow feedbackWindow) {
    super("content");
    feedbackWindow.setTitle(new StringResourceModel("ModalWindowTitle", this, null));
    feedbackWindow.setResizable(false);
    this.setEscapeModelStrings(false);

    add(new AjaxImageLink("close", new StringResourceModel("CloseModalButton", this, null)) {

      @Override
      public void onClick(AjaxRequestTarget target) {
        feedbackWindow.close(target);
      }

    });

  }

}
