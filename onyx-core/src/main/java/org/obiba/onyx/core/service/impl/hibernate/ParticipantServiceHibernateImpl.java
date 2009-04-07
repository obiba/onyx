/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.service.impl.hibernate;

import java.util.Date;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.obiba.core.service.PagingClause;
import org.obiba.core.service.SortingClause;
import org.obiba.core.service.impl.hibernate.AssociationCriteria;
import org.obiba.core.service.impl.hibernate.AssociationCriteria.Operation;
import org.obiba.onyx.core.domain.participant.InterviewStatus;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.service.impl.DefaultParticipantServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of hibernate specific methods of Participant Service
 * @see#ParticipantService.
 * @author Yannick Marcon
 * 
 */
@Transactional
public class ParticipantServiceHibernateImpl extends DefaultParticipantServiceImpl {

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(ParticipantServiceHibernateImpl.class);

  private SessionFactory factory;

  public void setSessionFactory(SessionFactory factory) {
    this.factory = factory;
  }

  private Session getSession() {
    return factory.getCurrentSession();
  }

  @Transactional(readOnly = true)
  public List<Participant> getParticipants(InterviewStatus status, PagingClause paging, SortingClause... clauses) {
    AssociationCriteria criteria = getCriteria(paging, clauses);
    if(status != null) criteria.add("interview.status", Operation.eq, status);
    return criteria.list();
  }

  @Transactional(readOnly = true)
  public int countParticipants(InterviewStatus status) {
    AssociationCriteria criteria = getCriteria(null, (SortingClause[]) null);
    if(status != null) criteria.add("interview.status", Operation.eq, status);
    return criteria.count();
  }

  @Transactional(readOnly = true)
  public List<Participant> getParticipants(Date from, Date to, PagingClause paging, SortingClause... clauses) {
    AssociationCriteria criteria = getCriteria(paging, clauses);

    if(from != null) criteria.add("appointment.date", Operation.ge, from);
    if(to != null) criteria.add("appointment.date", Operation.le, to);

    return criteria.list();
  }

  @Transactional(readOnly = true)
  public int countParticipants(Date from, Date to) {
    AssociationCriteria criteria = getCriteria(null, (SortingClause[]) null);

    if(from != null) criteria.add("appointment.date", Operation.ge, from);
    if(to != null) criteria.add("appointment.date", Operation.le, to);

    return criteria.count();
  }

  @Transactional(readOnly = true)
  private AssociationCriteria getCriteria(PagingClause paging, SortingClause... clauses) {
    AssociationCriteria criteria = AssociationCriteria.create(Participant.class, getSession());

    if(paging != null) criteria.addPagingClause(paging);
    if(clauses != null) criteria.addSortingClauses(clauses);

    return criteria;
  }

  public List<Participant> getParticipantsByName(String likeName, PagingClause paging, SortingClause... clauses) {
    return getCriteriaByName(likeName, paging, clauses).list();
  }

  public int countParticipantsByName(String likeName) {
    return getCriteriaByName(likeName, null, (SortingClause[]) null).count();
  }

  private AssociationCriteria getCriteriaByName(String likeName, PagingClause paging, SortingClause... clauses) {
    AssociationCriteria criteria = getCriteria(paging, clauses);

    if(likeName != null) {
      int idx = likeName.indexOf(' ');
      if(idx != -1) {
        String likeFirstName = likeName.substring(0, idx);
        String likeLastName = likeName.substring(idx + 1, likeName.length());
        criteria.add("firstName", Operation.ilike, "%" + likeFirstName + "%").add("lastName", Operation.ilike, "%" + likeLastName + "%");
      } else {
        criteria.getCriteria().add(Restrictions.or(Restrictions.ilike("firstName", "%" + likeName + "%"), Restrictions.ilike("lastName", "%" + likeName + "%")));
      }
    }

    return criteria;
  }

  @SuppressWarnings("unchecked")
  @Transactional(readOnly = true)
  public List<Participant> getParticipantsByCode(String code, PagingClause paging, SortingClause... clauses) {
    Query participantQuery = createParticipantsByCodeQuery(code);

    participantQuery.setMaxResults(paging.getLimit());
    participantQuery.setFirstResult(paging.getOffset());

    return participantQuery.list();
  }

  @SuppressWarnings("unchecked")
  @Transactional(readOnly = true)
  public int countParticipantsByCode(String code) {
    Query participantQuery = createParticipantsByCodeQuery(code);

    List results = participantQuery.list();

    int count = 0;

    if(results != null) {
      count = results.size();
    }

    return count;
  }

  private Query createParticipantsByCodeQuery(String code) {
    Query participantQuery = null;

    if(code != null) {
      participantQuery = getSession().createQuery("from Participant p where p.barcode = :barcode or p.enrollmentId = :appointmentCode");
      participantQuery.setString("barcode", code);
      participantQuery.setString("appointmentCode", code);
    } else {
      participantQuery = getSession().createQuery("from Participant");
    }

    return participantQuery;
  }

  @SuppressWarnings("unchecked")
  protected List<Participant> getNotReceivedParticipants() {
    return getSession().createCriteria(Participant.class).add(Restrictions.isNull("barcode")).list();
  }
}
