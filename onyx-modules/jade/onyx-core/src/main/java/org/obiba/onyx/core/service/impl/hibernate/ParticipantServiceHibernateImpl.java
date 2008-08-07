package org.obiba.onyx.core.service.impl.hibernate;

import java.util.Date;
import java.util.List;

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

  public List<Participant> getParticipants(String barcode, String likeName, PagingClause paging, SortingClause... clauses) {
    return getCriteria(barcode, likeName, paging, clauses).list();
  }

  public int countParticipants(String barcode, String likeName) {
    return getCriteria(barcode, likeName, null).count();
  }

  public List<Participant> getParticipants(String barcode, String likeName, InterviewStatus status, PagingClause paging, SortingClause... clauses) {
    AssociationCriteria criteria = getCriteria(barcode, likeName, paging, clauses);
    if(status != null) criteria.add("interview.status", Operation.eq, status);
    return criteria.list();
  }

  public int countParticipants(String barcode, String likeName, InterviewStatus status) {
    AssociationCriteria criteria = getCriteria(barcode, likeName, null);
    if(status != null) criteria.add("interview.status", Operation.eq, status);
    return criteria.count();
  }

  public List<Participant> getParticipants(String barcode, String likeName, Date from, Date to, PagingClause paging, SortingClause... clauses) {
    AssociationCriteria criteria = getCriteria(barcode, likeName, paging, clauses);
    if(from != null) criteria.add("lastAppointmentDate", Operation.ge, from);
    if(to != null) criteria.add("lastAppointmentDate", Operation.le, to);
    return criteria.list();
  }

  public int countParticipants(String barcode, String likeName, Date from, Date to) {
    AssociationCriteria criteria = getCriteria(barcode, likeName, null);
    if(from != null) criteria.add("lastAppointmentDate", Operation.ge, from);
    if(to != null) criteria.add("lastAppointmentDate", Operation.le, to);
    return criteria.count();
  }

  private AssociationCriteria getCriteria(String barcode, String likeName, PagingClause paging, SortingClause... clauses) {
    AssociationCriteria criteria = AssociationCriteria.create(Participant.class, getSession());

    if(barcode != null) criteria.add("barcode", Operation.eq, barcode);
    if(likeName != null) criteria.add("lastName", Operation.ilike, likeName + "%");
    if(paging != null) criteria.addPagingClause(paging);
    if(clauses != null) criteria.addSortingClauses(clauses);

    return criteria;
  }

}
