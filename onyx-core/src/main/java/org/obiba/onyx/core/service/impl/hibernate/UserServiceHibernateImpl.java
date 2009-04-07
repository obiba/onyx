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

  @Transactional(readOnly = true)
  private Session getSession() {
    return factory.getCurrentSession();
  }

  @Transactional(readOnly = true)
  public List<User> getUsers(User template, PagingClause paging, SortingClause... clauses) {
    return getUserCriteria(template, paging, clauses).list();
  }

  @Transactional(readOnly = true)
  public int getUserCount(User template) {
    return getUserCriteria(template, null, (SortingClause) null).count();
  }

  private AssociationCriteria getUserCriteria(User template, PagingClause paging, SortingClause... clauses) {
    AssociationCriteria criteria = AssociationCriteria.create(User.class, getSession());

    if(template.isDeleted() == false) criteria.add("deleted", Operation.eq, template.isDeleted());
    if(paging != null) criteria.addPagingClause(paging);
    if(clauses != null) criteria.addSortingClauses(clauses);

    return criteria;
  }

}
