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

import static org.apache.commons.lang.StringUtils.equalsIgnoreCase;
import static org.obiba.onyx.quartz.core.wicket.layout.impl.util.QuestionCategoryListToGridPermutator.ROW_COUNT_KEY;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AutoCompleteTextField;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.ajax.markup.html.tabs.AjaxTabbedPanel;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.form.SimpleFormComponentLabel;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.util.value.ValueMap;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.validator.AbstractValidator;
import org.apache.wicket.validation.validator.StringValidator.MaximumLengthValidator;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireElementComparator;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireFinder;
import org.obiba.onyx.quartz.core.wicket.layout.impl.util.ListToGridPermutator;
import org.obiba.onyx.quartz.editor.question.EditedQuestion;
import org.obiba.onyx.quartz.editor.widget.sortable.SortableList;
import org.obiba.onyx.wicket.Images;
import org.obiba.onyx.wicket.reusable.FeedbackWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
@SuppressWarnings("serial")
public class CategoriesPanel extends Panel {

  private static final String SINGLE_COLUMN_LAYOUT = "singleColumnLayout";

  private static final String GRID_LAYOUT = "gridLayout";

  private final transient Logger logger = LoggerFactory.getLogger(getClass());

  private final ModalWindow categoryWindow;

  private final FeedbackPanel feedbackPanel;

  private final FeedbackWindow feedbackWindow;

  private SortableList<QuestionCategory> categoryList;

  private List<Category> questionnaireCategories;

  private List<Category> sharedCategories;

  private Map<String, Category> categoriesByName;

  public CategoriesPanel(String id, final IModel<EditedQuestion> model, final IModel<Questionnaire> questionnaireModel, FeedbackPanel feedbackPanel, FeedbackWindow feedbackWindow) {
    super(id, model);
    this.feedbackPanel = feedbackPanel;
    this.feedbackWindow = feedbackWindow;

    Question question = model.getObject().getElement();

    QuestionnaireFinder questionnaireFinder = QuestionnaireFinder.getInstance(questionnaireModel.getObject());
    questionnaireCategories = questionnaireFinder.findCategories();
    Collections.sort(questionnaireCategories, new QuestionnaireElementComparator());

    categoriesByName = new HashMap<String, Category>();
    for(Category category : questionnaireCategories) {
      categoriesByName.put(category.getName(), category);
    }

    sharedCategories = questionnaireFinder.findSharedCategories();

    add(CSSPackageResource.getHeaderContribution(CategoriesPanel.class, "CategoriesPanel.css"));

    categoryWindow = new ModalWindow("categoryWindow");
    categoryWindow.setCssClassName("onyx");
    categoryWindow.setInitialWidth(950);
    categoryWindow.setInitialHeight(550);
    categoryWindow.setResizable(true);
    categoryWindow.setTitle(new ResourceModel("Category"));
    add(categoryWindow);

    // radio group without default selection
    String layoutValue = null;
    ValueMap uiArgumentsValueMap = question.getUIArgumentsValueMap();
    Integer nbRows = ListToGridPermutator.DEFAULT_ROW_COUNT;
    if(uiArgumentsValueMap != null && uiArgumentsValueMap.containsKey(ROW_COUNT_KEY)) {
      layoutValue = Integer.parseInt((String) uiArgumentsValueMap.get(ROW_COUNT_KEY)) == question.getCategories().size() ? SINGLE_COLUMN_LAYOUT : GRID_LAYOUT;
      nbRows = uiArgumentsValueMap.getInt(ROW_COUNT_KEY);
    }

    RadioGroup<String> layout = new RadioGroup<String>("layout", new Model<String>(uiArgumentsValueMap == null ? null : layoutValue));
    layout.setLabel(new ResourceModel("Layout"));
    layout.setRequired(true);
    add(layout);

    Radio<String> singleColumnLayout = new Radio<String>(SINGLE_COLUMN_LAYOUT, new Model<String>(SINGLE_COLUMN_LAYOUT));
    singleColumnLayout.setLabel(new ResourceModel("Layout.single"));
    layout.add(singleColumnLayout);
    layout.add(new SimpleFormComponentLabel("singleColumnLayoutLabel", singleColumnLayout));

    Radio<String> gridLayout = new Radio<String>(GRID_LAYOUT, new Model<String>(GRID_LAYOUT));
    gridLayout.setLabel(new ResourceModel("Layout.grid"));
    layout.add(gridLayout);
    layout.add(new SimpleFormComponentLabel("gridLayoutLabel", gridLayout));

    TextField<Integer> nbRowsField = new TextField<Integer>("nbRows", new Model<Integer>(nbRows), Integer.class);
    gridLayout.setLabel(new ResourceModel("NbRows"));
    add(nbRowsField);

    final IModel<String> addCategoryModel = new Model<String>();

    List<ITab> tabs = new ArrayList<ITab>();
    tabs.add(new AbstractTab(new ResourceModel("Add.simple")) {
      @Override
      public Panel getPanel(String panelId) {
        return new SimpleAddPanel(panelId, addCategoryModel);
      }
    });
    tabs.add(new AbstractTab(new ResourceModel("Add.bulk")) {
      @Override
      public Panel getPanel(String panelId) {
        return new BulkAddPanel(panelId, addCategoryModel);
      }
    });
    add(new AjaxTabbedPanel("addTabs", tabs));

    categoryList = new SortableList<QuestionCategory>("categories", question.getQuestionCategories()) {

      @Override
      public Component getItemTitle(@SuppressWarnings("hiding") String id, QuestionCategory questionCategory) {
        return new Label(id, questionCategory.getName());
      }

      @Override
      public void editItem(QuestionCategory questionCategory, AjaxRequestTarget target) {
        categoryWindow.setContent(new CategoryWindow("content", new Model<QuestionCategory>(questionCategory), questionnaireModel, categoryWindow) {
          @Override
          public void onSave(AjaxRequestTarget target1, EditedQuestionCategory editedCategory) {
            super.onSave(target1, editedCategory);
            refreshList(target1);
          }
        });
        categoryWindow.show(target);
      }

      @Override
      @SuppressWarnings("unchecked")
      public void deleteItem(QuestionCategory questionCategory, AjaxRequestTarget target) {
        ((IModel<EditedQuestion>) CategoriesPanel.this.getDefaultModel()).getObject().getElement().getQuestionCategories().remove(questionCategory);
        refreshList(target);
      }

      @Override
      public Button[] getButtons() {
        return null;
      }

    };
    add(categoryList);

  }

