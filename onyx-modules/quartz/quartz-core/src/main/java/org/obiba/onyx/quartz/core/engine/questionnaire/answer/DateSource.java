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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;

/**
 * Date source, based on current date if date not specified, support date modifiers.
 * 
 * @see Calendar#add(int, int)
 */
public class DateSource extends DataSource {

  private static final long serialVersionUID = 1L;

  private Date date;

  private List<DateModifier> dateModifiers;

  /**
   * Constructor, with current date.
   */
  public DateSource() {
    this.date = null;
  }

  /**
   * Constructor, given a date.
   * @param date
   */
  public DateSource(Date date) {
    Calendar calendar = GregorianCalendar.getInstance();
    calendar.setTime(date);
    this.date = calendar.getTime();
  }

  /**
   * Modify the date before returning the data.
   * @param field the calendar field
   * @param amount the amount of date or time to be added to the field (can be negative).
   * @return this for chaining
   * @see Calendar#add(int, int)
   */
  public DateSource setDateModifier(int field, int amount) {
    if(dateModifiers == null) {
      dateModifiers = new ArrayList<DateModifier>();
    }
    dateModifiers.add(new DateModifier(field, amount));
    return this;
  }

  @Override
  public Data getData(ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService) {
    Date dataDate = date;
    if(dataDate == null) {
      // get current date each time it is asked for
      dataDate = new Date();
    }

    if(dateModifiers != null) {
      Calendar calendar = GregorianCalendar.getInstance();
      calendar.setTime(dataDate);
      for(DateModifier dateModifier : dateModifiers) {
        calendar.add(dateModifier.getField(), dateModifier.getAmount());
      }
      return DataBuilder.buildDate(calendar.getTime());
    } else {
      return DataBuilder.buildDate(dataDate);
    }
  }

  public String getUnit() {
    // TODO Auto-generated method stub
    return null;
  }

}
