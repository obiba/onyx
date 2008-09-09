package org.obiba.onyx.core.service.impl.hibernate;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.obiba.core.service.SortingClause;
import org.obiba.core.service.impl.hibernate.AssociationCriteria;
import org.obiba.onyx.core.domain.user.Role;
import org.obiba.onyx.core.service.impl.DefaultRoleServiceImpl;

public class RoleServiceHibernateImpl extends DefaultRoleServiceImpl {

  private SessionFactory factory;

  public void setSessionFactory(SessionFactory factory) {
    this.factory = factory;
  }

  private Session getSession() {
    return factory.getCurrentSession();
  }
  
  public List<Role> getRoles(SortingClause... clauses) {
    return getCriteria(clauses).list();
  }
  
  private AssociationCriteria getCriteria(SortingClause... clauses) {
    AssociationCriteria criteria = AssociationCriteria.create(Role.class, getSession());

    if(clauses != null) criteria.addSortingClauses(clauses);

    return criteria;
  }

}
