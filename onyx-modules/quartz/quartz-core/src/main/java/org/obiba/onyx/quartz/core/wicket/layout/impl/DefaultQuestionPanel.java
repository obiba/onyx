/***********************************************************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 **********************************************************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.CheckGroup;
import org.apache.wicket.markup.html.form.FormComponentLabel;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.quartz.core.domain.answer.CategoryAnswer;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.quartz.core.wicket.layout.QuestionPanel;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireStringResourceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Support for question multiple or not, but without child questions.
 */
public class DefaultQuestionPanel extends QuestionPanel {

  private static final long serialVersionUID = 2951128797454847260L;

  private static final Logger log = LoggerFactory.getLogger(DefaultQuestionPanel.class);

  private DefaultOpenAnswerDefinitionPanel currentOpenField;

  @SpringBean
  private ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService;

  public DefaultQuestionPanel(String id, Question question) {
    super(id, question);

    if(question.getNumber() != null) {
      add(new Label("number", question.getNumber()));
    } else {
      add(new Label("number"));
    }
    add(new Label("label", new QuestionnaireStringResourceModel(question, "label")));
    QuestionnaireStringResourceModel helpModel = new QuestionnaireStringResourceModel(question, "help");
    if(helpModel.getString() != null && !helpModel.getString().trim().equals("")) {
      add(new HelpPanel("help", helpModel));
    } else {
      add(new EmptyPanel("help"));
    }
    add(new Label("instructions", new QuestionnaireStringResourceModel(question, "instructions")));
    add(new Label("caption", new QuestionnaireStringResourceModel(question, "caption")));

    if(!question.isMultiple()) {
      addRadioGroup(question);
    } else {
      addCheckBoxGroup(question);
    }

    if(question.isBoilerPlate()) {
      add(new AttributeModifier("class", new Model("boilerplate")));
    }
  }

  @SuppressWarnings("serial")
  private void addRadioGroup(Question question) {
    final RadioGroup radioGroup = new RadioGroup("categories", new Model());
    add(radioGroup);
    ListView radioList = new ListView("category", question.getQuestionCategories()) {

      @Override
      protected void populateItem(ListItem item) {
        final QuestionCategory questionCategory = (QuestionCategory) item.getModelObject();
        RadioInput radioInput = new RadioInput("input", item.getModel());// new Radio("radio", item.getModel());
        radioInput.radio.setLabel(new QuestionnaireStringResourceModel(questionCategory, "label"));

        FormComponentLabel radioLabel = new FormComponentLabel("categoryLabel", radioInput.radio);
        item.add(radioLabel);
        radioLabel.add(radioInput);
        radioLabel.add(new Label("label", radioInput.radio.getLabel()).setRenderBodyOnly(true));

        final DefaultOpenAnswerDefinitionPanel openField = createOpenAnswerDefinitionPanel(item, questionCategory);

        radioInput.radio.add(new AjaxEventBehavior("onchange") {

          @Override
          protected void onEvent(AjaxRequestTarget target) {
            log.info("radio.onchange.{}.{}", questionCategory.getQuestion().getName(), questionCategory.getCategory().getName());
            if(currentOpenField != null) {
              currentOpenField.setFieldEnabled(false);
              target.addComponent(currentOpenField);
            }
            currentOpenField = null;
            if(openField != null) {
              openField.setFieldEnabled(true);
              currentOpenField = openField;
              target.addComponent(openField);
            }

            // exclusive choice, only one answer per question
            activeQuestionnaireAdministrationService.deleteAnswers(questionCategory.getQuestion());
            // TODO get the open answer
            activeQuestionnaireAdministrationService.answer(questionCategory, null);
          }

        });

        if(questionCategory.isSelected()) {
          radioGroup.setModel(item.getModel());
          CategoryAnswer categoryAnswer = activeQuestionnaireAdministrationService.findAnswer(questionCategory);
          activeQuestionnaireAdministrationService.answer(questionCategory, categoryAnswer != null ? categoryAnswer.getData() : null);
        }
      }

    }.setReuseItems(true);
    radioGroup.add(radioList);
    radioGroup.setRequired(question.getQuestionCategories().size() > 0 && question.isRequired());
    radioGroup.setLabel(new QuestionnaireStringResourceModel(question, "label"));
  }

