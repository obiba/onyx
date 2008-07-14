package org.obiba.onyx.jade.core.domain.instrument;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Index;
import org.obiba.core.domain.AbstractEntity;

@Entity
public class InstrumentType extends AbstractEntity {

  private static final long serialVersionUID = 23414234L;

  @Column(length = 200)
  @Index(name = "name_index")
  private String name;

  @Column(length = 200)
  private String description;

  @OneToMany(mappedBy = "instrumentType")
  @Cascade( { CascadeType.SAVE_UPDATE, CascadeType.DELETE_ORPHAN })
  private List<Instrument> instruments;

  public InstrumentType() {
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
}
