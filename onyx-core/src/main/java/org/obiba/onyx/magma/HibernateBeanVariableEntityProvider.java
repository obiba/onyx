/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.magma;

import org.hibernate.SessionFactory;
import org.obiba.magma.beans.AbstractBeanVariableEntityProvider;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 */
public class HibernateBeanVariableEntityProvider<T> extends AbstractBeanVariableEntityProvider<T> {

  private SessionFactory sessionFactory;

  private Class<T> entityClass;

  public HibernateBeanVariableEntityProvider(String entityType, String entityIdentifierPropertyPath) {
    super(entityType, entityIdentifierPropertyPath);
  }

  @Autowired(required = true)
  public void setSessionFactory(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Autowired(required = true)
  public void setEntityClass(Class<T> entityClass) {
    this.entityClass = entityClass;
  }

  public SessionFactory getSessionFactory() {
    return sessionFactory;
  }

  @Override
  protected Iterable<T> loadBeans() {
    return sessionFactory.getCurrentSession().createCriteria(entityClass).list();
  }

}
