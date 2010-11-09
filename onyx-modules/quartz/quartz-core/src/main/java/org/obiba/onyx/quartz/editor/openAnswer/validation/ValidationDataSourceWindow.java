/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.openAnswer.validation;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.obiba.onyx.quartz.editor.openAnswer.validation.OpenAnswerValidator.Type.EXISTING_VARIABLE;
import static org.obiba.onyx.quartz.editor.openAnswer.validation.OpenAnswerValidator.Type.NEW_VARIABLE;
import static org.obiba.onyx.quartz.editor.openAnswer.validation.OpenAnswerValidator.Type.QUESTION_CATEGORY;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormChoiceComponentUpdatingBehavior;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.form.SimpleFormComponentLabel;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.validator.AbstractValidator;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.type.BinaryType;
import org.obiba.magma.type.BooleanType;
import org.obiba.magma.type.DateTimeType;
import org.obiba.magma.type.DateType;
import org.obiba.magma.type.DecimalType;
import org.obiba.magma.type.IntegerType;
import org.obiba.magma.type.LocaleType;
import org.obiba.magma.type.TextType;
import org.obiba.onyx.core.data.ComparingDataSource;
import org.obiba.onyx.core.data.VariableDataSource;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireBuilder;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireElementComparator;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireFinder;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.finder.QuestionnaireCache;
import org.obiba.onyx.quartz.editor.openAnswer.validation.OpenAnswerValidator.Type;
import org.obiba.onyx.quartz.editor.utils.QuestionnaireElementNameRenderer;
import org.obiba.onyx.quartz.editor.variable.VariableRenderer;
import org.obiba.onyx.quartz.editor.variable.VariableUtils;
import org.obiba.onyx.util.data.ComparisonOperator;
import org.obiba.onyx.util.data.DataType;
import org.obiba.onyx.wicket.reusable.FeedbackWindow;

/**
 *
 */
@SuppressWarnings("serial")
public abstract class ValidationDataSourceWindow extends Panel {

  private final FeedbackPanel feedbackPanel;

  private final FeedbackWindow feedbackWindow;

  private final Form<OpenAnswerValidator> form;

  private final IModel<OpenAnswerDefinition> openAnswerModel;

  private final IModel<Questionnaire> questionnaireModel;

  private final IModel<Question> questionModel;

