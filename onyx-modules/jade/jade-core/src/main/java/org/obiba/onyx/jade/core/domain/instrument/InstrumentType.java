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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Index;
import org.obiba.core.domain.AbstractEntity;
import org.obiba.onyx.core.domain.contraindication.Contraindication;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "name" }) })
public class InstrumentType extends AbstractEntity {

  private static final long serialVersionUID = 23414234L;

  @Column(length = 200)
  @Index(name = "name_index")
  private String name;

  @Column(length = 200)
  private String description;

  @OneToMany(cascade = CascadeType.ALL)
  @JoinColumn(name = "instrument_type_id")
  private List<Instrument> instruments;

  @OneToMany(cascade = CascadeType.ALL)
  @JoinColumn(name = "instrument_type_id")
  private List<InstrumentParameter> instrumentParameters;

  @OneToMany(cascade = CascadeType.ALL)
  private List<Contraindication> contraindications;

  public InstrumentType() {
  }

  public InstrumentType(String name, String description) {
    this.name = name;
    this.description = description;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public List<Instrument> getInstruments() {
    return instruments != null ? instruments : (instruments = new ArrayList<Instrument>());
  }

  public void addInstrument(Instrument instrument) {
    if(instrument != null) {
      getInstruments().add(instrument);
      instrument.setInstrumentType(this);
    }
  }

  public List<Contraindication> getContraindications() {
    return contraindications != null ? contraindications : (contraindications = new ArrayList<Contraindication>());
  }

  /**
   * 
   * @param ci
   * @return this for chaining
   */
  public InstrumentType addContraindication(Contraindication ci) {
    if(ci != null) {
      getContraindications().add(ci);
    }
    return this;
  }

  public List<InstrumentParameter> getInstrumentParameters() {
    return instrumentParameters != null ? instrumentParameters : (instrumentParameters = new ArrayList<InstrumentParameter>());
  }

  /**
   * 
   * @param parameter
   * @return this for chaining
   */
  public InstrumentType addInstrumentParameter(InstrumentParameter parameter) {
    if(parameter != null) {
      getInstrumentParameters().add(parameter);
      parameter.setInstrumentType(this);
    }
    return this;
  }

  @Override
  public String toString() {
    return name;
  }
}
