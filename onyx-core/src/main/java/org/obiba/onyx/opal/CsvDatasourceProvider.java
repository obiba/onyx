package org.obiba.onyx.opal;

import java.io.File;

import org.obiba.magma.MagmaEngine;
import org.obiba.magma.datasource.csv.CsvDatasource;
import org.obiba.onyx.core.service.ParticipantService;
import org.obiba.onyx.engine.variable.export.EnrollmentIdDatasource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class CsvDatasourceProvider implements InitializingBean {

  private static final Logger log = LoggerFactory.getLogger(CsvDatasourceProvider.class);

  private ParticipantService participantService;

  private MagmaEngine magmaEngine;

  private File file;

  private String entityType;

  private String tableName;

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

  public void setEntityType(String entityType) {
    this.entityType = entityType;
  }

  public void setDatasourceName(String datasourceName) {
    this.datasourceName = datasourceName;
  }

  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    if(datasourceName != null && datasourceName.isEmpty() == false) {
      CsvDatasource ds = new CsvDatasource(datasourceName);
      if(file.isDirectory()) {
        ds.addValueTable(file);
      } else {
        String table = tableName == null ? file.getName().substring(0, file.getName().lastIndexOf('.')) : tableName;
        ds.addValueTable(table, file, entityType == null ? "Participant" : entityType);
      }
      log.info("Adding datasource: {}", datasourceName);
      if(participantService != null) {
        magmaEngine.addDatasource(new EnrollmentIdDatasource(participantService, ds));
      } else {
        magmaEngine.addDatasource(ds);
      }
    }
  }
}
