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

import static org.obiba.onyx.quartz.editor.openAnswer.validation.OpenAnswerValidator.Type.QUESTION_CATEGORY;
import static org.obiba.onyx.quartz.editor.openAnswer.validation.OpenAnswerValidator.Type.VARIABLE;

import java.util.ArrayList;
import java.util.Arrays;
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
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.form.SimpleFormComponentLabel;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.onyx.core.data.ComparingDataSource;
import org.obiba.onyx.core.data.VariableDataSource;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionType;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireElementComparator;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireFinder;
import org.obiba.onyx.quartz.editor.behavior.syntaxHighlighter.SyntaxHighlighterBehavior;
import org.obiba.onyx.quartz.editor.behavior.tooltip.TooltipBehavior;
import org.obiba.onyx.quartz.editor.openAnswer.validation.OpenAnswerValidator.Type;
import org.obiba.onyx.quartz.editor.utils.QuestionnaireElementNameRenderer;
import org.obiba.onyx.quartz.editor.utils.SaveCancelPanel;
import org.obiba.onyx.quartz.editor.variable.VariablePanel;
import org.obiba.onyx.quartz.editor.variable.VariableRenderer;
import org.obiba.onyx.quartz.editor.variable.VariableUtils;
import org.obiba.onyx.util.data.ComparisonOperator;
import org.obiba.onyx.util.data.DataType;
import org.obiba.onyx.wicket.Images;
import org.obiba.onyx.wicket.reusable.FeedbackWindow;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

/**
 *
 */
@SuppressWarnings("serial")
public abstract class ValidationDataSourceWindow extends Panel {

  // private final transient Logger log = LoggerFactory.getLogger(getClass());

  @SpringBean
  private VariableUtils variableUtils;

  private final FeedbackPanel feedbackPanel;

  private final FeedbackWindow feedbackWindow;

  private final Form<OpenAnswerValidator> form;

  private final ModalWindow variableWindow;

  private final DataType dataType;

