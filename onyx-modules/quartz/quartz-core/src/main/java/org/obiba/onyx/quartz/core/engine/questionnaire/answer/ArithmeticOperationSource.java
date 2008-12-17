/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.engine.questionnaire.answer;

import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.util.data.ArithmeticOperator;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Data source represented by an arithmetic operation between two data source values. Automatic value conversion are
 * performed between INTEGER, DECIMAL and TEXT data types, both for operands and resulting value.
 */
public class ArithmeticOperationSource extends DataSource {

  private static final long serialVersionUID = 1L;

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(ArithmeticOperationSource.class);

  private DataSource dataSourceLeft;

  private ArithmeticOperator operator;

  private DataSource dataSourceRight;

  private DataType dataType = null;

  public ArithmeticOperationSource(DataSource dataSourceLeft, ArithmeticOperator operator, DataSource dataSourceRight) {
    super();
    this.dataSourceLeft = dataSourceLeft;
    this.operator = operator;
    this.dataSourceRight = dataSourceRight;
  }

  /**
   * Set the data type of the returned value.
   * @param dataType either INTEGER, DECIMAL or TEXT
   * @return this for chaining
   */
  public ArithmeticOperationSource setDataType(DataType dataType) {
    if(!dataType.equals(DataType.INTEGER) && !dataType.equals(DataType.DECIMAL) && !dataType.equals(DataType.TEXT)) {
      throw new IllegalArgumentException("Wrong DataType for arithmetic operation result: INTEGER, DECIMAL or TEXT expected, " + dataType + " found.");
    }
    this.dataType = dataType;

    return this;
  }

  @Override
  public Data getData(ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService) {
    Data left = dataSourceLeft.getData(activeQuestionnaireAdministrationService);
    Data right = dataSourceRight.getData(activeQuestionnaireAdministrationService);

    Data data = operator.resolve(left, right, dataType);

    log.debug("result={}", data);
    return data;
  }

  public String getUnit() {
    // TODO Auto-generated method stub
    return null;
  }

}
