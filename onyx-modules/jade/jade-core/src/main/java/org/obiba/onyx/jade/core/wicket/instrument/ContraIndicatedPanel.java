/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.core.wicket.instrument;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.SpringWebApplication;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.value.ValueMap;
import org.obiba.onyx.core.service.UserSessionService;
import org.obiba.onyx.jade.core.domain.instrument.ContraIndication;
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;

public class ContraIndicatedPanel extends Panel {

  private static final long serialVersionUID = 9014406108097758044L;

  @SpringBean
  private ActiveInstrumentRunService activeInstrumentRunService;

  @SpringBean(name = "userSessionService")
  private UserSessionService userSessionService;

  public ContraIndicatedPanel(String id) {
    super(id);

    ContraIndication ci = activeInstrumentRunService.getContraIndication();

    ci.setApplicationContext(((SpringWebApplication) getApplication()).getSpringContextLocator().getSpringContext());
    ci.setUserSessionService(userSessionService);

    String reason = ci.getDescription();
    if(ci.getName().equals("Other")) {
      reason += " (" + activeInstrumentRunService.getOtherContraIndication() + ")";
    }

    add(new Label("label", new StringResourceModel("ReasonForContraIndication", this, new Model(new ValueMap("ci=" + reason)))));
  }

}
