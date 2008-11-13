/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.engine.questionnaire.condition;

import java.io.Serializable;

import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.util.data.Data;

public class DataComparator extends AbstractDataComparator implements Serializable {

  private static final long serialVersionUID = 6128481252934955909L;

  private Data data;

  private OpenAnswerDefinition openAnswerDefinition;

  public DataComparator(ComparisionOperator comparisionOperator, Data data, OpenAnswerDefinition openAnswerDefinition) {
    super(comparisionOperator);
    this.data = data;
    this.openAnswerDefinition = openAnswerDefinition;
  }

  public OpenAnswerDefinition getOpenAnswerDefinition() {
    return openAnswerDefinition;
  }

  public void setOpenAnswerDefinition(OpenAnswerDefinition openAnswerDefinition) {
    this.openAnswerDefinition = openAnswerDefinition;
  }

  public Data getData() {
    return data;
  }

  public void setData(Data data) {
    this.data = data;
  }

  public boolean compare(Data dataToCompare) {
    if(dataToCompare == null) {
      return (data == null);
    }

    return isComparisonValid(dataToCompare.compareTo(data));
  }
}
