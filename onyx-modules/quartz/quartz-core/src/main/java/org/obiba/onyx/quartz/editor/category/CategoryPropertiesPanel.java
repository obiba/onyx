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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.SimpleFormComponentLabel;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.validator.StringValidator;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireBuilder;
import org.obiba.onyx.quartz.editor.locale.model.LocaleProperties;
import org.obiba.onyx.quartz.editor.locale.ui.LocalesPropertiesAjaxTabbedPanel;
import org.obiba.onyx.quartz.editor.openAnswerDefinition.EditedOpenAnswerDefinition;
import org.obiba.onyx.quartz.editor.openAnswerDefinition.OpenAnswerDefinitionPropertiesPanel;
import org.obiba.onyx.quartz.editor.questionnaire.QuestionnairePersistenceUtils;
import org.obiba.onyx.quartz.editor.utils.LocalePropertiesUtils;
import org.obiba.onyx.quartz.editor.utils.MapModel;
import org.obiba.onyx.quartz.editor.widget.sortable.SortableList;
import org.obiba.onyx.quartz.editor.widget.sortable.SortableListCallback;
import org.obiba.onyx.wicket.behavior.RequiredFormFieldBehavior;
import org.obiba.onyx.wicket.reusable.FeedbackWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

@SuppressWarnings("serial")
public class CategoryPropertiesPanel extends Panel {

  private final transient Logger log = LoggerFactory.getLogger(getClass());

  @SpringBean
  private QuestionnairePersistenceUtils questionnairePersistenceUtils;

  @SpringBean
  private LocalePropertiesUtils localePropertiesUtils;

  private final FeedbackPanel feedbackPanel;

  private final FeedbackWindow feedbackWindow;

  private ModalWindow openAnswerDefinitionWindow;

  private final Form<EditedQuestionCategory> form;

  private final IModel<Questionnaire> questionnaireModel;

  private ListModel<LocaleProperties> localePropertiesModelQuestionCategory;

  private SortableList<OpenAnswerDefinition> openAnswerDefinitionList;

