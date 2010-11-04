/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.question.condition;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.obiba.onyx.quartz.editor.question.condition.Condition.Type.NEW_VARIABLE;
import static org.obiba.onyx.quartz.editor.question.condition.Condition.Type.NONE;
import static org.obiba.onyx.quartz.editor.question.condition.Condition.Type.QUESTION_CATEGORY;
import static org.obiba.onyx.quartz.editor.question.condition.Condition.Type.VARIABLE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormChoiceComponentUpdatingBehavior;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.form.SimpleFormComponentLabel;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.validator.AbstractValidator;
import org.obiba.magma.Variable;
import org.obiba.onyx.core.data.VariableDataSource;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireBuilder;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireElementComparator;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireFinder;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.builder.QuestionBuilder;
import org.obiba.onyx.quartz.editor.question.condition.Condition.Type;
import org.obiba.onyx.quartz.editor.utils.QuestionnaireElementNameRenderer;

/**
 *
 */
@SuppressWarnings("serial")
public class ConditionPanel extends Panel {

  private final IModel<Questionnaire> questionnaireModel;

  private final IModel<Question> questionModel;

  public ConditionPanel(String id, final IModel<Question> questionModel, final IModel<Questionnaire> questionnaireModel) {
    super(id);
    this.questionModel = questionModel;
    this.questionnaireModel = questionnaireModel;

    add(CSSPackageResource.getHeaderContribution(ConditionPanel.class, "ConditionPanel.css"));

    Condition condition = new Condition();

    Question question = questionModel.getObject();
    final Questionnaire questionnaire = questionnaireModel.getObject();
    VariableDataSource variableDataSource = (VariableDataSource) question.getCondition();

    if(variableDataSource != null) {
      String tableName = variableDataSource.getTableName();
      String variableName = variableDataSource.getVariableName();

      Variable variable;
      try {
        variable = questionnaire.getVariable(variableName);
        condition.setType(Type.VARIABLE);
        condition.setVariable(variable);
      } catch(IllegalArgumentException e) {
        condition.setType(Type.QUESTION_CATEGORY);
        if(StringUtils.isNotEmpty(tableName) && !questionnaire.getName().equals(tableName)) {
          throw new RuntimeException("Cannot create use a Question[" + question + "] condition from another questionnaire: " + variableDataSource);
        }
        int index = variableName.lastIndexOf('.');
        condition.setQuestion(new QuestionnaireFinder(questionnaire).findQuestion(variableName.substring(0, index)));
        condition.setCategory(condition.getQuestion().getCategoriesByName().get(variableName.substring(index, variableName.length())));
      }
    }

    Model<Condition> model = new Model<Condition>(condition);
    setDefaultModel(model);

    final RadioGroup<Type> conditionType = new RadioGroup<Type>("conditionType", new PropertyModel<Type>(model, "type"));
    conditionType.setLabel(new ResourceModel("ConditionType")).setRequired(true);
    add(conditionType);

    final Radio<Type> noneType = new Radio<Type>("none", new Model<Type>(NONE));
    noneType.setLabel(new ResourceModel("NoCondition"));
    conditionType.add(noneType).add(new SimpleFormComponentLabel("noneLabel", noneType));

    final Radio<Type> questionType = new Radio<Type>("questionType", new Model<Type>(QUESTION_CATEGORY));
    questionType.setLabel(new ResourceModel("QuestionType"));
    conditionType.add(questionType).add(new SimpleFormComponentLabel("questionTypeLabel", questionType));

    final WebMarkupContainer questionTypeContainer = new WebMarkupContainer("questionTypeContainer");
    questionTypeContainer.setOutputMarkupId(true);
    conditionType.add(questionTypeContainer);

    final WebMarkupContainer questionConditionContainer = new WebMarkupContainer("questionConditionContainer");
    questionConditionContainer.setVisible(condition.getType() == QUESTION_CATEGORY);
    questionTypeContainer.add(questionConditionContainer);

    if(questionnaire.getQuestionnaireCache() == null) {
      QuestionnaireFinder.getInstance(questionnaire).buildQuestionnaireCache();
    }
    List<Question> questions = new ArrayList<Question>(questionnaire.getQuestionnaireCache().getQuestionCache().values());
    questions.remove(question);
    Collections.sort(questions, new QuestionnaireElementComparator());

    final DropDownChoice<Question> questionName = new DropDownChoice<Question>("question", new PropertyModel<Question>(model, "question"), questions, new QuestionnaireElementNameRenderer()) {
      @Override
      public boolean isRequired() {
        return conditionType.getModelObject() == QUESTION_CATEGORY;
      }
    };

    questionName.setLabel(new ResourceModel("Question"));
    questionConditionContainer.add(questionName).add(new SimpleFormComponentLabel("questionLabel", questionName));

    final List<Category> categories = new ArrayList<Category>();
    if(questionName.getModelObject() != null) {
      categories.addAll(questionName.getModelObject().getCategories());
    }

    final DropDownChoice<Category> categoryName = new DropDownChoice<Category>("category", new PropertyModel<Category>(model, "category"), categories, new QuestionnaireElementNameRenderer()) {
      @Override
      public boolean isRequired() {
        return conditionType.getModelObject() == QUESTION_CATEGORY;
      }
    };
    categoryName.setLabel(new ResourceModel("Category"));
    questionConditionContainer.add(categoryName).add(new SimpleFormComponentLabel("categoryLabel", categoryName));

    questionName.add(new OnChangeAjaxBehavior() {
      @Override
      protected void onUpdate(AjaxRequestTarget target) {
        if(questionName.getModelObject() != null) {
          categories.clear();
          categories.addAll(questionName.getModelObject().getCategories());
        }
        target.addComponent(categoryName);
      }
    });

    Radio<Type> variableType = new Radio<Type>("variableType", new Model<Type>(VARIABLE));
    variableType.setLabel(new ResourceModel("VariableType"));
    conditionType.add(variableType).add(new SimpleFormComponentLabel("variableTypeLabel", variableType));

    final WebMarkupContainer variableTypeContainer = new WebMarkupContainer("variableTypeContainer");
    variableTypeContainer.setOutputMarkupId(true);
    conditionType.add(variableTypeContainer);

    final WebMarkupContainer variableContainer = new WebMarkupContainer("variableContainer");
    variableContainer.setVisible(condition.getType() == VARIABLE);
    variableTypeContainer.add(variableContainer);

    List<Variable> variables = new ArrayList<Variable>(questionnaire.getVariables());
    Collections.sort(variables, new Comparator<Variable>() {
      @Override
      public int compare(Variable o1, Variable o2) {
        return o1.getName().compareTo(o2.getName());
      }
    });

    IChoiceRenderer<Variable> variableRenderer = new IChoiceRenderer<Variable>() {
      @Override
      public Object getDisplayValue(Variable object) {
        return object.getName();
      }

      @Override
      public String getIdValue(Variable object, int index) {
        return object.getName();
      }
    };
    final DropDownChoice<Variable> variable = new DropDownChoice<Variable>("variable", new PropertyModel<Variable>(model, "variable"), variables, variableRenderer) {
      @Override
      public boolean isRequired() {
        return conditionType.getModelObject() == VARIABLE;
      }
    };
    variable.setLabel(new ResourceModel("Variable"));
    variableContainer.add(variable).add(new SimpleFormComponentLabel("variableLabel", variable));

    Radio<Type> newVariableType = new Radio<Type>("newVariableType", new Model<Type>(NEW_VARIABLE));
    newVariableType.setLabel(new ResourceModel("NewVariable"));
    conditionType.add(newVariableType).add(new SimpleFormComponentLabel("newVariableTypeLabel", newVariableType));

    final WebMarkupContainer newVariableTypeContainer = new WebMarkupContainer("newVariableTypeContainer");
    newVariableTypeContainer.setOutputMarkupId(true);
    conditionType.add(newVariableTypeContainer);

    final WebMarkupContainer newVariableContainer = new WebMarkupContainer("newVariableContainer");
    newVariableContainer.setVisible(condition.getType() == NEW_VARIABLE);
    newVariableTypeContainer.add(newVariableContainer);

    final TextArea<String> script = new TextArea<String>("script", new PropertyModel<String>(model, "script")) {
      @Override
      public boolean isRequired() {
        return conditionType.getModelObject() == NEW_VARIABLE;
      }
    };
    script.setLabel(new ResourceModel("Script"));
    newVariableContainer.add(script).add(new SimpleFormComponentLabel("scriptLabel", script));

    final TextField<String> newVariableName = new TextField<String>("newVariableName", new PropertyModel<String>(model, "newVariableName"));
    newVariableName.setLabel(new ResourceModel("VariableName"));
    newVariableName.add(new AbstractValidator<String>() {
      @Override
      protected void onValidate(IValidatable<String> validatable) {
        if(checkIfVariableExists(validatable.getValue())) error(validatable, "VariableAlreadyExists");
      }
    });
    newVariableContainer.add(newVariableName).add(new SimpleFormComponentLabel("newVariableNameLabel", newVariableName));

    conditionType.add(new AjaxFormChoiceComponentUpdatingBehavior() {
      @Override
      protected void onUpdate(AjaxRequestTarget target) {
        Type type = conditionType.getModelObject();
        switch(type) {
        case NONE:
          questionName.setModelObject(null);
          categoryName.setModelObject(null);
          variable.setModelObject(null);
          newVariableName.setModelObject(null);
          script.setModelObject(null);
          break;
        case QUESTION_CATEGORY:
          variable.setModelObject(null);
          newVariableName.setModelObject(null);
          script.setModelObject(null);
          break;
        case VARIABLE:
          questionName.setModelObject(null);
          categoryName.setModelObject(null);
          newVariableName.setModelObject(null);
          script.setModelObject(null);
          break;
        case NEW_VARIABLE:
          questionName.setModelObject(null);
          categoryName.setModelObject(null);
          variable.setModelObject(null);
          break;

        }
        questionConditionContainer.setVisible(type == QUESTION_CATEGORY);
        variableContainer.setVisible(type == VARIABLE);
        newVariableContainer.setVisible(type == NEW_VARIABLE);
        target.addComponent(questionTypeContainer);
        target.addComponent(variableTypeContainer);
        target.addComponent(newVariableTypeContainer);
      }
    });

  }

