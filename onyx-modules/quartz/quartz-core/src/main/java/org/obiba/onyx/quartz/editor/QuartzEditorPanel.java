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
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.editor.question.QuestionPropertiesPanel;
import org.obiba.onyx.quartz.editor.questionnaire.QuestionnaireListPanel;
import org.obiba.onyx.quartz.editor.questionnaire.QuestionnairePropertiesPanel;

public class QuartzEditorPanel extends Panel {

  private static final long serialVersionUID = 1L;

  private ModalWindow modalWindow;

  @SuppressWarnings({ "rawtypes", "serial" })
  public QuartzEditorPanel(String id) {
    super(id);

    modalWindow = new ModalWindow("modalWindow");
    modalWindow.setCssClassName("onyx");
    modalWindow.setInitialWidth(500);
    modalWindow.setInitialHeight(300);
    modalWindow.setResizable(true);
    modalWindow.setCloseButtonCallback(new ModalWindow.CloseButtonCallback() {
      @Override
      public boolean onCloseButtonClicked(AjaxRequestTarget target) {
        return true; // same as cancel
      }
    });

    add(modalWindow);

    add(new QuestionnaireListPanel("questionnaire-list"));

    add(new AjaxLink("questionnaireProps") {
      @Override
      public void onClick(AjaxRequestTarget target) {
        modalWindow.setTitle(new StringResourceModel("Questionnaire", this, null));
        modalWindow.setContent(new QuestionnairePropertiesPanel("content", new Model<Questionnaire>(new Questionnaire("defaultName", "1.0")), modalWindow));
        modalWindow.show(target);
      }
    });

    add(new AjaxLink("questionProps") {
      @Override
      public void onClick(AjaxRequestTarget target) {
        modalWindow.setTitle(new StringResourceModel("Question", this, null));
        modalWindow.setContent(new QuestionPropertiesPanel("content", new Model<Question>(new Question("defaultName")), modalWindow));
        modalWindow.show(target);
      }
    });

  }
}
