/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.data;

import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.util.data.ComparisonOperator;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;

/**
 * Compares data from two datasources.
 */
public class ComparingDataSource implements IDataSource, Cloneable {

  private static final long serialVersionUID = 1L;

  private IDataSource dataSourceLeft;

  private ComparisonOperator comparisonOperator;

  private IDataSource dataSourceRight;

  public ComparingDataSource(IDataSource left, ComparisonOperator comparisonOperator, IDataSource right) {
    super();
    this.dataSourceLeft = left;
    this.dataSourceRight = right;
    this.comparisonOperator = comparisonOperator;
  }

  public void setComparisonOperator(ComparisonOperator comparisonOperator) {
    this.comparisonOperator = comparisonOperator;
  }

  public Data getData(Participant participant) {
    Data dataLeft = dataSourceLeft.getData(participant);
    Data dataRight = dataSourceRight.getData(participant);

    if(dataLeft == null && dataRight == null) {
      return DataBuilder.buildBoolean(isComparisonValid(0));
    } else if(dataLeft != null && comparisonOperator.equals(ComparisonOperator.in)) {
      return DataBuilder.buildBoolean(isComparisonValid(dataRight.getValueAsString().indexOf("\"" + dataLeft.getValueAsString() + "\"")));
    } else if(dataLeft != null) {
      return DataBuilder.buildBoolean(isComparisonValid(dataLeft.compareTo(dataRight)));
    } else {
      return DataBuilder.buildBoolean(false);
    }
  }

  public IDataSource getDataSourceLeft() {
    return dataSourceLeft;
  }

  public void setDataSourceLeft(IDataSource dataSourceLeft) {
    this.dataSourceLeft = dataSourceLeft;
  }

  public IDataSource getDataSourceRight() {
    return dataSourceRight;
  }

  public void setDataSourceRight(IDataSource dataSourceRight) {
    this.dataSourceRight = dataSourceRight;
  }

  public ComparisonOperator getComparisonOperator() {
    return comparisonOperator;
  }

  public String getUnit() {
    return null;
  }

  private boolean isComparisonValid(int result) {

    switch(comparisonOperator) {
    case eq:
      return result == 0;
    case ne:
      return result != 0;
    case lt:
      return result < 0;
    case le:
      return result <= 0;
    case gt:
      return result > 0;
    case ge:
      return result >= 0;
    case in:
      return result >= 0;
    default:
      return false;
    }
  }

  @Override
  public ComparingDataSource clone() {
    return new ComparingDataSource(dataSourceLeft, comparisonOperator, dataSourceRight);
  }

  @Override
  public String toString() {
    String op;
    switch(comparisonOperator) {
    case eq:
      op = "==";
      break;
    case ne:
      op = "!=";
      break;
    case lt:
      op = "<";
      break;
    case le:
      op = "<=";
      break;
    case gt:
      op = ">";
      break;
    case ge:
      op = ">=";
      break;
    case in:
      op = "in";
      break;
    default:
      op = "";
    }
    String rval = "Comparing[";
    if(dataSourceLeft != null) {
      rval += dataSourceLeft;
    } else {
      rval += "x";
    }

    rval += " " + op + " ";
    if(dataSourceRight != null) {
      rval += dataSourceRight;
    } else {
      rval += "y";
    }
    return rval + "]";
  }
}
