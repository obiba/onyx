/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.webapp.administration.panel;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.value.ValueMap;
import org.obiba.onyx.engine.variable.export.OnyxDataExport;
import org.obiba.onyx.wicket.reusable.ConfirmationDialog;
import org.obiba.onyx.wicket.reusable.ConfirmationDialog.OnYesCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataManagementPanel extends Panel {

  private static final Logger log = LoggerFactory.getLogger(DataManagementPanel.class);

  private static final long serialVersionUID = 1L;

  @SpringBean
  private OnyxDataExport onyxDataExport;

  public DataManagementPanel(String id) {
    super(id);

    AjaxLink exportLink = new AjaxLink("export") {
      private static final long serialVersionUID = 1L;

      @Override
      public void onClick(AjaxRequestTarget target) {
        ConfirmationDialog confirmationDialog = createExportDialog();
        MultiLineLabel label = new MultiLineLabel("content", new StringResourceModel("ConfirmExportMessage", new Model(new ValueMap("directory=" + onyxDataExport.getOutputRootDirectory().getAbsolutePath()))));
        label.add(new AttributeModifier("class", true, new Model("long-confirmation-dialog-content")));
        confirmationDialog.setContent(label);

        confirmationDialog.setYesButtonCallback(new OnYesCallback() {
          private static final long serialVersionUID = 1L;

          public void onYesButtonClicked(AjaxRequestTarget target) {
            try {
              onyxDataExport.exportInterviews();
            } catch(Exception e) {
              log.error("Error on data export.", e);
            }
          }
        });
        confirmationDialog.show(target);
      }

    };

    add(exportLink);

    AjaxLink purgeLink = new AjaxLink("purge") {
      private static final long serialVersionUID = 1L;

      @Override
      public void onClick(AjaxRequestTarget target) {
        PurgeDialog purgeDialog = new PurgeDialog("dialog");
        DataManagementPanel.this.replace(purgeDialog);
        purgeDialog.showConfirmation();
        purgeDialog.show(target);
        target.addComponent(purgeDialog);
      }

    };

    add(new EmptyPanel("dialog").setOutputMarkupId(true));
    add(purgeLink);
  }

  private ConfirmationDialog createExportDialog() {
    ConfirmationDialog confirmationDialog = new ConfirmationDialog("dialog");
    confirmationDialog.setTitle(new ResourceModel("ConfirmExport"));
    confirmationDialog.setHeightUnit("em");
    confirmationDialog.setInitialHeight(15);
    replace(confirmationDialog);
    return confirmationDialog;
  }
}
