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
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataType;

/**
 * given an algorithm (boolean, arithmetic, conditionnal etc. expressions) and operands provided by a list of
 * IDataSource, the equation is evaluated. Suggestion is to hold off this feature for another round of implementation.
 * It requires that we look into third-party libraries and do some prototyping/testing. Doubt we have time for the 1.1
 * release
 */
public class ComputingDataSource extends AbstractMultipleDataSource {

  private static final long serialVersionUID = 1L;

  // TODO maybe it would be a good idea to create an enum class for algorithm
  private String algorithm;

  private DataType type;

  public Data getData(Participant participant) {
    // TODO Auto-generated method stub
    return null;
  }

  public String getUnit() {
    // TODO Auto-generated method stub
    return null;
  }

  public ComputingDataSource(String algorithm, DataType type) {
    super();
    this.algorithm = algorithm;
    this.type = type;
  }

}
