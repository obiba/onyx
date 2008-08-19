package org.obiba.onyx.wicket.action;

import java.io.Serializable;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.IErrorMessageSource;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidationError;
import org.apache.wicket.validation.IValidator;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.domain.user.User;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ActionDefinition;
import org.obiba.onyx.wicket.behavior.RequiredFormFieldBehavior;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ActionDefinitionPanel extends Panel {

  private static final long serialVersionUID = -5173222062528691764L;

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(ActionDefinitionPanel.class);

  @SpringBean
  private ActiveInterviewService activeInterviewService;

  private boolean cancelled = true;

  private FeedbackPanel feedback;

  @SuppressWarnings("serial")
  public ActionDefinitionPanel(String id, ActionDefinition definition) {
    super(id);

    Action action = new Action(definition);
    setModel(new Model(action));

    add(new Label("description", definition.getDescription()));

    Form form = new Form("form");
    add(form);

    form.add(feedback = new FeedbackPanel("feedback"));
    feedback.setOutputMarkupId(true);

    form.add(new Label("participant", activeInterviewService.getParticipant().getFullName()));

    if(definition.isAskPassword()) {
      form.add(new PasswordFragment("password"));
    } else {
      form.add(new Fragment("password", "trFragment", this));
    }

    Participant participantTemplate = new Participant();
    TextField participantBarcode = new TextField("confirmBarcode", new PropertyModel(participantTemplate, "barcode"));
    participantBarcode.setLabel(new StringResourceModel("ConfirmParticipantCode", this, null));
    participantBarcode.add(new IValidator() {

      public void validate(IValidatable validatable) {
        if(!activeInterviewService.getParticipant().getBarcode().equals(validatable.getValue())) {
          validatable.error(new ParticipantValidationError());
        }
      }

    });
    form.add(participantBarcode.add(new RequiredFormFieldBehavior()));

    form.add(new TextArea("comment", new PropertyModel(this, "action.comment")));

    action.setEventReason(definition.getDefaultReason());
    if(definition.getReasons().size() > 0) {
      form.add(new ReasonsFragment("reasons", definition.getReasons()));
    } else {
      form.add(new Fragment("reasons", "trFragment", this));
    }

    form.add(new AjaxButton("submit", form) {

      @Override
      protected void onSubmit(AjaxRequestTarget target, Form form) {
        cancelled = false;
        target.addComponent(feedback);
        ActionDefinitionPanel.this.onClick(target);
      }

      @Override
      protected void onError(AjaxRequestTarget target, Form form) {
        cancelled = false;
        target.addComponent(feedback);
      }

    });

    form.add(new AjaxLink("cancel") {

      @Override
      public void onClick(AjaxRequestTarget target) {
        cancelled = true;
        target.addComponent(feedback);
        ActionDefinitionPanel.this.onClick(target);
      }

    });
  }

  public Action getAction() {
    return (Action) getModelObject();
  }

  public boolean isCancelled() {
    return cancelled;
  }

  /**
   * Called after submit or cancel button are clicked.
   * @param target
   */
  public abstract void onClick(AjaxRequestTarget target);

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

  @SuppressWarnings("serial")
  private class ReasonsFragment extends Fragment {

    public ReasonsFragment(String id, List<String> reasons) {
      super(id, "reasonsFragment", ActionDefinitionPanel.this);
      add(new DropDownChoice("reasonsSelect", new PropertyModel(ActionDefinitionPanel.this, "action.eventReason"), reasons));
    }

  }

  @SuppressWarnings("serial")
  private class PasswordFragment extends Fragment {

    public PasswordFragment(String id) {
      super(id, "passwordFragment", ActionDefinitionPanel.this);
      final User operatorTemplate = new User();
      PasswordTextField pwdTextField = new PasswordTextField("password", new PropertyModel(operatorTemplate, "password"));
      add(pwdTextField.add(new RequiredFormFieldBehavior()));
      pwdTextField.add(new IValidator() {

        public void validate(IValidatable validatable) {
          if (!User.digest((String)validatable.getValue()).equals(activeInterviewService.getInterview().getUser().getPassword())) {
            validatable.error(new UserValidationError());  
          }
        }
      });
    }
  }
}
