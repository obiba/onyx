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

import org.obiba.onyx.core.domain.participant.Gender;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;
import org.obiba.onyx.jade.core.service.InstrumentRunService;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
@DiscriminatorValue("RangeCheck")
public class RangeCheck extends AbstractIntegrityCheck implements IntegrityCheck {

  private static final long serialVersionUID = 1L;

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(RangeCheck.class);

  private Long integerMinValueMale;

  private Long integerMaxValueMale;

  private Long integerMinValueFemale;

  private Long integerMaxValueFemale;

  private Double decimalMinValueMale;

  private Double decimalMaxValueMale;

  private Double decimalMinValueFemale;

  private Double decimalMaxValueFemale;

  public RangeCheck() {
    super();
  }

  public DataType getValueType() {
    return getTargetParameter().getDataType();
  }

  public void setIntegerMinValueMale(Long value) {
    integerMinValueMale = value;
  }

  public void setIntegerMaxValueMale(Long value) {
    integerMaxValueMale = value;
  }

  public void setIntegerMinValueFemale(Long value) {
    integerMinValueFemale = value;
  }

  public void setIntegerMaxValueFemale(Long value) {
    integerMaxValueFemale = value;
  }

  public void setDecimalMinValueMale(Double value) {
    decimalMinValueMale = value;
  }

  public void setDecimalMaxValueMale(Double value) {
    decimalMaxValueMale = value;
  }

  public void setDecimalMinValueFemale(Double value) {
    decimalMinValueFemale = value;
  }

  public void setDecimalMaxValueFemale(Double value) {
    decimalMaxValueFemale = value;
  }

  //
  // IntegrityCheck Methods
  //

  @Override
  public boolean checkParameterValue(Data paramData, InstrumentRunService runService, ActiveInstrumentRunService activeRunService) {
    // Get the participant's gender (range is gender-dependent).
    Gender gender = activeRunService.getParticipant().getGender();

    if(getValueType().equals(DataType.INTEGER)) {
      return checkIntegerParameterValue(paramData, gender);
    } else if(getValueType().equals(DataType.DECIMAL)) {
      return checkDecimalParameterValue(paramData, gender);
    }

    return false;
  }

  protected Object[] getDescriptionArgs(ActiveInstrumentRunService activeRunService) {
    Object[] args = null;

    // For male participants, return the min/max values for males; for female participants,
    // return the min/max values for females.
    Participant participant = activeRunService.getParticipant();

    if(getValueType().equals(DataType.INTEGER)) {
      if(participant.getGender().equals(Gender.MALE)) {
        args = new Object[] { getTargetParameter().getDescription(), integerMinValueMale, integerMaxValueMale };
      } else if(participant.getGender().equals(Gender.FEMALE)) {
        args = new Object[] { getTargetParameter().getDescription(), integerMinValueFemale, integerMaxValueFemale };
      }
    } else if(getValueType().equals(DataType.DECIMAL)) {
      if(participant.getGender().equals(Gender.MALE)) {
        args = new Object[] { getTargetParameter().getDescription(), decimalMinValueMale, decimalMaxValueMale };
      } else if(participant.getGender().equals(Gender.FEMALE)) {
        args = new Object[] { getTargetParameter().getDescription(), decimalMinValueFemale, decimalMaxValueFemale };
      }
    }

    return args;
  }

  private boolean checkIntegerParameterValue(Data paramData, Gender gender) {
    boolean withinRange = true;

    Long minValue = gender.equals(Gender.MALE) ? integerMinValueMale : integerMinValueFemale;
    Long maxValue = gender.equals(Gender.MALE) ? integerMaxValueMale : integerMaxValueFemale;

    if(minValue != null) {
      if(minValue.compareTo((Long) paramData.getValue()) > 0) {
        withinRange = false;
      }
    }

    if(withinRange) {
      if(maxValue != null) {
        if(maxValue.compareTo((Long) paramData.getValue()) < 0) {
          withinRange = false;
        }
      }
    }

    return withinRange;
  }

  private boolean checkDecimalParameterValue(Data paramData, Gender gender) {
    boolean withinRange = true;

    Double minValue = gender.equals(Gender.MALE) ? decimalMinValueMale : decimalMinValueFemale;
    Double maxValue = gender.equals(Gender.MALE) ? decimalMaxValueMale : decimalMaxValueFemale;

    if(minValue != null) {
      if(minValue.compareTo((Double) paramData.getValue()) > 0) {
        withinRange = false;
      }
    }

    if(withinRange) {
      if(maxValue != null) {
        if(maxValue.compareTo((Double) paramData.getValue()) < 0) {
          withinRange = false;
        }
      }
    }

    return withinRange;
  }
}
