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
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow.CloseButtonCallback;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow.WindowClosedCallback;
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
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.validator.AbstractValidator;
import org.apache.wicket.validation.validator.PatternValidator;
import org.obiba.onyx.quartz.core.engine.questionnaire.IQuestionnaireElement;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireFinder;
import org.obiba.onyx.quartz.editor.QuartzEditorPanel;
import org.obiba.onyx.quartz.editor.behavior.VariableNameBehavior;
import org.obiba.onyx.quartz.editor.behavior.tooltip.HelpTooltipPanel;
import org.obiba.onyx.quartz.editor.locale.LabelsPanel;
import org.obiba.onyx.quartz.editor.locale.LocaleProperties;
import org.obiba.onyx.quartz.editor.locale.LocalePropertiesUtils;
import org.obiba.onyx.quartz.editor.openAnswer.OpenAnswerWindow;
import org.obiba.onyx.quartz.editor.utils.MapModel;
import org.obiba.onyx.quartz.editor.utils.QuestionnaireElementCloner;
import org.obiba.onyx.quartz.editor.utils.QuestionnaireElementCloner.CloneSettings;
import org.obiba.onyx.quartz.editor.utils.SaveCancelPanel;
import org.obiba.onyx.quartz.editor.widget.sortable.SortableList;
import org.obiba.onyx.wicket.Images;
import org.obiba.onyx.wicket.behavior.RequiredFormFieldBehavior;
import org.obiba.onyx.wicket.reusable.FeedbackWindow;
import org.springframework.util.CollectionUtils;

@SuppressWarnings("serial")
public abstract class CategoryWindow extends Panel {

  // private final transient Logger log = LoggerFactory.getLogger(getClass());

  @SpringBean
  private LocalePropertiesUtils localePropertiesUtils;

  private final FeedbackPanel feedbackPanel;

  private final FeedbackWindow feedbackWindow;

  private final Form<QuestionCategory> form;

  private final SortableList<OpenAnswerDefinition> openAnswerDefinitionList;

  private final ModalWindow openAnswerWindow;

  private final VariableNameBehavior variableNameBehavior;

  private final IModel<Questionnaire> questionnaireModel;

  private final IModel<LocaleProperties> localePropertiesModel;

