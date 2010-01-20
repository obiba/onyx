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
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.obiba.core.service.PagingClause;
import org.obiba.core.service.SortingClause;
import org.obiba.core.service.impl.hibernate.AssociationCriteria;
import org.obiba.core.service.impl.hibernate.AssociationCriteria.Operation;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentMeasurementType;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentStatus;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.service.impl.DefaultInstrumentServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class InstrumentServiceHibernateImpl extends DefaultInstrumentServiceImpl {

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(InstrumentServiceHibernateImpl.class);

  private SessionFactory factory;

  public void setSessionFactory(SessionFactory factory) {
    this.factory = factory;
  }

  private Session getSession() {
    return factory.getCurrentSession();
  }

  public List<Instrument> getWorkstationInstruments(String workstation, PagingClause paging, SortingClause... clauses) {
    AssociationCriteria criteria = AssociationCriteria.create(Instrument.class, getSession()).addPagingClause(paging).addSortingClauses(clauses);
    criteria.getCriteria().add(Restrictions.or(Restrictions.eq("workstation", workstation), Restrictions.isNull("workstation")));

    return criteria.list();
  }

  public int countWorkstationInstruments(String workstation) {
    AssociationCriteria criteria = AssociationCriteria.create(Instrument.class, getSession());
    criteria.getCriteria().add(Restrictions.or(Restrictions.eq("workstation", workstation), Restrictions.isNull("workstation")));

    return criteria.count();
  }

  public List<InstrumentMeasurementType> getWorkstationInstrumentMeasurementTypes(String workstation, PagingClause paging, SortingClause... clauses) {
    AssociationCriteria criteria = AssociationCriteria.create(InstrumentMeasurementType.class, getSession()).addPagingClause(paging).addSortingClauses(clauses);
    criteria.add("instrument", Operation.or, Restrictions.eq("workstation", workstation), Restrictions.isNull("workstation"));

    return criteria.list();
  }

  public int countWorkstationInstrumentMeasurementTypes(String workstation) {
    AssociationCriteria criteria = AssociationCriteria.create(InstrumentMeasurementType.class, getSession());
    criteria.add("instrument", Operation.or, Restrictions.eq("workstation", workstation), Restrictions.isNull("workstation"));

    return criteria.count();
  }

  public List<Instrument> getActiveInstruments(InstrumentType instrumentType) {
    AssociationCriteria criteria = AssociationCriteria.create(InstrumentMeasurementType.class, getSession());
    criteria.add("type", Operation.eq, instrumentType.getName());
    criteria.add("instrument.status", Operation.eq, InstrumentStatus.ACTIVE);

    List<Instrument> instruments = new ArrayList<Instrument>();
    List<InstrumentMeasurementType> types = criteria.list();
    for(InstrumentMeasurementType type : types) {
      instruments.add(type.getInstrument());
    }

    return instruments;
  }

}
