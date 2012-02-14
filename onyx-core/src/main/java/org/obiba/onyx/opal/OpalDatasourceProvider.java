package org.obiba.onyx.opal;

import org.obiba.magma.MagmaEngine;
import org.obiba.onyx.core.service.ParticipantService;
import org.obiba.onyx.engine.variable.export.EnrollmentIdDatasource;
import org.obiba.opal.rest.client.magma.OpalJavaClient;
import org.obiba.opal.rest.client.magma.RestDatasource;
import org.springframework.beans.factory.InitializingBean;

public class OpalDatasourceProvider implements InitializingBean {

  private ParticipantService participantService;

  private MagmaEngine magmaEngine;

  private OpalJavaClient opalJavaClient;

  private String datasourceName;

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

  @Override
  public void afterPropertiesSet() throws Exception {
    if(datasourceName != null && datasourceName.isEmpty() == false) {
      magmaEngine.addDatasource(new EnrollmentIdDatasource(participantService, new RestDatasource(datasourceName, opalJavaClient, datasourceName)));
    }
  }
}
