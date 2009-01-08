/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.core.domain.instrument.validation;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameter;
import org.obiba.onyx.jade.core.domain.instrument.UnitParameterValueConverter;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;
import org.obiba.onyx.jade.core.service.InstrumentRunService;
import org.obiba.onyx.util.data.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Integrity check to verify that an instrument run value is equal to the value of another parameter.
 * 
 * The check fails (returns <code>false</code>) if the values are <i>not</i> equal.
 * 
 * @author cag-dspathis
 * 
 */
@Entity
@DiscriminatorValue("EqualsParameterCheck")
public class EqualsParameterCheck extends AbstractIntegrityCheck implements IntegrityCheck {

  private static final long serialVersionUID = 1L;

  private static final Logger log = LoggerFactory.getLogger(EqualsParameterCheck.class);

  @Transient
  private EqualsValueCheck equalsValueCheck;

  @ManyToOne
  private InstrumentParameter parameter;

  @Enumerated(EnumType.STRING)
  private ComparisonOperator operator;

  public EqualsParameterCheck() {
    equalsValueCheck = new EqualsValueCheck();
  }

  public void setParameter(InstrumentParameter param) {
    this.parameter = param;
  }

  public InstrumentParameter getParameter() {
    return this.parameter;
  }

  //
  // IntegrityCheck Methods
  //

  /**
   * Returns <code>true</code> if the specified instrument run value is equal to the value of the configured other
   * parameter.
   * 
   * @param runValue instrument run value
   * @param runService instrument run service
   * @return <code>true</code> if instrument run value equals value of configured other parameter
   */
  public boolean checkParameterValue(Data paramData, InstrumentRunService runService, ActiveInstrumentRunService activeRunService) {
    // If the other parameter has not been specified, there is nothing to check!
    if(parameter == null) {
      return true;
    }

    //
    // Get the other parameter's value.
    //
    log.debug("Retrieving parameter value : {}", parameter.getCode());
    InstrumentRunValue otherRunValue = activeRunService.getInstrumentRunValue(parameter);
    Data otherData = null;

    if(otherRunValue != null) {

      if(!otherRunValue.getDataType().equals(paramData.getType())) {
        InstrumentRunValue targetRunValue = new InstrumentRunValue();
        targetRunValue.setInstrumentParameter(getTargetParameter());
        UnitParameterValueConverter converter = new UnitParameterValueConverter();
        converter.convert(targetRunValue, otherRunValue);
        otherData = targetRunValue.getData();
      } else {
        otherData = otherRunValue.getData();
        log.debug("Value is : {}", otherRunValue.getData());
      }
    } else {
      log.debug("Value is : null");
    }

    // Update the equalsValueCheck accordingly.
    equalsValueCheck.setTargetParameter(getTargetParameter());
    equalsValueCheck.setData(otherData);
    equalsValueCheck.setOperator(operator);

    return equalsValueCheck.checkParameterValue(paramData, null, null);
  }

  protected Object[] getDescriptionArgs(ActiveInstrumentRunService activeRunService) {
    return new Object[] { getTargetParameter().getLabel(), parameter.getLabel() };
  }

  public ComparisonOperator getOperator() {
    return operator;
  }

  public void setOperator(ComparisonOperator operator) {
    this.operator = operator;
  }
}
