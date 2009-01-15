/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.engine.variable.export;

import java.util.List;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * Factory bean for creating a chain of {@link IOnyxDataExportStrategy}. The attribute {@code chainedStrategies} should
 * be set with a list of implementations of {@code IChainingOnyxDataExportStrategy}. Only the last item in the list is
 * permitted to not implement {@code IChainingOnyxDataExportStrategy} (since it has nothing to chain to).
 */
public class ChainingDataExportStrategyWiringFactoryBean implements FactoryBean, InitializingBean {

  List<IOnyxDataExportStrategy> chainedStrategies;

  public void setChainedStrategies(List<IOnyxDataExportStrategy> chainedStrategies) {
    this.chainedStrategies = chainedStrategies;
  }

  public Object getObject() throws Exception {
    return chainedStrategies.get(0);
  }

  public Class<?> getObjectType() {
    return IOnyxDataExportStrategy.class;
  }

  public boolean isSingleton() {
    return true;
  }

  public void afterPropertiesSet() throws Exception {
    if(chainedStrategies == null) {
      throw new IllegalStateException("chainedStrategies attribute must be set.");
    }
    if(chainedStrategies.size() == 0) {
      throw new IllegalStateException("chainedStrategies attribute must contain at least one element.");
    }

    // Build the chain of export strategies.
    IOnyxDataExportStrategy previousStrategy = chainedStrategies.get(0);
    for(int i = 1; i < chainedStrategies.size(); i++) {
      IOnyxDataExportStrategy delegate = chainedStrategies.get(i);
      if(previousStrategy instanceof IChainingOnyxDataExportStrategy) {
        ((IChainingOnyxDataExportStrategy) previousStrategy).setDelegate(delegate);
        previousStrategy = delegate;
      } else {
        throw new IllegalArgumentException("Strategy type must implement IChainingOnyxDataExportStrategy in order to be chained with another. Violating type is " + previousStrategy.getClass().getName());
      }
    }
  }
}
