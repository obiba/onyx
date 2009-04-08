/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.util.data;

import java.util.Calendar;
import java.util.Date;

import junit.framework.Assert;

import org.junit.Test;
import org.obiba.onyx.util.DateUtil;

public class DateUtilTest {

  @Test
  public void testGetDaysBetween() {

    // StartDate = endDate, result should be equal to zero.
    Calendar calendar = Calendar.getInstance();
    Date currentDate = calendar.getTime();
    int daysBetween = DateUtil.getDaysBetween(currentDate, currentDate);
    Assert.assertEquals(0, daysBetween);

    // StartDate = current date, EndDate in the future.
    calendar.add(Calendar.DATE, 3);
    Date threeDaysInFuture = calendar.getTime();
    daysBetween = DateUtil.getDaysBetween(currentDate, threeDaysInFuture);
    Assert.assertEquals(3, daysBetween);

    // StartDate = current date, EndDate in the past.
    calendar.add(Calendar.DATE, -45);
    Date forthyTwoDaysInThePast = calendar.getTime();
    daysBetween = DateUtil.getDaysBetween(currentDate, forthyTwoDaysInThePast);
    Assert.assertEquals(-42, daysBetween);

    // Calculate the days between two dates in different months, EndDate in the future.
    calendar.set(2009, 01, 01);
    Date dateJanuary = calendar.getTime();
    calendar.set(2009, 02, 01);
    Date dateFebruary = calendar.getTime();
    daysBetween = DateUtil.getDaysBetween(dateJanuary, dateFebruary);
    Assert.assertEquals(28, daysBetween);

    // Calculate the days between two dates in different years, EndDate in the future.
    calendar.set(2009, 11, 23);
    Date dateYear2009 = calendar.getTime();
    calendar.set(2010, 00, 15);
    Date dateYear2010 = calendar.getTime();
    daysBetween = DateUtil.getDaysBetween(dateYear2009, dateYear2010);
    Assert.assertEquals(23, daysBetween);

    // Calculate the days between two dates in different years, EndDate in the past.
    // daysBetween = DateUtil.getDaysBetween(dateYear2010, dateYear2009);
    // Assert.assertEquals(-23, daysBetween);

  }
}
