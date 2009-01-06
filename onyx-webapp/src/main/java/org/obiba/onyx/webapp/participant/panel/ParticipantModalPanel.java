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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.panel.Panel;

public class ParticipantModalPanel extends Panel {

  private static final long serialVersionUID = 1L;

  public static final String CONTENT_PANEL_ID = "content";

  public ParticipantModalPanel(String id, Panel contentPanel, final ModalWindow modalWindow) {
    super(id);

    add(contentPanel);

    AjaxLink link = new AjaxLink("closeAction") {

      private static final long serialVersionUID = 1L;

      @Override
      public void onClick(AjaxRequestTarget target) {
        modalWindow.close(target);
      }
    };

    add(link);
  }
}
