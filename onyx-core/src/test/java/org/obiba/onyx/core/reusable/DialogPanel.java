/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.reusable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.panel.Panel;

public class DialogPanel extends Panel {

  private static final long serialVersionUID = 1L;

  public DialogPanel(String id, boolean feedbackType) {
    super(id);

    final Dialog dialog = feedbackType ? new FeedbackWindow("dialog") : new Dialog("dialog");

    add(dialog);
    add(new AjaxLink("openDialog") {
      private static final long serialVersionUID = 1L;

      public void onClick(AjaxRequestTarget target) {
        dialog.show(target);
      }
    });
  }

  public DialogPanel(String id) {
    this(id, false);
  }

  public DialogPanel(String id, Dialog.Option option, String... labels) {
    super(id);

    final Dialog dialog = new Dialog("dialog");
    dialog.setOptions(option, labels);

    add(dialog);
    add(new AjaxLink("openDialog") {
      private static final long serialVersionUID = 1L;

      public void onClick(AjaxRequestTarget target) {
        dialog.show(target);
      }
    });
  }

  public DialogPanel(String id, Component component) {
    super(id);

    final Dialog dialog = new Dialog("dialog");
    dialog.setContent(component);

    add(dialog);
    add(new AjaxLink("openDialog") {
      private static final long serialVersionUID = 1L;

      public void onClick(AjaxRequestTarget target) {
        dialog.show(target);
      }
    });
  }

}