  public CategoryPropertiesPanel(String id, IModel<QuestionCategory> model, final IModel<Questionnaire> questionnaireModel, final ModalWindow modalWindow) {
    super(id);

    IModel<EditedQuestionCategory> editedModel = new Model<EditedQuestionCategory>(new EditedQuestionCategory(model.getObject()));
    setDefaultModel(editedModel);

    this.questionnaireModel = questionnaireModel;

    Question question = model.getObject().getQuestion();

    add(form = new Form<EditedQuestionCategory>("form", editedModel));
    final Category category = form.getModelObject().getElement().getCategory();

    localePropertiesModelQuestionCategory = new ListModel<LocaleProperties>(localePropertiesUtils.loadLocaleProperties(model, questionnaireModel));

    feedbackPanel = new FeedbackPanel("content");
    feedbackWindow = new FeedbackWindow("feedback");
    feedbackWindow.setOutputMarkupId(true);

    add(feedbackWindow);

    TextField<String> name = new TextField<String>("name", new PropertyModel<String>(form.getModel(), "element.category.name"));
    name.setLabel(new ResourceModel("Name"));
    name.add(new RequiredFormFieldBehavior());
    form.add(name);
    form.add(new SimpleFormComponentLabel("nameLabel", name));

    TextField<String> variable = new TextField<String>("variable", new MapModel<String>(new PropertyModel<Map<String, String>>(model, "category.variableNames"), question.getName()));
    variable.setLabel(new ResourceModel("Variable"));
    variable.add(new StringValidator.MaximumLengthValidator(20));
    form.add(variable);
    form.add(new SimpleFormComponentLabel("variableLabel", variable));

    CheckBox escapeCheckBox = new CheckBox("escape", new PropertyModel<Boolean>(form.getModel(), "element.category.escape"));
    escapeCheckBox.setLabel(new ResourceModel("EscapeOrMissing"));
    form.add(escapeCheckBox);
    form.add(new SimpleFormComponentLabel("escapeLabel", escapeCheckBox));

    CheckBox noAnswerCheckBox = new CheckBox("noAnswer", new PropertyModel<Boolean>(form.getModel(), "element.category.noAnswer"));
    noAnswerCheckBox.setLabel(new ResourceModel("NoAnswer"));
    form.add(noAnswerCheckBox);
    form.add(new SimpleFormComponentLabel("noAnswerLabel", noAnswerCheckBox));

    form.add(new LocalesPropertiesAjaxTabbedPanel("questionCategoryLocalesPropertiesTabs", new PropertyModel<QuestionCategory>(form.getModel(), "element"), localePropertiesModelQuestionCategory));

    openAnswerDefinitionWindow = new ModalWindow("openAnswerDefinitionWindow");
    openAnswerDefinitionWindow.setCssClassName("onyx");
    openAnswerDefinitionWindow.setInitialWidth(1000);
    openAnswerDefinitionWindow.setInitialHeight(600);
    openAnswerDefinitionWindow.setResizable(true);
    form.add(openAnswerDefinitionWindow);

    LoadableDetachableModel<List<OpenAnswerDefinition>> listOpenAnswerDefinitionsModel = new LoadableDetachableModel<List<OpenAnswerDefinition>>() {

      @Override
      protected List<OpenAnswerDefinition> load() {
        List<OpenAnswerDefinition> listOpenAnswerDefinitions = new ArrayList<OpenAnswerDefinition>();
        if(category.getOpenAnswerDefinition() != null) {
          if(CollectionUtils.isEmpty(category.getOpenAnswerDefinition().getOpenAnswerDefinitions())) {
            listOpenAnswerDefinitions.add(category.getOpenAnswerDefinition());
          } else {
            listOpenAnswerDefinitions.addAll(category.getOpenAnswerDefinition().getOpenAnswerDefinitions());
          }
        }
        return listOpenAnswerDefinitions;
      }
    };

    openAnswerDefinitionList = new SortableList<OpenAnswerDefinition>("openAnswerDefinitionList", listOpenAnswerDefinitionsModel) {

      @Override
      public Component getItemTitle(@SuppressWarnings("hiding") String id, OpenAnswerDefinition openAnswerDefinition) {
        return new Label(id, openAnswerDefinition.getName());
      }

      @Override
      public void editItem(OpenAnswerDefinition openAnswerDefinition, AjaxRequestTarget target) {
        openAnswerDefinitionWindow.setTitle(new ResourceModel("OpenAnswerDefinition"));
        openAnswerDefinitionWindow.setContent(new OpenAnswerDefinitionPropertiesPanel("content", new Model<OpenAnswerDefinition>(openAnswerDefinition), questionnaireModel, openAnswerDefinitionWindow) {
          @Override
          public void onSave(AjaxRequestTarget target1, EditedOpenAnswerDefinition editedOpenAnswerDefinition) {
            super.onSave(target1, editedOpenAnswerDefinition);
            refreshList(target1);
            persist(target1);
          }
        });
        openAnswerDefinitionWindow.show(target);
      }

      @Override
      public void deleteItem(OpenAnswerDefinition openAnswerDefinitionToRemove, AjaxRequestTarget target) {
        OpenAnswerDefinition currentOpenAnswerDefinition = category.getOpenAnswerDefinition();
        if(!CollectionUtils.isEmpty(currentOpenAnswerDefinition.getOpenAnswerDefinitions())) {
          currentOpenAnswerDefinition.removeOpenAnswerDefinition(openAnswerDefinitionToRemove);
          if(currentOpenAnswerDefinition.getOpenAnswerDefinitions().size() == 1) {
            OpenAnswerDefinition next = currentOpenAnswerDefinition.getOpenAnswerDefinitions().iterator().next();
            next.setParentOpenAnswerDefinition(null);
            category.setOpenAnswerDefinition(next);
          }
        } else {
          category.setOpenAnswerDefinition(null);
        }
        refreshList(target);
        persist(target);
      }

      @Override
      @SuppressWarnings({ "rawtypes", "unchecked" })
      public SortableList<OpenAnswerDefinition>.Button[] getButtons() {
        SortableList<OpenAnswerDefinition>.Button addButton = new SortableList.Button(new ResourceModel("AddOpenAnswerDefinition")) {

          @Override
          public void callback(AjaxRequestTarget target) {
            openAnswerDefinitionWindow.setContent(new OpenAnswerDefinitionPropertiesPanel("content", new Model<OpenAnswerDefinition>(new OpenAnswerDefinition()), questionnaireModel, openAnswerDefinitionWindow) {
              @Override
              public void onSave(AjaxRequestTarget target1, EditedOpenAnswerDefinition editedOpenAnswerDefinition) {
                super.onSave(target1, editedOpenAnswerDefinition);
                OpenAnswerDefinition currentOpenAnswerDefinition = category.getOpenAnswerDefinition();
                if(currentOpenAnswerDefinition == null) {
                  category.setOpenAnswerDefinition(editedOpenAnswerDefinition.getElement());
                } else {
                  if(!CollectionUtils.isEmpty(currentOpenAnswerDefinition.getOpenAnswerDefinitions())) {
                    currentOpenAnswerDefinition.addOpenAnswerDefinition(editedOpenAnswerDefinition.getElement());
                  } else {
                    OpenAnswerDefinition newOpenAnswerDefinition = new OpenAnswerDefinition();
                    newOpenAnswerDefinition.setName("whatNamePutHere");
                    newOpenAnswerDefinition.addOpenAnswerDefinition(currentOpenAnswerDefinition);
                    newOpenAnswerDefinition.addOpenAnswerDefinition(editedOpenAnswerDefinition.getElement());
                    category.setOpenAnswerDefinition(newOpenAnswerDefinition);
                  }
                }
                refreshList(target1);
                persist(target1);
              }
            });
            openAnswerDefinitionWindow.show(target);
          }
        };
        return new SortableList.Button[] { addButton };
      }
    };

    form.add(openAnswerDefinitionList);

    form.add(new AjaxButton("save", form) {
      @Override
      public void onSubmit(AjaxRequestTarget target, Form<?> form2) {
        onSave(target, form.getModelObject());
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
   * 
   * @param target
   * @param editedQuestionCategory
   */
  public void onSave(AjaxRequestTarget target, final EditedQuestionCategory editedQuestionCategory) {
    editedQuestionCategory.setLocalePropertiesWithNamingStrategy(localePropertiesModelQuestionCategory.getObject());

    openAnswerDefinitionList.save(target, new SortableListCallback<OpenAnswerDefinition>() {

      @Override
      public void onSave(List<OpenAnswerDefinition> orderedItems, AjaxRequestTarget target1) {
        OpenAnswerDefinition currentOpenAnswerDefinition = editedQuestionCategory.getElement().getOpenAnswerDefinition();
        if(currentOpenAnswerDefinition != null && !CollectionUtils.isEmpty(currentOpenAnswerDefinition.getOpenAnswerDefinitions())) {
          currentOpenAnswerDefinition.getOpenAnswerDefinitions().clear();
          for(OpenAnswerDefinition openAnswerDefinition : orderedItems) {
            currentOpenAnswerDefinition.addOpenAnswerDefinition(openAnswerDefinition);
          }
        }
      }
    });
  }

  public void persist(AjaxRequestTarget target) {
    try {
      QuestionnaireBuilder builder = questionnairePersistenceUtils.createBuilder(questionnaireModel.getObject());
      questionnairePersistenceUtils.persist(form.getModelObject(), builder);
    } catch(Exception e) {
      log.error("Cannot persist questionnaire", e);
      error(e.getMessage());
      feedbackWindow.setContent(feedbackPanel);
      feedbackWindow.show(target);
    }
  }
}
