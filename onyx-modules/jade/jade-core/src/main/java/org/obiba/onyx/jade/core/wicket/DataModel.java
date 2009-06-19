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
import org.obiba.onyx.util.data.Data;

public class DataModel implements IModel {

  private static final long serialVersionUID = 1L;

  private Data data;

  public DataModel(Data data) {
    this.data = data;
  }

  public Object getObject() {
    return data;
  }

  public void setObject(Object object) {
    data = (Data) object;
  }

  public void detach() {
    data = null;
  }

}
