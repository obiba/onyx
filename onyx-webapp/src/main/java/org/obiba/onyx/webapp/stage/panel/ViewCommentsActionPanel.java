/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.webapp.stage.panel;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.image.ContextImage;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

abstract public class ViewCommentsActionPanel extends Panel {

  private static final long serialVersionUID = 1L;

  public ViewCommentsActionPanel(String id) {
    super(id);
    AjaxLink viewLogs = new AjaxLink("viewLogs") {

      private static final long serialVersionUID = 1L;

      public void onClick(AjaxRequestTarget target) {
        ViewCommentsActionPanel.this.onViewLogs(target);
      }
    };
    viewLogs.add(new ContextImage("viewLogsImg", new Model("icons/loupe_button.png")));
    add(viewLogs);

    AjaxLink viewComments = new AjaxLink("viewComments") {

      private static final long serialVersionUID = 1L;

      public void onClick(AjaxRequestTarget target) {
        ViewCommentsActionPanel.this.onViewComments(target);
      }
    };
    viewComments.add(new ContextImage("viewCommentsImg", new Model("icons/note.png")));
    add(viewComments);

  }

  public abstract void onViewComments(AjaxRequestTarget target);

  public abstract void onViewLogs(AjaxRequestTarget target);

}
