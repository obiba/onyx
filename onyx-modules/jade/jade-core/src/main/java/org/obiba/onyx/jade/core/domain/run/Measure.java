/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.core.domain.run;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.obiba.core.domain.AbstractEntity;
import org.obiba.onyx.core.domain.user.User;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameterCaptureMethod;

/**
 * 
 */
@Entity
public class Measure extends AbstractEntity {

  private static final long serialVersionUID = 1L;

  @ManyToOne
  @JoinColumn(name = "instrument_run_id")
  private InstrumentRun instrumentRun;

  @OneToMany(mappedBy = "measure", cascade = CascadeType.ALL)
  private List<InstrumentRunValue> instrumentRunValues;

  @ManyToOne
  @JoinColumn(name = "user_id")
  private User user;

  @Temporal(TemporalType.TIMESTAMP)
  private Date time;

  private String instrumentBarcode;

  @Enumerated(EnumType.STRING)
  private MeasureStatus status;

  public InstrumentRun getInstrumentRun() {
    return instrumentRun;
  }

  public void setInstrumentRun(InstrumentRun instrumentRun) {
    this.instrumentRun = instrumentRun;
  }

  public List<InstrumentRunValue> getInstrumentRunValues() {
    return instrumentRunValues != null ? instrumentRunValues : (instrumentRunValues = new ArrayList<InstrumentRunValue>());
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public Date getTime() {
    return time;
  }

  public void setTime(Date time) {
    this.time = time;
  }

  public String getInstrumentBarcode() {
    return instrumentBarcode;
  }

  public void setInstrumentBarcode(String instrumentBarcode) {
    this.instrumentBarcode = instrumentBarcode;
  }

  /**
   * Get the capture method used for this measure.
   * 
   * @return The capture method.
   */
  public InstrumentParameterCaptureMethod getCaptureMethod() {
    List<InstrumentRunValue> instrumentRunValues = getInstrumentRunValues();
    for(InstrumentRunValue instrumentRunValue : instrumentRunValues) {
      switch(instrumentRunValue.getCaptureMethod()) {
      case MANUAL:
      case AUTOMATIC:
        return instrumentRunValue.getCaptureMethod();
      default:
        break;
      }
    }
    return null;
  }

  public MeasureStatus getStatus() {
    return status;
  }

  public void setStatus(MeasureStatus status) {
    this.status = status;
  }

}
