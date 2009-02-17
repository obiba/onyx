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

import java.util.Calendar;

/**
 * An enum class that models the {@code Calendar} fields. It is primarily used for using a textual value instead of a
 * integer constant for using a particular field outside of Java source files. For example, in an XML file, one could
 * use &lt;field&gt;MONTH&lt;/field&gt; instead of &lt;field&gt;2&lt;/field&gt;.
 */
public enum DateField {

  AM_PM(Calendar.AM_PM), DATE(Calendar.DATE), DAY_OF_MONTH(Calendar.DAY_OF_MONTH), DAY_OF_WEEK(Calendar.DAY_OF_WEEK), DAY_OF_WEEK_IN_MONTH(Calendar.DAY_OF_WEEK_IN_MONTH), DAY_OF_YEAR(Calendar.DAY_OF_YEAR), DST_OFFSET(Calendar.DST_OFFSET), ERA(Calendar.ERA), HOUR(Calendar.HOUR), HOUR_OF_DAY(Calendar.HOUR_OF_DAY), MILLISECOND(Calendar.MILLISECOND), MINUTE(Calendar.MINUTE), MONTH(Calendar.MONTH), SECOND(Calendar.SECOND), WEEK_OF_MONTH(Calendar.WEEK_OF_MONTH), WEEK_OF_YEAR(Calendar.WEEK_OF_YEAR), YEAR(Calendar.YEAR), ZONE_OFFSET(Calendar.ZONE_OFFSET);

  private int field;

  private DateField(int calendarField) {
    this.field = calendarField;
  }

  /**
   * Returns the integer constant as specified in the {@code Calendar} class.
   * @return
   */
  public int toCalendarField() {
    return field;
  }

  /**
   * Converts an integer constant to the proper {@code DateField} instance for that constant.
   * 
   * @param field the integer constant to lookup
   * @return the {@code DateField} instance
   * @throws IllegalArgumentException when the field constant is unknown
   */
  public static DateField fromField(int field) {
    for(DateField dateField : DateField.values()) {
      if(dateField.field == field) {
        return dateField;
      }
    }
    throw new IllegalArgumentException("Unknown DateField " + field);
  }
}
