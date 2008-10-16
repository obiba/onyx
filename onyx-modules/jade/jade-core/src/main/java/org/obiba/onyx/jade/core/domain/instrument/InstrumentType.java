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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Index;
import org.obiba.core.domain.AbstractEntity;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "name" }) })
public class InstrumentType extends AbstractEntity {

  private static final long serialVersionUID = 23414234L;

  @Column(length = 200)
  @Index(name = "name_index")
  private String name;

  @Column(length = 200)
  private String description;

  @OneToMany(mappedBy = "instrumentType")
  private List<Instrument> instruments;

  @ManyToMany
  @JoinTable(name = "instrument_type_dependencies", joinColumns = @JoinColumn(name = "instrument_type_id"), inverseJoinColumns = @JoinColumn(name = "depends_on_instrument_type_id"))
  private List<InstrumentType> dependsOnTypes;

  @ManyToMany(mappedBy = "dependsOnTypes")
  private List<InstrumentType> dependentTypes;

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

  public List<InstrumentType> getDependsOnTypes() {
    return dependsOnTypes != null ? dependsOnTypes : (dependsOnTypes = new ArrayList<InstrumentType>());
  }

  public List<InstrumentType> getDependentTypes() {
    return dependentTypes != null ? dependentTypes : (dependentTypes = new ArrayList<InstrumentType>());
  }

  public void addDependentType(InstrumentType instrumentType) {
    if(!this.equals(instrumentType)) {
      getDependentTypes().add(instrumentType);
      instrumentType.getDependsOnTypes().add(this);
    }
  }

  @Override
  public String toString() {
    return name;
  }
}
