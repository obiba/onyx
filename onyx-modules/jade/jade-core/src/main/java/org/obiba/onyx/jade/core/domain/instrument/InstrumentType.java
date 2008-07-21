package org.obiba.onyx.jade.core.domain.instrument;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.obiba.onyx.engine.Stage;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "name" }) })
public class InstrumentType extends Stage {

  private static final long serialVersionUID = 23414234L;

  @OneToMany(mappedBy = "instrumentType")
  private List<Instrument> instruments;

  @ManyToMany
  @JoinTable(name = "instrument_type_dependencies", joinColumns = @JoinColumn(name = "instrument_type_id"), inverseJoinColumns = @JoinColumn(name = "depends_on_instrument_type_id"))
  private List<InstrumentType> dependsOnTypes;

  @ManyToMany(mappedBy = "dependsOnTypes")
  private List<InstrumentType> dependentTypes;

  public InstrumentType() {
    setModule("jade");
  }

  public InstrumentType(String name, String description) {
    setName(name);
    setDescription(description);
    setModule("jade");
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
    if (!this.equals(instrumentType)) {
      getDependentTypes().add(instrumentType);
      instrumentType.getDependsOnTypes().add(this);
    }
  }
  
  @Override
  public String toString() {
    return getName();
  }
}