  public ValidationDataSourceWindow(String id, IModel<ComparingDataSource> model, IModel<OpenAnswerDefinition> openAnswerModel, final IModel<Question> questionModel, final IModel<Questionnaire> questionnaireModel, final ModalWindow modalWindow) {
    super(id, model);
    this.openAnswerModel = openAnswerModel;
    this.questionModel = questionModel;
    this.questionnaireModel = questionnaireModel;

    add(CSSPackageResource.getHeaderContribution(ValidationDataSourceWindow.class, "ValidationDataSourceWindow.css"));

    final Questionnaire questionnaire = questionnaireModel.getObject();
    QuestionnaireFinder questionnaireFinder = QuestionnaireFinder.getInstance(questionnaire);

    final OpenAnswerValidator validator = new OpenAnswerValidator();
    if(model.getObject() != null) {
      ComparingDataSource comparingDataSource = model.getObject();
      validator.setOperator(comparingDataSource.getComparisonOperator());
      VariableDataSource variableDataSource = (VariableDataSource) comparingDataSource.getDataSourceRight();
      Variable variable = VariableUtils.findVariable(variableDataSource);
      if(variable != null) {
        Question question = VariableUtils.findQuestion(variable, questionnaireFinder);
        if(question != null) {
          validator.setType(QUESTION_CATEGORY);
          validator.setQuestion(question);
          validator.setCategory(VariableUtils.findCategory(variable, question));
        } else {
          validator.setType(EXISTING_VARIABLE);
          validator.setVariable(variable);
        }
      }
    }

    feedbackPanel = new FeedbackPanel("content");
    feedbackWindow = new FeedbackWindow("feedback");
    feedbackWindow.setOutputMarkupId(true);

    add(feedbackWindow);

    add(form = new Form<OpenAnswerValidator>("form", new Model<OpenAnswerValidator>(validator)));

    IChoiceRenderer<ComparisonOperator> operatorRenderer = new IChoiceRenderer<ComparisonOperator>() {
      @Override
      public String getIdValue(ComparisonOperator operator, int index) {
        return operator.name();
      }

      @Override
      public Object getDisplayValue(ComparisonOperator operator) {
        return new StringResourceModel("Operator." + operator, ValidationDataSourceWindow.this, null).getString();
      }
    };

    final DropDownChoice<ComparisonOperator> operator = new DropDownChoice<ComparisonOperator>("operator", new PropertyModel<ComparisonOperator>(form.getModel(), "operator"), Arrays.asList(ComparisonOperator.values()), operatorRenderer);
    form.add(operator.setLabel(new ResourceModel("Operator")).setRequired(true)).add(new SimpleFormComponentLabel("operatorLabel", operator));

    final RadioGroup<Type> validationType = new RadioGroup<Type>("validationType", new PropertyModel<Type>(form.getModel(), "type"));
    form.add(validationType.setLabel(new ResourceModel("Variable")).setRequired(true));

    final Radio<Type> questionType = new Radio<Type>("questionType", new Model<Type>(QUESTION_CATEGORY));
    validationType.add(questionType.setLabel(new ResourceModel("Question"))).add(new SimpleFormComponentLabel("questionTypeLabel", questionType));

    final WebMarkupContainer questionTypeContainer = new WebMarkupContainer("questionTypeContainer");
    validationType.add(questionTypeContainer.setOutputMarkupId(true));

    final WebMarkupContainer questionTypeVisibility = new WebMarkupContainer("questionTypeVisibility");
    questionTypeVisibility.setVisible(validator.getType() == QUESTION_CATEGORY);
    questionTypeContainer.add(questionTypeVisibility);

    if(questionnaire.getQuestionnaireCache() == null) {
      questionnaireFinder.buildQuestionnaireCache();
    }

    DataType dataType = openAnswerModel.getObject().getDataType();
    final DropDownChoice<Question> questionName = new DropDownChoice<Question>("question", new PropertyModel<Question>(form.getModel(), "question"), findQuestionsByDataType(dataType), new QuestionnaireElementNameRenderer()) {
      @Override
      public boolean isRequired() {
        return validator.getType() == QUESTION_CATEGORY;
      }
    };

    questionTypeVisibility.add(questionName.setLabel(new ResourceModel("Question"))).add(new SimpleFormComponentLabel("questionLabel", questionName));

    final DropDownChoice<Category> categoryName = new DropDownChoice<Category>("category", new PropertyModel<Category>(form.getModel(), "category"), findCategoryByDataType(questionName.getModelObject(), dataType), new QuestionnaireElementNameRenderer()) {
      @Override
      public boolean isRequired() {
        return validator.getType() == QUESTION_CATEGORY;
      }
    };
    questionTypeVisibility.add(categoryName.setLabel(new ResourceModel("Category"))).add(new SimpleFormComponentLabel("categoryLabel", categoryName));

    questionName.add(new OnChangeAjaxBehavior() {
      @Override
      protected void onUpdate(AjaxRequestTarget target) {
        target.addComponent(categoryName);
      }
    });

    final Radio<Type> existingVariable = new Radio<Type>("existingVariable", new Model<Type>(EXISTING_VARIABLE));
    validationType.add(existingVariable.setLabel(new ResourceModel("Variable.Existing"))).add(new SimpleFormComponentLabel("existingVariableLabel", existingVariable));

    final WebMarkupContainer existingVariableContainer = new WebMarkupContainer("existingVariableContainer");
    validationType.add(existingVariableContainer.setOutputMarkupId(true));

    final WebMarkupContainer existingVariableVisibility = new WebMarkupContainer("existingVariableVisibility");
    existingVariableVisibility.setVisible(validator.getType() == EXISTING_VARIABLE);
    existingVariableContainer.add(existingVariableVisibility);

    List<Variable> variables = new ArrayList<Variable>(questionnaire.getVariables());
    Collections.sort(variables, new Comparator<Variable>() {
      @Override
      public int compare(Variable o1, Variable o2) {
        return o1.getName().compareTo(o2.getName());
      }
    });

    final DropDownChoice<Variable> variable = new DropDownChoice<Variable>("variable", new PropertyModel<Variable>(form.getModel(), "variable"), variables, new VariableRenderer()) {
      @Override
      public boolean isRequired() {
        return validator.getType() == EXISTING_VARIABLE;
      }
    };
    existingVariableVisibility.add(variable.setLabel(new ResourceModel("Variable"))).add(new SimpleFormComponentLabel("variableLabel", variable));

    Radio<Type> newVariable = new Radio<Type>("newVariable", new Model<Type>(NEW_VARIABLE));
    validationType.add(newVariable.setLabel(new ResourceModel("Variable.New"))).add(new SimpleFormComponentLabel("newVariableLabel", newVariable));

    final WebMarkupContainer newVariableContainer = new WebMarkupContainer("newVariableContainer");
    validationType.add(newVariableContainer.setOutputMarkupId(true));

    final WebMarkupContainer newVariableVisibility = new WebMarkupContainer("newVariableVisibility");
    newVariableVisibility.setVisible(validator.getType() == NEW_VARIABLE);
    newVariableContainer.add(newVariableVisibility);

    final TextArea<String> script = new TextArea<String>("script", new PropertyModel<String>(form.getModel(), "script")) {
      @Override
      public boolean isRequired() {
        return validator.getType() == NEW_VARIABLE;
      }
    };
    newVariableVisibility.add(script.setLabel(new ResourceModel("Script"))).add(new SimpleFormComponentLabel("scriptLabel", script));

    List<ValueType> types = new ArrayList<ValueType>();
    types.add(IntegerType.get());
    types.add(DecimalType.get());
    types.add(BooleanType.get());
    types.add(DateType.get());
    types.add(DateTimeType.get());
    types.add(TextType.get());
    types.add(LocaleType.get());
    types.add(BinaryType.get());

    IChoiceRenderer<ValueType> valueTypeRenderer = new IChoiceRenderer<ValueType>() {
      @Override
      public String getIdValue(ValueType valueType, int index) {
        return valueType.getName();
      }

      @Override
      public Object getDisplayValue(ValueType valueType) {
        return WordUtils.capitalize(valueType.getName());
      }
    };

    final DropDownChoice<ValueType> valueType = new DropDownChoice<ValueType>("valueType", new PropertyModel<ValueType>(form.getModel(), "valueType"), types, valueTypeRenderer) {
      @Override
      public boolean isRequired() {
        return validator.getType() == NEW_VARIABLE;
      }
    };
    newVariableVisibility.add(valueType.setLabel(new ResourceModel("Type"))).add(new SimpleFormComponentLabel("valueTypeLabel", valueType));

    final TextField<String> newVariableName = new TextField<String>("newVariableName", new PropertyModel<String>(form.getModel(), "newVariableName"));
    newVariableName.setLabel(new ResourceModel("Variable.Name"));
    newVariableName.add(new AbstractValidator<String>() {
      @Override
      protected void onValidate(IValidatable<String> validatable) {
        if(checkIfVariableExists(validatable.getValue())) error(validatable, "VariableAlreadyExists");
      }
    });
    newVariableVisibility.add(newVariableName).add(new SimpleFormComponentLabel("newVariableNameLabel", newVariableName));

    validationType.add(new AjaxFormChoiceComponentUpdatingBehavior() {
      @Override
      protected void onUpdate(AjaxRequestTarget target) {
        Type type = validationType.getModelObject();
        switch(type) {
        case QUESTION_CATEGORY:
          variable.setModelObject(null);
          script.setModelObject(null);
          valueType.setModelObject(null);
          newVariableName.setModelObject(null);
          break;
        case EXISTING_VARIABLE:
          questionName.setModelObject(null);
          categoryName.setModelObject(null);
          script.setModelObject(null);
          valueType.setModelObject(null);
          newVariableName.setModelObject(null);
          break;
        case NEW_VARIABLE:
          questionName.setModelObject(null);
          categoryName.setModelObject(null);
          variable.setModelObject(null);
          break;

        }
        questionTypeVisibility.setVisible(type == QUESTION_CATEGORY);
        existingVariableVisibility.setVisible(type == EXISTING_VARIABLE);
        newVariableVisibility.setVisible(type == NEW_VARIABLE);
        target.addComponent(questionTypeContainer);
        target.addComponent(existingVariableContainer);
        target.addComponent(newVariableContainer);
      }
    });

    form.add(new AjaxButton("save", form) {
      @Override
      public void onSubmit(AjaxRequestTarget target, Form<?> form2) {
        String path = questionnaire.getName() + ":";
        if(validator.getType() == NEW_VARIABLE) {
          String name = validator.getNewVariableName();
          if(isBlank(name)) name = generateVariableName();
          QuestionnaireBuilder.getInstance(questionnaire).withVariable(name, validator.getValueType(), validator.getScript());
          path += name;
        } else {
          path += validator.getVariable().getName();
        }
        ComparingDataSource comparingDataSource = new ComparingDataSource(null, validator.getOperator(), new VariableDataSource(path));
        onSave(target, comparingDataSource);
        modalWindow.close(target);
      }

      @Override
      protected void onError(AjaxRequestTarget target, Form<?> form2) {
        feedbackWindow.setContent(feedbackPanel);
        feedbackWindow.show(target);
      }
    });

    form.add(new AjaxButton("cancel", form) {
      @Override
      public void onSubmit(AjaxRequestTarget target, Form<?> form2) {
        modalWindow.close(target);
      }
    }.setDefaultFormProcessing(false));

  }

