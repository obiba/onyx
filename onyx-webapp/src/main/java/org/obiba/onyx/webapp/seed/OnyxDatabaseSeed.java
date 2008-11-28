/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.webapp.seed;

import java.util.List;

import org.apache.wicket.protocol.http.WebApplication;
import org.obiba.core.service.PersistenceManager;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.engine.StageDependencyCondition;
import org.obiba.wicket.util.seed.XstreamResourceDatabaseSeed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;

public class OnyxDatabaseSeed extends XstreamResourceDatabaseSeed {
  private final Logger log = LoggerFactory.getLogger(getClass());

  private PersistenceManager persistenceManager;

  public void setPersistenceManager(PersistenceManager persistenceManager) {
    this.persistenceManager = persistenceManager;
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void handleXstreamResult(Object result) {
    if(result != null && result instanceof List) {
      List<Object> objects = (List<Object>) result;
      for(Object entity : objects) {
        log.info("Seeding database with entity {} of type {}", entity, entity.getClass().getSimpleName());
        persistenceManager.save(entity);
      }
    }
  }

  @Override
  protected boolean shouldSeed(WebApplication application) {
    return (persistenceManager.count(Stage.class) == 0);
  }

  @Override
  protected void initializeXstream(XStream xstream) {
    super.initializeXstream(xstream);
    xstream.alias("stage", Stage.class);
    xstream.alias("stageDependencyCondition", StageDependencyCondition.class);
  }
}
