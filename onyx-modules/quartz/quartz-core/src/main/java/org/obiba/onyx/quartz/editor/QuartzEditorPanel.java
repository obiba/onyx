/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.panel.Panel;
import org.obiba.onyx.quartz.editor.questionnaire.QuestionnaireListPanel;

@SuppressWarnings("serial")
public class QuartzEditorPanel extends Panel {

  protected ModalWindow modalWindow;

  public QuartzEditorPanel(String id) {
    super(id);

    modalWindow = new ModalWindow("modalWindow");
    modalWindow.setCssClassName("onyx");
    modalWindow.setInitialWidth(1000);
    modalWindow.setInitialHeight(600);
    modalWindow.setResizable(true);
    modalWindow.setCloseButtonCallback(new ModalWindow.CloseButtonCallback() {
      @Override
      public boolean onCloseButtonClicked(AjaxRequestTarget target) {
        return true; // same as cancel
      }
    });

    add(modalWindow);

    add(new QuestionnaireListPanel("questionnaire-list", modalWindow));

  }
}
