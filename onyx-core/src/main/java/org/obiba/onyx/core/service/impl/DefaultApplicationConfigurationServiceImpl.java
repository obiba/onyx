package org.obiba.onyx.core.service.impl;

import org.obiba.core.service.impl.PersistenceManagerAwareService;
import org.obiba.onyx.core.domain.application.ApplicationConfiguration;
import org.obiba.onyx.core.service.ApplicationConfigurationService;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class DefaultApplicationConfigurationServiceImpl extends PersistenceManagerAwareService implements ApplicationConfigurationService {

  public void createApplicationConfiguration(ApplicationConfiguration appConfiguration) {
    persistenceManager.save(appConfiguration);
  }

}