  /**
   * @param target
   * @param comparingDataSource
   */
  public abstract void onSave(AjaxRequestTarget target, ComparingDataSource comparingDataSource);

  /**
   * Return "openAnswerName.validation"<br>
   * If this name already exists, return "openAnswerName.validation.1", "openAnswerName.validation.2", etc.
   * 
   * @return
   */
  private String generateVariableName() {
    String name = openAnswerModel.getObject().getName() + ".validation";
    int i = 0;
    boolean exists = true;
    String newName = null;
    do {
      newName = name;
      if(i > 0) {
        newName += "." + i;
      }
      i++;
      exists = checkIfVariableExists(newName);
    } while(exists);
    return newName;
  }

  private boolean checkIfVariableExists(String name) {
    for(Variable v : questionnaireModel.getObject().getVariables()) {
      if(StringUtils.equalsIgnoreCase(v.getName(), name)) {
        return true;
      }
    }
    return false;
  }

  private List<Question> findQuestionsByDataType(DataType dataType) {
    List<Question> questions = new ArrayList<Question>();
    QuestionnaireCache questionnaireCache = questionnaireModel.getObject().getQuestionnaireCache();
    for(Question question : questionnaireCache.getQuestionCache().values()) {
      if(!question.equals(questionModel.getObject())) {
        categoryLoop: for(Category category : question.getCategories()) {
          for(OpenAnswerDefinition openAnswer : category.getOpenAnswerDefinitionsByName().values()) {
            if(dataType == openAnswer.getDataType()) {
              questions.add(question);
              break categoryLoop;
            }
          }
        }
      }
    }
    Collections.sort(questions, new QuestionnaireElementComparator());
    return questions;
  }

  private List<Category> findCategoryByDataType(Question question, DataType dataType) {
    List<Category> categories = new ArrayList<Category>();
    if(question != null) {
      for(Category category : question.getCategories()) {
        for(OpenAnswerDefinition openAnswer : category.getOpenAnswerDefinitionsByName().values()) {
          if(dataType == openAnswer.getDataType()) {
            categories.add(category);
            break;
          }
        }
      }
    }
    return categories;
  }

}
