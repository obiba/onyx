/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.core.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.obiba.core.service.impl.PersistenceManagerAwareService;
import org.obiba.onyx.core.domain.Attribute;
import org.obiba.onyx.jade.core.domain.workstation.ExperimentalCondition;
import org.obiba.onyx.jade.core.domain.workstation.ExperimentalConditionFactory;
import org.obiba.onyx.jade.core.domain.workstation.ExperimentalConditionLog;
import org.obiba.onyx.jade.core.domain.workstation.ExperimentalConditionValue;
import org.obiba.onyx.jade.core.domain.workstation.InstrumentCalibration;
import org.obiba.onyx.jade.core.service.ExperimentalConditionService;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public abstract class DefaultExperimentalConditionServiceImpl extends PersistenceManagerAwareService implements ExperimentalConditionService {

  private ExperimentalConditionFactory experimentalConditionFactory;

  private List<ExperimentalConditionLog> experimentalConditionLogs = new ArrayList<ExperimentalConditionLog>();

  public void init() {
    experimentalConditionFactory.registerExperimentalConditions();
  }

  public void save(ExperimentalCondition experimentalCondition) {
    getPersistenceManager().save(experimentalCondition);
  }

  public void register(ExperimentalConditionLog log) {
    experimentalConditionLogs.add(log);
  }

  public List<ExperimentalConditionLog> getExperimentalConditionLog() {
    return experimentalConditionLogs;
  }

  public ExperimentalConditionLog getExperimentalConditionLogByName(String name) {
    for(ExperimentalConditionLog log : experimentalConditionLogs) {
      if(log.getName().equals(name)) return log;
    }
    throw new IllegalStateException("The ExperimentalConditionLog [" + name + "] could not be found.");
  }

  public Attribute getAttribute(ExperimentalConditionValue experimentalConditionValue) {
    ExperimentalConditionLog experimentalConditionLog = getExperimentalConditionLogByName(experimentalConditionValue.getExperimentalCondition().getName());
    for(Attribute attribute : experimentalConditionLog.getAttributes()) {
      if(attribute.getName().equals(experimentalConditionValue.getAttributeName())) return attribute;
    }
    throw new IllegalStateException("The Attribute [" + experimentalConditionValue.getAttributeName() + "] belonging to the ExperimentalConditionLog [" + experimentalConditionValue.getExperimentalCondition().getName() + "] could not be found.");
  }

  public boolean instrumentCalibrationExists(String instrumentType) {
    for(InstrumentCalibration calibration : getInstrumentCalibrations()) {
      if(calibration.getInstrumentType().equalsIgnoreCase(instrumentType)) return true;
    }
    return false;
  }

  protected List<InstrumentCalibration> getInstrumentCalibrations() {
    List<InstrumentCalibration> instrumentCalibrations = new ArrayList<InstrumentCalibration>();
    for(ExperimentalConditionLog log : experimentalConditionLogs) {
      if(log instanceof InstrumentCalibration) {
        instrumentCalibrations.add((InstrumentCalibration) log);
      }
    }
    return instrumentCalibrations;
  }

  public List<InstrumentCalibration> getInstrumentCalibrationsByType(String instrumentType) {
    List<InstrumentCalibration> instrumentCalibrations = new ArrayList<InstrumentCalibration>();
    for(InstrumentCalibration calibration : getInstrumentCalibrations()) {
      if(calibration.getInstrumentType().equals(instrumentType)) {
        instrumentCalibrations.add(calibration);
      }
    }
    return instrumentCalibrations;
  }

  public void deleteExperimentalCondition(ExperimentalCondition experimentalCondition) {
    if(experimentalCondition == null) throw new IllegalArgumentException("The experimentalCondition must not be null.");
    getPersistenceManager().delete(experimentalCondition);
  }

  public void setExperimentalConditionFactory(ExperimentalConditionFactory experimentalConditionFactory) {
    this.experimentalConditionFactory = experimentalConditionFactory;
  }
}
