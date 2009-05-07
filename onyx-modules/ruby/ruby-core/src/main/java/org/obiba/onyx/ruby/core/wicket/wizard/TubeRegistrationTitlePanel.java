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
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.ruby.core.service.ActiveTubeRegistrationService;
import org.obiba.onyx.wicket.model.SpringStringResourceModel;

public class TubeRegistrationTitlePanel extends Panel {

  @SpringBean
  ActiveTubeRegistrationService tubeRegistration;

  private static final long serialVersionUID = 1L;

  @SuppressWarnings("serial")
  public TubeRegistrationTitlePanel(String id) {

    super(id);

    add(new Label("tubeCountLabel", new SpringStringResourceModel("Ruby.RegisteredParticipantTubeList")));

    Label tubeCount = new Label("tubeCount", new Model() {
      @Override
      public Object getObject() {
        return tubeRegistration.getRegisteredTubeCount();
      }
    });
    tubeCount.setOutputMarkupId(true);
    add(tubeCount);

  }

}
