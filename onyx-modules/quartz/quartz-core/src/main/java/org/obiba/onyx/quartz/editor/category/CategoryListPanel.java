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

import static org.apache.commons.lang.StringUtils.abbreviate;
import static org.apache.commons.lang.StringUtils.equalsIgnoreCase;

import java.io.Serializable;
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
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireElementComparator;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireFinder;
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
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;

/**
 *
 */
@SuppressWarnings("serial")
public class CategoryListPanel extends Panel {

  // private final transient Logger logger = LoggerFactory.getLogger(getClass());

  @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "SE_BAD_FIELD", justification = "Need to be be re-initialized upon deserialization")
  @SpringBean
  private LocalePropertiesUtils localePropertiesUtils;

  private final ModalWindow categoryWindow;

  private final FeedbackPanel feedbackPanel;

  private final FeedbackWindow feedbackWindow;

  private SortableList<QuestionCategory> categoryList;

  private Multimap<Category, Question> questionsByCategory;

  private List<Category> questionnaireCategories;

  private IModel<Questionnaire> questionnaireModel;

  private IModel<LocaleProperties> localePropertiesModel;

  public CategoryListPanel(String id, final IModel<EditedQuestion> model, final IModel<Questionnaire> questionnaireModel, final IModel<LocaleProperties> localePropertiesModel, FeedbackPanel feedbackPanel, FeedbackWindow feedbackWindow) {
    super(id, model);
    this.questionnaireModel = questionnaireModel;
    this.localePropertiesModel = localePropertiesModel;
    this.feedbackPanel = feedbackPanel;
    this.feedbackWindow = feedbackWindow;

    add(CSSPackageResource.getHeaderContribution(CategoryListPanel.class, "CategoryListPanel.css"));

    final Question question = model.getObject().getElement();

    final QuestionnaireFinder questionnaireFinder = QuestionnaireFinder.getInstance(questionnaireModel.getObject());
    questionnaireModel.getObject().setQuestionnaireCache(null);
    questionsByCategory = questionnaireFinder.findQuestionsByCategory();
    questionnaireCategories = new ArrayList<Category>(questionsByCategory.keySet());
    Collections.sort(questionnaireCategories, new QuestionnaireElementComparator());

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
      public Component getItemTitle(@SuppressWarnings("hiding") String id, final QuestionCategory questionCategory) {
        Category category = questionCategory.getCategory();

        if(isShared(questionCategory)) {
          StringBuilder sb = new StringBuilder();
          for(Question q : questionsByCategory.get(category)) {
            if(q.getName().equals(question.getName())) continue;
            if(sb.length() > 0) sb.append(", ");
            sb.append(q.getName());
          }
          String shared = " <span class=\"shared\">" + new StringResourceModel("sharedWith", CategoryListPanel.this, null, new Object[] { abbreviate(sb.toString(), 50) }).getString() + "</span>";
          return new Label(id, category.getName() + shared).setEscapeModelStrings(false);
        }
        return new Label(id, category.getName());
      }

      @Override
      public void editItem(final QuestionCategory questionCategory, AjaxRequestTarget target) {
        final ElementClone<QuestionCategory> original = QuestionnaireElementCloner.clone(questionCategory, new CloneSettings(true), localePropertiesModel.getObject());
        categoryWindow.setContent(new CategoryWindow("content", new Model<QuestionCategory>(questionCategory), questionnaireModel, localePropertiesModel, categoryWindow) {
          @Override
          public void onSave(@SuppressWarnings("hiding") AjaxRequestTarget target, @SuppressWarnings("hiding") QuestionCategory questionCategory) {
            Collection<QuestionCategory> others = findOtherQuestionCategories(questionCategory.getCategory(), questionCategory);
            if(!others.isEmpty()) {
              ListMultimap<Locale, KeyValue> elementLabelsQC = localePropertiesModel.getObject().getElementLabels(questionCategory);
              for(QuestionCategory other : others) {
                localePropertiesUtils.load(localePropertiesModel.getObject(), questionnaireModel.getObject(), other);
                ListMultimap<Locale, KeyValue> elementLabelsOtherQC = localePropertiesModel.getObject().getElementLabels(other);
                for(Locale locale : localePropertiesModel.getObject().getLocales()) {
                  List<KeyValue> list = elementLabelsOtherQC.get(locale);
                  list.get(0).setValue(elementLabelsQC.get(locale).get(0).getValue());
                }
              }
            }
          }

          public void onCancel(@SuppressWarnings("hiding") AjaxRequestTarget target, @SuppressWarnings("hiding") QuestionCategory questionCategory) {
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
          }
        });
        categoryWindow.show(target);
      }

      @Override
      @SuppressWarnings("unchecked")
      public void deleteItem(final QuestionCategory questionCategory, AjaxRequestTarget target) {
        ((IModel<EditedQuestion>) CategoryListPanel.this.getDefaultModel()).getObject().getElement().getQuestionCategories().remove(questionCategory);
        refreshList(target);
      }

      @Override
      public Button[] getButtons() {
        return null;
      }

    };
    add(categoryList);

  }

  /**
   * Return true is category associated to the given questionCategory is shared, false otherwise. we use this method if
   * question associated to questionCategory is not yet linked to the questionnaire.
   * (QuestionnaireFinder.findSharedCategories do not contains yet the category)
   * 
   * @param question
   * @param questionCategory
   * @param category
   * @return
   */
  private boolean isShared(final QuestionCategory questionCategory) {
    QuestionnaireFinder questionnaireFinder = QuestionnaireFinder.getInstance(questionnaireModel.getObject());
    questionnaireModel.getObject().setQuestionnaireCache(null);
    Multimap<Category, Question> categoriesFilterName = questionnaireFinder.findCategories(questionCategory.getCategory().getName());
    Collection<Category> categories = Collections2.filter(categoriesFilterName.keySet(), new Predicate<Category>() {

      @Override
      public boolean apply(Category input) {
        return input == questionCategory.getCategory();
      }
    });
    if(categoriesFilterName.isEmpty() || categories.isEmpty()) {
      return false;
    }
    Collection<Question> questions = categoriesFilterName.get(categories.iterator().next());
    Collection<Question> otherQuestions = Collections2.filter(questions, new Predicate<Question>() {

      @Override
      public boolean apply(Question input) {
        return input != questionCategory.getQuestion();
      }
    });
    return !otherQuestions.isEmpty();
  }

  private class SimpleAddPanel extends Panel {

    public SimpleAddPanel(String id) {
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
          if(checkIfCategoryAlreadyExists(((IModel<EditedQuestion>) CategoryListPanel.this.getDefaultModel()).getObject().getElement(), name)) {
            error(new StringResourceModel("CategoryAlreadyExists", CategoryListPanel.this, null).getObject());
            return;
          }
          addCategory(((IModel<EditedQuestion>) CategoryListPanel.this.getDefaultModel()).getObject().getElement(), name);
          categoryName.setModelObject(null);
          target.addComponent(categoryName);
          target.addComponent(categoryList);
        }

        @Override
        protected void onError(AjaxRequestTarget target, @SuppressWarnings("hiding") Form<?> form) {
          feedbackWindow.setContent(feedbackPanel);
          feedbackWindow.show(target);
        }

      };
      categoryName.add(new AttributeAppender("onkeypress", true, new Model<String>(buildPressEnterScript(addButton)), " "));
      addButton.add(new Image("img", Images.ADD).add(new AttributeModifier("title", true, new ResourceModel("Add"))));
      form.add(addButton);
    }
  }

  private class BulkAddPanel extends Panel {

    public BulkAddPanel(String id) {
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
          Question question = ((IModel<EditedQuestion>) CategoryListPanel.this.getDefaultModel()).getObject().getElement();
          for(String name : names) {
            name = StringUtils.trimToNull(name);
            if(name == null) continue;
            if(QuartzEditorPanel.ELEMENT_NAME_PATTERN.matcher(name).matches()) {
              addCategory(question, name);
            }
          }
          categories.setModelObject(null);
          target.addComponent(categories);
          target.addComponent(categoryList);
        }

        @Override
        protected void onError(AjaxRequestTarget target, Form<?> form1) {
          feedbackWindow.setContent(feedbackPanel);
          feedbackWindow.show(target);
        }
      };

      addLink.add(new Image("bulkAddImg", Images.ADD).add(new AttributeModifier("title", true, new ResourceModel("Add"))));
      form.add(addLink);
    }
  }

  private class AddExistingPanel extends Panel {

    private static final int AUTO_COMPLETE_SIZE = 15;

    public AddExistingPanel(String id) {
      super(id);
      Form<String> form = new Form<String>("form");
      form.setMultiPart(false);
      add(form);

      final AbstractAutoCompleteTextField<CategoryWithQuestions> categoryNameFinder = new AbstractAutoCompleteTextField<CategoryWithQuestions>("category", new Model<CategoryWithQuestions>()) {
        @SuppressWarnings("unchecked")
        @Override
        protected List<CategoryWithQuestions> getChoiceList(String input) {
          if(StringUtils.isBlank(input)) {
            List<CategoryWithQuestions> emptyList = Collections.emptyList();
            return emptyList;
          }
          Question question = ((IModel<EditedQuestion>) CategoryListPanel.this.getDefaultModel()).getObject().getElement();
          List<String> questionCatNames = new ArrayList<String>(question.getCategories().size());
          for(Category category : question.getCategories()) {
            questionCatNames.add(category.getName());
          }
          List<CategoryWithQuestions> choices = new ArrayList<CategoryWithQuestions>(AUTO_COMPLETE_SIZE);
          for(Category category : questionnaireCategories) {
            String name = category.getName();
            if(!questionCatNames.contains(name) && name.startsWith(input)) {
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
          if(checkIfCategoryAlreadyExists(((IModel<EditedQuestion>) CategoryListPanel.this.getDefaultModel()).getObject().getElement(), name)) {
            error(new StringResourceModel("CategoryAlreadyExists", CategoryListPanel.this, null).getObject());
            return;
          }
          Category category = categoryWithQuestions.getCategory();
          addCategory(((IModel<EditedQuestion>) CategoryListPanel.this.getDefaultModel()).getObject().getElement(), category);
          categoryNameFinder.setModelObject(null);

          target.addComponent(categoryNameFinder);
          target.addComponent(categoryList);
        }

        @Override
        protected void onError(AjaxRequestTarget target, Form<?> form1) {
          feedbackWindow.setContent(feedbackPanel);
          feedbackWindow.show(target);
        }
      };

      categoryNameFinder.add(new AttributeAppender("onkeypress", true, new Model<String>(buildPressEnterScript(addButton)), " "));
      addButton.add(new Image("img", Images.ADD).add(new AttributeModifier("title", true, new ResourceModel("Add"))));
      form.add(addButton);
    }

    private class CategoryWithQuestions implements Serializable {

      private Category category;

      private Collection<Question> questions;

      public CategoryWithQuestions(Category category, Collection<Question> questions) {
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
        return category.getName() + " (" + StringUtils.abbreviate(sb.toString(), 50) + ")";
      }
    }
  }

  private String buildPressEnterScript(AjaxButton addButton) {
    return "if (event.keyCode == 13) {document.getElementById('" + addButton.getMarkupId() + "').click(); return false;} else {return true;};";
  }

  private boolean checkIfCategoryAlreadyExists(Question question, String name) {
    for(QuestionCategory questionCategory : question.getQuestionCategories()) {
      if(equalsIgnoreCase(questionCategory.getName(), name) || equalsIgnoreCase(questionCategory.getCategory().getName(), name)) {
        return true; // category already exists
      }
    }
    return false;
  }

  private void addCategory(Question question, String name) {
    if(StringUtils.isNotBlank(name) && !checkIfCategoryAlreadyExists(question, name)) {
      addCategory(question, new Category(name));
    }
  }

  private void addCategory(final Question question, final Category category) {
    final QuestionCategory questionCategory = new QuestionCategory();
    questionCategory.setCategory(category);
    question.addQuestionCategory(questionCategory);

    localePropertiesUtils.load(localePropertiesModel.getObject(), questionnaireModel.getObject(), questionCategory);

    Collection<QuestionCategory> otherQuestionCategories = findOtherQuestionCategories(category, questionCategory);
    if(!otherQuestionCategories.isEmpty()) {
      QuestionCategory otherQuestionCategory = otherQuestionCategories.iterator().next();
      localePropertiesUtils.load(localePropertiesModel.getObject(), questionnaireModel.getObject(), otherQuestionCategory);

      ListMultimap<Locale, KeyValue> elementLabelsOtherQC = localePropertiesModel.getObject().getElementLabels(otherQuestionCategory);
      ListMultimap<Locale, KeyValue> elementLabelsQC = localePropertiesModel.getObject().getElementLabels(questionCategory);
      for(Locale locale : localePropertiesModel.getObject().getLocales()) {
        // we suppose that we have only one property in questionCategory : "label", then we use get(0)
        KeyValue kV = elementLabelsOtherQC.get(locale).get(0);
        elementLabelsQC.get(locale).get(0).setValue(kV.getValue());
      }
    }
  }

  /**
   * @param category
   * @param questionCategory
   * @return QuestionCategory which share category with only questionCategory, return null otherwise
   */
  private Collection<QuestionCategory> findOtherQuestionCategories(final Category category, final QuestionCategory questionCategory) {
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

  private synchronized void rollback(QuestionCategory modified, ElementClone<QuestionCategory> original) {
    Question question = ((EditedQuestion) getDefaultModelObject()).getElement();
    int index = question.getQuestionCategories().indexOf(modified);
    question.removeQuestionCategory(modified);
    question.addQuestionCategory(original.getElement(), index);
    QuestionnaireElementCloner.copy(original.getElement().getCategory(), modified.getCategory(), new CloneSettings(true, false, false, false));
    original.getElement().setCategory(modified.getCategory());
    Questionnaire questionnaire = questionnaireModel.getObject();
    localePropertiesUtils.remove(localePropertiesModel.getObject(), questionnaire, modified, modified.getCategory());
    QuestionnaireElementCloner.addProperties(original, localePropertiesModel.getObject());
  }
}