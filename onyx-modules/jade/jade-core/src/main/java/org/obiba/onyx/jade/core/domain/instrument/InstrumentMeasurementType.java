/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.core.domain.instrument;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.obiba.core.domain.AbstractEntity;

@Entity
@Table
// (uniqueConstraints = { @UniqueConstraint(columnNames = { "instrument_id, type" }) })
public class InstrumentMeasurementType extends AbstractEntity {

  private static final long serialVersionUID = 1L;

  @ManyToOne
  @JoinColumn(name = "instrument_id", nullable = false)
  private Instrument instrument;

  @Column(length = 200, nullable = false)
  private String type;

  public InstrumentMeasurementType() {
  }

  public InstrumentMeasurementType(Instrument instrument, String type) {
    this.instrument = instrument;
    this.type = type;
  }

  public Instrument getInstrument() {
    return instrument;
  }

  public void setInstrument(Instrument instrument) {
    this.instrument = instrument;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

}
