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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormChoiceComponentUpdatingBehavior;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.form.SimpleFormComponentLabel;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.obiba.core.domain.IEntity;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireFinder;
import org.obiba.onyx.wicket.reusable.FeedbackWindow;
import org.obiba.wicket.markup.html.table.EntityListTablePanel;
import org.obiba.wicket.markup.html.table.IColumnProvider;

/**
 *
 */
@SuppressWarnings("serial")
public abstract class CategoryFinderPanel extends Panel {

  private final IModel<Questionnaire> questionnaireModel;

  private TextField<String> questionName;

  private TextField<String> categoryName;

  public CategoryFinderPanel(String id, final IModel<Question> questionModel, IModel<Questionnaire> questionnaireModel, final ModalWindow modalWindow) {
    super(id, questionModel);
    this.questionnaireModel = questionnaireModel;

    final RadioGroup<String> radioGroup = new RadioGroup<String>("finderType", new Model<String>());
    radioGroup.setOutputMarkupId(true);
    add(radioGroup);

    final Radio<String> newCategoryRadio = new Radio<String>("newCategory", new Model<String>("newCategory"));
    newCategoryRadio.setLabel(new ResourceModel("NewCategory"));
    radioGroup.add(newCategoryRadio);
    radioGroup.add(new SimpleFormComponentLabel("newCategoryLabel", newCategoryRadio));

    final Radio<String> categoryRadio = new Radio<String>("categoryFinder", new Model<String>("category"));
    categoryRadio.setLabel(new ResourceModel("FindByCategory"));
    radioGroup.add(categoryRadio);
    radioGroup.add(new SimpleFormComponentLabel("categoryFinderLabel", categoryRadio));

    categoryName = new TextField<String>("categoryName", new Model<String>());
    categoryName.setVisible(false);
    radioGroup.add(categoryName);

    final WebMarkupContainer categoryListContainer = new WebMarkupContainer("categoryListContainer");
    categoryListContainer.setOutputMarkupId(true);
    categoryListContainer.setVisible(false);
    // categoryListContainer.add(new OnyxEntityList<User>("categoryList", new CategoryProvider(), new
    // CategoryListColumnProvider(), new ResourceModel("Categories")));
    radioGroup.add(categoryListContainer);

    categoryName.add(new AjaxFormComponentUpdatingBehavior("onkeyup") {
      @Override
      protected void onUpdate(AjaxRequestTarget target) {
        target.addComponent(categoryListContainer);
      }
    });

    final Radio<String> questionRadio = new Radio<String>("questionFinder", new Model<String>("question"));
    questionRadio.setLabel(new ResourceModel("FindByQuestion"));
    radioGroup.add(new SimpleFormComponentLabel("questionFinderLabel", questionRadio));
    radioGroup.add(questionRadio);

    questionName = new TextField<String>("questionName", new Model<String>());
    questionName.setVisible(false);
    radioGroup.add(questionName);

    final WebMarkupContainer questionListContainer = new WebMarkupContainer("questionListContainer");
    questionListContainer.setOutputMarkupId(true);
    questionListContainer.setVisible(false);
    radioGroup.add(questionListContainer);

    final EntityListTablePanel<QuestionCategoryEntity> questionList = new EntityListTablePanel<QuestionCategoryEntity>("questionList", new QuestionProvider(), new QuestionListColumnProvider(), new ResourceModel("Questions"), 50);
    questionList.setDisplayRowSelectionColumn(true);
    questionListContainer.add(questionList);

    questionName.add(new AjaxFormComponentUpdatingBehavior("onkeyup") {
      @Override
      protected void onUpdate(AjaxRequestTarget target) {
        target.addComponent(questionList);
      }
    });

    radioGroup.add(new AjaxFormChoiceComponentUpdatingBehavior() {
      @Override
      protected void onUpdate(AjaxRequestTarget target) {
        String selection = radioGroup.getModelObject();
        boolean searchCategory = selection.equals(categoryRadio.getModelObject());
        boolean searchQuestion = selection.equals(questionRadio.getModelObject());
        categoryName.setVisible(searchCategory);
        categoryListContainer.setVisible(searchCategory);
        questionName.setVisible(searchQuestion);
        questionListContainer.setVisible(searchQuestion);
        target.addComponent(radioGroup);
      }
    });

    final FeedbackPanel feedbackPanel = new FeedbackPanel("content");
    final FeedbackWindow feedbackWindow = new FeedbackWindow("feedback");
    feedbackWindow.setOutputMarkupId(true);
    add(feedbackWindow);

    @SuppressWarnings("rawtypes")
    Form<?> form = new Form("form");
    add(form);
    form.add(new AjaxButton("save") {

      @Override
      public void onSubmit(AjaxRequestTarget target, Form<?> form2) {
        List<Category> categories = new ArrayList<Category>();
        for(IEntity selected : questionList.getSelectedEntities()) {
          categories.add(((QuestionCategoryEntity) selected).getQuestionCategory().getCategory());
        }
        modalWindow.close(target);
        onSave(target, categories);
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

  public abstract void onSave(AjaxRequestTarget target, List<Category> categories);

  private class QuestionProvider extends SortableDataProvider<QuestionCategoryEntity> {

    private String lastSearch;

    private List<String> questionNames;

    private Map<String, Question> questionCache;

    private List<QuestionCategoryEntity> questions = new ArrayList<QuestionCategoryEntity>();

    public QuestionProvider() {
      Questionnaire questionnaire = questionnaireModel.getObject();
      if(questionnaire.getQuestionnaireCache() == null) {
        QuestionnaireFinder.getInstance(questionnaire).buildQuestionnaireCache();
      }
      questionNames = new ArrayList<String>(questionnaire.getQuestionnaireCache().getQuestionCache().keySet());
      questionNames.remove(((Question) CategoryFinderPanel.this.getDefaultModelObject()).getName());
      questionCache = questionnaire.getQuestionnaireCache().getQuestionCache();
      Collections.sort(questionNames);
    }

    @Override
    public Iterator<QuestionCategoryEntity> iterator(int first, int count) {
      search();
      return questions.subList(first, Math.min(first + count, questions.size())).iterator();
    }

    @Override
    public int size() {
      search();
      return questions.size();
    }

    @Override
    public IModel<QuestionCategoryEntity> model(QuestionCategoryEntity questionCategory) {
      return new Model<QuestionCategoryEntity>(questionCategory);
    }

    private void search() {
      String query = questionName.getModelObject();
      if(StringUtils.isNotBlank(query) && !StringUtils.equalsIgnoreCase(lastSearch, query)) {
        questions.clear();
        for(String name : questionNames) {
          if(name.toLowerCase().startsWith(query.toLowerCase())) {
            Question question = (Question) CategoryFinderPanel.this.getDefaultModelObject();
            for(QuestionCategory questionCategory : questionCache.get(name).getQuestionCategories()) {
              if(!question.getQuestionCategories().contains(questionCategory)) {
                QuestionCategoryEntity categoryEntity = new QuestionCategoryEntity(questionCategory);
                if(!questions.contains(categoryEntity)) questions.add(categoryEntity);
              }
            }
          }
        }
        lastSearch = query;
      }
    }
  }

  private class QuestionListColumnProvider implements IColumnProvider<QuestionCategoryEntity>, Serializable {

    private final List<IColumn<QuestionCategoryEntity>> columns = new ArrayList<IColumn<QuestionCategoryEntity>>();

    public QuestionListColumnProvider() {
      columns.add(new PropertyColumn<QuestionCategoryEntity>(new StringResourceModel("Question", CategoryFinderPanel.this, null), "questionCategory.question.name"));
      columns.add(new PropertyColumn<QuestionCategoryEntity>(new StringResourceModel("Category", CategoryFinderPanel.this, null), "questionCategory.category.name"));
    }

    @Override
    public List<IColumn<QuestionCategoryEntity>> getAdditionalColumns() {
      return null;
    }

    @Override
    public List<String> getColumnHeaderNames() {
      return null;
    }

    @Override
    public List<IColumn<QuestionCategoryEntity>> getDefaultColumns() {
      return columns;
    }

    @Override
    public List<IColumn<QuestionCategoryEntity>> getRequiredColumns() {
      return columns;
    }
  }

  private class QuestionCategoryEntity implements IEntity {

    private final QuestionCategory questionCategory;

    public QuestionCategoryEntity(QuestionCategory questionCategory) {
      this.questionCategory = questionCategory;
    }

    @Override
    public Serializable getId() {
      return questionCategory.getCategory().getName();
    }

    @Override
    public void setId(Serializable id) {
    }

    @Override
    public int hashCode() {
      return getId().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
      return getId().equals(((IEntity) obj).getId());
    }

    public QuestionCategory getQuestionCategory() {
      return questionCategory;
    }

  }
}
