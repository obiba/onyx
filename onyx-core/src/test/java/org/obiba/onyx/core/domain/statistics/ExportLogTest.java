package org.obiba.onyx.core.domain.statistics;

import java.util.Calendar;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.obiba.onyx.core.domain.user.User;

public class ExportLogTest {

  private static final Date sevenAm = ExportLogTest.constructDate(7);

  private static final Date eightAm = ExportLogTest.constructDate(8);

  private static final Date nineAm = ExportLogTest.constructDate(9);

  @Before
  public void setUp() throws Exception {
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullIdentifierFails() throws Exception {
    ExportLog.Builder.newLog().type("Participant").destination("DCC").start(sevenAm).end(eightAm).exportDate(nineAm).user(new User()).build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyIdentifierFails() throws Exception {
    ExportLog.Builder.newLog().type("Participant").identifier("").destination("DCC").start(sevenAm).end(eightAm).exportDate(nineAm).user(new User()).build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullCaptureStartDateFails() throws Exception {
    ExportLog.Builder.newLog().type("Participant").identifier("1234567").destination("DCC").end(eightAm).exportDate(nineAm).user(new User()).build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCaptureEndDateBeforeCaptureStartDateFails() throws Exception {
    ExportLog.Builder.newLog().type("Participant").identifier("1234567").destination("DCC").start(eightAm).end(sevenAm).exportDate(nineAm).user(new User()).build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testExportDateBeforeCaptureEndDateFails() throws Exception {
    ExportLog.Builder.newLog().type("Participant").identifier("1234567").destination("DCC").start(sevenAm).end(nineAm).exportDate(eightAm).user(new User()).build();
  }

  private static Date constructDate(int hour) {
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.HOUR, hour);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    calendar.set(Calendar.AM_PM, Calendar.AM);
    return calendar.getTime();
  }
}
