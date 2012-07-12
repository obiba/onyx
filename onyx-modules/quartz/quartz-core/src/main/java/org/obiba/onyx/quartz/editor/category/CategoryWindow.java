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
import org.apache.wicket.extensions.ajax.markup.html.tabs.AjaxTabbedPanel;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
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
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinitionAudio;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinitionSuggestion;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.editor.QuartzEditorPanel;
import org.obiba.onyx.quartz.editor.behavior.VariableNameBehavior;
import org.obiba.onyx.quartz.editor.behavior.tooltip.HelpTooltipPanel;
import org.obiba.onyx.quartz.editor.locale.LabelsPanel;
import org.obiba.onyx.quartz.editor.locale.LocaleProperties;
import org.obiba.onyx.quartz.editor.locale.LocalePropertiesUtils;
import org.obiba.onyx.quartz.editor.openAnswer.OpenAnswerUtils;
import org.obiba.onyx.quartz.editor.openAnswer.OpenAnswerWindow;
import org.obiba.onyx.quartz.editor.utils.MapModel;
import org.obiba.onyx.quartz.editor.utils.QuestionnaireElementCloner;
import org.obiba.onyx.quartz.editor.utils.QuestionnaireElementCloner.CloneSettings;
import org.obiba.onyx.quartz.editor.utils.QuestionnaireElementCloner.ElementClone;
import org.obiba.onyx.quartz.editor.utils.SaveCancelPanel;
import org.obiba.onyx.quartz.editor.widget.attributes.AttributesPanel;
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

  private final ModalWindow openAnswerWindow;

  private final VariableNameBehavior variableNameBehavior;

  private final List<String> otherCategoryNames = new ArrayList<String>();

  private AjaxTabbedPanel tabbedPanel;

  public CategoryWindow(String id, final IModel<QuestionCategory> model, final IModel<Questionnaire> questionnaireModel,
      final IModel<LocaleProperties> localePropertiesModel, final ModalWindow modalWindow) {
    super(id, model);

    List<ITab> tabs = new ArrayList<ITab>();
    ITab tab = new AbstractTab(new ResourceModel("Attributes")) {
      @Override
      public Panel getPanel(String panelId) {
        return new AttributesPanel(panelId, new Model(model.getObject().getCategory()),
            questionnaireModel.getObject().getLocales(),
            feedbackPanel,
            feedbackWindow);
      }
    };
    tabs.add(tab);

    form = new Form<QuestionCategory>("form", model);
    new AbstractTab(new ResourceModel("Category")) {
      @Override
      public Panel getPanel(String panelId) {
        Panel panel = new Panel(panelId);
        return panel;
      }
    };

    tabbedPanel = new AjaxTabbedPanel("categoryTabs", tabs);
    add(tabbedPanel);
    add(form);

    form.setMultiPart(false);

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
    openAnswerWindow.setTitle(new ResourceModel("OpenAnswerDefinition"));
    add(openAnswerWindow);

    String initialName = questionCategory.getCategory().getName();
    TextField<String> name = new TextField<String>("name", new PropertyModel<String>(model, "category.name"));
    name.setLabel(new ResourceModel("Name"));
    name.add(new RequiredFormFieldBehavior());
    name.add(new PatternValidator(QuartzEditorPanel.ELEMENT_NAME_PATTERN));

    for(Category otherCategory : question.getCategories()) {
      if(!otherCategory.getName().equals(initialName)) {
        otherCategoryNames.add(otherCategory.getName());
      }
    }

    name.add(new AbstractValidator<String>() {

      @Override
      protected void onValidate(IValidatable<String> validatable) {

        // test if category doesn't already exist when renaming
        String value = validatable.getValue();
        for(String categoryName : otherCategoryNames) {
          if(StringUtils.equalsIgnoreCase(value, categoryName)) {
            error(validatable, "CategoryAlreadyExistsForThisQuestion");
          }
        }
      }
    });
    form.add(name).add(new SimpleFormComponentLabel("nameLabel", name));
    form.add(new HelpTooltipPanel("nameHelp", new ResourceModel("Name.Tooltip")));

    final TextField<String> variable = new TextField<String>("variable",
        new MapModel<String>(new PropertyModel<Map<String, String>>(model, "category.variableNames"),
            question.getName()));
    variable.setLabel(new ResourceModel("Variable"));
    form.add(variable).add(new SimpleFormComponentLabel("variableLabel", variable));
    form.add(new HelpTooltipPanel("variableHelp", new ResourceModel("Variable.Tooltip")));

    add(variableNameBehavior = new VariableNameBehavior(name, variable, question.getParentQuestion(), question, null));

    CheckBox escapeCheckBox = new CheckBox("escape", new PropertyModel<Boolean>(model, "category.escape"));
    escapeCheckBox.setLabel(new ResourceModel("EscapeOrMissing"));
    form.add(escapeCheckBox).add(new SimpleFormComponentLabel("escapeLabel", escapeCheckBox));
    form.add(new HelpTooltipPanel("escapeHelp", new ResourceModel("EscapeOrMissing.Tooltip")));

    localePropertiesUtils.load(localePropertiesModel.getObject(), questionnaireModel.getObject(), model.getObject());
    form.add(new LabelsPanel("labels", localePropertiesModel, model, feedbackPanel, feedbackWindow));

    LoadableDetachableModel<List<OpenAnswerDefinition>> openAnswerModel = new LoadableDetachableModel<List<OpenAnswerDefinition>>() {

      @Override
      protected List<OpenAnswerDefinition> load() {
        if(category.getOpenAnswerDefinition() != null) {
          if(CollectionUtils.isEmpty(category.getOpenAnswerDefinition().getOpenAnswerDefinitions())) {
            List<OpenAnswerDefinition> list = new ArrayList<OpenAnswerDefinition>();
            list.add(category.getOpenAnswerDefinition());
            return list;
          }
          return category.getOpenAnswerDefinition().getOpenAnswerDefinitions();
        }
        return new ArrayList<OpenAnswerDefinition>();
      }
    };

    SortableList<OpenAnswerDefinition> openAnswerDefinitionList = new SortableList<OpenAnswerDefinition>(
        "openAnswerDefinitionList", openAnswerModel) {

      @Override
      public Component getItemTitle(@SuppressWarnings("hiding") String id, OpenAnswerDefinition openAnswer) {
        return new Label(id, openAnswer.getName());
      }

      @Override
      public void editItem(final OpenAnswerDefinition openAnswer, AjaxRequestTarget target) {
        final ElementClone<OpenAnswerDefinition> original = QuestionnaireElementCloner
            .clone(openAnswer, new CloneSettings(true), localePropertiesModel.getObject());
        openAnswerWindow.setContent(
            new OpenAnswerWindow("content", new Model<OpenAnswerDefinition>(openAnswer), new Model<Category>(category),
                new Model<Question>(question), questionnaireModel, localePropertiesModel, openAnswerWindow) {
              @Override
              protected void onSave(@SuppressWarnings("hiding") AjaxRequestTarget target,
                  @SuppressWarnings("hiding") OpenAnswerDefinition openAnswer) {
              }

              @Override
              protected void onCancel(@SuppressWarnings("hiding") AjaxRequestTarget target,
                  @SuppressWarnings("hiding") OpenAnswerDefinition openAnswer) {
                OpenAnswerUtils
                    .rollback(openAnswer, original, localePropertiesModel.getObject(), questionnaireModel.getObject(),
                        category);
              }
            });

        openAnswerWindow.setCloseButtonCallback(new CloseButtonCallback() {
          @Override
          public boolean onCloseButtonClicked(AjaxRequestTarget target) {
            OpenAnswerUtils
                .rollback(openAnswer, original, localePropertiesModel.getObject(), questionnaireModel.getObject(),
                    category);
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
        localePropertiesModel.getObject().remove(questionnaireModel.getObject(), openAnswerToRemove);
        refreshList(target);
      }

      @Override
      @SuppressWarnings({"rawtypes", "unchecked"})
      public SortableList<OpenAnswerDefinition>.Button[] getButtons() {
        SortableList<OpenAnswerDefinition>.Button addButton = new SortableList.Button(
            new ResourceModel("AddOpenAnswerDefinition"), Images.ADD) {

          @Override
          public void callback(AjaxRequestTarget target) {
            OpenAnswerDefinition openAnswerDefinition = new OpenAnswerDefinition();
            openAnswerDefinition.setRequired(true);
            openAnswerWindow.setContent(
                new OpenAnswerWindow("content", new Model<OpenAnswerDefinition>(openAnswerDefinition),
                    new Model<Category>(category), new Model<Question>(question), questionnaireModel,
                    localePropertiesModel, openAnswerWindow) {
                  @Override
                  protected void onSave(@SuppressWarnings("hiding") AjaxRequestTarget target,
                      OpenAnswerDefinition openAnswer) {
                    OpenAnswerDefinition currentOpenAnswer = category.getOpenAnswerDefinition();
                    if(currentOpenAnswer == null) {
                      category.setOpenAnswerDefinition(openAnswer);
                    } else {
                      if(!CollectionUtils.isEmpty(currentOpenAnswer.getOpenAnswerDefinitions())) {
                        currentOpenAnswer.addOpenAnswerDefinition(openAnswer);
                      } else {
                        OpenAnswerDefinition newOpenAnswer = new OpenAnswerDefinition();
                        // TODO System.currentTimeMillis() to ensure unique name (other solution ?)
                        newOpenAnswer.setName(category.getName() + System.currentTimeMillis());
                        newOpenAnswer.setRequired(true);
                        newOpenAnswer.addOpenAnswerDefinition(currentOpenAnswer);
                        newOpenAnswer.addOpenAnswerDefinition(openAnswer);
                        category.setOpenAnswerDefinition(newOpenAnswer);
                      }
                    }
                  }

                  @Override
                  protected void onCancel(@SuppressWarnings("hiding") AjaxRequestTarget target,
                      OpenAnswerDefinition openAnswer) {
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

        SortableList<OpenAnswerDefinition>.Button addAutoCompleteButton = new SortableList.Button(
            new ResourceModel("AddAutoComplete"), Images.ADD) {

          @Override
          public void callback(AjaxRequestTarget target) {

            OpenAnswerDefinition openAnswerDefinition = OpenAnswerDefinitionSuggestion.createNewSuggestionOpenAnswer()
                .getOpenAnswerDefinition();
            openAnswerDefinition.setRequired(true);
            openAnswerWindow.setContent(
                new OpenAnswerWindow("content", new Model<OpenAnswerDefinition>(openAnswerDefinition),
                    new Model<Category>(category), new Model<Question>(question), questionnaireModel,
                    localePropertiesModel, openAnswerWindow) {
                  @Override
                  protected void onSave(@SuppressWarnings("hiding") AjaxRequestTarget target,
                      OpenAnswerDefinition openAnswer) {
                    OpenAnswerDefinition currentOpenAnswer = category.getOpenAnswerDefinition();
                    if(currentOpenAnswer == null) {
                      category.setOpenAnswerDefinition(openAnswer);
                    } else {
                      if(!CollectionUtils.isEmpty(currentOpenAnswer.getOpenAnswerDefinitions())) {
                        currentOpenAnswer.addOpenAnswerDefinition(openAnswer);
                      } else {
                        OpenAnswerDefinition newOpenAnswer = new OpenAnswerDefinition();
                        // TODO System.currentTimeMillis() to ensure unique name (other solution ?)
                        newOpenAnswer.setName(category.getName() + System.currentTimeMillis());
                        newOpenAnswer.setRequired(true);
                        newOpenAnswer.addOpenAnswerDefinition(currentOpenAnswer);
                        newOpenAnswer.addOpenAnswerDefinition(openAnswer);
                        category.setOpenAnswerDefinition(newOpenAnswer);
                      }
                    }
                  }

                  @Override
                  protected void onCancel(@SuppressWarnings("hiding") AjaxRequestTarget target,
                      OpenAnswerDefinition openAnswer) {
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

        SortableList<OpenAnswerDefinition>.Button addAudioButton = new SortableList.Button(
            new ResourceModel("AddAudioRecording"), Images.ADD) {

          @Override
          public void callback(AjaxRequestTarget target) {
            OpenAnswerDefinition openAnswerDefinition = OpenAnswerDefinitionAudio.createOpenAnswerDefinitionAudio()
                .getOpenAnswer();
            openAnswerDefinition.setRequired(true);
            openAnswerWindow.setContent(
                new OpenAnswerWindow("content", new Model<OpenAnswerDefinition>(openAnswerDefinition),
                    new Model<Category>(category), new Model<Question>(question), questionnaireModel,
                    localePropertiesModel, openAnswerWindow) {
                  @Override
                  protected void onSave(@SuppressWarnings("hiding") AjaxRequestTarget target,
                      OpenAnswerDefinition openAnswer) {
                    OpenAnswerDefinition currentOpenAnswer = category.getOpenAnswerDefinition();
                    if(currentOpenAnswer == null) {
                      category.setOpenAnswerDefinition(openAnswer);
                    } else {
                      if(!CollectionUtils.isEmpty(currentOpenAnswer.getOpenAnswerDefinitions())) {
                        currentOpenAnswer.addOpenAnswerDefinition(openAnswer);
                      } else {
                        OpenAnswerDefinition newOpenAnswer = new OpenAnswerDefinition();
                        // TODO System.currentTimeMillis() to ensure unique name (other solution ?)
                        newOpenAnswer.setName(category.getName() + System.currentTimeMillis());
                        newOpenAnswer.setRequired(true);
                        newOpenAnswer.addOpenAnswerDefinition(currentOpenAnswer);
                        newOpenAnswer.addOpenAnswerDefinition(openAnswer);
                        category.setOpenAnswerDefinition(newOpenAnswer);
                      }
                    }
                  }

                  @Override
                  protected void onCancel(@SuppressWarnings("hiding") AjaxRequestTarget target,
                      OpenAnswerDefinition openAnswer) {
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

        return new SortableList.Button[] {addButton, addAutoCompleteButton, addAudioButton};
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

  public abstract void onSave(AjaxRequestTarget target, QuestionCategory questionCategory);

  public abstract void onCancel(AjaxRequestTarget target, QuestionCategory questionCategory);

}
