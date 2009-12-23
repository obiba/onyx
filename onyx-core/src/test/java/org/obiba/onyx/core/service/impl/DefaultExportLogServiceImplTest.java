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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.obiba.core.service.PersistenceManager;
import org.obiba.core.test.spring.BaseDefaultSpringContextTestCase;
import org.obiba.onyx.core.domain.statistics.ExportLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class DefaultExportLogServiceImplTest extends BaseDefaultSpringContextTestCase {
  //
  // Instance Variables
  //

  @Autowired(required = true)
  private PersistenceManager persistenceManager;

  private DefaultExportLogServiceImpl exportLogService;

  //
  // Fixture Methods (setUp / tearDown)
  //

  @Before
  public void setUp() {
    exportLogService = new DefaultExportLogServiceImpl();
    exportLogService.setPersistenceManager(persistenceManager);
  }

  //
  // Test Methods
  //

  @Test
  public void testSaveAnExportLog() {
    String entityType = "Participant";
    String entityIdentifier = "1234567";
    String destination = "DCC";
    Date startDate = DateBuilder.newBuilder().year(2009).month(1).day(1).build();
    Date endDate = DateBuilder.newBuilder().year(2009).month(1).day(2).build();
    Date exportDate = DateBuilder.newBuilder().year(2009).month(1).day(3).build();
    String user = "administrator";

    ExportLog exportLog = ExportLog.Builder.newLog().type(entityType).identifier(entityIdentifier).destination(destination).start(startDate).end(endDate).exportDate(exportDate).user(user).build();
    exportLogService.save(exportLog);

    List<ExportLog> exportLogs = exportLogService.getExportLogs(entityType, entityIdentifier, destination, true);
    assertNotNull(exportLogs);
    assertFalse(exportLogs.isEmpty());
    assertEquals(1, exportLogs.size());
    ExportLog persistedExportLog = exportLogs.get(0);
    assertEquals(entityType, persistedExportLog.getType());
    assertEquals(entityIdentifier, persistedExportLog.getIdentifier());
    assertEquals(startDate, persistedExportLog.getCaptureStartDate());
    assertEquals(endDate, persistedExportLog.getCaptureEndDate());
    assertEquals(exportDate, persistedExportLog.getExportDate());
    assertEquals(user, persistedExportLog.getUser());
  }

  //
  // Helper Classes / Methods
  //

  private static class DateBuilder {
    private int year;

    private int month;

    private int day;

    public static DateBuilder newBuilder() {
      return new DateBuilder();
    }

    public DateBuilder year(int year) {
      this.year = year;
      return this;
    }

    public DateBuilder month(int month) {
      this.month = month - 1; // zero-based
      return this;
    }

    public DateBuilder day(int day) {
      this.day = day;
      return this;
    }

    public Date build() {
      Calendar c = Calendar.getInstance();
      c.set(year, month, day);
      return c.getTime();
    }
  }
}
