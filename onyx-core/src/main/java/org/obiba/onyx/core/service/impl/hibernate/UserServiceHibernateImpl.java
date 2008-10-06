package org.obiba.onyx.core.service.impl.hibernate;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.obiba.core.service.PagingClause;
import org.obiba.core.service.SortingClause;
import org.obiba.core.service.impl.hibernate.AssociationCriteria;
import org.obiba.core.service.impl.hibernate.AssociationCriteria.Operation;
import org.obiba.onyx.core.domain.user.Role;
import org.obiba.onyx.core.domain.user.User;
import org.obiba.onyx.core.service.impl.DefaultUserServiceImpl;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of hibernate specific methods of User Service
 * @see#UserService.
 * @author acarey
 * 
 */
@Transactional
public class UserServiceHibernateImpl extends DefaultUserServiceImpl {
  
  private SessionFactory factory;

  public void setSessionFactory(SessionFactory factory) {
    this.factory = factory;
  }

  private Session getSession() {
    return factory.getCurrentSession();
  }
  
  public List<User> getUsers(User template, PagingClause paging, SortingClause... clauses) {
    return getUserCriteria(template, paging, clauses).list();
  }
  
  public int getUserCount(User template) {
    return getUserCriteria(template, null, (SortingClause) null).count();
  }
  
  private AssociationCriteria getUserCriteria(User template, PagingClause paging, SortingClause... clauses) {
    AssociationCriteria criteria = AssociationCriteria.create(User.class, getSession());

    if (template.isDeleted() == false) criteria.add("deleted", Operation.eq, template.isDeleted());
    if(paging != null) criteria.addPagingClause(paging);
    if(clauses != null) criteria.addSortingClauses(clauses);
    
    return criteria;
  }
  
  public List<Role> getRoles(SortingClause... clauses) {
    return getRoleCriteria(clauses).list();
  }
  
  private AssociationCriteria getRoleCriteria(SortingClause... clauses) {
    AssociationCriteria criteria = AssociationCriteria.create(Role.class, getSession());

    if(clauses != null) criteria.addSortingClauses(clauses);

    return criteria;
  }
  
}