  public CategoryWindow(String id, final IModel<QuestionCategory> model, final IModel<Questionnaire> questionnaireModel, final IModel<LocaleProperties> localePropertiesModel, final ModalWindow modalWindow) {
    super(id, model);
    this.questionnaireModel = questionnaireModel;
    this.localePropertiesModel = localePropertiesModel;

    add(form = new Form<QuestionCategory>("form", model));

    feedbackPanel = new FeedbackPanel("content");
    feedbackWindow = new FeedbackWindow("feedback");
    feedbackWindow.setOutputMarkupId(true);

    add(feedbackWindow);

    QuestionCategory questionCategory = model.getObject();
    final Question question = questionCategory.getQuestion();
    final Category category = questionCategory.getCategory();

    Questionnaire questionnaire = questionnaireModel.getObject();
    final QuestionnaireFinder questionnaireFinder = QuestionnaireFinder.getInstance(questionnaire);
    // questionnaireFinder.buildQuestionnaireCache();
    // StringBuilder sharedWithQuestions = new StringBuilder();
    // List<Category> sharedCategories = questionnaireFinder.findSharedCategories();
    // for(QuestionCategory qc : questionnaire.getQuestionnaireCache().getQuestionCategoryCache().values()) {
    // if(sharedCategories.contains(qc.getCategory())) {
    // if(sharedWithQuestions.length() > 0) sharedWithQuestions.append(", ");
    // sharedWithQuestions.append(qc.getQuestion().getName());
    // }
    // }
    // add(new Label("sharedWith", new StringResourceModel("sharedWith", this, null, new Object[] {
    // sharedWithQuestions.toString() })).setVisible(sharedWithQuestions.length() > 0));

    openAnswerWindow = new ModalWindow("openAnswerWindow");
    openAnswerWindow.setCssClassName("onyx");
    openAnswerWindow.setInitialWidth(900);
    openAnswerWindow.setInitialHeight(500);
    openAnswerWindow.setResizable(true);
    openAnswerWindow.setTitle(new ResourceModel("OpenAnswerDefinition"));
    add(openAnswerWindow);

    TextField<String> name = new TextField<String>("name", new PropertyModel<String>(model, "category.name"));
    name.setLabel(new ResourceModel("Name"));
    name.add(new RequiredFormFieldBehavior());
    name.add(new PatternValidator(QuartzEditorPanel.ELEMENT_NAME_PATTERN));
    name.add(new AbstractValidator<String>() {
      @Override
      protected void onValidate(IValidatable<String> validatable) {
        if(!StringUtils.equalsIgnoreCase(model.getObject().getCategory().getName(), validatable.getValue())) {
          if(questionnaireFinder.findCategory(validatable.getValue()) != null) {
            error(validatable, "CategoryAlreadyExists");
          }
        }
      }
    });
    form.add(name).add(new SimpleFormComponentLabel("nameLabel", name));
    form.add(new HelpTooltipPanel("nameHelp", new ResourceModel("Name.Tooltip")));

    final TextField<String> variable = new TextField<String>("variable", new MapModel<String>(new PropertyModel<Map<String, String>>(model, "category.variableNames"), question.getName()));
    variable.setLabel(new ResourceModel("Variable"));
    form.add(variable).add(new SimpleFormComponentLabel("variableLabel", variable));
    form.add(new HelpTooltipPanel("variableHelp", new ResourceModel("Variable.Tooltip")));

    add(variableNameBehavior = new VariableNameBehavior(name, variable, question.getParentQuestion(), question, null));

    CheckBox escapeCheckBox = new CheckBox("escape", new PropertyModel<Boolean>(model, "category.escape"));
    escapeCheckBox.setLabel(new ResourceModel("EscapeOrMissing"));
    form.add(escapeCheckBox).add(new SimpleFormComponentLabel("escapeLabel", escapeCheckBox));
    form.add(new HelpTooltipPanel("escapeHelp", new ResourceModel("EscapeOrMissing.Tooltip")));

    CheckBox noAnswerCheckBox = new CheckBox("noAnswer", new PropertyModel<Boolean>(model, "category.noAnswer"));
    noAnswerCheckBox.setLabel(new ResourceModel("NoAnswer"));
    form.add(noAnswerCheckBox).add(new SimpleFormComponentLabel("noAnswerLabel", noAnswerCheckBox));
    form.add(new HelpTooltipPanel("noAnswerHelp", new ResourceModel("NoAnswer.Tooltip")));

    IModel<? extends IQuestionnaireElement> editPropertyElement = null;
    if(questionnaireFinder.findSharedCategories().contains(category)) {
      editPropertyElement = new PropertyModel<Category>(model, "category");
    } else {
      editPropertyElement = model;
    }
    localePropertiesUtils.load(localePropertiesModel.getObject(), questionnaire, editPropertyElement.getObject());
    form.add(new LabelsPanel("labels", localePropertiesModel, editPropertyElement, feedbackPanel, feedbackWindow));

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
      public void editItem(final OpenAnswerDefinition openAnswer, AjaxRequestTarget target) {
        final OpenAnswerDefinition original = QuestionnaireElementCloner.cloneOpenAnswerDefinition(openAnswer, new CloneSettings(true));
        openAnswerWindow.setContent(new OpenAnswerWindow("content", new Model<OpenAnswerDefinition>(openAnswer), new Model<Category>(category), new Model<Question>(question), questionnaireModel, localePropertiesModel, openAnswerWindow) {
          @Override
          protected void onSave(@SuppressWarnings("hiding") AjaxRequestTarget target, @SuppressWarnings("hiding") OpenAnswerDefinition openAnswer) {
          }

          @Override
          protected void onCancel(@SuppressWarnings("hiding") AjaxRequestTarget target, @SuppressWarnings("hiding") OpenAnswerDefinition openAnswer) {
            rollback(openAnswer, original);
          }
        });
        openAnswerWindow.setCloseButtonCallback(new CloseButtonCallback() {
          @Override
          public boolean onCloseButtonClicked(@SuppressWarnings("hiding") AjaxRequestTarget target) {
            rollback(openAnswer, original);
            return true;
          }
        });
        openAnswerWindow.setWindowClosedCallback(new WindowClosedCallback() {
          @Override
          public void onClose(@SuppressWarnings("hiding") AjaxRequestTarget target) {
            refreshList(target);
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
            openAnswerWindow.setContent(new OpenAnswerWindow("content", new Model<OpenAnswerDefinition>(new OpenAnswerDefinition()), new Model<Category>(category), new Model<Question>(question), questionnaireModel, localePropertiesModel, openAnswerWindow) {
              @Override
              protected void onSave(@SuppressWarnings("hiding") AjaxRequestTarget target, OpenAnswerDefinition openAnswer) {
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
              }

              @Override
              protected void onCancel(@SuppressWarnings("hiding") AjaxRequestTarget target, OpenAnswerDefinition openAnswer) {
                // no special rollback to do for add
              }
            });
            openAnswerWindow.setCloseButtonCallback(new CloseButtonCallback() {
              @Override
              public boolean onCloseButtonClicked(@SuppressWarnings("hiding") AjaxRequestTarget target) {
                // no special rollback to do for add
                return true;
              }
            });
            openAnswerWindow.setWindowClosedCallback(new WindowClosedCallback() {
              @Override
              public void onClose(@SuppressWarnings("hiding") AjaxRequestTarget target) {
                refreshList(target);
              }
            });
            openAnswerWindow.show(target);
          }
        };
        return new SortableList.Button[] { addButton };
      }
    };

    form.add(openAnswerDefinitionList);

    form.add(new SaveCancelPanel("saveCancel", form) {
      @Override
      protected void onSave(AjaxRequestTarget target, Form<?> form1) {
        if(!variableNameBehavior.isVariableNameDefined()) {
          variable.setModelObject(null);
        }
        CategoryWindow.this.onSave(target, form.getModelObject());
        modalWindow.close(target);
      }

      @Override
      protected void onCancel(AjaxRequestTarget target, Form<?> form1) {
        CategoryWindow.this.onCancel(target, form.getModelObject());
        modalWindow.close(target);
      }

      @Override
      protected void onError(AjaxRequestTarget target, @SuppressWarnings("hiding") Form<?> form) {
        feedbackWindow.setContent(feedbackPanel);
        feedbackWindow.show(target);
      }
    });

  }

