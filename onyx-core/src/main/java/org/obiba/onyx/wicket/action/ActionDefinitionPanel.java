/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.wicket.action;

import java.io.Serializable;
import java.text.DateFormat;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.IErrorMessageSource;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidationError;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.validator.StringValidator;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.domain.user.User;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.core.service.UserSessionService;
import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ActionDefinition;
import org.obiba.onyx.wicket.model.SpringStringResourceModel;
import org.obiba.onyx.wicket.reusable.FeedbackWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ActionDefinitionPanel extends Panel {

  private static final long serialVersionUID = -5173222062528691764L;

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(ActionDefinitionPanel.class);

  @SpringBean(name = "activeInterviewService")
  private ActiveInterviewService activeInterviewService;

  @SpringBean(name = "userSessionService")
  private UserSessionService userSessionService;

  private boolean cancelled = true;

  private FeedbackWindow feedback;

  @SuppressWarnings("serial")
  public ActionDefinitionPanel(String id, ActionDefinition definition, AjaxRequestTarget target) {
    super(id);

    Action action = new Action(definition);
    setModel(new Model(action));

    add(feedback = new FeedbackWindow("feedback"));
    feedback.setOutputMarkupId(true);

    add(new Label("operator", userSessionService.getUser().getFullName()));

    // password field
    User operatorTemplate = new User();
    PasswordTextField password = new PasswordTextField("password", new PropertyModel(operatorTemplate, "password"));
    password.setRequired(definition.isAskPassword());
    password.add(new IValidator() {

      public void validate(IValidatable validatable) {
        if(!User.digest((String) validatable.getValue()).equals(userSessionService.getUser().getPassword())) {
          validatable.error(new UserValidationError());
        }
      }
    });
    add(password.setEnabled(definition.isAskPassword()));

    // participant barcode field
    Participant participantTemplate = new Participant();
    TextField barcode = new TextField("ParticipantCode", new PropertyModel(participantTemplate, "barcode"));
    barcode.setRequired(definition.isAskParticipantId());
    barcode.add(new IValidator() {

      public void validate(IValidatable validatable) {
        if(!activeInterviewService.getParticipant().getBarcode().equals(validatable.getValue())) {
          validatable.error(new ParticipantValidationError());
        }
      }

    });
    add(barcode.setEnabled(definition.isAskParticipantId()));

    TextArea commentArea = new TextArea("comment", new PropertyModel(this, "action.comment"));
    commentArea.setRequired(definition.isCommentMandatory());
    commentArea.add(new StringValidator.MaximumLengthValidator(2000));
    add(commentArea);

    // request for focus on first field
    if(password.isEnabled()) {
      password.setOutputMarkupId(true);
      target.focusComponent(password);
    } else if(barcode.isEnabled()) {
      barcode.setOutputMarkupId(true);
      target.focusComponent(barcode);
    } else {
      commentArea.setOutputMarkupId(true);
      target.focusComponent(commentArea);
    }

    action.setEventReason(definition.getDefaultReason());
    DropDownChoice reasonsDropDown = new DropDownChoice("reasonsSelect", new PropertyModel(ActionDefinitionPanel.this, "action.eventReason"), definition.getReasons(), new IChoiceRenderer() {
      public Object getDisplayValue(Object object) {
        return new SpringStringResourceModel(object.toString()).getString();
      }

      public String getIdValue(Object object, int index) {
        return object.toString();
      }
    });
    reasonsDropDown.setLabel(new ResourceModel("Reason"));
    reasonsDropDown.setRequired(definition.isReasonMandatory());
    reasonsDropDown.setEnabled(definition.getReasons().size() > 0);
    add(reasonsDropDown);
  }

  public Action getAction() {
    return (Action) getModelObject();
  }

  public boolean isCancelled() {
    return cancelled;
  }

  public DateFormat getDateFormat() {
    return userSessionService.getDateFormat();
  }

  @SuppressWarnings("serial")
  private class ParticipantValidationError implements IValidationError, Serializable {

    public String getErrorMessage(IErrorMessageSource messageSource) {
      return ActionDefinitionPanel.this.getString("NotTheInterviewParticipant");
    }

  }

  @SuppressWarnings("serial")
  private class UserValidationError implements IValidationError, Serializable {

    public String getErrorMessage(IErrorMessageSource messageSource) {
      return ActionDefinitionPanel.this.getString("WrongOperatorPassword", null, "Wrong Operator Password");
    }

  }

  public FeedbackWindow getFeedback() {
    return feedback;
  }

  public void setFeedback(FeedbackWindow feedback) {
    this.feedback = feedback;
  }
}
