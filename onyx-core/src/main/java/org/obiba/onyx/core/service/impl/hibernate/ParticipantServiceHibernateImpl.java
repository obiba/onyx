package org.obiba.onyx.core.service.impl.hibernate;

import java.util.Date;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.obiba.core.service.PagingClause;
import org.obiba.core.service.SortingClause;
import org.obiba.core.service.impl.hibernate.AssociationCriteria;
import org.obiba.core.service.impl.hibernate.AssociationCriteria.Operation;
import org.obiba.onyx.core.domain.participant.InterviewStatus;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.service.impl.DefaultParticipantServiceImpl;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of hibernate specific methods of Participant Service
 * @see#ParticipantService.
 * @author Yannick Marcon
 * 
 */
@Transactional
public class ParticipantServiceHibernateImpl extends DefaultParticipantServiceImpl {

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
    AssociationCriteria criteria = getCriteria(null, (SortingClause[])null);
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
    AssociationCriteria criteria = getCriteria(null, (SortingClause[])null);

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

  public List<Participant> getParticipantsByLastName(String likeName, PagingClause paging, SortingClause... clauses) {
    AssociationCriteria criteria = getCriteria(paging, clauses);

    if(likeName != null) criteria.add("lastName", Operation.ilike, likeName + "%");

    return criteria.list();
  }

  public int countParticipantsByLastName(String likeName) {
    AssociationCriteria criteria = getCriteria(null, (SortingClause[])null);

    if(likeName != null) criteria.add("lastName", Operation.ilike, likeName + "%");

    return criteria.count();
  }

  public List<Participant> getParticipantsByCode(String code, PagingClause paging, SortingClause... clauses) {
    Query participantQuery = createParticipantsByCodeQuery(code);
    
    participantQuery.setMaxResults(paging.getLimit());
    participantQuery.setFirstResult(paging.getOffset());
    
    return participantQuery.list();
  }

  public int countParticipantsByCode(String code) {
    Query participantQuery = createParticipantsByCodeQuery(code);

    List results = participantQuery.list();
    
    int count = 0;
    
    if (results != null) {
      count = results.size();  
    }
    
    return count;
  }
  
  private Query createParticipantsByCodeQuery(String code) {
    Query participantQuery = null;
    
    if(code != null) {
      participantQuery = getSession().createQuery("from Participant p where p.barcode = :barcode or p.appointment.appointmentCode = :appointmentCode");
      participantQuery.setString("barcode", code);      
      participantQuery.setString("appointmentCode", code);
    }
    else {
      participantQuery = getSession().createQuery("from Participant");  
    }
    
    return participantQuery;
  }
}
