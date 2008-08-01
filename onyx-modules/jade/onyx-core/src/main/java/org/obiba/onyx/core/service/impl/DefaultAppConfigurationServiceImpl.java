package org.obiba.onyx.core.service.impl;

import org.obiba.core.service.impl.PersistenceManagerAwareService;
import org.obiba.onyx.core.domain.application.AppConfiguration;
import org.obiba.onyx.core.service.AppConfigurationService;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class DefaultAppConfigurationServiceImpl extends PersistenceManagerAwareService implements AppConfigurationService {

  public void createAppConfiguration(AppConfiguration appConfiguration) {
    persistenceManager.save(appConfiguration);
  }

}
