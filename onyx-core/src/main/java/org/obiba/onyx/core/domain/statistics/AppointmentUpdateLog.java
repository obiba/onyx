/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.domain.statistics;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.springframework.batch.item.ExecutionContext;

/**
 * 
 */
public class AppointmentUpdateLog implements Serializable {

  public enum Level {
    INFO, WARN, ERROR;
  }

  private Date date;

  private Level level;

  private String participantId;

  private String message;

  //
  // Contructors
  //
  public AppointmentUpdateLog(Date date, Level level, String message) {
    this.date = date;
    this.level = level;
    this.message = message;
    this.participantId = null;
  }

  public AppointmentUpdateLog(Date date, Level level, String participantId, String message) {
    this.date = date;
    this.level = level;
    this.participantId = participantId;
    this.message = message;
  }

  //
  // Static methods
  //
  @SuppressWarnings("unchecked")
  public static void addLog(ExecutionContext context, AppointmentUpdateLog appointmentUpdateLog) {
    if(context == null) return;
    if(context.get("logList") == null) context.put("logList", new ArrayList<AppointmentUpdateLog>());
    ((ArrayList<AppointmentUpdateLog>) context.get("logList")).add(appointmentUpdateLog);
  }

  //
  // Getters & Setters
  //
  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }

  public Level getLevel() {
    return level;
  }

  public void setLevel(Level level) {
    this.level = level;
  }

  public String getParticipantId() {
    return participantId;
  }

  public void setParticipantId(String participantId) {
    this.participantId = participantId;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  };

  public String getFormatedDate() {
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    return formatter.format(getDate());
  }

  public String getFormatedTime() {
    SimpleDateFormat formatter = new SimpleDateFormat("hh:mm:ss");
    return formatter.format(getDate());
  }

}
