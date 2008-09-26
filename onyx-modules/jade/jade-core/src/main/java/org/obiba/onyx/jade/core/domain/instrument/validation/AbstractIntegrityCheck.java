package org.obiba.onyx.jade.core.domain.instrument.validation;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.obiba.core.domain.AbstractEntity;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameter;
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;
import org.obiba.onyx.jade.core.service.InstrumentRunService;
import org.obiba.onyx.util.data.Data;

@Entity(name = "IntegrityCheck")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "integrity_check_type", discriminatorType = DiscriminatorType.STRING, length = 100)
public abstract class AbstractIntegrityCheck extends AbstractEntity implements IntegrityCheck {

  @ManyToOne
  @JoinColumn(name = "instrument_parameter_id")
  private InstrumentParameter targetParameter;

  public void setTargetParameter(InstrumentParameter targetParameter) {
    this.targetParameter = targetParameter;
  }

  public InstrumentParameter getTargetParameter() {
    return targetParameter;
  }

  //
  // IntegrityCheck Methods
  //

  public abstract boolean checkParameterValue(Data paramData, InstrumentRunService runService, ActiveInstrumentRunService activeRunService);
}