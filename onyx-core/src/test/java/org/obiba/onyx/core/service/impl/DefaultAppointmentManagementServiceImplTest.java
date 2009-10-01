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

import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Date;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.obiba.core.service.PersistenceManager;
import org.obiba.core.test.spring.BaseDefaultSpringContextTestCase;
import org.obiba.core.test.spring.Dataset;
import org.obiba.onyx.core.domain.statistics.AppointmentUpdateStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 */
@Transactional
public class DefaultAppointmentManagementServiceImplTest extends BaseDefaultSpringContextTestCase {

  @Autowired(required = true)
  private PersistenceManager persistenceManager;

  private DefaultAppointmentManagementServiceImpl appointmentServiceImpl;

  @Before
  public void setUp() {
    appointmentServiceImpl = new DefaultAppointmentManagementServiceImpl();
    appointmentServiceImpl.setResourceLoader(new PathMatchingResourcePatternResolver());
    appointmentServiceImpl.setPersistenceManager(persistenceManager);
  }

  @Test
  public void testInitializeNoInputDirectoryString() {
    try {
      appointmentServiceImpl.initialize();
      fail("Should get IllegalArgumentException.");
    } catch(IllegalArgumentException e) {
      Assert.assertEquals("DefaultAppointmentManagementServiceImpl: InputDirectory should not be null", e.getMessage());
    }
  }

  @Test
  public void testInitializeEmptyInputDirectoryString() {
    appointmentServiceImpl.setInputDirectory("");

    try {
      appointmentServiceImpl.initialize();
      fail("Should get IllegalArgumentException.");
    } catch(IllegalArgumentException e) {
      Assert.assertEquals("DefaultAppointmentManagementServiceImpl: InputDirectory should not be null", e.getMessage());
    }
  }

  @Test
  public void testInitializeInputDirNotFound() {
    appointmentServiceImpl.setInputDirectory("TESTInputDir");

    try {
      appointmentServiceImpl.initialize();
      fail("Should get RuntimeException.");
    } catch(RuntimeException e) {
      Assert.assertEquals("DefaultAppointmentManagementServiceImpl: Failed to access directory - java.io.FileNotFoundException: class path resource [TESTInputDir] cannot be resolved to URL because it does not exist", e.getMessage());
    }
  }

  @Test
  public void testInitializeInputDir() {
    appointmentServiceImpl.setInputDirectory("file:./src/test/resources/appointments/in");
    appointmentServiceImpl.initialize();

    Assert.assertNotNull(appointmentServiceImpl.getInputDir());
    Assert.assertNull(appointmentServiceImpl.getOutputDir());
  }

  // @Test
  public void testUpdateAppointments() throws IllegalArgumentException, IOException {
    // TODO: Implement this test
  }

  @Test
  @Dataset
  public void testSaveAppointmentUpdateStats() {
    Date currentDate = new Date();
    AppointmentUpdateStats appointmentUpdateStats = new AppointmentUpdateStats(currentDate, 43, 2, 20, 3);
    appointmentUpdateStats.setFileName("testAppointmentFile");
    appointmentServiceImpl.saveAppointmentUpdateStats(appointmentUpdateStats);

    AppointmentUpdateStats persistedUpdateStats = persistenceManager.get(AppointmentUpdateStats.class, 1l);
    Assert.assertEquals(currentDate, persistedUpdateStats.getDate());
    Assert.assertEquals(43, (int) persistedUpdateStats.getAddedParticipants());
    Assert.assertEquals(2, (int) persistedUpdateStats.getUpdatedParticipants());
    Assert.assertEquals(20, (int) persistedUpdateStats.getIgnoredParticipants());
    Assert.assertEquals(3, (int) persistedUpdateStats.getUnreadableParticipants());
  }

}