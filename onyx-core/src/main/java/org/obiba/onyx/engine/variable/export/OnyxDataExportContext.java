/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.engine.variable.export;

import java.util.Calendar;
import java.util.Date;

import org.obiba.onyx.core.domain.user.User;

/**
 * 
 */
public class OnyxDataExportContext {

  private String destination;

  private String exportingUserLogin;

  private Date timeStart;

  private Date timeEnd;

  public OnyxDataExportContext(String destination, User user) {
    this.destination = destination;
    this.exportingUserLogin = user.getLogin();
    this.timeStart = new Date();
  }

  public void endExport() {
    timeEnd = new Date();
  }

  public String getExportingUserLogin() {
    return exportingUserLogin;
  }

  public Date getTimeStart() {
    return timeStart;
  }

  public Date getTimeEnd() {
    return timeEnd;
  }

  public String getDestination() {
    return destination;
  }

  public int getExportYear() {
    return getCalendarPart(Calendar.YEAR);
  }

  public int getExportMonth() {
    return getCalendarPart(Calendar.MONTH);
  }

  public int getExportDay() {
    return getCalendarPart(Calendar.DAY_OF_MONTH);
  }

  protected int getCalendarPart(int part) {
    Calendar startDate = Calendar.getInstance();
    startDate.setTime(timeStart);
    return startDate.get(part);
  }
}