  private void addCheckBoxGroup(Question question) {
    final List<IModel> checkedItems = new ArrayList<IModel>();

    ListView checkList = new ListView("category", question.getQuestionCategories()) {

      private static final long serialVersionUID = 1L;

      @SuppressWarnings("serial")
      @Override
      protected void populateItem(ListItem item) {
        final QuestionCategory questionCategory = (QuestionCategory) item.getModelObject();
        final QuestionCategorySelection categorySelection = new QuestionCategorySelection(questionCategory, questionCategory.isSelected());
        CheckBoxInput checkBoxInput = new CheckBoxInput("input", new PropertyModel(categorySelection, "selection"));
        checkBoxInput.checkbox.setLabel(new QuestionnaireStringResourceModel(questionCategory, "label"));

        FormComponentLabel checkBoxLabel = new FormComponentLabel("categoryLabel", checkBoxInput.checkbox);
        item.add(checkBoxLabel);
        checkBoxLabel.add(checkBoxInput);
        checkBoxLabel.add(new Label("label", checkBoxInput.checkbox.getLabel()).setRenderBodyOnly(true));

        final DefaultOpenAnswerDefinitionPanel openField = createOpenAnswerDefinitionPanel(item, questionCategory);

        checkBoxInput.checkbox.add(new AjaxEventBehavior("onchange") {

          @Override
          protected void onEvent(AjaxRequestTarget target) {
            log.info("checkbox.onchange.{}.{}", questionCategory.getQuestion().getName(), questionCategory.getCategory().getName());
            if(openField != null) {
              openField.setFieldEnabled(!openField.isFieldEnabled());
              target.addComponent(openField);
            }
            // multiple choice
            if(!categorySelection.isSelected()) {
              activeQuestionnaireAdministrationService.deleteAnswer(questionCategory);
            } else {
              // TODO get the open answer
              activeQuestionnaireAdministrationService.answer(questionCategory, null);
            }
          }

        });

        if(questionCategory.isSelected()) {
          checkedItems.add(checkBoxInput.checkbox.getModel());
        }
      }
    };

    CheckGroup checkGroup = new CheckGroup("categories", checkedItems);
    add(checkGroup);
    checkGroup.add(checkList);
    checkGroup.setRequired(question.getQuestionCategories().size() > 0 && question.isRequired());
    checkGroup.setLabel(new QuestionnaireStringResourceModel(question, "label"));
  }

  @SuppressWarnings("serial")
  private DefaultOpenAnswerDefinitionPanel createOpenAnswerDefinitionPanel(WebMarkupContainer parent, final QuestionCategory questionCategory) {
    DefaultOpenAnswerDefinitionPanel openField;

    if(questionCategory.getCategory().getOpenAnswerDefinition() != null) {
      openField = new DefaultOpenAnswerDefinitionPanel("open", new Model(questionCategory));
      parent.add(openField);
      openField.setFieldEnabled(questionCategory.isSelected());
    } else {
      openField = null;
      parent.add(new EmptyPanel("open"));
    }

    return openField;
  }

  public void onNext(AjaxRequestTarget target) {
    // TODO Auto-generated method stub

  }

  public void onPrevious(AjaxRequestTarget target) {
    // TODO Auto-generated method stub

  }

  @SuppressWarnings("serial")
  private class RadioInput extends Fragment {

    Radio radio;

    public RadioInput(String id, IModel model) {
      super(id, "radioInput", DefaultQuestionPanel.this);
      add(radio = new Radio("radio", model));
    }

  }

  @SuppressWarnings("serial")
  private class CheckBoxInput extends Fragment {

    CheckBox checkbox;

    public CheckBoxInput(String id, IModel model) {
      super(id, "checkboxInput", DefaultQuestionPanel.this);
      add(checkbox = new CheckBox("checkbox", model));
    }

  }

  @SuppressWarnings("serial")
  private class QuestionCategorySelection implements Serializable {

    private QuestionCategory questionCategory;

    private Boolean selection = Boolean.FALSE;

    public QuestionCategorySelection(QuestionCategory questionCategory, boolean selected) {
      this.questionCategory = questionCategory;
      this.selection = selected;
    }

    public QuestionCategory getQuestionCategory() {
      return questionCategory;
    }

    public void setQuestionCategory(QuestionCategory questionCategory) {
      this.questionCategory = questionCategory;
    }

    public Boolean getSelection() {
      return selection;
    }

    public void setSelection(Boolean selection) {
      this.selection = selection;
    }

    public boolean isSelected() {
      return selection;
    }

  }

  private class HelpPanel extends Fragment {

    private Label helpContent;

    private Label helpLabel;

    public HelpPanel(String id, IModel helpModel) {
      super(id, "helpFragment", DefaultQuestionPanel.this);
      setOutputMarkupId(true);

      AjaxLink link = new AjaxLink("helpLink") {

        @Override
        public void onClick(AjaxRequestTarget target) {
          helpContent.setVisible(!helpContent.isVisible());
          if(helpContent.isVisible()) {
            helpLabel.setModel(new StringResourceModel("HideHelp", DefaultQuestionPanel.this, null));
          } else {
            helpLabel.setModel(new StringResourceModel("Help", DefaultQuestionPanel.this, null));
          }
          target.addComponent(HelpPanel.this);
        }

      };
      add(link);

      link.add(helpLabel = new Label("helpLabel", new StringResourceModel("Help", DefaultQuestionPanel.this, null)));

      add(helpContent = new Label("helpContent", helpModel));
      helpContent.setVisible(false);
    }

  }

}
