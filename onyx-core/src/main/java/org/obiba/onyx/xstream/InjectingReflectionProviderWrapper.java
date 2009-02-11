/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.xstream;

import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;

import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import com.thoughtworks.xstream.converters.reflection.ReflectionProviderWrapper;

/**
 * 
 */
public class InjectingReflectionProviderWrapper extends ReflectionProviderWrapper {

  private ApplicationContext applicationContext;

  public InjectingReflectionProviderWrapper(ReflectionProvider wrapper, ApplicationContext applicationContext) {
    super(wrapper);
    this.applicationContext = applicationContext;
  }

  @Override
  public Object newInstance(Class type) {
    // Let the wrapped instance create the bean
    Object value = super.newInstance(type);
    if(value != null) {
      // If we can, autowire the instance
      if(applicationContext != null && applicationContext.getAutowireCapableBeanFactory() != null) {
        // Autowire by type
        applicationContext.getAutowireCapableBeanFactory().autowireBeanProperties(value, AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, false);
      }
    }
    return value;
  }

}
