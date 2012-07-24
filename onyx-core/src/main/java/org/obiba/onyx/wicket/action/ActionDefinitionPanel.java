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
import org.apache.wicket.model.StringResourceModel;
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

@SuppressWarnings("serial")
public abstract class ActionDefinitionPanel extends Panel {

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(ActionDefinitionPanel.class);

  @SpringBean(name = "activeInterviewService")
  private ActiveInterviewService activeInterviewService;

  @SpringBean(name = "userSessionService")
  private UserSessionService userSessionService;

  private boolean cancelled = true;

  private FeedbackWindow feedback;

  public ActionDefinitionPanel(String id, ActionDefinition definition, AjaxRequestTarget target) {
    super(id);

    Action action = new Action(definition);
    setDefaultModel(new Model<Action>(action));

    add(feedback = new FeedbackWindow("feedback"));
    feedback.setOutputMarkupId(true);

    add(new Label("operator", userSessionService.getUserName()));

    // password field
    User operatorTemplate = new User();
    PasswordTextField password = new PasswordTextField("password", new PropertyModel<String>(operatorTemplate, "password"));
    password.setRequired(definition.isAskPassword());
    password.add(new IValidator<String>() {

      public void validate(IValidatable<String> validatable) {
        if(userSessionService.authenticate(validatable.getValue()) == false) {
          validatable.error(new UserValidationError());
        }
      }
    });
    add(password.setEnabled(definition.isAskPassword()));

    // participant barcode field
    Participant participantTemplate = new Participant();
    TextField<String> barcode = new TextField<String>("ParticipantCode", new PropertyModel<String>(participantTemplate, "barcode"));
    barcode.setRequired(definition.isAskParticipantId());
    barcode.add(new IValidator<String>() {

      public void validate(IValidatable<String> validatable) {
        if(!activeInterviewService.getParticipant().getBarcode().equals(validatable.getValue())) {
          validatable.error(new ParticipantValidationError());
        }
      }

    });
    add(barcode.setEnabled(definition.isAskParticipantId()));

    Object commentNoteKey = new PropertyModel<Object>(definition, "commentNote").getObject();
    String defaultNote = new StringResourceModel("AnonymousComments", this, null).getString();
    Label commentNoteLabel = new Label("commentNote", new SpringStringResourceModel(commentNoteKey != null ? commentNoteKey.toString() : "", defaultNote).getString());
    add(commentNoteLabel.setEnabled(definition.isAskComment()));

    TextArea<String> commentArea = new TextArea<String>("comment", new PropertyModel<String>(this, "action.comment"));
    commentArea.setRequired(definition.isAskComment() && definition.isCommentMandatory());
    commentArea.add(new StringValidator.MaximumLengthValidator(2000));
    add(commentArea.setEnabled(definition.isAskComment()));

    // request for focus on first field
    if(password.isEnabled()) {
      password.setOutputMarkupId(true);
      target.focusComponent(password);
    } else if(barcode.isEnabled()) {
      barcode.setOutputMarkupId(true);
      target.focusComponent(barcode);
    } else if(commentArea.isEnabled()) {
      commentArea.setOutputMarkupId(true);
      target.focusComponent(commentArea);
    }

    action.setEventReason(definition.getDefaultReason());
    DropDownChoice<String> reasonsDropDown = new DropDownChoice<String>("reasonsSelect", new PropertyModel<String>(ActionDefinitionPanel.this, "action.eventReason"), definition.getReasons(), new IChoiceRenderer<String>() {
      public Object getDisplayValue(String object) {
        return new SpringStringResourceModel(object).getString();
      }

      public String getIdValue(String object, int index) {
        return object;
      }
    });
    reasonsDropDown.setLabel(new ResourceModel("Reason"));
    reasonsDropDown.setRequired(definition.isReasonMandatory());
    reasonsDropDown.setEnabled(definition.getReasons().size() > 0);
    add(reasonsDropDown);
  }

  public Action getAction() {
    return (Action) getDefaultModelObject();
  }

  public boolean isCancelled() {
    return cancelled;
  }

  public DateFormat getDateFormat() {
    return userSessionService.getDateFormat();
  }

  private class ParticipantValidationError implements IValidationError, Serializable {

    public String getErrorMessage(IErrorMessageSource messageSource) {
      return ActionDefinitionPanel.this.getString("NotTheInterviewParticipant");
    }

  }

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