  public void onSave(@SuppressWarnings("unused") AjaxRequestTarget target) {
    Condition condition = (Condition) getDefaultModelObject();
    if(condition.getType() == null) return;
    QuestionnaireBuilder questionnaireBuilder = QuestionnaireBuilder.getInstance(questionnaireModel.getObject());
    QuestionBuilder questionBuilder = QuestionBuilder.inQuestion(questionnaireBuilder, questionModel.getObject());
    switch(condition.getType()) {
    case NONE:
      break;
    case QUESTION_CATEGORY:
      questionBuilder.setCondition(condition.getQuestion().getName(), condition.getCategory().getName());
      break;
    case VARIABLE:
      questionBuilder.setVariableName(condition.getVariable().getName());
      break;
    case NEW_VARIABLE:
      String name = condition.getNewVariableName();
      questionBuilder.setQuestionnaireVariableCondition(isBlank(name) ? generateVariableName() : name, condition.getScript());
      break;
    }
  }

  private boolean checkIfVariableExists(String name) {
    for(Variable v : questionnaireModel.getObject().getVariables()) {
      if(StringUtils.equalsIgnoreCase(v.getName(), name)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Return "variable.questionName"<br>
   * If this name already exists, return "variable.questionName.1", "variable.questionName.2", etc.
   * 
   * @return
   */
  private String generateVariableName() {
    String name = "variable." + questionModel.getObject().getName();
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

}
