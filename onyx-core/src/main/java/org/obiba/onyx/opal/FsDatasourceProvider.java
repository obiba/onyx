package org.obiba.onyx.opal;

import java.io.File;

import org.obiba.magma.MagmaEngine;
import org.obiba.magma.datasource.fs.FsDatasource;
import org.obiba.onyx.core.service.ParticipantService;
import org.obiba.onyx.engine.variable.export.EnrollmentIdDatasource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class FsDatasourceProvider implements InitializingBean {

  private static final Logger log = LoggerFactory.getLogger(FsDatasourceProvider.class);

  private ParticipantService participantService;

  private MagmaEngine magmaEngine;

  private File file;

  private String datasourceName;

  public void setParticipantService(ParticipantService participantService) {
    this.participantService = participantService;
  }

  public void setMagmaEngine(MagmaEngine magmaEngine) {
    this.magmaEngine = magmaEngine;
  }

  public void setFile(File file) {
    this.file = file;
  }

  public void setDatasourceName(String datasourceName) {
    this.datasourceName = datasourceName;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    if(datasourceName != null && datasourceName.isEmpty() == false) {
      FsDatasource ds = new FsDatasource(datasourceName, file);
      log.info("Adding datasource: {}", datasourceName);
      if(participantService != null) {
        magmaEngine.addDatasource(new EnrollmentIdDatasource(participantService, ds));
      } else {
        magmaEngine.addDatasource(ds);
      }
    }
  }
}
