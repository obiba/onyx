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

import org.apache.wicket.model.Model;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;

public class RunValueLabelModel extends Model {

  private static final long serialVersionUID = 1338477100531673569L;

  public RunValueLabelModel(InstrumentRunValue runValue) {
    String unit = runValue.getInstrumentParameter().getMeasurementUnit();
    if(unit == null) unit = "";
    String label = runValue.getValue() == null ? unit : runValue.getData().getValueAsString() + " " + unit;
    setObject(label);
  }
  
}
