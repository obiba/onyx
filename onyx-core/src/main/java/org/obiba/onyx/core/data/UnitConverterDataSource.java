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

import javax.measure.unit.Dimension;
import javax.measure.unit.Unit;

import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;

/**
 * class used to modify the data unit: use JScience to convert a value to a different unit
 */
public class UnitConverterDataSource extends AbstractDataSourceDataModifier {

  private static final long serialVersionUID = 1L;

  private String unit;

  @Override
  protected Data modify(Data data, Participant participant) {

    if(data == null) return null;
    if(!data.getType().isNumberType()) throw new IllegalArgumentException("DataType of number kind expected, " + data.getType() + " received.");
    if(getDataSource().getUnit() == null) throw new IllegalArgumentException("Unit source cannot be null.");

    Unit<?> sourceUnit = Unit.valueOf(getDataSource().getUnit());
    Unit<?> targetUnit = Unit.valueOf(unit);

    double newValue = sourceUnit.getConverterTo(targetUnit).convert(Double.parseDouble(data.getValueAsString()));
    if(targetUnit.getDimension().equals(Dimension.TIME)) return DataBuilder.buildInteger(Math.round(Math.floor(newValue)));
    if(data.getType().equals(DataType.INTEGER)) return DataBuilder.buildInteger(Math.round(newValue));

    return DataBuilder.build(data.getType(), String.valueOf(newValue));
  }

  @Override
  public String getUnit() {
    return unit.toString();
  }

  /**
   * Constructor, given a type.
   * @param dataSource
   * @param unit
   */
  public UnitConverterDataSource(IDataSource iDataSource, String unit) {
    super(iDataSource);
    if(unit == null) throw new IllegalArgumentException("Target unit cannot be null.");
    this.unit = unit;
  }

}
