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

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.obiba.onyx.core.service.UserSessionService;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;

/**
 * class used to modify the data type: When primary data source provides the data in a type that is not suitable, use
 * this to, for instance, round a DECIMAL to a INTEGER, parse a TEXT as an INTEGER, turn a TEXT to a DATA etc.
 */
public class TypeConverterDataSource extends AbstractDataSourceDataModifier {

  private static final long serialVersionUID = 1L;

  private DataType type;

  @Override
  protected Data modify(Data data) {

    if(data == null) return null;
    String value = data.getValueAsString();
    if(value == null || value.length() == 0) return null;

    if(type.equals(DataType.DATE)) {
      try {
        DateFormat format = new SimpleDateFormat(UserSessionService.DEFAULT_DATE_FORMAT_PATTERN);
        return DataBuilder.buildDate(format.parse(value));
      } catch(Exception e) {
        throw new IllegalArgumentException("Data cannot be parsed in type DATE: wrong format " + value);
      }
    } else if(type.equals(DataType.INTEGER)) {
      return DataBuilder.buildInteger(Long.valueOf(Math.round(Double.parseDouble(value))));
    } else {
      return DataBuilder.build(type, value);
    }
  }

  /**
   * Constructor, given a type.
   * @param dataSource
   * @param type
   */
  public TypeConverterDataSource(IDataSource iDataSource, DataType type) {
    super(iDataSource);
    if(type == null) throw new IllegalArgumentException("DataType cannot be null.");
    this.type = type;
  }
}
