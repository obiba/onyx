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

import java.util.List;

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
import org.obiba.onyx.quartz.core.domain.answer.CategoryAnswer;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.quartz.core.wicket.layout.impl.AbstractOpenAnswerDefinitionPanel;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireStringResourceModel;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;
import org.obiba.onyx.wicket.data.DataField;
import org.obiba.onyx.wicket.data.IDataValidator;
import org.obiba.onyx.wicket.link.AjaxImageLink;
import org.obiba.onyx.wicket.link.AjaxImageSubmitLink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class NumericPad extends AbstractOpenAnswerDefinitionPanel implements IPadSelectionListener {

  private static final long serialVersionUID = 1L;

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(NumericPad.class);

  final DataField valuePressed;

  @SpringBean
  private ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService;

  /**
   * @param id
   * @param model
   */
  @SuppressWarnings("serial")
  public NumericPad(String id, IModel questionModel, IModel questionCategoryModel, IModel openAnswerDefinitionModel, final ModalWindow padWindow) {
    super(id, questionModel, questionCategoryModel, openAnswerDefinitionModel);

    final DataType type = getOpenAnswerDefinition().getDataType();

    // Adding numeric pad's buttons.
    add(new PadButton("0", new Model("0")));
    add(new PadButton("1", new Model("1")));
    add(new PadButton("2", new Model("2")));
    add(new PadButton("3", new Model("3")));
    add(new PadButton("4", new Model("4")));
    add(new PadButton("5", new Model("5")));
    add(new PadButton("6", new Model("6")));
    add(new PadButton("7", new Model("7")));
    add(new PadButton("8", new Model("8")));
    add(new PadButton("9", new Model("9")));
    PadButton decimalSeparator = new PadButton("separator", new Model("."));
    add(decimalSeparator);

    if(type.equals(DataType.DECIMAL)) {
      log.debug("DataType is decimal, so the decimal separator will be enabled on the numeric pad");
    } else {
      decimalSeparator.setButtonEnabled(false);
    }

    Form padForm = new Form("form");

    // Create and add the numeric pad input field.
    valuePressed = new DataField("value", new PropertyModel(this, "data"), type);
    valuePressed.getField().clearInput();
    valuePressed.setOutputMarkupId(true);
    valuePressed.setMarkupId("valuePressed");
    valuePressed.setRequired(true);
    padForm.add(valuePressed);

    // Transfer the validators of the OpenAnswer field to the numeric pad.
    List<IDataValidator> validators = getOpenAnswerDefinition().getDataValidators();
    for(IDataValidator dataValidator : validators) {
      valuePressed.add(dataValidator);
    }

    // Create and add the label for the numeric input field.
    OpenAnswerDefinition parentOpenAnswer = ((OpenAnswerDefinition) openAnswerDefinitionModel.getObject()).getParentOpenAnswerDefinition();
    IModel labelModel;
    if(parentOpenAnswer != null && parentOpenAnswer.getOpenAnswerDefinitions().size() > 0) {
      labelModel = openAnswerDefinitionModel;
    } else {
      labelModel = questionCategoryModel;
    }
    padForm.add(new Label("category", new QuestionnaireStringResourceModel(labelModel, "label")));

    // Add a feedback panel to the numeric pad for error reporting.
    final FeedbackPanel padFeedbackPanel = new FeedbackPanel("feedback");
    add(padFeedbackPanel.setOutputMarkupId(true));

    // Create a decorated submit "OK" button.
    ResourceReference buttonDecorator = new ResourceReference(NumericPad.class, "check2.gif");
    AjaxImageSubmitLink submitLink = new AjaxImageSubmitLink("ok", new QuestionnaireStringResourceModel(activeQuestionnaireAdministrationService.getQuestionnaire(), "ok")) {

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

    padForm.add(submitLink);

    AjaxImageLink cancelLink = new AjaxImageLink("cancel", new QuestionnaireStringResourceModel(activeQuestionnaireAdministrationService.getQuestionnaire(), "cancel")) {

      @Override
      public void onClick(AjaxRequestTarget target) {
        padWindow.close(target);
      }

    };
    padForm.add(cancelLink);

    AjaxImageLink clearLink = new AjaxImageLink("clear", new QuestionnaireStringResourceModel(activeQuestionnaireAdministrationService.getQuestionnaire(), "reset")) {

      @Override
      public void onClick(AjaxRequestTarget target) {
        resetField();
        valuePressed.getField().clearInput();
        target.addComponent(valuePressed);
      }

    };
    padForm.add(clearLink);

    add(padForm);

    padWindow.setTitle(new StringResourceModel("NumericPadTitle", this, null));

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
