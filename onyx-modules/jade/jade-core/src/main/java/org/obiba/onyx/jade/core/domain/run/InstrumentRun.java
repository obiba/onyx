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
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import org.obiba.core.domain.AbstractEntity;
import org.obiba.onyx.core.domain.contraindication.Contraindication;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.domain.user.User;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameter;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "participant_id", "instrumentType" }) })
public class InstrumentRun extends AbstractEntity {

  private static final long serialVersionUID = -2756620040202577411L;

  @ManyToOne
  @JoinColumn(name = "participant_id", nullable = false)
  private Participant participant;

  @OneToMany(cascade = { CascadeType.REMOVE }, mappedBy = "instrumentRun")
  private List<Measure> measures;

  @OneToMany(cascade = { CascadeType.REMOVE }, mappedBy = "instrumentRun")
  private List<InstrumentRunValue> instrumentRunValues;

  /**
   * Name of the type of instrument used in this instrument run.
   */
  private String instrumentType;

  @ManyToOne
  @JoinColumn(name = "instrument_id", nullable = false)
  private Instrument instrument;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Enumerated(EnumType.STRING)
  private InstrumentRunStatus status;

  @Temporal(TemporalType.TIMESTAMP)
  private Date timeStart;

  @Temporal(TemporalType.TIMESTAMP)
  private Date timeEnd;

  private String contraindicationCode;

  @Column(length = 2000)
  private String otherContraindication;

  @Column(length = 2000)
  private String skipComment;

  public InstrumentRun() {
    super();
  }

  public Participant getParticipant() {
    return participant;
  }

  public void setParticipant(Participant participant) {
    this.participant = participant;
  }

  public Instrument getInstrument() {
    return instrument;
  }

  public void setInstrument(Instrument instrument) {
    this.instrument = instrument;
  }

  public List<Measure> getMeasures() {
    return measures != null ? measures : (measures = new ArrayList<Measure>());
  }

  public int getMeasureCount() {
    return getMeasures().size();
  }

  public List<InstrumentRunValue> getInstrumentRunValues() {
    return instrumentRunValues != null ? instrumentRunValues : (instrumentRunValues = new ArrayList<InstrumentRunValue>());
  }

  public void addInstrumentRunValue(InstrumentRunValue value) {
    if(value != null) {
      getInstrumentRunValues().add(value);
      value.setInstrumentRun(this);
    }
  }

  public InstrumentRunValue getInstrumentRunValue(InstrumentParameter instrumentParameter) {
    for(InstrumentRunValue runValue : getInstrumentRunValues()) {
      if(runValue.getInstrumentParameter().equals(instrumentParameter.getCode())) return runValue;
    }
    return null;
  }

  /**
   * Returns the name of type of instrument used in this instrument run.
   * 
   * @return name of associated instrument type
   */
  public String getInstrumentType() {
    return instrumentType;
  }

  public void setInstrumentType(String instrumentType) {
    this.instrumentType = instrumentType;
  }

  public InstrumentRunStatus getStatus() {
    return status;
  }

  public void setStatus(InstrumentRunStatus status) {
    this.status = status;
  }

  public Date getTimeStart() {
    return timeStart;
  }

  public void setTimeStart(Date timeStart) {
    this.timeStart = timeStart;
  }

  public Date getTimeEnd() {
    return timeEnd;
  }

  public void setTimeEnd(Date timeEnd) {
    this.timeEnd = timeEnd;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public String getContraindication() {
    return contraindicationCode;
  }

  public void setContraindication(Contraindication contraindication) {
    if(contraindication != null) {
      this.contraindicationCode = contraindication.getCode();
    } else {
      this.contraindicationCode = null;
    }
  }

  public String getOtherContraindication() {
    return otherContraindication;
  }

  public void setOtherContraindication(String reason) {
    otherContraindication = reason;
  }

  public boolean isCompletedOrContraindicated() {
    if(getStatus().equals(InstrumentRunStatus.COMPLETED) || getStatus().equals(InstrumentRunStatus.IN_PROGRESS) || getStatus().equals(InstrumentRunStatus.CONTRA_INDICATED)) {
      return true;
    }
    return false;
  }

  public String getSkipComment() {
    return skipComment;
  }

  public void setSkipComment(String skipComment) {
    this.skipComment = skipComment;
  }
}
