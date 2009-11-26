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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Criteria;
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
import org.obiba.onyx.engine.variable.export.OnyxDataExportDestination;
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

  private List<OnyxDataExportDestination> exportDestinations;

  public void setExportDestinations(List<OnyxDataExportDestination> exportDestinations) {
    this.exportDestinations = exportDestinations;
  }

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
    Query participantQuery = createParticipantsByInputFieldQuery(inputField);

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

  private Query createParticipantsByInputFieldQuery(String inputField) {
    Query participantQuery = null;

    if(inputField != null) {
      participantQuery = getSession().createQuery("from Participant p where p.barcode = :barcode or p.enrollmentId = :appointmentCode or p.lastName like :lastName or p.firstName like :firstName or concat(p.firstName, ' ', p.lastName) = :fullName");
      participantQuery.setString("barcode", inputField);
      participantQuery.setString("appointmentCode", inputField);
      participantQuery.setString("lastName", "%" + inputField + "%");
      participantQuery.setString("firstName", "%" + inputField + "%");
      participantQuery.setString("fullName", inputField);
    } else {
      participantQuery = getSession().createQuery("from Participant");
    }

    return participantQuery;
  }

  protected List<Participant> getNotReceivedParticipants() {
    return getSession().createCriteria(Participant.class).add(Restrictions.isNull("barcode")).list();
  }

  public List<Participant> getExportedParticipants() {
    return getExportedParticipants(null);
  }

  public List<Participant> getExportedParticipants(Date exportedBefore) {
    AssociationCriteria criteria = AssociationCriteria.create(Participant.class, getSession());
    criteria.add("exported", Operation.eq, true);
    if(exportedBefore != null) {
      criteria.add("exportDate", Operation.lt, exportedBefore);
    }
    return criteria.list();
  }

  public List<Participant> getNonExportableParticipants() {
    return getNonExportableParticipants(null);
  }

  public List<Participant> getNonExportableParticipants(Date olderThan) {

    Set<InterviewStatus> exportedInterviewStatuses = new HashSet<InterviewStatus>();
    // TODO: changes to OnyxExportDestination/Magma integration make this fail. Need new way to purge.
    // for(OnyxDataExportDestination destination : exportDestinations) {
    // exportedInterviewStatuses.addAll(destination.getExportedInterviewStatuses());
    // }

    Criteria criteria = getSession().createCriteria(Participant.class);
    criteria.createAlias("interview", "interview");
    criteria.add(Restrictions.isNotNull("interview"));
    for(InterviewStatus interviewStatus : exportedInterviewStatuses) {
      criteria.add(Restrictions.ne("interview.status", interviewStatus));
    }

    if(olderThan != null) {
      criteria.add(Restrictions.lt("interview.startDate", olderThan));
    }

    return criteria.list();
  }
}
