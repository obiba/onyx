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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;

public class DataManagementPanel extends Panel {

  // private static final Logger log = LoggerFactory.getLogger(DataManagementPanel.class);

  private static final long serialVersionUID = 1L;

  public DataManagementPanel(String id) {
    super(id);

    @SuppressWarnings("rawtypes")
    AjaxLink<?> purgeLink = new AjaxLink("purge") {
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

}
