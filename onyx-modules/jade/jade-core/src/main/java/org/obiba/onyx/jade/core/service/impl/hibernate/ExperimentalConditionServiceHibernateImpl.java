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
import org.obiba.onyx.jade.core.service.ExperimentalConditionService;
import org.obiba.onyx.jade.core.service.impl.DefaultExperimentalConditionServiceImpl;
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

  @SuppressWarnings("unchecked")
  public List<ExperimentalCondition> getExperimentalConditionsRecordedAfter(String workstationId, Date date) {
    Criteria criteria = getSession().createCriteria(ExperimentalCondition.class).add(Restrictions.eq("workstation", workstationId)).add(Restrictions.gt("time", date)).addOrder(Order.asc("time"));
    List<ExperimentalCondition> results = criteria.list();

    List<ExperimentalConditionLog> experimentalConditionLogs = this.getExperimentalConditionLog();
    Set<String> logNames = new HashSet<String>();
    for(ExperimentalConditionLog log : experimentalConditionLogs) {
      logNames.add(log.getName());
    }

    List<ExperimentalCondition> filteredResults = new ArrayList<ExperimentalCondition>();
    for(ExperimentalCondition ec : results) {
      if(logNames.contains(ec.getName())) {
        filteredResults.add(ec);
      }
    }

    return filteredResults;
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

}
