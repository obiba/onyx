/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.category;

import java.io.Serializable;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow.CloseButtonCallback;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow.WindowClosedCallback;
import org.apache.wicket.extensions.ajax.markup.html.tabs.AjaxTabbedPanel;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.SimpleFormComponentLabel;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.validator.PatternValidator;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.CategoryByQuestionsComparator;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireFinder;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireSharedCategory;
import org.obiba.onyx.quartz.editor.QuartzEditorPanel;
import org.obiba.onyx.quartz.editor.locale.LocaleProperties;
import org.obiba.onyx.quartz.editor.locale.LocaleProperties.KeyValue;
import org.obiba.onyx.quartz.editor.locale.LocalePropertiesUtils;
import org.obiba.onyx.quartz.editor.question.EditedQuestion;
import org.obiba.onyx.quartz.editor.utils.AbstractAutoCompleteTextField;
import org.obiba.onyx.quartz.editor.utils.QuestionnaireElementCloner;
import org.obiba.onyx.quartz.editor.utils.QuestionnaireElementCloner.CloneSettings;
import org.obiba.onyx.quartz.editor.utils.QuestionnaireElementCloner.ElementClone;
import org.obiba.onyx.quartz.editor.widget.sortable.SortableList;
import org.obiba.onyx.wicket.Images;
import org.obiba.onyx.wicket.reusable.FeedbackWindow;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;

import static java.text.Normalizer.normalize;
import static org.apache.commons.lang.StringUtils.abbreviate;
import static org.apache.commons.lang.StringUtils.equalsIgnoreCase;

/**
 *
 */
@SuppressWarnings("serial")
public class CategoryListPanel extends Panel {

  // private final transient Logger logger = LoggerFactory.getLogger(getClass());