  private synchronized void rollback(OpenAnswerDefinition modified, OpenAnswerDefinition original) {
    Category category = ((QuestionCategory) getDefaultModelObject()).getCategory();
    if(category.getOpenAnswerDefinition().equals(modified)) { // parent open answer
      category.setOpenAnswerDefinition(original);
      for(OpenAnswerDefinition child : modified.getOpenAnswerDefinitions()) {
        original.addOpenAnswerDefinition(child);
      }
    } else {
      int index = category.getOpenAnswerDefinition().getOpenAnswerDefinitions().indexOf(modified);
      category.getOpenAnswerDefinition().removeOpenAnswerDefinition(modified);
      category.getOpenAnswerDefinition().addOpenAnswerDefinition(original, index);
    }
    Questionnaire questionnaire = questionnaireModel.getObject();
    LocaleProperties localeProperties = localePropertiesModel.getObject();
    localePropertiesUtils.remove(localeProperties, questionnaire, modified);
    localePropertiesUtils.load(localeProperties, questionnaire, original, original);
    QuestionnaireFinder.getInstance(questionnaire).buildQuestionnaireCache();
  }

  public abstract void onSave(AjaxRequestTarget target, final QuestionCategory questionCategory);

  public abstract void onCancel(AjaxRequestTarget target, final QuestionCategory questionCategory);

}