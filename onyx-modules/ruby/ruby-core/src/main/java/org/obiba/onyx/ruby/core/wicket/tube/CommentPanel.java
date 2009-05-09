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

import java.util.Map;

import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.validator.StringValidator;
import org.obiba.onyx.ruby.core.domain.RegisteredParticipantTube;
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

  private TextArea commentField;

  //
  // Constructors
  //

  public CommentPanel(String id, IModel rowModel) {
    super(id, rowModel);
    setOutputMarkupId(true);

    addCommentField();
  }

  @SuppressWarnings("serial")
  private void addCommentField() {
    RegisteredParticipantTube registeredParticipantTube = (RegisteredParticipantTube) CommentPanel.this.getModelObject();

    commentField = new TextArea("comment", new Model(registeredParticipantTube.getComment()));

    commentField.add(new StringValidator.MaximumLengthValidator(2000) {

      @Override
      protected Map variablesMap(IValidatable validatable) {
        Map map = super.variablesMap(validatable);
        map.put("barcode", ((RegisteredParticipantTube) CommentPanel.this.getModelObject()).getBarcode());
        return map;
      }

    });

    add(commentField);
  }

  public TextArea getCommentField() {
    return commentField;
  }

}
