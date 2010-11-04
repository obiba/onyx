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

import org.apache.commons.lang.StringUtils;
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
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.validator.AbstractValidator;
import org.apache.wicket.validation.validator.StringValidator;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireFinder;
import org.obiba.onyx.quartz.editor.locale.LabelsPanel;
import org.obiba.onyx.quartz.editor.locale.LocaleProperties;
import org.obiba.onyx.quartz.editor.locale.LocalePropertiesUtils;
import org.obiba.onyx.quartz.editor.openAnswerDefinition.OpenAnswerWindow;
import org.obiba.onyx.quartz.editor.questionnaire.QuestionnairePersistenceUtils;
import org.obiba.onyx.quartz.editor.utils.MapModel;
import org.obiba.onyx.quartz.editor.widget.sortable.SortableList;
import org.obiba.onyx.wicket.Images;
import org.obiba.onyx.wicket.behavior.RequiredFormFieldBehavior;
import org.obiba.onyx.wicket.reusable.FeedbackWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

@SuppressWarnings("serial")
public abstract class CategoryWindow extends Panel {

  private final transient Logger log = LoggerFactory.getLogger(getClass());

  @SpringBean
  private transient QuestionnairePersistenceUtils questionnairePersistenceUtils;

  @SpringBean
  private transient LocalePropertiesUtils localePropertiesUtils;

  private final FeedbackPanel feedbackPanel;

  private final FeedbackWindow feedbackWindow;

  private final Form<QuestionCategory> form;

  private final IModel<Questionnaire> questionnaireModel;

  private final SortableList<OpenAnswerDefinition> openAnswerDefinitionList;

  private final IModel<LocaleProperties> localePropertiesModel;

  private final ModalWindow openAnswerWindow;

