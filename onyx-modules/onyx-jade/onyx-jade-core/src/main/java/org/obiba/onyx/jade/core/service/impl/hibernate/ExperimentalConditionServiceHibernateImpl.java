/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.core.service.impl.hibernate;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.obiba.onyx.jade.core.domain.workstation.ExperimentalCondition;
import org.obiba.onyx.jade.core.domain.workstation.ExperimentalConditionLog;
import org.obiba.onyx.jade.core.domain.workstation.ExperimentalConditionValue;
import org.obiba.onyx.jade.core.domain.workstation.InstrumentCalibration;
import org.obiba.onyx.jade.core.service.ExperimentalConditionService;
import org.obiba.onyx.jade.core.service.impl.DefaultExperimentalConditionServiceImpl;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class ExperimentalConditionServiceHibernateImpl extends DefaultExperimentalConditionServiceImpl {
  //
  // Instance Variables
  //

  private SessionFactory factory;

  //
  // DefaultExperimentalConditionServiceImpl Methods
  //

  @SuppressWarnings("unchecked")
  public List<ExperimentalCondition> getExperimentalConditions(ExperimentalCondition template) {
    return getCriteria(template).addOrder(Order.asc("time")).list();
  }

  public List<ExperimentalCondition> getNonInstrumentRelatedConditions(String workstationId) {
    ExperimentalCondition template = new ExperimentalCondition();
    template.setWorkstation(workstationId);
    List<ExperimentalCondition> results = getExperimentalConditions(template);

    return extractNonInstrumentRelatedConditions(results);
  }

  @SuppressWarnings("unchecked")
  public List<ExperimentalCondition> getNonInstrumentRelatedConditionsRecordedAfter(String workstationId, Date date) {
    Criteria criteria = getSession().createCriteria(ExperimentalCondition.class).add(Restrictions.eq("workstation", workstationId)).add(Restrictions.gt("time", date)).addOrder(Order.asc("time"));
    List<ExperimentalCondition> results = criteria.list();

    return extractNonInstrumentRelatedConditions(results);
  }

  public List<ExperimentalCondition> getInstrumentCalibrations(String instrumentBarcode) {
    ExperimentalCondition template = new ExperimentalCondition();
    ExperimentalConditionValue experimentalConditionValue = new ExperimentalConditionValue();
    experimentalConditionValue.setAttributeName(ExperimentalConditionService.INSTRUMENT_BARCODE);
    experimentalConditionValue.setAttributeType(DataType.TEXT);
    experimentalConditionValue.setData(DataBuilder.buildText(instrumentBarcode));
    template.addExperimentalConditionValue(experimentalConditionValue);
    List<ExperimentalCondition> results = getExperimentalConditions(template);

    return extractInstrumentCalibrations(results);
  }

  @SuppressWarnings("unchecked")
  public List<ExperimentalCondition> getInstrumentCalibrationsRecordedAfter(String instrumentBarcode, Date date) {
    Criteria criteria = getSession().createCriteria(ExperimentalCondition.class).add(Restrictions.gt("time", date)).addOrder(Order.asc("time")).createCriteria("experimentalConditionValues").add(Restrictions.eq("attributeName", ExperimentalConditionService.INSTRUMENT_BARCODE)).add(Restrictions.eq("textValue", instrumentBarcode));
    List<ExperimentalCondition> results = criteria.list();

    return extractInstrumentCalibrations(results);
  }

  //
  // Methods
  //

  public void setSessionFactory(SessionFactory factory) {
    this.factory = factory;
  }

  private Session getSession() {
    return factory.getCurrentSession();
  }

  private Criteria getCriteria(ExperimentalCondition template) {
    Criteria criteria = getSession().createCriteria(ExperimentalCondition.class);

    if(template != null) {
      if(template.getId() != null) criteria.add(Restrictions.eq("id", template.getId()));
      if(template.getName() != null) criteria.add(Restrictions.like("name", template.getName()));
      if(template.getWorkstation() != null) criteria.add(Restrictions.like("workstation", template.getWorkstation()));
      if(!template.getExperimentalConditionValues().isEmpty()) {
        for(ExperimentalConditionValue ecv : template.getExperimentalConditionValues()) {
          if(ecv.getAttributeName().equals(ExperimentalConditionService.INSTRUMENT_BARCODE)) {
            criteria.createCriteria("experimentalConditionValues").add(Restrictions.eq("attributeName", ExperimentalConditionService.INSTRUMENT_BARCODE)).add(Restrictions.eq("textValue", ecv.getValue()));
          }
        }
      }
    }
    return criteria;
  }

  private List<ExperimentalCondition> extractNonInstrumentRelatedConditions(List<ExperimentalCondition> experimentalConditions) {
    List<ExperimentalConditionLog> experimentalConditionLogs = this.getExperimentalConditionLog();
    Set<String> logNames = new HashSet<String>();
    for(ExperimentalConditionLog log : experimentalConditionLogs) {
      logNames.add(log.getName());
    }

    List<ExperimentalCondition> nonInstrumentRelatedConditions = new ArrayList<ExperimentalCondition>();
    for(ExperimentalCondition ec : experimentalConditions) {
      if(logNames.contains(ec.getName())) {
        nonInstrumentRelatedConditions.add(ec);
      }
    }

    return nonInstrumentRelatedConditions;
  }

  private List<ExperimentalCondition> extractInstrumentCalibrations(List<ExperimentalCondition> experimentalConditions) {
    List<InstrumentCalibration> instrumentCalibrations = getInstrumentCalibrations();
    Set<String> instrumentCalibrationNames = new HashSet<String>();
    for(InstrumentCalibration instrumentCalibration : instrumentCalibrations) {
      instrumentCalibrationNames.add(instrumentCalibration.getName());
    }

    List<ExperimentalCondition> persistedInstrumentCalibrations = new ArrayList<ExperimentalCondition>();
    for(ExperimentalCondition ec : experimentalConditions) {
      if(instrumentCalibrationNames.contains(ec.getName())) {
        persistedInstrumentCalibrations.add(ec);
      }
    }

    return persistedInstrumentCalibrations;
  }
}
