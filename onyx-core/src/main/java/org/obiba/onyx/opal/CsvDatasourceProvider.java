package org.obiba.onyx.opal;

import java.io.File;

import org.obiba.magma.MagmaEngine;
import org.obiba.magma.datasource.csv.CsvDatasource;
import org.obiba.onyx.core.service.ParticipantService;
import org.obiba.onyx.engine.variable.export.EnrollmentIdDatasource;
import org.springframework.beans.factory.InitializingBean;

public class CsvDatasourceProvider implements InitializingBean {

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
      String table = tableName == null ? file.getName().substring(0, file.getName().lastIndexOf('.')) : tableName;
      ds.addValueTable(table, file, entityType == null ? "Participant" : entityType);
      if(participantService != null) {
        magmaEngine.addDatasource(new EnrollmentIdDatasource(participantService, ds));
      } else {
        magmaEngine.addDatasource(ds);
      }
    }
  }
}
