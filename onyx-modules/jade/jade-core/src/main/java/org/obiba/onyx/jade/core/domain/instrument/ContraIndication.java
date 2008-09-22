package org.obiba.onyx.jade.core.domain.instrument;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Index;
import org.obiba.core.domain.AbstractEntity;

@Entity
public class ContraIndication extends AbstractEntity {

  private static final long serialVersionUID = 13324234234234L;

  @ManyToOne
  @JoinColumn(name = "instrument_id")
  private Instrument instrument;

  @Column(length = 200)
  @Index(name = "name_index")
  private String name;

  @Column(length = 200)
  private String description;

  @Enumerated(EnumType.STRING)
  private ContraIndicationType type;

  public Instrument getInstrument() {
    return instrument;
  }

  public void setInstrument(Instrument instrument) {
    this.instrument = instrument;
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

  public ContraIndicationType getType() {
    return type;
  }

  public void setType(ContraIndicationType type) {
    this.type = type;
  }

  @Override
  public String toString() {
    return getName();
  }

}
