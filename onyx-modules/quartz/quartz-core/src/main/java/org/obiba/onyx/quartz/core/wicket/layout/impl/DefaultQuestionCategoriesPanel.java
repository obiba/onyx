/***********************************************************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 **********************************************************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout.impl;

import java.util.ArrayList;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.CheckGroup;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireModel;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireStringResourceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultQuestionCategoriesPanel extends Panel {

  private static final long serialVersionUID = 5144933183339704600L;

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(DefaultQuestionCategoriesPanel.class);

  private AbstractOpenAnswerDefinitionPanel currentOpenField;

  public DefaultQuestionCategoriesPanel(String id, IModel questionModel) {
    super(id, questionModel);
    setOutputMarkupId(true);

    Question question = (Question) getModelObject();
    if(!question.isMultiple()) {
      addRadioGroup(question);
    } else {
      addCheckBoxGroup(question);
    }
  }

  /**
   * Add a radio group, used by single choice question.
   * @param question
   */
  @SuppressWarnings("serial")
  private void addRadioGroup(Question question) {
    final RadioGroup radioGroup = new RadioGroup("categories", new Model());
    radioGroup.setLabel(new QuestionnaireStringResourceModel(question, "label"));
    radioGroup.setRequired(!question.isBoilerPlate() && question.isRequired());
    add(radioGroup);

    RepeatingView repeater = new RepeatingView("category");
    radioGroup.add(repeater);

    for(QuestionCategory questionCategory : ((Question) getModelObject()).getQuestionCategories()) {
      WebMarkupContainer item = new WebMarkupContainer(repeater.newChildId());
      repeater.add(item);
      item.setModel(new QuestionnaireModel(questionCategory));

      item.add(new RadioQuestionCategoryPanel("input", item.getModel(), radioGroup) {

        @Override
        public void onOpenFieldSelection(AjaxRequestTarget target, IModel questionModel, IModel questionCategoryModel) {
          // ignore if multiple click in the same open field
          if(getOpenField().equals(currentOpenField)) return;

          // make sure a previously selected open field is not asked for
          if(currentOpenField != null) {
            currentOpenField.setRequired(false);
          }
          // make the open field active
          currentOpenField = getOpenField();

          // update all
          target.addComponent(DefaultQuestionCategoriesPanel.this);
        }

        @Override
        public void onRadioSelection(AjaxRequestTarget target, IModel questionModel, IModel questionCategoryModel) {
          // make inactive the previously selected open field
          if(currentOpenField != null) {
            currentOpenField.setData(null);
            currentOpenField.setRequired(false);
          }
          currentOpenField = getOpenField();

          // update all
          target.addComponent(DefaultQuestionCategoriesPanel.this);
        }

      });
    }
  }

  /**
   * Add a check box group, used by multiple choice question.
   * @param question
   */
  @SuppressWarnings("serial")
  private void addCheckBoxGroup(Question question) {
    CheckGroup checkGroup = new CheckGroup("categories", new ArrayList<IModel>());
    checkGroup.setLabel(new QuestionnaireStringResourceModel(question, "label"));
    // checkGroup.setRequired(!question.isBoilerPlate() && question.isRequired());
    checkGroup.add(new MultipleChoiceQuestionValidator(getModel()));
    add(checkGroup);

    RepeatingView repeater = new RepeatingView("category");
    checkGroup.add(repeater);

    for(final QuestionCategory questionCategory : ((Question) getModelObject()).getQuestionCategories()) {
      WebMarkupContainer item = new WebMarkupContainer(repeater.newChildId());
      repeater.add(item);
      item.setModel(new QuestionnaireModel(questionCategory));

      item.add(new CheckBoxQuestionCategoryPanel("input", item.getModel(), checkGroup) {
        @Override
        public void onOpenFieldSelection(AjaxRequestTarget target, IModel questionModel, IModel questionCategoryModel) {
          // update all
          target.addComponent(this);
        }
      });
    }
  }

}
