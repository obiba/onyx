/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout.impl.simplified.pad;

import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.IValidator;
import org.obiba.onyx.quartz.core.domain.answer.CategoryAnswer;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.quartz.core.wicket.layout.impl.AbstractOpenAnswerDefinitionPanel;
import org.obiba.onyx.quartz.core.wicket.layout.impl.simplified.NoDragBehavior;
import org.obiba.onyx.quartz.core.wicket.layout.impl.util.OpenAnswerDefinitionValidatorFactory;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireStringResourceModel;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;
import org.obiba.onyx.wicket.data.DataField;
import org.obiba.onyx.wicket.link.AjaxImageLink;
import org.obiba.onyx.wicket.link.AjaxImageSubmitLink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NumericPad extends AbstractOpenAnswerDefinitionPanel implements IPadSelectionListener {

  private static final long serialVersionUID = 1L;

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(NumericPad.class);

  /** reference to the ok button check mark gif file. */
  public static final ResourceReference CHECKMARK = new ResourceReference(NumericPad.class, "check2.gif");

  private DataField valuePressed;

  @SpringBean
  private ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService;

  /**
   * @param id
   * @param model
   */
  @SuppressWarnings("serial")
  public NumericPad(String id, IModel questionModel, IModel questionCategoryModel, IModel openAnswerDefinitionModel, final ModalWindow padWindow) {

    super(id, questionModel, questionCategoryModel, openAnswerDefinitionModel);

    // Set the title of the modal window.
    padWindow.setTitle(new StringResourceModel("NumericPadTitle", this, null));

    final DataType type = getOpenAnswerDefinition().getDataType();

    // Create the dialog input field.
    valuePressed = createPadInputField(type);
    QuestionnaireStringResourceModel labelModel = createCategoryLabel(questionCategoryModel, openAnswerDefinitionModel);
    valuePressed.setLabel(labelModel);

    // Add a feedback panel to the numeric pad for error reporting.
    final FeedbackPanel padFeedbackPanel = new FeedbackPanel("feedback");

    // Create the dialog action buttons.
    AjaxImageSubmitLink submitButton = createSubmitButton(padWindow, type, padFeedbackPanel);
    AjaxImageLink cancelButton = createCancelButton(padWindow);
    AjaxImageLink clearButton = createClearButton();

    // Add the numeric buttons to the numeric pad.
    addNumericButtons();

    // Add the other wicket components.
    Form padForm = new Form("form");
    padForm.add(valuePressed);
    padForm.add(new Label("category", labelModel));
    padForm.add(submitButton);
    padForm.add(cancelButton);
    padForm.add(clearButton);
    add(padFeedbackPanel.setOutputMarkupId(true));
    add(padForm);

  }

  private AjaxImageLink createClearButton() {
    AjaxImageLink link = new AjaxImageLink("clear", new QuestionnaireStringResourceModel(activeQuestionnaireAdministrationService.getQuestionnaire(), "reset")) {

      private static final long serialVersionUID = 1L;

      @Override
      public void onClick(AjaxRequestTarget target) {
        resetField();
        valuePressed.getField().clearInput();
        target.addComponent(valuePressed);
      }

    };
    link.getLink().add(new NoDragBehavior());
    return link;
  }

  private AjaxImageLink createCancelButton(final ModalWindow padWindow) {
    AjaxImageLink link = new AjaxImageLink("cancel", new QuestionnaireStringResourceModel(activeQuestionnaireAdministrationService.getQuestionnaire(), "cancel")) {

      private static final long serialVersionUID = 1L;

      @Override
      public void onClick(AjaxRequestTarget target) {
        resetField();
        padWindow.close(target);
      }

    };
    link.getLink().add(new NoDragBehavior());
    return link;
  }

  private AjaxImageSubmitLink createSubmitButton(final ModalWindow padWindow, final DataType type, final FeedbackPanel padFeedbackPanel) {
    AjaxImageSubmitLink link = new AjaxImageSubmitLink("ok", new QuestionnaireStringResourceModel(activeQuestionnaireAdministrationService.getQuestionnaire(), "ok"), null, CHECKMARK) {

      private static final long serialVersionUID = 1L;

      @Override
      public void onError(AjaxRequestTarget target, Form form) {
        log.debug("Error submitting numeric pad data, data is not valid.");
        valuePressed.getField().clearInput();
        resetField();
        target.addComponent(valuePressed);
        target.addComponent(padFeedbackPanel);
        target.appendJavascript("resizeNumericPad();");
      }

      @Override
      public void onSubmit(AjaxRequestTarget target, Form form) {

        log.debug("Submitting numeric pad data...");

        // build final data
        Data data = getData();
        if(data != null && data.getValue() != null && data.getValueAsString().length() > 0) {
          try {
            setData(DataBuilder.build(type, data.getValueAsString()));
          } catch(Exception e) {
            log.warn("Failed parsing as a " + type + ":" + data.getValueAsString(), e);
            setData(null);
          }
        } else {
          setData(null);
        }

        // persist
        if(!getQuestion().isMultiple()) {
          // exclusive choice: delete other category answers
          for(CategoryAnswer categoryAnswer : activeQuestionnaireAdministrationService.findAnswers(getQuestion())) {
            if(!categoryAnswer.getCategoryName().equals(getQuestionCategory().getCategory().getName())) {
              activeQuestionnaireAdministrationService.deleteAnswer(getQuestion(), getQuestion().findQuestionCategory(categoryAnswer.getCategoryName()));
            }
          }
        }
        if(getData() != null) {
          activeQuestionnaireAdministrationService.answer(getQuestion(), getQuestionCategory(), getOpenAnswerDefinition(), getData());
        } else {
          activeQuestionnaireAdministrationService.deleteAnswer(getQuestion(), getQuestionCategory(), getOpenAnswerDefinition());
        }

        valuePressed.getField().clearInput();
        resetField();
        target.addComponent(valuePressed);

        // close pad modal window
        padWindow.close(target);

      }

      @Override
      public void onClick(AjaxRequestTarget target) {
        // TODO Auto-generated method stub
      }

    };
    link.getLink().add(new NoDragBehavior());
    return link;
  }

  private QuestionnaireStringResourceModel createCategoryLabel(IModel questionCategoryModel, IModel openAnswerDefinitionModel) {
    // Create and add the label for the numeric input field.
    OpenAnswerDefinition parentOpenAnswer = ((OpenAnswerDefinition) openAnswerDefinitionModel.getObject()).getParentOpenAnswerDefinition();
    IModel labelModel;
    if(parentOpenAnswer != null && parentOpenAnswer.getOpenAnswerDefinitions().size() > 0) {
      labelModel = openAnswerDefinitionModel;
    } else {
      labelModel = questionCategoryModel;
    }
    return new QuestionnaireStringResourceModel(labelModel, "label");
  }

  private void addNumericButtons() {
    for(int i = 0; i < 10; i++) {
      add(new PadButton(String.valueOf(i), new Model(String.valueOf(i))));
    }
  }

  private DataField createPadInputField(final DataType type) {
    // Create the numeric pad input field.
    DataField valuePressed = new DataField("value", new PropertyModel(this, "data"), type);
    valuePressed.getField().clearInput();
    valuePressed.setOutputMarkupId(true);
    valuePressed.setMarkupId("valuePressed");
    valuePressed.setRequired(true);

    // Transfer the validators of the OpenAnswer field to the numeric pad.
    for(IValidator dataValidator : OpenAnswerDefinitionValidatorFactory.getValidators(getOpenAnswerDefinitionModel(), activeQuestionnaireAdministrationService.getQuestionnaireParticipant().getParticipant())) {
      valuePressed.add(dataValidator);
    }

    return valuePressed;
  }

  @Override
  public void resetField() {
    setData(null);
  }

  public void onPadSelection(AjaxRequestTarget target, IModel model) {
    String key = (String) model.getObject();
    log.debug("Button {} has been pressed on the numeric pad", key);

    Data data = getData();

    if(data == null) {
      setData(DataBuilder.buildText(key));
    } else {
      log.debug("Value already displayed on the numeric pad: {}", data.getValueAsString());
      String val = data.getValueAsString();
      if(key.equals(".") && val.contains(".")) {
        // ignore
      } else {
        setData(DataBuilder.buildText(val + key));
      }
    }

    log.debug("The following value will now be displayed by the numeric pad: {}", getData().getValueAsString());

    target.addComponent(valuePressed);
  }

}
