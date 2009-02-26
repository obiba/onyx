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
public class ComparingDataSource extends AbstractMultipleDataSource {

  private static final long serialVersionUID = 1L;

  private ComparisonOperator comparisonOperator;

  public ComparingDataSource(IDataSource dataSource1, ComparisonOperator comparisonOperator, IDataSource dataSource2) {
    super();
    addDataSource(dataSource1);
    addDataSource(dataSource2);
    this.comparisonOperator = comparisonOperator;
  }

  public void setComparisonOperator(ComparisonOperator comparisonOperator) {
    this.comparisonOperator = comparisonOperator;
  }

  public Data getData(Participant participant) {
    if(getDataSources().size() != 2) {
      throw new IllegalArgumentException("Comparing requires two datasources.");
    }
    Data data1 = getDataSources().get(0).getData(participant);
    Data data2 = getDataSources().get(1).getData(participant);

    if(data1 == null && data2 == null) {
      return DataBuilder.buildBoolean(isComparisonValid(0));
    } else if(data1 != null) {
      return DataBuilder.buildBoolean(isComparisonValid(data1.compareTo(data2)));
    } else {
      return DataBuilder.buildBoolean(false);
    }
  }

  @Override
  public AbstractMultipleDataSource addDataSource(IDataSource dataSource) {
    if(getDataSources().size() == 2) {
      throw new IllegalArgumentException("Comparing requires two datasources.");
    }
    return super.addDataSource(dataSource);
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
    default:
      return false;
    }
  }

}
