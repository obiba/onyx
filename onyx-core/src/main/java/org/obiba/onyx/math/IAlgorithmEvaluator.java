/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.math;

import java.util.List;

import org.obiba.onyx.core.data.IDataSource;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.util.data.Data;

/**
 * Mathematical expression evaluator interface, where operands can be expressed as {@link IDataSource} or {@link Data}.
 */
public interface IAlgorithmEvaluator {

  /**
   * Evaluate an arithmetic expression.
   * @param algorithm
   * @param participant
   * @param operands
   * @return
   */
  public double evaluateDouble(String algorithm, Participant participant, List<IDataSource> operands);

  /**
   * Evaluate a boolean expression.
   * @param algorithm
   * @param participant
   * @param operands
   * @return
   */
  public boolean evaluateBoolean(String algorithm, Participant participant, List<IDataSource> operands);

  /**
   * Evaluate an arithmetic expression.
   * @param algorithm
   * @param operands
   * @return
   */
  public double evaluateDouble(String algorithm, List<Data> operands);

  /**
   * Evaluate a boolean expression.
   * @param algorithm
   * @param operands
   * @return
   */
  public boolean evaluateBoolean(String algorithm, List<Data> operands);

}