  @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "SE_BAD_FIELD",
      justification = "Need to be be re-initialized upon deserialization")
  @SpringBean
  private LocalePropertiesUtils localePropertiesUtils;

  private final ModalWindow categoryWindow;

  private final FeedbackPanel feedbackPanel;

  private final FeedbackWindow feedbackWindow;

  private SortableList<QuestionCategory> categoryList;

  private final Multimap<Category, Question> questionsByCategory;

  private final List<Category> questionnaireCategories;

  private final IModel<Questionnaire> questionnaireModel;

  private final IModel<LocaleProperties> localePropertiesModel;

  private final Panel parentPanel;

  public CategoryListPanel(String id, IModel<EditedQuestion> model, final IModel<Questionnaire> questionnaireModel,
      final IModel<LocaleProperties> localePropertiesModel, FeedbackPanel feedbackPanel, FeedbackWindow feedbackWindow,
      Panel parentPanel) {
    super(id, model);
    this.questionnaireModel = questionnaireModel;
    this.localePropertiesModel = localePropertiesModel;
    this.feedbackPanel = feedbackPanel;
    this.feedbackWindow = feedbackWindow;
    this.parentPanel = parentPanel;

    add(CSSPackageResource.getHeaderContribution(CategoryListPanel.class, "CategoryListPanel.css"));

    final Question question = model.getObject().getElement();

    QuestionnaireFinder questionnaireFinder = QuestionnaireFinder.getInstance(questionnaireModel.getObject());
    questionnaireModel.getObject().setQuestionnaireCache(null);
    questionsByCategory = questionnaireFinder.findQuestionsByCategory();
    questionnaireCategories = new ArrayList<Category>(questionsByCategory.keySet());
    Collections.sort(questionnaireCategories, new CategoryByQuestionsComparator(questionsByCategory));

    categoryWindow = new ModalWindow("categoryWindow");
    categoryWindow.setCssClassName("onyx");
    categoryWindow.setInitialWidth(950);
    categoryWindow.setInitialHeight(550);
    categoryWindow.setResizable(true);
    categoryWindow.setTitle(new ResourceModel("Category"));
    add(categoryWindow);

    List<ITab> tabs = new ArrayList<ITab>();
    tabs.add(new AbstractTab(new ResourceModel("Add.simple")) {
      @Override
      public Panel getPanel(String panelId) {
        return new SimpleAddPanel(panelId);
      }
    });
    tabs.add(new AbstractTab(new ResourceModel("Add.bulk")) {
      @Override
      public Panel getPanel(String panelId) {
        return new BulkAddPanel(panelId);
      }
    });
    tabs.add(new AbstractTab(new ResourceModel("Add.existing")) {
      @Override
      public Panel getPanel(String panelId) {
        return new AddExistingPanel(panelId);
      }
    });
    add(new AjaxTabbedPanel("addTabs", tabs));

    categoryList = new SortableList<QuestionCategory>("categories", question.getQuestionCategories()) {

      @Override
      public void onItemPopulation(QuestionCategory questionCategory) {
      }

      @Override
      public Component getItemTitle(@SuppressWarnings("hiding") String id, QuestionCategory questionCategory) {
        Category category = questionCategory.getCategory();

        if(QuestionnaireSharedCategory.isSharedIfLink(questionCategory, questionnaireModel.getObject())) {
          StringBuilder sb = new StringBuilder();
          for(Question q : questionsByCategory.get(category)) {
            if(q.getName().equals(question.getName())) continue;
            if(sb.length() > 0) sb.append(", ");
            sb.append(q.getName());
          }
          String shared = " <span class=\"shared\">" + new StringResourceModel("sharedWith", CategoryListPanel.this,
              null, new Object[] { abbreviate(sb.toString(), 50) }).getString() + "</span>";
          return new Label(id, category.getName() + shared).setEscapeModelStrings(false);
        }
        return new Label(id, category.getName());
      }

      @Override
      public void editItem(final QuestionCategory questionCategory, AjaxRequestTarget target) {
        final ElementClone<QuestionCategory> original = QuestionnaireElementCloner
            .clone(questionCategory, new CloneSettings(true), localePropertiesModel.getObject());
        categoryWindow.setContent(
            new CategoryWindow("content", new Model<QuestionCategory>(questionCategory), questionnaireModel,
                localePropertiesModel, categoryWindow) {
              @Override
              public void onSave(@SuppressWarnings("hiding") AjaxRequestTarget target,
                  @SuppressWarnings("hiding") QuestionCategory questionCategory) {
                Collection<QuestionCategory> others = findOtherQuestionCategories(questionCategory.getCategory(),
                    questionCategory);
                if(!others.isEmpty()) {
                  LocaleProperties localeProperties = localePropertiesModel.getObject();
                  ListMultimap<Locale, KeyValue> elementLabelsQC = localeProperties.getElementLabels(questionCategory);
                  for(QuestionCategory other : others) {
                    localePropertiesUtils.load(localeProperties, questionnaireModel.getObject(), other);
                    ListMultimap<Locale, KeyValue> elementLabelsOtherQC = localeProperties.getElementLabels(other);
                    copyLabels(elementLabelsQC, elementLabelsOtherQC);
                  }
                }
              }

              @Override
              public void onCancel(@SuppressWarnings("hiding") AjaxRequestTarget target,
                  QuestionCategory questionCategory) {
                rollback(questionCategory, original);
              }
            });
        categoryWindow.setCloseButtonCallback(new CloseButtonCallback() {
          @Override
          public boolean onCloseButtonClicked(@SuppressWarnings("hiding") AjaxRequestTarget target) {
            rollback(questionCategory, original);
            return true;
          }
        });
        categoryWindow.setWindowClosedCallback(new WindowClosedCallback() {
          @Override
          public void onClose(@SuppressWarnings("hiding") AjaxRequestTarget target) {
            refreshList(target);
            refreshListAndNoAnswerPanel(categoryList, target);
          }
        });
        categoryWindow.show(target);
      }

      @Override
      @SuppressWarnings("unchecked")
      public void deleteItem(QuestionCategory questionCategory, AjaxRequestTarget target) {
        ((IModel<EditedQuestion>) CategoryListPanel.this.getDefaultModel()).getObject().getElement()
            .getQuestionCategories().remove(questionCategory);
        localePropertiesModel.getObject().remove(questionnaireModel.getObject(), questionCategory);
        refreshListAndNoAnswerPanel(categoryList, target);
      }

      @Override
      public Button[] getButtons() {
        return null;
      }

    };
    add(categoryList);

  }

  private class SimpleAddPanel extends Panel {

    private static final long serialVersionUID = -428429946304239202L;

    private SimpleAddPanel(String id) {
      super(id);
      Form<String> form = new Form<String>("form");
      form.setMultiPart(false);
      add(form);

      final TextField<String> categoryName = new TextField<String>("category", new Model<String>());
      categoryName.setOutputMarkupId(true);
      categoryName.setLabel(new ResourceModel("NewCategory"));
      categoryName.add(new PatternValidator(QuartzEditorPanel.ELEMENT_NAME_PATTERN));

      form.add(categoryName);
      form.add(new SimpleFormComponentLabel("categoryLabel", categoryName));

      AjaxButton addButton = new AjaxButton("addButton", form) {
        @SuppressWarnings("unchecked")
        @Override
        protected void onSubmit(AjaxRequestTarget target, @SuppressWarnings("hiding") Form<?> form) {
          String name = categoryName.getModelObject();
          if(StringUtils.isBlank(name)) return;
          if(checkIfCategoryAlreadyExists(
              ((IModel<EditedQuestion>) CategoryListPanel.this.getDefaultModel()).getObject().getElement(), name)) {
            error(new StringResourceModel("CategoryAlreadyExistsForThisQuestion", CategoryListPanel.this, null)
                .getObject());
            return;
          }
          addCategory(((IModel<EditedQuestion>) CategoryListPanel.this.getDefaultModel()).getObject().getElement(),
              name, false);
          categoryName.setModelObject(null);
          target.addComponent(categoryName);
          refreshListAndNoAnswerPanel(categoryList, target);
        }

        @Override
        protected void onError(AjaxRequestTarget target, @SuppressWarnings("hiding") Form<?> form) {
          feedbackWindow.setContent(feedbackPanel);
          feedbackWindow.show(target);
        }

      };
      categoryName
          .add(new AttributeAppender("onkeypress", true, new Model<String>(buildPressEnterScript(addButton)), " "));
      addButton.add(new Image("img", Images.ADD).add(new AttributeModifier("title", true, new ResourceModel("Add"))));
      form.add(addButton);
    }
  }

  private class BulkAddPanel extends Panel {

    private static final long serialVersionUID = -6212227855720485511L;

    private BulkAddPanel(String id) {
      super(id);
      Form<String> form = new Form<String>("form");
      form.setMultiPart(false);
      add(form);
      final TextArea<String> categories = new TextArea<String>("categories", new Model<String>());
      categories.setOutputMarkupId(true);
      categories.setLabel(new ResourceModel("NewCategories"));
      form.add(categories);
      form.add(new SimpleFormComponentLabel("categoriesLabel", categories));
      AjaxSubmitLink addLink = new AjaxSubmitLink("bulkAddLink") {
        @Override
        @SuppressWarnings("unchecked")
        protected void onSubmit(AjaxRequestTarget target, Form<?> form1) {
          String[] names = StringUtils.split(categories.getModelObject(), ',');
          if(names == null) return;
          Question question = ((IModel<EditedQuestion>) CategoryListPanel.this.getDefaultModel()).getObject()
              .getElement();
          for(String name : names) {
            name = StringUtils.trimToNull(name);
            if(name == null) continue;
            if(QuartzEditorPanel.ELEMENT_NAME_PATTERN.matcher(name).matches()) {
              addCategory(question, name, false);
            }
          }
          categories.setModelObject(null);
          target.addComponent(categories);
          refreshListAndNoAnswerPanel(categoryList, target);
        }

        @Override
        protected void onError(AjaxRequestTarget target, Form<?> form1) {
          feedbackWindow.setContent(feedbackPanel);
          feedbackWindow.show(target);
        }
      };

      addLink
          .add(new Image("bulkAddImg", Images.ADD).add(new AttributeModifier("title", true, new ResourceModel("Add"))));
      form.add(addLink);
    }
  }

  private class AddExistingPanel extends Panel {

    private static final long serialVersionUID = -4040203819940405857L;

    private static final int AUTO_COMPLETE_SIZE = 15;

    private AddExistingPanel(String id) {
      super(id);
      Form<String> form = new Form<String>("form");
      form.setMultiPart(false);
      add(form);

      final AbstractAutoCompleteTextField<CategoryWithQuestions> categoryNameFinder = new AbstractAutoCompleteTextField<CategoryWithQuestions>(
          "category", new Model<CategoryWithQuestions>()) {
        @SuppressWarnings("unchecked")
        @Override
        protected List<CategoryWithQuestions> getChoiceList(String input) {
          if(StringUtils.isBlank(input)) {
            return Collections.emptyList();
          }
          Question question = ((IModel<EditedQuestion>) CategoryListPanel.this.getDefaultModel()).getObject()
              .getElement();
          List<String> questionCatNames = new ArrayList<String>(question.getCategories().size());
          for(Category category : question.getCategories()) {
            questionCatNames.add(category.getName());
          }
          List<CategoryWithQuestions> choices = new ArrayList<CategoryWithQuestions>(AUTO_COMPLETE_SIZE);
          for(Category category : questionnaireCategories) {
            String name = category.getName();
            if(!questionCatNames.contains(name)
                && normalize(name, Normalizer.Form.NFD).toLowerCase().startsWith(normalize(input, Normalizer.Form.NFD).toLowerCase())) {
              choices.add(new CategoryWithQuestions(category, questionsByCategory.get(category)));
              if(choices.size() == AUTO_COMPLETE_SIZE) break;
            }
          }
          return choices;
        }

        @Override
        protected String getChoiceValue(CategoryWithQuestions categoryWithQuestions) throws Throwable {
          return categoryWithQuestions.toString();
        }

      };
      categoryNameFinder.setOutputMarkupId(true);
      categoryNameFinder.setLabel(new ResourceModel("CategoryName"));

      form.add(categoryNameFinder);
      form.add(new SimpleFormComponentLabel("categoryLabel", categoryNameFinder));

      AjaxButton addButton = new AjaxButton("addButton", form) {
        @SuppressWarnings("unchecked")
        @Override
        protected void onSubmit(AjaxRequestTarget target, Form<?> form1) {
          CategoryWithQuestions categoryWithQuestions = categoryNameFinder.findChoice();
          if(categoryWithQuestions == null) {
            error(new StringResourceModel("CategoryDoesNotExist", CategoryListPanel.this, null).getObject());
            return;
          }
          String name = categoryWithQuestions.getCategory().getName();
          if(checkIfCategoryAlreadyExists(
              ((IModel<EditedQuestion>) CategoryListPanel.this.getDefaultModel()).getObject().getElement(), name)) {
            error(new StringResourceModel("CategoryAlreadyExistsForThisQuestion", CategoryListPanel.this, null)
                .getObject());
            return;
          }
          Category category = categoryWithQuestions.getCategory();
          addCategory(((IModel<EditedQuestion>) CategoryListPanel.this.getDefaultModel()).getObject().getElement(),
              category, true);
          categoryNameFinder.setModelObject(null);

          target.addComponent(categoryNameFinder);
          refreshListAndNoAnswerPanel(categoryList, target);
        }

        @Override
        protected void onError(AjaxRequestTarget target, Form<?> form1) {
          feedbackWindow.setContent(feedbackPanel);
          feedbackWindow.show(target);
        }
      };

      categoryNameFinder
          .add(new AttributeAppender("onkeypress", true, new Model<String>(buildPressEnterScript(addButton)), " "));
      addButton.add(new Image("img", Images.ADD).add(new AttributeModifier("title", true, new ResourceModel("Add"))));
      form.add(addButton);
    }

    private class CategoryWithQuestions implements Serializable {

      private static final long serialVersionUID = 7682388660029075598L;

      private final Category category;

      private final Collection<Question> questions;

      private CategoryWithQuestions(Category category, Collection<Question> questions) {
        this.category = category;
        this.questions = questions;
      }

      public Category getCategory() {
        return category;
      }

      @Override
      public String toString() {
        StringBuilder sb = new StringBuilder();
        for(Question q : questions) {
          if(sb.length() > 0) sb.append(", ");
          sb.append(q.getName());
        }
        return category.getName() + " (" + abbreviate(sb.toString(), 50) + ")";
      }
    }
  }

  private String buildPressEnterScript(AjaxButton addButton) {
    return "if (event.keyCode == 13) {document.getElementById('" + addButton
        .getMarkupId() + "').click(); return false;} else {return true;};";
  }

  private boolean checkIfCategoryAlreadyExists(Question question, String name) {
    for(QuestionCategory questionCategory : question.getQuestionCategories()) {
      if(equalsIgnoreCase(questionCategory.getName(), name) || equalsIgnoreCase(
          questionCategory.getCategory().getName(), name)) {
        return true; // category already exists
      }
    }
    return false;
  }

  private void addCategory(Question question, String name, boolean shared) {
    if(StringUtils.isNotBlank(name) && !checkIfCategoryAlreadyExists(question, name)) {
      addCategory(question, new Category(name), shared);
    }
  }

  private void addCategory(Question question, Category category, boolean shared) {
    QuestionCategory questionCategory = new QuestionCategory();
    questionCategory.setCategory(category);
    question.addQuestionCategory(questionCategory);

    if(shared) {
      localePropertiesUtils.load(localePropertiesModel.getObject(), questionnaireModel.getObject(), questionCategory);

      Collection<QuestionCategory> otherQuestionCategories = findOtherQuestionCategories(category, questionCategory);
      if(!otherQuestionCategories.isEmpty()) {
        QuestionCategory otherQuestionCategory = otherQuestionCategories.iterator().next();
        localePropertiesUtils
            .load(localePropertiesModel.getObject(), questionnaireModel.getObject(), otherQuestionCategory);

        ListMultimap<Locale, KeyValue> elementLabelsOtherQC = localePropertiesModel.getObject()
            .getElementLabels(otherQuestionCategory);
        ListMultimap<Locale, KeyValue> elementLabelsQC = localePropertiesModel.getObject()
            .getElementLabels(questionCategory);
        copyLabels(elementLabelsOtherQC, elementLabelsQC);
      }
    }
  }

  /**
   * @param from
   * @param to
   */
  private void copyLabels(ListMultimap<Locale, KeyValue> from, ListMultimap<Locale, KeyValue> to) {
    for(Locale locale : localePropertiesModel.getObject().getLocales()) {
      for(final KeyValue kv : from.get(locale)) {
        KeyValue findKey = Iterables.find(to.get(locale), new Predicate<KeyValue>() {

          @Override
          public boolean apply(KeyValue input) {
            return kv.getKey().equals(input.getKey());
          }

        });
        findKey.setValue(kv.getValue());
      }
    }
  }

  /**
   * @param category
   * @param questionCategory
   * @return QuestionCategory which share category with only questionCategory, return null otherwise
   */
  private Collection<QuestionCategory> findOtherQuestionCategories(final Category category,
      final QuestionCategory questionCategory) {
    QuestionnaireFinder questionnaireFinder = QuestionnaireFinder.getInstance(questionnaireModel.getObject());
    questionnaireModel.getObject().setQuestionnaireCache(null);
    Multimap<Category, Question> categories = questionnaireFinder.findCategories(category.getName());
    Collection<QuestionCategory> filter = new ArrayList<QuestionCategory>();
    for(Question findQuestion : categories.get(category)) {
      List<QuestionCategory> questionCategories = findQuestion.getQuestionCategories();
      filter.addAll(Collections2.filter(questionCategories, new Predicate<QuestionCategory>() {
        @Override
        public boolean apply(QuestionCategory input) {
          return input.getCategory() == category && input != questionCategory;
        }
      }));
    }
    return filter;
  }

  @SuppressWarnings("unchecked")
  private void refreshListAndNoAnswerPanel(
      @SuppressWarnings({ "hiding", "ParameterHidesMemberVariable" }) SortableList<QuestionCategory> categoryList,
      AjaxRequestTarget target) {
    categoryList.refreshList(target);
    MultipleChoiceCategoryHeaderPanel multipleChoiceCategoryHeaderPanel = new MultipleChoiceCategoryHeaderPanel(
        "headerMultipleChoice", questionnaireModel, (IModel<EditedQuestion>) getDefaultModel());
    parentPanel.addOrReplace(multipleChoiceCategoryHeaderPanel);
    target.addComponent(multipleChoiceCategoryHeaderPanel);
  }

  private synchronized void rollback(QuestionCategory modified, ElementClone<QuestionCategory> original) {
    Question question = ((EditedQuestion) getDefaultModelObject()).getElement();
    int index = question.getQuestionCategories().indexOf(modified);
    question.removeQuestionCategory(modified);
    question.addQuestionCategory(original.getElement(), index);
    QuestionnaireElementCloner.copy(original.getElement().getCategory(), modified.getCategory(),
        new CloneSettings(true, false, false, false));
    original.getElement().setCategory(modified.getCategory());
    Questionnaire questionnaire = questionnaireModel.getObject();
    LocaleProperties localeProperties = localePropertiesModel.getObject();
    localeProperties.remove(questionnaire, modified, modified.getCategory());
    QuestionnaireElementCloner.addProperties(original, localeProperties);
  }

}