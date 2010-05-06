/***********************************************************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 **********************************************************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout.impl.singledocument;

import java.util.List;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.validation.validator.MaximumValidator;
import org.apache.wicket.validation.validator.MinimumValidator;
import org.apache.wicket.validation.validator.RangeValidator;
import org.obiba.magma.type.BooleanType;
import org.obiba.onyx.magma.DataTypes;
import org.obiba.onyx.quartz.core.engine.questionnaire.QuestionnaireVariableNameResolver;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireUniqueVariableNameResolver;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireStringModifierModel;
import org.obiba.onyx.quartz.core.wicket.provider.AllChildQuestionsProvider;
import org.obiba.onyx.quartz.core.wicket.provider.AllOpenAnswerDefinitionsProvider;
import org.obiba.onyx.quartz.core.wicket.provider.AllQuestionCategoriesProvider;
import org.obiba.onyx.wicket.data.IDataValidator;

/**
 * Panel containing the question categories in a grid view of radios or checkboxes depending the questions multiple
 * flag.
 */
public class SingleDocumentQuestionContentPanel extends Panel {

  private static final long serialVersionUID = 1L;

  /**
   * Context in which answer are given (case of joined categories question array).
   */
  @SuppressWarnings("unused")
  private IModel<QuestionCategory> parentQuestionCategoryModel;

  /**
   * Constructor for a stand-alone question.
   * @param id
   * @param questionModel
   */
  public SingleDocumentQuestionContentPanel(String id, IModel<Question> questionModel) {
    super(id, questionModel);

    Question question = questionModel.getObject();

    if(question.getCondition() != null) {
      add(new ConditionFragment("conditionFragmentContent", questionModel));
    } else {
      add(new EmptyPanel("conditionFragmentContent"));
    }

    if(!question.hasSubQuestions()) {
      add(new CategoryFragment("categoryFragmentContent", questionModel));
    } else if(!question.hasCategories()) {
      add(new QuestionFragment("categoryFragmentContent", questionModel));
    } else if(question.isArrayOfSharedCategories()) {
      add(new SharedCategoryFragment("categoryFragmentContent", questionModel));
    } else {
      throw new UnsupportedOperationException("Joined categories array questions not supported yet");
    }
  }

  private class CategoryFragment extends Fragment {

    private static final long serialVersionUID = 1L;

    public CategoryFragment(String id, final IModel<Question> questionModel) {
      super(id, "categoryFragment", SingleDocumentQuestionContentPanel.this, questionModel);

      IDataProvider<QuestionCategory> questionCategoryProvider;
      questionCategoryProvider = new AllQuestionCategoriesProvider(questionModel);

      DataView<QuestionCategory> repeater = new DataView<QuestionCategory>("categories", questionCategoryProvider) {

        private static final long serialVersionUID = 1L;

        @Override
        protected void populateItem(Item<QuestionCategory> item) {
          item.add(new Label("code", new PropertyModel<String>(item.getModel(), "exportName")));
          item.add(new Label("name", new PropertyModel<String>(item.getModel(), "category.name")));
          item.add(new Label("escape", new Model<String>((item.getModelObject().getCategory().isEscape()) ? "x" : "")));
        }

      };
      add(repeater);

      DataView<QuestionCategory> questionCategories = new DataView<QuestionCategory>("questionCategories", questionCategoryProvider) {
        private static final long serialVersionUID = 1L;

        @Override
        protected void populateItem(final Item<QuestionCategory> item) {
          final QuestionCategory questionCategory = item.getModelObject();
          QuestionnaireVariableNameResolver variableNameResolver = new QuestionnaireUniqueVariableNameResolver();

          WebMarkupContainer category = new WebMarkupContainer("category");
          item.add(category);
          category.setVisible(questionModel.getObject().isMultiple());
          category.add(new Label("label", new Model<String>(variableNameResolver.variableName(questionModel.getObject(), questionCategory))));
          category.add(new Label("type", "[" + BooleanType.get().getName() + "]"));

          DataView<OpenAnswerDefinition> validations = new DataView<OpenAnswerDefinition>("opens", new AllOpenAnswerDefinitionsProvider(item.getModel())) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void populateItem(Item<OpenAnswerDefinition> itemOp) {
              OpenAnswerDefinition oad = itemOp.getModelObject();
              QuestionnaireVariableNameResolver variableNameResolver = new QuestionnaireUniqueVariableNameResolver();
              itemOp.add(new Label("label", new Model<String>(variableNameResolver.variableName(questionModel.getObject(), questionCategory, oad))));
              itemOp.add(new Label("type", "[" + DataTypes.valueTypeFor(oad.getDataType()).getName() + "]"));
              itemOp.add(new Label("validation", new Model<String>(getValidationString(oad.getDataValidators()))));
            }
          };
          item.add(validations);
        }
      };
      add(questionCategories);

      if(questionCategoryProvider.size() > 0) {
        this.setVisible(true);
      } else {
        this.setVisible(false);
      }
    }
  }

  private class ConditionFragment extends Fragment {

    private static final long serialVersionUID = 1L;

    public ConditionFragment(String id, IModel<Question> questionModel) {
      super(id, "conditionFragment", SingleDocumentQuestionContentPanel.this);
      add(new Label("condition", new QuestionnaireStringModifierModel(new PropertyModel<String>(questionModel, "condition"))));
    }
  }

  private class QuestionFragment extends Fragment {

    private static final long serialVersionUID = 1L;

    public QuestionFragment(String id, IModel<Question> questionModel) {
      super(id, "questionFragment", SingleDocumentQuestionContentPanel.this);

      DataView<Question> repeater = new DataView<Question>("subQuestions", new AllChildQuestionsProvider(questionModel)) {

        private static final long serialVersionUID = 1L;

        @Override
        protected void populateItem(Item<Question> item) {
          item.add(new SingleDocumentQuestionDetailsPanel("subQuestion", item.getModel()));
        }

      };
      add(repeater);
    }
  }

  private class SharedCategoryFragment extends Fragment {

    private static final long serialVersionUID = 1L;

    public SharedCategoryFragment(String id, IModel<Question> questionModel) {
      super(id, "sharedCategoryFragment", SingleDocumentQuestionContentPanel.this);

      DataView<Question> subQuestions = new DataView<Question>("subQuestions", new AllChildQuestionsProvider(questionModel)) {

        private static final long serialVersionUID = 1L;

        @Override
        protected void populateItem(Item<Question> item) {
          item.add(new SingleDocumentQuestionDetailsPanel("subQuestion", item.getModel()));
        }

      };
      add(subQuestions);
    }
  }

  private String getValidationString(List<IDataValidator> validators) {
    String validation = "";

    for(IDataValidator validator : validators) {
      validation = "- Validation: ";
      String classPath = validator.getValidator().getClass().getName();
      if(validator.getValidator() instanceof MinimumValidator) {
        validation += ">= " + ((MinimumValidator) validator.getValidator()).getMinimum();
      } else if(validator.getValidator() instanceof MaximumValidator) {
        validation += "<= " + ((MaximumValidator) validator.getValidator()).getMaximum();
      } else if(validator.getValidator() instanceof RangeValidator) {
        validation += ">= " + ((RangeValidator) validator.getValidator()).getMinimum() + "; <= " + ((RangeValidator) validator.getValidator()).getMaximum();
      } else {
        validation += validator.getValidator().toString();
      }
      validation += "\n";
    }

    return validation;
  }
}
