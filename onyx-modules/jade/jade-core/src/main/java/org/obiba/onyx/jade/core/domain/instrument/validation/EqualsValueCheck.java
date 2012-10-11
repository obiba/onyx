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
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;
import org.obiba.onyx.jade.core.service.InstrumentRunService;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Integrity check to verify that an instrument run value is equal to a given (fixed) value.
 * 
 * The check fails (returns <code>false</code>) if the values are <i>not</i> equal.
 */
public class EqualsValueCheck extends AbstractIntegrityCheck {

  private static final Logger log = LoggerFactory.getLogger(EqualsValueCheck.class);

  private static final long serialVersionUID = 1L;

  private Data value;

  private ComparisonOperator operator;

  public EqualsValueCheck() {
    super();
  }

  public ComparisonOperator getOperator() {
    return operator;
  }

  public void setOperator(ComparisonOperator operator) {
    this.operator = operator;
  }

  public void setData(Data data) {
    this.value = data;
  }

  public Data getData() {
    return value;
  }

  //
  // IntegrityCheck Methods
  //

  /**
   * Returns <code>true</code> if the specified instrument run value is equal to the configured value.
   * 
   * @param runValue instrument run value
   * @param runService instrument run service (not used by this check)
   * @return <code>true</code> if instrument run value equals configured value
   */
  public boolean checkParameterValue(InstrumentParameter checkedParameter, Data paramData, InstrumentRunService runService, ActiveInstrumentRunService activeRunService) {

    int compareResult = paramData.compareTo(getData());

    // Set default comparison operator to equals.
    if(operator == null) {
      operator = ComparisonOperator.EQUALS;
    }

    log.debug("Compare result = {}", compareResult);
    log.debug("Value being checked = {} ", paramData);
    log.debug("Check = {}, Operator = {}", getData(), getOperator());

    boolean result = false;
    if(compareResult == 0) {
      if(operator == ComparisonOperator.LESSER_EQUALS || operator == ComparisonOperator.EQUALS || operator == ComparisonOperator.GREATER_EQUALS || getData().getType() == DataType.BOOLEAN) {
        result = true;
      }
    } else if(compareResult < 0) {
      if(operator == ComparisonOperator.LESSER || operator == ComparisonOperator.LESSER_EQUALS) {
        result = true;
      }
    } else {
      if(operator == ComparisonOperator.GREATER || operator == ComparisonOperator.GREATER_EQUALS) {
        result = true;
      }
    }

    log.debug("Result is {}", result);
    return result;

  }

  protected Object[] getDescriptionArgs(InstrumentParameter checkedParameter, ActiveInstrumentRunService activeRunService) {
    return new Object[] { checkedParameter.getLabel(), getData().getValue() };
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

    return super.toString() + "[x " + op + " " + getData() + "]";
  }
}
