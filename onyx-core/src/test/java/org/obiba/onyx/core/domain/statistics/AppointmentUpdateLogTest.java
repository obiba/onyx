package org.obiba.onyx.core.domain.statistics;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class AppointmentUpdateLogTest {

  private ObjectMapper mapper = new ObjectMapper();

  @Test
  public void deserializationTest() throws IOException {
    ArrayList<AppointmentUpdateLog> logList = new ArrayList<>();
    logList.add(new AppointmentUpdateLog(new Date(), AppointmentUpdateLog.Level.INFO, "test 1"));

    String logListAsString = mapper.writeValueAsString(logList);

    mapper.readValue(logListAsString, mapper.getTypeFactory().constructCollectionType(ArrayList.class, AppointmentUpdateLog.class));
  }
}
