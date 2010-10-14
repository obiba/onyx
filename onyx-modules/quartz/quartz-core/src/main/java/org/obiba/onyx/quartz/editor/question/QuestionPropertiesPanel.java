/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.question;

import static org.obiba.onyx.quartz.core.wicket.layout.impl.util.QuestionCategoryListToGridPermutator.ROW_COUNT_KEY;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.form.SimpleFormComponentLabel;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.value.ValueMap;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.validator.AbstractValidator;
import org.apache.wicket.validation.validator.StringValidator;
import org.obiba.onyx.core.data.IDataSource;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundle;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundleManager;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.IHasQuestion;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireBuilder;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.builder.ConditionBuilder;
import org.obiba.onyx.quartz.core.wicket.layout.impl.util.ListToGridPermutator;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireStringResourceModelHelper;
import org.obiba.onyx.quartz.editor.category.CategoryFinderPanel;
import org.obiba.onyx.quartz.editor.category.CategoryPropertiesPanel;
import org.obiba.onyx.quartz.editor.category.EditedQuestionCategory;
import org.obiba.onyx.quartz.editor.locale.model.LocaleProperties;
import org.obiba.onyx.quartz.editor.locale.ui.LocalesPropertiesAjaxTabbedPanel;
import org.obiba.onyx.quartz.editor.question.condition.ConditionDataSource;
import org.obiba.onyx.quartz.editor.question.condition.ConditionPanel;
import org.obiba.onyx.quartz.editor.question.condition.Conditions;
import org.obiba.onyx.quartz.editor.questionnaire.EditedQuestionnaire;
import org.obiba.onyx.quartz.editor.questionnaire.QuestionnairePersistenceUtils;
import org.obiba.onyx.quartz.editor.widget.sortable.SortableList;
import org.obiba.onyx.quartz.editor.widget.sortable.SortableListCallback;
import org.obiba.onyx.wicket.behavior.RequiredFormFieldBehavior;
import org.obiba.onyx.wicket.reusable.FeedbackWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class QuestionPropertiesPanel extends Panel {

  private static final String SINGLE_COLUMN_LAYOUT = "singleColumnLayout";

  private static final String GRID_LAYOUT = "gridLayout";

  protected final transient Logger log = LoggerFactory.getLogger(getClass());

  @SpringBean
  protected QuestionnaireBundleManager questionnaireBundleManager;

  @SpringBean
  private QuestionnairePersistenceUtils questionnairePersistenceUtils;

  private final ModalWindow categoryWindow;

  private final FormComponent<String> layoutRadioGroup;

  private final TextField<Integer> nbRowsField;

  private final SortableList<QuestionCategory> categoryList;

  private final FeedbackPanel feedbackPanel;

  private final FeedbackWindow feedbackWindow;

  private final Form<EditedQuestion> form;

  private final IModel<EditedQuestionnaire> questionnaireModel;

  private ListModel<LocaleProperties> localePropertiesModel;

  private ConditionPanel conditionPanel;

  @SuppressWarnings("unchecked")
  public QuestionPropertiesPanel(String id, IModel<Question> model, final IModel<IHasQuestion> parentModel, final IModel<EditedQuestionnaire> questionnaireModel, final ModalWindow questionWindow) {
    super(id, new Model<EditedQuestion>(new EditedQuestion(model.getObject())));
    this.questionnaireModel = questionnaireModel;

    List<LocaleProperties> listLocaleProperties = new ArrayList<LocaleProperties>();
    Questionnaire questionnaire = questionnaireModel.getObject().getElement();
    for(Locale locale : questionnaire.getLocales()) {
      LocaleProperties localeProperties = new LocaleProperties(locale, model);
      List<String> values = new ArrayList<String>();
      for(String property : localeProperties.getKeys()) {
        if(StringUtils.isNotBlank(model.getObject().getName())) {
          QuestionnaireBundle bundle = questionnaireBundleManager.getClearedMessageSourceCacheBundle(questionnaire.getName());
          if(bundle != null) {
            values.add(QuestionnaireStringResourceModelHelper.getNonRecursiveResolutionMessage(bundle, model.getObject(), property, new Object[0], locale));
          }
        }
      }
      localeProperties.setValues(values.toArray(new String[localeProperties.getKeys().length]));
      listLocaleProperties.add(localeProperties);
    }
    localePropertiesModel = new ListModel<LocaleProperties>(listLocaleProperties);

    feedbackPanel = new FeedbackPanel("content");
    feedbackWindow = new FeedbackWindow("feedback");
    feedbackWindow.setOutputMarkupId(true);

    add(feedbackWindow);

    add(form = new Form<EditedQuestion>("form", (IModel<EditedQuestion>) getDefaultModel()));

    categoryWindow = new ModalWindow("categoryWindow");
    categoryWindow.setCssClassName("onyx");
    categoryWindow.setInitialWidth(1000);
    categoryWindow.setInitialHeight(600);
    categoryWindow.setResizable(true);
    form.add(categoryWindow);

    TextField<String> name = new TextField<String>("name", new PropertyModel<String>(form.getModel(), "element.name"));
    name.setLabel(new ResourceModel("Name"));
    name.add(new RequiredFormFieldBehavior());
    final Question question = form.getModelObject().getElement();
    name.add(new AbstractValidator<String>() {

      @Override
      protected void onValidate(IValidatable<String> validatable) {
        for(Question q : parentModel.getObject().getQuestions()) {
          if(question != q && q.getName().equalsIgnoreCase(validatable.getValue())) {
            error(validatable, "QuestionAlreadyExists");
            return;
          }
        }
      }
    });
    form.add(name);
    form.add(new SimpleFormComponentLabel("nameLabel", name));

    TextField<String> variableName = new TextField<String>("variableName", new PropertyModel<String>(form.getModel(), "element.variableName"));
    variableName.setLabel(new ResourceModel("VariableName"));
    variableName.add(new StringValidator.MaximumLengthValidator(20));
    form.add(variableName);
    form.add(new SimpleFormComponentLabel("variableNameLabel", variableName));

    CheckBox multiple = new CheckBox("multiple", new PropertyModel<Boolean>(form.getModel(), "element.multiple"));
    multiple.setLabel(new ResourceModel("Multiple"));
    form.add(multiple);
    form.add(new SimpleFormComponentLabel("multipleLabel", multiple));

    // radio group without default selection
    ValueMap uiArgumentsValueMap = question.getUIArgumentsValueMap();

    String layoutRadioGroupString = null;
    Integer nbRows = ListToGridPermutator.DEFAULT_ROW_COUNT;
    if(uiArgumentsValueMap != null && uiArgumentsValueMap.containsKey(ROW_COUNT_KEY)) {
      layoutRadioGroupString = Integer.parseInt((String) uiArgumentsValueMap.get(ROW_COUNT_KEY)) == question.getCategories().size() ? SINGLE_COLUMN_LAYOUT : GRID_LAYOUT;
      nbRows = uiArgumentsValueMap.getInt(ROW_COUNT_KEY);
    }

    layoutRadioGroup = new RadioGroup<String>("layoutRadioGroup", uiArgumentsValueMap == null ? new Model<String>() : new Model<String>(layoutRadioGroupString));
    form.add(layoutRadioGroup);

    Radio<String> singleColumnLayout = new Radio<String>(SINGLE_COLUMN_LAYOUT, new Model<String>(SINGLE_COLUMN_LAYOUT));
    singleColumnLayout.setLabel(new StringResourceModel("LayoutSingle", QuestionPropertiesPanel.this, null));
    layoutRadioGroup.add(singleColumnLayout);
    layoutRadioGroup.add(new SimpleFormComponentLabel("singleColumnLayoutLabel", singleColumnLayout));

    Radio<String> gridLayout = new Radio<String>(GRID_LAYOUT, new Model<String>(GRID_LAYOUT));
    gridLayout.setLabel(new StringResourceModel("LayoutGrid", QuestionPropertiesPanel.this, null));
    layoutRadioGroup.add(gridLayout);
    layoutRadioGroup.add(new SimpleFormComponentLabel("gridLayoutLabel", gridLayout));

    form.add(nbRowsField = new TextField<Integer>("nbRows", new Model<Integer>(nbRows), Integer.class));

    form.add(new LocalesPropertiesAjaxTabbedPanel("localesPropertiesTabs", new PropertyModel<Question>(form.getModel(), "element"), localePropertiesModel));

    categoryList = new SortableList<QuestionCategory>("categoryList", question.getQuestionCategories()) {

      @Override
      @SuppressWarnings({ "rawtypes" })
      public SortableList<QuestionCategory>.Button[] getButtons() {

        SortableList<QuestionCategory>.Button addButton = new SortableList.Button(new StringResourceModel("AddCategory", QuestionPropertiesPanel.this, null)) {

          @Override
          public void callback(AjaxRequestTarget target) {
            QuestionCategory questionCategory = new QuestionCategory();
            questionCategory.setCategory(new Category(null));
            questionCategory.setQuestion(question);
            categoryWindow.setContent(new CategoryPropertiesPanel("content", new Model<QuestionCategory>(questionCategory), questionnaireModel, categoryWindow) {

              @Override
              public void onSave(AjaxRequestTarget target1, EditedQuestionCategory editedCategory) {
                super.onSave(target1, editedCategory);
                QuestionPropertiesPanel.this.form.getModelObject().getElement().addQuestionCategory(editedCategory.getElement());
                refreshList(target1);
                persist(target1);
              }
            });
            categoryWindow.show(target);
          }
        };

        SortableList<QuestionCategory>.Button addExistingButton = new SortableList.Button(new StringResourceModel("AddExistingCategory", QuestionPropertiesPanel.this, null)) {

          @Override
          public void callback(AjaxRequestTarget target) {
            categoryWindow.setContent(new CategoryFinderPanel("content", new Model<Question>(QuestionPropertiesPanel.this.form.getModelObject().getElement()), questionnaireModel, categoryWindow) {
              @Override
              public void onSave(AjaxRequestTarget target1, List<Category> categories) {
                for(Category category : categories) {
                  QuestionCategory questionCategory = new QuestionCategory();
                  questionCategory.setCategory(category);
                  question.addQuestionCategory(questionCategory);
                }
                refreshList(target1);
                persist(target1);
              }
            });
            categoryWindow.show(target);
          }
        };

        return new SortableList.Button[] { addButton, addExistingButton };
      }

      @Override
      public String getItemLabel(QuestionCategory questionCategory) {
        return questionCategory.getCategory().getName();
      }

      @Override
      public void editItem(QuestionCategory questionCategory, AjaxRequestTarget target) {
        categoryWindow.setTitle(new ResourceModel("Category"));
        categoryWindow.setContent(new CategoryPropertiesPanel("content", new Model<QuestionCategory>(questionCategory), questionnaireModel, categoryWindow) {
          @Override
          public void onSave(AjaxRequestTarget target1, EditedQuestionCategory editedCategory) {
            super.onSave(target1, editedCategory);
            refreshList(target1);
            persist(target1);
          }
        });
        categoryWindow.show(target);
      }

      @Override
      public void deleteItem(QuestionCategory questionCategory, AjaxRequestTarget target) {
        QuestionPropertiesPanel.this.form.getModelObject().getElement().getQuestionCategories().remove(questionCategory);
        refreshList(target);
        persist(target);
      }

    };
    form.add(form.getModelObject().getElement().getParentQuestion() == null ? categoryList : new WebMarkupContainer("categoryList"));

    form.add(conditionPanel = new ConditionPanel("conditions", new Model<Conditions>(new Conditions()), new Model<Question>(question), questionnaireModel));

    form.add(new AjaxButton("save", form) {
      @Override
      public void onSubmit(AjaxRequestTarget target, Form<?> form2) {
        onSave(target, form.getModelObject());
        questionWindow.close(target);
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
        questionWindow.close(target);
      }
    }.setDefaultFormProcessing(false));
  }

  public void onSave(AjaxRequestTarget target, final EditedQuestion editedQuestion) {
    editedQuestion.setLocalePropertiesWithNamingStrategy(localePropertiesModel.getObject());
    final Question question = editedQuestion.getElement();
    categoryList.save(target, new SortableListCallback<QuestionCategory>() {
      @Override
      public void onSave(List<QuestionCategory> orderedItems, AjaxRequestTarget target1) {
        question.getQuestionCategories().clear();
        for(QuestionCategory questionCategory : orderedItems) {
          question.getQuestionCategories().add(questionCategory);
        }

        // Layout single or grid: make sure that the categories are added before this...
        String layoutSelection = layoutRadioGroup.getModelObject();
        if(SINGLE_COLUMN_LAYOUT.equals(layoutSelection)) {
          question.clearUIArguments();
          question.addUIArgument(ROW_COUNT_KEY, question.getCategories().size() + "");
        } else if(GRID_LAYOUT.equals(layoutSelection)) {
          question.clearUIArguments();
          question.addUIArgument(ROW_COUNT_KEY, nbRowsField.getModelObject() + "");
        }
      }
    });
    editedQuestion.setConditions(((Conditions) conditionPanel.getDefaultModelObject()));
  }

  protected void persist(AjaxRequestTarget target) {
    try {
      EditedQuestion editedQuestion = form.getModelObject();
      Question question = editedQuestion.getElement();
      QuestionnaireBuilder builder = questionnairePersistenceUtils.createBuilder(questionnaireModel.getObject());

      Conditions conditions = editedQuestion.getConditions();
      int nbDataSources = conditions.getDataSources().size();
      if(nbDataSources > 0) {
        List<IDataSource> ds = new ArrayList<IDataSource>(nbDataSources);
        for(ConditionDataSource dataSource : conditions.getDataSources()) {
          ConditionBuilder conditionBuilder = ConditionBuilder.createQuestionCondition(builder, dataSource.getQuestion().getName(), dataSource.getCategory() == null ? null : dataSource.getCategory().getName(), dataSource.getOpenAnswerDefinition() == null ? null : dataSource.getOpenAnswerDefinition().getName());
          ds.add(conditionBuilder.getElement());
        }
        if(nbDataSources == 1) {
          question.setCondition(ds.get(0));
        } else {
          builder.inQuestion(question.getName()).setCondition(conditions.getExpression(), ds.toArray(new IDataSource[nbDataSources]));
        }
      }

      questionnairePersistenceUtils.persist(editedQuestion, builder);

    } catch(Exception e) {
      log.error("Cannot persist questionnaire", e);
      error(e.getMessage());
      feedbackWindow.setContent(feedbackPanel);
      feedbackWindow.show(target);
    }
  }
}
