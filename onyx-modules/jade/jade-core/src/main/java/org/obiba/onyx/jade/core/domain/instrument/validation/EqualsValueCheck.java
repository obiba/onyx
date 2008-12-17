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

import java.util.Date;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;
import org.obiba.onyx.jade.core.service.InstrumentRunService;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Integrity check to verify that an instrument run value is equal to a given (fixed) value.
 * 
 * The check fails (returns <code>false</code>) if the values are <i>not</i> equal.
 * 
 * @author cag-dspathis
 * 
 */
@Entity
@DiscriminatorValue("EqualsValueCheck")
public class EqualsValueCheck extends AbstractIntegrityCheck implements IntegrityCheck {

  private static final Logger log = LoggerFactory.getLogger(EqualsValueCheck.class);

  private static final long serialVersionUID = 1L;

  private Boolean booleanValue;

  private Long integerValue;

  private Double decimalValue;

  private String textValue;

  @Enumerated(EnumType.STRING)
  private ComparisonOperator operator = ComparisonOperator.EQUALS;

  @Temporal(TemporalType.TIMESTAMP)
  private Date dateValue;

  public EqualsValueCheck() {
    super();
  }

  public DataType getValueType() {
    return getTargetParameter().getDataType();
  }

  public void setBooleanValue(boolean value) {
    booleanValue = Boolean.valueOf(value);
  }

  public void setIntegerValue(long value) {
    integerValue = Long.valueOf(value);
  }

  public void setDecimalValue(double value) {
    decimalValue = Double.valueOf(value);
  }

  public void setTextValue(String value) {
    textValue = value;
  }

  public void setDateValue(Date value) {
    dateValue = value;
  }

  public ComparisonOperator getOperator() {
    return operator;
  }

  public void setOperator(ComparisonOperator operator) {
    this.operator = operator;
  }

  public void setData(Data data) {
    if(data != null) {
      if(data.getType().equals(getValueType())) {

        switch(getValueType()) {
        case BOOLEAN:
          booleanValue = data.getValue();
          break;

        case INTEGER:
          integerValue = data.getValue();
          break;

        case DECIMAL:
          decimalValue = data.getValue();
          break;

        case TEXT:
          textValue = data.getValue();
          break;

        case DATE:
          dateValue = data.getValue();
          break;
        }
      } else {
        throw new IllegalArgumentException("DataType " + getValueType() + " expected, " + data.getType() + " received.");
      }
    }
  }

  public Data getData() {
    Data data = null;

    switch(getValueType()) {
    case BOOLEAN:
      data = DataBuilder.buildBoolean(booleanValue);
      break;

    case INTEGER:
      data = DataBuilder.buildInteger(integerValue);
      break;

    case DECIMAL:
      data = DataBuilder.buildDecimal(decimalValue);
      break;

    case TEXT:
      data = DataBuilder.buildText(textValue);
      break;

    case DATE:
      data = DataBuilder.buildDate(dateValue);
    }

    return data;
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
  public boolean checkParameterValue(Data paramData, InstrumentRunService runService, ActiveInstrumentRunService activeRunService) {

    int compareResult = paramData.compareTo(getData());

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

  protected Object[] getDescriptionArgs(ActiveInstrumentRunService activeRunService) {
    return new Object[] { getTargetParameter().getDescription(), getData().getValue() };
  }

}
