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

import static org.obiba.onyx.quartz.editor.question.condition.Condition.Type.NONE;
import static org.obiba.onyx.quartz.editor.question.condition.Condition.Type.QUESTION_CATEGORY;
import static org.obiba.onyx.quartz.editor.question.condition.Condition.Type.VARIABLE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormChoiceComponentUpdatingBehavior;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.form.SimpleFormComponentLabel;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.magma.Variable;
import org.obiba.magma.type.BooleanType;
import org.obiba.onyx.core.data.VariableDataSource;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionType;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireBuilder;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireElementComparator;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireFinder;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.builder.QuestionBuilder;
import org.obiba.onyx.quartz.editor.behavior.syntaxHighlighter.SyntaxHighlighterBehavior;
import org.obiba.onyx.quartz.editor.behavior.tooltip.TooltipBehavior;
import org.obiba.onyx.quartz.editor.question.condition.Condition.Type;
import org.obiba.onyx.quartz.editor.utils.QuestionnaireElementNameRenderer;
import org.obiba.onyx.quartz.editor.variable.VariablePanel;
import org.obiba.onyx.quartz.editor.variable.VariableRenderer;
import org.obiba.onyx.quartz.editor.variable.VariableUtils;
import org.obiba.onyx.wicket.Images;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

/**
 *
 */
@SuppressWarnings("serial")
public class ConditionPanel extends Panel {

  // private final transient Logger logger = LoggerFactory.getLogger(getClass());

  @SpringBean
  private VariableUtils variableUtils;

  private final IModel<Questionnaire> questionnaireModel;

  private final IModel<Question> questionModel;

  private final ModalWindow variableWindow;

  private DropDownChoice<Question> questionName;

