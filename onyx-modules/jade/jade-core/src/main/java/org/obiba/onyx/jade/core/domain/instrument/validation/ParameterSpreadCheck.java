package org.obiba.onyx.jade.core.domain.instrument.validation;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameter;
import org.obiba.onyx.jade.core.domain.run.InstrumentRun;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;
import org.obiba.onyx.jade.core.service.InstrumentRunService;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataType;

@Entity
@DiscriminatorValue("ParameterSpreadCheck")
public class ParameterSpreadCheck extends AbstractIntegrityCheck implements IntegrityCheck {

  private static final long serialVersionUID = 1L;

  @Transient
  private RangeCheck rangeCheck;

  @ManyToOne
  private InstrumentParameter parameter;

  private Integer percent;

  public ParameterSpreadCheck() {
    rangeCheck = new RangeCheck();
  }

  public void setParameter(InstrumentParameter param) {
    this.parameter = param;
  }

  public InstrumentParameter getParameter() {
    return parameter;
  }

  public DataType getValueType() {
    return getTargetParameter().getDataType();
  }

  public void setPercent(Integer percent) {
    this.percent = percent;
  }

  public Integer getPercent() {
    return percent;
  }

  //
  // IntegrityCheck Methods
  //

  @Override
  public boolean checkParameterValue(Data paramData, InstrumentRunService runService, ActiveInstrumentRunService activeRunService) {
    // Get the other parameter's value.
    InstrumentRun instrumentRun = activeRunService.getInstrumentRun();
    InstrumentRunValue paramValue = runService.findInstrumentRunValue(instrumentRun.getParticipantInterview(), instrumentRun.getInstrument().getInstrumentType(), parameter.getName());

    // Update the rangeCheck accordingly.
    rangeCheck.setTargetParameter(getTargetParameter());

    if(getValueType().equals(DataType.INTEGER)) {
      initIntegerRangeCheck(paramData, paramValue.getData());
    } else if(getValueType().equals(DataType.DECIMAL)) {
      initDecimalRangeCheck(paramData, paramValue.getData());
    } else {
      return false;
    }

    return rangeCheck.checkParameterValue(paramData, null, null);
  }

  private void initIntegerRangeCheck(Data checkedData, Data otherData) {
    Long otherValue = otherData.getValue();

    double percentValue = percent / 100.0;

    Long minCheckedValue = new Double(Math.ceil((1.0 - percentValue) * otherValue.longValue())).longValue();
    Long maxCheckedValue = new Double(Math.floor((1.0 + percentValue) * otherValue.longValue())).longValue();

    rangeCheck.setIntegerMinValue(minCheckedValue);
    rangeCheck.setIntegerMaxValue(maxCheckedValue);
  }

  private void initDecimalRangeCheck(Data checkedData, Data otherData) {
    Double otherValue = otherData.getValue();

    double percentValue = percent / 100.0;

    Double minCheckedValue = (1.0 - percentValue) * otherValue;
    Double maxCheckedValue = (1.0 + percentValue) * otherValue;

    rangeCheck.setDecimalMinValue(minCheckedValue);
    rangeCheck.setDecimalMaxValue(maxCheckedValue);
  }
}