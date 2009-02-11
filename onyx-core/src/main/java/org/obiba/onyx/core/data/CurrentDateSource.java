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

/**
 * Get the current date
 */
public class CurrentDateSource implements IDataSource {

  private Integer field;

  private List<DateModifier> dateModifiers;

  /**
   * returns {@link Data} containing the current complete date or a specified field of the current date
   * @param participant
   * @return
   */
  public Data getData(Participant participant) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(new Date());

    for(DateModifier dateModifier : getDateModifiers()) {
      dateModifier.modify(cal);
    }

    return (field == null) ? DataBuilder.buildDate(cal.getTime()) : DataBuilder.buildInteger(cal.get(field));
  }

  public String getUnit() {
    return null;
  }

  // Constructors
  public CurrentDateSource() {
    this.field = null;
  }

  public CurrentDateSource(Integer field) {
    this.field = field;
  }

  public CurrentDateSource(List<DateModifier> dateModifiers) {
    this.field = null;
    this.dateModifiers = dateModifiers;
  }

  public CurrentDateSource(Integer field, List<DateModifier> dateModifiers) {
    this.field = field;
    this.dateModifiers = dateModifiers;
  }

  public List<DateModifier> getDateModifiers() {
    return (dateModifiers != null) ? dateModifiers : new ArrayList<DateModifier>();
  }

  public void addDateModifiers(DateModifier dateModifier) {
    if(dateModifier != null) getDateModifiers().add(dateModifier);
  }
}
