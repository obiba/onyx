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

import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.ResourceModel;

public class NoInstrumentAvailablePanel extends Panel {

  private static final long serialVersionUID = 1L;

  public NoInstrumentAvailablePanel(String id) {
    super(id);
    MultiLineLabel message;
    add(message = new MultiLineLabel("noInstrumentAvailableMessage", new ResourceModel("NoInstrumentAvailable")));
    message.setEscapeModelStrings(false);
  }
}
