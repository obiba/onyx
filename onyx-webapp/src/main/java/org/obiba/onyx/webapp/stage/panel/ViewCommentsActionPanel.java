package org.obiba.onyx.webapp.stage.panel;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.panel.Panel;

abstract public class ViewCommentsActionPanel extends Panel {

  private static final long serialVersionUID = 1L;

  public ViewCommentsActionPanel(String id) {
    super(id);
    add(new AjaxLink("viewComments") {

      private static final long serialVersionUID = 1L;

      public void onClick(AjaxRequestTarget target) {
        ViewCommentsActionPanel.this.onViewComments(target);
      }
    });   
  }

  public abstract void onViewComments(AjaxRequestTarget target);

}
