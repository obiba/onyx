/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.core.wicket;

import org.apache.wicket.model.IModel;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataType;

public class InstrumentRunValueDataModel implements IModel<Data> {

  private static final long serialVersionUID = 1L;

  private IModel<InstrumentRunValue> instrumentRunValueModel;

  private DataType dataType;

  public InstrumentRunValueDataModel(IModel<InstrumentRunValue> instrumentRunValueModel, DataType dataType) {
    this.instrumentRunValueModel = instrumentRunValueModel;
    this.dataType = dataType;
  }

  public Data getObject() {
    InstrumentRunValue instrumentRunValue = (InstrumentRunValue) instrumentRunValueModel.getObject();
    return instrumentRunValue.getData(dataType);
  }

  public void setObject(Data object) {
    InstrumentRunValue instrumentRunValue = (InstrumentRunValue) instrumentRunValueModel.getObject();
    instrumentRunValue.setData((Data) object);
  }

  public void detach() {
    instrumentRunValueModel.detach();
  }
}