  public ConditionPanel(String id, final IModel<Question> questionModel, final IModel<Questionnaire> questionnaireModel) {
    super(id);
    this.questionModel = questionModel;
    this.questionnaireModel = questionnaireModel;

    add(CSSPackageResource.getHeaderContribution(ConditionPanel.class, "ConditionPanel.css"));

    variableWindow = new ModalWindow("variableWindow");
    variableWindow.setCssClassName("onyx");
    variableWindow.setInitialWidth(950);
    variableWindow.setInitialHeight(540);
    variableWindow.setResizable(true);
    variableWindow.setTitle(new ResourceModel("Variable"));
    add(variableWindow);

    add(new MultiLineLabel("explain", new ResourceModel("Explain")));

    Condition condition = new Condition();

    final Question question = questionModel.getObject();

    final Questionnaire questionnaire = questionnaireModel.getObject();
    QuestionnaireFinder questionnaireFinder = QuestionnaireFinder.getInstance(questionnaire);
    if(questionnaire.getQuestionnaireCache() == null) {
      questionnaireFinder.buildQuestionnaireCache();
    }

    if(question.getCondition() != null) {
      VariableDataSource variableDataSource = (VariableDataSource) question.getCondition();
      if(questionnaire.getName().equals(variableDataSource.getTableName())) {
        try {
          condition.setVariable(questionnaire.getVariable(variableDataSource.getVariableName()));
          condition.setType(Type.VARIABLE);
        } catch(IllegalArgumentException e) {
          // not found in this questionnaire
        }
      }
      if(condition.getType() == NONE) { // not found yet
        Variable variable = variableUtils.findVariable(variableDataSource);
        if(variable != null) {
          try {
            condition.setVariable(questionnaire.getVariable(variable.getName()));
            condition.setType(Type.VARIABLE);
          } catch(IllegalArgumentException e) {
            // not found
            Question questionCondition = VariableUtils.findQuestion(variable, questionnaireFinder);
            condition.setType(Type.QUESTION_CATEGORY);
            condition.setQuestion(questionCondition);
            condition.setCategory(VariableUtils.findCategory(variable, questionCondition));
          }
        }
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

    final List<Question> questions = new ArrayList<Question>(Collections2.filter(questionnaire.getQuestionnaireCache().getQuestionCache().values(), new Predicate<Question>() {
      @Override
      public boolean apply(Question q) {
        return !q.equals(question) && q.getType() != QuestionType.BOILER_PLATE;
      }
    }));
    Collections.sort(questions, new QuestionnaireElementComparator());

    questionName = new DropDownChoice<Question>("question", new PropertyModel<Question>(model, "question"), questions, new QuestionnaireElementNameRenderer()) {
      @Override
      public boolean isRequired() {
        return conditionType.getModelObject() == QUESTION_CATEGORY;
      }
    };

    questionName.setLabel(new ResourceModel("Question"));
    questionName.setNullValid(false);
    questionConditionContainer.add(questionName).add(new SimpleFormComponentLabel("questionLabel", questionName));

    final List<Category> categories = findCategories();
    final DropDownChoice<Category> categoryName = new DropDownChoice<Category>("category", new PropertyModel<Category>(model, "category"), categories, new QuestionnaireElementNameRenderer()) {
      @Override
      public boolean isRequired() {
        return conditionType.getModelObject() == QUESTION_CATEGORY;
      }
    };
    categoryName.setLabel(new ResourceModel("Category"));
    categoryName.setNullValid(false);
    questionConditionContainer.add(categoryName).add(new SimpleFormComponentLabel("categoryLabel", categoryName));

    questionName.add(new OnChangeAjaxBehavior() {
      @Override
      protected void onUpdate(AjaxRequestTarget target) {
        categories.clear();
        categories.addAll(findCategories());
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

    final List<Variable> variables = new ArrayList<Variable>(Collections2.filter(questionnaire.getVariables(), new Predicate<Variable>() {
      @Override
      public boolean apply(Variable v) {
        return v.getValueType().equals(BooleanType.get());
      }
    }));

    final DropDownChoice<Variable> variableDropDown = new DropDownChoice<Variable>("variable", new PropertyModel<Variable>(model, "variable"), variables, new VariableRenderer()) {
      @Override
      public boolean isRequired() {
        return conditionType.getModelObject() == VARIABLE;
      }
    };
    variableDropDown.setLabel(new ResourceModel("Variable")).setOutputMarkupId(true);
    variableContainer.add(variableDropDown);

    final WebMarkupContainer previewVariableVisibility = new WebMarkupContainer("previewVariableVisibility");
    variableContainer.add(previewVariableVisibility.setOutputMarkupId(true));

    final Image previewVariable = new Image("previewVariable", Images.ZOOM);
    previewVariable.add(new AttributeModifier("title", true, new ResourceModel("Preview")));
    previewVariable.setVisible(variableDropDown.getModelObject() != null);
    previewVariableVisibility.add(previewVariable);

    final Label previewScript = new Label("previewScript", variableDropDown.getModelObject() == null ? "" : variableDropDown.getModelObject().getAttributeStringValue("script"));
    previewScript.add(new SyntaxHighlighterBehavior());
    previewScript.add(new AttributeAppender("style", true, new Model<String>("display: none;"), " "));
    previewVariableVisibility.add(previewScript);

    final Map<String, Object> tooltipCfg = new HashMap<String, Object>();
    tooltipCfg.put("delay", 100);
    tooltipCfg.put("showURL", false);
    tooltipCfg.put("top", -30);
    tooltipCfg.put("bodyHandler", "function() { return $(\"#" + previewScript.getMarkupId(true) + "\").html(); }");
    previewVariable.add(new TooltipBehavior(null, tooltipCfg));

    variableContainer.add(new AjaxLink<Void>("newVariable") {
      @Override
      public void onClick(AjaxRequestTarget target) {

        @SuppressWarnings({ "rawtypes", "unchecked" })
        VariablePanel variablePanel = new VariablePanel("content", new Model(null), questionnaireModel, BooleanType.get()) {
          @Override
          public void onSave(@SuppressWarnings("hiding") AjaxRequestTarget target, Variable createdVariable) {
            variables.add(createdVariable);
            questionnaire.addVariable(createdVariable);
            variableDropDown.setModelObject(createdVariable);
            previewVariable.setVisible(true);
            previewScript.setDefaultModelObject(createdVariable.getAttributeStringValue("script"));
            variableWindow.close(target);
            target.addComponent(variableDropDown);
            target.addComponent(previewVariableVisibility);
          }

          @Override
          public void onCancel(@SuppressWarnings("hiding") AjaxRequestTarget target) {
            variableWindow.close(target);
          }
        };

        variableWindow.setContent(variablePanel);
        variableWindow.show(target);
      }
    }.add(new Image("newVariableImg", Images.ADD)).add(new AttributeModifier("title", true, new ResourceModel("NewVariable"))));

    variableDropDown.add(new OnChangeAjaxBehavior() {
      @Override
      protected void onUpdate(AjaxRequestTarget target) {
        Variable variable = variableDropDown.getModelObject();
        previewVariable.setVisible(variable != null);
        previewScript.setDefaultModelObject(variable == null ? null : variable.getAttributeStringValue("script"));
        target.addComponent(previewVariableVisibility);
      }
    });

    conditionType.add(new AjaxFormChoiceComponentUpdatingBehavior() {
      @Override
      protected void onUpdate(AjaxRequestTarget target) {
        Type type = conditionType.getModelObject();
        switch(type) {
        case NONE:
          questionName.setModelObject(null);
          categoryName.setModelObject(null);
          variableDropDown.setModelObject(null);
          break;
        case QUESTION_CATEGORY:
          variableDropDown.setModelObject(null);
          break;
        case VARIABLE:
          questionName.setModelObject(null);
          categoryName.setModelObject(null);
          break;

        }
        questionConditionContainer.setVisible(type == QUESTION_CATEGORY);
        variableContainer.setVisible(type == VARIABLE);
        target.addComponent(questionTypeContainer);
        target.addComponent(variableTypeContainer);
      }
    });

  }

  private List<Category> findCategories() {
    final List<Category> categories = new ArrayList<Category>();
    final Question selectedQuestion = questionName.getModelObject();
    if(selectedQuestion != null) {
      if(selectedQuestion.getParentQuestion() == null) {
        categories.addAll(selectedQuestion.getCategories());
      } else {
        // get parent categories for array
        categories.addAll(selectedQuestion.getParentQuestion().getCategories());
      }
    }
    return categories;
  }

  public void onSave(@SuppressWarnings("unused") AjaxRequestTarget target) {
    Condition condition = (Condition) getDefaultModelObject();
    if(condition.getType() == null) return;
    Questionnaire questionnaire = questionnaireModel.getObject();
    QuestionnaireBuilder questionnaireBuilder = QuestionnaireBuilder.getInstance(questionnaire);
    Question question = questionModel.getObject();
    QuestionBuilder questionBuilder = QuestionBuilder.inQuestion(questionnaireBuilder, question);
    switch(condition.getType()) {
    case NONE:
      question.setCondition(null);
      break;
    case QUESTION_CATEGORY:
      questionBuilder.setCondition(condition.getQuestion().getName(), condition.getCategory().getName());
      break;
    case VARIABLE:
      questionBuilder.setVariableCondition(questionnaire.getName() + ":" + condition.getVariable().getName());
      break;
    }
  }

}
