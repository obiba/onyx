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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.wicket.protocol.http.WebApplication;
import org.obiba.core.service.PersistenceManager;
import org.obiba.onyx.core.domain.application.ApplicationConfiguration;
import org.obiba.onyx.core.domain.participant.Appointment;
import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.domain.user.Role;
import org.obiba.onyx.core.domain.user.User;
import org.obiba.onyx.crypt.OnyxKeyStore;
import org.obiba.wicket.util.seed.XstreamResourceDatabaseSeed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;

import com.thoughtworks.xstream.XStream;

public class TestDatabaseSeed extends XstreamResourceDatabaseSeed {
  private final Logger log = LoggerFactory.getLogger(getClass());

  @Autowired
  private PersistenceManager persistenceManager;

  @Autowired
  private OnyxKeyStore keystore;

  @Override
  protected void handleXstreamResult(Resource resource, Object result) {
    if(result != null) {
      SeedConfiguration config = (SeedConfiguration) result;

      for(Object entity : config.entities) {
        log.info("Seeding database with entity {} of type {}", entity, entity.getClass().getSimpleName());
        persistenceManager.save(entity);
      }

      ApplicationConfiguration oldConfig = persistenceManager.matchOne(new ApplicationConfiguration());
      if(oldConfig != null) {
        persistenceManager.delete(oldConfig);
      }
      // Save new config
      persistenceManager.save(config.config);

      for(User user : config.users) {
        String encryptedPassword = User.digest(user.getPassword());
        log.info("Password: " + user.getPassword());
        log.info("Encypted Password: " + encryptedPassword);
        user.setPassword(encryptedPassword);

        User template = new User();
        template.setLogin(user.getLogin());
        for(User u : persistenceManager.match(template)) {
          persistenceManager.delete(u);
        }
        Set<Role> persistedRoles = new HashSet<Role>();
        for(Role role : user.getRoles()) {
          Role persistedRole = persistenceManager.matchOne(role);
          persistedRoles.add(persistedRole);
        }
        user.setRoles(persistedRoles);
        persistenceManager.save(user);
      }

      for(Map.Entry<String, String> certEntry : config.destinationCerts.entrySet()) {
        keystore.setCertificate(certEntry.getKey(), certEntry.getValue());
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

    xstream.alias("seedConfig", SeedConfiguration.class);
    xstream.alias("user", User.class);
    xstream.alias("role", Role.class);
    xstream.alias("participant", Participant.class);
    xstream.alias("interview", Interview.class);
    xstream.alias("appointment", Appointment.class);

  }

  private static class SeedConfiguration {

    private ApplicationConfiguration config;

    private List<User> users;

    private Map<String, String> destinationCerts;

    private List<Object> entities;

  }

}
