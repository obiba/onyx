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
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.value.ValueMap;
import org.obiba.onyx.engine.variable.export.OnyxDataExport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class ConfirmExportPanel extends Panel {

  private static final long serialVersionUID = 1L;

  private static final Logger log = LoggerFactory.getLogger(ConfirmExportPanel.class);

  @SpringBean
  private OnyxDataExport onyxDataExport;

  public ConfirmExportPanel(String id) {
    super(id);

    add(new MultiLineLabel("confirm", new StringResourceModel("ConfirmExport", ConfirmExportPanel.this, new Model(new ValueMap("directory=" + onyxDataExport.getOutputRootDirectory().getPath())))));

    AjaxLink okLink = new AjaxLink("ok") {

      private static final long serialVersionUID = 1L;

      @Override
      public void onClick(AjaxRequestTarget target) {
        ModalWindow.closeCurrent(target);
        try {
          onyxDataExport.exportCompletedInterviews();
        } catch(Exception e) {
          log.error("Error on data export.", e);
        }
      }
    };
    add(okLink);

    AjaxLink cancelLink = new AjaxLink("cancel") {
      private static final long serialVersionUID = 1L;

      @Override
      public void onClick(AjaxRequestTarget target) {
        ModalWindow.closeCurrent(target);
      }
    };
    add(cancelLink);

  }
}