  public class SimpleAddPanel extends Panel {

    private static final int AUTO_COMPLETE_SIZE = 15;

    public SimpleAddPanel(String id, IModel<String> model) {
      super(id, model);
      Form<String> form = new Form<String>("form", model);
      add(form);

      final AutoCompleteTextField<String> categoryName = new AutoCompleteTextField<String>("category", model) {
        @Override
        protected Iterator<String> getChoices(String input) {
          if(StringUtils.isBlank(input)) {
            List<String> emptyList = Collections.emptyList();
            return emptyList.iterator();
          }
          @SuppressWarnings("unchecked")
          Question question = ((IModel<EditedQuestion>) CategoriesPanel.this.getDefaultModel()).getObject().getElement();
          List<String> questionCatNames = new ArrayList<String>(question.getCategories().size());
          for(Category category : question.getCategories()) {
            questionCatNames.add(category.getName().toUpperCase());
          }
          List<String> choices = new ArrayList<String>(AUTO_COMPLETE_SIZE);
          for(Category category : questionnaireCategories) {
            String name = category.getName().toUpperCase();
            if(!questionCatNames.contains(name) && name.startsWith(input.toUpperCase())) {
              choices.add(name);
              if(choices.size() == AUTO_COMPLETE_SIZE) break;
            }
          }
          return choices.iterator();
        }
      };
      categoryName.setOutputMarkupId(true);
      categoryName.setLabel(new ResourceModel("NewCategory"));
      categoryName.add(new MaximumLengthValidator(20));
      categoryName.add(new AbstractValidator<String>() {
        @Override
        @SuppressWarnings("unchecked")
        protected void onValidate(IValidatable<String> validatable) {
          String name = validatable.getValue();
          if(StringUtils.isBlank(name)) return;
          if(checkIfCategoryAlreadyExists(((IModel<EditedQuestion>) CategoriesPanel.this.getDefaultModel()).getObject().getElement(), name)) {
            error(validatable, "CategoryAlreadyExists");
          }
        }
      });
      form.add(categoryName);

      form.add(new SimpleFormComponentLabel("categoryLabel", categoryName));
      AjaxSubmitLink simpelAddLink = new AjaxSubmitLink("link", form) {
        @Override
        @SuppressWarnings("unchecked")
        protected void onSubmit(AjaxRequestTarget target, Form<?> form1) {
          addCategory(((IModel<EditedQuestion>) CategoriesPanel.this.getDefaultModel()).getObject().getElement(), categoryName.getModelObject());
          categoryName.setModelObject(null);
          target.addComponent(categoryName);
          target.addComponent(categoryList);
        }

        @Override
        protected void onError(AjaxRequestTarget target, Form<?> form1) {
          feedbackWindow.setContent(feedbackPanel);
          feedbackWindow.show(target);
        }
      };

      simpelAddLink.add(Images.getAddImage("img").add(new AttributeModifier("title", true, new ResourceModel("Add"))));
      form.add(simpelAddLink);
    }
  }

  public class BulkAddPanel extends Panel {

    public BulkAddPanel(String id, IModel<String> model) {
      super(id, model);
      Form<String> form = new Form<String>("form", model);
      add(form);
      final TextArea<String> categories = new TextArea<String>("categories", model);
      categories.setOutputMarkupId(true);
      categories.setLabel(new ResourceModel("NewCategories"));
      form.add(categories);
      form.add(new SimpleFormComponentLabel("categoriesLabel", categories));
      AjaxSubmitLink bulkAddLink = new AjaxSubmitLink("bulkAddLink") {
        @Override
        @SuppressWarnings("unchecked")
        protected void onSubmit(AjaxRequestTarget target, Form<?> form1) {
          String[] names = StringUtils.split(categories.getModelObject(), ',');
          if(names == null) return;

          Question question = ((IModel<EditedQuestion>) CategoriesPanel.this.getDefaultModel()).getObject().getElement();
          for(String name : new HashSet<String>(Arrays.asList(names))) {
            addCategory(question, name);
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
      bulkAddLink.add(Images.getAddImage("bulkAddImg").add(new AttributeModifier("title", true, new ResourceModel("Add"))));
      form.add(bulkAddLink);
    }
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
    if(!checkIfCategoryAlreadyExists(question, name)) {
      Category category = categoriesByName.containsKey(name) ? categoriesByName.get(name) : new Category(name);
      QuestionCategory questionCategory = new QuestionCategory();
      questionCategory.setCategory(category);
      question.addQuestionCategory(questionCategory);
    }
  }

}
