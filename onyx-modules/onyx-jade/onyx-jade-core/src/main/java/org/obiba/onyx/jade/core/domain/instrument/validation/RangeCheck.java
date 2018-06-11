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

import org.obiba.onyx.core.data.IDataSource;
import org.obiba.onyx.core.domain.participant.Gender;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameter;
import org.obiba.onyx.jade.core.service.ActiveInstrumentRunService;
import org.obiba.onyx.jade.core.service.InstrumentRunService;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RangeCheck extends AbstractIntegrityCheck {

  private static final long serialVersionUID = 1L;

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(RangeCheck.class);

  private IDataSource genderSource;

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

  public void setGenderSource(IDataSource genderSource) {
    this.genderSource = genderSource;
  }

  private Gender getGender(ActiveInstrumentRunService activeRunService) {
    // Get the participant's gender (range is gender-dependent).
    Gender gender;
    if(genderSource != null) {
      gender = Gender.valueOf(genderSource.getData(activeRunService.getParticipant()).getValueAsString());
    } else {
      gender = activeRunService.getParticipant().getGender();
    }
    return gender;
  }

  //
  // IntegrityCheck Methods
  //

  @Override
  public boolean checkParameterValue(InstrumentParameter checkedParameter, Data paramData, InstrumentRunService runService, ActiveInstrumentRunService activeRunService) {

    Gender gender = getGender(activeRunService);
    if(checkedParameter.getDataType() == DataType.INTEGER) {
      return checkIntegerParameterValue(paramData, gender);
    } else if(checkedParameter.getDataType() == DataType.DECIMAL) {
      return checkDecimalParameterValue(paramData, gender);
    }

    return false;
  }

  protected Object[] getDescriptionArgs(InstrumentParameter checkedParameter, ActiveInstrumentRunService activeRunService) {
    Object[] args = null;

    // For male participants, return the min/max values for males; for female participants,
    // return the min/max values for females.
    Gender gender = getGender(activeRunService);

    if(checkedParameter.getDataType() == DataType.INTEGER) {
      if(gender.equals(Gender.MALE)) {
        args = new Object[] { checkedParameter.getLabel(), integerMinValueMale, integerMaxValueMale };
      } else if(gender.equals(Gender.FEMALE)) {
        args = new Object[] { checkedParameter.getLabel(), integerMinValueFemale, integerMaxValueFemale };
      }
    } else if(checkedParameter.getDataType() == DataType.DECIMAL) {
      if(gender.equals(Gender.MALE)) {
        args = new Object[] { checkedParameter.getLabel(), decimalMinValueMale, decimalMaxValueMale };
      } else if(gender.equals(Gender.FEMALE)) {
        args = new Object[] { checkedParameter.getLabel(), decimalMinValueFemale, decimalMaxValueFemale };
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

  @Override
  public String toString() {
    StringBuffer rval = new StringBuffer(super.toString()).append("[");

    boolean male = false;
    if(integerMinValueMale != null || integerMaxValueMale != null || decimalMinValueMale != null || decimalMaxValueMale != null) {
      rval.append("MALE[");
      if(integerMinValueMale != null && integerMaxValueMale != null) {
        rval.append(integerMinValueMale).append(" < x < ").append(integerMaxValueMale);
      } else if(integerMinValueMale != null) {
        rval.append("x > ").append(integerMinValueMale);
      } else if(integerMaxValueMale != null) {
        rval.append("x < ").append(integerMaxValueMale);
      }
      if(integerMinValueMale != null || integerMaxValueMale != null) {
        rval.append(" || ");
      }
      if(decimalMinValueMale != null && decimalMaxValueMale != null) {
        rval.append(decimalMinValueMale).append(" < x < ").append(decimalMaxValueMale);
      } else if(decimalMinValueMale != null) {
        rval.append("x > ").append(decimalMinValueMale);
      } else if(decimalMaxValueMale != null) {
        rval.append("x < ").append(decimalMaxValueMale);
      }
      rval.append("]");
      male = true;
    }

    if(integerMinValueFemale != null || integerMaxValueFemale != null || decimalMinValueFemale != null || decimalMaxValueFemale != null) {
      if(male) {
        rval.append(", ");
      }
      rval.append("FEMALE[");
      if(integerMinValueFemale != null && integerMaxValueFemale != null) {
        rval.append(integerMinValueFemale).append(" < x < ").append(integerMaxValueFemale);
      } else if(integerMinValueFemale != null) {
        rval.append("x > ").append(integerMinValueFemale);
      } else if(integerMaxValueFemale != null) {
        rval.append("x < ").append(integerMaxValueFemale);
      }
      if(integerMinValueFemale != null || integerMaxValueFemale != null) {
        rval.append(" || ");
      }
      if(decimalMinValueFemale != null && decimalMaxValueFemale != null) {
        rval.append(decimalMinValueFemale).append(" < x < ").append(decimalMaxValueFemale);
      } else if(decimalMinValueFemale != null) {
        rval.append("x > ").append(decimalMinValueFemale);
      } else if(decimalMaxValueFemale != null) {
        rval.append("x < ").append(decimalMaxValueFemale);
      }
      rval.append("]");
    }
    rval.append("]");
    return rval.toString();
  }

}
