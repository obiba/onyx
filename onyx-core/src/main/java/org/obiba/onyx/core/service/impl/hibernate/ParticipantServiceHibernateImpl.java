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
import org.obiba.onyx.core.exception.NonUniqueParticipantException;
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

  public List<Participant> getParticipants(InterviewStatus status, PagingClause paging, SortingClause... clauses) {
    AssociationCriteria criteria = getCriteria(paging, clauses);
    if(status != null) criteria.add("interview.status", Operation.eq, status);
    return criteria.list();
  }

  public int countParticipants(InterviewStatus status) {
    AssociationCriteria criteria = getCriteria(null, (SortingClause[]) null);
    if(status != null) criteria.add("interview.status", Operation.eq, status);
    return criteria.count();
  }

  public List<Participant> getParticipants(Date from, Date to, PagingClause paging, SortingClause... clauses) {
    AssociationCriteria criteria = getCriteria(paging, clauses);

    if(from != null) criteria.add("appointment.date", Operation.ge, from);
    if(to != null) criteria.add("appointment.date", Operation.le, to);

    return criteria.list();
  }

  public int countParticipants(Date from, Date to) {
    AssociationCriteria criteria = getCriteria(null, (SortingClause[]) null);

    if(from != null) criteria.add("appointment.date", Operation.ge, from);
    if(to != null) criteria.add("appointment.date", Operation.le, to);

    return criteria.count();
  }

  private AssociationCriteria getCriteria(PagingClause paging, SortingClause... clauses) {
    AssociationCriteria criteria = AssociationCriteria.create(Participant.class, getSession());

    if(paging != null) criteria.addPagingClause(paging);
    if(clauses != null) criteria.addSortingClauses(clauses);

    return criteria;
  }

  @SuppressWarnings("unchecked")
  public List<Participant> getParticipantsByInputField(String inputField, PagingClause paging, SortingClause... clauses) {
    Query participantQuery = createParticipantsByInputFieldQuery(inputField, clauses);

    participantQuery.setMaxResults(paging.getLimit());
    participantQuery.setFirstResult(paging.getOffset());

    return participantQuery.list();
  }

  @SuppressWarnings("unchecked")
  public int countParticipantsByInputField(String inputField) {
    Query participantQuery = createParticipantsByInputFieldQuery(inputField);

    List results = participantQuery.list();

    int count = 0;

    if(results != null) {
      count = results.size();
    }

    return count;
  }

  private Query createParticipantsByInputFieldQuery(String inputField, SortingClause... clauses) {
    Query participantQuery = null;

    String queryString = "from Participant p where p.barcode = :barcode or p.enrollmentId = :appointmentCode or p.lastName like :lastName or p.firstName like :firstName or concat(p.firstName, ' ', p.lastName) = :fullName";
    if(clauses != null) {
      int i = 0;
      for(SortingClause clause : clauses) {
        queryString += (i == 0) ? " order by " : ", ";
        queryString += clause.getField() + ((clause.isAscending()) ? " asc" : " desc");
        i++;
      }

      if(inputField != null) {
        participantQuery = getSession().createQuery(queryString);
        participantQuery.setString("barcode", inputField);
        participantQuery.setString("appointmentCode", inputField);
        participantQuery.setString("lastName", "%" + inputField + "%");
        participantQuery.setString("firstName", "%" + inputField + "%");
        participantQuery.setString("fullName", inputField);
      } else {
        participantQuery = getSession().createQuery("from Participant");
      }
    }
    return participantQuery;
  }

  protected List<Participant> getNotReceivedParticipants() {
    return getSession().createCriteria(Participant.class).add(Restrictions.isNull("barcode")).list();
  }

  @SuppressWarnings("unchecked")
  public Participant getParticipant(String participantIdentifier) throws NonUniqueParticipantException {
    Query participantQuery = getSession().createQuery("from Participant p where p.barcode = :identifier or p.enrollmentId = :identifier");
    participantQuery.setString("identifier", participantIdentifier);
    List<Participant> results = participantQuery.list();
    if(results.size() > 1) {
      throw new NonUniqueParticipantException("More than one participant was found for the specified identifier.");
    } else if(results.size() == 1) {
      return results.get(0);
    }
    return null;
  }

}
