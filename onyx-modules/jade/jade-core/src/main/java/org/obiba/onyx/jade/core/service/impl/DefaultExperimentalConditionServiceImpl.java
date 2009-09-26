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

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.obiba.core.service.PagingClause;
import org.obiba.core.service.SortingClause;
import org.obiba.core.service.impl.PersistenceManagerAwareService;
import org.obiba.core.service.impl.hibernate.AssociationCriteria;
import org.obiba.core.service.impl.hibernate.AssociationCriteria.Operation;
import org.obiba.onyx.core.domain.Attribute;
import org.obiba.onyx.jade.core.domain.workstation.ExperimentalCondition;
import org.obiba.onyx.jade.core.domain.workstation.ExperimentalConditionLog;
import org.obiba.onyx.jade.core.domain.workstation.ExperimentalConditionValue;
import org.obiba.onyx.jade.core.domain.workstation.InstrumentCalibration;
import org.obiba.onyx.jade.core.service.ExperimentalConditionService;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class DefaultExperimentalConditionServiceImpl extends PersistenceManagerAwareService implements ExperimentalConditionService {

  private List<ExperimentalConditionLog> experimentalConditionLogs = new ArrayList<ExperimentalConditionLog>();

  private SessionFactory factory;

  public void setSessionFactory(SessionFactory factory) {
    this.factory = factory;
  }

  private Session getSession() {
    return factory.getCurrentSession();
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

  private AssociationCriteria getCriteria(ExperimentalCondition template, PagingClause paging, SortingClause... clauses) {
    AssociationCriteria criteria = AssociationCriteria.create(ExperimentalCondition.class, getSession());

    if(template != null) {
      if(template.getId() != null) criteria.add("id", Operation.eq, template.getId());
      if(template.getName() != null) criteria.add("name", Operation.like, template.getName());
      if(template.getWorkstation() != null) criteria.add("workstation", Operation.like, template.getWorkstation());
    }
    if(paging != null) criteria.addPagingClause(paging);
    if(clauses != null) criteria.addSortingClauses(clauses);

    return criteria;
  }

  public List<ExperimentalCondition> getExperimentalConditions(ExperimentalCondition template, PagingClause paging, SortingClause... clauses) {
    return getCriteria(template, paging, clauses).list();
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

  public boolean experimentalConditionLogExists(String name) {
    for(ExperimentalConditionLog log : experimentalConditionLogs) {
      if(log.getName().equals(name)) return true;
    }
    return false;
  }

  public InstrumentCalibration getInstrumentCalibrationByType(String instrumentType) {
    for(InstrumentCalibration calibration : getInstrumentCalibrations()) {
      if(calibration.getInstrumentType().equalsIgnoreCase(instrumentType)) return calibration;
    }
    throw new IllegalStateException("The InstrumentCalibration for the instrument type [" + instrumentType + "] could not be found.");
  }

  public boolean instrumentCalibrationExists(String instrumentType) {
    for(InstrumentCalibration calibration : getInstrumentCalibrations()) {
      if(calibration.getInstrumentType().equalsIgnoreCase(instrumentType)) return true;
    }
    return false;
  }

  private List<InstrumentCalibration> getInstrumentCalibrations() {
    List<InstrumentCalibration> instrumentCalibrations = new ArrayList<InstrumentCalibration>();
    for(ExperimentalConditionLog log : experimentalConditionLogs) {
      if(log instanceof InstrumentCalibration) {
        instrumentCalibrations.add((InstrumentCalibration) log);
      }
    }
    return instrumentCalibrations;
  }

}
