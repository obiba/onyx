package org.obiba.onyx.jade.core.domain.instrument;

import java.util.List;
import java.util.ArrayList;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;

import org.hibernate.annotations.Index;
import org.obiba.core.domain.AbstractEntity;

@Entity
public class ContraIndication extends AbstractEntity {

  private static final long serialVersionUID = 13324234234234L;

  @ManyToMany(mappedBy = "contraIndications")
  private List<InstrumentType> instrumentTypes;

  @Column(length = 200)
  @Index(name = "name_index")
  private String name;

  @Column(length = 200)
  private String description;

  public List<InstrumentType> getInstrumentTypes() {
    return instrumentTypes != null ? instrumentTypes : (instrumentTypes = new ArrayList<InstrumentType>());
  }

  public void addInstrumentType(InstrumentType instrumentType) {
    if (instrumentType != null) {
      getInstrumentTypes().add(instrumentType);
    }
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

  @Override
  public String toString() {
    return getName();
  }
  
}
