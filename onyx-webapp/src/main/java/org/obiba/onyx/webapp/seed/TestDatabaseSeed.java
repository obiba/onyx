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
import org.obiba.onyx.core.domain.application.ApplicationConfiguration;
import org.obiba.onyx.core.domain.participant.Appointment;
import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.domain.user.Role;
import org.obiba.onyx.core.domain.user.User;
import org.obiba.wicket.util.seed.XstreamResourceDatabaseSeed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import com.thoughtworks.xstream.XStream;

public class TestDatabaseSeed extends XstreamResourceDatabaseSeed {
  private final Logger log = LoggerFactory.getLogger(getClass());

  private PersistenceManager persistenceManager;

  public void setPersistenceManager(PersistenceManager persistenceManager) {
    this.persistenceManager = persistenceManager;
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void handleXstreamResult(Resource resource, Object result) {
    if(result != null && result instanceof List) {
      List<Object> objects = (List<Object>) result;
      for(Object entity : objects) {

        // Encrypt password
        if(entity instanceof User) {
          User user = (User) entity;
          String encryptedPassword = User.digest(user.getPassword());
          user.setPassword(encryptedPassword);
          log.info("Password: " + user.getPassword());
          log.info("Encypted Password: " + encryptedPassword);

          User template = new User();
          template.setLogin(user.getLogin());
          for(User u : persistenceManager.match(template)) {
            persistenceManager.delete(u);
          }
          if(persistenceManager.count(template) > 0) entity = null;
        } else if(entity instanceof ApplicationConfiguration) {
          ApplicationConfiguration template = new ApplicationConfiguration();
          for(ApplicationConfiguration conf : persistenceManager.match(template)) {
            persistenceManager.delete(conf);
          }
          ApplicationConfiguration appConfig = (ApplicationConfiguration) entity;
        }

        if(entity != null) {
          log.info("Seeding database with entity {} of type {}", entity, entity.getClass().getSimpleName());
          persistenceManager.save(entity);
        }
      }
    }
  }

  @Override
  protected boolean shouldSeed(WebApplication application) {
    boolean seed = super.shouldSeed(application);
    return seed && (persistenceManager.count(User.class) == 0);
  }

  @Override
  protected void initializeXstream(XStream xstream) {
    super.initializeXstream(xstream);

    xstream.alias("config", ApplicationConfiguration.class);
    xstream.alias("role", Role.class);
    xstream.alias("user", User.class);
    xstream.alias("participant", Participant.class);
    xstream.alias("interview", Interview.class);
    xstream.alias("appointment", Appointment.class);

  }
}
