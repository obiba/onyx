/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.util;

import java.util.Calendar;
import java.util.Date;

public class DateUtil {

  /**
   * Returns the number of calendar days between two dates.
   * 
   * @param startDate start date
   * @param endDate end date
   * @return number of days between start date and end date (<code>-1</code> if the former is after the latter)
   */
  public static int getDaysBetween(Date startDate, Date endDate) {
    int daysBetween = -1;

    Calendar c = Calendar.getInstance();

    if(startDate.after(endDate)) return daysBetween;

    c.setTime(startDate);
    int startYear = c.get(Calendar.YEAR);
    int startDay = c.get(Calendar.DAY_OF_YEAR);

    c.setTime(endDate);
    int endYear = c.get(Calendar.YEAR);
    int endDay = c.get(Calendar.DAY_OF_YEAR);

    if(startYear == endYear) {
      daysBetween = endDay - startDay;
    } else {
      c.setTime(startDate);
      daysBetween = c.getActualMaximum(Calendar.DAY_OF_YEAR) - startDay;

      for(int year = startYear + 1; year < endYear; year++) {
        c.set(Calendar.YEAR, year);
        daysBetween += c.getActualMaximum(Calendar.DAY_OF_YEAR);
      }

      daysBetween += endDay;
    }

    return daysBetween;
  }
}
