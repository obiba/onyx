/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.utils;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.tabs.AjaxTabbedPanel;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.obiba.onyx.wicket.reusable.FeedbackWindow;

/**
 * 
 */
public class AjaxSubmitTabbedPanel extends AjaxTabbedPanel {

  private static final long serialVersionUID = 1L;

  private final FeedbackPanel feedbackPanel;

  private final FeedbackWindow feedbackWindow;

  public AjaxSubmitTabbedPanel(String id, FeedbackPanel feedbackPanel, FeedbackWindow feedbackWindow, List<ITab> tabs) {
    super(id, tabs);
    this.feedbackPanel = feedbackPanel;
    this.feedbackWindow = feedbackWindow;
  }

  @Override
  protected WebMarkupContainer newLink(final String linkId, final int index) {
    AjaxIndicatingSubmitLink link = new AjaxIndicatingSubmitLink(linkId) {
      private static final long serialVersionUID = 1L;

      @Override
      protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
        setSelectedTab(index);
        if(target != null) {
          target.addComponent(AjaxSubmitTabbedPanel.this);
        }
        onAjaxUpdate(target);
      }

      @Override
      protected void onError(AjaxRequestTarget target, Form<?> form) {
        if(target != null) {
          feedbackWindow.setContent(feedbackPanel);
          feedbackWindow.show(target);
          target.addComponent(AjaxSubmitTabbedPanel.this);
        }
        onAjaxUpdate(target);
      }

    };
    return link;
  }

}
