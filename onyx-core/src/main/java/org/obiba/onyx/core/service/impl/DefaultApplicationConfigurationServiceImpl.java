/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.service.impl;

import java.io.IOException;

import org.obiba.core.service.impl.PersistenceManagerAwareService;
import org.obiba.onyx.core.domain.application.ApplicationConfiguration;
import org.obiba.onyx.core.service.ApplicationConfigurationService;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class DefaultApplicationConfigurationServiceImpl extends PersistenceManagerAwareService implements ApplicationConfigurationService, ResourceLoaderAware {
  protected ResourceLoader resourceLoader;

  public void createApplicationConfiguration(ApplicationConfiguration appConfiguration) {
    try {
      appConfiguration.setParticipantDirectoryPath(resourceLoader.getResource("/participants/").getFile().getAbsolutePath());
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
    persistenceManager.save(appConfiguration);
  }

  public void setResourceLoader(ResourceLoader resourceLoader) {
    this.resourceLoader = resourceLoader;
  }

}
