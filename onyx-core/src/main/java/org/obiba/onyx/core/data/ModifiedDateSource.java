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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;

/**
 * class used to modify a given date: some amounts can be added to various fields (see Calendar)
 */
public class ModifiedDateSource extends AbstractDataSourceDataModifier {

  private static final long serialVersionUID = 1L;

  private List<DateModifier> dateModifiers;

  @Override
  protected Data modify(Data data, Participant participant) {

    if(data == null) return null;
    if(!data.getType().equals(DataType.DATE)) throw new IllegalArgumentException("DataType " + DataType.DATE + " expected, " + data.getType() + " received.");

    Calendar cal = Calendar.getInstance();
    cal.setTime((Date) data.getValue());

    for(DateModifier dateModifier : getDateModifiers()) {
      dateModifier.modify(cal, participant);
    }

    return DataBuilder.buildDate(cal.getTime());
  }

  /**
   * Constructor
   * @param dataSource
   */
  public ModifiedDateSource(IDataSource iDataSource, List<DateModifier> dateModifiers) {
    super(iDataSource);
    this.dateModifiers = dateModifiers;
  }

  public List<DateModifier> getDateModifiers() {
    return (dateModifiers != null) ? dateModifiers : new ArrayList<DateModifier>();
  }

  public void addDateModifiers(DateModifier dateModifier) {
    if(dateModifier != null) getDateModifiers().add(dateModifier);
  }

  @Override
  public String toString() {
    String rval = "ModifiedDate[" + super.toString();
    if(getDateModifiers().size() > 0) {
      rval += ", " + getDateModifiers();
    }
    rval += "]";
    return rval;
  }
}
