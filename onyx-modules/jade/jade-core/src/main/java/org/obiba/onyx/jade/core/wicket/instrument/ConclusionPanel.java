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

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.obiba.onyx.jade.core.domain.run.InstrumentRun;
import org.obiba.onyx.jade.core.wicket.run.InstrumentRunPanel;

public class ConclusionPanel extends Panel {

  private static final long serialVersionUID = 3008363510160516288L;

  private static final int DEFAULT_INITIAL_HEIGHT = 420;

  private static final int DEFAULT_INITIAL_WIDTH = 400;

  @SuppressWarnings("serial")
  public ConclusionPanel(String id) {
    super(id);
    setOutputMarkupId(true);
    InstrumentRunPanel instrumentRunPanel = new InstrumentRunPanel("instrumentRun");
    instrumentRunPanel.add(new AttributeModifier("class", true, new Model("obiba-content instrument-run-panel-content")));
    add(instrumentRunPanel);

    add(new Label("title", instrumentRunPanel.getTitle((InstrumentRun) instrumentRunPanel.getDefaultModelObject())));
  }

}
