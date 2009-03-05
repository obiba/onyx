/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.core.wicket.instrument;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.panel.Panel;
import org.obiba.onyx.jade.core.wicket.run.InstrumentRunPanel;

public class ConclusionPanel extends Panel {

  private static final long serialVersionUID = 3008363510160516288L;

  @SuppressWarnings("serial")
  public ConclusionPanel(String id) {
    super(id);
    setOutputMarkupId(true);

    final ModalWindow modal;
    add(modal = new ModalWindow("modal"));
    modal.setCssClassName("onyx");
    modal.setCookieName("instrument-run-modal");

    add(new AjaxLink("show") {

      @Override
      public void onClick(AjaxRequestTarget target) {
        modal.setContent(new InstrumentRunPanel(modal.getContentId(), modal));
        modal.show(target);
      }

    });
  }
}
