package org.obiba.onyx.quartz.editor.openAnswer;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinitionAudio;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinitionSuggestion;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.editor.locale.LocaleProperties;
import org.obiba.onyx.quartz.editor.utils.QuestionnaireElementCloner;
import org.obiba.onyx.quartz.editor.widget.sortable.SortableList;
import org.obiba.onyx.wicket.Images;
import org.obiba.onyx.wicket.reusable.FeedbackWindow;
import org.springframework.util.CollectionUtils;

public class OpenAnswerListPanel extends Panel {

  private final ModalWindow openAnswerWindow;

  public OpenAnswerListPanel(String id, IModel<QuestionCategory> model,
      final IModel<Questionnaire> questionnaireModel, final IModel<LocaleProperties> localePropertiesModel,
      FeedbackPanel feedbackPanel, FeedbackWindow feedbackWindow) {
    super(id);

    openAnswerWindow = new ModalWindow("openAnswerWindow");
    openAnswerWindow.setCssClassName("onyx");
    openAnswerWindow.setInitialWidth(900);
    openAnswerWindow.setInitialHeight(500);
    openAnswerWindow.setResizable(true);
    openAnswerWindow.setTitle(new ResourceModel("OpenAnswerDefinition"));
    add(openAnswerWindow);

    final Question question = model.getObject().getQuestion();
    final Category category = model.getObject().getCategory();

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
        final QuestionnaireElementCloner.ElementClone<OpenAnswerDefinition> original = QuestionnaireElementCloner
            .clone(openAnswer, new QuestionnaireElementCloner.CloneSettings(true), localePropertiesModel.getObject());
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

        openAnswerWindow.setCloseButtonCallback(new ModalWindow.CloseButtonCallback() {
          @Override
          public boolean onCloseButtonClicked(AjaxRequestTarget target) {
            OpenAnswerUtils
                .rollback(openAnswer, original, localePropertiesModel.getObject(), questionnaireModel.getObject(),
                    category);
            return true;
          }
        });
        openAnswerWindow.setWindowClosedCallback(new ModalWindow.WindowClosedCallback() {
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
            openAnswerWindow.setCloseButtonCallback(new ModalWindow.CloseButtonCallback() {
              @Override
              public boolean onCloseButtonClicked(@SuppressWarnings("hiding") AjaxRequestTarget target) {
                // no special rollback to do for add
                return true;
              }
            });
            openAnswerWindow.setWindowClosedCallback(new ModalWindow.WindowClosedCallback() {
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
            openAnswerWindow.setCloseButtonCallback(new ModalWindow.CloseButtonCallback() {
              @Override
              public boolean onCloseButtonClicked(@SuppressWarnings("hiding") AjaxRequestTarget target) {
                // no special rollback to do for add
                return true;
              }
            });
            openAnswerWindow.setWindowClosedCallback(new ModalWindow.WindowClosedCallback() {
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
            openAnswerWindow.setCloseButtonCallback(new ModalWindow.CloseButtonCallback() {
              @Override
              public boolean onCloseButtonClicked(@SuppressWarnings("hiding") AjaxRequestTarget target) {
                // no special rollback to do for add
                return true;
              }
            });
            openAnswerWindow.setWindowClosedCallback(new ModalWindow.WindowClosedCallback() {
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

    add(openAnswerDefinitionList);

  }
}
