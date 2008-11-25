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
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.ruby.core.domain.RegisteredParticipantTube;
import org.obiba.onyx.ruby.core.service.ActiveTubeRegistrationService;

public class CommentPanel extends Panel {

  private static final long serialVersionUID = 3243362200850374728L;

  @SpringBean(name = "activeTubeRegistrationService")
  private ActiveTubeRegistrationService activeTubeRegistrationService;

  private String selectedComment;

  /**
   * Panel for the comment input
   * @param id
   * @param rowModel
   */
  public CommentPanel(String id, IModel rowModel) {
    super(id, rowModel);
    setOutputMarkupId(true);

    RegisteredParticipantTube registeredParticipantTube = (RegisteredParticipantTube) rowModel.getObject();

    if(registeredParticipantTube.getComment() != null) selectedComment = registeredParticipantTube.getComment();

    TextField commentField = new TextField("comment", new PropertyModel(this, "selectedComment"));
    commentField.add(new AjaxFormComponentUpdatingBehavior("onblur") {

      private static final long serialVersionUID = 1L;

      @Override
      protected void onUpdate(AjaxRequestTarget target) {
        RegisteredParticipantTube registeredParticipantTube = (RegisteredParticipantTube) getModelObject();
        activeTubeRegistrationService.setTubeComment(registeredParticipantTube.getBarcode(), selectedComment);

        // Update component
        target.addComponent(CommentPanel.this);
      }

    });

    add(commentField);
  }

  public String getSelectedComment() {
    return selectedComment;
  }

  public void setSelectedComment(String comment) {
    this.selectedComment = comment;
  }

}
