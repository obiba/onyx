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
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.value.ValueMap;
import org.obiba.onyx.ruby.core.service.ActiveTubeRegistrationService;

public class ValidationPanel extends Panel {
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

    add(new Label("registeredTubeCount", new StringResourceModel("RegisteredTubeCount", this, new Model(new ValueMap("count=" + activeTubeRegistrationService.getRegisteredTubeCount())))));
    add(new Label("expectedTubeCount", new StringResourceModel("ExpectedTubeCount", this, new Model(new ValueMap("count=" + activeTubeRegistrationService.getExpectedTubeCount())))));
  }
}
