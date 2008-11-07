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

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.form.Button;
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

  private Page sourcePage;

  private static final long serialVersionUID = 1L;

  public AssignCodeToParticipantPanel(String id, IModel participantModel, Page sourcePage) {

    super(id);

    this.sourcePage = sourcePage;

    add(new AssignCodeToParticipantForm("assignCodeToParticipantForm", participantModel));

  }

  private class AssignCodeToParticipantForm extends Form {

    private static final long serialVersionUID = 1L;

    @SuppressWarnings("serial")
    public AssignCodeToParticipantForm(String id, final IModel participantModel) {
      super(id);
      final Participant participantTemplate = new Participant();

      TextField participantCode = new TextField("participantCode", new PropertyModel(participantTemplate, "barcode"));
      participantCode.add(new RequiredFormFieldBehavior());
      participantCode.add(new IValidator() {

        public void validate(final IValidatable validatable) {
          Participant template = new Participant();
          template.setBarcode((String) validatable.getValue());
          if(queryService.count(template) > 0) {
            validatable.error(new ParticipantIDValidationError((String) validatable.getValue()));
          }
        }

      });
      add(participantCode);

      // TODO comment at reception time
      final Model receptionCommentModel = new Model();
      add(new TextArea("comment", receptionCommentModel));

      add(new Button("submit", participantModel) {

        @Override
        public void onSubmit() {
          participantService.assignCodeToParticipant((Participant) participantModel.getObject(), participantTemplate.getBarcode(), (String) receptionCommentModel.getObject(), OnyxAuthenticatedSession.get().getUser());
          setResponsePage(sourcePage);
        }
      });

      add(new AjaxLink("cancel") {

        @Override
        public void onClick(AjaxRequestTarget target) {
          setResponsePage(sourcePage);
        }

      });

    }
  }

  @SuppressWarnings("serial")
  private class ParticipantIDValidationError implements IValidationError, Serializable {

    private String id;

    public ParticipantIDValidationError(String id) {
      this.id = id;
    }

    public String getErrorMessage(IErrorMessageSource messageSource) {
      StringResourceModel strModel = new StringResourceModel("ParticipantIDAlreadyAssigned", AssignCodeToParticipantPanel.this, new Model(new ValueMap("id=" + id)));
      return strModel.getString();
    }

  }

}
