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

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.service.ParticipantRegistry;
import org.obiba.onyx.core.service.impl.NoSuchParticipantException;
import org.obiba.onyx.core.service.impl.ParticipantRegistryLookupException;
import org.obiba.onyx.wicket.behavior.ExecuteJavaScriptBehaviour;

/**
 * Participant registry search field, error messages and participant lookup method. Error messages fade after being
 * displayed for 3 seconds.
 */
public class ParticipantRegistrySearchPanel extends Panel {
  private static final long serialVersionUID = 1L;

  @SpringBean
  private ParticipantRegistry participantRegistry;

  private TextField<String> uniqueIdSearchField;

  private Label label;

  private Label messageLabel;

  ParticipantRegistrySearchPanel(String id) {
    super(id);

    label = new Label("search-instructions", new ResourceModel("SearchInstructions"));
    label.setOutputMarkupId(true);
    add(label);
    addForm();
    messageLabel = new Label("search-messages", "              ");
    messageLabel.setOutputMarkupId(true);
    add(messageLabel);
  }

  private void addForm() {
    uniqueIdSearchField = new TextField<String>("inputField", new Model<String>(""));
    add(uniqueIdSearchField);
  }

  public String getUniqueId() {
    return uniqueIdSearchField.getModelObject();
  }

  public Participant lookupParticipant() throws NoSuchParticipantException, ParticipantRegistryLookupException {
    return participantRegistry.lookupParticipant(getUniqueId());
  }

  public void setMessage(String string) {
    messageLabel.setDefaultModel(new ResourceModel(string));
    messageLabel.add(new ExecuteJavaScriptBehaviour("$('#" + messageLabel.getMarkupId() + "').show();setTimeout(\"$('#" + messageLabel.getMarkupId() + "').fadeOut(4000)\", \"3000\");"));
  }

  public void clearMessage() {
    messageLabel.setDefaultModel(new Model<String>(""));
  }

  public void reset() {
    uniqueIdSearchField.clearInput();
    uniqueIdSearchField.setDefaultModelObject("");
    messageLabel.setDefaultModel(new Model<String>(""));
  }
}
