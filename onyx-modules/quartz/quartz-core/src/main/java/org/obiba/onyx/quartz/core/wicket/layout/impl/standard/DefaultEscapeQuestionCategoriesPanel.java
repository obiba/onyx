/***********************************************************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 **********************************************************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout.impl.standard;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.GridView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.IValidator;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.wicket.layout.impl.AbstractOpenAnswerDefinitionPanel;
import org.obiba.onyx.quartz.core.wicket.layout.impl.AbstractQuestionCategoriesView;
import org.obiba.onyx.quartz.core.wicket.layout.impl.util.QuestionCategoryEscapeFilter;
import org.obiba.onyx.quartz.core.wicket.layout.impl.util.QuestionCategoryListToGridPermutator;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireStringResourceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Escape question categories are a valid way of not answering the normal set of categories (for instance "Prefer not
 * answer" regarding a multiple choice question). The escape categories are presented in a radio group.
 */
public class DefaultEscapeQuestionCategoriesPanel extends Panel {

  private static final long serialVersionUID = 5144933183339704600L;

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(DefaultEscapeQuestionCategoriesPanel.class);

  private RadioGroup radioGroup;

  /**
   * Question category, usefull for joined categories array questions.
   */
  @SuppressWarnings("unused")
  private IModel parentQuestionCategoryModel;

  /**
   * Constructor around given question.
   * @param id
   * @param questionModel
   */
  public DefaultEscapeQuestionCategoriesPanel(String id, IModel questionModel) {
    this(id, questionModel, null);
  }

  /**
   * Constructor, given a question and a parent question category context.
   * @param id
   * @param questionModel
   * @param parentQuestionCategoryModel
   */
  @SuppressWarnings("serial")
  public DefaultEscapeQuestionCategoriesPanel(String id, IModel<Question> questionModel, IModel parentQuestionCategoryModel) {
    super(id, questionModel);
    setOutputMarkupId(true);

    this.parentQuestionCategoryModel = parentQuestionCategoryModel;

    Question question = (Question) getDefaultModelObject();

    radioGroup = new RadioGroup("categories", new Model()) {
      @Override
      public void updateModel() {
        // ONYX-344: Do nothing -- QuestionCategoryRadioPanel sets the model to a read-only QuestionnaireModel
        // whenever a radio button is selected.
      }
    };
    radioGroup.setLabel(new QuestionnaireStringResourceModel(question, "label"));
    add(radioGroup);

    GridView<QuestionCategory> repeater = new AbstractQuestionCategoriesView("category", questionModel, new QuestionCategoryEscapeFilter(true), new QuestionCategoryListToGridPermutator(questionModel)) {

      @Override
      protected void populateItem(Item<QuestionCategory> item) {
        if(item.getModel() == null) {
          item.add(new EmptyPanel("input").setVisible(false));
        } else {
          item.add(new QuestionCategoryRadioPanel("input", item.getModel(), radioGroup));
        }
      }

    };
    radioGroup.add(repeater);
  }

  /**
   * Reset the model of the radio group (no selection).
   */
  public void setNoSelection() {
    radioGroup.setModel(new Model());

    // clear the open fields if any
    visitChildren(AbstractOpenAnswerDefinitionPanel.class, new Component.IVisitor() {

      public Object component(Component component) {
        AbstractOpenAnswerDefinitionPanel open = (AbstractOpenAnswerDefinitionPanel) component;
        open.resetField();
        return null;
      }

    });
  }

  /**
   * Add a validator to radio group.
   * @param validator
   */
  public void add(IValidator validator) {
    radioGroup.add(validator);
  }

}
