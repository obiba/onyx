/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PageLayoutFactoryRegistry {

  private static final Logger log = LoggerFactory.getLogger(PageLayoutFactoryRegistry.class);

  private Map<String, IPageLayoutFactory> factories = Collections.synchronizedMap(new HashMap<String, IPageLayoutFactory>());

  public void registerFactory(IPageLayoutFactory factory) {
    log.info("Registering IPageLayoutFactory {}", factory.getName());
    if(factories.containsKey(factory.getName())) throw new IllegalArgumentException("IPageLayoutFactory with name " + factory.getName() + " already registered.");
    factories.put(factory.getName(), factory);
  }

  public void unregisterFactory(String name) {
    factories.remove(name);
  }

  public IPageLayoutFactory getFactory(String name) {
    return factories.get(name);
  }

}
