/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.data;

import java.io.Serializable;

import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;

/**
 * Get a fix data.
 */
public class FixedDataSource implements IDataSource {

  private static final long serialVersionUID = 1L;

  private Data data;

  private String unit;

  public FixedDataSource(Serializable value) {
    this(DataBuilder.build(value));
  }

  public FixedDataSource(DataType type, String value) {
    this(DataBuilder.build(type, value));
  }

  public FixedDataSource(Data data) {
    this.data = data;
    this.unit = null;
  }

  public FixedDataSource(Data data, String unit) {
    this.data = data;
    this.unit = unit;
  }

  @Override
  public Data getData(Participant participant) {
    return data;
  }

  @Override
  public String getUnit() {
    return unit;
  }

  @Override
  public String toString() {
    return "Data" + data;
  }
}
