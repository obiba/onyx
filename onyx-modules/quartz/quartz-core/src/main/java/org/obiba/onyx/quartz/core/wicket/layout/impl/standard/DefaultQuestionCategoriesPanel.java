/***********************************************************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 **********************************************************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout.impl.standard;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.CheckBox;
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
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.quartz.core.wicket.layout.IQuestionCategorySelectionListener;
import org.obiba.onyx.quartz.core.wicket.layout.impl.AbstractOpenAnswerDefinitionPanel;
import org.obiba.onyx.quartz.core.wicket.layout.impl.AbstractQuestionCategoriesView;
import org.obiba.onyx.quartz.core.wicket.layout.impl.util.QuestionCategoryEscapeFilter;
import org.obiba.onyx.quartz.core.wicket.layout.impl.util.QuestionCategoryListToGridPermutator;
import org.obiba.onyx.quartz.core.wicket.layout.impl.validation.AnswerCountValidator;
import org.obiba.onyx.wicket.behavior.InvalidFormFieldBehavior;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Panel containing the question categories in a grid view of radios or checkboxes depending the questions multiple
 * flag.
 */
public class DefaultQuestionCategoriesPanel extends Panel implements IQuestionCategorySelectionListener {

  private static final long serialVersionUID = 5144933183339704600L;

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(DefaultQuestionCategoriesPanel.class);

  private CheckGroup checkGroup;

  @SpringBean
  private ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService;

  private DefaultEscapeQuestionCategoriesPanel escapeQuestionCategoriesPanel;

  /**
   * Context in which answer are given (case of joined categories question array).
   */
  @SuppressWarnings("unused")
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

    add(new InvalidFormFieldBehavior());
  }

  /**
   * Escape categories are presented in an additional radio grid view if any.
   * @return
   * @see DefaultEscapeQuestionCategoriesPanel
   */
  private boolean hasEscapeQuestionCategories() {
    return ((Question) getModelObject()).hasEscapeCategories();
  }

  private IModel getQuestionModel() {
    return getModel();
  }

  /**
   * Add a radio group, used by single choice question.
   * @param question
   */
  @SuppressWarnings("serial")
  private void addRadioGroup(Question question) {
    final RadioGroup radioGroup = new RadioGroup("categories", new Model()) {
      @Override
      public void updateModel() {
        // ONYX-344: Do nothing -- QuestionCategoryRadioPanel sets the model to a read-only QuestionnaireModel
        // whenever a radio button is selected.
      }
    };
    radioGroup.add(new AnswerCountValidator(getQuestionModel()));
    add(radioGroup);

    GridView repeater = new AbstractQuestionCategoriesView("category", getModel(), null, new QuestionCategoryListToGridPermutator(getModel())) {

      @Override
      protected void populateItem(Item item) {
        if(item.getModel() == null) {
          item.add(new EmptyPanel("input").setVisible(false));
        } else {
          item.add(new QuestionCategoryRadioPanel("input", item.getModel(), radioGroup));
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
    checkGroup = new CheckGroup("categories", new ArrayList<IModel>());
    checkGroup.add(new AnswerCountValidator(getQuestionModel()));
    add(checkGroup);

    GridView repeater = new AbstractQuestionCategoriesView("category", getModel(), new QuestionCategoryEscapeFilter(false), new QuestionCategoryListToGridPermutator(getModel())) {

      @Override
      protected void populateItem(Item item) {
        if(item.getModel() == null) {
          item.add(new EmptyPanel("input").setVisible(false));
        } else {
          item.add(new QuestionCategoryCheckBoxPanel("input", item.getModel(), checkGroup.getModel()));
        }
      }

    };
    checkGroup.add(repeater);

    if(hasEscapeQuestionCategories()) {
      add(escapeQuestionCategoriesPanel = new DefaultEscapeQuestionCategoriesPanel("escapeCategories", getQuestionModel()));
    } else {
      add(new EmptyPanel("escapeCategories").setVisible(false));
    }
  }

  @SuppressWarnings("unchecked")
  public void onQuestionCategorySelection(AjaxRequestTarget target, IModel questionModel, final IModel questionCategoryModel, boolean isSelected) {
    // repaint the panel
    target.addComponent(this);

    boolean isEscape = ((QuestionCategory) questionCategoryModel.getObject()).isEscape();

    if(checkGroup != null) {
      if(isEscape) {
        // case we are called by an escape category in a multiple choice context
        ((Collection<IModel>) checkGroup.getModelObject()).clear();
        // QUA-108 need to do this otherwise check box inputs are not cleared following a validation error
        checkGroup.visitChildren(CheckBox.class, new Component.IVisitor() {

          public Object component(Component component) {
            CheckBox cb = (CheckBox) component;
            cb.clearInput();
            return null;
          }

        });

        // clear the open answers also (but not the one of the selected category!)
        checkGroup.visitChildren(AbstractOpenAnswerDefinitionPanel.class, new Component.IVisitor() {

          public Object component(Component component) {
            AbstractOpenAnswerDefinitionPanel open = (AbstractOpenAnswerDefinitionPanel) component;
            if(!open.getQuestionCategoryModel().equals(questionCategoryModel)) {
              open.resetField();
            }
            return null;
          }

        });

      } else if(escapeQuestionCategoriesPanel != null) {
        // exclude escape questions if currently selected is not an escape one in a multiple choice context
        Question question = (Question) questionModel.getObject();
        for(CategoryAnswer answer : activeQuestionnaireAdministrationService.findAnswers(question)) {
          QuestionCategory questionCategory = question.findQuestionCategory(answer.getCategoryName());
          if(questionCategory.getCategory().isEscape()) {
            activeQuestionnaireAdministrationService.deleteAnswers(answer);
          }
        }
        escapeQuestionCategoriesPanel.setNoSelection();
      }
    }

    // forward event to parent
    IQuestionCategorySelectionListener parentListener = (IQuestionCategorySelectionListener) findParent(IQuestionCategorySelectionListener.class);
    if(parentListener != null) {
      parentListener.onQuestionCategorySelection(target, questionModel, questionCategoryModel, isSelected);
    }
  }

}
