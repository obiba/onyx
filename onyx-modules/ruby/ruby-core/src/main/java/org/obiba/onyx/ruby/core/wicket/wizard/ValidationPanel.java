/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.ruby.core.wicket.wizard;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.ruby.core.service.ActiveTubeRegistrationService;
import org.obiba.onyx.wicket.model.SpringStringResourceModel;

public class ValidationPanel extends Panel {
  //
  // Constants
  //

  private static final long serialVersionUID = 1L;

  private static final String REGISTERED_TUBE_COUNT_RESOURCE_KEY = "Ruby.RegisteredTubeCount";

  private static final String EXPECTED_TUBE_COUNT_RESOURCE_KEY = "Ruby.ExpectedTubeCount";

  //
  // Instance Variables
  //

  @SpringBean
  protected ActiveTubeRegistrationService activeTubeRegistrationService;

  //
  // Constructors
  //

  public ValidationPanel(String id) {
    super(id);

    add(new Label("registeredTubeCount", new PropertyModel(this, "registeredTubeCountLabelText")));
    add(new Label("expectedTubeCount", new PropertyModel(this, "expectedTubeCountLabelText")));
  }

  //
  // Methods
  //

  @SuppressWarnings("unused")
  public String getRegisteredTubeCountLabelText() {
    int registeredTubeCount = activeTubeRegistrationService.getRegisteredTubeCount();
    SpringStringResourceModel model = new SpringStringResourceModel(REGISTERED_TUBE_COUNT_RESOURCE_KEY, new Object[] { registeredTubeCount }, REGISTERED_TUBE_COUNT_RESOURCE_KEY);

    return model.getString();
  }

  @SuppressWarnings("unused")
  public String getExpectedTubeCountLabelText() {
    int expectedTubeCount = activeTubeRegistrationService.getExpectedTubeCount();
    SpringStringResourceModel model = new SpringStringResourceModel(EXPECTED_TUBE_COUNT_RESOURCE_KEY, new Object[] { expectedTubeCount }, EXPECTED_TUBE_COUNT_RESOURCE_KEY);

    return model.getString();
  }
}