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
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.CheckGroup;
import org.apache.wicket.markup.html.form.FormComponentLabel;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.obiba.onyx.quartz.core.domain.answer.CategoryAnswer;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.wicket.layout.QuestionPanel;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireModel;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireStringResourceModel;
import org.obiba.onyx.wicket.toggle.ToggleLink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Support for question multiple or not, but without child questions.
 */
public class DefaultQuestionPanel extends QuestionPanel {

  private static final long serialVersionUID = 2951128797454847260L;

  private static final Logger log = LoggerFactory.getLogger(DefaultQuestionPanel.class);

  private DefaultOpenAnswerDefinitionPanel currentOpenField;

  public DefaultQuestionPanel(String id, Question question) {
    super(id, question);
    setOutputMarkupId(true);

    if(question.getNumber() != null) {
      add(new Label("number", question.getNumber()));
    } else {
      add(new Label("number"));
    }
    add(new Label("label", new QuestionnaireStringResourceModel(question, "label")));

    // help toggle
    QuestionnaireStringResourceModel helpModel = new QuestionnaireStringResourceModel(question, "help");
    if(helpModel.getString() != null && !helpModel.getString().trim().equals("")) {
      Label helpContent = new Label("help", helpModel);
      // help resource can contain html formatting
      helpContent.setEscapeModelStrings(false);
      add(helpContent);

      // toggle has background image defined by css
      ToggleLink toggleLink = new ToggleLink("helpToggle", new Model("&nbsp;&nbsp;&nbsp;&nbsp;"), new Model("&nbsp;&nbsp;&nbsp;&nbsp;"), helpContent);
      toggleLink.setLabelEscapeModelStrings(false);
      add(toggleLink);
    } else {
      // dummy content
      add(new EmptyPanel("helpToggle").setVisible(false));
      add(new EmptyPanel("help").setVisible(false));
    }

    add(new Label("instructions", new QuestionnaireStringResourceModel(question, "instructions")));
    add(new Label("caption", new QuestionnaireStringResourceModel(question, "caption")));

    if(!question.isMultiple()) {
      addRadioGroup(question);
    } else {
      addCheckBoxGroup(question);
    }

    // change the css rendering in case of a boiler plate
    if(question.isBoilerPlate()) {
      add(new AttributeModifier("class", new Model("boilerplate")));
    }
  }

  /**
   * Add a radio group, used by single choice question.
   * @param question
   */
  @SuppressWarnings("serial")
  private void addRadioGroup(final Question question) {
    final RadioGroup radioGroup = new RadioGroup("categories", new Model());
    radioGroup.setRequired(!question.isBoilerPlate() && question.isRequired());
    add(radioGroup);

    RepeatingView repeater = new RepeatingView("category");
    radioGroup.add(repeater);

    for(final QuestionCategory questionCategory : ((Question) getModelObject()).getQuestionCategories()) {
      final WebMarkupContainer item = new WebMarkupContainer(repeater.newChildId());
      repeater.add(item);
      item.setModel(new Model(questionCategory));

      RadioInput radioInput = new RadioInput("input", item.getModel());
      radioInput.radio.setLabel(new QuestionnaireStringResourceModel(questionCategory, "label"));

      FormComponentLabel radioLabel = new FormComponentLabel("categoryLabel", radioInput.radio);
      item.add(radioLabel);
      radioLabel.add(radioInput);
      radioLabel.add(new Label("label", radioInput.radio.getLabel()).setRenderBodyOnly(true));

      // previous answer or default selection
      CategoryAnswer previousAnswer = activeQuestionnaireAdministrationService.findAnswer(questionCategory);

      if(questionCategory.getCategory().getOpenAnswerDefinition() != null) {
        // there is an open field
        // hide the associated radio and fake selection on click event of open field
        DefaultOpenAnswerDefinitionPanel openField = new DefaultOpenAnswerDefinitionPanel("open", new QuestionnaireModel(questionCategory)) {

          @Override
          public void onClick(AjaxRequestTarget target) {
            log.info("open.onclick.{}", questionCategory.getName());
            // ignore if multiple click in the same open field
            if(this.equals(currentOpenField)) return;

            // make sure a previously selected open field is not asked for
            if(currentOpenField != null) {
              currentOpenField.setRequired(false);
            }
            // make the open field active
            currentOpenField = this;
            currentOpenField.setRequired(question.isRequired() ? true : false);
            // make sure radio selection does not conflict with open field selection
            radioGroup.setModel(new Model());
            radioGroup.setRequired(false);
            // update all
            target.addComponent(DefaultQuestionPanel.this);
            // exclusive choice, only one answer per question
            activeQuestionnaireAdministrationService.deleteAnswers(questionCategory.getQuestion());
            // TODO get the open answer
            activeQuestionnaireAdministrationService.answer(questionCategory, null);
          }

        };
        item.add(openField);
        radioInput.radio.setVisible(false);

        // previous answer or default selection ?
        if(previousAnswer != null) {
          openField.setRequired(question.isRequired() ? true : false);
          radioGroup.setRequired(false);
          currentOpenField = openField;
        } else if(questionCategory.isSelected()) {
          openField.setRequired(question.isRequired() ? true : false);
          activeQuestionnaireAdministrationService.answer(questionCategory, null);
        } else {
          // make sure it is not asked for as it is not selected at creation time
          openField.setRequired(false);
        }

      } else {
        // no open answer
        item.add(new EmptyPanel("open").setVisible(false));
        // persist selection on change event
        // and make sure there is no active open field previously selected
        radioInput.radio.add(new AjaxEventBehavior("onchange") {

          @Override
          protected void onEvent(AjaxRequestTarget target) {
            log.info("radio.onchange.{}", questionCategory.getName());
            // make the radio group active for the selection
            radioGroup.setModel(item.getModel());
            radioGroup.setRequired(question.isRequired() ? true : false);
            // make inactive the previously selected open field
            if(currentOpenField != null) {
              currentOpenField.setData(null);
              currentOpenField.setRequired(false);
              target.addComponent(currentOpenField);
              currentOpenField = null;
            }
            // exclusive choice, only one answer per question
            activeQuestionnaireAdministrationService.deleteAnswers(questionCategory.getQuestion());
            // TODO get the open answer
            activeQuestionnaireAdministrationService.answer(questionCategory, null);
          }

        });

        // previous answer or default selection ?
        if(previousAnswer != null) {
          radioGroup.setModel(item.getModel());
        } else if(questionCategory.isSelected()) {
          radioGroup.setModel(item.getModel());
          activeQuestionnaireAdministrationService.answer(questionCategory, null);
        }
      }

    }
    radioGroup.setLabel(new QuestionnaireStringResourceModel(question, "label"));
  }

