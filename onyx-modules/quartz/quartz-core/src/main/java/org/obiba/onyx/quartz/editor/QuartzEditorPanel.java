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
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Section;
import org.obiba.onyx.quartz.editor.category.CategoryPropertiesPanel;
import org.obiba.onyx.quartz.editor.openAnswerDefinition.OpenAnswerDefinitionPropertiesPanel;
import org.obiba.onyx.quartz.editor.question.QuestionPropertiesPanel;
import org.obiba.onyx.quartz.editor.questionnaire.QuestionnaireListPanel;
import org.obiba.onyx.quartz.editor.questionnaire.QuestionnairePropertiesPanel;
import org.obiba.onyx.quartz.editor.section.SectionPropertiesPanel;

@SuppressWarnings("serial")
public class QuartzEditorPanel extends Panel {

  private ModalWindow modalWindow;

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

    add(new AjaxLink<Void>("questionnaireProps") {
      @Override
      public void onClick(AjaxRequestTarget target) {
        modalWindow.setTitle(new StringResourceModel("Questionnaire", this, null));
        modalWindow.setContent(new QuestionnairePropertiesPanel("content", new Model<Questionnaire>(new Questionnaire(null, "1.0")), modalWindow));
        modalWindow.show(target);
      }
    });

    add(new AjaxLink<Void>("sectionProps") {
      @Override
      public void onClick(AjaxRequestTarget target) {
        modalWindow.setTitle(new StringResourceModel("Section", this, null));
        modalWindow.setContent(new SectionPropertiesPanel("content", new Model<Section>(new Section(null)), modalWindow));
        modalWindow.show(target);
      }
    });

    add(new AjaxLink<Void>("questionProps") {
      @Override
      public void onClick(AjaxRequestTarget target) {
        modalWindow.setTitle(new StringResourceModel("Question", this, null));
        modalWindow.setContent(new QuestionPropertiesPanel("content", new Model<Question>(new Question(null)), modalWindow));
        modalWindow.show(target);
      }
    });

    add(new AjaxLink<Void>("categoryProps") {
      @Override
      public void onClick(AjaxRequestTarget target) {
        modalWindow.setTitle(new StringResourceModel("Category", this, null));
        modalWindow.setContent(new CategoryPropertiesPanel("content", new Model<Category>(new Category(null)), modalWindow));
        modalWindow.show(target);
      }
    });

    add(new AjaxLink<Void>("openAnswerDefinitionProps") {
      @Override
      public void onClick(AjaxRequestTarget target) {
        modalWindow.setTitle(new StringResourceModel("OpenAnswerDefinition", this, null));
        modalWindow.setContent(new OpenAnswerDefinitionPropertiesPanel("content", new Model<OpenAnswerDefinition>(new OpenAnswerDefinition(null, null)), modalWindow));
        modalWindow.show(target);
      }
    });

  }
}
