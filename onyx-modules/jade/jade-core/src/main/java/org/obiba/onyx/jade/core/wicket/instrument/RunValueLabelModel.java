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