  /**
   * Add a check box group, used by multiple choice question.
   * @param question
   */
  @SuppressWarnings("serial")
  private void addCheckBoxGroup(Question question) {
    final List<IModel> checkedItems = new ArrayList<IModel>();

    RepeatingView repeater = new RepeatingView("category");

    for(final QuestionCategory questionCategory : ((Question) getModelObject()).getQuestionCategories()) {
      WebMarkupContainer item = new WebMarkupContainer(repeater.newChildId());
      repeater.add(item);

      final QuestionCategorySelection categorySelection = new QuestionCategorySelection(questionCategory, questionCategory.isSelected());
      item.setModel(new PropertyModel(categorySelection, "selection"));

      CheckBoxInput checkBoxInput = new CheckBoxInput("input", item.getModel());
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

      // previous answer or default selection
      CategoryAnswer previousAnswer = activeQuestionnaireAdministrationService.findAnswer(questionCategory);
      if(previousAnswer != null) {
        checkedItems.add(item.getModel());
        if(openField != null) {
          openField.setFieldEnabled(true);
        }
      } else if(questionCategory.isSelected()) {
        checkedItems.add(item.getModel());
        if(openField != null) {
          openField.setFieldEnabled(true);
        }
        activeQuestionnaireAdministrationService.answer(questionCategory, null);
      }
    }
    ;

    CheckGroup checkGroup = new CheckGroup("categories", checkedItems);
    add(checkGroup);
    checkGroup.add(repeater);
    checkGroup.setRequired(question.getQuestionCategories().size() > 0 && question.isRequired());
    checkGroup.setLabel(new QuestionnaireStringResourceModel(question, "label"));
  }

  /**
   * Create an open answer definition panel if given {@link QuestionCategory} has a {@link OpenAnswerDefinition}
   * associated to.
   * @param parent
   * @param questionCategory
   * @return null if no open answer definition
   */
  @SuppressWarnings("serial")
  private DefaultOpenAnswerDefinitionPanel createOpenAnswerDefinitionPanel(WebMarkupContainer parent, final QuestionCategory questionCategory) {
    DefaultOpenAnswerDefinitionPanel openField;

    if(questionCategory.getCategory().getOpenAnswerDefinition() != null) {
      openField = new DefaultOpenAnswerDefinitionPanel("open", new QuestionnaireModel(questionCategory)) {

        @Override
        public void onClick(AjaxRequestTarget target) {

        }

      };
      // openField.setFieldEnabled(false);
      parent.add(openField);
    } else {
      openField = null;
      parent.add(new EmptyPanel("open"));
    }

    return openField;
  }

  /**
   * The radio input chunk.
   */
  @SuppressWarnings("serial")
  private class RadioInput extends Fragment {

    Radio radio;

    public RadioInput(String id, IModel model) {
      super(id, "radioInput", DefaultQuestionPanel.this);
      setOutputMarkupId(true);
      add(radio = new Radio("radio", model));
    }

  }

  /**
   * The checkbox input chunk.
   */
  @SuppressWarnings("serial")
  private class CheckBoxInput extends Fragment {

    CheckBox checkbox;

    public CheckBoxInput(String id, IModel model) {
      super(id, "checkboxInput", DefaultQuestionPanel.this);
      setOutputMarkupId(true);
      add(checkbox = new CheckBox("checkbox", model));
    }

  }

  /**
   * Private class for storing category selections in case of a multiple choice question.
   */
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

}
