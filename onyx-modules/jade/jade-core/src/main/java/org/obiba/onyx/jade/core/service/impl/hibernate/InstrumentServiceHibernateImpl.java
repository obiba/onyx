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

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.obiba.core.service.PagingClause;
import org.obiba.core.service.SortingClause;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
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
    Query query = createQuery(workstation, clauses);

    query.setMaxResults(paging.getLimit());
    query.setFirstResult(paging.getOffset());

    return query.list();
  }

  public int countWorkstationInstruments(String workstation) {
    Query query = createQuery(workstation);

    List results = query.list();

    int count = 0;

    if(results != null) {
      count = results.size();
    }

    return count;
  }

  private Query createQuery(String workstation, SortingClause... clauses) {
    String queryString = "from Instrument i";

    if(workstation != null) {
      queryString += " where i.workstation = '" + workstation + "' or i.workstation is null";
    }

    if(clauses != null) {
      int i = 0;
      for(SortingClause clause : clauses) {
        queryString += (i == 0) ? " order by " : ", ";
        queryString += clause.getField() + ((clause.isAscending()) ? " asc" : " desc");
        i++;
      }
    }

    return getSession().createQuery(queryString);
  }
}
