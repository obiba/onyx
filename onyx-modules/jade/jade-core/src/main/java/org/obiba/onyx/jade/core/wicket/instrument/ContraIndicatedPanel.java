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

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.core.domain.contraindication.Contraindication;
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;

public class ContraIndicatedPanel extends Panel {

  private static final long serialVersionUID = 9014406108097758044L;

  @SpringBean
  private ActiveInstrumentRunService activeInstrumentRunService;

  public ContraIndicatedPanel(String id) {
    super(id);

    Contraindication ci = activeInstrumentRunService.getContraindication();

    /*
     * String reason = ci.getDescription(); if(ci.getName().equals("Other")) { reason += " (" +
     * activeInstrumentRunService.getOtherContraIndication() + ")"; }
     * 
     * add(new Label("label", new StringResourceModel("ReasonForContraIndication", this, new Model(new ValueMap("ci=" +
     * reason)))));
     */
  }

}
