/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.core.wicket.seed;

import java.util.LinkedList;

import org.apache.wicket.protocol.http.WebApplication;
import org.obiba.core.service.PersistenceManager;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.wicket.util.seed.XstreamResourceDatabaseSeed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import com.thoughtworks.xstream.XStream;

public class JadeDatabaseSeed extends XstreamResourceDatabaseSeed {

  private final Logger log = LoggerFactory.getLogger(getClass());

  private PersistenceManager persistenceManager;

  public void setPersistenceManager(PersistenceManager persistenceManager) {
    this.persistenceManager = persistenceManager;
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void handleXstreamResult(Resource resource, Object result) {
    if(result != null && result instanceof LinkedList) {
      LinkedList<Instrument> instruments = (LinkedList<Instrument>) result;

      for(Instrument instrument : instruments) {
        Instrument template = new Instrument();
        template.setBarcode(instrument.getBarcode());

        if(persistenceManager.count(template) == 0) {
          persistenceManager.save(instrument);
          log.info("Persisted instrument {} with barcode {}.", instrument.getName(), instrument.getBarcode());
        } else {
          log.info("Not persisting instrument {} with barcode {}. An instrument with the same barcode already exists.", instrument.getName(), instrument.getBarcode());
        }
      }
    }
  }

  @Override
  protected boolean shouldSeed(WebApplication application) {
    return true;
  }

  @Override
  protected void initializeXstream(XStream xstream) {
    super.initializeXstream(xstream);
    xstream.alias("instruments", LinkedList.class);
    xstream.alias("instrument", Instrument.class);
  }
}