  public ValidationDataSourceWindow(String id, IModel<ComparingDataSource> model, final IModel<Question> questionModel, final IModel<Questionnaire> questionnaireModel, final DataType dataType, final ModalWindow modalWindow) {
    super(id, model);
    this.dataType = dataType;

    final ValueType valueType = VariableUtils.convertToValueType(dataType);

    add(CSSPackageResource.getHeaderContribution(ValidationDataSourceWindow.class, "ValidationDataSourceWindow.css"));

    variableWindow = new ModalWindow("variableWindow");
    variableWindow.setCssClassName("onyx");
    variableWindow.setInitialWidth(950);
    variableWindow.setInitialHeight(540);
    variableWindow.setResizable(true);
    variableWindow.setTitle(new ResourceModel("Variable"));
    add(variableWindow);

    final Questionnaire questionnaire = questionnaireModel.getObject();

    final OpenAnswerValidator validator = new OpenAnswerValidator();
    if(model.getObject() != null) {
      ComparingDataSource comparingDataSource = model.getObject();
      validator.setOperator(comparingDataSource.getComparisonOperator());
      VariableDataSource variableDataSource = (VariableDataSource) comparingDataSource.getDataSourceRight();
      if(questionnaire.getName().equals(variableDataSource.getTableName())) {
        try {
          validator.setVariable(questionnaire.getVariable(variableDataSource.getVariableName()));
          validator.setType(Type.VARIABLE);
        } catch(IllegalArgumentException e) {
          // not found in this questionnaire
        }
      }
      if(validator.getType() == null) { // not found yet
        Variable variable = variableUtils.findVariable(variableDataSource);
        if(variable != null) {
          try {
            validator.setVariable(questionnaire.getVariable(variable.getName()));
            validator.setType(Type.VARIABLE);
          } catch(IllegalArgumentException e) {
            // not found
            Question question = VariableUtils.findQuestion(variable, QuestionnaireFinder.getInstance(questionnaire));
            validator.setType(Type.QUESTION_CATEGORY);
            validator.setQuestion(question);
            validator.setCategory(VariableUtils.findCategory(variable, question));
          }
        }
      }

    }

    feedbackPanel = new FeedbackPanel("content");
    feedbackWindow = new FeedbackWindow("feedback");
    feedbackWindow.setOutputMarkupId(true);

    add(feedbackWindow);

    add(form = new Form<OpenAnswerValidator>("form", new Model<OpenAnswerValidator>(validator)));
    form.setMultiPart(false);

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
    questionType.setLabel(new ResourceModel("QuestionType"));
    validationType.add(questionType).add(new SimpleFormComponentLabel("questionTypeLabel", questionType));

    final WebMarkupContainer questionTypeContainer = new WebMarkupContainer("questionTypeContainer");
    questionTypeContainer.setOutputMarkupId(true);
    validationType.add(questionTypeContainer);

    final WebMarkupContainer questionConditionContainer = new WebMarkupContainer("questionConditionContainer");
    questionConditionContainer.setVisible(validator.getType() == QUESTION_CATEGORY);
    questionTypeContainer.add(questionConditionContainer);

    if(questionnaire.getQuestionnaireCache() == null) {
      QuestionnaireFinder.getInstance(questionnaire).buildQuestionnaireCache();
    }

    List<Question> questions = new ArrayList<Question>(questionnaire.getQuestionnaireCache().getQuestionCache().values());
    Collections.sort(questions, new QuestionnaireElementComparator());
    final Multimap<Question, Category> questionCategories = LinkedHashMultimap.create();
    for(Question q : questions) {
      if(!q.equals(questionModel.getObject()) && q.getType() != QuestionType.BOILER_PLATE) {
        questionCategories.putAll(q, findCategories(q));
      }
    }

    final DropDownChoice<Question> questionName = new DropDownChoice<Question>("question", new PropertyModel<Question>(form.getModel(), "question"), new ArrayList<Question>(questionCategories.keySet()), new QuestionnaireElementNameRenderer()) {
      @Override
      public boolean isRequired() {
        return validationType.getModelObject() == QUESTION_CATEGORY;
      }
    };

    questionName.setLabel(new ResourceModel("Question"));
    questionConditionContainer.add(questionName).add(new SimpleFormComponentLabel("questionLabel", questionName));

    final List<Category> categories = questionName.getModelObject() == null ? new ArrayList<Category>() : new ArrayList<Category>(questionCategories.get(questionName.getModelObject()));

    final DropDownChoice<Category> categoryName = new DropDownChoice<Category>("category", new PropertyModel<Category>(form.getModel(), "category"), categories, new QuestionnaireElementNameRenderer()) {
      @Override
      public boolean isRequired() {
        return validationType.getModelObject() == QUESTION_CATEGORY;
      }
    };
    categoryName.setLabel(new ResourceModel("Category"));
    questionConditionContainer.add(categoryName).add(new SimpleFormComponentLabel("categoryLabel", categoryName));

    questionName.add(new OnChangeAjaxBehavior() {
      @Override
      protected void onUpdate(AjaxRequestTarget target) {
        categories.clear();
        categories.addAll(questionName.getModelObject() == null ? new ArrayList<Category>() : new ArrayList<Category>(questionCategories.get(questionName.getModelObject())));
        target.addComponent(categoryName);
      }
    });

    Radio<Type> variableType = new Radio<Type>("variableType", new Model<Type>(VARIABLE));
    variableType.setLabel(new ResourceModel("Variable"));
    validationType.add(variableType).add(new SimpleFormComponentLabel("variableTypeLabel", variableType));

    final WebMarkupContainer variableTypeContainer = new WebMarkupContainer("variableTypeContainer");
    variableTypeContainer.setOutputMarkupId(true);
    validationType.add(variableTypeContainer);

    final WebMarkupContainer variableContainer = new WebMarkupContainer("variableContainer");
    variableContainer.setVisible(validator.getType() == VARIABLE);
    variableTypeContainer.add(variableContainer);

    final List<Variable> variables = new ArrayList<Variable>(Collections2.filter(questionnaire.getVariables(), new Predicate<Variable>() {
      @Override
      public boolean apply(Variable v) {
        return v.getValueType().equals(valueType);
      }
    }));

    final DropDownChoice<Variable> variableDropDown = new DropDownChoice<Variable>("variable", new PropertyModel<Variable>(form.getModel(), "variable"), variables, new VariableRenderer()) {
      @Override
      public boolean isRequired() {
        return validationType.getModelObject() == VARIABLE;
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
        VariablePanel variablePanel = new VariablePanel("content", new Model(null), questionnaireModel, valueType) {
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

    validationType.add(new AjaxFormChoiceComponentUpdatingBehavior() {
      @Override
      protected void onUpdate(AjaxRequestTarget target) {
        Type type = validationType.getModelObject();
        switch(type) {
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

    form.add(new SaveCancelPanel("saveCancel", form) {
      @Override
      protected void onSave(AjaxRequestTarget target, Form<?> form1) {

        VariableDataSource variableDataSource = null;
        switch(validator.getType()) {
        case QUESTION_CATEGORY:
          variableDataSource = new VariableDataSource(questionnaire.getName() + ":" + validator.getQuestion().getName() + "." + validator.getCategory().getName());
          break;
        case VARIABLE:
          variableDataSource = new VariableDataSource(questionnaire.getName() + ":" + validator.getVariable().getName());
          break;
        }

        ComparingDataSource comparingDataSource = new ComparingDataSource(null, validator.getOperator(), variableDataSource);
        ValidationDataSourceWindow.this.onSave(target, comparingDataSource);
        modalWindow.close(target);
      }

      @Override
      protected void onCancel(AjaxRequestTarget target, @SuppressWarnings("hiding") Form<?> form) {
        modalWindow.close(target);
      }

      @Override
      protected void onError(AjaxRequestTarget target, @SuppressWarnings("hiding") Form<?> form) {
        feedbackWindow.setContent(feedbackPanel);
        feedbackWindow.show(target);
      }
    });

  }

  protected abstract void onSave(AjaxRequestTarget target, ComparingDataSource comparingDataSource);

  private List<Category> findCategories(Question question) {
    final List<Category> categories = new ArrayList<Category>();
    if(question != null) {
      if(question.getParentQuestion() == null) {
        for(Category category : question.getCategories()) {
          for(OpenAnswerDefinition openAnswer : category.getOpenAnswerDefinitionsByName().values()) {
            if(dataType == openAnswer.getDataType()) {
              categories.add(category);
              break;
            }
          }
        }
      } else {
        // get parent categories for array
        for(Category category : question.getParentQuestion().getCategories()) {
          for(OpenAnswerDefinition openAnswer : category.getOpenAnswerDefinitionsByName().values()) {
            if(dataType == openAnswer.getDataType()) {
              categories.add(category);
              break;
            }
          }
        }
      }
    }
    return categories;
  }

}
