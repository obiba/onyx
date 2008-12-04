/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.webapp.participant.panel;

import java.io.Serializable;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.value.ValueMap;
import org.apache.wicket.validation.IErrorMessageSource;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidationError;
import org.apache.wicket.validation.IValidator;
import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.service.ParticipantService;
import org.obiba.onyx.webapp.OnyxAuthenticatedSession;
import org.obiba.onyx.wicket.behavior.RequiredFormFieldBehavior;

public class AssignCodeToParticipantPanel extends Panel {

  @SpringBean
  private ParticipantService participantService;

  @SpringBean
  private EntityQueryService queryService;

  private final Model receptionCommentModel = new Model();

  private static final long serialVersionUID = 1L;

  public AssignCodeToParticipantPanel(String id) {
    super(id);
  }

  public AssignCodeToParticipantPanel(String id, IModel participantModel) {
    super(id);
    add(new AssignCodeToParticipantForm("assignCodeToParticipantForm", participantModel));
  }

  public class AssignCodeToParticipantForm extends Form {

    private static final long serialVersionUID = 1L;

    @SuppressWarnings("serial")
    public AssignCodeToParticipantForm(String id, final IModel participantModel) {
      super(id, participantModel);

      TextField participantCode = new TextField("participantCode", new PropertyModel(getModel(), "barcode"));
      participantCode.add(new RequiredFormFieldBehavior());
      participantCode.add(new IValidator() {

        public void validate(final IValidatable validatable) {

          Participant participant = (Participant) getModel().getObject();
          if(participant.getBarcode() != null) {
            validatable.error(new ParticipantAlreadyReceivedError());
          } else {
            Participant template = new Participant();
            template.setBarcode((String) validatable.getValue());
            if(queryService.count(template) > 0) {
              validatable.error(new BarCodeAlreadyUsedError((String) validatable.getValue()));
            }
          }
        }

      });
      add(participantCode);
      add(new TextArea("comment", receptionCommentModel));
    }

    public void onSubmit(Participant participant) {
      participantService.assignCodeToParticipant(participant, participant.getBarcode(), (String) receptionCommentModel.getObject(), OnyxAuthenticatedSession.get().getUser());
    }
  }

  @SuppressWarnings("serial")
  private class BarCodeAlreadyUsedError implements IValidationError, Serializable {

    private String id;

    public BarCodeAlreadyUsedError(String id) {
      this.id = id;
    }

    public String getErrorMessage(IErrorMessageSource messageSource) {
      StringResourceModel strModel = new StringResourceModel("BarCodeAlreadyUsed", AssignCodeToParticipantPanel.this, new Model(new ValueMap("id=" + id)));
      return strModel.getString();
    }
  }

  @SuppressWarnings("serial")
  private class ParticipantAlreadyReceivedError implements IValidationError, Serializable {

    public String getErrorMessage(IErrorMessageSource messageSource) {
      String cancelButtonLabel = new StringResourceModel("Cancel", AssignCodeToParticipantPanel.this, null).getString();
      return new StringResourceModel("ParticipantAlreadyReceived", AssignCodeToParticipantPanel.this, null, new Object[] { cancelButtonLabel }).getString();
    }
  }

}
