package org.obiba.onyx.opal;

import org.obiba.magma.MagmaEngine;
import org.obiba.onyx.core.service.ParticipantService;
import org.obiba.onyx.engine.variable.export.EnrollmentIdDatasource;
import org.obiba.opal.rest.client.magma.OpalJavaClient;
import org.obiba.opal.rest.client.magma.RestDatasource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class OpalDatasourceProvider implements InitializingBean {

  private static final Logger log = LoggerFactory.getLogger(OpalDatasourceProvider.class);

  private ParticipantService participantService;

  private MagmaEngine magmaEngine;

  private OpalJavaClient opalJavaClient;

  private String datasourceName;

  private String[] datasourceNames;

  public void setParticipantService(ParticipantService participantService) {
    this.participantService = participantService;
  }

  public void setMagmaEngine(MagmaEngine magmaEngine) {
    this.magmaEngine = magmaEngine;
  }

  public void setOpalJavaClient(OpalJavaClient opalJavaClient) {
    this.opalJavaClient = opalJavaClient;
  }

  public void setDatasourceName(String datasourceName) {
    this.datasourceName = datasourceName;
  }

  public void setDatasourceNames(String[] datasourceNames) {
    this.datasourceNames = datasourceNames;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    if(datasourceName != null) {
      addDatasource(datasourceName);
    }

    if(datasourceNames != null) {
      for(String dsName : datasourceNames) {
        addDatasource(dsName);
      }
    }
  }

  private void addDatasource(String dsName) {
    if(dsName.isEmpty() == false) {
      log.info("Adding datasource: {}", dsName);
      magmaEngine.addDatasource(new EnrollmentIdDatasource(participantService, new RestDatasource(dsName, opalJavaClient, dsName)));
    }
  }
}
