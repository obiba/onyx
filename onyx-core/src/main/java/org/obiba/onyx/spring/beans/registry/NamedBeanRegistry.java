/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.spring.beans.registry;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NamedBean;

public class NamedBeanRegistry<T extends NamedBean> {

  private static final Logger log = LoggerFactory.getLogger(NamedBeanRegistry.class);

  private Map<String, T> namedBeans = Collections.synchronizedMap(new HashMap<String, T>());

  public void register(T bean) {
    log.info("Registering T {}", bean.getBeanName());
    if(namedBeans.containsKey(bean.getBeanName())) throw new IllegalArgumentException("Bean with name " + bean.getBeanName() + " already registered in " + getClass().getSimpleName() + ".");
    namedBeans.put(bean.getBeanName(), bean);
  }

  public void unregister(String name) {
    namedBeans.remove(name);
  }

  public T get(String name) {
    return namedBeans.get(name);
  }

}
