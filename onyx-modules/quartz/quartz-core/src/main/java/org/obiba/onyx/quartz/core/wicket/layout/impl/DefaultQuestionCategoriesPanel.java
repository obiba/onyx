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
import java.util.Collection;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.CheckGroup;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.GridView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.quartz.core.domain.answer.CategoryAnswer;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.quartz.core.wicket.layout.impl.util.QuestionCategoriesToMatrixPermutator;
import org.obiba.onyx.quartz.core.wicket.layout.impl.util.QuestionCategoryEscapeFilter;
import org.obiba.onyx.quartz.core.wicket.layout.impl.validation.AnswerCountValidator;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireStringResourceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultQuestionCategoriesPanel extends Panel {

  private static final long serialVersionUID = 5144933183339704600L;

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(DefaultQuestionCategoriesPanel.class);

  @SpringBean
  private ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService;

  private DefaultEscapeQuestionCategoriesPanel escapeQuestionCategoriesPanel;

  private IModel parentQuestionCategoryModel;

  /**
   * Constructor for a stand-alone question.
   * @param id
   * @param questionModel
   */
  public DefaultQuestionCategoriesPanel(String id, IModel questionModel) {
    this(id, questionModel, null);
  }

  /**
   * Constructor for a joined categories question.
   * @param id
   * @param questionModel
   * @param parentQuestionCategoryModel
   */
  public DefaultQuestionCategoriesPanel(String id, IModel questionModel, IModel parentQuestionCategoryModel) {
    super(id, questionModel);
    setOutputMarkupId(true);

    this.parentQuestionCategoryModel = parentQuestionCategoryModel;

    Question question = (Question) getModelObject();
    if(!question.isMultiple()) {
      addRadioGroup(question);
    } else {
      addCheckBoxGroup(question);
    }
  }

  private boolean hasEscapeQuestionCategories() {
    for(Category category : ((Question) getModelObject()).getCategories()) {
      if(category.isEscape()) return true;
    }
    return false;
  }

  private IModel getQuestionModel() {
    return getModel();
  }

  private Question getQuestion() {
    return (Question) getModelObject();
  }

  private QuestionCategory getParentQuestionCategory() {
    if(parentQuestionCategoryModel == null) return null;

    return (QuestionCategory) parentQuestionCategoryModel.getObject();
  }

  /**
   * Add a radio group, used by single choice question.
   * @param question
   */
  @SuppressWarnings("serial")
  private void addRadioGroup(Question question) {
    final RadioGroup radioGroup = new RadioGroup("categories", new Model());
    radioGroup.setLabel(new QuestionnaireStringResourceModel(question, "label"));
    radioGroup.add(new AnswerCountValidator(getQuestionModel()));
    add(radioGroup);

    GridView repeater = new AbstractQuestionCategoriesView("category", getModel(), null, new QuestionCategoriesToMatrixPermutator(getModel())) {

      @Override
      protected void populateItem(Item item) {
        if(item.getModel() == null) {
          item.add(new EmptyPanel("input").setVisible(false));
        } else {
          item.add(new QuestionCategoryRadioPanel("input", item.getModel(), radioGroup) {

            @Override
            public void onSelection(AjaxRequestTarget target, IModel questionModel, IModel questionCategoryModel) {
              // update all
              target.addComponent(DefaultQuestionCategoriesPanel.this);
              fireQuestionAnswerChanged(target, questionModel, questionCategoryModel);
            }

            @Override
            public void onOpenFieldSubmit(AjaxRequestTarget target, IModel questionModel, IModel questionCategoryModel) {
              fireQuestionAnswerChanged(target, questionModel, questionCategoryModel);
            }

          });
        }
      }

    };
    radioGroup.add(repeater);

    add(new EmptyPanel("escapeCategories").setVisible(false));
  }

  /**
   * Add a check box group, used by multiple choice question.
   * @param question
   */
  @SuppressWarnings("serial")
  private void addCheckBoxGroup(Question question) {
    final CheckGroup checkGroup = new CheckGroup("categories", new ArrayList<IModel>());
    checkGroup.setLabel(new QuestionnaireStringResourceModel(question, "label"));
    checkGroup.add(new AnswerCountValidator(getQuestionModel()));
    add(checkGroup);

    GridView repeater = new AbstractQuestionCategoriesView("category", getModel(), new QuestionCategoryEscapeFilter(false), new QuestionCategoriesToMatrixPermutator(getModel())) {

      @Override
      protected void populateItem(Item item) {
        if(item.getModel() == null) {
          item.add(new EmptyPanel("input").setVisible(false));
        } else {
          item.add(new QuestionCategoryCheckBoxPanel("input", item.getModel(), checkGroup) {

            @Override
            public void onSelection(AjaxRequestTarget target, IModel questionModel, IModel questionCategoryModel) {
              if(escapeQuestionCategoriesPanel != null) {
                Question question = getQuestion();
                for(CategoryAnswer answer : activeQuestionnaireAdministrationService.findAnswers(question)) {
                  QuestionCategory questionCategory = question.findQuestionCategory(answer.getCategoryName());
                  if(questionCategory.getCategory().isEscape()) {
                    activeQuestionnaireAdministrationService.deleteAnswer(question, questionCategory);
                  }
                }
                escapeQuestionCategoriesPanel.setNoSelection();
              }
              target.addComponent(DefaultQuestionCategoriesPanel.this);
              fireQuestionAnswerChanged(target, questionModel, questionCategoryModel);
            }

            @Override
            public void onOpenFieldSubmit(AjaxRequestTarget target, IModel questionModel, IModel questionCategoryModel) {
              fireQuestionAnswerChanged(target, questionModel, questionCategoryModel);
            }

          });

        }
      }

    };
    checkGroup.add(repeater);

    if(hasEscapeQuestionCategories()) {
      add(escapeQuestionCategoriesPanel = new DefaultEscapeQuestionCategoriesPanel("escapeCategories", getQuestionModel()) {

        @SuppressWarnings("unchecked")
        @Override
        public void onSelection(AjaxRequestTarget target, IModel questionModel, IModel questionCategoryModel) {
          ((Collection<IModel>) checkGroup.getModelObject()).clear();
          target.addComponent(DefaultQuestionCategoriesPanel.this);
        }

      });
    } else {
      add(new EmptyPanel("escapeCategories").setVisible(false));
    }
  }
}
