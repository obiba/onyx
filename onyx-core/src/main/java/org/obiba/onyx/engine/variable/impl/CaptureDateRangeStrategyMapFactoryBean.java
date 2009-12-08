/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.engine.variable.impl;

import java.util.HashMap;
import java.util.Map;

import org.obiba.onyx.engine.variable.CaptureDateRangeStrategy;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * FactoryBean for creating a map of entity types to CaptureDateRangeStrategies.
 */
public class CaptureDateRangeStrategyMapFactoryBean implements FactoryBean, ApplicationContextAware {
  //
  // Instance Variables
  //

  private ApplicationContext applicationContext;

  private Map<String, CaptureDateRangeStrategy> strategyMap;

  //
  // FactoryBean Methods
  //

  @SuppressWarnings("unchecked")
  public Object getObject() throws Exception {
    if(strategyMap == null) {
      strategyMap = new HashMap<String, CaptureDateRangeStrategy>();

      Map strategies = applicationContext.getBeansOfType(CaptureDateRangeStrategy.class);
      for(Object value : strategies.values()) {
        CaptureDateRangeStrategy strategy = (CaptureDateRangeStrategy) value;
        strategyMap.put(strategy.getEntityType(), strategy);
      }
    }
    return strategyMap;
  }

  @SuppressWarnings("unchecked")
  public Class getObjectType() {
    return Map.class;
  }

  public boolean isSingleton() {
    return true;
  }

  //
  // ApplicationContextAware Methods
  //

  public void setApplicationContext(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }
}
