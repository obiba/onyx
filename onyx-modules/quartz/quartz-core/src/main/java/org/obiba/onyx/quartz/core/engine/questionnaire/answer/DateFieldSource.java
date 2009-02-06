/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.engine.questionnaire.answer;

import java.util.Calendar;
import java.util.Date;

import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;

/**
 * class to extract a special field (YEAR, MONTH, DAY, HOUR, etc. as specified by Calendar) from date provided by any
 * source
 */
public class DateFieldSource extends AbstractDataSourceDataModifier {

  private static final long serialVersionUID = 1L;

  private int field;

  /**
   * Constructor, given a field.
   * @param dataSource
   * @param field
   */
  public DateFieldSource(DataSource dataSource, int field) {
    super();
    this.innerSource = dataSource;
    this.field = field;
  }

  @Override
  protected Data modify(Data data) {
    if(data.getType().equals(DataType.DATE)) {
      Date date = data.getValue();
      Calendar cal = Calendar.getInstance();
      cal.setTime(date);
      return (DataBuilder.buildInteger((long) cal.get(getField())));
    }
    return null;
  }

  public String getUnit() {
    return null;
  }

  public int getField() {
    return field;
  }

  public void setField(int field) {
    this.field = field;
  }
}
