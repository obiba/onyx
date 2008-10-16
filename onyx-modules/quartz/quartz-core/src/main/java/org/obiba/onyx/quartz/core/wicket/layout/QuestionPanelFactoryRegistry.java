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

public class QuestionPanelFactoryRegistry {

  private static final Logger log = LoggerFactory.getLogger(QuestionPanelFactoryRegistry.class);
  
  private Map<String, IQuestionPanelFactory> factories = Collections.synchronizedMap(new HashMap<String, IQuestionPanelFactory>());

  public void registerFactory(IQuestionPanelFactory factory) {
    log.info("Registering IQuestionPanelFactory {}", factory.getName());
    factories.put(factory.getName(), factory);
  }

  public void unregisterFactory(String name) {
    factories.remove(name);
  }
  
  public IQuestionPanelFactory getFactory(String name) {
    return factories.get(name);
  }

}
