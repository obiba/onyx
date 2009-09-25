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
 */
public class EqualsParameterCheck extends AbstractIntegrityCheck implements IntegrityCheck {

  private static final long serialVersionUID = 1L;

  private static final Logger log = LoggerFactory.getLogger(EqualsParameterCheck.class);

  private String parameterCode;

  private ComparisonOperator operator;

  public void setParameterCode(String parameterCode) {
    this.parameterCode = parameterCode;
  }

  public String getParameterCode() {
    return this.parameterCode;
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
  public boolean checkParameterValue(InstrumentParameter checkedParameter, Data paramData, InstrumentRunService runService, ActiveInstrumentRunService activeRunService) {
    // If the other parameter has not been specified, there is nothing to check!
    if(parameterCode == null) {
      return true;
    }

    //
    // Get the other parameter's value.
    //
    log.debug("Retrieving parameter value : {}", parameterCode);

    InstrumentRunValue otherRunValue = activeRunService.getInstrumentRunValue(parameterCode);
    Data otherData = null;

    if(otherRunValue != null) {
      InstrumentParameter otherParam = activeRunService.getInstrumentType().getInstrumentParameter(otherRunValue.getInstrumentParameter());

      if(!otherParam.getDataType().equals(paramData.getType())) {
        InstrumentRunValue targetRunValue = new InstrumentRunValue();
        targetRunValue.setInstrumentParameter(checkedParameter.getCode());
        UnitParameterValueConverter converter = new UnitParameterValueConverter();
        converter.convert(activeRunService, targetRunValue, otherRunValue);
        otherData = targetRunValue.getData(paramData.getType());
      } else {
        InstrumentParameter otherParameter = activeRunService.getInstrumentType().getInstrumentParameter(parameterCode);
        otherData = otherRunValue.getData(otherParameter.getDataType());
      }
    } else {
      log.debug("Value is : null");
    }

    // Lazily instantiate the equalsValueCheck.
    EqualsValueCheck equalsValueCheck = new EqualsValueCheck();

    // Update the equalsValueCheck accordingly.
    equalsValueCheck.setData(otherData);
    equalsValueCheck.setOperator(operator);

    return equalsValueCheck.checkParameterValue(checkedParameter, paramData, runService, activeRunService);
  }

  protected Object[] getDescriptionArgs(InstrumentParameter checkedParameter, ActiveInstrumentRunService activeRunService) {
    InstrumentParameter otherParameter = activeRunService.getInstrumentType().getInstrumentParameter(parameterCode);
    return new Object[] { checkedParameter.getLabel(), otherParameter.getLabel() };
  }

  public ComparisonOperator getOperator() {
    return operator;
  }

  public void setOperator(ComparisonOperator operator) {
    this.operator = operator;
  }

  @Override
  public String toString() {
    String op = "==";
    if(operator != null) {
      switch(operator) {
      case EQUALS:
        op = "==";
        break;
      case GREATER:
        op = ">";
        break;
      case GREATER_EQUALS:
        op = ">=";
        break;
      case LESSER:
        op = "<";
        break;
      case LESSER_EQUALS:
        op = "<=";
        break;
      }
    }

    return super.toString() + "[x " + op + " " + parameterCode + "]";
  }
}