  public CategoryWindow(String id, final IModel<QuestionCategory> model, final IModel<Questionnaire> questionnaireModel, final IModel<LocaleProperties> localePropertiesModel, final ModalWindow modalWindow) {
    super(id, model);
    this.questionnaireModel = questionnaireModel;
    this.localePropertiesModel = localePropertiesModel == null ? new Model<LocaleProperties>(localePropertiesUtils.load(questionnaireModel.getObject(), model.getObject())) : localePropertiesModel;

    add(form = new Form<QuestionCategory>("form", model));

    feedbackPanel = new FeedbackPanel("content");
    feedbackWindow = new FeedbackWindow("feedback");
    feedbackWindow.setOutputMarkupId(true);

    add(feedbackWindow);

    QuestionCategory questionCategory = model.getObject();
    final Question question = questionCategory.getQuestion();
    final Category category = questionCategory.getCategory();

    openAnswerWindow = new ModalWindow("openAnswerWindow");
    openAnswerWindow.setCssClassName("onyx");
    openAnswerWindow.setInitialWidth(900);
    openAnswerWindow.setInitialHeight(500);
    openAnswerWindow.setResizable(true);
    openAnswerWindow.setTitle(new StringResourceModel("OpenAnswerDefinition", CategoryWindow.this, null));
    add(openAnswerWindow);

    TextField<String> name = new TextField<String>("name", new PropertyModel<String>(model, "category.name"));
    name.setLabel(new ResourceModel("Name"));
    name.add(new RequiredFormFieldBehavior());
    name.add(new AbstractValidator<String>() {
      @Override
      protected void onValidate(IValidatable<String> validatable) {
        if(!StringUtils.equalsIgnoreCase(model.getObject().getCategory().getName(), validatable.getValue())) {
          QuestionnaireFinder questionnaireFinder = QuestionnaireFinder.getInstance(questionnaireModel.getObject());
          if(questionnaireFinder.findCategory(validatable.getValue()) != null) {
            error(validatable, "CategoryAlreadyExists");
          }
        }
      }
    });
    form.add(name);
    form.add(new SimpleFormComponentLabel("nameLabel", name));

    TextField<String> variable = new TextField<String>("variable", new MapModel<String>(new PropertyModel<Map<String, String>>(model, "category.variableNames"), question.getName()));
    variable.setLabel(new ResourceModel("Variable"));
    variable.add(new StringValidator.MaximumLengthValidator(20));
    form.add(variable);
    form.add(new SimpleFormComponentLabel("variableLabel", variable));

    CheckBox escapeCheckBox = new CheckBox("escape", new PropertyModel<Boolean>(model, "category.escape"));
    escapeCheckBox.setLabel(new ResourceModel("EscapeOrMissing"));
    form.add(escapeCheckBox);
    form.add(new SimpleFormComponentLabel("escapeLabel", escapeCheckBox));

    CheckBox noAnswerCheckBox = new CheckBox("noAnswer", new PropertyModel<Boolean>(model, "category.noAnswer"));
    noAnswerCheckBox.setLabel(new ResourceModel("NoAnswer"));
    form.add(noAnswerCheckBox);
    form.add(new SimpleFormComponentLabel("noAnswerLabel", noAnswerCheckBox));

    localePropertiesUtils.load(this.localePropertiesModel.getObject(), questionnaireModel.getObject(), questionCategory, category);
    form.add(new LabelsPanel("labels", localePropertiesModel, model, feedbackPanel, feedbackWindow));

    LoadableDetachableModel<List<OpenAnswerDefinition>> openAnswerModel = new LoadableDetachableModel<List<OpenAnswerDefinition>>() {

      @Override
      protected List<OpenAnswerDefinition> load() {
        List<OpenAnswerDefinition> list = new ArrayList<OpenAnswerDefinition>();
        if(category.getOpenAnswerDefinition() != null) {
          if(CollectionUtils.isEmpty(category.getOpenAnswerDefinition().getOpenAnswerDefinitions())) {
            list.add(category.getOpenAnswerDefinition());
          } else {
            list.addAll(category.getOpenAnswerDefinition().getOpenAnswerDefinitions());
          }
        }
        return list;
      }
    };

    openAnswerDefinitionList = new SortableList<OpenAnswerDefinition>("openAnswerDefinitionList", openAnswerModel) {

      @Override
      public Component getItemTitle(@SuppressWarnings("hiding") String id, OpenAnswerDefinition openAnswer) {
        return new Label(id, openAnswer.getName());
      }

      @Override
      public void editItem(OpenAnswerDefinition openAnswer, AjaxRequestTarget target) {
        openAnswerWindow.setContent(new OpenAnswerWindow("content", new Model<OpenAnswerDefinition>(openAnswer), new Model<Question>(question), questionnaireModel, localePropertiesModel, openAnswerWindow) {
          @Override
          public void onSave(AjaxRequestTarget target1, OpenAnswerDefinition openAnswer1) {
            refreshList(target1);
          }
        });
        openAnswerWindow.show(target);
      }

      @Override
      public void deleteItem(OpenAnswerDefinition openAnswerToRemove, AjaxRequestTarget target) {
        OpenAnswerDefinition currentOpenAnswerDefinition = category.getOpenAnswerDefinition();
        if(!CollectionUtils.isEmpty(currentOpenAnswerDefinition.getOpenAnswerDefinitions())) {
          currentOpenAnswerDefinition.removeOpenAnswerDefinition(openAnswerToRemove);
          if(currentOpenAnswerDefinition.getOpenAnswerDefinitions().size() == 1) {
            OpenAnswerDefinition next = currentOpenAnswerDefinition.getOpenAnswerDefinitions().iterator().next();
            next.setParentOpenAnswerDefinition(null);
            category.setOpenAnswerDefinition(next);
          }
        } else {
          category.setOpenAnswerDefinition(null);
        }
        refreshList(target);
      }

      @Override
      @SuppressWarnings({ "rawtypes", "unchecked" })
      public SortableList<OpenAnswerDefinition>.Button[] getButtons() {
        SortableList<OpenAnswerDefinition>.Button addButton = new SortableList.Button(new ResourceModel("AddOpenAnswerDefinition"), Images.ADD) {

          @Override
          public void callback(AjaxRequestTarget target) {
            openAnswerWindow.setContent(new OpenAnswerWindow("content", new Model<OpenAnswerDefinition>(new OpenAnswerDefinition()), new Model<Question>(question), questionnaireModel, localePropertiesModel, openAnswerWindow) {
              @Override
              public void onSave(AjaxRequestTarget target1, OpenAnswerDefinition openAnswer) {
                OpenAnswerDefinition currentOpenAnswer = category.getOpenAnswerDefinition();
                if(currentOpenAnswer == null) {
                  category.setOpenAnswerDefinition(openAnswer);
                } else {
                  if(!CollectionUtils.isEmpty(currentOpenAnswer.getOpenAnswerDefinitions())) {
                    currentOpenAnswer.addOpenAnswerDefinition(openAnswer);
                  } else {
                    OpenAnswerDefinition newOpenAnswer = new OpenAnswerDefinition();
                    newOpenAnswer.setName(category.getName());
                    newOpenAnswer.addOpenAnswerDefinition(currentOpenAnswer);
                    newOpenAnswer.addOpenAnswerDefinition(openAnswer);
                    category.setOpenAnswerDefinition(newOpenAnswer);
                  }
                }
                refreshList(target1);
              }
            });
            openAnswerWindow.show(target);
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
   * @param questionCategory
   */
  public abstract void onSave(AjaxRequestTarget target, final QuestionCategory questionCategory);

}
