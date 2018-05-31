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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 
 */
public class AppointmentUpdateLog implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public enum Level {
    INFO, WARN, ERROR
  }

  @JsonProperty
  private Date date;

  @JsonProperty
  private Level level;

  @JsonProperty
  private String participantId;

  @JsonProperty
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

  @JsonCreator
  public AppointmentUpdateLog(@JsonProperty("date") Date date, @JsonProperty("level") Level level, @JsonProperty("message") String participantId, @JsonProperty("participantId") String message) {
    this.date = date;
    this.level = level;
    this.participantId = participantId;
    this.message = message;
  }

  //
  // Static methods
  //

  /**
   * Adds {@link AppointmentUpdateLog}s with the level {@link AppointmentUpdateLog.Level}=ERROR to the Spring Batch
   * {@link ExecutionContext}.
   * @param context Spring Batch ExecutionContext.
   * @param appointmentUpdateLog A single appointment update log entry.
   */
  @SuppressWarnings("unchecked")
  public static void addLog(ExecutionContext context, AppointmentUpdateLog appointmentUpdateLog) {
    if(context == null) return;
    if(context.get("logList") == null) context.put("logList", new ArrayList<AppointmentUpdateLog>());
    if(appointmentUpdateLog != null) {
      ((ArrayList<AppointmentUpdateLog>) context.get("logList")).add(appointmentUpdateLog);
    }
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

  @JsonIgnore
  public String getFormatedDate() {
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    return formatter.format(getDate());
  }

  @JsonIgnore
  public String getFormatedTime() {
    SimpleDateFormat formatter = new SimpleDateFormat("hh:mm:ss");
    return formatter.format(getDate());
  }

}
