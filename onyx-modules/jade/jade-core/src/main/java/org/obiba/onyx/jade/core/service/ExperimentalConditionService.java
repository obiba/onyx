/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.core.service;

import java.util.List;

import org.obiba.core.service.PagingClause;
import org.obiba.core.service.SortingClause;
import org.obiba.onyx.core.domain.Attribute;
import org.obiba.onyx.jade.core.domain.workstation.ExperimentalCondition;
import org.obiba.onyx.jade.core.domain.workstation.ExperimentalConditionLog;
import org.obiba.onyx.jade.core.domain.workstation.ExperimentalConditionValue;
import org.obiba.onyx.jade.core.domain.workstation.InstrumentCalibration;

public interface ExperimentalConditionService {

  public void save(ExperimentalCondition experimentalCondition);

  public List<ExperimentalConditionLog> getExperimentalConditionLog();

  public void register(ExperimentalConditionLog log);

  /**
   * Returns a list {@link ExperimentalConditions}. If all parameters are null, then all the ExperimentalConditions will
   * be returned.
   * @param template Supply a template with a "name" and "workstation" to match on those values.
   * @param paging
   * @param clauses
   * @return
   */
  public List<ExperimentalCondition> getExperimentalConditions(ExperimentalCondition template, PagingClause paging, SortingClause... clauses);

  public ExperimentalConditionLog getExperimentalConditionLogByName(String name);

  public boolean instrumentCalibrationExists(String instrumentType);

  public InstrumentCalibration getInstrumentCalibrationByType(String instrumentType);

  public Attribute getAttribute(ExperimentalConditionValue experimentalConditionValue);

}
