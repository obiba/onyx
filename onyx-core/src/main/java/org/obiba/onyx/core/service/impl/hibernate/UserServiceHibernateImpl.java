package org.obiba.onyx.core.service.impl.hibernate;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.obiba.core.service.PagingClause;
import org.obiba.core.service.SortingClause;
import org.obiba.core.service.impl.hibernate.AssociationCriteria;
import org.obiba.core.service.impl.hibernate.AssociationCriteria.Operation;
import org.obiba.onyx.core.domain.user.User;
import org.obiba.onyx.core.service.impl.DefaultUserServiceImpl;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class UserServiceHibernateImpl extends DefaultUserServiceImpl {
  
  private SessionFactory factory;

  public void setSessionFactory(SessionFactory factory) {
    this.factory = factory;
  }

  private Session getSession() {
    return factory.getCurrentSession();
  }
  
  public List<User> getUsers(boolean isDeleted, PagingClause paging, SortingClause... clauses) {
    return getCriteria(isDeleted, paging, clauses).list();
  }
  
  private AssociationCriteria getCriteria(boolean isDeleted, PagingClause paging, SortingClause... clauses) {
    AssociationCriteria criteria = AssociationCriteria.create(User.class, getSession());

    if (isDeleted == false) criteria.add("deleted", Operation.eq, isDeleted);
    if(paging != null) criteria.addPagingClause(paging);
    if(clauses != null) criteria.addSortingClauses(clauses);
    
    return criteria;
  }
  
}
