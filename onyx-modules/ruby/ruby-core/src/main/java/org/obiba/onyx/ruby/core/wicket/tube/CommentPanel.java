/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.ruby.core.wicket.tube;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.validator.StringValidator;
import org.obiba.onyx.ruby.core.domain.RegisteredParticipantTube;
import org.obiba.onyx.ruby.core.service.ActiveTubeRegistrationService;
import org.obiba.onyx.wicket.behavior.EnterOnKeyPressBehaviour;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommentPanel extends Panel {
  //
  // Constants
  //

  private static final long serialVersionUID = 3243362200850374728L;

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(CommentPanel.class);

  //
  // Instance Variables
  //

  @SpringBean(name = "activeTubeRegistrationService")
  private ActiveTubeRegistrationService activeTubeRegistrationService;

  private TextField commentField;

  private AjaxSubmitLink submitLink;

  //
  // Constructors
  //

  public CommentPanel(String id, IModel rowModel) {
    super(id, rowModel);
    setOutputMarkupId(true);

    add(new CommentForm("commentForm"));
  }

  //
  // Inner Classes
  //

  private class CommentForm extends Form {
    //
    // Constants
    //

    private static final long serialVersionUID = 1L;

    //
    // Constructors
    //

    public CommentForm(String id) {
      super(id);

      addSubmitLink();
      addCommentField();
    }

    //
    // Methods
    //

    private void addSubmitLink() {
      submitLink = new AjaxSubmitLink("submitTubeComment") {
        private static final long serialVersionUID = 1L;

        @Override
        protected void onSubmit(AjaxRequestTarget target, Form form) {
          submitComment();
        }
      };

      submitLink.setOutputMarkupId(true);

      add(submitLink);
    }

    private void addCommentField() {
      RegisteredParticipantTube registeredParticipantTube = (RegisteredParticipantTube) CommentPanel.this.getModelObject();

      commentField = new TextField("comment", new Model(registeredParticipantTube.getComment()));

      commentField.add(new StringValidator.MaximumLengthValidator(2000));

      commentField.add(new EnterOnKeyPressBehaviour(submitLink));

      commentField.add(new AjaxFormComponentUpdatingBehavior("onblur") {
        private static final long serialVersionUID = 1L;

        @Override
        protected void onUpdate(final AjaxRequestTarget target) {
          submitComment();
        }
      });

      add(commentField);
    }

    private void submitComment() {
      String comment = commentField.getModelObjectAsString();

      if(comment != null && comment.trim().length() == 0) {
        comment = null;
      }

      RegisteredParticipantTube registeredParticipantTube = (RegisteredParticipantTube) CommentPanel.this.getModelObject();
      activeTubeRegistrationService.setTubeComment(registeredParticipantTube.getBarcode(), comment);
    }
  }
}